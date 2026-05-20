package apptive.team5.config;

import apptive.team5.alarm.entity.AlarmMessage;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String LIKE_ALARM_CACHE = "likeAlarm";
    public static final String SUBSCRIBE_ALARM_CACHE = "subscribeAlarm";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();


        cacheManager.registerCustomCache(
                LIKE_ALARM_CACHE,
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .build()
        );

        cacheManager.registerCustomCache(
                SUBSCRIBE_ALARM_CACHE,
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .build()
        );

        return cacheManager;

    }
}
