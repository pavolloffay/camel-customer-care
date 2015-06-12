package at.tu.wmpm.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import at.tu.wmpm.model.FacebookBusinessCase;

import com.mongodb.DBObject;

@Configuration
public class EmployeeFacebookSimulationProcessor {

    private static final Logger log = LoggerFactory
            .getLogger(EmployeeFacebookSimulationProcessor.class);

    public void commentOnFacebookBusinessCase(Exchange e) throws Exception {
        String fbMessage = "Have you tried turning it off and on again? ;-)";

        Message m = addComment(e, fbMessage);

        if (null == m) {
            throw new Exception("An error occured while processing message.");
        }
    }

    public void closeFacebookBusinessCase(Exchange e) throws Exception {
        String fbMessage = "Your ticket was successfully processed and is being closed now. Thank you for your patience. Have a nice day :-)";
        Message m = addComment(e, fbMessage);

        if (null == m) {
            throw new Exception("An error occured while processing message.");
        }

        // TODO Think about the reasonableness of closing a FacebookBusinessCase
        // (deleting a post) and therefore implement next steps
    }

    public Message addComment(Exchange e, String message) throws Exception {

        @SuppressWarnings("unchecked")
        List<DBObject> DBObjectList = e.getIn().getMandatoryBody(
                (Class<List<DBObject>>) (Class<?>) List.class);
        List<FacebookBusinessCase> businessCaseList = null;

        int count;
        Random random = new Random();
        FacebookBusinessCase chosenBusinessCase;
        Message m;
        String fbMessage = "";

        count = e.getIn().getHeader("CamelMongoDbResultTotalSize",
                Integer.class);
        log.info("Got " + count + " DBObjects according to header");
        if (null != DBObjectList)
            log.info("Got " + DBObjectList.size()
                    + " DBObjects according to list");

        if (null != DBObjectList) {
            businessCaseList = generateFacebookBusinessCases(DBObjectList);

            if (null != businessCaseList) {
                if (!businessCaseList.isEmpty()) {
                    chosenBusinessCase = businessCaseList.get(random
                            .nextInt(businessCaseList.size()));
                    log.info("Chosen BusinessCase: "
                            + chosenBusinessCase.getId());

                    if (chosenBusinessCase.getFacebookPostId() != null) {
                        fbMessage = "Dear " + chosenBusinessCase.getSender()
                                + "\n" + message + "\n\n" + "Yours sincerely,"
                                + "\n" + "The IT Crowd";
                        log.info(fbMessage);

                        m = e.getOut();
                        m.setHeader("CamelFacebook.postId",
                                chosenBusinessCase.getFacebookPostId());
                        m.setHeader("CamelFacebook.message", fbMessage);
                        m.setHeader("bc", chosenBusinessCase);

                        return m;
                    }
                }
            }
        }

        return null;
    }

    private List<FacebookBusinessCase> generateFacebookBusinessCases(
            List<DBObject> DBObjectList) {
        Morphia morphia = new Morphia();
        morphia.map(FacebookBusinessCase.class);
        List<FacebookBusinessCase> tempList = new ArrayList<FacebookBusinessCase>();
        FacebookBusinessCase tempObject = null;

        for (DBObject x : DBObjectList) {
            tempObject = morphia.fromDBObject(FacebookBusinessCase.class, x);

            if (null != tempObject)
                log.info("FacebookBusinessCase: " + tempObject.getId());
            else
                log.info("FacebookBusinessCase is null");

            tempList.add(tempObject);
        }

        return tempList;
    }
}