/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.nodecli.operators.teststeps;

import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.nodesecurity.constant.AgnosticConstants.EXPECTED_MESSAGE;
import static com.ericsson.oss.testware.nodesecurity.constant.AgnosticConstants.FILE_NAME;
import static com.ericsson.oss.testware.nodesecurity.constant.AgnosticConstants.NETWORK_ELEMENT_ID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.nodesecurity.operators.RestImpl;
import com.ericsson.oss.testware.nodesecurity.operators.factory.CertificateIssueFactory;
import com.ericsson.oss.testware.nodesecurity.steps.CertificateIssueTestSteps;
import com.ericsson.oss.testware.nodesecurity.steps.TestStepUtil;
import com.ericsson.oss.testware.nodesecurity.utils.JobIdUtils;
import com.ericsson.oss.testware.nodesecurity.utils.SecurityUtil;

/**
 * <pre>
 * <b>Name</b>: IssueMngTestSteps      <i>[public (Class)]</i>
 * <b>Description</b>: This class contains the Test Steps to perform the
 * Certificate Issue operations..
 * </pre>
 */
public class IssueMngTestSteps extends CertificateIssueTestSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(IssueMngTestSteps.class);

    @Inject
    CertificateIssueFactory issueFactory;

    @Inject
    private Provider<RestImpl> provider;

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps.IssueMngTestSteps.StepIds#CERTIFICATE_ISSUE_BASE}
     * <b>Method Name</b>: certificateIssueBase <i>[public]</i>
     * <b>Description</b>: This <i>TestStep</i> is used to execute Certificate Issue Base tests.
     * </pre>
     *
     * @param node contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @param value Datarecord for Certificate information
     * @return - Datarecord with Job execution result.
     */
    @TestStep(id = StepIds.CERTIFICATE_ISSUE_BASE)
    public DataRecord certificateIssueBase(@Input(ADDED_NODES) final DataRecord node,
            @Input(DataSource.CERT_ISSUE_REISSUE_DATASOURCE) final DataRecord value) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.trace("{} - Method Input DataSources:\n\tNode --> {}\n\tCert Issue --> {}", methodName, node, value);
        SecurityUtil.checkDataSource(value, ADDED_NODES);
        final DataRecord newNode = TestStepUtil.mergeMap(node, value);
        final String fdnAuthentication = newNode.getFieldValue(LdapMngTestStep.Param.AUTHENTICATIONFDN);
        final String fdnLdap = newNode.getFieldValue(LdapMngTestStep.Param.LDAP_FDN);
        LOGGER.debug("{} - Executing Certificate Issue TestStep:\n\tNetwork Element Id --> {}\n\tFDN authentication --> {}\n\tFDN lapd value --> {}",
                methodName, newNode.getFieldValue(NETWORK_ELEMENT_ID), fdnAuthentication, fdnLdap);
        if (fdnAuthentication != null && !fdnAuthentication.isEmpty() && fdnLdap != null && !fdnLdap.isEmpty()) {
            final String nodeName = newNode.getFieldValue(NETWORK_ELEMENT_ID);
            final String filename = newNode.getFieldValue(FILE_NAME);
            LOGGER.debug("{} - ldap managed, Send Certificate:\n\tNetwork Element Id --> {}\n\tFileName --> {}", methodName, nodeName, filename);
            final String xmlContentBefore = SecurityUtil.readResourceFile(filename).replace("\r\n", "\n");       // Dos2Unix Replace
            final String xmlContentAfter = String.format(xmlContentBefore, nodeName);
            LOGGER.trace("{} - Certificate content Elaboration:\n\tBefore -> {}\n\tAfter --> {}", methodName, xmlContentBefore, xmlContentAfter);
            final byte[] fileContents = xmlContentAfter.getBytes();
            final String targetFile = nodeName + filename;
            final String commandString = issueFactory.createIssue(newNode);
            final RestImpl restImpl = provider.get();
            LOGGER.trace("{} - Send Command Information:\n\tCommand -> {}\n\tTarget File -> {}\n\tFile Content -> {}",
                    methodName, commandString, targetFile, fileContents);
            final EnmCliResponse response = restImpl.sendCommandWithFile(commandString, targetFile, fileContents);
            LOGGER.debug("{} - Command Response:\n\tCommand -> {}\n\tResponse -> {}", methodName, commandString, response);
            SecurityUtil.checkResponseDto(response, (String) newNode.getFieldValue(EXPECTED_MESSAGE));

            return TestStepUtil.mergeMap(newNode, JobIdUtils.fillJobIdDataRecord(response, (String) newNode.getFieldValue(NETWORK_ELEMENT_ID),
                    (String) newNode.getFieldValue(EXPECTED_MESSAGE)));
        } else {
            if (LOGGER.isInfoEnabled()) {
                final String fdnValue = getContentValue(fdnAuthentication);
                final String fdnLdapValue = getContentValue(fdnLdap);
                LOGGER.info("{} - Certificate Issue is skipped for node {}: \n\tFDN Authentication -> {}\n\tFDN ldap -> {}", methodName,
                        newNode.getFieldValue(NETWORK_ELEMENT_ID), fdnValue, fdnLdapValue);
            }
            return newNode;
        }
    }

    private String getContentValue(final String value) {
        return value == null ? "null" : value.isEmpty() ? "empty" : value;
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
    public static final class StepIds {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CERTIFICATE_ISSUE_BASE = "certificateIssueBase";

        private StepIds() {
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
        public static final String SSO_SET_VERIFY = "SsoSetVerify";

        private Param() {
        }
    }

    /**
     * <pre>
     * <b>Class Name</b>: Command
     * <b>Description</b>: This subclass contains commands to execute with <i>Certificate</i>
     * command executor.
     * </pre>
     */
    public static final class Command {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String ENABLED = "ENABLED";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String DISABLED = "DISABLED";

        private Command() {
        }
    }
}
