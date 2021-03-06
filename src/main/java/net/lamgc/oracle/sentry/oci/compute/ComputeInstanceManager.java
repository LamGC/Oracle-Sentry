package net.lamgc.oracle.sentry.oci.compute;

import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.requests.ListInstancesRequest;
import com.oracle.bmc.core.responses.ListInstancesResponse;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import net.lamgc.oracle.sentry.oci.account.OracleAccount;
import net.lamgc.oracle.sentry.oci.compute.ssh.SshAuthIdentityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 计算实例管理器.
 * @author LamGC
 */
public final class ComputeInstanceManager {

    private final static Logger log = LoggerFactory.getLogger(ComputeInstanceManager.class);

    private final Map<String, ComputeInstance> instanceMap = new ConcurrentHashMap<>();
    private SshAuthIdentityProvider sshIdentityProvider;

    /**
     * 初始化 SSH 认证配置提供器.
     * @param sshIdentityJson SSH 认证配置文件.
     * @throws IOException 加载时如有异常将直接抛出.
     */
    public void initialSshIdentityProvider(File sshIdentityJson) throws IOException {
        log.debug("正在初始化 SSH 认证配置提供器...");
        sshIdentityProvider = new SshAuthIdentityProvider(this, sshIdentityJson);
        sshIdentityProvider.loadAuthInfo();
        log.debug("SSH 认证配置提供器已初始化完成.");
    }

    /**
     * 获取实例 SSH 认证配置提供器.
     * @return 返回 SSH 认证配置提供器.
     */
    public SshAuthIdentityProvider getSshIdentityProvider() {
        return sshIdentityProvider;
    }

    /**
     * 单独添加一个计算实例对象.
     * @param instance 计算实例对象.
     * @throws NullPointerException 当 instance 为 {@code null} 时抛出异常.
     */
    public void addComputeInstance(ComputeInstance instance) {
        Objects.requireNonNull(instance);
        instanceMap.put(instance.getInstanceId(), instance);
    }

    /**
     * 获取某一用户的所有已添加实例.
     * @param userId 用户 Id.
     * @return 返回该用户所拥有的的所有已添加实例.
     * @throws NullPointerException 当 userId 为 {@code null} 时抛出异常.
     */
    public Set<ComputeInstance> getInstancesByUserId(String userId) {
        Objects.requireNonNull(userId);
        return instanceMap.values().stream()
                .filter(computeInstance -> computeInstance.getFromAccount().id().equals(userId))
                .collect(Collectors.toSet());
    }

    /**
     * 添加某一用户的所有计算实例.
     * @param account Oracle 云账号对象.
     * @return 返回已成功添加的实例数量.
     * @throws NullPointerException 如果 provider 为 {@code null} 则抛出异常.
     */
    public int addComputeInstanceFromUser(OracleAccount account) {
        Objects.requireNonNull(account);
        ListCompartmentsResponse listCompartments = account.clients().identity()
                .listCompartments(ListCompartmentsRequest.builder()
                    .compartmentId(account.tenantId())
                    .build());
        int addCount = 0;
        Set<String> compartmentIds = listCompartments.getItems().stream()
                .map(Compartment::getId).collect(Collectors.toSet());
        compartmentIds.add(account.tenantId());
        for (String compartmentId : compartmentIds) {
            ListInstancesResponse listInstances = account.clients().compute()
                    .listInstances(ListInstancesRequest.builder()
                        .compartmentId(compartmentId)
                        .build());
            for (Instance instance : listInstances.getItems()) {
                if (instance.getLifecycleState() == Instance.LifecycleState.Terminated ||
                        instance.getLifecycleState() == Instance.LifecycleState.Terminating) {
                    log.debug("实例 {} 状态为 {}, 不添加该实例.", instance.getId(), instance.getLifecycleState());
                    continue;
                }
                ComputeInstance computeInstance = new ComputeInstance(this, instance.getId(),
                        compartmentId, instance.getImageId(), account);
                addComputeInstance(computeInstance);
                log.debug("已为用户 {} 添加计算实例: {}", account.id(), instance.getId());
                addCount ++;
            }
        }

        return addCount;
    }

    /**
     * 通过实例 Id 获取计算实例对象.
     * @param instanceId 实例 Id.
     * @return 返回计算实例对象.
     * @throws NullPointerException 当 instanceId 为 {@code null} 时抛出异常.
     * @throws NoSuchElementException 当未找到指定计算实例时抛出该异常.
     */
    public ComputeInstance getComputeInstanceById(String instanceId) {
        Objects.requireNonNull(instanceId);
        if (!instanceMap.containsKey(instanceId)) {
            throw new NoSuchElementException(instanceId);
        }
        return instanceMap.get(instanceId);
    }

    /**
     * 获取所有计算实例.
     * @return 返回所有已添加的计算实例.
     */
    public Set<ComputeInstance> getComputeInstances() {
        return instanceMap.values().stream().collect(Collectors.toUnmodifiableSet());
    }

}
