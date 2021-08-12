package net.lamgc.oracle.sentry.script;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * @author LamGC
 */
public abstract class Script {

    /**
     * 获取脚本信息.
     * @return 返回脚本 ScriptInfo 对象.
     */
    public abstract ScriptInfo getScriptInfo();

}
