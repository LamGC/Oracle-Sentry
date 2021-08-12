package net.lamgc.oracle.sentry.script;

import java.io.File;

/**
 * 脚本加载器.
 * @author LamGC
 */
public interface ScriptLoader {

    /**
     * 是否可以加载.
     * @param scriptFile 脚本文件.
     * @return 如果可以加载, 返回 {@code true}.
     */
    boolean canLoad(File scriptFile);

    /**
     * 加载脚本.
     * @param context 脚本上下文.
     * @param scriptFile 脚本文件.
     * @return 返回脚本对象.
     * @throws Exception 当 Loader 抛出异常时, 将视为脚本加载失败, 该脚本跳过加载.
     */
    Script loadScript(ScriptComponent context, File scriptFile) throws Exception;

    /**
     * 获取脚本信息.
     * @param script 脚本对象.
     * @return 返回脚本信息.
     */
    ScriptInfo getScriptInfo(Script script);

}
