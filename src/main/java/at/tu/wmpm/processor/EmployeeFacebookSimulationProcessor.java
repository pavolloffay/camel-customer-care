package at.tu.wmpm.processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import at.tu.wmpm.model.BusinessCase;
import at.tu.wmpm.model.Comment;
import at.tu.wmpm.model.FacebookBusinessCase;
import at.tu.wmpm.model.MailBusinessCase;
import at.tu.wmpm.model.TwitterBusinessCase;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Configuration
public class EmployeeFacebookSimulationProcessor {

    private static final Logger log = LoggerFactory
            .getLogger(EmployeeFacebookSimulationProcessor.class);

    public void process(Exchange e) throws Exception {

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
            businessCaseList = generateBusinessCases(DBObjectList);

            if (null != businessCaseList) {
                if (!businessCaseList.isEmpty()) {
                    chosenBusinessCase = businessCaseList.get(random
                            .nextInt(businessCaseList.size()));
                    log.info("Chosen BusinessCase: "
                            + chosenBusinessCase.getId());

                    if (chosenBusinessCase.getFacebookPostId() != null) {
                        fbMessage = "Dear "
                                + chosenBusinessCase.getSender()
                                + "\n"
                                + "Have you tried turning it off and on again? ;-)";
                        log.info(fbMessage);

                        m = e.getOut();
                        m.setHeader("CamelFacebook.postId",
                                chosenBusinessCase.getFacebookPostId());
                        m.setHeader("CamelFacebook.message", fbMessage);
                        m.setHeader("bc", chosenBusinessCase);
                    }
                }
            }
        }

        // in.setHeader("CamelGoogleCalendar.content",
        // (com.google.api.services.calendar.model.Event) event);
        // in.setBody((com.google.api.services.calendar.model.Event) event);
        // e.setOut(in);
    }

    private List<FacebookBusinessCase> generateBusinessCases(
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