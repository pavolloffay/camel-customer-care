package at.tu.wmpm;

//import java.text.SimpleDateFormat;
//import java.util.Date;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import at.tu.wmpm.exception.DropboxLogException;
import at.tu.wmpm.exception.FacebookException;
import at.tu.wmpm.exception.MailException;
import at.tu.wmpm.exception.TwitterException;
import at.tu.wmpm.filter.SpamFilter;
import at.tu.wmpm.model.BusinessCase;
import at.tu.wmpm.model.FacebookBusinessCase;
import at.tu.wmpm.processor.AutoReplyHeadersProcessor;
import at.tu.wmpm.processor.CalendarProcessor;
import at.tu.wmpm.processor.FacebookProcessor;
import at.tu.wmpm.processor.FileAggregationStrategy;
import at.tu.wmpm.processor.FileProcessor;
import at.tu.wmpm.processor.MailProcessor;
import at.tu.wmpm.processor.MongoProcessor;
import at.tu.wmpm.processor.TwitterProcessor;
import at.tu.wmpm.processor.WireTapLogDropbox;
import at.tu.wmpm.processor.WireTapLogFacebook;
import at.tu.wmpm.processor.WireTapLogMail;
import at.tu.wmpm.processor.WireTapLogTwitter;

import com.mongodb.BasicDBObject;

/**
 * Created by pavol on 30.04.2015 Edited by christian on 19.05.2015 edited by johannes on 31.05.2015
 */
public class RouteConfig extends RouteBuilder {

    private static final Logger log = LoggerFactory
            .getLogger(RouteConfig.class);

    @Value("${dropbox.access.token}")
    private String DROPBOX_ACCESS_TOKEN;
    @Value("${dropbox.client.identifier}")
    private String DROPBOX_CLIENT_IDENTIFIER;
    private String DROPBOX__AUTH_PARAMETERS;

    @Autowired
    private MailProcessor mailProcessor;
    @Autowired
    private FacebookProcessor facebookProcessor;
    @Autowired
    private MongoProcessor mongoProcessor;
    @Autowired
    private AutoReplyHeadersProcessor autoReplyHeadersProcessor;
    @Autowired
    private CalendarProcessor calendarProcessor;
    @Autowired
    private WireTapLogMail wiretapMail;
    @Autowired
    private WireTapLogFacebook wiretapFacebook;
    @Autowired
    private WireTapLogTwitter wiretapTwitter;
    @Autowired
    private WireTapLogDropbox wiretapDropbox;
    @Autowired
    private TwitterProcessor twitterProcessor;
    @Autowired
    private FileAggregationStrategy faStrategy;
    @Autowired
    private FileProcessor fileProcessor;

    @PostConstruct
    public void postConstruct() {
        log.debug("Configuring routes");
        DROPBOX__AUTH_PARAMETERS = "accessToken=" + DROPBOX_ACCESS_TOKEN
                + "&clientIdentifier=" + DROPBOX_CLIENT_IDENTIFIER;
    }

    @SuppressWarnings({ "deprecation" })
    @Override
    public void configure() throws Exception {

        JAXBContext jaxbContext = JAXBContext.newInstance(BusinessCase.class);
        JaxbDataFormat jaxbFormat = new JaxbDataFormat(jaxbContext);
        // String filename = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss")
        // .format(new Date()) + ".xml";

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

        /**
         * E-Mail Channel
         */
        from(
                "pop3s://{{mail.userName}}@{{mail.pop.address}}:{{mail.pop.port}}?password={{mail.password}}")
                .wireTap("direct:logMail", wiretapMail)
                // .process(mailTranslator)
                .process(mailProcessor)
                // .multicast().parallelProcessing()
                .to("direct:spamChecking");

        from("direct:storeXMLEmail")
                .marshal(jaxbFormat)
                .setHeader(Exchange.FILE_NAME, constant("ex1.xml"))
                .to("file:logs/XMLExports?autoCreate=true")
                .recipientList(
                        simple("dropbox://put?"
                                + DROPBOX__AUTH_PARAMETERS
                                + "&uploadMode=add&localPath=logs/XMLExports/ex1.xml&remotePath=/XMLExports/M_${date:now:yyyyMMdd_HH-mm-SS}.xml"));

        from("direct:logMail")
                .to("file:logs/workingdir/wiretap-logs/logMail?fileName=mail_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");

        from("direct:spamChecking")
                .filter()
                .method(SpamFilter.class, "isNoSpam")
                // store to DB, load parent
                .process(mongoProcessor)
                .choice()
                .when(body(BusinessCase.class).method("isNew").isEqualTo(true))
                .setHeader("Subject", body(BusinessCase.class).method("getId"))
                .multicast()
                .parallelProcessing()
                .to("direct:autoReplyEmail", "direct:addToCalendar",
                        "direct:storeXMLEmail").endChoice().otherwise()
                .to("direct:addToCalendar");

        from("direct:autoReplyEmail")
                .process(autoReplyHeadersProcessor)
                .to("velocity:mail-templates/auto-reply.vm")
                .to("smtps://{{mail.smtp.address}}:{{mail.smtp.port}}?password={{mail.password}}&username={{mail.userName}}");

        /**
         * add calendar events for employees forward event for employees
         */
        from("direct:addToCalendar").process(calendarProcessor);
        // //.to("google-calendar:createNewEvent")

        /**
         * process for care center employees from(direct:careCenter).to(smtp
         * send email)
         */

        /**
         * Facebook Channel
         */
        from(
                "facebook://getTagged?reading.since=1.1.2015&userId={{facebook.page.id}}&consumer.delay=10000")
                .process(facebookProcessor)
                .multicast()
                .parallelProcessing()
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=insert",
                        "direct:facebookToXml");
        // we could perform spam checking and then distinguish multiple paths
        // for beans see body().isInstanceOf()
        // .to("direct:spam");

        from("direct:logFacebook")
                .to("file:logs/workingdir/wiretap-logs/logFacebook?fileName=facebook_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");

        from("timer://commentfetch?fixedRate=true&period=10000").to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=findAll").split(body()).process(new Processor() {
            public void process(Exchange exchange) throws Exception {
                BasicDBObject dbObject = (BasicDBObject)exchange.getIn().getBody();
                
	                Morphia morphia = new Morphia();
	                morphia.map(FacebookBusinessCase.class);
	                FacebookBusinessCase bc = morphia.fromDBObject(FacebookBusinessCase.class, dbObject);	                
                
                // do something with the payload and/or exchange here
           }
        });
        
        from("direct:facebookToXml")
                .marshal(jaxbFormat)
                .setHeader(Exchange.FILE_NAME, constant("ex2.xml"))
                .to("file:logs/XMLExports?autoCreate=true")
                .recipientList(
                        simple("dropbox://put?"
                                + DROPBOX__AUTH_PARAMETERS
                                + "&uploadMode=add&localPath=logs/XMLExports/ex2.xml&remotePath=/XMLExports/FB_${date:now:yyyyMMdd_HH-mm-SS}.xml"));
        /**
         * Twitter Channel
         */

        from("twitter://timeline/home?type=polling&delay=10&consumerKey={{twitter.consumer.key}}&"
                + "consumerSecret={{twitter.consumer.secret}}&accessToken={{twitter.access.token}}&"
                + "accessTokenSecret={{twitter.access.token.secret}}")
                .wireTap("direct:logTwitter", wiretapTwitter)
                .process(twitterProcessor)
                .multicast()
                .parallelProcessing()
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=insert",
                        "direct:twitterToXml");

        from("direct:twitterToXml")
                .marshal(jaxbFormat)
                .setHeader(Exchange.FILE_NAME, constant("twitter_ex.xml"))
                .to("file:logs/XMLExports?autoCreate=true")
                .recipientList(
                        simple("dropbox://put?"
                                + DROPBOX__AUTH_PARAMETERS
                                + "&uploadMode=add&localPath=logs/XMLExports/twitter_ex.xml&remotePath=/XMLExports/Twitter_${date:now:yyyyMMdd_HH-mm-SS}.xml"))
                .wireTap("direct:logDropbox", wiretapDropbox);

        from("direct:logTwitter")
        .to("file:logs/wiretap-logs/logTwitter?fileName=twitter_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");


        /**
         * TODO remove - just test for google-calendar
         */
        from(
                "google-calendar://calendars/get?calendarId={{google.calendar.id}}")
                .process(calendarProcessor);

        /**
         * Backup Logs to dropbox every 30 seconds (interval currently set for
         * testing purposes)
         */
        from(
                "file:logs/workingdir?recursive=true&delete=false&scheduler=quartz2&scheduler.cron=0/30+*+*+*+*+?")
                .process(fileProcessor)
                .aggregate(constant(true), faStrategy)
                .completionFromBatchConsumer()
                .to("file:logs/forDropbox?fileName=forDropbox.txt")
                .recipientList(
                        simple("dropbox://put?"
                                + DROPBOX__AUTH_PARAMETERS
                                + "&uploadMode=add&localPath=logs/forDropbox/forDropbox.txt&remotePath=/logs/backup_log_${date:now:yyyyMMdd_HH-mm-SS}.txt"))
                .wireTap("direct:logDropbox", wiretapDropbox);

        from("direct:logDropbox")
                .to("file:logs/workingdir/wiretap-logs/logDropbox?fileName=upload_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");

    }
}
