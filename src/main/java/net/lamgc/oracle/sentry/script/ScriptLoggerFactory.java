package net.lamgc.oracle.sentry.script;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 脚本日志记录器工厂.
 * <p> 通过 CGLIB 无缝为脚本设置 {@link Marker} 以将脚本日志输出到特定文件中.
 * @author LamGC
 */
public class ScriptLoggerFactory implements ScriptComponentFactory<Logger> {

    public final static Marker SCRIPT_MARKER = MarkerFactory.getMarker("Script");

    @Override
    public Logger getInstance(ScriptInfo info) {
        Logger realLogger = LoggerFactory.getLogger(info.getGroup() + ":" + info.getName());
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Logger.class);
        enhancer.setCallback(new LoggerProxyImpl(realLogger));
        return (Logger) enhancer.create();
    }

    @Override
    public String getPropertyName() {
        return "Log";
    }

    private static class LoggerProxyImpl implements MethodInterceptor {

        private final static Set<String> PROXY_METHOD_NAMES = Set.of(
                "trace", "debug", "info", "warn", "error",
                "isTraceEnabled",
                "isDebugEnabled",
                "isInfoEnabled",
                "isWarnEnabled",
                "isErrorEnabled"
        );

        private final Logger targetLog;
        private final Class<? extends Logger> logClass;

        public LoggerProxyImpl(Logger targetLog) {
            this.targetLog = targetLog;
            logClass = targetLog.getClass();
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (PROXY_METHOD_NAMES.contains(method.getName())) {
                Class<?>[] types = method.getParameterTypes();
                List<Class<?>> typeList = new ArrayList<>(Arrays.asList(types));
                typeList.add(0, Marker.class);
                if (types.length != 0 && !Marker.class.isAssignableFrom(types[0])) {
                    Class<?>[] realMethodParamTypes = typeList.toArray(new Class<?>[0]);
                    Method realMethod = logClass.getDeclaredMethod(method.getName(), realMethodParamTypes);
                    List<Object> paramList = new ArrayList<>(Arrays.asList(args));
                    paramList.add(0, SCRIPT_MARKER);
                    Object[] params = paramList.toArray(new Object[0]);
                    realMethod.invoke(targetLog, params);
                    return null;
                }
            }
            return proxy.invoke(targetLog, args);
        }
    }

}
