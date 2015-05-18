package at.tu.wmpm.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by pavol on 18.5.2015.
 */
@Service
public class MongoProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MongoProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        log.debug(ReflectionToStringBuilder.toString(exchange));
    }
}
