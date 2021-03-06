package com.interview.mae.submission;

import com.interview.mae.Tweet;

import java.util.*;

/**
 * @Author: Mae
 * @Date: 2021/3/6 6:54 下午
 */
public class Solution {
}

class MiniTwitter {
    private int tweetIdGenerator;
    private Map<Integer, TweetUser> userMap;

    public TweetUser getUser(int user_id) {
        if (!this.userMap.containsKey(user_id)) {
            return null;
        }
        return userMap.get(user_id);
    }

    public MiniTwitter() {
        this.userMap = new HashMap<>();
        this.tweetIdGenerator = 1;
    }

    /**
     * @param user_id:    an integer
     * @param tweet_text: a string, tweet content
     * @return a tweet
     */
    public Tweet postTweet(int user_id, String tweet_text) {
        Tweet tweet = Tweet.create(user_id, tweet_text);
        int tweetId = tweetIdGenerator++;
        tweet.id = tweetId;

        if (!userMap.containsKey(user_id)) {
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
    public List<Tweet> getTimeline(int user_id) {
        if (!userMap.containsKey(user_id)) {
            userMap.put(user_id, new TweetUser(user_id));
        }
        TweetUser user = this.userMap.get(user_id);
        return user.getLastPostedTweets();
    }

    /**
     * @param user_id: An integer
     * @return: a list of 10 new posts recently and sorted by timeline
     */
    public List<Tweet> getNewsFeed(int user_id) {
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


class TweetUser {
    // tweet user should have two cache to store his/her followers and she/he is as who's followers

    // user unique id
    public Integer user_id;

    // current user is followed by whom
    public Set<Integer> followers;

    // current user is following to who
    public Set<Integer> followTo;

    // all posted tweet list
    public List<Tweet> postedTweets;

    public TweetUser(int user_id) {
        this.user_id = user_id;

        this.postedTweets = new ArrayList<>();

        // here we init follower id list
        this.followers = new HashSet<>();

        // here we init follow id list
        this.followTo = new HashSet<>();

    }

    /**
     * every time user post a tweet his/her inner tweet's list
     * will be sorted by tweet.id which is the same order as tweet generated
     * timestamp inside mini-tweet system.
     *
     * @param tweet tweet posting by current user
     * @return
     */
    public void postATweet(Tweet tweet) {
        postedTweets.add(tweet);

        // refresh posted tweet order
        Collections.sort(postedTweets, (Tweet t1, Tweet t2) -> {
            return (-t1.id + t2.id);
        });
    }

    public List<Tweet> getLastPostedTweets() {
        // what if the length of the current user posted tweet's size is < 10?
        int n = Math.min(10, postedTweets.size());
        List<Tweet> ans = new ArrayList<>(n);

        int i = 0;
        while (i < n) {
            ans.add(postedTweets.get(i));
            i++;
        }

        return ans;
    }

    public List<Tweet> queryAllTopK(Map<Integer, TweetUser> userMap) {
        List<TweetUser> usersList = new ArrayList<>(userMap.keySet().size());


        for (Integer userId : followTo) {
            TweetUser user = userMap.get(userId);
            usersList.add(user);
        }


        // do not forget yourself
        usersList.add(this);

        // get each users' topK tweet
        List<Tweet> tweets = new ArrayList<>();

        usersList.forEach(user -> {
            List<Tweet> topNTweets = user.getLastPostedTweets();
            tweets.addAll(topNTweets);
        });

        // execute another sort and return result
        List<Tweet> ans = new ArrayList<>();
        Collections.sort(tweets, (t1, t2) -> {
            return (-t1.id + t2.id);
        });

        int len = Math.min(10, tweets.size());
        int i = 0;

        while (i < len) {
            ans.add(tweets.get(i));
            i++;
        }

        return ans;
    }
}

