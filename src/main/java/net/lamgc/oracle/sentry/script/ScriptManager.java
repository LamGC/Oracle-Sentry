package net.lamgc.oracle.sentry.script;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本管理器.
 * @author LamGC
 */
public final class ScriptManager {

    private final static Logger log = LoggerFactory.getLogger(ScriptManager.class);

    private final Set<ScriptLoader> loaders = new HashSet<>();
    private final File scriptsLocation;
    private final ScriptComponents context;

    private final Map<ScriptInfo, Script> scripts = new ConcurrentHashMap<>();

    /**
     * 创建新的脚本管理器.
     * @param scriptsLocation 脚本加载路径.
     * @param components 脚本组件.
     */
    public ScriptManager(File scriptsLocation, ScriptComponents components) {
        this.scriptsLocation = scriptsLocation;
        this.context = components;
        loadScriptLoaders();
    }

    private synchronized void loadScriptLoaders() {
        if (!loaders.isEmpty()) {
            return;
        }
        ServiceLoader<ScriptLoader> serviceLoader = ServiceLoader.load(ScriptLoader.class);
        for (ScriptLoader scriptLoader : serviceLoader) {
            loaders.add(scriptLoader);
        }
        log.info("存在 {} 个加载器可用.", loaders.size());
    }

    /**
     * 从文件中加载一个脚本.
     * @param scriptFile 脚本文件.
     * @return 如果加载成功, 返回 {@code true}, 加载失败或无加载器可用时返回 {@code false}
     * @throws InvocationTargetException 当加载器加载脚本抛出异常时, 将通过该异常包装后抛出.
     * @throws NullPointerException 当 scriptFile 为 {@code null} 时抛出.
     */
    public boolean loadScript(File scriptFile) throws InvocationTargetException {
        Objects.requireNonNull(scriptFile);
        for (ScriptLoader loader : loaders) {
            Script script;
            try {
                if (loader.canLoad(scriptFile)) {
                    script = loader.loadScript(context, scriptFile);
                    if (script == null) {
                        log.warn("加载器未能正确加载脚本, 已跳过该脚本.(ScriptName: {})", scriptFile.getName());
                        return false;
                    }
                } else {
                    continue;
                }
                ScriptInfo scriptInfo = loader.getScriptInfo(script);
                if (scriptInfo == null) {
                    log.warn("脚本加载成功, 但加载器没有返回脚本信息, 该脚本已放弃.");
                    return false;
                }
                scripts.put(scriptInfo, script);
                return true;
            } catch (Exception e) {
                log.error("脚本加载时发生异常.(Loader: {}, Path: {})\n{}",
                        loader.getClass().getName(),
                        scriptFile.getAbsolutePath(),
                        Throwables.getStackTraceAsString(e));
                throw new InvocationTargetException(e);
            }
        }
        return false;
    }

    /**
     * 从指定位置加载所有脚本.
     */
    public void loadScripts() {
        log.info("正在加载脚本...(Path: {})", scriptsLocation.getAbsolutePath());
        File[] files = scriptsLocation.listFiles(File::isFile);
        if (files == null) {
            log.warn("脚本目录无法访问, 请检查程序是否有权限访问脚本目录.(Path: {})", scriptsLocation.getAbsolutePath());
            return;
        }
        int loadCount = 0;
        for (File scriptFile : files) {
            try {
                if (loadScript(scriptFile)) {
                    loadCount ++;
                }
            } catch (InvocationTargetException ignored) {
            }
        }
        log.info("脚本已全部加载完成, 共成功加载了 {} 个脚本.", loadCount);
    }

    /**
     * 通过脚本信息返回脚本对象.
     * @param scriptInfo 脚本信息.
     * @return 返回脚本对象.
     * @throws NoSuchElementException 当指定的 ScriptInfo 没有对应脚本对象时抛出该异常.
     * @throws NullPointerException 当 scriptInfo 为 {@code null} 时抛出该异常.
     */
    public Script getScriptByScriptInfo(ScriptInfo scriptInfo) {
        Objects.requireNonNull(scriptInfo);
        if (!scripts.containsKey(scriptInfo)) {
            throw new NoSuchElementException(scriptInfo.toString());
        }
        return scripts.get(scriptInfo);
    }

}
