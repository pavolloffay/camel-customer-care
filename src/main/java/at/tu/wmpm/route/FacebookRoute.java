package at.tu.wmpm.route;

import javax.xml.bind.JAXBContext;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import at.tu.wmpm.model.BusinessCase;
import at.tu.wmpm.processor.CalendarProcessor;
import at.tu.wmpm.processor.FacebookProcessor;
import at.tu.wmpm.processor.FacebookUpdatePostProcessor;
import at.tu.wmpm.processor.MongoDbBusinessCaseProcessor;

/**
 * Created by pavol on 8.6.2015.
 */
@Component
@DependsOn({"google-calendar"})
public class FacebookRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(ExceptionRoute.class);

    @Value("${dropbox.auth.param}")
    private String DROPBOX_AUTH_PARAMETERS;

    @Autowired
    private FacebookProcessor facebookProcessor;
    @Autowired
    private FacebookUpdatePostProcessor facebookUpdatePostProcessor;
    @Autowired
    private MongoDbBusinessCaseProcessor mongoDbProcessor;


    @Override
    public void configure() throws Exception {

        JAXBContext jaxbContext = JAXBContext.newInstance(BusinessCase.class);
        JaxbDataFormat jaxbFormat = new JaxbDataFormat(jaxbContext);

        from("facebook://getTagged?reading.since=1.1.2015&userId={{facebook.page.id}}&consumer.delay=10000")
                .process(facebookProcessor)
                .multicast()
                .parallelProcessing()
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=insert",
                        "seda:facebookToXml"/*, "direct:addToFBCalendar"*/);


        from("direct:logFacebook")
                .to("file:logs/workingdir/wiretap-logs/logFacebook?fileName=facebook_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");

        from("timer://commentfetch?fixedRate=true&period=10000")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=findAll")
                .split(body()).process(mongoDbProcessor)
                .to("direct:processNewComments");

        from("direct:processNewComments")
                .to("facebook://post?postId="+header("FacebookCamel.postId"))
                .process(facebookUpdatePostProcessor)
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=save");

        from("seda:facebookToXml?concurrentConsumers=3")
                .marshal(jaxbFormat)
                .setHeader(Exchange.FILE_NAME, constant("ex2.xml"))
                .to("file:logs/XMLExports?autoCreate=true")
                .recipientList(
                        simple("dropbox://put?"
                                + DROPBOX_AUTH_PARAMETERS
                                + "&uploadMode=add&localPath=logs/XMLExports/ex2.xml&remotePath=/XMLExports/FB_${date:now:yyyyMMdd_HH-mm-SS}.xml"));

        from("direct:addToFBCalendar")
            .bean(CalendarProcessor.class, "process")
            .to("google-calendar://events/insert?calendarId={{google.calendar.id}}");

    }
}
