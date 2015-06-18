package at.tu.wmpm.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import at.tu.wmpm.exception.FacebookException;
import at.tu.wmpm.model.Comment;
import at.tu.wmpm.model.FacebookBusinessCase;
import facebook4j.Post;

/**
 * Created by pavol on 8.5.2015.
 **/
@Configuration
public class FacebookUpdatePostProcessor {

    private static final Logger log = LoggerFactory
            .getLogger(FacebookUpdatePostProcessor.class);

    public void process(Exchange exchange) throws FacebookException {

        FacebookBusinessCase bc = (FacebookBusinessCase) exchange.getIn()
                .getHeader("bc");

        Post post = (Post) exchange.getIn().getBody();

        if (post.getComments() != null) {
            for (facebook4j.Comment c : post.getComments()) {

                if (!bc.hasCommentWithId(c.getId())) {
                    Comment newComment = new Comment();
                    newComment.setFrom(c.getFrom().getName());
                    newComment.setMessage(c.getMessage());
                    newComment.setDate(c.getCreatedTime());
                    newComment.setId(c.getId());
                    bc.addComment(newComment);

                    log.debug("FB Comment added: " + c.getId());
                }
            }
        }

        Message message = new DefaultMessage();
        message.setBody(bc);

        exchange.setOut(message);
    }
}