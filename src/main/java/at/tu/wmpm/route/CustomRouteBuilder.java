package at.tu.wmpm.route;

import at.tu.wmpm.exception.DropboxLogException;
import at.tu.wmpm.exception.FacebookException;
import at.tu.wmpm.exception.MailException;
import at.tu.wmpm.exception.TwitterException;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pavol on 30.04.2015 Edited by christian on 19.05.2015 edited by
 * johannes on 31.05.2015
 */
public abstract class CustomRouteBuilder extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(CustomRouteBuilder.class);


    @Override
    public void configure() throws Exception {

        // Exception handling
        onException(MailException.class)
                .transform(simple("${exception.stacktrace}"))
                .handled(true)
                .to("file:logs/workingdir/exceptions/logMail?fileName=exception_mail_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");

        onException(FacebookException.class)
                .transform(simple("${exception.stacktrace}"))
                .handled(true)
                .to("file:logs/workingdir/exceptions/logFacebook?fileName=exception_facebook_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");

        onException(TwitterException.class)
                .transform(simple("${exception.stacktrace}"))
                .handled(true)
                .to("file:logs/workingdir/exceptions/logTwitter?fileName=exception_twitter_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");

        onException(DropboxLogException.class)
                .transform(simple("${exception.stacktrace}"))
                .handled(true)
                .to("file:logs/workingdir/exceptions/logDropbox?fileName=exception_dropbox_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");
    }
}
