package at.tu.wmpm.route;

import javax.xml.bind.JAXBContext;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
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
public class TwitterRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(ExceptionRoute.class);

    @Autowired
    private WireTapLogTwitter wiretapTwitter;
    @Autowired
    private WireTapLogDropbox wiretapDropbox;
    @Autowired
    private TwitterProcessor twitterProcessor;

    @Value("${dropbox.auth.param}")
    private String DROPBOX_AUTH_PARAMETERS;


    @Override
    @SuppressWarnings("deprecation")
    public void configure() throws Exception {

        JAXBContext jaxbContext = JAXBContext.newInstance(BusinessCase.class);
        JaxbDataFormat jaxbFormat = new JaxbDataFormat(jaxbContext);

        from("twitter://timeline/home?type=polling&delay=10&consumerKey={{twitter.consumer.key}}&"
                + "consumerSecret={{twitter.consumer.secret}}&accessToken={{twitter.access.token}}&"
                + "accessTokenSecret={{twitter.access.token.secret}}")
                .wireTap("direct:logTwitter", wiretapTwitter)
                .process(twitterProcessor)
                .multicast()
                .parallelProcessing()
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=insert",
                        "direct:twitterToXml"/*, "direct:addToTWCalendar"*/);


        from("direct:twitterToXml")
                .marshal(jaxbFormat)
                .setHeader(Exchange.FILE_NAME, constant("twitter_ex.xml"))
                .to("file:logs/XMLExports?autoCreate=true")
                .recipientList(
                        simple("dropbox://put?"
                                + DROPBOX_AUTH_PARAMETERS
                                + "&uploadMode=add&localPath=logs/XMLExports/twitter_ex.xml&remotePath=/XMLExports/Twitter_${date:now:yyyyMMdd_HH-mm-SS}.xml"))
                .wireTap("direct:logDropbox", wiretapDropbox);

        from("direct:logTwitter")
                .to("file:logs/workingdir/wiretap-logs/logTwitter?fileName=twitter_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");


        from("direct:addToTWCalendar")
            .bean(CalendarProcessor.class, "process")
            .to("google-calendar://events/insert?calendarId={{google.calendar.id}}");
    }
}
