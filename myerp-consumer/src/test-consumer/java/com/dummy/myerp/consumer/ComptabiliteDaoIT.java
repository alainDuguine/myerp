package com.dummy.myerp.consumer;


import com.dummy.myerp.consumer.db.AbstractDbConsumer;
import com.dummy.myerp.consumer.db.DataSourcesEnum;
import com.dummy.myerp.model.bean.comptabilite.*;
import com.dummy.myerp.technical.exception.NotFoundException;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/dummy/myerp/testconsumer/consumer/testContext.xml")
public class ComptabiliteDaoIT extends AbstractDbConsumer {

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
    public void GivenId_whenLoadListLigneEcriture_isNotNull() {
        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.setId(-1);

        dao.loadListLigneEcriture(ecritureComptable);
        assertThat(ecritureComptable.getListLigneEcriture()).isNotEmpty();
    }

    @Test
    public void GivenEmptyId_whenLoadListLigneEcriture_isNull(){
        EcritureComptable ecritureComptable = new EcritureComptable();

        dao.loadListLigneEcriture(ecritureComptable);
        assertThat(ecritureComptable.getListLigneEcriture()).isEmpty();
    }

    @Test(expected = BadSqlGrammarException.class)
    public void GivenWrongSequenceName_whenGetSequenceValuePostgreSQL_ThrowsBadSqlGrammarException(){
        Integer id = this.queryGetSequenceValuePostgreSQL(DataSourcesEnum.MYERP, "myerp.comptable_id_seq", Integer.class);
    }

    @Test
    public void GivenSequenceEcritureComptable_withValueEqualsOne_ThenInsertSequence() throws NotFoundException {
        //given
        String codeJournal = "AC";
        int annee = 2020;
        int derniereValeur = 1;
        SequenceEcritureComptable sequenceEcritureComptable = new SequenceEcritureComptable();
        sequenceEcritureComptable.setJournal(new JournalComptable(codeJournal,"Achat"));
        sequenceEcritureComptable.setAnnee(annee);
        sequenceEcritureComptable.setDerniereValeur(derniereValeur);

        //when
        dao.insertOrUpdateSequenceEcritureComptable(sequenceEcritureComptable);
        SequenceEcritureComptable result = dao.getSequenceFromJournalAndAnnee(codeJournal, annee);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getDerniereValeur()).isEqualTo(1);
        // Suppression de l'enregistrement pour garantir l'intégrité des tests, avec vérification de la suppression
        dao.deleteSequenceEcritureComptable(codeJournal, annee);
        assertThrows(NotFoundException.class, () -> dao.getSequenceFromJournalAndAnnee(codeJournal, annee));
    }

    @Test
    public void GivenSequenceEcritureComptable_withValueNotEqualsOne_ThenUpdateSequence() throws NotFoundException {
        //given
        String codeJournal = "AC";
        int annee = 2020;
        int derniereValeur = 1;
        SequenceEcritureComptable sequenceEcritureComptable = new SequenceEcritureComptable();
        sequenceEcritureComptable.setJournal(new JournalComptable(codeJournal,"Achat"));
        sequenceEcritureComptable.setAnnee(annee);
        sequenceEcritureComptable.setDerniereValeur(derniereValeur);
        dao.insertOrUpdateSequenceEcritureComptable(sequenceEcritureComptable);

        SequenceEcritureComptable sequenceEcritureComptable1 = new SequenceEcritureComptable();
        sequenceEcritureComptable1.setJournal(new JournalComptable(codeJournal,"Achat"));
        sequenceEcritureComptable1.setAnnee(annee);
        sequenceEcritureComptable1.setDerniereValeur(2);

        dao.insertOrUpdateSequenceEcritureComptable(sequenceEcritureComptable1);
        SequenceEcritureComptable result = dao.getSequenceFromJournalAndAnnee(codeJournal, annee);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getDerniereValeur()).isEqualTo(2);

        // Suppression de l'enregistrement pour garantir l'intégrité des tests, avec vérification de la suppression
        dao.deleteSequenceEcritureComptable(codeJournal, annee);
        assertThrows(NotFoundException.class, () -> dao.getSequenceFromJournalAndAnnee(codeJournal, annee));
    }


    @Test
    public void GivenEcritureComptable_WhenInsertNotFullEcritureComptable_ThrowsException(){
        //given
        int nbResult = dao.getListEcritureComptable().size();
        EcritureComptable ecritureComptable = new EcritureComptable();

        JournalComptable journal = new JournalComptable();
        ecritureComptable.setJournal(journal);
        //when

        //then
        assertThrows(DataIntegrityViolationException.class, () -> dao.insertEcritureComptable(ecritureComptable));
        assertThat(dao.getListEcritureComptable().size()).isEqualTo(nbResult);
    }

    @Test
    public void GivenEcritureComptable_WhenInsertFullEcritureComptable_isInserted(){
        //given
        int nbResult = dao.getListEcritureComptable().size();
        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.setLibelle("TestDao");
        ecritureComptable.setDate(new Date());

        JournalComptable journal = new JournalComptable();
        journal.setCode("AC");
        journal.setLibelle("Achat");

        ecritureComptable.setJournal(journal);
        ecritureComptable.setReference("AC-2016/00002");

        //when
        dao.insertEcritureComptable(ecritureComptable);

        //then
        assertThat(dao.getListEcritureComptable().size()).isEqualTo(nbResult+1);
        assertThat(ecritureComptable.getId()).isNotNull();

        //suppression de l'enregistrement pour garantir l'intégrité des tests
        dao.deleteEcritureComptable(ecritureComptable.getId());
    }

    @Test
    public void GivenEcritureComptable_WhenUpdateEcritureComptable_isUpdated() throws NotFoundException {
        //given
        Calendar now = Calendar.getInstance();

        String newLibelle = "Changement libelle";
        EcritureComptable oldEcritureComptable = dao.getEcritureComptable(-1);
        String oldLibelle = oldEcritureComptable.getLibelle();
        oldEcritureComptable.setLibelle(newLibelle);
        oldEcritureComptable.setDate(now.getTime());

        //when
        dao.updateEcritureComptable(oldEcritureComptable);

        //then
        EcritureComptable newEcritureComptable = dao.getEcritureComptable(-1);
        Calendar updatedDate = Calendar.getInstance();
        updatedDate.setTime(newEcritureComptable.getDate());
        assertThat(now.get(Calendar.DATE)).isEqualTo(updatedDate.get(Calendar.DATE));
        assertThat(newEcritureComptable.getLibelle()).isEqualTo(newLibelle);

        oldEcritureComptable.setLibelle(oldLibelle);
        dao.updateEcritureComptable(oldEcritureComptable);
    }

    @Test
    public void GivenIdEcritureComptable_WhenDeleteEcritureComptable_ObjectIsDeletedAndLigneEcritureComptableIsDeleted() throws NotFoundException {
        // given
        EcritureComptable ecritureComptable = dao.getEcritureComptable(-1);
        ecritureComptable.setId(null);
        dao.insertEcritureComptable(ecritureComptable);
        int nbEcriture = dao.getListEcritureComptable().size();

        //when
        dao.deleteEcritureComptable(ecritureComptable.getId());

        //then
        assertThat(dao.getListEcritureComptable().size()).isEqualTo(nbEcriture-1);
    }

}
