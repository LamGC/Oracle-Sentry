package net.lamgc.oracle.sentry.script.groovy;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.util.DelegatingScript;
import net.lamgc.oracle.sentry.script.Script;
import net.lamgc.oracle.sentry.script.ScriptComponents;
import net.lamgc.oracle.sentry.script.ScriptInfo;
import net.lamgc.oracle.sentry.script.ScriptLoader;
import net.lamgc.oracle.sentry.script.ScriptComponentFactory;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy 脚本加载器.
 * @author LamGC
 */
@SuppressWarnings("MapOrSetKeyShouldOverrideHashCodeEquals")
public class GroovyScriptLoader implements ScriptLoader {

    private final static Logger log = LoggerFactory.getLogger(GroovyScriptLoader.class);

    private final GroovyClassLoader scriptClassLoader;
    private final Map<Script, ScriptInfo> scriptInfoMap = new ConcurrentHashMap<>();
    private final Set<Script> initialedScript = new HashSet<>();

    /**
     * 构造一个新的脚本加载器.
     * <p> 每个加载器所使用的 {@link GroovyClassLoader} 实例是不一样的.
     */
    public GroovyScriptLoader() {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setScriptBaseClass(DelegatingScript.class.getName());
        this.scriptClassLoader = new GroovyClassLoader(GroovyClassLoader.class.getClassLoader(), compilerConfiguration);
    }

    @Override
    public boolean canLoad(File scriptFile) {
        return scriptFile.getName().endsWith(".groovy");
    }

    @Override
    public Script loadScript(ScriptComponents context, File scriptFile) throws IOException {
        Class<?> scriptClass = scriptClassLoader.parseClass(scriptFile);
        if (!DelegatingScript.class.isAssignableFrom(scriptClass)) {
            return null;
        }
        try {
            Constructor<? extends DelegatingScript> constructor =
                    scriptClass.asSubclass(DelegatingScript.class).getConstructor();
            DelegatingScript newScriptObject = constructor.newInstance();
            GroovyDslDelegate dslDelegate = new GroovyDslDelegate(this);
            newScriptObject.setDelegate(dslDelegate);
            newScriptObject.run();
            ScriptInfo scriptInfo = dslDelegate.getScriptInfo();
            if (!checkScriptInfo(scriptInfo)) {
                return null;
            }
            initialedScript.add(dslDelegate);
            newScriptObject.setBinding(createBinding(context, scriptInfo));
            newScriptObject.run();
            scriptInfoMap.put(dslDelegate, scriptInfo);
            return dslDelegate;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("加载脚本时发生异常.(ScriptPath: {})\n{}", scriptFile.getAbsolutePath(), Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    @Override
    public ScriptInfo getScriptInfo(Script script) {
        return scriptInfoMap.get(script);
    }

    /**
     * 检查脚本是否已经初始化完毕.
     * @param script 脚本对象.
     * @return 如果已初始化完毕, 返回 {@code true}.
     */
    public boolean isInitialed(GroovyDslDelegate script) {
        return initialedScript.contains(script);
    }

    private static Binding createBinding(ScriptComponents components, ScriptInfo info) {
        Binding binding = new Binding();
        for (Field field : components.getClass().getDeclaredFields()) {
            try {
                String name = field.getName();
                field.setAccessible(true);
                Object o = field.get(components);
                if (o instanceof ScriptComponentFactory factory) {
                    binding.setProperty(factory.getPropertyName(), factory.getInstance(info));
                } else {
                    binding.setProperty(name, o);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return binding;
    }

    private static boolean checkScriptInfo(ScriptInfo info) {
        if (Strings.isNullOrEmpty(info.getGroup())) {
            log.warn("脚本信息缺少 {}, 跳过加载.", "Group");
            return false;
        }
        if (Strings.isNullOrEmpty(info.getName())) {
            log.warn("脚本信息缺少 {}, 跳过加载.", "Name");
            return false;
        }
        if (Strings.isNullOrEmpty(info.getVersion())) {
            log.warn("脚本信息缺少 {}, 跳过加载.", "Version");
            return false;
        }
        return true;
    }

}
