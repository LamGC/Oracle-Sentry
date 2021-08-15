package net.lamgc.oracle.sentry.script.groovy;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import net.lamgc.oracle.sentry.script.groovy.trigger.*;
import net.lamgc.oracle.sentry.script.tools.http.ScriptHttpClient;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * Groovy DSL 脚本的父类.
 * @author LamGC
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class GroovyDslDelegate extends Script {

    private final GroovyScriptInfo scriptInfo = new GroovyScriptInfo();
    private final ScriptHttpClient HTTP;
    private final ComputeInstanceManager InstanceManager;

    public GroovyDslDelegate(ScriptHttpClient httpClient, ComputeInstanceManager instanceManager) {
        HTTP = httpClient;
        InstanceManager = instanceManager;
    }
    
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
