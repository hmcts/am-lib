package uk.gov.hmcts.reform.amlib.internal.aspects;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

public class StaticLogAppender extends AppenderBase<ILoggingEvent> {
    private static List<ILoggingEvent> events = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent event) {
        events.add(event);
    }

    static String getMessage() {
        return events.get(events.size() - 1).getMessage();
    }

    static void clear() {
        events.clear();
    }
}
