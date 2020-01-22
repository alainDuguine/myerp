package com.dummy.myerp.consumer.dao.impl.db.dao;

import com.dummy.myerp.model.bean.comptabilite.CompteComptable;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/dummy/myerp/testconsumer/consumer/testContext.xml")
public class ComptabiliteDaoImplTest {

    @Autowired
    private ComptabiliteDaoImpl dao;

    @Test
    public void getInstance() throws Exception {
        List<CompteComptable> list = dao.getListCompteComptable();
        System.out.println(list);
    }
}
