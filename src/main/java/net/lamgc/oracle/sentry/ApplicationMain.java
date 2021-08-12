package net.lamgc.oracle.sentry;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import net.lamgc.oracle.sentry.script.ScriptComponent;
import net.lamgc.oracle.sentry.script.ScriptManager;
import net.lamgc.oracle.sentry.script.tools.http.ScriptHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

/**
 * <p> 程序入口
 * @author LamGC
 */
@SpringBootApplication
@Configuration
public class ApplicationMain {

    private final static Logger log = LoggerFactory.getLogger(ApplicationMain.class);

    @SuppressWarnings("AlibabaConstantFieldShouldBeUpperCase")
    private final static Object mainThreadWaiter = new Object();

    @Value("${oracle.identity.location:./identity/}")
    private String identityDirectory;
    @Value("${oracle.identity.pattern:.*\\.oracle.ini$}")
    private String identityFilePattern;
    @Value("${sentry.script.location:./scripts/}")
    private String scriptsLocation;
    @Value("${oracle.script.ssh.identityPath:./ssh.config.json}")
    private String sshIdentityPath;

    public static void main(String[] args) {
        SpringApplication.run(ApplicationMain.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread(mainThreadWaiter::notifyAll, "ShutdownMainThread"));
        synchronized (mainThreadWaiter) {
            try {
                mainThreadWaiter.wait();
            } catch (InterruptedException e) {
                log.warn("", e);
            }
        }
    }

    @Bean("oracle.identity.manager")
    public OracleIdentityManager initialOracleIdentityManager() throws IOException {
        OracleIdentityManager oracleUserManager = new OracleIdentityManager();
        log.info("正在加载 Oracle API 身份配置...");
        log.debug("Oracle API 身份配置查找路径: \"{}\", 匹配表达式: {}", identityDirectory, identityFilePattern);
        int loadedCount = oracleUserManager.loadFromDirectory(new File(identityDirectory), identityFilePattern);
        log.info("已加载 {} 个身份配置.", loadedCount);
        return oracleUserManager;
    }

    @Bean("oracle.compute.instance.manager")
    @Autowired
    public ComputeInstanceManager initialComputeInstanceManager(OracleIdentityManager identityManager) throws IOException {
        ComputeInstanceManager instanceManager = new ComputeInstanceManager();
        int addTotal = 0;
        for (AuthenticationDetailsProvider provider : identityManager.getProviders()) {
            String identityName = identityManager.getIdentityName(provider.getUserId());
            log.info("正在加载用户 {} 所拥有的所有实例...", identityName);
            int addCount;
            try {
                addCount = instanceManager.addComputeInstanceFromUser(provider);
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
    public ScriptManager initialScriptManager(ComputeInstanceManager instanceManager) {
        ScriptComponent context = new ScriptComponent(new ScriptHttpClient(HttpClientBuilder.create()
                .build()),
                instanceManager);

        ScriptManager manager = new ScriptManager(new File(scriptsLocation), context);
        manager.loadScripts();
        return manager;
    }

}
