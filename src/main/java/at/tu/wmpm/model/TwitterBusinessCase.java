package at.tu.wmpm.model;

public class TwitterBusinessCase extends BusinessCase {
    private long tweetID;

    private String screenName;

    public long getTweetID() {
        return tweetID;
    }

    public void setTweetID(long tweetID) {
        this.tweetID = tweetID;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }


}
