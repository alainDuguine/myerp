package com.dummy.myerp.model.bean.comptabilite;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;


public class EcritureComptableTest {

    private LigneEcritureComptable createLigne(Integer pCompteComptableNumero, String pDebit, String pCredit) {
        BigDecimal vDebit = pDebit == null ? null : new BigDecimal(pDebit);
        BigDecimal vCredit = pCredit == null ? null : new BigDecimal(pCredit);
        String vLibelle = ObjectUtils.defaultIfNull(vDebit, BigDecimal.ZERO)
                                     .subtract(ObjectUtils.defaultIfNull(vCredit, BigDecimal.ZERO)).toPlainString();
        LigneEcritureComptable vRetour = new LigneEcritureComptable(new CompteComptable(pCompteComptableNumero),
                                                                    vLibelle,
                                                                    vDebit, vCredit);
        return vRetour;
    }

    @Test
    public void Given_ListLignes_When_GetTotalDebit_IsEqualToDebitSum(){
        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"100.00",null));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,null,"100"));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"90.50",null));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"1250.50",null));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,null,"900"));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"0.75",null));

        assertThat(ecritureComptable.getTotalDebit()).isEqualTo(BigDecimal.valueOf(100+90.50+1250.50+0.75));

    }

    @Test
    public void Given_ListLignes_When_GetTotalCredit_IsEqualToCreditSum(){
        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"100.00",null));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,null,"1500.60"));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"90.50",null));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,null,"0.25"));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,null,"452"));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"0.75",null));

        assertThat(ecritureComptable.getTotalCredit()).isEqualTo(BigDecimal.valueOf(1500.60+0.25+452));

    }

    @Test
    public void isEquilibree() {
        EcritureComptable ecritureComptable = new EcritureComptable();

        ecritureComptable.setLibelle("Equilibrée");
        ecritureComptable.getListLigneEcriture().add(this.createLigne(1, "200.50", null));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(1, "100.50", "33"));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(2, null, "301"));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(2, "40", "7"));

        assertThat(ecritureComptable.isEquilibree()).isTrue();

        ecritureComptable.getListLigneEcriture().clear();
        ecritureComptable.setLibelle("Non équilibrée");
        ecritureComptable.getListLigneEcriture().add(this.createLigne(1, "10", null));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(1, "20", "1"));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(2, null, "30"));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(2, "1", "2"));
        assertThat(ecritureComptable.isEquilibree()).isFalse();

        ecritureComptable.getListLigneEcriture().clear();
        ecritureComptable.setLibelle("Nulle");
        ecritureComptable.getListLigneEcriture().add(this.createLigne(1, null, null));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(1, null, null));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(2, null, null));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(2, null, null));

        assertThat(ecritureComptable.isEquilibree()).isTrue();
    }

    @Test
    public void toString_NewEcritureComptable(){
        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.setLibelle("EcritureComptable Test");
        ecritureComptable.setJournal(new JournalComptable("BQ", "Journal Test"));
        ecritureComptable.setId(1);
        ecritureComptable.setDate(new Date());
        ecritureComptable.setReference("AA-1234-12345");
        String sep = ", ";
        String expectedString = "EcritureComptable{id="+ecritureComptable.getId()
                +sep+"journal="+ecritureComptable.getJournal()
                +sep+"reference='"+ecritureComptable.getReference()+"'"
                +sep+"date="+ecritureComptable.getDate()
                +sep+"libelle='"+ecritureComptable.getLibelle()+"'"
                +sep+"totalDebit="+ecritureComptable.getTotalDebit().toPlainString()
                +sep+"totalCredit="+ecritureComptable.getTotalCredit().toPlainString()
                +sep+"listLigneEcriture=[\n"+
                StringUtils.join(ecritureComptable.getListLigneEcriture(), "\n")+
                "\n]}";
        String resultString = ecritureComptable.toString();
        Assert.assertEquals(expectedString, resultString);
    }

}
