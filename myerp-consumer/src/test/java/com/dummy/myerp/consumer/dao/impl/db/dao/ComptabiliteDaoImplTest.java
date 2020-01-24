package com.dummy.myerp.consumer.dao.impl.db.dao;

import com.dummy.myerp.model.bean.comptabilite.CompteComptable;
import com.dummy.myerp.model.bean.comptabilite.JournalComptable;
import com.dummy.myerp.model.bean.comptabilite.SequenceEcritureComptable;
import com.dummy.myerp.technical.exception.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/dummy/myerp/testconsumer/consumer/testContext.xml")
public class ComptabiliteDaoImplTest {

    @Autowired
    private ComptabiliteDaoImpl dao;


    @Test
    public void getListCompteComptable_isNotEmpty() {
        List<CompteComptable> list = dao.getListCompteComptable();

        assertThat(list).isNotEmpty();
    }

    @Test
    public void getListJournalComptable_isNotEmpty() {
        List<JournalComptable> list = dao.getListJournalComptable();
        assertThat(list).isNotEmpty();
    }

    @Test
    public void GivenAnneeAndJournal_whenGetSequenceFromJournalAndAnnee_isNotEmpty() throws NotFoundException {
        SequenceEcritureComptable seq = dao.getSequenceFromJournalAndAnnee("AC",2016);
        assertThat(seq).isNotNull();
    }
}
