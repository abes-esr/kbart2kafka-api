package fr.abes.kafkaconvergence.configuration;

import fr.abes.kafkaconvergence.utils.ExecutionTimeAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AspectJConfig {
    @Bean
    public ExecutionTimeAspect executionTimeAspect() {
        return new ExecutionTimeAspect();
    }
}
