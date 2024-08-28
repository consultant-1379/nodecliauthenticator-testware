/*
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson  2020
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *
 */

package com.ericsson.oss.services.nodecli.operators.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep;

/**
 * <pre>
 * <b>Class Name</b>: LdapMngFlow
 * <b>Description</b>: This class contains Flows for LDAP Management operation.
 * </pre>
 */
public class LdapMngFlow {
    @Inject
    private LdapMngTestStep ldapMngTestStep;

    /**
     * <pre>
     * <b>Name</b>: setAuthenticationEnabled            <i>[public]</i>
     * <b>Description</b>: This method prepares a <i>testStepFlowBuilder</i> that performs the
     * operation of unlocking the administrative state for the <u>LDAP</u> function.
     * </pre>
     *
     * @return TestStep flow builder
     */
    public TestStepFlowBuilder setAuthenticationEnabled() {
        return flow("Set Authentication State Enabled")
                .addTestStep(annotatedMethod(ldapMngTestStep, LdapMngTestStep.StepIds.LDAP_SET_ADMINISTRATIVESTATE)
                        .withParameter(LdapMngTestStep.Param.VALUE, LdapMngTestStep.Param.ADMSTATE_UNLOCKED));
    }

    /**
     * <pre>
     * <b>Name</b>: setAuthenticationEnabled            <i>[public]</i>
     * <b>Description</b>: This method prepares a <i>testStepFlowBuilder</i> that performs the
     * operation of locking the administrative state for the <u>LDAP</u> function.
     * </pre>
     * @return TestStep flow builder
     */
    public TestStepFlowBuilder setAuthenticationDisabled() {
        return flow("Set Authentication State Disabled")
                .addTestStep(annotatedMethod(ldapMngTestStep, LdapMngTestStep.StepIds.LDAP_SET_ADMINISTRATIVESTATE)
                        .withParameter(LdapMngTestStep.Param.VALUE, LdapMngTestStep.Param.ADMSTATE_LOCKED));
    }

    /**
     * <pre>
     * <b>Name</b>: setAuthenticationEnabled            <i>[public]</i>
     * <b>Description</b>: This method prepares a <i>testStepFlowBuilder</i> that performs the
     * operation of setting the Profile Filter for the <u>LDAP</u> function.
     * </pre>
     * @return TestStep flow builder
     */
    public TestStepFlowBuilder setLdapProfileFilter() {
        return flow("Set Ldap Profile Filter")
                .addTestStep(annotatedMethod(ldapMngTestStep, LdapMngTestStep.StepIds.LDAP_SET_PROFILEFILTER));
    }
}
