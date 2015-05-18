package at.tu.wmpm.processor;

import at.tu.wmpm.dao.IBusinessCaseDAO;
import at.tu.wmpm.model.MailBusinessCase;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pavol on 18.5.2015.
 */
@Service
public class MailProcessor implements Processor {

    private final Logger log = LoggerFactory.getLogger(MailProcessor.class);

    @Autowired
    private IBusinessCaseDAO businessCaseDAO;

    @Override
    public void process(Exchange exchange) throws Exception {
        log.debug(ReflectionToStringBuilder.toString(exchange));

        Message in = exchange.getIn();
        String inMessageBody = in.getBody(String.class);
        Map<String, Object> inHeaders = in.getHeaders();
        log.debug("\n\nMail body:\n" + inMessageBody + "\n");

        Map<String, Object> outHeaders = prepareHeaders(inHeaders);
        String newBody = prepareBody(in.getBody(String.class));

        /**
         * Send auto reply
         */
//                        CamelContext camel = new DefaultCamelContext();
//                        ProducerTemplate template = camel.createProducerTemplate();
//                        camel.addComponent("properties", new PropertiesComponent("accounts.properties"));
//                        TODO uncomment
//                        template.sendBodyAndHeaders(
//                                "smtps://{{eMailSMTPAddress}}:{{eMailSMTPPort}}?password={{eMailPassword}}&username={{eMailUserName}}",
//                                newBody, outHeaders);

        log.debug("Successfully sent mail to {}", inHeaders.get("Return-Path"));

        MailBusinessCase mailToSave = new MailBusinessCase();
        mailToSave.setSender(inHeaders.get("To").toString());
        mailToSave.setBody(inMessageBody);
        mailToSave.setSubject(inHeaders.get("Subject").toString());
        businessCaseDAO.save(mailToSave);

        if (businessCaseDAO == null) {
            log.error("business dao is null");
        }
        for(MailBusinessCase mailBusinessCase: businessCaseDAO.findAll()) {
            log.debug(mailBusinessCase.toString());
        }

        /**
         * Set up velocity parameters
         */
        Map<String, Object> velocityParams = new HashMap<>();
        velocityParams.put("timeStamp", new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss").format(Calendar
                .getInstance().getTime()));
        velocityParams.put("oldBody", inMessageBody.replace(System.lineSeparator(), System.lineSeparator() + " >"));
        VelocityContext velocityContext = new VelocityContext(velocityParams);

//                        exchange.getIn().setHeader("CamelVelocityContext", velocityContext);
        Message message = new DefaultMessage();
        message.setHeaders(outHeaders);
        message.setHeader("CamelVelocityContext", velocityContext);
        exchange.setOut(message);
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
