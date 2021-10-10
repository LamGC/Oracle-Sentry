package net.lamgc.oracle.sentry.script;

import com.google.common.base.Strings;

import java.util.*;

/**
 * 脚本组件集合.
 * <p> 存储了脚本可以使用的对象.
 * <p> 后续可能会改成用 {@link javax.script.Bindings} 之类的.
 * @author LamGC
 */
public final class ScriptComponents {

    private final ScriptComponents parent;
    private final Map<String, Object> componentObjects = new HashMap<>();
    private final Set<ScriptComponentFactory<?>> factories = new HashSet<>();

    public ScriptComponents() {
        this.parent = null;
    }

    public ScriptComponents(ScriptComponents parent) {
        this.parent = parent;
    }

    public void addComponentObject(String componentName, Object componentObject) {
        if (Strings.isNullOrEmpty(componentName)) {
            throw new NullPointerException("The component name is null or empty.");
        } else if (componentObject == null) {
            throw new NullPointerException("ComponentObject is null");
        } else if (componentObjects.containsKey(componentName)) {
            throw new IllegalArgumentException("The corresponding object already exists for the component name.");
        }

        componentObjects.put(componentName, componentObject);
    }

    public void addComponentFactory(ScriptComponentFactory<?> factory) {
        Objects.requireNonNull(factory);
        factories.add(factory);
    }

    public Map<String, Object> getComponentObjects() {
        Map<String, Object> componentObjects = this.parent == null ?
                new HashMap<>() : new HashMap<>(this.parent.getComponentObjects());
        componentObjects.putAll(this.componentObjects);
        return Collections.unmodifiableMap(componentObjects);
    }

    public Set<ScriptComponentFactory<?>> getScriptComponentFactories() {
        Set<ScriptComponentFactory<?>> factories = this.parent == null ?
                new HashSet<>() : new HashSet<>(this.parent.factories);
        factories.addAll(this.factories);
        return Collections.unmodifiableSet(factories);
    }

}
