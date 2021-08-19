package net.lamgc.oracle.sentry.script.groovy;

import com.google.common.base.Strings;
import net.lamgc.oracle.sentry.script.groovy.trigger.GroovyTrigger;
import net.lamgc.oracle.sentry.script.groovy.trigger.TriggerName;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy 脚本语言的触发器提供者.
 * <p> 根据脚本需要创建并注册触发器.
 * @author LamGC
 */
public class GroovyTriggerProvider {

    /**
     * Trigger Provider 唯一实例.
     */
    public final static GroovyTriggerProvider INSTANCE = new GroovyTriggerProvider();


    private final Map<String, ServiceLoader.Provider<GroovyTrigger>> triggerProviderMap = new ConcurrentHashMap<>();

    private GroovyTriggerProvider() {
        ServiceLoader<GroovyTrigger> loader = ServiceLoader.load(GroovyTrigger.class);
        loader.stream().iterator().forEachRemaining(triggerProvider -> {
            Class<? extends GroovyTrigger> triggerClass = triggerProvider.type();
            if (!triggerClass.isAnnotationPresent(TriggerName.class)) {
                return;
            }

            TriggerName triggerName = triggerClass.getAnnotation(TriggerName.class);
            if (!Strings.isNullOrEmpty(triggerName.value())) {
                String name = triggerName.value().toLowerCase();
                if (triggerProviderMap.containsKey(name)) {
                    return;
                }
                triggerProviderMap.put(name, triggerProvider);
            }

        });
    }

    /**
     * 通过 Trigger 名称获取新的 Trigger.
     * @param triggerName Trigger 名称.
     * @return 返回指定 Trigger 的新实例.
     * @throws NoSuchElementException 当指定的 Trigger 名称没有对应 Trigger 时抛出该异常.
     */
    public GroovyTrigger getTriggerByName(String triggerName) {
        if (!triggerProviderMap.containsKey(triggerName.toLowerCase())) {
            throw new NoSuchElementException("The specified trigger could not be found: " + triggerName);
        }
        return triggerProviderMap.get(triggerName).get();
    }

}
