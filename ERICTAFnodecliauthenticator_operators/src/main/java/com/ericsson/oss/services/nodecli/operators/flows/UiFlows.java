/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
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
import static com.ericsson.oss.testware.security.authentication.steps.LoginLogoutUiTestSteps.TEST_STEP_LOGIN;
import static com.ericsson.oss.testware.security.authentication.steps.LoginLogoutUiTestSteps.TEST_STEP_LOGOUT;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliUiTestStep;
import com.ericsson.oss.testware.security.authentication.steps.LoginLogoutUiTestSteps;

/**
 * <pre>
 * <b>Class Name</b>: UiFlows
 * <b>Description</b>: This class contains the flows related to the management of
 * graphics operations for this testware..
 * </pre>
 */
public class UiFlows {

    @Inject
    private NodeCliUiTestStep uiSteps;

    @Inject
    private LoginLogoutUiTestSteps loginLogoutTestSteps;

    /**
     * <pre>
     * <b>Name</b>: closeBrowser            <i>[public]</i>
     * <b>Description</b>: This method creates the flow for closing open browsers.
     * </pre>
     *
     * @param numberOfNodes number of parallel execution.
     * @return flow which performs Browser close.
     */
    public TestStepFlow closeBrowser(final int numberOfNodes) {
        return flow("Closes the browser").withVusers(numberOfNodes)
                .addTestStep(annotatedMethod(uiSteps, NodeCliUiTestStep.StepIds.CLOSE_BROWSER)).build();
    }

    /**
     * <pre>
     * <b>Name</b>: searchAndLaunchParallelNodeCLI            <i>[public]</i>
     * <b>Description</b>: This method is used to create a flow that, through the UI,
     * provides access to the node's CLI, verifying its correct opening.
     * </pre>
     *
     * @param vUserCount number of parallel execution.
     * @return flow which performs Node Cli Check.
     */
    public TestStepFlowBuilder searchAndLaunchParallelNodeCLI(final int vUserCount) {
        return flow("Searches for nodes and launches the CLI, working")
                .addTestStep(annotatedMethod(uiSteps, NodeCliUiTestStep.StepIds.INIT_BROWSER_AND_LOGIN))
                .addTestStep(annotatedMethod(uiSteps, NodeCliUiTestStep.StepIds.SEARCH_FOR_NODE_VUSERS))
                .addTestStep(annotatedMethod(uiSteps, NodeCliUiTestStep.StepIds.ASSERT_LAUNCH_NODECLI_PRESENT))
                .addTestStep(annotatedMethod(uiSteps, NodeCliUiTestStep.StepIds.LAUNCH_AND_VERIFY_CLI_PARALLEL)
                        .withParameter(NodeCliUiTestStep.Param.VUSER_PARAM, vUserCount))
                .addTestStep(annotatedMethod(uiSteps, NodeCliUiTestStep.StepIds.CLOSE_BROWSER)).alwaysRun();
    }

    /**
     * <pre>
     * <b>Name</b>: login            <i>[public]</i>
     * <b>Description</b>: Perform login operation using Browser and set that in
     * user session.
     * </pre>
     *
     * @return flow which performs login flowBuilder
     * @see LoginLogoutUiTestSteps#login
     */
    public TestStepFlowBuilder login() {
        return flow("Login").addTestStep(annotatedMethod(loginLogoutTestSteps, TEST_STEP_LOGIN));
    }

    /**
     * <pre>
     * <b>Name</b>: logout            <i>[public]</i>
     * <b>Description</b>: Perform logout operation using Browser.
     * user session.
     * </pre>
     *
     * @return flow which performs logout FlowBuilder
     * @see LoginLogoutUiTestSteps#logout()
     */
    public TestStepFlowBuilder logout() {
        return flow("Logout").addTestStep(annotatedMethod(loginLogoutTestSteps, TEST_STEP_LOGOUT));
    }

}
