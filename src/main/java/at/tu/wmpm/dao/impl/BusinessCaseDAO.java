package at.tu.wmpm.dao.impl;

import at.tu.wmpm.model.MailBusinessCase;
import at.tu.wmpm.dao.IBusinessCaseDAO;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import org.mongojack.*;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pavol on 17.5.2015.
 */
@Service
public class BusinessCaseDAO implements IBusinessCaseDAO {

    private static final Logger log = LoggerFactory.getLogger(BusinessCaseDAO.class);

    @Value("${mongodb.database}")
    private String DB_NAME;
    @Value("${mongodb.collection}")
    private String COLLECTION_NAME = "businessCases";

    @Autowired
    private Mongo mongo;

    private DB db;
    private DBCollection dbCollection;
    private JacksonDBCollection<MailBusinessCase, String> jacksonDBCollection;

//    private MongoClient mongoClient;
//    private MongoDatabase mongoDatabase;
//    private MongoCollection mongoCollection;


    @PostConstruct
    public void postConstruct() throws IOException {
        /**
         * remove old data
         */
        db = mongo.getDB(DB_NAME);
        dbCollection = db.getCollection(COLLECTION_NAME);
        dbCollection.drop();

        if (mongo == null) {
            log.debug("\n\n\n\n is null");
        }
        db = mongo.getDB(DB_NAME);
        dbCollection = db.getCollection(COLLECTION_NAME);
        jacksonDBCollection = JacksonDBCollection.wrap(dbCollection, MailBusinessCase.class, String.class);

//        mongoClient = embeddedMongoFactoryBean.getObject();
//        mongoDatabase = mongoClient.getDatabase(DB_NAME);
//        mongoCollection = mongoDatabase.getCollection(COLLECTION_NAME);
    }

    @Override
    public void save(MailBusinessCase mailBusinessCase) {
        log.debug("Saving mail {}", mailBusinessCase);
        WriteResult<MailBusinessCase, String> result = jacksonDBCollection.insert(mailBusinessCase);
        mailBusinessCase.setId(result.getSavedId());
    }

    @Override
    public void remove(String id) {
        jacksonDBCollection.removeById(id);
    }

    @Override
    public List<MailBusinessCase> findAll() {
        return cursorToList(jacksonDBCollection.find());
    }

    @Override
    public MailBusinessCase findById(String id) {
        return jacksonDBCollection.findOneById(id);
    }

    private List<MailBusinessCase> cursorToList(org.mongojack.DBCursor<MailBusinessCase> dbCursor) {
        List<MailBusinessCase> mailBusinessCases = new ArrayList<>();
        while (dbCursor.hasNext()) {
            MailBusinessCase mailBusinessCase = dbCursor.next();
            mailBusinessCases.add(mailBusinessCase);
        }

        return mailBusinessCases;
    }
}
