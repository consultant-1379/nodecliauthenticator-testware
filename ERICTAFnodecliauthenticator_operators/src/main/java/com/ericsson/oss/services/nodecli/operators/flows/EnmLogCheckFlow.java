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

package com.ericsson.oss.services.nodecli.operators.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.fromTestStepResult;
import static com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliLogTestStep.Param.LOG_FROM_NODECLI_PARAM;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliLogTestStep;
import com.ericsson.oss.testware.teststeps.EnmLogTestStep;

/**
 * <pre>
 * <b>Class Name</b>: EnmLogCheckFlow
 * <b>Description</b>: This class contains the flows relating to the operations
 *   performed on the LOG of the ENM 'blades' (hosts), and in particular it takes
 *   care of 'downloading' the LOGs and examining their contents.
 * </pre>
 */
public class EnmLogCheckFlow {
    private static final int WAIT_FOR_LOG_COLLECTION = DataHandler.getConfiguration().getProperty("log.nodecli.collectingWait.seconds", 10,
            Integer.class);
    @Inject
    private NodeCliLogTestStep nodeCliTestStep;
    @Inject
    private EnmLogTestStep enmHostLogCollectorTestStep;

    /**
     * <pre>
     * <b>Name</b>: checKLoggedUserFlow            <i>[public]</i>
     * <b>Description</b>: TThis flow builder is used to 'read' the LOG of the <i>NodeCli</i>
     *   host and search inside for the correct connection to the node with the
     *   designated user.
     * </pre>
     *
     * @return Test Flow Builder
     */
    public TestStepFlowBuilder checKLoggedUserFlow() {
        return flow("Get 'nodeCli' Log and Check User")
                .pause(WAIT_FOR_LOG_COLLECTION, TimeUnit.SECONDS)
                .addTestStep(annotatedMethod(enmHostLogCollectorTestStep, EnmLogTestStep.StepIds.GET_LOG_TO_STRINGS))
                // TODO - Add Test Step to get Configured Roles for Selected User and pass Getted User Parameters to Next Test Step
                .addTestStep(annotatedMethod(nodeCliTestStep, NodeCliLogTestStep.StepIds.CHECK_NODECLI_LOGGED_USER)
                        .withParameter(LOG_FROM_NODECLI_PARAM, fromTestStepResult(EnmLogTestStep.StepIds.GET_LOG_TO_STRINGS)));
    }

}
