package net.lamgc.oracle.sentry.oci.compute;

import com.oracle.bmc.core.model.Image;

import java.util.Date;

/**
 * 引导镜像.
 * <p> 创建实例时所指定的引导镜像.
 * <p> 如果实例经过其他方式重新安装了系统, 则本信息有偏差.
 * @author LamGC
 */
public final class BootImage {

    private final Image image;

    BootImage(Image image) {
        this.image = image;
    }

    /**
     * 获取镜像 Id.
     * <p> 该 Id 可在创建服务器时指定所使用的系统镜像.
     * @return 返回镜像在 Oracle 的 Id.
     */
    public String getImageId() {
        return image.getId();
    }

    /**
     * 获取镜像所在的区域 Id.
     * @return 返回镜像所在区域的 Id.
     */
    public String getCompartmentId() {
        return image.getCompartmentId();
    }

    /**
     * 获取镜像系统名称.
     * <p> 比如 Ubuntu 或者说 CentOS.
     * @return 返回系统名称(不是计算机名称).
     */
    public String getOS() {
        return image.getOperatingSystem();
    }

    /**
     * 获取该镜像基于某一镜像的 Id.
     * <p> Oracle 提供了方法, 可以通过当前服务器生成新的镜像,
     * 生成后, 新的镜像就是基于原镜像生成, 该项就不为空.
     * @return 如果存在, 返回基础镜像 Id, 无基础镜像则返回 {@code null}.
     */
    public String getBaseImageId() {
        return image.getBaseImageId();
    }

    /**
     * 镜像的显示名称.
     * <p> 获取镜像显示名, 该名称与该镜像系统在官方为 iso 的命名差不多.
     * @return 获取镜像的显示名称.
     */
    public String getName() {
        return image.getDisplayName();
    }

    /**
     * 获取镜像大小.
     * @return 返回镜像大小, 单位为 MiB.
     */
    public Long getSize() {
        return image.getSizeInMBs();
    }

    /**
     * 获取系统版本号.
     * @return 返回镜像内系统的版本号, 如果版本较旧且服务器更新过系统, 则版本号不是最新的.
     */
    public String getOSVersion() {
        return image.getOperatingSystemVersion();
    }

    /**
     * 获取镜像创建时间.
     * @return 获取镜像创建时间.
     */
    public Date getTimeCreated() {
        return image.getTimeCreated();
    }

}
