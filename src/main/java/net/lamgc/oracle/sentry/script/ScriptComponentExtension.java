package net.lamgc.oracle.sentry.script;

/**
 * 脚本组件扩展.
 * <p> 通过实现该接口, 可在脚本加载前设置组件对象, 为脚本提供更多功能.
 * <p> 实现接口后, 需按 SPI 方式添加实现.
 * @author LamGC
 */
public interface ScriptComponentExtension {

    /**
     * 配置脚本组件.
     * <p> 在方法中为组件集合添加组件.
     * @param components 脚本组件集合.
     */
    void configureScriptComponents(final ScriptComponents components);

}
