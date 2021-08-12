package net.lamgc.oracle.sentry.script;

import net.lamgc.oracle.sentry.ComputeInstanceManager;
import net.lamgc.oracle.sentry.script.tools.http.ScriptHttpClient;

/**
 * @author LamGC
 */
public final record ScriptComponent(
        ScriptHttpClient HTTP,
        ComputeInstanceManager InstanceManager
) {

}
