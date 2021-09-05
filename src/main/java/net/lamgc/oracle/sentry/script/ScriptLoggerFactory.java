package net.lamgc.oracle.sentry.script;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本日志记录器工厂.
 * <p> 通过 CGLIB 无缝为脚本设置 {@link Marker} 以将脚本日志输出到特定文件中.
 * @author LamGC
 */
public class ScriptLoggerFactory implements ScriptComponentFactory<Logger> {

    private final static Marker SCRIPT_MARKER = MarkerFactory.getMarker("Script");

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

        private final static ConcurrentHashMap<Method, Method> METHOD_CACHE_MAP =
                new ConcurrentHashMap<>(PROXY_METHOD_NAMES.size(), 1);

        private final Logger targetLog;
        private final Class<? extends Logger> logClass;

        public LoggerProxyImpl(Logger targetLog) {
            this.targetLog = targetLog;
            logClass = targetLog.getClass();
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (METHOD_CACHE_MAP.contains(method)) {
                return METHOD_CACHE_MAP.get(method).invoke(targetLog, insertParameterToArray(args, SCRIPT_MARKER));
            } else if (PROXY_METHOD_NAMES.contains(method.getName())) {
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 0 && !Marker.class.isAssignableFrom(types[0])) {
                    Class<?>[] realMethodParamTypes = insertTypeToArray(types, Marker.class);
                    Method realMethod = logClass.getDeclaredMethod(method.getName(), realMethodParamTypes);
                    METHOD_CACHE_MAP.put(method, realMethod);
                    realMethod.invoke(targetLog, insertParameterToArray(args, SCRIPT_MARKER));
                    return null;
                }
            }
            return proxy.invoke(targetLog, args);
        }
        
        public Object[] insertParameterToArray(Object[] arr, Object newElement) {
            if (arr.length == 0) {
                return new Object[] {newElement};
            }
            Object[] newArr = new Object[arr.length + 1];
            newArr[0] = newElement;
            System.arraycopy(arr, 0, newArr, 1, arr.length);
            return newArr;
        }

        public Class<?>[] insertTypeToArray(Class<?>[] arr, Class<?> newElement) {
            if (arr.length == 0) {
                return new Class<?>[] {newElement};
            }
            Class<?>[] newArr = new Class<?>[arr.length + 1];
            newArr[0] = newElement;
            System.arraycopy(arr, 0, newArr, 1, arr.length);
            return newArr;
        }
    }

}
