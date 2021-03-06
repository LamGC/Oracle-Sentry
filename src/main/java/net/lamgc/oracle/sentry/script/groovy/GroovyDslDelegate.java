package net.lamgc.oracle.sentry.script.groovy;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import net.lamgc.oracle.sentry.script.Script;
import net.lamgc.oracle.sentry.script.ScriptInfo;
import net.lamgc.oracle.sentry.script.groovy.trigger.GroovyTrigger;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * Groovy DSL 脚本的父类.
 * @author LamGC
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class GroovyDslDelegate implements Script {

    private final GroovyScriptInfo scriptInfo = new GroovyScriptInfo();
    private final GroovyScriptLoader scriptLoader;

    /**
     * 构建一个 DSL Delegate, 并传入可操作对象.
     * @param scriptLoader 该脚本所属的加载器.
     */
    public GroovyDslDelegate(GroovyScriptLoader scriptLoader) {
        this.scriptLoader = scriptLoader;
    }

    /**
     * 注册触发器.
     * <p> 注意: 如果脚本尚未初始化完成, 将无法注册触发器, 可通过 {@link #isInitialed()} 检查是否已经完成初始化.
     * @param triggerName 触发器名称.
     * @param closure 待执行闭包.
     */
    private void trigger(String triggerName, Closure<?> closure){
        if (!scriptLoader.isInitialed(this)) {
            return;
        }
        GroovyTrigger trigger = GroovyTriggerProvider.INSTANCE.getTriggerByName(triggerName);
        DefaultGroovyMethods.with(trigger, closure);
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

    /**
     * 检查脚本当前是否已经初始化完成.
     * @return 如果脚本已经初始化, 本方法将返回 {@code true}.
     */
    public final boolean isInitialed() {
        return scriptLoader.isInitialed(this);
    }
}
