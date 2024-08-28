/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.nodecli.operators.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodecli.operators.teststeps.SsoMngTestSteps;

/**
 * Sso flows.
 */
@SuppressWarnings({ "PMD.LawOfDemeter" })
public class SsoMngFlows {

    @Inject
    private SsoMngTestSteps ssoMngTestSteps;

    /**
     * <pre>
     * <b>Name</b>: setVerifySso            <i>[public]</i>
     * <b>Description</b>: .
     * </pre>
     *
     * @param expValue Parameter for 'expected value' check.
     * @return TestStep flow builder object,
     */
    public TestStepFlowBuilder setVerifySso(final String expValue) {
        final TestStepFlowBuilder flowGet = flow("Set and verify Sso")
                .addTestStep(annotatedMethod(ssoMngTestSteps, SsoMngTestSteps.StepIds.SSO_SET_VERIFY)
                        .withParameter(SsoMngTestSteps.Parameter.SSO_SET_VERIFY, expValue));
        return flowGet;
    }

    /**
     * <pre>
     * <b>Name</b>: setVerifySsoRegression            <i>[public]</i>
     * <b>Description</b>: .
     * </pre>
     *
     * @param expValue Parameter for 'expected value' check.
     * @param expMsg Parameter for 'expected Message' check.
     * @return TestStep flow builder object,
     */
    public TestStepFlowBuilder setVerifySsoRegression(final String expValue, final String expMsg) {
        final TestStepFlowBuilder flowGet = flow("Set and verify Sso")
                .addTestStep(annotatedMethod(ssoMngTestSteps, SsoMngTestSteps.StepIds.SSO_SET_VERIFY)
                        .withParameter(SsoMngTestSteps.Parameter.SSO_SET_VERIFY, expValue)
                        .withParameter(SsoMngTestSteps.Parameter.SSO_SET_EXP_MSG, expMsg));
        return flowGet;
    }
}
