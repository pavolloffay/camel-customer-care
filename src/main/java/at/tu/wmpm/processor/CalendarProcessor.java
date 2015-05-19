package at.tu.wmpm.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by pavol on 19.5.2015.
 */
@Service
public class CalendarProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(CalendarProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
//        log.debug(ReflectionToStringBuilder.toString(exchange));
    }
}
