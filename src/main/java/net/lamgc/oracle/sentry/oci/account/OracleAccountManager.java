package net.lamgc.oracle.sentry.oci.account;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
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
 * Oracle 云账号管理器.
 * @author LamGC
 */
public final class OracleAccountManager {

    private final static Logger log = LoggerFactory.getLogger(OracleAccountManager.class);

    /**
     * 认证身份 Map.
     * Key: Identity Id
     * Value {@link AuthenticationDetailsProvider}
     */
    private final Map<String, OracleAccount> accountMap = new ConcurrentHashMap<>();

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
                OracleAccount account = loadFromConfigFile(configFile);
                if (account == null) {
                    continue;
                }
                loadedCount ++;
                log.info("已成功加载身份配置文件.\n\tUserId: {}\n\tUsername: {}\n\tPath: {}",
                        account.id(),
                        account.name(),
                        configFile.getCanonicalPath());
            } catch (Exception e) {
                log.error("加载身份配置文件时发生异常.(Path: {})\n{}", configFile.getCanonicalPath(), Throwables.getStackTraceAsString(e));
            }
        }
        return loadedCount;
    }

    /**
     * 通过配置文件加载身份信息.
     * <p> 加载成功后, 将会注册到身份管理器中.
     * @param identityConfig 身份信息文件.
     * @return 返回已成功加载后, 配置文件对应的身份配置提供器.
     * @throws IOException 如果读取文件发生问题时将抛出该异常.
     */
    public OracleAccount loadFromConfigFile(File identityConfig) throws IOException {
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
        OracleAccount oracleAccount = new OracleAccount(provider);
        String accountName = oracleAccount.name();
        if (accountName == null) {
            throw new NullPointerException("Failed to obtain the account name. The identity configuration may be incorrect.");
        }
        log.debug("已成功通过身份配置获取用户名称: {}", accountName);
        accountMap.put(oracleAccount.id(), oracleAccount);
        return oracleAccount;
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
     * 通过 UserId 获取指定身份提供器.
     * @param userId 用户 Id.
     * @return 返回身份提供器.
     * @throws NullPointerException 当 userId 为 {@code null} 时抛出该异常.
     * @throws NoSuchElementException 指定的 UserId 未找到对应 Provider 时抛出该异常.
     */
    public OracleAccount getAccountByUserId(String userId) {
        Objects.requireNonNull(userId);
        if (!accountMap.containsKey(userId)) {
            throw new NoSuchElementException(userId);
        }
        return accountMap.get(userId);
    }

    /**
     * 获取所有身份提供器.
     * @return 返回包含所有身份提供器的集合对象.
     */
    public Set<OracleAccount> getAccounts() {
        return accountMap.values().stream().collect(Collectors.toUnmodifiableSet());
    }

}
