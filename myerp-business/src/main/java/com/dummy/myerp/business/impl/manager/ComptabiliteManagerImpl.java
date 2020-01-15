package com.dummy.myerp.business.impl.manager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.dummy.myerp.model.bean.comptabilite.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.TransactionStatus;
import com.dummy.myerp.business.contrat.manager.ComptabiliteManager;
import com.dummy.myerp.business.impl.AbstractBusinessManager;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;


/**
 * Comptabilite manager implementation.
 */
public class ComptabiliteManagerImpl extends AbstractBusinessManager implements ComptabiliteManager {

    // ==================== Attributs ====================


    // ==================== Constructeurs ====================
    /**
     * Instantiates a new Comptabilite manager.
     */
    public ComptabiliteManagerImpl() {
    }


    // ==================== Getters/Setters ====================
    @Override
    public List<CompteComptable> getListCompteComptable() {
        return getDaoProxy().getComptabiliteDao().getListCompteComptable();
    }


    @Override
    public List<JournalComptable> getListJournalComptable() {
        return getDaoProxy().getComptabiliteDao().getListJournalComptable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EcritureComptable> getListEcritureComptable() {
        return getDaoProxy().getComptabiliteDao().getListEcritureComptable();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public SequenceEcritureComptable getSequenceFromJournalAndAnnee(String code, Integer year) {
        return getDaoProxy().getComptabiliteDao().getSequenceFromJournalAndAnnee(code, year);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addReference(EcritureComptable pEcritureComptable) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(pEcritureComptable.getDate());
        Integer year = calendar.get(Calendar.YEAR);
        String codeJournal = pEcritureComptable.getJournal().getCode();
        // récupération de la dernière valeur depuis BDD
        SequenceEcritureComptable sequence = this.getSequenceFromJournalAndAnnee(codeJournal, year);
                // Si l'enregistrement n'existe pas on l'initialise à un
        Integer newSequence = sequence == null ? 1: sequence.getDerniereValeur()+1;
        String reference = codeJournal+"-"+year+"/"+String.format("%05d", newSequence);
        pEcritureComptable.setReference(reference);
        if(newSequence==1){
            this.insertSequenceEcritureComptable(codeJournal, new SequenceEcritureComptable(year, newSequence));
        }else{
            this.updateSequenceEcritureComptable(codeJournal, new SequenceEcritureComptable(year, newSequence));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        this.checkEcritureComptableUnit(pEcritureComptable);
        this.checkEcritureComptableContext(pEcritureComptable);
    }


    /**
     * Vérifie que l'Ecriture comptable respecte les règles de gestion unitaires,
     * c'est à dire indépendemment du contexte (unicité de la référence, exercie comptable non cloturé...)
     *
     * @param pEcritureComptable -
     * @throws FunctionalException Si l'Ecriture comptable ne respecte pas les règles de gestion
     */
    protected void checkEcritureComptableUnit(EcritureComptable pEcritureComptable) throws FunctionalException {
        // ===== Vérification des contraintes unitaires sur les attributs de l'écriture
        Set<ConstraintViolation<EcritureComptable>> vViolations = getConstraintValidator().validate(pEcritureComptable);
        if (!vViolations.isEmpty()) {
            throw new FunctionalException("L'écriture comptable ne respecte pas les règles de gestion.",
                                          new ConstraintViolationException(
                                              "L'écriture comptable ne respecte pas les contraintes de validation",
                                              vViolations));
        }

        // ===== RG_Compta_2 : Pour qu'une écriture comptable soit valide, elle doit être équilibrée
        if (!pEcritureComptable.isEquilibree()) {
            throw new FunctionalException("L'écriture comptable n'est pas équilibrée.");
        }

        // ===== RG_Compta_3 : une écriture comptable doit avoir au moins 2 lignes d'écriture (1 au débit, 1 au crédit)
        int vNbrCredit = 0;
        int vNbrDebit = 0;
        for (LigneEcritureComptable vLigneEcritureComptable : pEcritureComptable.getListLigneEcriture()) {
            if (BigDecimal.ZERO.compareTo(ObjectUtils.defaultIfNull(vLigneEcritureComptable.getCredit(),
                                                                    BigDecimal.ZERO)) != 0) {
                vNbrCredit++;
            }
            if (BigDecimal.ZERO.compareTo(ObjectUtils.defaultIfNull(vLigneEcritureComptable.getDebit(),
                                                                    BigDecimal.ZERO)) != 0) {
                vNbrDebit++;
            }
        }
        // On test le nombre de lignes car si l'écriture à une seule ligne
        //      avec un montant au débit et un montant au crédit ce n'est pas valable
        if (pEcritureComptable.getListLigneEcriture().size() < 2
            || vNbrCredit < 1
            || vNbrDebit < 1) {
            throw new FunctionalException(
                "L'écriture comptable doit avoir au moins deux lignes : une ligne au débit et une ligne au crédit.");
        }
        try {
            this.checkEcritureComptableReference(pEcritureComptable);
        }catch (FunctionalException ex){
            throw new FunctionalException(ex.getMessage());
        }

    }

    /**
     * <strong>RG_COMPTA_5</strong>
     * Vérifie si La référence d'une ecriture comptable est composée du code du {@link JournalComptable}
     * suivi de l'année de l'{@link EcritureComptable} sur 4 chiffres
     * puis d'un numéro de séquence (sur 5 chiffres) incrémenté automatiquement à chaque écriture (dernière valeur a récupérer dans la table SequenceEcritureComptable)
     * Le formatage de la référence est : XX-AAAA/##### -> BQ-2016/00001, il est vérifiée par le validator dans le model {@link SequenceEcritureComptable} via une expression régulière
     * @param pEcritureComptable {@link EcritureComptable} dont on veut tester la reference
     * @throws FunctionalException si la référence enfreint une de ces règles
     */
    private void checkEcritureComptableReference(EcritureComptable pEcritureComptable) throws FunctionalException{
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(pEcritureComptable.getDate());
        String yearInRef = String.valueOf(calendar.get(Calendar.YEAR));
        if (pEcritureComptable.getReference() != null) {
            String[] referenceSplit = pEcritureComptable.getReference().split("-|/");
            // Vérification du code journal
            if(!referenceSplit[0].equals(pEcritureComptable.getJournal().getCode())){
                throw new FunctionalException(
                        "La référence de l'écriture " + referenceSplit[0] + " ne correspond pas au code journal " + pEcritureComptable.getJournal().getCode()
                );
                // vérification année
            }else if (!referenceSplit[1].equals(yearInRef)){
                throw new FunctionalException(
                        "La référence de l'écriture " + referenceSplit[1] + " ne correspond pas à l'année de l'écriture " + yearInRef
                );
            }
            // si le journal et la date sont bons, on récupère la lastSequence pour ce journal et cette annee depuis la base de données
            SequenceEcritureComptable sequence = this.getSequenceFromJournalAndAnnee(pEcritureComptable.getJournal().getCode(), calendar.get(Calendar.YEAR));
            // Si la sequence est nulle la nouvelle sequence sera 1, sinon lastSequence + 1
            Integer newSequence = sequence == null ? 1: sequence.getDerniereValeur()+1;
            // On formate la séquence sur 5 chiffres
            String newSequenceWithLeadingZeros = String.format("%05d", newSequence);
            if (!referenceSplit[2].equals(newSequenceWithLeadingZeros)){
                throw new FunctionalException(
                        "Le numéro de séquence de l'écriture " + referenceSplit[2] + " ne correspond pas à la dernière séquence du journal " + newSequenceWithLeadingZeros);
            }
        }else{
            throw new FunctionalException(
                    "La référence de l'écriture ne peut pas être nulle.");
        }
    }

    /**
     * Vérifie que l'Ecriture comptable respecte les règles de gestion liées au contexte
     * (unicité de la référence, année comptable non cloturé...)
     *
     * @param pEcritureComptable -
     * @throws FunctionalException Si l'Ecriture comptable ne respecte pas les règles de gestion
     */
    protected void checkEcritureComptableContext(EcritureComptable pEcritureComptable) throws FunctionalException {
        // ===== RG_Compta_6 : La référence d'une écriture comptable doit être unique
        if (StringUtils.isNoneEmpty(pEcritureComptable.getReference())) {
            try {
                // Recherche d'une écriture ayant la même référence
                EcritureComptable vECRef = getDaoProxy().getComptabiliteDao().getEcritureComptableByRef(
                    pEcritureComptable.getReference());

                int idpEcritureComptable = pEcritureComptable.getId() == null ? 0 : pEcritureComptable.getId();

                // si il y a un enregistrement et qu'il est d'un id différent de celui qu'on vérifie
                if (vECRef != null && !vECRef.getId().equals(idpEcritureComptable) ){
                    throw new FunctionalException("Une autre écriture comptable existe déjà avec la même référence.");
                }
            } catch (NotFoundException vEx) {
               Logger.getAnonymousLogger().log(Level.INFO, vEx.getMessage());
                // Dans ce cas, c'est bon, ça veut dire qu'on n'a aucune autre écriture avec la même référence.
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        this.checkEcritureComptable(pEcritureComptable);
        TransactionStatus vTS = getTransactionManager().beginTransactionMyERP();
        try {
            getDaoProxy().getComptabiliteDao().insertEcritureComptable(pEcritureComptable);
            getTransactionManager().commitMyERP(vTS);
            vTS = null;
        } finally {
            getTransactionManager().rollbackMyERP(vTS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        TransactionStatus vTS = getTransactionManager().beginTransactionMyERP();
        try {
            getDaoProxy().getComptabiliteDao().updateEcritureComptable(pEcritureComptable);
            getTransactionManager().commitMyERP(vTS);
            vTS = null;
        } finally {
            getTransactionManager().rollbackMyERP(vTS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteEcritureComptable(Integer pId) {
        TransactionStatus vTS = getTransactionManager().beginTransactionMyERP();
        try {
            getDaoProxy().getComptabiliteDao().deleteEcritureComptable(pId);
            getTransactionManager().commitMyERP(vTS);
            vTS = null;
        } finally {
            getTransactionManager().rollbackMyERP(vTS);
        }
    }

    @Override
    public void updateSequenceEcritureComptable(String codeJournal, SequenceEcritureComptable sequenceEcritureComptable) {
        // TODO implement
    }

    @Override
    public void insertSequenceEcritureComptable(String codeJournal, SequenceEcritureComptable sequenceEcritureComptable) {
        // TODO implement
    }
}
