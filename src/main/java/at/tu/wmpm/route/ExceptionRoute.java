package at.tu.wmpm.route;


import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tu.wmpm.exception.DropboxLogException;
import at.tu.wmpm.exception.FacebookException;
import at.tu.wmpm.exception.MailException;
import at.tu.wmpm.exception.TwitterException;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Created by pavol on 30.04.2015 Edited by christian on 19.05.2015 edited by
 * johannes on 31.05.2015
 */
@Component
@DependsOn({"google-calendar", "facebookConfiguration"})
public class ExceptionRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(ExceptionRoute.class);

    @Override
    public void configure() throws Exception {

        // Exception handling
        onException(MailException.class).continued(true).to(
                "direct:logMailException"); // TODO directly
        // .to(file:logs/workingdir/excp/logMail)
        // ??
        onException(FacebookException.class).continued(true).to(
                "direct:logFacebookException");
        onException(TwitterException.class).continued(true).to(
                "direct:logTwitterException");
        onException(DropboxLogException.class).continued(true).to(
                "direct:logDropboxException");
        from("direct:logMailException")
                .to("file:logs/workingdir/exceptions/logMail?fileName=exception_mail_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");
        from("direct:logFacebookException")
                .to("file:logs/workingdir/exceptions/logFacebook?fileName=exception_facebook_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");
        from("direct:logTwitterException")
                .to("file:logs/workingdir/exceptions/logTwitter?fileName=exception_twitter_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");
        from("direct:logDropboxException")
                .to("file:logs/workingdir/exceptions/logDropbox?fileName=exception_dropbox_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");
    }
}
