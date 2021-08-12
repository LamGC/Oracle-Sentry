package net.lamgc.oracle.sentry.script.groovy;

import com.google.common.base.Strings;
import net.lamgc.oracle.sentry.script.groovy.trigger.GroovyTrigger;
import net.lamgc.oracle.sentry.script.groovy.trigger.TriggerName;
import org.springframework.scheduling.Trigger;

import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LamGC
 */
public class GroovyTriggerProvider {

    private final Map<String, ServiceLoader.Provider<GroovyTrigger>> triggerProviderMap = new ConcurrentHashMap<>();

    public final static GroovyTriggerProvider INSTANCE = new GroovyTriggerProvider();

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

    public GroovyTrigger getTriggerByName(String triggerName) {
        if (!triggerProviderMap.containsKey(triggerName.toLowerCase())) {
            throw new NoSuchElementException("The specified trigger could not be found: " + triggerName);
        }
        return triggerProviderMap.get(triggerName).get();
    }

}
