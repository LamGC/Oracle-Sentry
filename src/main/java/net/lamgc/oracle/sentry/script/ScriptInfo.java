package net.lamgc.oracle.sentry.script;

import java.util.Objects;

/**
 * 脚本信息.
 * <p> 脚本信息的 Group, Name 和 Version 遵循 Java 依赖管理的 GAV 坐标规则。
 * @author LamGC
 */
public class ScriptInfo {

    private String group;
    private String name;
    private String version;

    /**
     * 获取组名.
     * @return 返回组名.
     */
    public String getGroup() {
        return group;
    }

    /**
     * 获取组名.
     * @return 返回组名.
     */
    public String getName() {
        return name;
    }

    /**
     * 获取组名.
     * @return 返回组名.
     */
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return getGroup() + ":" + getName() + ":" + getVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScriptInfo that = (ScriptInfo) o;
        return group.equals(that.group) && name.equals(that.name) && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, name, version);
    }

    /**
     * 设置组名.
     * @param group 新的组名.
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 设置脚本名称.
     * @param name 设置脚本名称.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置版本号.
     * @param version 脚本版本号.
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
