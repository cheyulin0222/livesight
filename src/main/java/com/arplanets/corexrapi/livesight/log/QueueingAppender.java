package com.arplanets.corexrapi.livesight.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Getter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueingAppender extends AppenderBase<ILoggingEvent> {

    @Getter
    private static final BlockingQueue<ILoggingEvent> logQueue = new LinkedBlockingQueue<>(10000);

    @Override
    protected void append(ILoggingEvent event) {
        event.prepareForDeferredProcessing();
        boolean offered = logQueue.offer(event);
        if (!offered) {
            addWarn("Log buffer is full. Dropping event: " + event.getMessage());
        }
    }

}
