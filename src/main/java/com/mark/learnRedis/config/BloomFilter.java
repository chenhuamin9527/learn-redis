package com.mark.learnRedis.config;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class BloomFilter {

    private final RedisTemplate<String, String> redisTemplate;

    private long bitSize;
    private int hashFunctions;

    private static final String BLOOM_KEY_PREFIX = "bloom:";
    private static final HashFunction MURMUR_HASH_3 = Hashing.murmur3_128();

    public BloomFilter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void init(String key, long expectedCount, double fpp) {
        double m = Math.ceil(-(expectedCount * Math.log(fpp)) / Math.pow(Math.log(2), 2));
        this.bitSize = (long) m;

        this.hashFunctions = (int) Math.ceil((bitSize / (double) expectedCount) * Math.log(2));

        String configKey = BLOOM_KEY_PREFIX + key + ":config";
        redisTemplate.opsForValue().set(configKey, bitSize + "," + hashFunctions, 7, TimeUnit.DAYS);
    }

    public void add(String key, String value) {
        int[] positions = getPositions(value);

        for (int position : positions) {
            long byteOffset = position / 8;
            redisTemplate.opsForValue().setBit(getBitKey(key), byteOffset, true);
        }
    }

    public boolean contains(String key, String value) {
        int[] positions = getPositions(value);

        for (int position : positions) {
            long byteOffset = position / 8;
            Boolean bit = redisTemplate.opsForValue().getBit(getBitKey(key), byteOffset);
            if (Boolean.FALSE.equals(bit)) {
                return false;
            }
        }
        return true;
    }

    private int[] getPositions(String value) {
        int[] positions = new int[hashFunctions];

        long hash1 = MURMUR_HASH_3.hashBytes(value.getBytes()).asLong();
        long hash2 = MURMUR_HASH_3.hashBytes((value + "salt").getBytes()).asLong();

        for (int i = 0; i < hashFunctions; i++) {
            long combinedHash = hash1 + (long) i * hash2;
            combinedHash = combinedHash & 0xFFFFFFFFL;
            positions[i] = (int) (combinedHash % bitSize);
        }

        return positions;
    }

    private String getBitKey(String key) {
        return BLOOM_KEY_PREFIX + key + ":bits";
    }
}
