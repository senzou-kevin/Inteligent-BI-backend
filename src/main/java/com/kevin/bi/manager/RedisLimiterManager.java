package com.kevin.bi.manager;

import com.kevin.bi.exception.BusinessException;
import com.kevin.bi.common.ErrorCode;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 限流管理
 * @author zousen
 **/
@Component
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;


    /**
     * 限流操作
     * @param key 用于区分不同的限流器，比如不同用户的id应该分别统计
     */
    public void rateLimit(String key){
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //每秒最多访问2次
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        //每次操作拿取1个令牌
        boolean canExecute = rateLimiter.tryAcquire(1);
        if(!canExecute){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
