package com.interview.mae;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Mae
 * @Date: 2021/3/6 1:40 下午
 * <p>
 * after comparing posted tweet info should be hold in each user(class TweetUser) or tweeter system(class MiniTwitter)
 * I think cache inside user is a better solution, query methods can directly provide by user
 * and we don't need to organize a large middle hold cache in MiniTwitter.
 */
public class TweetUser {
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

    public List<Tweet> queryAllTopK( Map<Integer, TweetUser> userMap) {
        List<TweetUser> usersList = followTo.stream().map(userId -> {
            return userMap.get(userId);
        }).collect(Collectors.toList());

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

        int i = 0;

        while (i < 10) {
            ans.add(tweets.get(i));
            i++;
        }

        return ans;
    }
}
