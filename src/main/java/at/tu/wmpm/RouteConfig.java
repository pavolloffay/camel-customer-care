package at.tu.wmpm;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.tu.wmpm.filter.SpamFilter;
import at.tu.wmpm.processor.MailToXml;
import at.tu.wmpm.processor.FacebookProcessor;
import at.tu.wmpm.processor.MailProcessor;
import at.tu.wmpm.processor.MongoProcessor;
import at.tu.wmpm.model.BusinessCase;
import at.tu.wmpm.processor.*;

import javax.annotation.PostConstruct;

/**
 * Created by pavol on 30.04.2015 Edited by christian on 07.05.2015
 */
public class RouteConfig extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(RouteConfig.class);

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
    private MailToXml mailTranslator;

    @PostConstruct
    public void postConstruct() {
        log.debug("Configuring routes");
    }

    @Override
    public void configure() throws Exception {

        from("pop3s://{{eMailUserName}}@{{eMailPOPAddress}}:{{eMailPOPPort}}?password={{eMailPassword}}")
                .process(mailTranslator)
                .process(mailProcessor)
                .to("direct:spamChecking")
                .wireTap("direct:log");
        
        from("direct:log").to("file:target/reports/report.txt");

        from("direct:spamChecking")
                .filter().method(SpamFilter.class, "isNoSpam")
//                store to DB, load parent
                .process(mongoProcessor)
                .choice()
                    .when(body(BusinessCase.class).method("isNew").isEqualTo(true)).setHeader("Subject", body(BusinessCase.class).method("getId"))
                        .multicast().parallelProcessing().to("direct:autoReplyEmail", "direct:addToCalendar").endChoice()
                    .otherwise()
                        .to("direct:addToCalendar");

        from("direct:autoReplyEmail").process(autoReplyHeadersProcessor)
                .to("velocity:mail-templates/auto-reply.vm")
                .to("smtps://{{eMailSMTPAddress}}:{{eMailSMTPPort}}?password={{eMailPassword}}&username={{eMailUserName}}");


        /**
         * add calendar events for employees
         * forward event for employees
         */
        from("direct:addToCalendar").process(calendarProcessor); //.to("direct:careCenter")

        /**
         * process for care center employees
         * from(direct:careCenter).().(send email)
         */

        from("facebook://getTagged?reading.since=1.1.2015&userId={{FBpageId}}&oAuthAppId={{FBid}}&oAuthAppSecret={{FBsecret}}&oAuthAccessToken={{FBaccessToken}}")
                .process(facebookProcessor)
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=insert");
//        we could perform spam checking and then distinguish multiple paths for beans see body().isInstanceOf()
//            .to("direct:spam");
    }
}
