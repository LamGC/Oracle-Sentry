package net.lamgc.oracle.sentry.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

/**
 * @author LamGC
 */
@SuppressWarnings("unused")
public class MarkerFilter extends Filter<ILoggingEvent> {

    private String markerName;
    private FilterReply onMatch = FilterReply.NEUTRAL;
    private FilterReply onMismatch = FilterReply.DENY;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        return event.getMarker() != null && event.getMarker().getName().equals(markerName) ? onMatch : onMismatch;
    }

    /**
     * 设置要匹配的 {@link Marker} 名称.
     * @param markerName Marker 名称.
     */
    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }

    /**
     * 如果匹配, 需要执行的操作.
     * @see FilterReply
     * @param onMatch 操作名称.
     */
    public void setOnMatch(String onMatch) {
        this.onMatch = FilterReply.valueOf(onMatch);
    }

    /**
     * 如果不匹配, 需要执行的操作.
     * @see FilterReply
     * @param onMismatch 操作名称.
     */
    public void setOnMismatch(String onMismatch) {
        this.onMismatch = FilterReply.valueOf(onMismatch);
    }

    @Override
    public void start() {
        if (markerName != null && onMatch != null && onMismatch != null) {
            super.start();
        }
    }
}
