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

package com.ericsson.oss.services.nodecli.operators.factory;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.oss.services.nodecli.operators.utility.SsoOperation;
import com.ericsson.oss.testware.nodesecurity.operators.factory.CredentialFactory;

/**
 * Used to create enable or disable SSO command.
 */
public class SsoFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SsoFactory.class);

    @Inject
    private CredentialFactory credentialFactory;

    /**
     * This method is used to prepare Sso enable or disable command for single node, list nodes, and the list nodes from file .
     * @param dataRecord
     *            the object containing the target node for the Sso operation.
     * @param ssoOperation
     *            the object specifying the type of operation to perform.
     * @return command
     */
    public String prepareSsoCommand(final DataRecord dataRecord, final SsoOperation ssoOperation) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String preparedCommand = null;
        switch (ssoOperation) {
            case ENABLE:
                preparedCommand = String.format(Commands.SSO_ENABLE, credentialFactory.getTargetNode(dataRecord));
                break;
            case DISABLE:
                preparedCommand = String.format(Commands.SSO_DISABLE, credentialFactory.getTargetNode(dataRecord));
                break;
            case GET:
                preparedCommand = String.format(Commands.SSO_GET, credentialFactory.getTargetNode(dataRecord));
                break;
            default:
                LOGGER.error("Invalid SSO operation {}", ssoOperation.toString());
                break;
        }
        LOGGER.trace("{} - 'secadm' command to execute: [{}]", methodName, preparedCommand);
        return preparedCommand;
    }

    private static final class Commands {
        static final String SSO_ENABLE = "secadm sso enable %s";
        static final String SSO_DISABLE = "secadm sso disable %s";
        static final String SSO_GET = "secadm sso get %s";
    }
}
