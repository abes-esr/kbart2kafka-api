package fr.abes.kbart2kafka.configuration;

import com.github.danielwegener.logback.kafka.keying.KeyingStrategy;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.logging.log4j.ThreadContext;

public class LoggerKeyingStrategy implements KeyingStrategy<ILoggingEvent> {
    @Override
    public byte[] createKey(ILoggingEvent o) {
        return ThreadContext.get("package").getBytes();
    }
}
