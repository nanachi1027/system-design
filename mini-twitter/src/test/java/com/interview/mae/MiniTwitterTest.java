package com.interview.mae;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @Author: Mae
 * @Date: 2021/3/6 3:58 下午
 */
public class MiniTwitterTest {

    private com.interview.mae.submission.MiniTwitter miniTwitter;

    @Before
    public void initMiniTwitter() {
        miniTwitter = new com.interview.mae.submission.MiniTwitter();
    }


    @Test
    public void postTweet() {
        int id1 = 0;
        miniTwitter.postTweet(id1, "test");
        Assert.assertTrue(miniTwitter.getNewsFeed(0) != null);
    }

    @Test
    public void getNewsFeed() {
        // user1 post 100 tweet and get latest tweet
        // which ids range in [99 - 89]
        for (int i = 0; i < 100; i++) {
            miniTwitter.postTweet(0, "tweet content " + i);
        }

        List<Tweet> tweets = miniTwitter.getNewsFeed(0);
        for (int i = 99; i > 89; i--) {
            Tweet t = tweets.get(99 - i);
            System.out.println("tweet id " + t.id + ", posted by user " + t.user_id + ", posted content " + t.text);
            Assert.assertTrue(t.id == i);
        }
    }

    @Test
    public void getTimeline() {
        // user0, user1 post tweet crisscross
        // user0 follows to user1
        int userId0 = 0;
        int userId1 = 1;

        miniTwitter.follow(userId0, userId1);

        for (int i = 0; i < 21; i++) {
            int id = i % 2;
            miniTwitter.postTweet(id, "tweet content with index " + i);
        }

        List<Tweet> userId0VisiableTweets = miniTwitter.getTimeline(userId0);
        for (int i = 20; i > 10; i--) {
            Tweet t = userId0VisiableTweets.get(20 - i);
            Assert.assertTrue(t.id == i);
            System.out.println("tweet id " + t.id + " posted by " + t.user_id);
        }
    }

    // suppose user1, user2
    // user1 -> user2 --> user1.toFollow.contains(user2), user2.followers.contains(user1)
    // user2 -> user1 --> user2.toFollow.contains(user1), user1.followers.contains(user2)
    // user1 -> x -> user2 --> !user1.toFollow.contains(user2)
    // and so on ...
    @Test
    public void follow() {
        int userA = 8;
        int userB = 11;

        // follow A -> B
        miniTwitter.follow(userA, userB);
        com.interview.mae.submission.TweetUser tweetUserA = miniTwitter.getUser(userA);
        Assert.assertNotNull(tweetUserA);

        com.interview.mae.submission.TweetUser tweetUserB = miniTwitter.getUser(userB);
        Assert.assertNotNull(tweetUserB);

        Assert.assertTrue(tweetUserA.followTo.contains(userB));
        Assert.assertTrue(tweetUserB.followers.contains(userA));

        // follow B -> A
        miniTwitter.follow(userB, userA);
        tweetUserA = miniTwitter.getUser(userA);
        tweetUserB = miniTwitter.getUser(userB);

        Assert.assertTrue(tweetUserB.followTo.contains(userA));
        Assert.assertTrue(tweetUserA.followers.contains(userB));

        // unfollow A -> x -> B
        miniTwitter.unfollow(userA, userB);
        tweetUserA = miniTwitter.getUser(userA);
        tweetUserB = miniTwitter.getUser(userB);

        Assert.assertTrue(tweetUserA.followers.contains(userB));
        Assert.assertFalse(tweetUserA.followTo.contains(userB));

        Assert.assertTrue(tweetUserB.followTo.contains(userA));
        Assert.assertFalse(tweetUserB.followers.contains(userA));

    }

    @Test
    public void unfollow() {
        // if user D and user C has neither follower nor follow to relationship
        // make sure there is no NPE exceptions thrown
        int userC = 19;
        int userD = 283;

        miniTwitter.unfollow(userC, userD);
        miniTwitter.unfollow(userD, userC);
    }
}