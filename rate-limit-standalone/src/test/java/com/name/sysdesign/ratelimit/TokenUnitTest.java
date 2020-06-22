package com.name.sysdesign.ratelimit;

import com.mae.sysdesign.ratelimit.Token;
import org.junit.Assert;
import org.junit.Test;

public class TokenUnitTest {
    @Test
    public void isExpired() throws InterruptedException {
        long period = 100;
        Token token = new Token(period);
        Assert.assertFalse(token.isTokenExpired());
        Thread.sleep(token.getExpiredDurationInMS());
        Assert.assertTrue(token.isTokenExpired());
    }
}
