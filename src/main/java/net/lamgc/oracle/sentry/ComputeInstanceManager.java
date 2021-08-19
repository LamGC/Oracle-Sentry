package net.lamgc.oracle.sentry;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.requests.ListInstancesRequest;
import com.oracle.bmc.core.responses.ListInstancesResponse;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import net.lamgc.oracle.sentry.oci.compute.ComputeInstance;
import net.lamgc.oracle.sentry.oci.compute.ssh.SshAuthIdentityProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 计算实例管理器.
 * @author LamGC
 */
public class ComputeInstanceManager {

    private final Map<String, ComputeInstance> instanceMap = new ConcurrentHashMap<>();
    private SshAuthIdentityProvider sshIdentityProvider;

    /**
     * 初始化 SSH 认证配置提供器.
     * @param sshIdentityJson SSH 认证配置文件.
     * @throws IOException 加载时如有异常将直接抛出.
     */
    public void initialSshIdentityProvider(File sshIdentityJson) throws IOException {
        sshIdentityProvider = new SshAuthIdentityProvider(this, sshIdentityJson);
        sshIdentityProvider.loadAuthInfo();
    }

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
                .filter(computeInstance -> computeInstance.getUserId().equals(userId))
                .collect(Collectors.toSet());
    }

    /**
     * 添加某一用户的所有计算实例.
     * @param provider 用户身份提供器.
     * @return 返回已成功添加的实例数量.
     * @throws NullPointerException 如果 provider 为 {@code null} 则抛出异常.
     */
    public int addComputeInstanceFromUser(AuthenticationDetailsProvider provider) {
        Objects.requireNonNull(provider);
        IdentityClient identityClient = new IdentityClient(provider);
        ComputeClient computeClient = new ComputeClient(provider);
        ListCompartmentsResponse listCompartments = identityClient.listCompartments(ListCompartmentsRequest.builder()
                .compartmentId(provider.getTenantId())
                .build());
        int addCount = 0;
        Set<String> compartmentIds = listCompartments.getItems().stream()
                .map(Compartment::getId).collect(Collectors.toSet());
        compartmentIds.add(provider.getTenantId());
        for (String compartmentId : compartmentIds) {
            ListInstancesResponse listInstances = computeClient.listInstances(ListInstancesRequest.builder()
                    .compartmentId(compartmentId)
                    .build());
            for (Instance instance : listInstances.getItems()) {
                if (instance.getLifecycleState() == Instance.LifecycleState.Terminated) {
                    continue;
                }
                ComputeInstance computeInstance = new ComputeInstance(this, instance.getId(),
                        provider.getUserId(), compartmentId, instance.getImageId(), provider);

                addComputeInstance(computeInstance);
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
