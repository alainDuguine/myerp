package com.dummy.myerp.business.impl.manager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import com.dummy.myerp.business.impl.AbstractBusinessManager;
import com.dummy.myerp.consumer.dao.contrat.ComptabiliteDao;
import com.dummy.myerp.consumer.dao.contrat.DaoProxy;
import com.dummy.myerp.model.bean.comptabilite.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.dummy.myerp.technical.exception.FunctionalException;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComptabiliteManagerImplTest extends AbstractBusinessManager{

    private ComptabiliteManagerImpl manager = new ComptabiliteManagerImpl();

    @Mock
    DaoProxy daoProxy;
    @Mock
    private ComptabiliteDao comptabiliteDao;

    private EcritureComptable vEcritureComptable;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void initEcritureComptable(){
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                null, null,
                new BigDecimal(123)));
    }

    @Test
    public void GivenEcritureComptableWhithoutReference_WhenCheckEcritureComptableUnit_ThrowsNullFunctionalException() throws Exception {
        exceptionRule.expect(FunctionalException.class);
        exceptionRule.expectMessage( "La référence de l'écriture ne peut pas être nulle.");

        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test
    public void GivenEcritureComptableWhithWrongCodeJournalInReference_WhenCheckEcritureComptableUnit_ThrowsWrongCodeJournalFunctionalException() throws Exception {
        String wrongCodeJournal = "AA";
        vEcritureComptable.setReference(wrongCodeJournal+"-2020/00001");

        exceptionRule.expect(FunctionalException.class);
        exceptionRule.expectMessage( "La référence de l'écriture " + wrongCodeJournal + " ne correspond pas au code journal " + vEcritureComptable.getJournal().getCode());

        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test
    public void GivenEcritureComptableWhithWrongDateInReference_WhenCheckEcritureComptableUnit_ThrowsWrongDateFunctionalException() throws Exception {
        String wrongYear = "2015";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(vEcritureComptable.getDate());
        vEcritureComptable.setReference(vEcritureComptable.getJournal().getCode()+"-"+wrongYear+"/00001");

        exceptionRule.expect(FunctionalException.class);
        exceptionRule.expectMessage( "La référence de l'écriture "+wrongYear+" ne correspond pas à l'année de l'écriture " + calendar.get(Calendar.YEAR));

        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test
    public void GivenEcritureComptableWhithWrongSequenceInReference_WhenCheckEcritureComptableUnit_ThrowsWrongSequenceFunctionalException() throws Exception {
        // initialisation du mock pour récupérer la dernier valeur de SequenceEcritureComptable
        AbstractBusinessManager.configure(null, this.daoProxy, null);
        given(getDaoProxy().getComptabiliteDao()).willReturn(this.comptabiliteDao);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(vEcritureComptable.getDate());
        SequenceEcritureComptable sequenceMock = new SequenceEcritureComptable(calendar.get(Calendar.YEAR),12);
        given(comptabiliteDao.getSequenceFromJournalAndAnnee(any(String.class), any(Integer.class))).willReturn(sequenceMock);
        String wrongSequence = "00005";

        exceptionRule.expect(FunctionalException.class);
        exceptionRule.expectMessage( "Le numéro de séquence de l'écriture " + wrongSequence + " ne correspond pas à la dernière séquence du journal "
                + String.format("%05d",sequenceMock.getDerniereValeur()+1));
        vEcritureComptable.setReference(vEcritureComptable.getJournal().getCode()+"-"+sequenceMock.getAnnee()+"/"+wrongSequence);

        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test
    public void GivenEcritureComptableWhithGoodReference_WhenAddReference_Ok() throws Exception {
        // TODO implement test
    }

    @Test(expected = FunctionalException.class)
    public void checkEcritureComptableUnitViolation() throws Exception {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test(expected = FunctionalException.class)
    public void checkEcritureComptableUnitRG2() throws Exception {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                                                                                 null, new BigDecimal(123),
                                                                                 null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                                                                                 null, null,
                                                                                 new BigDecimal(1234)));
        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test(expected = FunctionalException.class)
    public void checkEcritureComptableUnitRG3() throws Exception {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                                                                                 null, new BigDecimal(123),
                                                                                 null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                                                                                 null, new BigDecimal(123),
                                                                                 null));
        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

}
