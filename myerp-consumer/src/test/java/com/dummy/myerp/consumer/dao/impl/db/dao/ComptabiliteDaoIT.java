package com.dummy.myerp.consumer.dao.impl.db.dao;


import com.dummy.myerp.model.bean.comptabilite.*;
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
public class ComptabiliteDaoIT {

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
    public void getListEcritureComptable_isNotEmpty() {
        List<EcritureComptable> list = dao.getListEcritureComptable();
        assertThat(list).isNotEmpty();
    }

    @Test
    public void GivenAnneeAndJournal_whenGetSequenceFromJournalAndAnnee_isNotNull() throws NotFoundException {
        SequenceEcritureComptable seq = dao.getSequenceFromJournalAndAnnee("AC",2016);
        assertThat(seq).isNotNull();
    }

    @Test(expected = NotFoundException.class)
    public void GivenAnneeAndJournal_whenGetSequenceFromJournalAndAnnee_throwsNotFoundException() throws NotFoundException {
        SequenceEcritureComptable seq = dao.getSequenceFromJournalAndAnnee("AC",2018);
    }

    @Test
    public void GivenId_whenGetEcritureComptable_isNotNull() throws NotFoundException {
        EcritureComptable ecritureComptable = dao.getEcritureComptable(-1);
        assertThat(ecritureComptable).isNotNull();
    }

    @Test(expected = NotFoundException.class)
    public void GivenId_whenGetEcritureComptable_throwsNotFoundException() throws NotFoundException {
        EcritureComptable ecritureComptable = dao.getEcritureComptable(1);
    }

    @Test
    public void GivenRef_whenGetEcritureComptableByRef_isNotNull() throws NotFoundException {
        EcritureComptable ecritureComptable = dao.getEcritureComptableByRef("AC-2016/00001");
        assertThat(ecritureComptable).isNotNull();
    }

    @Test(expected = NotFoundException.class)
    public void GivenId_whenGetEcritureComptableByRef_throwsNotFoundException() throws NotFoundException {
        EcritureComptable ecritureComptable = dao.getEcritureComptableByRef("AC-2000/00001");
    }

    @Test
    public void GivenId_whenLoadListLigneEcriture_isNotNull() throws NotFoundException {
        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.setId(-1);

        dao.loadListLigneEcriture(ecritureComptable);
        assertThat(ecritureComptable.getListLigneEcriture()).isNotEmpty();
    }

    @Test
    public void GivenEmptyId_whenLoadListLigneEcriture_isNull() throws NotFoundException {
        EcritureComptable ecritureComptable = new EcritureComptable();

        dao.loadListLigneEcriture(ecritureComptable);
        assertThat(ecritureComptable.getListLigneEcriture()).isEmpty();
    }


}
