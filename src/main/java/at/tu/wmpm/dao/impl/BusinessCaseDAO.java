package at.tu.wmpm.dao.impl;

import at.tu.beans.MailBean;
import at.tu.wmpm.dao.IBusinessCaseDAO;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.mongojack.*;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static String DB_NAME = "customerCare";
    private static String COLLECTION_NAME = "businessCases";

    @Autowired
    private Mongo mongo;


    private DB db;
    private DBCollection dbCollection;
    private JacksonDBCollection<MailBean, String> jacksonDBCollection;

//    private MongoClient mongoClient;
//    private MongoDatabase mongoDatabase;
//    private MongoCollection mongoCollection;


    @PostConstruct
    public void postConstruct() throws IOException {
        log.info("\n\nmongo DAO\n\n");

        if (mongo == null) {
            log.debug("\n\n\n\n is null");
        }
        db = mongo.getDB(DB_NAME);
        dbCollection = db.getCollection(COLLECTION_NAME);
        jacksonDBCollection = JacksonDBCollection.wrap(dbCollection, MailBean.class, String.class);

//        mongoClient = embeddedMongoFactoryBean.getObject();
//        mongoDatabase = mongoClient.getDatabase(DB_NAME);
//        mongoCollection = mongoDatabase.getCollection(COLLECTION_NAME);
    }

    @Override
    public void save(MailBean mailBean) {
        log.debug("Saving mail {}", mailBean);
        WriteResult<MailBean, String> result = jacksonDBCollection.insert(mailBean);
        mailBean.setId(result.getSavedId());
    }

    @Override
    public void remove(String id) {
        jacksonDBCollection.removeById(id);
    }

    @Override
    public List<MailBean> findAll() {
        return cursorToList(jacksonDBCollection.find());
    }

    @Override
    public MailBean findById(String id) {
        return jacksonDBCollection.findOneById(id);
    }

    private List<MailBean> cursorToList(org.mongojack.DBCursor<MailBean> dbCursor) {
        List<MailBean> mailBeans = new ArrayList<>();
        while (dbCursor.hasNext()) {
            MailBean mailBean = dbCursor.next();
            mailBeans.add(mailBean);
        }

        return mailBeans;
    }
}
