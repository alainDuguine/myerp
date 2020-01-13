package com.dummy.myerp.model.bean.comptabilite;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class CompteComptableTest {

    private CompteComptable compteComptable;
    private Integer numero = 1;
    private String libelle = "Compte test";

    @Before
    public void init(){
        this.compteComptable = new CompteComptable(numero,libelle);
    }

    @Test
    public void Constructor_withDefaultValues(){
        Assert.assertEquals(compteComptable.getLibelle(),libelle);
        Assert.assertEquals(compteComptable.getNumero(),numero);
    }

    @Test
    public void toString_NewCompteComptable(){
        String expectedString = "CompteComptable{numero="+numero+", libelle='"+libelle+"'}";
        String resultString = compteComptable.toString();
        Assert.assertEquals(expectedString, resultString);
    }

    @Test
    public void getByNumero_ReturnsCompteComptable_IfCompteExists(){
        List<CompteComptable> compteComptableList = getListCompteComptable();
        CompteComptable compteComptableFromSut = CompteComptable.getByNumero(compteComptableList, 5);
        Assert.assertEquals(compteComptableFromSut, compteComptableList.get(5));
    }

    @Test
    public void getByNumero_ReturnsNull_IfCompteDoesNotExists(){
        List<CompteComptable> compteComptableList = getListCompteComptable();
        CompteComptable compteComptableFromSut = CompteComptable.getByNumero(compteComptableList, 11);
        Assert.assertEquals(compteComptableFromSut, null);
    }

    public List<CompteComptable> getListCompteComptable(){
        List<CompteComptable> compteComptableList = new ArrayList<>();
        CompteComptable compteComptable;
        for (int i = 0; i < 10; i++) {
            compteComptable = new CompteComptable();
            compteComptable.setNumero(i);
            compteComptable.setLibelle("compte numéro " + (i));
            compteComptableList.add(compteComptable);
        }
        return compteComptableList;
    }

}
