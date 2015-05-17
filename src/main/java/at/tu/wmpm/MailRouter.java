package at.tu.wmpm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import at.tu.beans.MailBean;
import at.tu.wmpm.dao.IBusinessCaseDAO;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import javax.annotation.PostConstruct;

/**
 * Created by pavol on 30.04.2015 Edited by christian on 07.05.2015
 */
public class MailRouter extends RouteBuilder {

private static final Logger log = LoggerFactory.getLogger(MailRouter.class);

    @Autowired
    private IBusinessCaseDAO businessCaseDAO;

    @PostConstruct
    public void postConstruct() {
        log.debug("Mail component initialized");
    }

    @Override
    public void configure() throws Exception {

        from("pop3s://{{eMailUserName}}@{{eMailPOPAddress}}:{{eMailPOPPort}}?password={{eMailPassword}}")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        log.debug(ReflectionToStringBuilder.toString(exchange));

                        Message in = exchange.getIn();
                        String inMessageBody = in.getBody(String.class);
                        Map<String, Object> inHeaders = in.getHeaders();
                        log.debug("Mail body:\n" + inMessageBody + "\n");

                        Map<String, Object> outHeaders = prepareHeaders(inHeaders);
                        String newBody = prepareBody(in.getBody(String.class));

                        /**
                         * Send auto reply
                         */
                        CamelContext camel = new DefaultCamelContext();
                        ProducerTemplate template = camel.createProducerTemplate();
                        camel.addComponent("properties", new PropertiesComponent("accounts.properties"));
//                        TODO uncomment
//                        template.sendBodyAndHeaders(
//                                "smtps://{{eMailSMTPAddress}}:{{eMailSMTPPort}}?password={{eMailPassword}}&username={{eMailUserName}}",
//                                newBody, outHeaders);

                        log.debug("Successfully sent mail to {}", inHeaders.get("Return-Path"));


                        MailBean mailToSave = new MailBean();
                        mailToSave.setSender(inHeaders.get("To").toString());
                        mailToSave.setBody(inMessageBody);
                        mailToSave.setSubject(inHeaders.get("Subject").toString());
                        businessCaseDAO.save(mailToSave);

                        if (businessCaseDAO == null) {
                            log.error("business dao is null");
                        }
                        for(MailBean mailBean: businessCaseDAO.findAll()) {
                            log.debug(mailBean.toString());
                        }
                    }
                });
    }

    private String prepareBody(String oldBody) {
        String timeStamp = new SimpleDateFormat(
                "yyyy-MM-dd @ HH:mm:ss").format(Calendar
                .getInstance().getTime());

        String body = "Dear customer,\nWe received your mail and are currently processing the information\n\nbest regards, customer suppoprt\n\n\n"
                + "Original mail, received at "
                + timeStamp
                + "\n\n"
                + oldBody.replace(System.lineSeparator(), System.lineSeparator() + " >");

        return body;
    }

    private Map<String, Object> prepareHeaders(Map<String, Object> inHeaders) {
        Map<String, Object> outHeaders = new HashMap<String, Object>();
        outHeaders.put("To", inHeaders.get("Return-Path"));
        outHeaders.put("From", inHeaders.get("To"));
        outHeaders.put("Subject", "We received your request");

        return outHeaders;
    }
}
