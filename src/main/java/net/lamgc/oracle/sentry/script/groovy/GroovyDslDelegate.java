package net.lamgc.oracle.sentry.script.groovy;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import net.lamgc.oracle.sentry.ComputeInstanceManager;
import net.lamgc.oracle.sentry.script.Script;
import net.lamgc.oracle.sentry.script.ScriptInfo;
import net.lamgc.oracle.sentry.script.tools.http.ScriptHttpClient;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * Groovy DSL 脚本的父类.
 * @author LamGC
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class GroovyDslDelegate implements Script {

    private final GroovyScriptInfo scriptInfo = new GroovyScriptInfo();
    private final ScriptHttpClient HTTP;
    private final ComputeInstanceManager InstanceManager;

    /**
     * 构建一个 DSL Delegate, 并传入可操作对象.
     * @param httpClient Http 客户端.
     * @param instanceManager 实例管理器.
     */
    public GroovyDslDelegate(ScriptHttpClient httpClient, ComputeInstanceManager instanceManager) {
        HTTP = httpClient;
        InstanceManager = instanceManager;
    }

    /**
     * 注册触发器.
     * @param triggerName 触发器名称.
     * @param closure 待执行闭包.
     */
    private void trigger(String triggerName, Closure<?> closure){
        DefaultGroovyMethods.with(GroovyTriggerProvider.INSTANCE.getTriggerByName(triggerName), closure);
    }

    /**
     * 脚本的基本信息.
     * @param scriptInfoClosure 配置了脚本信息的闭包对象.
     */
    private void info(@DelegatesTo(GroovyScriptInfo.class) Closure<GroovyScriptInfo> scriptInfoClosure) {
        DefaultGroovyMethods.with(scriptInfo, scriptInfoClosure);
    }

    @Override
    public ScriptInfo getScriptInfo() {
        return scriptInfo;
    }
}
