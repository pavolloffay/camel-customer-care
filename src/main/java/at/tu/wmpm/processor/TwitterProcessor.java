package at.tu.wmpm.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import twitter4j.Status;
import at.tu.wmpm.exception.TwitterException;
import at.tu.wmpm.model.Comment;
import at.tu.wmpm.model.TwitterBusinessCase;

/**
 * Twitter Processor Created by Johannes on 31.5.2015. Updated by Christian on
 * 18.06.2015
 **/
@Configuration
public class TwitterProcessor {

    private static final Logger log = LoggerFactory
            .getLogger(TwitterProcessor.class);

    public void process(Exchange exchange) throws TwitterException {

        log.debug("\nTweet recieved\n");

        Message in = exchange.getIn();
        Status status = (Status) in.getBody();

        TwitterBusinessCase tBC = new TwitterBusinessCase();

        tBC.setSender(status.getUser().getName());
        tBC.setIncomingDate(status.getCreatedAt().toString());
        tBC.setTweetID(status.getId());
        Comment c = new Comment();
        c.setDate(status.getCreatedAt());
        c.setFrom(tBC.getSender());
        c.setMessage(status.getText());
        log.debug("TWITTER MESSAGE: " + c.toString());
        tBC.addComment(c);

        Message tweets = new DefaultMessage();
        tweets.setBody(tBC);
        exchange.setOut(tweets);
    }
}