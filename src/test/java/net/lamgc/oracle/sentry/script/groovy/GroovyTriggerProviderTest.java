package net.lamgc.oracle.sentry.script.groovy;

import net.lamgc.oracle.sentry.script.groovy.trigger.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * @see GroovyTriggerProvider
 */
class GroovyTriggerProviderTest {

    @Test
    public void standardRunTest() {
        GroovyTrigger trigger = GroovyTriggerProvider.INSTANCE.getTriggerByName("once");
        Assertions.assertNotNull(trigger);
        Assertions.assertEquals(OnceTrigger.class, trigger.getClass());
    }

    @Test
    public void noAnnotationTriggerTest() throws NoSuchFieldException, IllegalAccessException {
        failIfHasTrigger(NoAnnotationTrigger.class);
    }

    @Test
    public void badTriggerNameLoadTest() throws NoSuchFieldException, IllegalAccessException {
        failIfHasTrigger(BadAnnotationTrigger.class);
    }

    @Test
    public void duplicateTriggerLoadTest() throws NoSuchFieldException, IllegalAccessException {
        Assertions.assertFalse(hasTrigger(DuplicateTriggerA.class) && hasTrigger(DuplicateTriggerB.class));
    }

    @Test
    public void tryToGetNoExistTriggerTest() {
        Assertions.assertThrows(NoSuchElementException.class, () ->
                GroovyTriggerProvider.INSTANCE.getTriggerByName("NoExistTrigger"));
    }

    @Test
    public void nullTest() {
        Assertions.assertThrows(NullPointerException.class, () ->
                GroovyTriggerProvider.INSTANCE.getTriggerByName(null));
    }

    private void failIfHasTrigger(Class<? extends GroovyTrigger> triggerClass)
            throws NoSuchFieldException, IllegalAccessException {
        if (hasTrigger(triggerClass)) {
            Assertions.fail("Trigger did not appear as expected.");
        }
    }

    @SuppressWarnings("unchecked")
    private boolean hasTrigger(Class<? extends GroovyTrigger> triggerClass)
            throws NoSuchFieldException, IllegalAccessException  {
        Field providerMapField =
                GroovyTriggerProvider.class.getDeclaredField("triggerProviderMap");
        providerMapField.setAccessible(true);
        Map<String, ServiceLoader.Provider<GroovyTrigger>> map =
                (Map<String, ServiceLoader.Provider<GroovyTrigger>>) providerMapField.get(
                        GroovyTriggerProvider.INSTANCE
                );
        providerMapField.setAccessible(false);
        for (ServiceLoader.Provider<GroovyTrigger> value : map.values()) {
            if (triggerClass.equals(value.type())) {
                return true;
            }
        }
        return false;
    }

}