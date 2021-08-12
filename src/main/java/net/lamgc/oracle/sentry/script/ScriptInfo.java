package net.lamgc.oracle.sentry.script;

import java.util.Objects;

/**
 * 脚本信息.
 * @author LamGC
 */
public class ScriptInfo {

    private String group;
    private String artifact;
    private String version;

    public String getGroup() {
        return group;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return getGroup() + ":" + getArtifact() + ":" + getVersion();
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
        return group.equals(that.group) && artifact.equals(that.artifact) && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, artifact, version);
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
