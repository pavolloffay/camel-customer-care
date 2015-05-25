package at.tu.wmpm;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import at.tu.wmpm.exception.FacebookException;
import at.tu.wmpm.exception.MailException;
import at.tu.wmpm.exception.TwitterException;
import at.tu.wmpm.filter.SpamFilter;
import at.tu.wmpm.model.BusinessCase;
import at.tu.wmpm.processor.AutoReplyHeadersProcessor;
import at.tu.wmpm.processor.CalendarProcessor;
import at.tu.wmpm.processor.FacebookProcessor;
import at.tu.wmpm.processor.MailProcessor;
import at.tu.wmpm.processor.MongoProcessor;
import at.tu.wmpm.processor.WireTapLogFacebook;
import at.tu.wmpm.processor.WireTapLogMail;
import at.tu.wmpm.processor.WireTapLogTwitter;

/**
 * Created by pavol on 30.04.2015 Edited by christian on 19.05.2015
 */
public class RouteConfig extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(RouteConfig.class);

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

    @PostConstruct
    public void postConstruct() {
        log.debug("Configuring routes");
        DROPBOX__AUTH_PARAMETERS = "accessToken=" + DROPBOX_ACCESS_TOKEN + "&clientIdentifier=" +  DROPBOX_CLIENT_IDENTIFIER;
    }

    @SuppressWarnings({"deprecation"})
    @Override
    public void configure() throws Exception {

        JAXBContext jaxbContext = JAXBContext.newInstance(BusinessCase.class);
        JaxbDataFormat jaxbFormat = new JaxbDataFormat(jaxbContext);
        String filename = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date())+".xml";

        // Exception handling
        onException(MailException.class).continued(true)
                .to("direct:logMailException"); //TODO directly .to(file:logs/excp/logMail) ??
        onException(FacebookException.class).continued(true)
                .to("direct:logFacebookException");
        onException(TwitterException.class).continued(true)
                .to("direct:logTwitterException");
        from("direct:logMailException")
                .to("file:logs/exceptions/logMail");
        from("direct:logFacebookException")
                .to("file:logs/exceptions/logFacebook");
        from("direct:logTwitterException")
                .to("file:logs/exceptions/logTwitter");

        /**
         * E-Mail Channel
         */
        from("pop3s://{{mail.userName}}@{{mail.pop.address}}:{{mail.pop.port}}?password={{mail.password}}")
                .wireTap("direct:logMail", wiretapMail)
                //.process(mailTranslator)
                .process(mailProcessor)
                //.multicast().parallelProcessing()
                .to("direct:spamChecking");

        from("direct:storeXMLEmail")
                .marshal(jaxbFormat).setHeader(Exchange.FILE_NAME, constant("ex1.xml"))
                .to("file:logs/XMLExports?autoCreate=true")
                .to("dropbox://put?" + DROPBOX__AUTH_PARAMETERS + "&uploadMode=add&localPath=logs/XMLExports/ex1.xml&remotePath=/XMLExports/"+filename);

        from("direct:logMail")
                .to("file:logs/wiretap-logs/logMail");

        from("direct:spamChecking")
                .filter().method(SpamFilter.class, "isNoSpam")
                // store to DB, load parent
                .process(mongoProcessor).choice()
                .when(body(BusinessCase.class).method("isNew").isEqualTo(true))
                    .setHeader("Subject", body(BusinessCase.class).method("getId"))
                    .multicast().parallelProcessing()
                    .to("direct:autoReplyEmail", "direct:addToCalendar", "direct:storeXMLEmail").endChoice()
                .otherwise()
                    .to("direct:addToCalendar");

        from("direct:autoReplyEmail")
                .process(autoReplyHeadersProcessor)
                .to("velocity:mail-templates/auto-reply.vm")
                .to("smtps://{{mail.smtp.address}}:{{mail.smtp.port}}?password={{mail.password}}&username={{mail.userName}}");

        /**
         * add calendar events for employees forward event for employees
         */
        from("direct:addToCalendar")
                .process(calendarProcessor);
        // //.to("google-calendar:createNewEvent")

        /**
         * process for care center employees from(direct:careCenter).to(smtp send email)
         */

        /**
         * Facebook Channel
         */
        from("facebook://getTagged?reading.since=1.1.2015&userId={{facebook.page.id}}")
                .process(facebookProcessor)
                .multicast().parallelProcessing()
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=insert", "direct:facebookToXml");
        // we could perform spam checking and then distinguish multiple paths
        // for beans see body().isInstanceOf()
        // .to("direct:spam");

        from("direct:logFacebook")
                .to("file:logs/wiretap-logs/logFacebook");

        from("direct:facebookToXml")
                .marshal(jaxbFormat).setHeader(Exchange.FILE_NAME, constant("ex2.xml"))
                .to("file:logs/XMLExports?autoCreate=true")
                .to("dropbox://put?" + DROPBOX__AUTH_PARAMETERS + "&uploadMode=add&localPath=logs/XMLExports/ex2.xml&remotePath=/XMLExports/"+filename);

        /**
         * TODO remove - just test for google-calendar
         */
        from("google-calendar://calendars/get?calendarId={{google.calendar.id}}")
            .process(calendarProcessor);
    }
}
