package com.interview.mae;


import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Mae
 * @Date: 2021/3/6 1:32 下午
 */
public class TweetTest {

    @Test
    public void testTweet() {
        int user_id = 0;
        String tweetContent = "good idea!";
        Tweet tweet = Tweet.create(user_id, tweetContent);

    }

    @Test
    public void testSortTweetByTimestamp() {
        List<Tweet> tweetList = new ArrayList<>();
        AtomicInteger tweetIdGenerator = new AtomicInteger(0);
        for (int i = 0; i < 10; i++) {
            Tweet t = Tweet.create(i * 2938, "tweet content no." + i);
            t.id = tweetIdGenerator.getAndIncrement();
            tweetList.add(t);
        }

        Collections.sort(tweetList, (t1, t2) -> {
            return t1.id - t2.id;
        });

        tweetList.forEach(tweet -> {
            System.out.println(tweet.id + " " + tweet.text + " " + tweet.user_id);
        });
    }
}