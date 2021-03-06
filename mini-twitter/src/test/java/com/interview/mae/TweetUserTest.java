package com.interview.mae;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * @Author: Mae
 * @Date: 2021/3/6 3:59 下午
 */
public class TweetUserTest {

    private TweetUser tweetUserA;
    private TweetUser tweetUserB;
    private AtomicInteger tweetIdGenerator;
    private Map<Integer, TweetUser> userMap;

    @Before
    public void initTweetUser() {
        this.tweetUserA = new TweetUser(0);
        this.tweetUserB = new TweetUser(1);
        this.tweetIdGenerator = new AtomicInteger(0);

        Assert.assertNotNull(tweetUserA.followers);
        Assert.assertNotNull(tweetUserB.followers);
        Assert.assertNotNull(tweetUserA.followTo);
        Assert.assertNotNull(tweetUserB.followTo);
        Assert.assertNotNull(tweetUserA.user_id);
        Assert.assertNotNull(tweetUserB.user_id);
        Assert.assertNotNull(tweetUserA.postedTweets);
        Assert.assertNotNull(tweetUserB.postedTweets);

        this.userMap = new HashMap<>(2);
        userMap.put(tweetUserA.user_id, tweetUserA);
        userMap.put(tweetUserB.user_id, tweetUserB);
    }


    @Test
    public void postATweet() {
        Tweet t = Tweet.create(0, "tweet content.");
        tweetUserA.postATweet(t);
        Assert.assertTrue(tweetUserA.postedTweets.size() == 1);

        List<Tweet> postedTweetsList = tweetUserA.getLastPostedTweets();
        Assert.assertNotNull(postedTweetsList);
        Assert.assertTrue(postedTweetsList.size() == 1);
        Assert.assertNotNull(postedTweetsList.get(0).text.endsWith("tweet content."));
    }

    // suppose current user has posted 11 tweets in order
    // we need to retrieve them in order by calling the getLastPostedTweets
    // create 11 tweets with ids from 0 -> 10 in order and posted in same order by user
    @Test
    public void getLastPostedTweets() {
        for (int i = 0; i < 11; i++) {
            Tweet t = Tweet.create(0, "tweet content " + i);
            t.id = tweetIdGenerator.getAndIncrement();
            tweetUserA.postATweet(t);
        }

        // here we call get last posted tweets method and make sure the return tweets returns in order
        List<Tweet> queryResult = tweetUserA.getLastPostedTweets();
        for (int i = 0; i < 10; i++) {
            int tweetId = queryResult.get(i).id;
            Assert.assertEquals(i, tweetId);
        }
    }

    // suppose we have two tweet users A and B,
    // A is follow to B : A -> B

    // they post their tweets in cross order total post 20 tweets
    // finally we query with A's view find out how many tweets A can get

    // condition1: is A not follow B after A and B sending total 20 tweets, A -> B, then query results should be from both A and B's posted tweets.
    // condition2: is A follow B and A & B sending total 20 tweets, do nothing, then query results should be from both A and B's posted tweets
    // condition3: is A flollow B , A, B sending total 20 tweets, then, A -> not follow B any more , then query results should only contains A's posted tweets
    @Test
    public void postTweet_follow_query() {
        // posting
        for (int i = 0; i < 20; i++) {
            int user_id = i % 2;
            Tweet t = Tweet.create(user_id, "tweet content " + i);
            t.id = i;
            if (i % 2 == 0) {
                tweetUserA.postATweet(t);
            } else {
                tweetUserB.postATweet(t);
            }
        }

        // follow
        tweetUserA.followTo.add(tweetUserB.user_id);
        tweetUserB.followers.add(tweetUserA.user_id);

        // query
        List<Tweet> ret = tweetUserA.queryAllTopK(userMap);

        ret.forEach(t -> {
            System.out.println("tweet_id " + t.id + " posted by " + t.user_id);
        });
    }

    @Test
    public void follow_postTweet_unfollow_query() {
        // 1. A follow B
        // 2. A, B post tweet
        // 3. A unfollow B
        // 4. query A's recently all top k tweets
    }

    @Test
    public void post_follow_query() {
        // 1. A, B post tweet
        // 2. A follow B
        // 3. query A's recently all top k tweets
    }

}