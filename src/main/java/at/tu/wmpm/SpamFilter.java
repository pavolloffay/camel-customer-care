package at.tu.wmpm;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pavol on 18.5.2015.
 */
public class SpamFilter {

    public static final Logger log = LoggerFactory.getLogger(SpamFilter.class);


    public static boolean isNotSpam(@Body String body, @Header("Return-Path") String from) {

        if (body.contains("spam")) {
            log.info("spam from:{}, body:\n{}", from, body);
            return false;
        }

        return true;
    }
}
