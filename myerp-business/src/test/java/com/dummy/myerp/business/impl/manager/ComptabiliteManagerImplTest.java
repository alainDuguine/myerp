package com.dummy.myerp.business.impl.manager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import com.dummy.myerp.business.impl.AbstractBusinessManager;
import com.dummy.myerp.consumer.dao.contrat.ComptabiliteDao;
import com.dummy.myerp.consumer.dao.contrat.DaoProxy;
import com.dummy.myerp.model.bean.comptabilite.*;
import com.dummy.myerp.technical.exception.NotFoundException;

import com.dummy.myerp.technical.exception.FunctionalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComptabiliteManagerImplTest extends AbstractBusinessManager{

    @InjectMocks
    private ComptabiliteManagerImpl manager;

    @Mock
    DaoProxy daoProxy;
    @Mock
    private ComptabiliteDao comptabiliteDao;


    private EcritureComptable vEcritureComptable;

    @BeforeEach
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

    private void initializeMockDao(){
        // initialisation du mock pour récupérer la dernier valeur de SequenceEcritureComptable
        AbstractBusinessManager.configure(null, this.daoProxy, null);
        given(getDaoProxy().getComptabiliteDao()).willReturn(this.comptabiliteDao);
    }

    @Test
    public void checkRG5_GivenEcritureComptable_WhithoutReference_WhenCheckEcritureComptableUnit_ThrowsNullFunctionalException() throws Exception {
        Exception exception = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableUnit(vEcritureComptable));

        assertThat("La référence de l'écriture ne peut pas être nulle.").isEqualTo(exception.getMessage());
    }

    @Test
    public void checkRG5_GivenEcritureComptable_WhithWrongCodeJournalInReference_WhenCheckEcritureComptableUnit_ThrowsWrongCodeJournalFunctionalException() throws Exception {
        String wrongCodeJournal = "AA";
        vEcritureComptable.setReference(wrongCodeJournal+"-2020/00001");

        Exception exception = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableUnit(vEcritureComptable));

        assertThat("La référence de l'écriture " + wrongCodeJournal + " ne correspond pas au code journal " + vEcritureComptable.getJournal().getCode()).isEqualTo(exception.getMessage());
    }

    @Test
    public void checkRG5_GivenEcritureComptable_WhithWrongDateInReference_WhenCheckEcritureComptableUnit_ThrowsWrongDateFunctionalException() throws Exception {
        String wrongYear = "2015";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(vEcritureComptable.getDate());
        vEcritureComptable.setReference(vEcritureComptable.getJournal().getCode()+"-"+wrongYear+"/00001");

        Exception exception = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableUnit(vEcritureComptable));

        assertThat("La référence de l'écriture "+wrongYear+" ne correspond pas à l'année de l'écriture " + calendar.get(Calendar.YEAR)).isEqualTo(exception.getMessage());
    }

    @Test
    public void checkRG5_GivenEcritureComptable_WhithWrongSequenceInReference_WhenCheckEcritureComptableUnit_ThrowsWrongSequenceFunctionalException() throws Exception {
        this.initializeMockDao();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(vEcritureComptable.getDate());
        SequenceEcritureComptable sequenceMock = new SequenceEcritureComptable(calendar.get(Calendar.YEAR),12);
        given(comptabiliteDao.getSequenceFromJournalAndAnnee(any(String.class), any(Integer.class))).willReturn(sequenceMock);

        String wrongSequence = "00005";
        vEcritureComptable.setReference(vEcritureComptable.getJournal().getCode()+"-"+sequenceMock.getAnnee()+"/"+wrongSequence);

        Exception exception = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableUnit(vEcritureComptable));

        assertThat("Le numéro de séquence de l'écriture " + wrongSequence + " ne correspond pas à la dernière séquence du journal "
                + String.format("%05d",sequenceMock.getDerniereValeur()+1))
                .isEqualTo(exception.getMessage());
    }

    @Test
    public void checkRG5_GivenEcritureComptable_WhithGoodReference_WhenAddReference_Ok() throws Exception {
        this.initializeMockDao();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(vEcritureComptable.getDate());
        SequenceEcritureComptable sequenceMock = new SequenceEcritureComptable(calendar.get(Calendar.YEAR),12);
        given(comptabiliteDao.getSequenceFromJournalAndAnnee(any(String.class), any(Integer.class))).willReturn(sequenceMock);

        manager.addReference(vEcritureComptable);
        manager.checkEcritureComptableUnit(vEcritureComptable);
        assertThat(vEcritureComptable.getReference().split("/")[1]).isEqualTo("00013");
    }

    @Test
    public void checkRG5_GivenEcritureComptable_WhithEmptySequence_WhenAddReference_Ok() throws Exception {
        this.initializeMockDao();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(vEcritureComptable.getDate());
        given(comptabiliteDao.getSequenceFromJournalAndAnnee(any(String.class), any(Integer.class))).willReturn(null);

        manager.addReference(vEcritureComptable);
        manager.checkEcritureComptableUnit(vEcritureComptable);
        assertThat(vEcritureComptable.getReference().split("/")[1]).isEqualTo("00001");
    }

    @Test
    public void checkRG6_GivenEcritureComptable_WithReferenceAlreadyExisting_WhenCheckEcritureComptableReference_ThrowsFunctionalException() throws FunctionalException, NotFoundException {
        this.initializeMockDao();

        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.setId(1);
        ecritureComptable.setReference("AA-2015/00001");
        given(comptabiliteDao.getEcritureComptableByRef(anyString())).willReturn(ecritureComptable);

        vEcritureComptable.setReference("AA-2015/00001");

        Exception exception = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableContext(vEcritureComptable));

        assertThat("Une autre écriture comptable existe déjà avec la même référence.").isEqualTo(exception.getMessage());
    }

    @Test
    public void checkRG6_GivenEcritureComptable_WithNoReferenceExisting_WhenCheckEcritureComptableReference_Ok() throws FunctionalException, NotFoundException {
        this.initializeMockDao();

        when(comptabiliteDao.getEcritureComptableByRef(anyString())).thenThrow(NotFoundException.class);

        vEcritureComptable.setReference("AA-2015/00001");

        manager.checkEcritureComptableContext(vEcritureComptable);
    }

    @Test
    public void GivenEcritureComptable_WithContextMistake__WhenCheckEcritureComptable__ThrowsFunctionalException() throws FunctionalException, NotFoundException {
        this.initializeMockDao();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(vEcritureComptable.getDate());
        SequenceEcritureComptable sequenceMock = new SequenceEcritureComptable(calendar.get(Calendar.YEAR),12);
        given(comptabiliteDao.getSequenceFromJournalAndAnnee(any(String.class), any(Integer.class))).willReturn(sequenceMock);

        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.setId(1);
        ecritureComptable.setReference("AA-2015/00001");
        given(comptabiliteDao.getEcritureComptableByRef(anyString())).willReturn(ecritureComptable);

        manager.addReference(vEcritureComptable);
        assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(vEcritureComptable));
    }

    @Test
    public void GivenEcritureComptable_WithoutMistake__WhenCheckEcritureComptable__Ok() throws FunctionalException, NotFoundException {
        this.initializeMockDao();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(vEcritureComptable.getDate());
        SequenceEcritureComptable sequenceMock = new SequenceEcritureComptable(calendar.get(Calendar.YEAR),12);
        given(comptabiliteDao.getSequenceFromJournalAndAnnee(any(String.class), any(Integer.class))).willReturn(sequenceMock);

        when(comptabiliteDao.getEcritureComptableByRef(anyString())).thenThrow(NotFoundException.class);

        manager.addReference(vEcritureComptable);
        manager.checkEcritureComptable(vEcritureComptable);
    }

    // =================== TESTS ALREADY IMPLEMENTED ====================================

    @Test
    public void checkEcritureComptableUnitViolation() throws Exception {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableUnit(vEcritureComptable));
    }

    @Test
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

        assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableUnit(vEcritureComptable));
    }

    @Test
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
                                                                                 null, new BigDecimal(-123),
                                                                                 null));

        assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableUnit(vEcritureComptable));
    }

}
