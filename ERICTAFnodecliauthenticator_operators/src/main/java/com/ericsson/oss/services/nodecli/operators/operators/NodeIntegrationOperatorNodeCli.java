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

package com.ericsson.oss.services.nodecli.operators.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.enmbase.data.NetworkNode;
import com.ericsson.oss.testware.nodeintegration.exceptions.NodeIntegrationOperatorException;
import com.ericsson.oss.testware.nodeintegration.operators.impl.NodeIntegrationOperatorBase;

/**
 * <pre>
 * <b>Name</b>: NodeIntegrationOperatorNodeCli      <i>[public (Class)]</i>
 * <b>Description</b>: This class is an extract of the ENM node synchronization check
 *   functions retrieved from the CM libraries to be customized..
 * </pre>
 */
@Operator
public class NodeIntegrationOperatorNodeCli extends NodeIntegrationOperatorBase {
    private static final Integer NODECLI_REPEAT_COUNT = DataHandler.getConfiguration().getProperty("fm.cmsync.repeat.count", 80, Integer.class);
    private static final Integer NODECLI_WAIT_FOR_REPEAT_COUNT = DataHandler.getConfiguration().getProperty("fm.cmsync.wait.count", 3000,
            Integer.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeIntegrationOperatorNodeCli.class);

    /**
     * <pre>
     * <b>Method Name</b>: fmWaitForSyncToComplete                       <i>[public]</i>
     * <b>Description</b>: This method, obtained from an extension of the
     *   '{@link com.ericsson.oss.testware.nodeintegration.operators.impl.NodeIntegrationOperatorBase}' class of the <i>enm-cm-test-library</i>
     *   library, is used to be able to modify the number of repetitions and the
     *   interval between them, retrieving the values from the
     *   '{@link #NODECLI_REPEAT_COUNT}' and '{@link  #NODECLI_WAIT_FOR_REPEAT_COUNT}' properties.
     * </pre>
     *
     * @param node NetworkNode Object
     * @param httpTool Rest Tool for sending Sync Command
     * @throws NodeIntegrationOperatorException Exception for NodeIntegrator
     */
    public void fmWaitForSyncToComplete(final NetworkNode node, final HttpTool httpTool) throws NodeIntegrationOperatorException {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final int numRetries = NODECLI_REPEAT_COUNT;
        final int interval = NODECLI_WAIT_FOR_REPEAT_COUNT;
        LOGGER.debug("{} - Wait foer Sync:\n\tNode Name --> {} [{}]\n\tWait Time interval --> {}\n\tWait Repeat Count --> {}",
                methodName, node.getNetworkElementId(), node.getNodeType(), interval, numRetries);
        waitForSyncToComplete(node, numRetries, interval, httpTool);
    }

    @Override
    public boolean isApplicable(final NetworkNode networkNode) {
        return false;
    }

    @Override
    public EnmCliResponse addSingleNode(final NetworkNode networkNode, final HttpTool httpTool) {
        return null;
    }

    @Override
    public boolean confirmSingleNodeAdded(final NetworkNode networkNode, final HttpTool httpTool) {
        return false;
    }

    @Override
    public void waitForSyncToComplete(final NetworkNode networkNode, final HttpTool httpTool) throws NodeIntegrationOperatorException {
    }

    @Override
    public void waitForSyncRepeat(final NetworkNode networkNode, final HttpTool httpTool) throws NodeIntegrationOperatorException {
    }

    /**
     * <pre>
     * <b>Method Name</b>: executeRestCall                       <i>[public]</i>
     * <b>Description</b>: .
     * </pre>
     *
     * @param command Rest command to send
     * @param httpTool Rest Tool for sending Sync Command
     * @return EnmCliResponse for selected command
     */
    public EnmCliResponse executeRestCall(final String command, final HttpTool httpTool) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.debug("{} - Executing Rest Command:\n\tCommand --> {} ", methodName, command);
        return super.executeRestCall(command, httpTool);
    }
}
