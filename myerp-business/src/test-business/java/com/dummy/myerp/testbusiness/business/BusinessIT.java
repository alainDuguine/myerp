package com.dummy.myerp.testbusiness.business;

import com.dummy.myerp.business.contrat.BusinessProxy;
import com.dummy.myerp.business.impl.AbstractBusinessManager;
import com.dummy.myerp.business.impl.TransactionManager;
import com.dummy.myerp.consumer.dao.contrat.DaoProxy;
import com.dummy.myerp.model.bean.comptabilite.*;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/com/dummy/myerp/testbusiness/business/testContext.xml")
public class BusinessIT extends AbstractBusinessManager{

    @Autowired
    private BusinessProxy business;
    @Autowired
    private DaoProxy dao;
    @Autowired
    private TransactionManager transactionManager;

    @Before
    public void init() {
        configure(business, dao, transactionManager);
    }

    @Test
    public void givenEcritureWithoutRG2_WhenInsertEcritureComptable_NotPersisted(){
        List<EcritureComptable> listEcriture = getBusinessProxy().getComptabiliteManager().getListEcritureComptable();
        EcritureComptable ecritureComptable = this.getNewGoodEcritureComptable();
        ecritureComptable.getListLigneEcriture().get(1).setCredit(new BigDecimal(1300));

        //when
        String message = null;
        try {
            getBusinessProxy().getComptabiliteManager().insertEcritureComptable(ecritureComptable);
        } catch (FunctionalException e) {
            message = e.getMessage();
        }finally {
            //then
            assertThat(getBusinessProxy().getComptabiliteManager().getListEcritureComptable().size()).isEqualTo(listEcriture.size());
            assertThat(message).isEqualTo("L'écriture comptable n'est pas équilibrée.");
        }
    }

    @Test
    public void givenEcritureWithoutRG3_WhenInsertEcritureComptable_NotPersisted(){
        List<EcritureComptable> listEcriture = getBusinessProxy().getComptabiliteManager().getListEcritureComptable();
        EcritureComptable ecritureComptable = this.getNewGoodEcritureComptable();
        ecritureComptable.getListLigneEcriture().get(1).setDebit(new BigDecimal(-1500));
        ecritureComptable.getListLigneEcriture().get(1).setCredit(null);

        //when
        String message = null;
        try {
            getBusinessProxy().getComptabiliteManager().insertEcritureComptable(ecritureComptable);
        } catch (FunctionalException e) {
            message = e.getMessage();
        }finally {
            //then
            assertThat(getBusinessProxy().getComptabiliteManager().getListEcritureComptable().size()).isEqualTo(listEcriture.size());
            assertThat(message).isEqualTo("L'écriture comptable doit avoir au moins deux lignes : une ligne au débit et une ligne au crédit.");
        }
    }

    @Test
    @Ignore("Test doesn't pass while no implementation for insertSequence")
    public void checkRG4_givenEcritureWithNegativeNumber_WhenInsertEcritureComptable_Persisted() throws NotFoundException {
        List<EcritureComptable> listEcriture = getBusinessProxy().getComptabiliteManager().getListEcritureComptable();
        EcritureComptable ecritureComptable = this.getNewGoodEcritureComptable();
        ecritureComptable.getListLigneEcriture().get(0).setDebit(new BigDecimal(-1500));
        ecritureComptable.getListLigneEcriture().get(1).setCredit(new BigDecimal(-1500));

        //when
        String message = null;
        try {
            getBusinessProxy().getComptabiliteManager().insertEcritureComptable(ecritureComptable);
        } catch (FunctionalException e) {
            message = e.getMessage();
        }finally {
            //then
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ecritureComptable.getDate());
            int yearInRef = calendar.get(Calendar.YEAR);
            assertThat(getBusinessProxy().getComptabiliteManager().getSequenceFromJournalAndAnnee(ecritureComptable.getJournal().getCode(), yearInRef));
            assertThat(getBusinessProxy().getComptabiliteManager().getListEcritureComptable().size()).isEqualTo(listEcriture.size()+1);
        }
    }

    private EcritureComptable getNewGoodEcritureComptable(){
        List<CompteComptable> listCompte = getBusinessProxy().getComptabiliteManager().getListCompteComptable();
        List<JournalComptable> listJournal = getBusinessProxy().getComptabiliteManager().getListJournalComptable();
        //given
        JournalComptable journalComptable = listJournal.get(0);
        CompteComptable compteComptable1 = listCompte.get(0);
        CompteComptable compteComptable2 = listCompte.get(1);

        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.setLibelle("IntegrationTest");
        ecritureComptable.setJournal(journalComptable);
        ecritureComptable.setDate(new Date());

        LigneEcritureComptable ligne1 = new LigneEcritureComptable();
        ligne1.setCompteComptable(compteComptable1);
        ligne1.setLibelle("IntegrationTest");
        ligne1.setDebit(new BigDecimal(1500));

        LigneEcritureComptable ligne2 = new LigneEcritureComptable();
        ligne2.setCompteComptable(compteComptable2);
        ligne2.setLibelle("IntegrationTest");
        ligne2.setCredit(new BigDecimal(1500));

        ecritureComptable.getListLigneEcriture().addAll(Arrays.asList(ligne1,ligne2));
        getBusinessProxy().getComptabiliteManager().addReference(ecritureComptable);

        return ecritureComptable;
    }

}
