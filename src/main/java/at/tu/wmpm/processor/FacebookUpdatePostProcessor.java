package at.tu.wmpm.processor;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
public class FacebookUpdatePostProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(FacebookUpdatePostProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
    	 
    	FacebookBusinessCase bc = (FacebookBusinessCase)exchange.getIn().getHeader("bc");
    	    	
    	Post post = (Post)exchange.getIn().getBody();

        if(post.getComments() != null){
            for(facebook4j.Comment c: post.getComments()){
            	
            	if(!bc.hasCommentWithId(c.getId())){
	                Comment newComment = new Comment();
	                newComment.setFrom(c.getFrom().getName());
	                newComment.setMessage(c.getMessage());
	                newComment.setDate(c.getCreatedTime());
	                newComment.setId(c.getId());
	
	                bc.addComment(newComment);
	                
	                log.debug("FB Comment added: "+c.getId());
            	}
            }
        }
        
        Message message = new DefaultMessage();
        message.setBody(bc);

        exchange.setOut(message);

   }

}
