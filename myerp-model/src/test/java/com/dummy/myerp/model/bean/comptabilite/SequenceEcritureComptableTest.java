package com.dummy.myerp.model.bean.comptabilite;

import org.junit.Assert;
import org.junit.Test;


public class SequenceEcritureComptableTest {

    private Integer annee = 2020;
    private Integer derniereValeur = 18;

    @Test
    public void Constructor_withDefaultValues(){
        SequenceEcritureComptable sequenceEcritureComptable = new SequenceEcritureComptable(annee,derniereValeur);
        Assert.assertEquals(sequenceEcritureComptable.getAnnee(),annee);
        Assert.assertEquals(sequenceEcritureComptable.getDerniereValeur(),derniereValeur);
    }

    @Test
    public void toString_NewCompteComptable(){
        SequenceEcritureComptable sequenceEcritureComptable = new SequenceEcritureComptable();
        sequenceEcritureComptable.setAnnee(annee);
        sequenceEcritureComptable.setDerniereValeur(derniereValeur);
        String expectedString = "SequenceEcritureComptable{annee="+annee+", derniereValeur="+derniereValeur+"}";
        String resultString = sequenceEcritureComptable.toString();
        Assert.assertEquals(expectedString, resultString);
    }
}
