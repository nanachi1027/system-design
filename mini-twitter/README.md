#### Mini-Twitter 
This is a system design question in lintcode: 
[link](https://www.lintcode.com/problem/501/?_from=ladder&fromId=8)

#### Description 
Implement a mini-twitter in which support several services/APIs/methods shown below:

1. `postTweet(user_id, tweet_text)` : publish a twitter 

2. `getTimeline(user_id)` : retrieve the user(user_id)'s recently published 10 twitter contents sorted by each twitter's timestamp. 

3. `getNewsFeed(user_id)` : retrieve specified user herself/himself or her/his friends' recently published lastest 10 twitter contents, also sorted by timestamp from new to old; 

4. `follow(from_user_id, to_user_id)` : from_user_id will follow to_user_id 

5. `unfollow(from_user_id, to_user_id)` : from_user_id will not follow to_user_id anymore 

#### Input 1
```java
postTweet(1, "LintCode is Good")
getNewsFeed(1)
getTimeline(1)
follow(2, 1)
getNewsFeed(2)
unfollow(2,1)
getNewsFeed(2)
```

#### Ouput 1
```java
1
[1]
[1]
[1]
[]
```


#### Input2 
```java
postTweet(1, "LintCode is Good!!!")
getNewsFeed(1)
getTimeline(1)
follow(2,1)
postTweet(1, "LintCode is best!!!!")
getNewsFeed(2)
unfollow(2, 1)
getNewsFeed(2)
```


#### Output2
```java
1
[1]
[1]
2
[2,1]
[]
```

#### post-mortem
* If no apis are given to decompose this model's feature, how to decompose the integration question into apis?    













 