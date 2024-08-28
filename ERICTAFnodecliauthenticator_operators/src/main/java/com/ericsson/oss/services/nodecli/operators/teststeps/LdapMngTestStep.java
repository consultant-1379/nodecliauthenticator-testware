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

import java.util.Arrays;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.OptionalValue;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.oss.services.nodecli.operators.factory.LdapFactory;
import com.ericsson.oss.services.nodecli.operators.utility.DataSourceName;
import com.ericsson.oss.services.scriptengine.spi.dtos.summary.SummaryDto;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.nodesecurity.operators.RestImpl;
import com.ericsson.oss.testware.nodesecurity.steps.TestStepUtil;
import com.ericsson.oss.testware.nodesecurity.utils.JobIdUtils;
import com.ericsson.oss.testware.nodesecurity.utils.SecurityUtil;
import com.google.common.collect.Maps;

/**
 * <pre>
 * <b>Class Name</b>: AgatNetSimTestStep
 * <b>Description</b>: This class contains the test steps of the operations performed on the 'NetSim' node simulator..
 * </pre>
 */
public class LdapMngTestStep extends BaseTestStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapMngTestStep.class);
    private static final String NEWLINE = "[\\n]";
    private static final String NULL_STRING = "null";

    @Inject
    private TestContext context;
    @Inject
    private Provider<RestImpl> restImpl;
    @Inject
    private LdapFactory ldapFactory;

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep.StepIds#LDAP_GET}
     * <b>Method Name</b>: ldapget <i>[public]</i>
     * <b>Description</b>: This <i>TestStep</i> is used to verify, through the <u>ENM CLI</u>,
     * the presence of the 'ldap' object on the selected node.
     * The <i>TestStep</i> returns the result of the verification by creating a
     * <i>DataRecord</i> in the <i>DataSource</i> containing the list of nodes with the 'ldap'
     * object present.
     * </pre>
     *
     * @param node DataRecord containing node information ({@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES}).
     * @return - Datarecord containing 'ldap' information.
     */
    @TestStep(id = StepIds.LDAP_GET)
    public DataRecord ldapGet(@Input(ADDED_NODES) final DataRecord node) {
        // TODO - insert here LDAP check for PCC/PCG nodes...
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String nodeName = node.getFieldValue(NETWORKELEMENTID);
        final String command = String.format(Command.LDAP_GET_COMMAND, nodeName);
        LOGGER.debug("{} - Executing Command:\n\tNetwork Element Id --> {} [{}]\n\tCommand --> {}",
                methodName, nodeName, node.getFieldValue(NETWORKELEMENTTYPE), command);
        final EnmCliResponse enmCliResponse = restImpl.get().sendCommand(command);
        LOGGER.trace("{} - Command Response: [{}]\n\t--> {}", methodName, enmCliResponse.isCommandSuccessful(), enmCliResponse);
        String ldapFdn = "";
        if (enmCliResponse.isCommandSuccessful()) {
            final Map<String, Map<String, String>> mapFdnValue = enmCliResponse.getAttributesPerFdn();
            if (mapFdnValue != null && !mapFdnValue.isEmpty() && (mapFdnValue.size() != 0)) {
                final Map<String, String> map = (Map<String, String>) mapFdnValue.values().toArray()[0];
                LOGGER.trace("{} - Get response Values:\n\tAttribute per FDN -> {}\n\tMap Values -> {}\n\tMap Element -> {}",
                        methodName, mapFdnValue, mapFdnValue.values(), map);
                if (map != null) {
                    ldapFdn = map.get("FDN");
                }
            }
        }

        // Create LDAP NodeToAdd DataSource
        final Map<String, Object> data = Maps.newHashMap(node.getAllFields());
        data.put(Param.LDAP_FDN, ldapFdn);
        final DataRecord newNode = new DataRecordImpl(data);
        context.dataSource(DataSourceName.LDAPNODESTOADD).addRecord().setFields(newNode);
        LOGGER.trace("{} - Added Parameter '{}' [{}] to dataRecord: --> {}",
                methodName, Param.LDAP_FDN, ldapFdn, newNode);
        return newNode;
    }

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep.StepIds#LDAP_GET_ADMINISTRATIVESTATE}
     * <b>Method Name</b>: ldapGetAdministrativeState <i>[public]</i>
     * <b>Description</b>: This <i>TestStep</i> is used to query, through the <u>ENM CLI</u>,
     * the 'administrative status' of LDAP.
     * If the 'LdapAuthenticationMethod' object is present, the field is added to
     * the <i>DataRecord</i> of the node and returned as output of the <i>TestStep</i>.
     * </pre>
     *
     * @param node DataRecord containing node information ({@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES}).
     * @return - Datarecord containing 'administrative status' information.
     */
    @TestStep(id = StepIds.LDAP_GET_ADMINISTRATIVESTATE)
    public DataRecord ldapGetAdministrativeState(@Input(ADDED_NODES) final DataRecord node) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String nodeName = node.getFieldValue(NETWORKELEMENTID);
        final String commandToUse = Command.AUTHENTICATION_GET_COMMAND;
        // TODO - PCC/PCG nodes: find suitable command to get LDAP capability
        // if ("PCC".equalsIgnoreCase(node.getFieldValue("nodeType")) || "PCG".equalsIgnoreCase(node.getFieldValue("nodeType"))) {
        //     commandToUse = Command.SIMPLEAUTHENTICATED_GET_COMMAND;
        // }
        final String command = String.format(commandToUse, nodeName);
        LOGGER.debug("{} - Get Ldap Authentication Status: \n\tNetwork Elemet ID --> {}\n\tCommand to execute --> {}",
                methodName, nodeName, command);
        final EnmCliResponse enmCliResponse = restImpl.get().sendCommand(command);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("{} - Get Command response {}", methodName, enmCliResponse);
        }
        String authenticanFdn = "";
        if (enmCliResponse.isCommandSuccessful()) {
            final Map<String, Map<String, String>> mapFdnValue = enmCliResponse.getAttributesPerFdn();
            if (!mapFdnValue.isEmpty() && (mapFdnValue.values().size() != 0)) {
                final Map<String, String> map = (Map<String, String>) mapFdnValue.values().toArray()[0];
                LOGGER.trace("{} - Mapping response result:\n\tAttribute 'getAttributesPerFdn()' --> {}\n\tAttribute Value --> {}"
                        + "\n\tMapping Value --> {}", methodName, mapFdnValue, mapFdnValue.values(), map);
                if (map != null) {
                    authenticanFdn = map.get("FDN");
                }
            }
        }
        final Map<String, Object> data = Maps.newHashMap(node.getAllFields());
        data.put(Param.AUTHENTICATIONFDN, authenticanFdn);
        final DataRecord newNode = new DataRecordImpl(data);
        LOGGER.trace("{} - Added Parameter '{}' [{}] to dataRecord: --> {}",
                methodName, Param.AUTHENTICATIONFDN, authenticanFdn, newNode);
        return newNode;
    }

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep.StepIds#LDAP_SET_ADMINISTRATIVESTATE}
     * <b>Method Name</b>: ldapSetAdministrativeState <i>[public]</i>
     * <b>Description</b>: This <i>TestStep</i> is used to configure, through the <u>ENM CLI</u>, the
     * 'administrative status' of LDAP based on the value indicated by the
     * 'value' parameter.
     * The Test Step throws an exception if the configuration fails,
     * </pre>
     *
     * @param node DataRecord containing node information ({@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES}).
     * @param value Value to set ({@link com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep.Param#ADMSTATE_LOCKED}/
     * {@link com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep.Param#ADMSTATE_UNLOCKED})
     */
    @TestStep(id = StepIds.LDAP_SET_ADMINISTRATIVESTATE)
    public void ldapSetAdministrativeState(@Input(ADDED_NODES) final DataRecord node, @Input(Param.VALUE) final String value) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String ldapFDN = node.getFieldValue(Param.AUTHENTICATIONFDN);
        LOGGER.debug("{} - Executing command:\n\tLDAP fdn -->  {}\n\tParam value {}", methodName, ldapFDN, value);
        // TODO - PCC/PCG nodes: find suitable command to enable LDAP capability
        final String command = String.format(Command.AUTHENTICATION_SET__COMMAND, ldapFDN, value);
        final EnmCliResponse enmCliResponse = restImpl.get().sendCommand(command);
        if (LOGGER.isTraceEnabled()) {
            final SummaryDto commandResult = enmCliResponse.getSummaryDto();
            final Boolean isNotNull = commandResult != null;
            LOGGER.trace("{} - Command Executed with result:\n\tCommand -> {}\n\tResult -> {}\n\tError Code -> {}\n\tError Message -> {}"
                            + "\n\tStatus Message -> {}",
                    methodName, command, enmCliResponse.isCommandSuccessful(),
                    isNotNull ? commandResult.getErrorCode() : NULL_STRING, isNotNull ? commandResult.getErrorMessage() : NULL_STRING,
                    isNotNull ? commandResult.getStatusMessage() : NULL_STRING);
        }
        Assertions.assertThat(enmCliResponse.isCommandSuccessful())
                .as(String.format("Set Authentication failure (Step ID -> %s) on node: %s", StepIds.LDAP_SET_ADMINISTRATIVESTATE,
                        node.getFieldValue(NETWORKELEMENTID))).isTrue();
    }

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep.StepIds#LDAP_SET_PROFILEFILTER}
     * <b>Method Name</b>: ldapSetprofileFilter <i>[public]</i>
     * <b>Description</b>: This <i>TestStep</i> is used to configure, through the <u>ENM CLI</u>, the
     * 'profileFilter' of LDAP based on the value indicated by the
     * 'value' parameter.
     * The Test Step throws an exception if the configuration fails,
     * </pre>
     *
     * @param node DataRecord containing node information ({@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES}).
     * @param filterValue (optional value) String with filter name (default one, if omitted, is 'ERICSSON_FILTER',
     */
    @TestStep(id = StepIds.LDAP_SET_PROFILEFILTER)
    public void ldapSetprofileFilter(@Input(ADDED_NODES) final DataRecord node,
            @Input(Param.PROFILE_FILTER) @OptionalValue final String filterValue) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String fdnAuthentication = node.getFieldValue(Param.AUTHENTICATIONFDN);
        final String fdnLdap = node.getFieldValue(Param.LDAP_FDN);
        LOGGER.debug("{} - Setting Profile filter:\n\tNode Authentication Field --> {}\n\tLdap FDN --> {}", methodName, fdnAuthentication, fdnLdap);
        if (!fdnAuthentication.isEmpty() && !fdnLdap.isEmpty()) {
            final String profileFilter = filterValue == null ? Param.DEFAULT_FILTER_VALUE : filterValue;
            final String command = String.format(Command.LDAP_SET__COMMAND, fdnLdap, profileFilter);
            LOGGER.trace("{} - Executing Command: {}", methodName, command);
            final EnmCliResponse enmCliResponse = restImpl.get().sendCommand(command);
            if (LOGGER.isTraceEnabled()) {
                final SummaryDto commandResult = enmCliResponse.getSummaryDto();
                int errorCode = -1;
                String errorMessage = NULL_STRING;
                String statusMessage = NULL_STRING;
                if (commandResult != null) {
                    errorCode = commandResult.getErrorCode();
                    errorMessage = commandResult.getErrorMessage();
                    statusMessage = commandResult.getStatusMessage();
                }
                LOGGER.trace("{} - Command Executed with result:\n\tCommand -> {}\n\tResult -> {}\n\tError Code -> {}\n\tError Message -> {}"
                        + "\n\tStatus Message -> {}",
                        methodName, command, enmCliResponse.isCommandSuccessful(), errorCode, errorMessage, statusMessage);
            }
            Assertions.assertThat(enmCliResponse.isCommandSuccessful())
                    .as(String.format("Set Ldap failure (Step ID -> %s) on node: %s", StepIds.LDAP_SET_PROFILEFILTER,
                            node.getFieldValue(NETWORKELEMENTID))).isTrue();
        } else {
            LOGGER.debug("ldapSetprofileFilter Ldap FDN is empty");
        }
    }

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep.StepIds#LDAP_BASE}
     * <b>Method Name</b>: ldapsBase <i>[public]</i>
     * <b>Description</b>: This <i>TestStep</i> is used to configure the 'ldap' function for
     * the selected node via the <u>ENM CLI</u> and with a <u>XML configuration file</u>
     * (prepared dynamically).
     * The Test Step returns a DataRecord with all the information on the JOB of execution of
     * the configuration command.
     * </pre>
     *
     * @param node DataRecord containing node information ({@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES}).
     * @param value DataRecord with configuration parameters.
     * @return - JOB configuration DataRecord.
     */
    @TestStep(id = StepIds.LDAP_BASE)
    public DataRecord ldapsBase(@Input(ADDED_NODES) final DataRecord node,
            @Input(LdapMngTestStep.DataSource.LDAP_DATASOURCE) final DataRecord value) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final DataRecord newNode = TestStepUtil.mergeMap(node, value);
        final String xmlOutputPattern = "\n\t\t|";
        final String nodeName = newNode.getFieldValue(NETWORK_ELEMENT_ID);
        final String fdnAuthentication = node.getFieldValue(Param.AUTHENTICATIONFDN);
        final String transportType = node.getFieldValue(Param.TRANSPORTTYPE);
        final String transportTypeValue = (transportType != null && "TLS".equals(transportType)) ? "STARTTLS" : "LDAPS";
        final String fdnLdap = node.getFieldValue(Param.LDAP_FDN);
        final Boolean ldapJobResultStatus = (Boolean) node.getFieldValue(LdapMngTestStep.Param.LDAP_JOBSRESULT);
        LOGGER.debug("{} - Executing LDAP base Configuration:\n\tNetwork Element ID --> {} ({})\n\tTransport Type --> {}\n\t"
                        + "FDN Authentication --> {}\n\tFDN LDAP info --> {}\n\tLDAP Job Result --> {}",
                methodName, nodeName, node.getFieldValue(NETWORK_ELEMENT_ID), transportType,
                fdnAuthentication == null ? "---" : fdnAuthentication, fdnLdap == null ? "---" : fdnLdap,
                ldapJobResultStatus == null ? "---" : ldapJobResultStatus ? "DONE" : "FAIL");

        if (fdnAuthentication != null && !fdnAuthentication.isEmpty() && fdnLdap != null && !fdnLdap.isEmpty()) {
            final String filename = newNode.getFieldValue(FILE_NAME);
            String xmlContent = SecurityUtil.readResourceFile(filename);
            String xmlContentLog = xmlContent.replaceAll(NEWLINE, xmlOutputPattern);
            LOGGER.trace("{} - Preparing XML file for LDAP configuration:\n\tNode Name --> {}\n\tFile Name --> {}\n\tFile Content -->\n\t\t|{}",
                    methodName, nodeName, filename, xmlContentLog);
            xmlContent = String.format(xmlContent, nodeName, transportTypeValue).replace("\r\n", "\n");
            xmlContentLog = xmlContent.replaceAll(NEWLINE, xmlOutputPattern);
            LOGGER.trace("{} - Modified XML file:\n\t|{}", methodName, xmlContentLog);
            final byte[] fileContents = xmlContent.getBytes();
            final String targetFile = String.valueOf(nodeName) + String.valueOf(filename);
            final String commandString = ldapFactory.ldapConfigure(newNode);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("{} - Execute command with file:\n\tCommand --> {},\n\tTarget File -->{}\n\tFile Content -->\n\t\t|{}\n", methodName,
                        commandString, targetFile, Arrays.toString(fileContents).replaceAll(NEWLINE, xmlOutputPattern));
            }
            final EnmCliResponse response = restImpl.get().sendCommandWithFile(commandString, targetFile, fileContents);
            LOGGER.trace("{} - Command Result:\n\tCommand success --> {}\n\tCommand Message --> {}\n\tRAW response --> {}",
                    methodName, response.isCommandSuccessful(), response.getSummaryDto().getStatusMessage(), response);
            SecurityUtil.checkResponseDto(response, (String) newNode.getFieldValue(EXPECTED_MESSAGE));
            return TestStepUtil.mergeMap(newNode, JobIdUtils.fillJobIdDataRecord(response, (String) newNode.getFieldValue(NETWORK_ELEMENT_ID),
                    (String) newNode.getFieldValue(EXPECTED_MESSAGE)));
        } else {
            LOGGER.debug("{} - FDN authentication and FDN ldap are empty", methodName);
            return newNode;
        }
    }

    // ************************************************************************
    // * Inner Classes to defin TestId, Parameters and local Commands.
    // ************************************************************************

    /**
     * <pre>
     * <b>Class Name</b>: StepIds
     * <b>Description</b>: This subclass contains the test steps identifiers
     *   contained in this class.
     * </pre>
     */
    public static final class StepIds {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LDAP_GET_ADMINISTRATIVESTATE = "ldapGetAdministrativeState";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LDAP_GET = "ldapGet";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LDAP_SET_ADMINISTRATIVESTATE = "ldapSetAdministrativeState";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LDAP_SET_PROFILEFILTER = "ldapSetProfileFilter";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LDAP_BASE = "ldapBase";

        private StepIds() {
        }
    }

    /**
     * <pre>
     * <b>Class Name</b>: DataSource
     * <b>Description</b>: DataSource Names for LDAP.
     * </pre>
     */
    public static final class DataSource {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LDAP_DATASOURCE = "LdapDataSource";

        private DataSource() {
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
        public static final String AUTHENTICATIONFDN = "authenticatonFdn";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LDAP_JOBSRESULT = "ldspJobResult";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LDAP_MANAGED = "ldapManaged";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LDAP_FDN = "ldapFdn";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String TRANSPORTTYPE = "transportType";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String VALUE = "value";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String PROFILE_FILTER = "profileFilterValue";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String ADMSTATE_LOCKED = "LOCKED";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String ADMSTATE_UNLOCKED = "UNLOCKED";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String DEFAULT_FILTER_VALUE = "ERICSSON_FILTER";

        private Param() {
        }
    }

    /**
     * <pre>
     * <b>Class Name</b>: Command
     * <b>Description</b>: This subclass contains commands to execute with <i>LDAP</i>
     * command executor.
     * </pre>
     */
    public static final class Command {
        static final String AUTHENTICATION_GET_COMMAND = "cmedit get %s LdapAuthenticationMethod";
        static final String SIMPLEAUTHENTICATED_GET_COMMAND = "cmedit get %s simple-authenticated";
        static final String LDAP_GET_COMMAND = "cmedit get %s Ldap";
        static final String AUTHENTICATION_SET__COMMAND = "cmedit set %s administrativeState=%s";
        static final String LDAP_SET__COMMAND = "cmedit set %s profileFilter=%s";

        private Command() {}
    }
}
