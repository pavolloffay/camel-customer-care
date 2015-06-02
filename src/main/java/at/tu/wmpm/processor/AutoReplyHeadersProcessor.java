package at.tu.wmpm.processor;

import at.tu.wmpm.model.MailBusinessCase;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pavol on 18.5.2015.
 */
@Service
public class AutoReplyHeadersProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(AutoReplyHeadersProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        log.debug(ReflectionToStringBuilder.toString(exchange));

        Message inMessage = exchange.getIn();
        MailBusinessCase mailBusinessCase = inMessage.getBody(MailBusinessCase.class);

        Message outMessage = new DefaultMessage();
        outMessage.setBody(mailBusinessCase);
        outMessage.setHeaders(prepareOutHeaders(mailBusinessCase));
        outMessage.setHeader("CamelVelocityContext", getVelocityContext(mailBusinessCase.getComments().get(0).getMessage()));
        exchange.setOut(outMessage);
    }

    private Map<String, Object> prepareOutHeaders(MailBusinessCase mailBusinessCase) {
        Map<String, Object> outHeaders = new HashMap<String, Object>();
        outHeaders.put("To", mailBusinessCase.getSender());
        outHeaders.put("From", "customer.care.tu.wien@gmail.com");
        outHeaders.put("Subject", "We have received your request, ID:" + mailBusinessCase.getId());

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
