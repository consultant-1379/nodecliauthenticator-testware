/*
 *
 *    * ------------------------------------------------------------------------------
 *     *******************************************************************************
 *     * COPYRIGHT Ericsson 2023
 *     *
 *     * The copyright to the computer program(s) herein is the property of
 *     * Ericsson Inc. The programs may be used and/or copied only with written
 *     * permission from Ericsson Inc. or in accordance with the terms and
 *     * conditions stipulated in the agreement/contract under which the
 *     * program(s) have been supplied.
 *     *******************************************************************************
 *     *----------------------------------------------------------------------------
 *
 *
 */

package com.ericsson.oss.services.nodecli.testware.predicate;

import java.util.Arrays;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep;
import com.google.common.base.Predicate;

/**
 * <pre>
 * <b>Name</b>: PredicateExtended      <i>[public (Class)]</i>
 * <b>Description</b>: This class contains the definition of 'specific' predicates
 * for testware (In addition to those present in the 'base' class).
 * </pre>
 */
public class PredicateExtended extends Predicates {
    private static final Boolean FORCE_NODE_CREATION = (Boolean) DataHandler.getConfiguration()
            .getProperty("force.node.creation.flag", false, Boolean.class);

    /**
     * <pre>
     * <b>Name</b>: netsimNodePredicate            <i>[public]</i>
     * <b>Description</b>: The predicate is used to select the datasource records that
     * contain the value 'NETSIM' in the field
     * --> '{@link Predicates.Fields#COLUMN_NAME_NODEOPERTYPE}' .
     * </pre>
     *
     * @return predicate object
     */
    public static Predicate<DataRecord> netsimNodePredicate() {
        return com.google.common.base.Predicates.or(
                singleValuePredicate(Fields.COLUMN_NAME_NODEOPERTYPE, "NETSIM", true),
                booleanAlwaysPredicete(FORCE_NODE_CREATION));
    }

    /**
     * <pre>
     * <b>Name</b>: DataRecord            <i>[public]</i>
     * <b>Description</b>: The predicate is used to select the datasource records that
     * contain the value 'REAL_NODE' in the field
     * --> '{@link Predicates.Fields#COLUMN_NAME_NODEOPERTYPE}' .
     * </pre>
     *
     * @return predicate object
     */
    public static Predicate<DataRecord> realNodePredicate() {
        return singleValuePredicate(Fields.COLUMN_NAME_NODEOPERTYPE, "REAL_NODE", true);
    }

    /**
     * <pre>
     * <b>Name</b>: ldapManagedPredicate            <i>[public]</i>
     * <b>Description</b>: The predicate is used to evaluate whether a node
     * (described by the DataRecord in inlput) handles the LDAP function.
     * This is deduced from the evaluation of these two fields, dynamically
     * filled by testStep ({@link LdapMngTestStep.StepIds#LDAP_GET}):
     * --> '{@link PredicateExtended.SpecificFields#AUTHENTICATION_FDN}' e '{@link PredicateExtended.SpecificFields#LDAP_FDN}'.
     * </pre>
     * @return predicate object
     */
    public static Predicate<DataRecord> ldapManagedPredicate() {
        return com.google.common.base.Predicates.and(
                com.google.common.base.Predicates.not(singleValuePredicate(SpecificFields.AUTHENTICATION_FDN, "", true)),
                com.google.common.base.Predicates.not(singleValuePredicate(SpecificFields.LDAP_FDN, "", true))
        );
    }

    /**
     * <b>Name</b>: cmAdm            <i>[public]</i>
     * <b>Description</b>: The predicate is used to select the datasource records that
     * contain the value 'Cmedit_Administrator' in the field
     * --> '{@link Predicates.Fields#COLUMN_NAME_ROLES}' .
     *
     * @return predicate object
     */
    public static Predicate<DataRecord> cmAdm() {
        return multiValuesPredicate(Fields.COLUMN_NAME_ROLES, Arrays.asList("Cmedit_Administrator"));
    }

    /**
     * <b>Name</b>: cmAdm            <i>[public]</i>
     * <b>Description</b>: The predicate is used to select the datasource records that
     * contain the value 'Cmedit_Administrator' in the field
     * --> '{@link Predicates.Fields#COLUMN_NAME_ROLES}' .
     *
     * @return predicate object
     */
    public static Predicate<DataRecord> nodeCliOperator() {
        return multiValuesPredicate(Fields.COLUMN_NAME_ROLES, Arrays.asList("NodeCLI_Operator"));
    }

    /**
     * <pre>
     * <b>Name</b>: SpecificFields      <i>[package protected (Class)]</i>
     * <b>Description</b>: This class contains the definition of the 'specific fields'
     * costants.
     * </pre>
     */
    static final class SpecificFields {
        static final String AUTHENTICATION_FDN = LdapMngTestStep.Param.AUTHENTICATIONFDN;
        static final String LDAP_FDN = LdapMngTestStep.Param.LDAP_FDN;
    }
}
