package at.tu.wmpm.processor;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;

import at.tu.wmpm.model.Comment;
import at.tu.wmpm.model.FacebookBusinessCase;
import facebook4j.Post;

/**
 * Created by pavol on 8.5.2015.
 *
 * Access token is probably valid only for short period of time
 *      get accessToken - https://developers.facebook.com/tools/explorer/145634995501895/
 *
 * Facebook app - Customer Care
 *      https://developers.facebook.com/apps/833818856683698/dashboard/
 *
 * Facebook page - Area 51 Customer Care
 *      https://www.facebook.com/area51customercare
 *
 **/
@Service
public class MongoDbBusinessCaseProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MongoDbBusinessCaseProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        BasicDBObject dbObject = (BasicDBObject)exchange.getIn().getBody();
        
            Morphia morphia = new Morphia();
            morphia.map(FacebookBusinessCase.class);
            FacebookBusinessCase bc = morphia.fromDBObject(FacebookBusinessCase.class, dbObject);	
        
            exchange.setPattern(ExchangePattern.InOut);
            Message m = exchange.getOut();
            if(bc.getFacebookPostId() != null){
            	m.setHeader("CamelFacebook.postId", bc.getFacebookPostId());
            	m.setHeader("bc", bc);
            }
            	                //log.debug(ReflectionToStringBuilder.toString(exchange));
   }


}
