package org.example.aioschedulingservice.application.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LockService {
    private static final Logger logger = LoggerFactory.getLogger(LockService.class);
    private final StringRedisTemplate redisTemplate;
    private static final long DEFAULT_LOCK_TIMEOUT_MS = 30000;

    public boolean acquireLock(String lockKey, String lockValue, Duration timeout) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout);
        if (success != null && success) {
            logger.info("Acquired lock for key: {} with value: {}", lockKey, lockValue);
            return true;
        }
        logger.warn("Failed to acquire lock for key: {} (possibly held by another process)", lockKey);
        return false;
    }

    public boolean releaseLock(String lockKey, String lockValue) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute(
                ((RedisCallback<Long>)(connection) -> connection.eval(
                        script.getBytes(),
                        org.springframework.data.redis.connection.ReturnType.INTEGER,
                        1,
                        lockKey.getBytes(),
                        lockValue.getBytes()
                )
        ));

        if (result != null && result == 1) {
            logger.info("Released lock for key: {} with value: {}", lockKey, lockValue);
            return true;
        }
        logger.warn("Failed to release lock for key: {} (lock not held by {} or expired)", lockKey, lockValue);
        return false;
    }

}
