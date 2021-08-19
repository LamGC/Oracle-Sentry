package net.lamgc.oracle.sentry.oci.compute;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.requests.GetImageRequest;
import com.oracle.bmc.core.requests.GetInstanceRequest;
import com.oracle.bmc.core.requests.InstanceActionRequest;
import com.oracle.bmc.core.responses.GetImageResponse;
import com.oracle.bmc.core.responses.GetInstanceResponse;
import com.oracle.bmc.core.responses.InstanceActionResponse;
import net.lamgc.oracle.sentry.ComputeInstanceManager;
import net.lamgc.oracle.sentry.oci.compute.ssh.InstanceSsh;
import net.lamgc.oracle.sentry.oci.compute.ssh.SshAuthInfo;

import java.util.Objects;

/**
 * 计算实例.
 * @author LamGC
 */
public final class ComputeInstance {

    private final ComputeInstanceManager instanceManager;

    private final String instanceId;
    private final String userId;
    private final String compartmentId;
    private final String imageId;
    private final AuthenticationDetailsProvider authProvider;
    private final InstanceNetwork network;

    private final ComputeClient computeClient;

    /**
     * 构造一个计算实例对象.
     * @param instanceManager 实例所属的管理器.
     * @param instanceId 实例 Id.
     * @param userId 所属用户 Id.
     * @param compartmentId 实例所在区域的 Id.
     * @param imageId 镜像 Id.
     * @param provider 所属用户的身份配置提供器.
     */
    public ComputeInstance(ComputeInstanceManager instanceManager, String instanceId, String userId,
                           String compartmentId, String imageId, AuthenticationDetailsProvider provider) {
        this.instanceManager = instanceManager;
        this.instanceId = instanceId;
        this.userId = userId;
        this.compartmentId = compartmentId;
        this.imageId = imageId;
        this.authProvider = provider;

        computeClient = new ComputeClient(provider);
        this.network = new InstanceNetwork(this);
    }

    /**
     * 获取实例 Id.
     * <p> 可通过实例 Id 直接调用 Oracle Cloud SDK, 也可以作为服务器的唯一标识.
     * @return 返回实例 Id.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * 获取所属用户的 Id.
     * @return 返回用户 Id.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取服务器所属区域的 Id.
     * <p> 使用的资源必须要处于同一区域, 例如 IP 资源, 磁盘.
     * @return 返回服务器所属区域的 Id.
     */
    public String getCompartmentId() {
        return compartmentId;
    }

    /**
     * 获取并返回实例镜像信息.
     * <p> 可获取系统信息.
     * <p> 如果实例被 dd, 则本信息不准确.
     * @return 返回实例信息.
     */
    public BootImage getImage() {
        GetImageResponse image = computeClient.getImage(GetImageRequest.builder()
                .imageId(imageId)
                .build());
        return new BootImage(image.getImage());
    }

    /**
     * 获取实例网络对象.
     * <p> 可通过 InstanceNetwork 操作实例网络相关.
     * @return 返回 InstanceNetwork.
     */
    public InstanceNetwork network() {
        return network;
    }

    /**
     * 获取实例的 SSH 客户端.
     * @return 返回实例 SSH 客户端.
     */
    public InstanceSsh ssh() {
        String instanceState = getInstanceState();
        if (!Instance.LifecycleState.Running.name().equals(instanceState)) {
            throw new IllegalStateException("The state of the current instance cannot connect to SSH: " + instanceState);
        }
        return new InstanceSsh(this, getSshIdentity());
    }

    /**
     * 获取实例当前状态.
     * <p> 实例可有以下状态:
     *      <ul>
     *          <li> Moving: 实例正在转移中;
     *          <li> Provisioning: 实例正在预分配中(正在创建实例);
     *          <li> Running: 实例正在运行中;
     *          <li> Starting: 实例正在启动中;
     *          <li> Stopping: 实例正在停止中;
     *          <li> Stopped: 实例已停止运行;
     *          <li> CreatingImage: 正在通过实例构建镜像;
     *          <li> Terminating: 正在终止实例(正在删除实例);
     *          <li> Terminated: 实例已经终止(已删除实例)
     *      </ul>
     * @return 返回实例状态.
     */
    public String getInstanceState() {
        GetInstanceResponse instance = computeClient.getInstance(GetInstanceRequest.builder()
                .instanceId(instanceId)
                .build());

        return instance.getInstance().getLifecycleState().name();
    }

    /**
     * 对实例执行操作.
     * @param action 操作类型.
     * @return 如果成功, 返回实例最新状态(返回值意义见 {@link #getInstanceState()} 文档).
     */
    public String execAction(InstanceAction action) {
        InstanceActionResponse actionResponse = computeClient.instanceAction(InstanceActionRequest.builder()
                .instanceId(instanceId)
                .action(action.getActionValue())
                .build());
        return actionResponse.getInstance().getLifecycleState().name();
    }

    /**
     * 获取实例名称.
     * @return 返回实例显示名.
     */
    public String getInstanceName() {
        GetInstanceResponse instance = computeClient.getInstance(GetInstanceRequest.builder()
                .instanceId(instanceId)
                .build());
        return instance.getInstance().getDisplayName();
    }

    /**
     * 获得 OCI 的计算 API 客户端, 可通过该客户端执行更多的操作.
     * @return 返回计算 API 客户端.
     */
    public ComputeClient getComputeClient() {
        return computeClient;
    }

    AuthenticationDetailsProvider getAuthProvider() {
        return authProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComputeInstance that = (ComputeInstance) o;
        return instanceId.equals(that.instanceId) && userId.equals(that.userId) && compartmentId.equals(that.compartmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId, userId, compartmentId);
    }

    /**
     * 获取 SSH 认证信息.
     * @return 返回实例 SSH 认证信息.
     * @throws java.util.NoSuchElementException 如果没有指定配置信息则抛出该异常.
     */
    private SshAuthInfo getSshIdentity() {
        return instanceManager.getSshIdentityProvider()
                .getAuthInfoByInstanceId(instanceId);
    }

}
