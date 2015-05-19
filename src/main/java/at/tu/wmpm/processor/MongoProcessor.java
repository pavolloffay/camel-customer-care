package at.tu.wmpm.processor;

import at.tu.wmpm.dao.impl.BusinessCaseDAO;
import at.tu.wmpm.model.BusinessCase;
import at.tu.wmpm.model.MailBusinessCase;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Created by pavol on 18.5.2015.
 */
@Service
public class MongoProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MongoProcessor.class);

    @Autowired
    private BusinessCaseDAO businessCaseDAO;

    @Override
    public void process(Exchange exchange) throws Exception {
        log.debug(ReflectionToStringBuilder.toString(exchange));

        Message message = exchange.getIn();
        MailBusinessCase businessCase = message.getBody(MailBusinessCase.class);

        BusinessCase oldBusinessCase = null;
        if (!businessCase.isNew()) {
            oldBusinessCase = businessCaseDAO.findById(businessCase.getParentId());
            businessCase.setParentId(oldBusinessCase.getId());
        }

        businessCaseDAO.save(businessCase);
    }
}
