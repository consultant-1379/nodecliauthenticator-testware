/*
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson  2021
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 */

package com.ericsson.oss.services.nodecli.operators.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodecli.operators.teststeps.NetSimTestStep;

/**
 * <pre>
 * <b>Class Name</b>: NewNetsimFlow
 * <b>Description</b>: This class contains the <u>test flows</u> to execute operations on
 * <i>NetSim</i> node simulator and <i>ENM</i> application.
 * </pre>
 */
public class NetsimEnmFlow {
    @Inject
    private NetSimTestStep netsimTestStep;

    /**
     * <pre>
     * <b>Name</b>: resetUserOnNodes            <i>[public]</i>
     * <b>Description</b>: This Test Flow provides for the creation of a sequence
     * dedicated to the execution of the <i>Reset User</i> on the <u>nodes</u> on the
     * selected NetSim hosts.
     * </pre>
     *
     * @return - Test Step flow
     */
    public TestStepFlowBuilder resetUserOnNodes() {
        return flow("Reset Node Users on Netsim Simulation Flow")
                .addTestStep(annotatedMethod(netsimTestStep, NetSimTestStep.StepIds.TEST_STEP_NETSIM_NODE_OPERATION)
                        .withParameter(NetSimTestStep.Param.NETSIM_NODE_COMMAND, "setuser")
                        .withParameter(NetSimTestStep.Param.NETSIM_COMMAND_EXPECTED_RESULT, NetSimTestStep.Param.NETSIM_SUCCESS_RESULT));
    }
}
