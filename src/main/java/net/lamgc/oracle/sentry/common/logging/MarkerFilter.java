package net.lamgc.oracle.sentry.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

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

    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }

    public void setOnMatch(String onMatch) {
        this.onMatch = FilterReply.valueOf(onMatch);
    }

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
