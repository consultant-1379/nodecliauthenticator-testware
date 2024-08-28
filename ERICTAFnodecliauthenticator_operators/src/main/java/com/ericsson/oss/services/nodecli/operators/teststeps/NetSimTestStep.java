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

package com.ericsson.oss.services.nodecli.operators.teststeps;

import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.NetsimInfoDatafield.NETSIM_INFO_FIELD_NETSIMHOST;
import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.NetsimInfoDatafield.NETSIM_INFO_FIELD_SIMULNAME;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.NODES_TO_ADD;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.tools.cli.TafCliToolShell;
import com.ericsson.de.tools.cli.CliCommandResult;
import com.ericsson.oss.services.nodecli.operators.utility.BasicUtility;
import com.ericsson.oss.testware.enmbase.data.NetworkNode;
import com.ericsson.oss.testware.hostconfigurator.HostConfigurator;
import com.ericsson.oss.testware.network.teststeps.NetworkElementTestSteps;
import com.ericsson.oss.testware.remoteexecution.operators.PibConnectorImpl;

/**
 * <pre>
 * <b>Class Name</b>: NetSimTestStep
 * <b>Description</b>: This class contains the test steps of the operations performed on
 * the 'NetSim' node simulator.
 * It's a new version that get information from Deployment Web Repository.
 * </pre>
 */
public class NetSimTestStep extends BasicUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetSimTestStep.class);

    private static final String COMMAND_RESULT =
            "Executed Command: \nCommand result ({}): \n\tExit Code: {}, execution time: {}\n\tResult Output: " + "{}";
    private static final String COMMAND_HOSTNAME = "hostname";
    private static final Boolean FORCE_NODE_CREATION = (Boolean) DataHandler.getConfiguration()
            .getProperty("force.node.creation.flag", false, Boolean.class);
    private String netsimPipePath = DataHandler.getConfiguration().getProperty("netsim_pipe.path", "/netsim/inst/netsim_pipe", String.class);
    @Inject
    private NetworkElementTestSteps networkElementTestSteps;

    /**
     * <pre>
     * <b>Method Name</b>: netSimNodeOperation           <i>[public]</i>
     * <b>Test Step Name</b>: StepIds.TEST_STEP_NETSIM_NODE_OPERATION
     * <b>Description</b>: This Test Step allows you to execute some commands on NetSim
     * nodes, checking the output and comparing it with the expected one.
     * </pre>
     *
     * @param netsim   DataRecord with NetSim Host info
     * @param command  Netsim command to execute.
     * @param expected Expected NetSim output
     */
    @TestStep(id = NetSimTestStep.StepIds.TEST_STEP_NETSIM_NODE_OPERATION)
    public void netSimNodeOperation(@Input(NODES_TO_ADD) final DataRecord netsim,
            @Input(Param.NETSIM_NODE_COMMAND) final String command, @Input(Param.NETSIM_COMMAND_EXPECTED_RESULT) final String expected) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final Host netSimHost = HostConfigurator.getHost(netsim.getFieldValue(NETSIM_INFO_FIELD_NETSIMHOST));
        final String simulation = netsim.getFieldValue(NETSIM_INFO_FIELD_SIMULNAME);
        final String networkElementId = netsim.getFieldValue(Param.NODE_FIELD_NE_ID);
        final String netsimOperation = "." + command.toLowerCase();
        final String netsimStringExpected = expected;
        LOGGER.info("{} - Input Data (From DataRecord):\n\tNetSim Host Name --> {} ({})\n\tSimulation Name --> {}\n\tNetwork Element ID --> {}"
                + "\n\tNetsim Operation Command --> '{}'\n\tNetsim Expected Result --> '{}'",
                methodName, netSimHost, netsim.getFieldValue(NETSIM_INFO_FIELD_NETSIMHOST), simulation, networkElementId, netsimOperation,
                netsimStringExpected);
        final TafCliToolShell cliToolShell = getNetsimConnection(netSimHost);
        Assertions.assertThat(cliToolShell).as("Cannot Open NetSim CliTolShell (Null)").isNotNull();
        final String netSimSelectedOperation = command.toLowerCase().substring(0, 1).toUpperCase() + command.toLowerCase().substring(1);
        LOGGER.trace("\n{} Nodes ...\n\ton Netsim = {}\n\tSimulation = {}\n\tNetwork Element = {}\n\tPIB Host = {}\n\tNetsim Command = "
                        + "'{}'\n\tExpected Output = '{}'", netSimSelectedOperation,
                netSimHost.getHostname(), simulation, networkElementId, HostConfigurator.getPibHost().getHostname(), netsimOperation,
                netsimStringExpected);

        final String netsimNodeCommand = String.format("echo -e \"%s\" | %s -sim %s -ne %s", netsimOperation, netsimPipePath, simulation,
                networkElementId);
        final String sanityAndNesimCommand = "whoami;hostname;pwd;" + netsimNodeCommand;
        LOGGER.debug("Sending {} Nodes command: {}", netSimSelectedOperation,
                sanityAndNesimCommand);
        final String result = getCommandResultAndCloseShell(netSimHost, cliToolShell, sanityAndNesimCommand, 60);
        LOGGER.debug("Getting {} Nodes Result: {}", netSimSelectedOperation, result);
        Assertions.assertThat(result.contains(netsimStringExpected)).as(String.format("%s Node '%s' Failed",
                netSimSelectedOperation, networkElementId)).isTrue();
    }

    /**
     * Start then node.
     *
     * @param node
     *         NetworkNode
     */
    @TestStep(id =  NetSimTestStep.StepIds.TEST_STEP_NETSIM_NODE_START)
    public void startNode(@Input(NODES_TO_ADD) final NetworkNode node) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String operationType = FORCE_NODE_CREATION ? "NETSIM" : node.getFieldValue("nodeOperatorType");
        final String networkElementId = node.getNetworkElementId();
        LOGGER.trace("{} - Input Parameter:\n\tNode Name --> {}\n\tNode Operator Type --> {} (Flag -> {}, OperationType -> {}) {}",
                methodName, networkElementId, operationType, FORCE_NODE_CREATION, node.getFieldValue("nodeOperatorType"),
                operationType == null ? "--> Skip Start Nodes" : "");
        if (operationType != null) {
            networkElementTestSteps.startNode(networkElementId, operationType, "", null);
        }
    }

    /**
     * <pre>
     * <b>Name</b>: getPibConnectio  <i>[public]</i>
     * <b>Description</b>: PibConnector function .
     * </pre>
     * downloadFileFromRemote
     *
     * @param netSimHost - NetSimHost
     */
    private TafCliToolShell getNetsimConnection(final Host netSimHost) {
        LOGGER.info("=== Get PIB connector and Open NetSim Shell ===");
        final Host gateWay = HostConfigurator.getPibHost();
        if (netSimHost == null || gateWay == null) {
            LOGGER.error(" Cannot Get Cli Tool Shell because one of these host are null:\n\tnetSimHost is null --> '{}'\n\tgateWay is null --> '{}'",
                    netSimHost == null, gateWay == null);
            return null;
        }
        LOGGER.debug("\nPib Connector Info:\n\tPIB name --> {}\n\tip --> {}\n\tUsername --> {}\n\tNetsim HostName -->{}"
                        + "\n\tNetSim Host Address --> {}/{}\n\tNetSim Original Address --> {}:{}\n\tNetsim Default User --> {} ({})",
                gateWay.getHostname(), gateWay.getIp(), gateWay.getDefaultUser().getUsername(),
                netSimHost.getHostname(), netSimHost.getIp(), netSimHost.getIpv6(), netSimHost.getOriginalIp(), netSimHost.getOriginalPort(),
                netSimHost.getDefaultUser().getUsername(), netSimHost.getDefaultUser().getType());
        final TafCliToolShell cliToolShell = new PibConnectorImpl().getConnection();
        CliCommandResult hostNameValue = cliToolShell.execute(COMMAND_HOSTNAME);
        LOGGER.debug("\n PIB Host name (from remote) --> {} \n", hostNameValue.getOutput());

        // Original IP/Port kept for reference after tunnel is opened
        if (netSimHost.getOriginalIp() != null) {
            netSimHost.setIp(netSimHost.getOriginalIp());
        }
        if (netSimHost.getOriginalPort() != null) {
            netSimHost.setPort(netSimHost.getOriginalPort());
        }

        cliToolShell.hopper().hop(netSimHost);
        hostNameValue = cliToolShell.execute(COMMAND_HOSTNAME);
        LOGGER.debug("\n Netsim Host name (from remote Hop) --> {} \n", hostNameValue.getOutput());
        return cliToolShell;
    }

    /**
     * <pre>
     * <b>Name</b>: getCommandResultAndCloseShell  <i>[public]</i>
     * <b>Description</b>: Resul from Shell command .
     * </pre>
     *
     * @param netSimHost   - NetSimHost
     * @param cliToolShell - TafCliToolShell
     * @param command      - String
     */
    private String getCommandResultAndCloseShell(final Host netSimHost, final TafCliToolShell cliToolShell, final String command, final int timeout) {
        return getCommandResultAndCloseShell(netSimHost, cliToolShell, command, timeout, false);
    }

    private String getCommandResultAndCloseShell(final Host netSimHost, final TafCliToolShell cliToolShell, final String command, final int timeout,
            final boolean skipAssert) {
        CliCommandResult result = cliToolShell.execute(COMMAND_HOSTNAME);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing operation...\n"
                            + "Cli command execution (from {}):\n\tHost Name --> {}\n\tHost Address --> {}\n\tCommand to execute --> {}"
                            + "\n\tTimeout --> {}\n\tSkip Command Fail Assert --> {}",
                    result.getOutput(), netSimHost.getHostname(), getpriorityIp(netSimHost), command, timeout > 0 ? timeout : "default", skipAssert);
        }
        if (timeout > 0) {
            result = cliToolShell.execute(command, timeout);
        } else {
            result = cliToolShell.execute(command);
        }
        LOGGER.debug(COMMAND_RESULT, netSimHost.getHostname(), result.getExitCode(), result.getExecutionTime(), result.getOutput());
        if (!skipAssert) {
            Assertions.assertThat(result.isSuccess()).as("Cli Command Execution Failed").isTrue();
        }
        cliToolShell.close();
        return result.getOutput();
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
        public static final String NETSIM_NODE_COMMAND = "NetsimNodeCommandParameter";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String NETSIM_COMMAND_EXPECTED_RESULT = "NetsimNodeExpectedParameter";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String NODE_FIELD_NE_ID = "networkElementId";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String NETSIM_SUCCESS_RESULT = "OK";

        private Param() {
        }
    }

    /**
     * <pre>
     * <b>Class Name</b>: StepIds
     * <b>Description</b>: This subclass contains the test steps identifiers
     * contained in this class.
     * </pre>
     */
    public static final class StepIds {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String TEST_STEP_NETSIM_NODE_OPERATION = "NetSimNodeOperation";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String TEST_STEP_NETSIM_NODE_START = "NetsimNodeStart";

        private StepIds() {
        }
    }
}
