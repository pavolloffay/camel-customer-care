package at.tu.wmpm.dao;

import at.tu.beans.MailBean;

import java.util.List;

/**
 * Created by pavol on 17.5.2015.
 */
public interface IBusinessCaseDAO {
    void save(MailBean mailBean);
    void remove(String id);

    List<MailBean> findAll();
    MailBean findById(String id);
}
