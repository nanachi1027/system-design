package com.interview.mae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Mae
 * @Date: 2021/3/6 1:31 下午
 */
public class MiniTwitter {

    private AtomicInteger tweetIdGenerator;
    private Map<Integer, TweetUser> userMap;


    public MiniTwitter() {
        userMap = new HashMap<>();
        tweetIdGenerator = new AtomicInteger(0);
    }

    /**
     * @param user_id:    an integer
     * @param tweet_text: a string, tweet content
     * @return a tweet
     */
    public Tweet postTweet(int user_id, String tweet_text) {
        Tweet tweet = Tweet.create(user_id, tweet_text);
        int tweetId = this.tweetIdGenerator.getAndIncrement();
        tweet.id = tweetId;

        if (userMap.containsKey(user_id)) {
            TweetUser user = new TweetUser(user_id);
            userMap.put(user.user_id, user);
        }

        TweetUser user = userMap.get(user_id);

        // append posted tweet info to user
        user.postATweet(tweet);


        // finally we return the user new created tweet
        return tweet;
    }

    /**
     * @param user_id: An integer
     * @return a list of 10 new feeds recently and sorted by timeline
     */
    public List<Tweet> getNewsFeed(int user_id) {
        TweetUser user = this.userMap.get(user_id);
        return user.getLastPostedTweets();
    }

    /**
     * @param user_id: An integer
     * @return: a list of 10 new posts recently and sorted by timeline
     */
    public List<Tweet> getTimeline(int user_id) {
        if (!userMap.containsKey(user_id)) {
            userMap.put(user_id, new TweetUser(user_id));
        }

        TweetUser user = userMap.get(user_id);

        List<Tweet> tweets = user.queryAllTopK(this.userMap);
        return tweets;
    }

    /**
     * from_user_id -> -> to_user_id
     * from_user_id.followTo <- to_user_id
     * to_user_id.followers <- from_user_id
     *
     * @param from_user_id: An integer
     * @param to_user_id:   An integer
     * @return nothing
     */
    public void follow(int from_user_id, int to_user_id) {
        // first checkout users are exists in cache
        if (!userMap.containsKey(from_user_id)) {
            userMap.put(from_user_id, new TweetUser(from_user_id));
        }

        if (!userMap.containsKey(to_user_id)) {
            userMap.put(to_user_id, new TweetUser(to_user_id));
        }

        // cause we use set to store followers and followTo, so we don't care duplicated user ids
        TweetUser follower = userMap.get(from_user_id);
        TweetUser beFollowed = userMap.get(to_user_id);

        follower.followTo.add(beFollowed.user_id);
        beFollowed.followers.add(follower.user_id);
    }

    /**
     * from_user -> x -> to_user
     * <p>
     * from_user.followTo -> remove -> to_user_id
     * to_user.followers  -> remove -> from_user_id
     *
     * @param from_user_id: An integer
     * @param to_user_id:   An integer
     * @return nothing
     */
    public void unfollow(int from_user_id, int to_user_id) {
        // first make sure user_id is created and really exists in MiniTwitter's cache
        // and make sure the relation ship that from_user -> follow -> to_user already established, if not establish
        if (!this.userMap.containsKey(from_user_id)) {
            TweetUser fromUser = new TweetUser(from_user_id);
            userMap.put(from_user_id, fromUser);
        }

        if (!this.userMap.containsKey(to_user_id)) {
            TweetUser toUser = new TweetUser(to_user_id);
            userMap.put(to_user_id, toUser);
        }

        TweetUser fromUser = userMap.get(from_user_id);
        TweetUser toUser = userMap.get(to_user_id);

        // only relationship exists we can remove
        if (fromUser.followTo.contains(to_user_id)) {
            fromUser.followTo.remove(to_user_id);
        }

        // only relationship exists we can remove
        if (toUser.followers.contains(from_user_id)) {
            toUser.followers.remove(from_user_id);
        }
    }
}
