/*
 * ------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.nodecli.operators.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.oss.services.nodecli.operators.teststeps.CmSyncTestSteps.Param.CM_SYNCSTATUS_TIMEOUT_PARAMETER;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodecli.operators.teststeps.CmSyncTestSteps;

/**
 * <pre>
 * <b>Class Name</b>: CmSyncFlows
 * <b>Description</b>: This class contains Flows for CM Sync Management operation.
 * </pre>
 */
public class CmSyncFlows {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmSyncFlows.class);
    @Inject
    private CmSyncTestSteps cmSyncTestSteps;

    /**
     * <pre>
     * <b>Name</b>: getCmSync            <i>[public]</i>
     * <b>Description</b>: This Test Flow is used to get Node Synchronization Status for
     *   all 'ADDED_NODES'.
     * </pre>
     *
     * @param syncTimeout (Optional)
     *         This parameter can be provided to configure the timeout
     *                    duration for waiting for node synchronization.
     *                    If not, the default value is used
     * @return Test Flow builder
     */
    public TestStepFlowBuilder getCmSync(final String syncTimeout) {
        LOGGER.debug("Send CM Sync Get Flow ");
        final TestStepFlowBuilder flowCm = flow("Send CM Sync Get flow")
                .addTestStep(annotatedMethod(cmSyncTestSteps, CmSyncTestSteps.TestId.CM_SYNC_GET)
                        .withParameter(CM_SYNCSTATUS_TIMEOUT_PARAMETER, syncTimeout));
        return flowCm.syncPoint("CM synch");
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    public TestStepFlowBuilder getCmSync() {
        return getCmSync("");
    }
}
