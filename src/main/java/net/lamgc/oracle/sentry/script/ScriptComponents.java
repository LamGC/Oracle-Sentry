package net.lamgc.oracle.sentry.script;

import net.lamgc.oracle.sentry.ComputeInstanceManager;
import net.lamgc.oracle.sentry.oci.account.OracleAccountManager;
import net.lamgc.oracle.sentry.script.tools.http.ScriptHttpClient;

/**
 * 脚本组件集合.
 * <p> 存储了脚本可以使用的对象.
 * <p> 后续可能会改成用 {@link javax.script.Bindings} 之类的.
 * @author LamGC
 */
public final record ScriptComponents(
        ScriptHttpClient HTTP,
        ComputeInstanceManager InstanceManager,
        ScriptLoggerFactory loggerFactory,
        OracleAccountManager AccountManager
) {

}
