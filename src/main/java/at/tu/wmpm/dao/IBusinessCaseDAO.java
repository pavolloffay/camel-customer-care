package at.tu.wmpm.dao;

import at.tu.wmpm.model.MailBusinessCase;

import java.util.List;

/**
 * Created by pavol on 17.5.2015.
 */
public interface IBusinessCaseDAO {
    void save(MailBusinessCase mailBean);
    void remove(String id);

    List<MailBusinessCase> findAll();
    MailBusinessCase findById(String id);
}
