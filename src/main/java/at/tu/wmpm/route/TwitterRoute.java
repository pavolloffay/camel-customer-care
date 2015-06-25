package at.tu.wmpm.route;

import javax.xml.bind.JAXBContext;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import at.tu.wmpm.model.BusinessCase;
import at.tu.wmpm.processor.CalendarProcessor;
import at.tu.wmpm.processor.TwitterProcessor;
import at.tu.wmpm.processor.WireTapLogDropbox;
import at.tu.wmpm.processor.WireTapLogTwitter;

/**
 * Created by pavol on 8.6.2015.
 */
@Component
public class TwitterRoute extends CustomRouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(CustomRouteBuilder.class);

    @Value("${dropbox.auth.param}")
    private String DROPBOX_AUTH_PARAMETERS;

    @Autowired
    private WireTapLogTwitter wiretapTwitter;
    @Autowired
    private WireTapLogDropbox wiretapDropbox;
    @Autowired
    private CalendarProcessor calendarProcessor;


    @Override
    @SuppressWarnings("deprecation")
    public void configure() throws Exception {
        super.configure();

        JAXBContext jaxbContext = JAXBContext.newInstance(BusinessCase.class);
        JaxbDataFormat jaxbFormat = new JaxbDataFormat(jaxbContext);

        from("twitter://timeline/home?type=polling&delay=900&consumerKey={{twitter.consumer.key}}&"
                        + "consumerSecret={{twitter.consumer.secret}}&accessToken={{twitter.access.token}}&"
                        + "accessTokenSecret={{twitter.access.token.secret}}")
                 .to("log:at.tu.wmpm.model.BusinessCase?level=INFO")
                .wireTap("direct:logTwitter", wiretapTwitter)
                .bean(TwitterProcessor.class, "process")
                .multicast()
                .parallelProcessing()
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.twitterBcCollection}}&operation=insert",
                        "seda:twitterToXml", "direct:addToTWCalendar");

        from("seda:twitterToXml?concurrentConsumers=3")
                .marshal(jaxbFormat)
                .setHeader(Exchange.FILE_NAME, constant("twitter_ex.xml"))
                .to("file:logs/XMLExports?autoCreate=true")
                .recipientList(
                        simple("dropbox://put?"
                                + DROPBOX_AUTH_PARAMETERS
                                + "&uploadMode=add&localPath=logs/XMLExports/twitter_ex.xml&remotePath=/XMLExports/Twitter_${date:now:yyyyMMdd_HH-mm-SS}.xml"))
                .log(LoggingLevel.INFO, org.slf4j.LoggerFactory.getLogger("CustomRouteBuilder.class"), "Twitter message was converted and uploaded to Dropbox")
                .wireTap("seda:logDropbox", wiretapDropbox);

        from("direct:logTwitter")
                .to("file:logs/workingdir/wiretap-logs/logTwitter?fileName=twitter_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");

        from("direct:addToTWCalendar")
                .bean(calendarProcessor, "process")
                .to("google-calendar://events/insert?calendarId={{google.calendar.id}}");
    }
}
