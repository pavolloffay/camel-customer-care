package at.tu.wmpm.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pavol on 18.5.2015.
 */
public class VelocityProcessor implements Processor {

    private final static Logger log = LoggerFactory.getLogger(VelocityProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        log.debug("\n\n velocityMail:\n");
        log.debug(ReflectionToStringBuilder.toString(exchange) + "\n\n");
    }
}
