/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.nodecli.operators.teststeps;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static com.ericsson.oss.services.nodecli.operators.teststeps.BaseTestStep.NETWORKELEMENTTYPE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.nodeintegration.utilities.NodeIntegrationConstants.NETWORKELEMENTID;
import static com.ericsson.oss.testware.nodesecurity.constant.AgnosticConstants.JOB_ID_COMMAND;
import static com.ericsson.oss.testware.nodesecurity.constant.AgnosticConstants.JOB_ID_ELEMENT_KEY;
import static com.ericsson.oss.testware.nodesecurity.utils.JobIdUtils.COMMNAND_ID_COLUMN_NAME;
import static com.ericsson.oss.testware.nodesecurity.utils.JobIdUtils.JOB_STATUS_COLUMN_NAME;
import static com.ericsson.oss.testware.nodesecurity.utils.JobIdUtils.JOB_STATUS_COMPLETED;
import static com.ericsson.oss.testware.nodesecurity.utils.JobIdUtils.WORKFLOW_DETAILS_COLUMN_NAME;
import static com.ericsson.oss.testware.nodesecurity.utils.JobIdUtils.WORKFLOW_STATUS_COLUMN_NAME;
import static com.ericsson.oss.testware.nodesecurity.utils.JobIdUtils.WORKFLOW_STATUS_SUCCESS;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.nodesecurity.operators.RestImpl;
import com.ericsson.oss.testware.nodesecurity.steps.JobIdMonitorTestSteps;
import com.ericsson.oss.testware.nodesecurity.utils.JobIdUtils;
import com.ericsson.oss.testware.nodesecurity.utils.SecurityUtil;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * <pre>
 * <b>Name</b>: JobIdMonitorMngTestSteps      <i>[public (Class)]</i>
 * <b>Description</b>: This class contain Test Step to Job Monitor Operation.
 * </pre>
 */
public class JobIdMonitorMngTestSteps extends JobIdMonitorTestSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobIdMonitorMngTestSteps.class);
    private static final Integer JOB_ID_MONITOR_ITERATION = DataHandler.getConfiguration().getProperty("jobMonitor.iteration", 70, Integer.class);
    private static final Integer JOB_ID_MONITOR_DELAY = DataHandler.getConfiguration().getProperty("jobMonitor.timer", 12000, Integer.class);
    private static final String DETAILS_RESPONCE = DataHandler.getConfiguration().getProperty("jobMonitor.result", "already installed", String.class);

    private static final String JOBID_INFO_FULL_DETAIL_FORMAT =
            "Node Name = %s -- ob Status = %s - Workflow Status = %s - Workflow Details = %s - " + "Command Id = %s";
    private static final String JOBID_INFO_FAILED_FORMAT = "%s FAILURE - Info [%s]";
    private static final String WORKFLOW_INFO_FAILED_FORMAT = "%s Workflow FAILURE - Info [%s]";
    private static Map jobStatus = new HashMap<String, String>();
    private static Map workFlowStatus = new HashMap<String, String>();
    private static Map workFlowDetails = new HashMap<String, String>();
    private static Map commandId = new HashMap<String, String>();

    @Inject
    private RestImpl restImpl;

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps}
     * <b>Method Name</b>: jobIdMonitor <i>[public]</i>
     * <b>Description</b>: This <i>TestStep</i> is used to monitor the status of the <u>ENM JOB</u>
     * whose parameters are indicated in the DataRecord received as parameter
     * (function).
     * </pre>
     *
     * @param value contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @return DataRecord
     */
    @TestStep(id = JOB_ID_MONITOR)
    public DataRecord jobIdMonitor(@Input(ADDED_NODES) final DataRecord value) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String nodeId = value.getFieldValue(JOB_ID_ELEMENT_KEY);
        final String networkElemetId = value.getFieldValue(NETWORKELEMENTID) != null ? value.getFieldValue(NETWORKELEMENTID) : "null";

        if (LOGGER.isInfoEnabled()) {
            final String networkElemetType = value.getFieldValue(NETWORKELEMENTTYPE) != null ? value.getFieldValue(NETWORKELEMENTTYPE) : "null";
            LOGGER.info("{} - Executing Test Step with these input:\n\tNetwork Element Id --> {}/{}[{}]\n\tJob ID Command --> {}",
                    methodName, networkElemetId, nodeId, networkElemetType, value.getFieldValue(JOB_ID_COMMAND));
        }

        if (value.getFieldValue(JOB_ID_COMMAND) != null) {
            // Executing function to check Job ID
            final boolean checkJobResult = checkJobId(value);
            LOGGER.trace("{} - Updating '{}' datasource with Job Result:\n\tNetwork Element Id --> {}\n\t{} --> {}", methodName, ADDED_NODES,
                    networkElemetId, LdapMngTestStep.Param.LDAP_JOBSRESULT, checkJobResult);
            final Map<String, Object> data = Maps.newHashMap(value.getAllFields());
            data.put(LdapMngTestStep.Param.LDAP_JOBSRESULT, checkJobResult);
            final DataRecord newNode = new DataRecordImpl(data);
            return newNode;
        } else {
            LOGGER.warn("{} - No JobId on {} ---> {}- check cliResponse for the result ", methodName, networkElemetId, nodeId != null ? nodeId :
                    "-- none --");
            return value;
        }
    }

    /**
     * <pre>
     * <b>Name</b>: checkJobId            <i>[private]</i>
     * <b>Description</b>: This method execute Job ID check and return
     * Job Result message.
     * </pre>
     *
     * @param value contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     */
    private boolean checkJobId(final DataRecord value) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String jobIdCommand = value.getFieldValue(JOB_ID_COMMAND);
        final String jobNetworkElementId = value.getFieldValue(JOB_ID_ELEMENT_KEY);
        final String networkElementId = value.getFieldValue(NETWORKELEMENTID);

        // Start Job monitoring if Job ID is not null.
        if (jobIdCommand != null) {
            int iterationCount = JOB_ID_MONITOR_ITERATION;
            final int iterationInterval = JOB_ID_MONITOR_DELAY;
            LOGGER.debug("{} - Start JOB monitoring:\n\tNetwork Element ID --> {}({})\n\tJob Command --> {}"
                            + "\n\tIteration Count --> {}\n\tIteration Interval (sec) --> {}",
                    methodName, jobNetworkElementId, networkElementId, jobIdCommand, iterationCount, MILLISECONDS.toSeconds(iterationInterval));

            // Init Job monitor Data
            jobStatus.put(jobNetworkElementId, null);
            workFlowStatus.put(jobNetworkElementId, null);
            workFlowDetails.put(jobNetworkElementId, null);
            String detailInfo = "";

            // Loop for Job Monitor
            do {
                SecurityUtil.delay(iterationInterval);
                final EnmCliResponse responseJobId = restImpl.sendCommand(jobIdCommand);

                // Update Job Monitor data Structures
                jobStatus.put(jobNetworkElementId, JobIdUtils.findStatus(responseJobId, JOB_STATUS_COLUMN_NAME));
                workFlowStatus.put(jobNetworkElementId, JobIdUtils.findStatus(responseJobId, WORKFLOW_STATUS_COLUMN_NAME));
                workFlowDetails.put(jobNetworkElementId, JobIdUtils.findStatus(responseJobId, WORKFLOW_DETAILS_COLUMN_NAME));
                commandId.put(jobNetworkElementId, JobIdUtils.findStatus(responseJobId, COMMNAND_ID_COLUMN_NAME));

                workFlowDetails.putIfAbsent(jobNetworkElementId, workFlowDetails.put(jobNetworkElementId, "No Details available"));

                detailInfo = String.format(JOBID_INFO_FULL_DETAIL_FORMAT, jobNetworkElementId, jobStatus.get(jobNetworkElementId),
                        workFlowStatus.get(jobNetworkElementId), workFlowDetails.get(jobNetworkElementId), commandId.get(jobNetworkElementId));
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(String.format("%s - Job Recursive Info: Iteration Count -> %d/%d%s[%s]", methodName,
                            JOB_ID_MONITOR_ITERATION - iterationCount + 1, JOB_ID_MONITOR_ITERATION, "\n\t", detailInfo));
                }
                iterationCount--;
            }
            while (iterationCount > 0 && !JOB_STATUS_COMPLETED.equals(jobStatus.get(jobNetworkElementId)));
            final String finalJobStatus = (String) jobStatus.get(jobNetworkElementId);
            final String jobWorkflowStatusValue = (String) workFlowStatus.get(jobNetworkElementId);
            boolean jobResultStatus = finalJobStatus.contains(JOB_STATUS_COMPLETED) && jobWorkflowStatusValue.equals(WORKFLOW_STATUS_SUCCESS);
            LOGGER.debug("{} - Job Monitor final Result for NetworkElementId '{}'\n\tJob Status --> '{}' after {} loop"
                    + "\n\tWorkflow Status --> {}\n\tJob Boolean Result --> {}", methodName, jobNetworkElementId,
                    finalJobStatus, JOB_ID_MONITOR_ITERATION - iterationCount + 1, jobWorkflowStatusValue, jobResultStatus);

            // Parse Detailed Response
            for (final String singleItemResult : DETAILS_RESPONCE.split(",")) {
                jobResultStatus |= jobWorkflowStatusValue.contains(singleItemResult);
            }

            final String jobFailMessage = String.format(JOBID_INFO_FAILED_FORMAT, jobNetworkElementId, detailInfo);
            LOGGER.info("{} - Check JOB ID result for {}:\n\t{}", methodName, jobNetworkElementId, jobFailMessage);
            return jobResultStatus;
        } else {
            LOGGER.warn("{} - No JobId on {} ({}) - check cliResponse for the result ", methodName, jobNetworkElementId, networkElementId);
            return true;
        }
    }

}
