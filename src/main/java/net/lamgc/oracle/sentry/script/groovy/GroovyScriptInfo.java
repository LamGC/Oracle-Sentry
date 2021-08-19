package net.lamgc.oracle.sentry.script.groovy;

import net.lamgc.oracle.sentry.script.ScriptInfo;

/**
 * 适配 Groovy 的脚本信息对象.
 * @author LamGC
 */
public class GroovyScriptInfo extends ScriptInfo {

    /**
     * 设置脚本名.
     * <p> 不能有空格.
     * @param name 脚本名.
     */
    public void name(String name) {
        super.setName(name);
    }

    /**
     * 设置组名.
     * <p> 组名是脚本开发者的域名倒写, 如果你的域名是 example.com,
     * 那么组名就是 com.example, 没有域名可以用 Github 的,
     * io.github.[你的 Github 用户名]
     *
     * @param group 组名.
     */
    public void group(String group) {
        super.setGroup(group);
    }

    /**
     * 脚本版本号.
     * <p> 遵循 SemVer 版本号规范.
     * @param version 当前脚本版本号.
     */
    public void version(String version) {
        super.setVersion(version);
    }
}
