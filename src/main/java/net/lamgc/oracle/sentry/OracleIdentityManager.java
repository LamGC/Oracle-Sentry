package net.lamgc.oracle.sentry;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.requests.GetUserRequest;
import com.oracle.bmc.identity.responses.GetUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Oracle 身份管理器.
 * @author LamGC
 */
public final class OracleIdentityManager {

    private final static Logger log = LoggerFactory.getLogger(OracleIdentityManager.class);

    /**
     * 认证身份 Map.
     * Key: Identity Id
     * Value {@link AuthenticationDetailsProvider}
     */
    private final Map<String, AuthenticationDetailsProvider> identityMap = new ConcurrentHashMap<>();
    /**
     * 用户名 Map.
     * key Identity Id
     * Value: Username
     */
    private final Map<String, String> identityNameMap = new ConcurrentHashMap<>();

    /**
     * 从目录扫描匹配的配置文件并加载.
     * @param directory 待扫描的目录.
     * @param pattern 文件匹配规则(正则表达式).
     * @throws IOException 当加载发生异常时将抛出该异常.
     * @return 返回成功加载的身份配置数量.
     */
    public int loadFromDirectory(File directory, String pattern) throws IOException {
        if (!directory.exists()) {
            throw new FileNotFoundException(directory.getCanonicalPath());
        } else if (!directory.isDirectory()) {
            throw new IOException("The specified path is not a folder");
        }

        File[] configFiles = directory.listFiles(file -> file.isFile() && file.getName().matches(pattern));
        if (configFiles == null) {
            throw new IOException("Unable to access the specified directory: " + directory.getCanonicalPath());
        }
        int loadedCount = 0;
        for (File configFile : configFiles) {
            try {
                AuthenticationDetailsProvider provider = loadFromConfigFile(configFile);
                if (provider == null) {
                    continue;
                }
                loadedCount ++;
                log.info("已成功加载身份配置文件.\n\tUserId: {}\n\tUsername: {}\n\tPath: {}",
                        provider.getUserId(),
                        getIdentityName(provider.getUserId()),
                        configFile.getCanonicalPath());
            } catch (Exception e) {
                log.error("加载身份配置文件时发生异常.(Path: {})\n{}", configFile.getCanonicalPath(), Throwables.getStackTraceAsString(e));
            }
        }
        return loadedCount;
    }

    /**
     * 通过配置文件加载身份信息.
     * @param identityConfig 身份信息文件.
     * @throws IOException 如果读取文件发生问题时将抛出该异常.
     */
    public AuthenticationDetailsProvider loadFromConfigFile(File identityConfig) throws IOException {
        if (!identityConfig.exists()) {
            throw new FileNotFoundException(identityConfig.getAbsolutePath());
        }

        ConfigFileReader.ConfigFile config
                = ConfigFileReader.parse(identityConfig.getAbsolutePath());
        if (!checkIdentityProfileConfig(config)) {
            log.warn("该配置文件缺少必要信息, 跳过加载.(Path: {})", identityConfig.getCanonicalPath());
            return null;
        }


        String keyFilePath = config.get("key_file");
        if (keyFilePath.startsWith(".")) {
            keyFilePath = new File(identityConfig.getParent(), config.get("key_file")).getCanonicalPath();
        }
        Supplier<InputStream> privateKeySupplier
                = new SimplePrivateKeySupplier(keyFilePath);

        AuthenticationDetailsProvider provider
                = SimpleAuthenticationDetailsProvider.builder()
                .region(Region.fromRegionCode(config.get("region")))
                .tenantId(config.get("tenancy"))
                .userId(config.get("user"))
                .fingerprint(config.get("fingerprint"))
                .privateKeySupplier(privateKeySupplier::get)
                .build();

        // 尝试获取身份所属用户名, 以此检查该身份配置是否正确.
        String identityName = getIdentityName0(provider);
        identityNameMap.put(provider.getUserId(), identityName);
        identityMap.put(provider.getUserId(), provider);
        return provider;
    }

    private boolean checkIdentityProfileConfig(ConfigFileReader.ConfigFile config) {
        String[] fields = new String[] {
                "key_file",
                "region",
                "tenancy",
                "user",
                "fingerprint"
        };
        for (String field : fields) {
            if (Strings.isNullOrEmpty(config.get(field))) {
                return false;
            }
        }
        return true;
    }


    /**
     * 获取身份所属用户的名称.
     * @param provider 身份提供器.
     * @return 返回用户名.
     */
    private String getIdentityName0(AuthenticationDetailsProvider provider) {
        IdentityClient identityClient = new IdentityClient(provider);
        GetUserResponse user = identityClient.getUser(GetUserRequest.builder()
                .userId(provider.getUserId())
                .build());
        return user.getUser().getName();
    }

    /**
     * 获取身份信息所属的用户名.
     * @param userId 身份信息所属的用户 Id.
     * @return 返回用户名.
     * @throws NullPointerException 当 userId 为 {@code null} 时抛出该异常.
     * @throws NoSuchElementException 指定的 UserId 未找到对应用户名时抛出该异常.
     */
    public String getIdentityName(String userId) {
        Objects.requireNonNull(userId);
        if (!identityMap.containsKey(userId)) {
            throw new NoSuchElementException(userId);
        }
        return identityNameMap.get(userId);
    }

    /**
     * 通过 UserId 获取指定身份提供器.
     * @param userId 用户 Id.
     * @return 返回身份提供器.
     * @throws NullPointerException 当 userId 为 {@code null} 时抛出该异常.
     * @throws NoSuchElementException 指定的 UserId 未找到对应 Provider 时抛出该异常.
     */
    public AuthenticationDetailsProvider getProviderByUserId(String userId) {
        Objects.requireNonNull(userId);
        if (!identityMap.containsKey(userId)) {
            throw new NoSuchElementException(userId);
        }
        return identityMap.get(userId);
    }

    /**
     * 导出身份信息.
     * <p> 不包含私钥.
     * @return 返回 Json 形式的身份信息数组.
     */
    public JsonArray exportIdentityInfo() {
        JsonArray identityInfoArray = new JsonArray(identityMap.size());
        for (AuthenticationDetailsProvider provider : identityMap.values()) {
            JsonObject identity = new JsonObject();
            identity.addProperty("UserId", provider.getUserId());
            identity.addProperty("TenantId", provider.getTenantId());
            identity.addProperty("Fingerprint", provider.getFingerprint());
            identityInfoArray.add(identity);
        }
        return identityInfoArray;
    }

    /**
     * 获取所有身份提供器.
     * @return 返回包含所有身份提供器的集合对象.
     */
    public Set<AuthenticationDetailsProvider> getProviders() {
        return identityMap.values().stream().collect(Collectors.toUnmodifiableSet());
    }

}
