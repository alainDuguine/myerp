package com.dummy.myerp.consumer.dao.impl.db.dao;

import com.dummy.myerp.consumer.db.AbstractDbConsumer;
import com.dummy.myerp.consumer.db.DataSourcesEnum;
import com.dummy.myerp.model.bean.comptabilite.EcritureComptable;
import com.dummy.myerp.technical.exception.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/dummy/myerp/testconsumer/consumer/testContext.xml")
public class ComptabiliteDaoImplTestInsert extends AbstractDbConsumer {

    @Autowired
    private ComptabiliteDaoImpl dao;

    @Test(expected = BadSqlGrammarException.class)
    public void GivenWrongSequenceName_whenGetSequenceValuePostgreSQL_ThrowsBadSqlGrammarException(){
        Integer id = this.queryGetSequenceValuePostgreSQL(DataSourcesEnum.MYERP, "myerp.comptable_id_seq", Integer.class);
    }

    @Test
    public void GivenEcritureComptable_WhenInsertEcritureComptable_isInserted() throws NotFoundException {
        //given
        int nbResult = dao.getListEcritureComptable().size();
        EcritureComptable ecritureComptable = dao.getEcritureComptable(-1);
        //when
        dao.insertEcritureComptable(ecritureComptable);
        //then
        assertThat(dao.getListEcritureComptable().size()).isEqualTo(nbResult+1);
    }

    @Test
    public void GivenEcritureComptable_WhenUpdateEcritureComptable_isUpdated() throws NotFoundException {
        //given

        Calendar now = Calendar.getInstance();
        String newLibelle = "Changement libelle";
        EcritureComptable oldEcritureComptable = dao.getEcritureComptable(-1);
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
    }
}
