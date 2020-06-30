package com.mae.flag.systemdesign.redis;

import com.mae.flag.systemdesign.BaseUnitTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTemplateUnitTest extends BaseUnitTest {

    private static Logger logger = LoggerFactory.getLogger(RedisTemplateUnitTest.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    // this is embedded redis server
    // if we don't set up this embedded server, RedisTemplate's initialization will go error.
    private RedisServer redisServer;

    @Before
    public void setup() throws Exception {
        redisServer = new RedisServer();
        redisServer.start();
        logger.info("Embedded Redis Server is started, and running at " + redisServer.ports().get(0));
    }

    @Test
    public void redisTemplateTest() {
        Assert.assertNotNull(redisTemplate);
        redisTemplate.opsForValue().set("test", "redisTest");
        String ret = redisTemplate.opsForValue().get("test");
        Assert.assertNotNull(ret);
        Assert.assertEquals("redisTest", ret);
    }

    @Test
    public void templateRedisIncrement() {
        // if value of "test" is not exist, it will return default value -1
        Long d = redisTemplate.opsForValue().increment("test", -1);

        // return -1
        logger.info("d value is " + d);

        redisTemplate.opsForValue().set("test", "10");

        while ((d = redisTemplate.opsForValue().increment("test", -1)) > 0) {
            logger.info("d=" + d);
        }
    }

    /**
     * Suppose we have a key = Leader_Lock
     * in this unit test, we gonna set lock and release lock via redis APIs.
     */
    @Test
    public void redisLock() {
        Assert.assertNotNull(redisTemplate);
        String lockName = "LeaderLock";
        String lockValue = "Leader_UID";

        // at first, we suppose the lock doesn't exist
        boolean ret = redisTemplate.opsForValue().setIfAbsent(lockName, lockValue);

        // this means current thread get lock success
        Assert.assertTrue(ret);

        // what we gonna got if the lock's k,v doesn't exist at first.
        logger.info("ret-1=" + ret);


        // then, after we executing setIfAbsent, the k,v is set to the reids
        // second time we get the value, see what we got
        ret = redisTemplate.opsForValue().setIfAbsent(lockName, lockValue);
        logger.info("ret-2=" + ret);


        // this means current can not update the value
        Assert.assertFalse(ret);

        // then, release the lock
        boolean deleteRet = redisTemplate.delete(lockName);
        logger.info("release lock ret=" + deleteRet);

        // the lock already deleted, this can be considered lock is released
        // other threads can occupy the thread via setIfAbsent
        Assert.assertTrue(deleteRet);

        // again try to get the lock
        ret = redisTemplate.opsForValue().setIfAbsent(lockName, lockValue);
        logger.info("setIfAbsent ret = " + ret);

        // again current occupy the lock
        Assert.assertTrue(ret);
    }


    @After
    public void teardown() {
        logger.info("Embedded Redis Server is stopped.");
        redisServer.stop();
        redisServer = null;
    }
}
