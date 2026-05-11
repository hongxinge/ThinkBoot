package com.thinkboot.redis.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "think-boot.redis", name = "enabled", havingValue = "true", matchIfMissing = false)
public class RedisUtils {

    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private void validateKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Redis key must not be null or empty");
        }
    }

    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public void delete(String... keys) {
        if (keys != null && keys.length > 0) {
            List<String> validKeys = Arrays.stream(keys)
                    .filter(k -> k != null && !k.isEmpty())
                    .collect(Collectors.toList());
            if (validKeys.isEmpty()) {
                return;
            }
            if (validKeys.size() == 1) {
                redisTemplate.delete(validKeys.get(0));
            } else {
                redisTemplate.delete(validKeys);
            }
        }
    }

    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, Object value) {
        try {
            validateKey(key);
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public boolean set(String key, Object value, long time) {
        try {
            validateKey(key);
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("Increment delta must be greater than 0");
        }
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            throw e;
        }
    }

    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("Decrement delta must be greater than 0");
        }
        try {
            return redisTemplate.opsForValue().increment(key, -delta);
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            throw e;
        }
    }

    public Object hGet(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public boolean hmSet(String key, Map<String, Object> map) {
        try {
            if (map == null || map.isEmpty()) {
                return false;
            }
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public boolean hSet(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public boolean hSet(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public void hDelete(String key, Object... items) {
        redisTemplate.opsForHash().delete(key, items);
    }

    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public long sGetSetSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    public List<Object> lGet(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public long lGetListSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public Object lGetIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public boolean lSetList(String key, List<Object> value) {
        try {
            if (value == null || value.isEmpty()) {
                return false;
            }
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public boolean lSetList(String key, List<Object> value, long time) {
        try {
            if (value == null || value.isEmpty()) {
                return false;
            }
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return false;
        }
    }

    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            log.error("Redis operation failed for key: {}", key, e);
            return 0;
        }
    }
}
