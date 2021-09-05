package net.lamgc.oracle.sentry.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * 级别范围过滤器.
 * @author LamGC
 */
@SuppressWarnings("unused")
public class LevelRangeFilter extends Filter<ILoggingEvent> {

    private Level maxLevel;
    private Level minLevel;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        int level = event.getLevel().levelInt;
        if (level > maxLevel.levelInt || level < minLevel.levelInt) {
            return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
    }

    /**
     * 设置最高等级.
     * <p> 仅限 XML 配置文件设置.
     * @param maxLevel 最高等级(包括).
     */
    public void setMaxLevel(String maxLevel) {
        this.maxLevel = Level.toLevel(maxLevel);
    }

    /**
     * 设置最低等级.
     * @param minLevel 允许的最低等级(包括).
     */
    public void setMinLevel(String minLevel) {
        this.minLevel = Level.toLevel(minLevel);
    }

    @Override
    public void start() {
        if (maxLevel != null && minLevel != null) {
            if (maxLevel.levelInt < minLevel.levelInt) {
                throw new IllegalArgumentException("The maximum level cannot be less than the minimum level.");
            }
            super.start();
        }
    }
}
