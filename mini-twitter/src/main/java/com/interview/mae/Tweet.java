package com.interview.mae;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * Tweet is already given in question so no extra info or fields should be added to this class.
 *
 * @Author: Mae
 * @Date: 2021/3/6 1:31 下午
 */
public class Tweet {
    public int id;
    public int user_id;
    public String text;

    public Tweet(int user_id, String text) {
        this.user_id = user_id;
        this.text = text;
    }

    public static Tweet create(int user_id, String tweet_text) {
        // this will create a new tweet object,
        // and auto fill id
        return new Tweet(user_id, tweet_text);
    }
}
