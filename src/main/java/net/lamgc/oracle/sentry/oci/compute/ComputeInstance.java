package net.lamgc.oracle.sentry.oci.compute;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.model.Image;
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

    public String getInstanceId() {
        return instanceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCompartmentId() {
        return compartmentId;
    }

    public String getImageId() {
        return imageId;
    }

    /**
     * 获取并返回实例镜像信息.
     * <p> 可获取系统信息.
     * <p> 如果实例被 dd, 则本信息不准确.
     * @return 返回实例信息.
     */
    public Image getImage() {
        GetImageResponse image = computeClient.getImage(GetImageRequest.builder()
                .imageId(imageId)
                .build());
        return image.getImage();
    }

    /**
     * 获取实例网络对象.
     * <p> 可通过 InstanceNetwork 操作实例网络相关.
     * @return 返回 InstanceNetwork.
     */
    public InstanceNetwork network() {
        return network;
    }

    public InstanceSsh ssh() {
        Instance.LifecycleState instanceState = getInstanceState();
        if (instanceState != Instance.LifecycleState.Running) {
            throw new IllegalStateException("The state of the current instance cannot connect to SSH: " + instanceState);
        }
        return new InstanceSsh(this, getSshIdentity());
    }

    public Instance.LifecycleState getInstanceState() {
        GetInstanceResponse instance = computeClient.getInstance(GetInstanceRequest.builder()
                .instanceId(instanceId)
                .build());

        return instance.getInstance().getLifecycleState();
    }

    /**
     * 对实例执行操作.
     * @param action 操作类型.
     * @return 如果成功, 返回实例最新状态.
     */
    public Instance.LifecycleState execAction(InstanceAction action) {
        InstanceActionResponse actionResponse = computeClient.instanceAction(InstanceActionRequest.builder()
                .instanceId(instanceId)
                .action(action.getActionValue())
                .build());
        return actionResponse.getInstance().getLifecycleState();
    }

    /**
     * 获取实例名称.
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
     */
    private SshAuthInfo getSshIdentity() {
        return instanceManager.getSshIdentityProvider()
                .getAuthInfoByInstanceId(instanceId);
    }

}
