package at.tu.wmpm.processor;

import at.tu.wmpm.model.FacebookBusinessCase;
import com.mongodb.BasicDBObject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by pavol on 8.5.2015.
 **/
@Service
public class MongoDbBusinessCaseProcessor {

    private static final Logger log = LoggerFactory
            .getLogger(MongoDbBusinessCaseProcessor.class);

    public void process(Exchange exchange) throws Exception {
        BasicDBObject dbObject = (BasicDBObject) exchange.getIn().getBody();

        Morphia morphia = new Morphia();
        morphia.map(FacebookBusinessCase.class);
        FacebookBusinessCase bc = morphia.fromDBObject(
                FacebookBusinessCase.class, dbObject);

        exchange.setPattern(ExchangePattern.InOut);
        Message m = exchange.getOut();
        if (bc.getFacebookPostId() != null) {
            m.setHeader("CamelFacebook.postId", bc.getFacebookPostId());
            m.setHeader("bc", bc);
        }
    }
}
