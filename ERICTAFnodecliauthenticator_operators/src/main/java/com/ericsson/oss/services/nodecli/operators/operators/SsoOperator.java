/*
 *
 *    * ------------------------------------------------------------------------------
 *     *******************************************************************************
 *     * COPYRIGHT Ericsson 2022
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

import static com.ericsson.oss.testware.nodesecurity.constant.AgnosticConstants.FILE_NAME;
import static com.ericsson.oss.testware.nodesecurity.constant.AgnosticConstants.NETWORK_ELEMENT_ID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.oss.services.nodecli.operators.factory.SsoFactory;
import com.ericsson.oss.services.nodecli.operators.utility.SsoOperation;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.nodesecurity.operators.RestImpl;
import com.ericsson.oss.testware.nodesecurity.operators.factory.CredentialFactory;
import com.ericsson.oss.testware.nodesecurity.utils.SecurityUtil;

/**
 * <pre>
 * <b>Name</b>: SsoOperator      <i>[public class]</i>
 * <b>Description</b>: This class contains the methods needed to perform SSO commands.
 * </pre>
 */

public class SsoOperator extends RestImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(SsoOperator.class);

    @Inject
    private SsoFactory ssoFactory;

    @Inject
    private CredentialFactory credentialFactory;

    /**
     * <pre>
     * <b>Name</b>: executeSsoCommand      <i>[public]</i>
     * <b>Description</b>: This method executes the required SSO command ({@link SsoOperation})
     * with the information contained in the supplied Datarecord..
     * </pre>
     *
     * @param value DataRecord to use
     * @param ssoOperation Operation type to be performed
     * @return Command Response
     */
    public EnmCliResponse executeSsoCommand(final DataRecord value, final SsoOperation ssoOperation) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} - Executing SSO Operation '{}' with DataRecord\n\t{}", methodName, ssoOperation, value);
        }

        final String fileName = value.getFieldValue(FILE_NAME);
        final String nodeName = value.getFieldValue(NETWORK_ELEMENT_ID);
        final String commandString = ssoFactory.prepareSsoCommand(value, ssoOperation);
        LOGGER.trace("{} - SSO command to send: [{}] to the node: [{}]", methodName, commandString, nodeName);
        if (fileName == null) {
            return sendSecurityCommand(commandString, null, null);
        } else {
            return sendSecurityCommand(commandString, credentialFactory.getTargetFileCmd(value), SecurityUtil.createByteArray(nodeName));
        }
    }
}
