package net.lamgc.oracle.sentry.script;

/**
 * 脚本基类.
 * <p> 实现该接口的可视为脚本, 具体细节由具体脚本语言的模块定义.
 * @author LamGC
 */
public interface Script {

    /**
     * 获取脚本信息.
     * @return 返回脚本 ScriptInfo 对象.
     */
    ScriptInfo getScriptInfo();

}
