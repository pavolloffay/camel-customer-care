package at.tu.wmpm.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import twitter4j.Status;
import at.tu.wmpm.model.TwitterBusinessCase;

/**
 * Twitter Processor
 * Created by Johannes on 31.5.2015.
 **/
@Service
public class TwitterProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(TwitterProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {

        log.debug("\nTweet recieved\n");

        Message in = exchange.getIn();
        Status status = (Status) in.getBody();

        TwitterBusinessCase tBC = new TwitterBusinessCase();

        tBC.setSender(status.getUser().getName());
        tBC.setIncomingDate(status.getCreatedAt().toString());
        tBC.setTweetID(status.getId());

        Message tweets = new DefaultMessage();
        tweets.setBody(tBC);
        exchange.setOut(tweets);
    }
}
