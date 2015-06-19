package at.tu.wmpm.route;

import at.tu.wmpm.filter.SpamFilter;
import at.tu.wmpm.model.BusinessCase;
import at.tu.wmpm.processor.*;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;

/**
 * Created by pavol on 8.6.2015.
 */
@Component
@DependsOn({"google-calendar"})
public class MailRoute extends CustomRouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(CustomRouteBuilder.class);

    @Value("${dropbox.auth.param}")
    private String DROPBOX_AUTH_PARAMETERS;

    @Autowired
    private WireTapLogMail wiretapMail;
    @Autowired
    private MailProcessor mailProcessor;
    @Autowired
    private MailUpdateCommentsProcessor mailUpdateCommentsProcessor;
    @Autowired
    private AutoReplyHeadersProcessor autoReplyHeadersProcessor;
    @Autowired
    private CalendarProcessor calendarProcessor;


    @Override
    @SuppressWarnings("deprecation")
    public void configure() throws Exception {
        super.configure();

        JAXBContext jaxbContext = JAXBContext.newInstance(BusinessCase.class);
        JaxbDataFormat jaxbFormat = new JaxbDataFormat(jaxbContext);

        from("pop3s://{{mail.userName}}@{{mail.pop.address}}:{{mail.pop.port}}?password={{mail.password}}")
                .wireTap("seda:logMail", wiretapMail)
                .bean(mailProcessor, "process")
                .filter()
                .method(SpamFilter.class, "isNoSpam")
                .choice()
                    .when(header("hasParent").isEqualTo(true))
                        .to("direct:mailUpdateComment")
                    .otherwise()
                        .multicast()
                        .parallelProcessing()
                        .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.mailBcCollection}}&operation=insert",
                                "direct:autoReplyEmail", "direct:addToCalendar",
                                "seda:storeXMLEmail")
                .end();

        from("direct:mailUpdateComment")
                .transform(body(BusinessCase.class).method("getParentId"))
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.mailBcCollection}}&operation=findById")
                .bean(mailUpdateCommentsProcessor, "process")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.mailBcCollection}}&operation=save");

        from("seda:logMail?concurrentConsumers=3")
                .to("file:logs/workingdir/wiretap-logs/logMail?fileName=mail_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");

        from("direct:autoReplyEmail")
                .bean(autoReplyHeadersProcessor, "process")
                .to("velocity:mail-templates/auto-reply.vm")
                .to("smtps://{{mail.smtp.address}}:{{mail.smtp.port}}?password={{mail.password}}&username={{mail.userName}}");

        from("direct:addToCalendar")
                .bean(calendarProcessor, "process")
                .to("google-calendar://events/insert?calendarId={{google.calendar.id}}");

        from("seda:storeXMLEmail?concurrentConsumers=3")
                .marshal(jaxbFormat)
                .setHeader(Exchange.FILE_NAME, constant("ex1.xml"))
                .to("file:logs/XMLExports?autoCreate=true")
                .recipientList(
                        simple("dropbox://put?"
                                + DROPBOX_AUTH_PARAMETERS
                                + "&uploadMode=add&localPath=logs/XMLExports/ex1.xml&remotePath=/XMLExports/M_${date:now:yyyyMMdd_HH-mm-SS}.xml"));
    }
}
