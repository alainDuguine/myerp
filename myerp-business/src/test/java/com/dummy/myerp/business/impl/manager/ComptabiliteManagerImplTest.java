package com.dummy.myerp.business.impl.manager;

import java.math.BigDecimal;
import java.util.Date;

import javafx.scene.control.cell.TextFieldListCell;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import com.dummy.myerp.model.bean.comptabilite.CompteComptable;
import com.dummy.myerp.model.bean.comptabilite.EcritureComptable;
import com.dummy.myerp.model.bean.comptabilite.JournalComptable;
import com.dummy.myerp.model.bean.comptabilite.LigneEcritureComptable;
import com.dummy.myerp.technical.exception.FunctionalException;
import org.junit.rules.ExpectedException;


public class ComptabiliteManagerImplTest {

    private ComptabiliteManagerImpl manager = new ComptabiliteManagerImpl();

    private EcritureComptable vEcritureComptable;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @BeforeClass
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
    public void GivenEcritureComptableWhithWrongDateInReference_WhenCheckEcritureComptableUnit_ThrowsWrongDateFunctionalException() throws Exception {
        String wrongDate = "2015";
        String dateRef = String.valueOf(vEcritureComptable.getDate().getYear()+1900);
        vEcritureComptable.setReference(vEcritureComptable.getJournal().getCode()+"-"+wrongDate+"/00001");
        exceptionRule.expect(FunctionalException.class);
        exceptionRule.expectMessage( "La référence de l'écriture "+wrongDate+" ne correspond pas à l'année de l'écriture " + dateRef);
        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test
    public void GivenEcritureComptableWhithWrongCodeJournal_WhenCheckEcritureComptableUnit_ThrowsWrongCodeJournalFunctionalException() throws Exception {
        String wrongCodeJournal = "AA";
        vEcritureComptable.setReference(wrongCodeJournal+"-2020/00001");
        exceptionRule.expect(FunctionalException.class);
        exceptionRule.expectMessage( "La référence de l'écriture " + wrongCodeJournal + " ne correspond pas au code journal " + vEcritureComptable.getJournal().getCode());
        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test
    public void GivenEcritureComptableWhithWrongSequence_WhenCheckEcritureComptableUnit_ThrowsWrongSequenceFunctionalException() throws Exception {
        String wrongCodeJournal = "AA";
        vEcritureComptable.setReference(wrongCodeJournal+"-2020/00001");
        exceptionRule.expect(FunctionalException.class);
        exceptionRule.expectMessage( "La référence de l'écriture " + wrongCodeJournal + " ne correspond pas au code journal " + vEcritureComptable.getJournal().getCode());
        manager.checkEcritureComptableUnit(vEcritureComptable);
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
