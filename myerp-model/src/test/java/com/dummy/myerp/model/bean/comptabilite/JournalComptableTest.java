package com.dummy.myerp.model.bean.comptabilite;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class JournalComptableTest {

    private JournalComptable journalComptable;
    private String code = "AAAA";
    private String libelle = "Test journal comptable";

    @Before
    public void init(){
        this.journalComptable = new JournalComptable(code,libelle);
    }

    @Test
    public void Constructor_withDefaultValues(){
        Assert.assertEquals(journalComptable.getLibelle(),libelle);
        Assert.assertEquals(journalComptable.getCode(),code);
    }

    @Test
    public void toString_NewJournalComptable(){
        String expectedString = "JournalComptable{code='"+code+"', libelle='"+libelle+"'}";
        String resultString = this.journalComptable.toString();
        Assert.assertEquals(expectedString, resultString);
    }

    @Test
    public void getByCode_ReturnsJournalComptable_IfCompteExists(){
        List<JournalComptable> journalComptableList = this.getJournalComptableList();
        JournalComptable journalComptableFromSut = JournalComptable.getByCode(journalComptableList, "555");
        Assert.assertEquals(journalComptableFromSut, journalComptableList.get(5));
    }

    @Test
    public void getByNumero_ReturnsNull_IfCompteDoesNotExists(){
        // List initialization
        List<JournalComptable> journalComptableList = getJournalComptableList();
        JournalComptable journalComptableFromSut = JournalComptable.getByCode(journalComptableList, "12345");
        Assert.assertNull(journalComptableFromSut);
    }

    public List<JournalComptable> getJournalComptableList(){
        // List initialization
        List<JournalComptable> journalComptableList = new ArrayList<>();
        JournalComptable journalComptable;
        for (int i = 0; i < 10; i++) {
            journalComptable = new JournalComptable();
            journalComptable.setCode(""+i+i+i);
            journalComptable.setLibelle("journal numéro " + journalComptable.getCode());
            journalComptableList.add(journalComptable);
        }
        return journalComptableList;
    }

}
