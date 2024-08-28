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

package com.ericsson.oss.services.nodecli.operators.teststeps;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Provider;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.OptionalValue;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.oss.services.nodecli.operators.operators.NodeIntegrationOperatorNodeCli;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.enmbase.data.NetworkNode;
import com.ericsson.oss.testware.nodeintegration.operators.impl.NodeSupervisionOperator;
import com.ericsson.oss.testware.nodeintegration.teststeps.NodeIntegrationTestSteps;
import com.ericsson.oss.testware.security.authentication.tool.TafToolProvider;

/**
 * This class contains specific CliCommand test steps where an enhanced syntax verification is needed.
 * TestStep ID = CLI_COMMAND retrieves DataRecords from DataSource CLI_COMMANDS_DS or CLI_COMMANDS_TIME_DS,
 * checks its data consistency (not to be null), and verifies the response conformance against
 * the expected value.
 */
public class CmSyncTestSteps extends NodeIntegrationTestSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmSyncTestSteps.class);
    private static final Integer NODESYNC_AWAIT_ATMOST_SEC = DataHandler.getConfiguration().getProperty("nodeCliSync.await.atmost", 60,
            Integer.class);
    private static final Integer NODECLISYNC_AWAIT_INTERVAL_SEC = DataHandler.getConfiguration().getProperty("nodeCliSync.await.interval", 5,
            Integer.class);
    private static final Integer NODESYNC_AWAIT_DELAY_SEC = DataHandler.getConfiguration().getProperty("nodeCliSync.await.delay", 1, Integer.class);

    @Inject
    private TafToolProvider tafToolProvider;
    @Inject
    private TestContext context;
    @Inject
    private Provider<NodeIntegrationOperatorNodeCli> nodeIntegrationOperator;
    @Inject
    private Provider<NodeSupervisionOperator> nodeSupervisionOperatorProvider;

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link TestId#CM_SYNC_GET}
     * <b>Method Name</b>: getCmSyncRecoursive                       <i>[public]</i>
     * <b>Description</b>: T.
     * </pre>
     *
     * @param node
     *         NetworkNode Object used in Test Step
     * @param expectedStatus (Optional)
     *         Expected Sync Status: default value {@link Param#CM_SYNC_SYNCHRONIZED}
     * @param syncTimeoutValueString (Optional)
     *         Timeout Sync Value
     */
    @TestStep(id = TestId.CM_SYNC_GET)
    public void getCmSyncRecoursive(@Input(ADDED_NODES) final NetworkNode node,
            @Input(Param.CM_SYNCSTATUS_PARAMETER) @OptionalValue(Param.CM_SYNC_SYNCHRONIZED) final String expectedStatus,
            @Input(Param.CM_SYNCSTATUS_TIMEOUT_PARAMETER) @OptionalValue("") final String syncTimeoutValueString) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        Assertions.assertThat(node).as("Expected 'NetworkNode' parameter is NULL").isNotNull();
        final int delayToCheck = context.getVUser() * NODESYNC_AWAIT_DELAY_SEC;
        final int pollInterval = NODECLISYNC_AWAIT_INTERVAL_SEC;
        final int timeout = syncTimeoutValueString.isEmpty() || !Pattern.matches("[0-9]+[\\.]?[0-9]*", syncTimeoutValueString)
                ? NODESYNC_AWAIT_ATMOST_SEC : Integer.valueOf(syncTimeoutValueString);
        LOGGER.debug("{} - Test Step Name --> {}\n\tNetwork Node --> {} ({})\n\tExpected Status --> {}"
                + "\n\tStart Polling Delay --> {} sec\n\tPoll interval --> {} sec\n\tTimeout --> {} sec",
                methodName, TestId.CM_SYNC_GET, node.getNetworkElementId(), node.getNodeType(), expectedStatus, delayToCheck, pollInterval, timeout);
        final NodeIntegrationOperatorNodeCli nodeIntegrationOperatorNodeCliLoc = nodeIntegrationOperator.get();
        final String nodeSyncStatus;

        try {
            Awaitility.with().pollDelay(delayToCheck, TimeUnit.SECONDS).pollInterval(pollInterval, TimeUnit.SECONDS).atMost(timeout, TimeUnit.SECONDS)
                    .await().until(getSyncStatusCallable(nodeIntegrationOperatorNodeCliLoc, node, expectedStatus), is(equalTo(true)));
        } catch (final ConditionTimeoutException exception) {
            LOGGER.warn("{} - Reached Timeout for Sync Check", methodName);
        } finally {
            nodeSyncStatus = getSyncStatus(node, nodeIntegrationOperatorNodeCliLoc);
        }

        LOGGER.debug("{} - Node Sync Status check:\n\tNode --> {}[{}]\n\tExpected Sync Status --> {}\n\tNode Sync Status --> {}"
                + "\n\tCheck Result --> {}", methodName, node.getNetworkElementId(), node.getNodeType(), expectedStatus,
                nodeSyncStatus, expectedStatus.equalsIgnoreCase(nodeSyncStatus));
        Assertions.assertThat(nodeSyncStatus)
                .as(String.format("%s (%s) - %s",
                        node.getNetworkElementId(), node.getNodeType(), nodeSyncStatus.toUpperCase()))
                .isEqualToIgnoringCase(expectedStatus);
    }

    private String getSyncStatus(final NetworkNode node, final NodeIntegrationOperatorNodeCli nodeIntegrationOperatorNodeCliLoc) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.debug("{} - Check Node Sync Status:\n\tNode --> {}\n\tOperator --> {}", methodName, node.getNetworkElementId(),
                nodeIntegrationOperatorNodeCliLoc);

        final String command = String.format(Command.CM_SYNC_GET_COMMAND, node.getNetworkElementId());
        final EnmCliResponse enmCliResponse = nodeIntegrationOperatorNodeCliLoc.executeRestCall(command, tafToolProvider.getHttpTool());

        String syncStatus = "";
        final List<Map<String, String>> dataRecordList = enmCliResponse.getAllAtributesPerObjectSingleTableView();
        for (int i = 0; i < dataRecordList.size(); i++) {
            final Map<String, String> datarecord = dataRecordList.get(i);
            syncStatus = datarecord.get(Param.CM_SYNCSTATUS_FIELD);
        }
        LOGGER.debug("{} - Check Node Sync Result:\n\tNode --> {}[{}]\n\tSync Status --> {}",
                methodName, node.getNetworkElementId(), node.getNodeType(), syncStatus);
        return syncStatus;
    }

    private Callable<Boolean> getSyncStatusCallable(final NodeIntegrationOperatorNodeCli nodeIntegrationOperatorNodeCliLoc, final NetworkNode node,
            final String expectedStatus) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                final String syncStatus = getSyncStatus(node, nodeIntegrationOperatorNodeCliLoc);
                return expectedStatus.equalsIgnoreCase(syncStatus);
            }
        };
    }

    // ************************************************************************
    // * Inner Classes to defin TestId, Parameters and local Commands.
    // ************************************************************************

    /**
     * <pre>
     * <b>Class Name</b>: StepIds
     * <b>Description</b>: This subclass contains the test steps identifiers
     * contained in this class.
     * </pre>
     */
    public static final class TestId {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CM_SYNC_GET = "CmSyncGet";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CM_TOGGLE_SYNC_SET = "CmSyncToggleSet";

        private TestId() {
        }
    }

    /**
     * <pre>
     * <b>Class Name</b>: Param
     * <b>Description</b>: This subclass contains local parameters names for
     * this TestStep class.
     * </pre>
     */
    public static final class Param {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CM_SYNC_PARAM = "cmSyncParam";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CM_SYNCSTATUS_FIELD = "syncStatus";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CM_SYNCSTATUS_PARAMETER = "syncStatusParameter";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CM_SYNCSTATUS_TIMEOUT_PARAMETER = "syncStatusTimeoutParameter";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CM_SYNCSTATUS_SETUP_FIELD = "syncStatusSetup";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CM_SYNC_UNSYNCHRONIZED = "UNSYNCHRONIZED";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CM_SYNC_SYNCHRONIZED = "SYNCHRONIZED";

        private Param() {
        }
    }

    /**
     * <pre>
     * <b>Class Name</b>: Command
     * <b>Description</b>: This subclass contains commands to execute with <i>Credential</i>
     * command executor.
     * </pre>
     */
    public static final class Command {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CM_SYNC_GET_COMMAND = "cmedit get %s CmFunction.syncStatus --table";

        private Command() {
        }
    }
}

