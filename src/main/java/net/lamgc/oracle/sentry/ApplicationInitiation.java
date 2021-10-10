package net.lamgc.oracle.sentry;

import com.google.common.base.Throwables;
import net.lamgc.oracle.sentry.oci.account.OracleAccount;
import net.lamgc.oracle.sentry.oci.account.OracleAccountManager;
import net.lamgc.oracle.sentry.oci.compute.ComputeInstanceManager;
import net.lamgc.oracle.sentry.script.ScriptComponentExtension;
import net.lamgc.oracle.sentry.script.ScriptComponents;
import net.lamgc.oracle.sentry.script.ScriptManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ServiceLoader;

/**
 * @author LamGC
 */
@Configuration
class ApplicationInitiation {

    private final static Logger log = LoggerFactory.getLogger(ApplicationInitiation.class);

    @Value("${oracle.identity.location}")
    @NonNull
    private String identityDirectory;

    @Value("${oracle.identity.pattern}")
    @NonNull
    private String identityFilePattern;

    @Value("${oracle.script.location}")
    @NonNull
    private String scriptsLocation;

    @Value("${oracle.ssh.identityPath}")
    @NonNull
    private String sshIdentityPath;

    @Bean("oracle.identity.manager")
    public OracleAccountManager initialOracleAccountManager() throws IOException {
        OracleAccountManager oracleUserManager = new OracleAccountManager();
        log.info("正在加载 Oracle API 身份配置...");
        log.debug("Oracle API 身份配置查找路径: \"{}\", 匹配表达式: {}", identityDirectory, identityFilePattern);
        File identityDir = new File(identityDirectory);
        if (!identityDir.exists()) {
            if (identityDir.mkdirs()) {
                log.warn("身份配置文件夹不存在, 已创建该文件夹, 请将身份配置放入该文件夹后重新启动程序.");
            } else {
                log.error("身份配置文件夹创建失败, 请手动在运行目录下创建 'identity' 文件夹, 并将身份配置放入该文件夹中, 重新启动程序.");
            }
            System.exit(1);
        }
        int loadedCount = oracleUserManager.loadFromDirectory(identityDir, identityFilePattern);
        log.info("已加载 {} 个身份配置.", loadedCount);
        return oracleUserManager;
    }

    @Bean("oracle.compute.instance.manager")
    @Autowired
    public ComputeInstanceManager initialComputeInstanceManager(OracleAccountManager accountManager) throws IOException {
        ComputeInstanceManager instanceManager = new ComputeInstanceManager();
        int addTotal = 0;
        for (OracleAccount account : accountManager.getAccounts()) {
            String identityName = account.name();
            log.info("正在加载用户 {} 所拥有的所有实例...", identityName);
            int addCount;
            try {
                addCount = instanceManager.addComputeInstanceFromUser(account);
            } catch (Exception e) {
                log.error("加载实例时发生异常.", e);
                continue;
            }
            log.info("用户 {} 已添加 {} 个计算实例.", identityName, addCount);
            addTotal += addCount;
        }

        log.info("正在初始化 SSH 认证配置提供器...");
        instanceManager.initialSshIdentityProvider(new File(sshIdentityPath));
        log.info("已完成 ComputeInstanceManager 初始化, 共加载了 {} 个计算实例.", addTotal);
        return instanceManager;
    }

    @Bean("sentry.script.manager")
    @Autowired
    public ScriptManager initialScriptManager(ComputeInstanceManager instanceManager, OracleAccountManager accountManager) {
        ScriptComponents components = new ScriptComponents();
        components.addComponentObject("InstanceManager", instanceManager);
        components.addComponentObject("AccountManager", accountManager);

        configureScriptComponentsFromExtension(components);

        ScriptManager manager = new ScriptManager(new File(scriptsLocation), components);
        manager.loadScripts();
        return manager;
    }

    private void configureScriptComponentsFromExtension(ScriptComponents components) {
        ServiceLoader<ScriptComponentExtension> extensions = ServiceLoader.load(ScriptComponentExtension.class);
        for (ScriptComponentExtension extension : extensions) {
            try {
                extension.configureScriptComponents(components);
            } catch (Exception e) {
                log.error("脚本组件扩展配置组件时发生未捕获异常.(Extension: {})\n{}",
                        extension.getClass().getName(), Throwables.getStackTraceAsString(e));
            }
        }
    }

    @PostConstruct
    @Order(1)
    @SuppressWarnings({"MismatchedReadAndWriteOfArray", "RedundantOperationOnEmptyContainer"})
    private void initialEnvironment() throws IOException {
        String[] directors = new String[] {
                "./config",
                identityDirectory,
                scriptsLocation
        };

        String[] files = new String[] {

        };

        for (String directory : directors) {
            File dir = new File(directory);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    log.error("文件夹 {} 创建失败.", dir.getCanonicalPath());

                }
            }
        }

        for (String file : files) {
            File dir = new File(file);
            if (!dir.exists()) {
                if (!dir.createNewFile()) {
                    log.error("文件 {} 创建失败.", dir.getCanonicalPath());

                }
            }
        }
        log.debug("目录检查完成.");
    }

    @Autowired
    private void initialConfigurationFile(ApplicationContext context) {
        File configFile = new File("config/application.yml");
        if (!configFile.exists()) {
            Resource resource = context.getResource("application.yml");
            if (!resource.exists()) {
                log.error("默认配置初始化失败(包内资源不存在).");
                return;
            }

            try {
                Files.copy(resource.getInputStream(), configFile.toPath());
                log.info("默认配置文件已初始化完成, 如果调整配置, 可修改配置文件中的相应配置项.");
            } catch (IOException e) {
                log.error("初始化默认配置文件失败!(Path: {})\n{}",
                        configFile.getAbsolutePath(), Throwables.getStackTraceAsString(e));
            }
        } else {
            log.debug("配置文件存在, 无需初始化.");
        }

    }

}
