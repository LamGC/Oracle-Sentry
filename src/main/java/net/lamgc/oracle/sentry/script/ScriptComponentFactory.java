package net.lamgc.oracle.sentry.script;

/**
 * 脚本组件工厂.
 * @author LamGC
 */
public interface ScriptComponentFactory<T> {

    /**
     * 创建并获取实例.
     * @param info 脚本信息.
     * @return 返回对象.
     */
    T getInstance(ScriptInfo info);

    /**
     * 对象属性名.
     * @return 返回建议的对象属性名, {@link ScriptLoader} 并不一定遵守.
     */
    String getPropertyName();

}
