package com.dummy.myerp.testbusiness.business;

import com.dummy.myerp.business.contrat.BusinessProxy;
import com.dummy.myerp.business.impl.AbstractBusinessManager;
import com.dummy.myerp.business.impl.TransactionManager;
import com.dummy.myerp.consumer.dao.contrat.DaoProxy;
import com.dummy.myerp.model.bean.comptabilite.*;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;

import org.junit.After;
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
import static org.junit.Assert.assertThrows;

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

    @After
    public void reset(){
        getBusinessProxy().getComptabiliteManager().deleteSequenceEcritureComptable("AC",2020);
    }

    @Test
    public void givenEcritureWithoutRG2_WhenInsertEcritureComptable_NotPersisted(){
        List<EcritureComptable> listEcriture = getBusinessProxy().getComptabiliteManager().getListEcritureComptable();
        EcritureComptable ecritureComptable = this.getEcritureComptable();
        ecritureComptable.getListLigneEcriture().get(1).setCredit(new BigDecimal(1300));

        getBusinessProxy().getComptabiliteManager().addReference(ecritureComptable);

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
        EcritureComptable ecritureComptable = this.getEcritureComptable();
        ecritureComptable.getListLigneEcriture().get(1).setDebit(new BigDecimal(-1500));
        ecritureComptable.getListLigneEcriture().get(1).setCredit(null);
        getBusinessProxy().getComptabiliteManager().addReference(ecritureComptable);

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
    public void checkRG4_givenEcritureWithNegativeNumberAndNewSequence_WhenInsertEcriture_Persisted() throws NotFoundException {
        List<EcritureComptable> listEcriture = getBusinessProxy().getComptabiliteManager().getListEcritureComptable();
        EcritureComptable ecritureComptable = this.getEcritureComptable();
        ecritureComptable.getListLigneEcriture().get(0).setDebit(new BigDecimal(-1500));
        ecritureComptable.getListLigneEcriture().get(1).setCredit(new BigDecimal(-1500));
        getBusinessProxy().getComptabiliteManager().addReference(ecritureComptable);

        //when
        String message = null;
        try {
            getBusinessProxy().getComptabiliteManager().insertEcritureComptable(ecritureComptable);
        } catch (FunctionalException e) {
            e.getMessage();
        }finally {
            //then
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ecritureComptable.getDate());
            int yearInRef = calendar.get(Calendar.YEAR);
            assertThat(getBusinessProxy().getComptabiliteManager().getSequenceFromJournalAndAnnee(ecritureComptable.getJournal().getCode(), yearInRef));
            assertThat(getBusinessProxy().getComptabiliteManager().getListEcritureComptable().size()).isEqualTo(listEcriture.size()+1);

            getBusinessProxy().getComptabiliteManager().deleteEcritureComptable(ecritureComptable.getId());
        }
    }

    @Test
    public void checkRGG_givenEcritureWithExistingSequence_WhenInsertEcriture_PersistedWithGoodSequenceNumber() throws NotFoundException {
        List<EcritureComptable> listEcriture = getBusinessProxy().getComptabiliteManager().getListEcritureComptable();
        EcritureComptable ecriture = listEcriture.get(0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ecriture.getDate());
        SequenceEcritureComptable sequenceRecorded = getBusinessProxy().getComptabiliteManager().getSequenceFromJournalAndAnnee(
                ecriture.getJournal().getCode(),
                calendar.get(Calendar.YEAR));

        EcritureComptable ecritureComptable = this.getEcritureComptable();
        ecritureComptable.setDate(ecriture.getDate());
        ecritureComptable.setJournal(ecriture.getJournal());
        getBusinessProxy().getComptabiliteManager().addReference(ecritureComptable);

        //when
        String message = null;
        try {
            getBusinessProxy().getComptabiliteManager().insertEcritureComptable(ecritureComptable);
        } catch (FunctionalException e) {
            message = e.getMessage();
        }finally {
            //then

            assertThat(getBusinessProxy().getComptabiliteManager().getSequenceFromJournalAndAnnee(ecritureComptable.getJournal().getCode(), calendar.get(Calendar.YEAR))).isNotNull();
            assertThat(getBusinessProxy().getComptabiliteManager().getListEcritureComptable().size()).isEqualTo(listEcriture.size()+1);

            getBusinessProxy().getComptabiliteManager().deleteEcritureComptable(ecritureComptable.getId());
        }
    }

    @Test
    public void givenEmptyEcritureComptable_WhenUpdate_ThrowFunctionalException() throws FunctionalException {
        //given
        List<EcritureComptable> listEcriture = getBusinessProxy().getComptabiliteManager().getListEcritureComptable();
        EcritureComptable ecriture = listEcriture.get(0);
        ecriture.setReference("");

        //then
        assertThrows(FunctionalException.class, () -> getBusinessProxy().getComptabiliteManager().updateEcritureComptable(ecriture));
    }

    @Test
    public void givenUpdatedEcritureComptable_WhenUpdate_DoUpdate() throws FunctionalException, NotFoundException {
        //given
        List<EcritureComptable> listEcriture = getBusinessProxy().getComptabiliteManager().getListEcritureComptable();
        EcritureComptable ecriture = listEcriture.get(0);
        String oldLibelle = ecriture.getLibelle();
        String newLibelle = "Integration Test Update";
        String ref = ecriture.getReference();
        ecriture.setLibelle(newLibelle);

        //then
        getBusinessProxy().getComptabiliteManager().updateEcritureComptable(ecriture);
        assertThat(dao.getComptabiliteDao().getEcritureComptableByRef(ref).getLibelle()).isEqualTo(newLibelle);

        // reinitialisation du champ
        ecriture.setLibelle(oldLibelle);
        getBusinessProxy().getComptabiliteManager().updateEcritureComptable(ecriture);
    }

    @Test
    public void checkRG6_givenExistingReferenceToEcritureComptable_WhenUpdate_ThrowsFunctionalException() throws FunctionalException, NotFoundException {
        //given
        List<EcritureComptable> listEcriture = getBusinessProxy().getComptabiliteManager().getListEcritureComptable();
        EcritureComptable ecriture = listEcriture.get(0);
        ecriture.getJournal().setCode(listEcriture.get(1).getJournal().getCode());
        ecriture.setReference(listEcriture.get(1).getReference());

        //then
        Exception exception = assertThrows(FunctionalException.class, () -> getBusinessProxy().getComptabiliteManager().updateEcritureComptable(ecriture));
        assertThat("Une autre écriture comptable existe déjà avec la même référence.").isEqualTo(exception.getMessage());
        assertThat(getBusinessProxy().getComptabiliteManager().getListEcritureComptable().size()).isEqualTo(listEcriture.size());
    }

    private EcritureComptable getEcritureComptable(){
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

        return ecritureComptable;
    }

}
