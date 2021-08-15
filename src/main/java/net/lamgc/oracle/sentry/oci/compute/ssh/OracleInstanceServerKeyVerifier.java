package net.lamgc.oracle.sentry.oci.compute.ssh;

import net.lamgc.oracle.sentry.Constants;
import net.lamgc.oracle.sentry.oci.compute.ComputeInstance;
import org.apache.sshd.client.keyverifier.RequiredServerKeyVerifier;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.security.PublicKey;
import java.util.Scanner;

/**
 * @author LamGC
 */
public class OracleInstanceServerKeyVerifier implements ServerKeyVerifier {

    private final static Logger log = LoggerFactory.getLogger(OracleInstanceServerKeyVerifier.class);

    private final ComputeInstance instance;
    private final SshAuthInfo info;

    public OracleInstanceServerKeyVerifier(ComputeInstance instance, SshAuthInfo info) {
        this.instance = instance;
        this.info = info;
    }

    @Override
    public boolean verifyServerKey(ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey) {
        if (info.getServerKey() != null) {
            return new RequiredServerKeyVerifier(info.getServerKey())
                    .verifyServerKey(clientSession, remoteAddress, serverKey);
        } else {
            log.warn("首次连接实例 SSH, 需要用户确认服务器公钥是否可信...");
            boolean result = usePolicyConfirm(remoteAddress, serverKey);
            if (result) {
                log.info("用户已确认服务器密钥可信, 将该密钥列入该实例下的信任密钥.");
                info.setServerKey(serverKey);
                return true;
            } else {
                log.warn("用户已确认该密钥不可信, 拒绝本次连接.");
                return false;
            }
        }
    }

    private boolean usePolicyConfirm(SocketAddress address, PublicKey serverKey) {
        String policyName = Constants.instance.getFirstConnectionPolicy().toUpperCase();
        FirstConnectionPolicy policy;
        try {
            policy = FirstConnectionPolicy.valueOf(policyName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported policy: " + policyName);
        }
        return policy.confirmFunction.confirm(this.instance, address, serverKey);
    }

    @SuppressWarnings("unused")
    private enum FirstConnectionPolicy {
        ACCEPT((instance, address, key) -> true),
        REJECT((instance, address, key) -> false),
        CONFIRM((instance, address, key) -> {
            String fingerPrint = KeyUtils.getFingerPrint(key);
            log.warn("开始密钥认证流程... (InstanceId: {}, ServerAddress: {}, KeyFingerPrint: {})",
                    instance.getInstanceId(), address, fingerPrint);
            Scanner scanner = new Scanner(System.in);
            log.info("""
                    本次连接 SSH 为首次连接, 为确保 SSH 安全性，请通过可信渠道获取服务器密钥指纹, 并与下列指纹比对:
                    实例 ID：{}
                    实例名称：{}
                    连接地址：{}
                    密钥指纹：
                    
                                {}
                                
                    以上密钥指纹是否与服务器密钥指纹相同？如果指纹相同，对该密钥可信，请输入“Yes”，否则输入任意内容拒绝连接。
                    该密钥是否可信？（Yes/No）：""",
                    instance.getInstanceId(),
                    instance.getInstanceName(),
                    address,
                    fingerPrint
            );

            do {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    return "yes".trim().equalsIgnoreCase(input);
                }
            } while (true);
        });

        private final PublicKeyConfirm confirmFunction;


        FirstConnectionPolicy(PublicKeyConfirm confirmFunction) {
            this.confirmFunction = confirmFunction;
        }
    }

    @FunctionalInterface
    private interface PublicKeyConfirm {

        /**
         * 确认密钥.
         * @param instance 待认证的服务器所属计算实例.
         * @param address 远程地址.
         * @param serverKey 服务器密钥.
         * @return 如果通过, 返回 {@code true}.
         */
        boolean confirm(ComputeInstance instance, SocketAddress address, PublicKey serverKey);

    }

}
