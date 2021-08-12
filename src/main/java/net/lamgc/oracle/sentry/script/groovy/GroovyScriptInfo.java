package net.lamgc.oracle.sentry.script.groovy;

import net.lamgc.oracle.sentry.script.ScriptInfo;

public class GroovyScriptInfo extends ScriptInfo {

    public void artifact(String artifact) {
        super.setArtifact(artifact);
    }

    public void group(String group) {
        super.setGroup(group);
    }

    public void version(String version) {
        super.setVersion(version);
    }
}
