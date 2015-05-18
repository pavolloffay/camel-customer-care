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


        MailBusinessCase mailToSave = new MailBusinessCase();
        mailToSave.setSender(inHeaders.get("To").toString());
        mailToSave.setBody(inMessageBody);
        mailToSave.setSubject(inHeaders.get("Subject").toString());
        businessCaseDAO.save(mailToSave);
        for(MailBusinessCase mailBusinessCase: businessCaseDAO.findAll()) {
            log.debug(mailBusinessCase.toString());
        }

        Map<String, Object> outHeaders = prepareOutHeaders(inHeaders);
        Message message = new DefaultMessage();
        message.setHeaders(outHeaders);
        message.setHeader("CamelVelocityContext", getVelocityContext(inMessageBody));
        exchange.setOut(message);
    }

    private Map<String, Object> prepareOutHeaders(Map<String, Object> inHeaders) {
        Map<String, Object> outHeaders = new HashMap<String, Object>();
        outHeaders.put("To", inHeaders.get("Return-Path"));
        outHeaders.put("From", inHeaders.get("To"));
        outHeaders.put("Subject", "We received your request");

        return outHeaders;
    }

    private VelocityContext getVelocityContext(String oldBody) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss").format(Calendar
                .getInstance().getTime());
        oldBody = System.lineSeparator() + oldBody;
        oldBody = oldBody.replace(System.lineSeparator(), System.lineSeparator() + " >");

        Map<String, Object> velocityParams = new HashMap<>();
        velocityParams.put("timeStamp", timeStamp);
        velocityParams.put("oldBody", oldBody);

        return new VelocityContext(velocityParams);
    }
}
