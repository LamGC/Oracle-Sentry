package net.lamgc.oracle.sentry.oci.compute;

import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.Vnic;
import com.oracle.bmc.core.model.VnicAttachment;
import com.oracle.bmc.core.requests.GetVnicRequest;
import com.oracle.bmc.core.requests.ListVnicAttachmentsRequest;
import com.oracle.bmc.core.responses.GetVnicResponse;
import com.oracle.bmc.core.responses.ListVnicAttachmentsResponse;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 实例网络操作类.
 * @author LamGC
 */
public class InstanceNetwork {

    private final ComputeInstance instance;
    private final VirtualNetworkClient vcnClient;

    InstanceNetwork(ComputeInstance instance) {
        this.instance = instance;
        this.vcnClient = instance.getFromAccount().clients().network();
    }

    /**
     * 获取实例的所有公共 IP.
     * @return 返回所有公共 IP.
     * @throws NoSuchElementException 当 InstanceId 所属实例未添加时抛出该异常.
     * @throws NullPointerException 当 instanceId 为 {@code null} 时抛出该异常.
     */
    public Set<String> getInstancePublicIp() {
        Set<String> publicIpSet = new HashSet<>();
        for (VnicAttachment vnicAttachment : listVnicAttachments()) {
            GetVnicResponse vnic = vcnClient.getVnic(GetVnicRequest.builder()
                    .vnicId(vnicAttachment.getVnicId())
                    .build());
            publicIpSet.add(vnic.getVnic().getPublicIp());
        }

        return publicIpSet;
    }

    /**
     * 获取所有已连接的 Vnic 信息.
     * @return 返回所有已连接的 Vnic.
     */
    public List<VnicAttachment> listVnicAttachments() {
        ListVnicAttachmentsResponse listVnicAttachments = instance.getComputeClient()
                .listVnicAttachments(ListVnicAttachmentsRequest.builder()
                        .compartmentId(instance.getCompartmentId())
                        .instanceId(instance.getInstanceId())
                        .build()
                );

        return listVnicAttachments.getItems();
    }

    /**
     * 获取实例的主要 VNIC(虚拟网络接口)
     * @return 返回实例的主要 VNIC 对象.
     * @throws NoSuchElementException 当找不到主要 VNIC 时抛出该异常.
     */
    public Vnic getPrimaryVnic() {
        for (VnicAttachment vnicAttachment : listVnicAttachments()) {
            GetVnicResponse vnic = vcnClient.getVnic(GetVnicRequest.builder()
                    .vnicId(vnicAttachment.getVnicId())
                    .build());
            if (vnic.getVnic().getIsPrimary()) {
                return vnic.getVnic();
            }
        }
        throw new NoSuchElementException("Primary vnic not found.");
    }

}
