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

import static com.ericsson.oss.services.nodecli.operators.teststeps.CredentialMngTestSteps.Command.CMEDIT_GET;
import static com.ericsson.oss.services.nodecli.operators.utility.UserCredentialType.NODECLI_USER;
import static com.ericsson.oss.services.nodecli.operators.utility.UserCredentialType.NORMAL_USER;
import static com.ericsson.oss.services.nodecli.operators.utility.UserCredentialType.ROOT_USER;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.nodesecurity.constant.AgnosticConstants.EXPECTED_MESSAGE;
import static com.ericsson.oss.testware.scenario.ScenarioUtilities.getPasswordValue;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.oss.services.nodecli.operators.utility.UserCredentialType;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.nodesecurity.operators.RestImpl;
import com.ericsson.oss.testware.nodesecurity.utils.SecurityUtil;
import com.google.common.collect.Maps;

/**
 * <pre>
 * <b>Name</b>: CredentialMngTestSteps      <i>[public (Class)]</i>
 * <b>Description</b>: This class contain Test Step to manage Credential Management
 * function.
 * </pre>
 */
public class CredentialMngTestSteps extends BaseTestStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialMngTestSteps.class);

    private static final Integer CRED_ITERATION = DataHandler.getConfiguration().getProperty("credentials.issue.iteration", 10, Integer.class);

    private static final Integer CRED_TIMER = DataHandler.getConfiguration().getProperty("credentials.issue.timer", 2000, Integer.class);

    private static final String SUCCESS_UPDATE_CREDENTIAL_MESSAGE = "\n\n Credentials %s successfully updated - node %s\n\n";
    private static final String FAIL_UPDATE_CREDENTIAL_MESSAGE = "\n\n Credentials %s update FAILED - node %s\n%s\n\n";
    @Inject
    private Provider<RestImpl> provider;

    /**
     * <pre>
     * <b>Name</b>: credentialsGet            <i>[public]</i>
     * <b>Description</b>: This Test Step is used to retrieve the credentials of the
     * node using the CLI command 'secadm'.
     * </pre>
     *
     * @param node contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @return - DataRecord with credential Values
     */
    @TestStep(id = StepIds.CRED_GET)
    public DataRecord credentialsGet(@Input(ADDED_NODES) final DataRecord node) {
        SecurityUtil.checkDataSource(node, ADDED_NODES);

        return getGenericCredential(node, false, "user name", "user password");
    }

    /**
     * <pre>
     * <b>Name</b>: credentialsGet            <i>[public]</i>
     * <b>Description</b>: This Test Step is used to retrieve the credentials of the
     * node using the CLI command 'secadm'.
     * </pre>
     *
     * @param node contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @return - DataRecord with credential Values
     */
    @TestStep(id = StepIds.NODE_CRED_GET)
    public DataRecord nodeCredentialsGet(@Input(ADDED_NODES) final DataRecord node) {
        SecurityUtil.checkDataSource(node, ADDED_NODES);
        return getGenericCredential(node, true, "applicationusername", "applicationuserpassword");
    }

    /**
     * <pre>
     * <b>Name</b>: credentialsGet            <i>[public]</i>
     * <b>Description</b>: This Test Step is used to retrieve the credentials of the
     * node using the CLI command 'secadm'. Get NetworkElementSecurity and check
     * credentials are updated.
     * </pre>
     *
     * @param node contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @param userType Enumerated {@link UserCredentialType}indicating the type of user to be configured.
     */
    @TestStep(id = StepIds.CRED_UPDATE)
    public void credentialsUpdate(@Input(ADDED_NODES) final DataRecord node, @Input(Param.USER_TYPE) final UserCredentialType userType) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        SecurityUtil.checkDataSource(node, ADDED_NODES);
        final String networkElementId = node.getFieldValue(NETWORKELEMENTID);
        final String networkElementType = node.getFieldValue(NETWORKELEMENTTYPE);
        LOGGER.trace("{} - Credential Update Parameters:\n\tNetwork Element ID -> {}[{}]\n\tUser Type --> {}\n\tUser Name --> {}({})",
                methodName, networkElementId, networkElementType, userType.name(), node.getFieldValue(userType.getUserNameField()),
                getPasswordValue(userType.getUserPasswordField()));

        // Get fields from DataRecord to be use for Credential Update
        final String expectedMessage = (String) node.getFieldValue(EXPECTED_MESSAGE);
        EnmCliResponse responseDtoAfter = null;

        // Get previous Security info for Node
        final EnmCliResponse responseDtoBefore = commandGetSecurityInfo(node);
        SecurityUtil.checkResponseDto(commandCredentialsUpdate(node), expectedMessage);
        int iteration = CRED_ITERATION;
        boolean responseOk = expectedMessage != null;

        // Wait until Get Seurity info changes
        while (!responseOk && (iteration >= 0)) {
            SecurityUtil.delay(CRED_TIMER);
            responseDtoAfter = commandGetSecurityInfo(node);
            responseOk = !responseDtoBefore.equals(responseDtoAfter);
            iteration--;
        }

        final String responseDtoString = (responseDtoAfter != null) ? responseDtoAfter.toString() : responseDtoBefore.toString();

        // Check response
        Assertions.assertThat(responseOk)
                .as(String.format(FAIL_UPDATE_CREDENTIAL_MESSAGE, "", networkElementId, responseDtoString))
                .isTrue();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format(SUCCESS_UPDATE_CREDENTIAL_MESSAGE, "", networkElementId));
        }
    }

    /**
     * <pre>
     * <b>Name</b>: credentialsUpdateLdap            <i>[public]</i>
     * <b>Description</b>: This Test Step is used to <i>enale</i>/<i>disable</i> LDAP function
     * on selected node.
     * </pre>
     *
     * @param node contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @param ldapStatus status to set for LDAP update oòperation (enable/disable)
     */
    @TestStep(id = StepIds.CRED_UPDATE_LDAP)
    public void credentialsUpdateLdap(@Input(ADDED_NODES) final DataRecord node, @Input(Param.LDAP) final String ldapStatus) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        SecurityUtil.checkDataSource(node, ADDED_NODES);
        final String networkElementId = node.getFieldValue(NETWORKELEMENTID);
        final String networkElementType = node.getFieldValue(NETWORKELEMENTTYPE);
        LOGGER.trace("{} - Credential Update Parameters:\n\tNetwork Element ID -> {}[{}]\n\tLDAP status --> {}",
                methodName, networkElementId, networkElementType, ldapStatus);

        final EnmCliResponse responseDto = commandCredentialsUpdateLdap(node, ldapStatus);
        final boolean responseOk = responseDto != null ? responseDto.isCommandSuccessful() : false;

        // Check response
        Assertions.assertThat(responseOk).as(String.format(FAIL_UPDATE_CREDENTIAL_MESSAGE, "", networkElementId,
                responseDto != null ? responseDto.toString() : "null"))
                .isTrue();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format(SUCCESS_UPDATE_CREDENTIAL_MESSAGE, "", networkElementId));
        }
    }

    /**
     * <pre>
     * <b>Name</b>: deleteNodeCliInfo            <i>[public]</i>
     * <b>Description</b>: This Test Step is used to <u>NodeCli</u> info on selected node.
     * </pre>
     *
     * @param node contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     */
    @TestStep(id = StepIds.DELETE_NODECLI_USER)
    public void deleteNodeCliInfo(@Input(ADDED_NODES) final DataRecord node) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        SecurityUtil.checkDataSource(node, ADDED_NODES);
        final String networkElementId = node.getFieldValue(NETWORKELEMENTID);
        final String networkElementType = node.getFieldValue(NETWORKELEMENTTYPE);
        //DELETE
        LOGGER.trace("{} - Credential Delete Parameters:\n\tNetwork Element ID -> {}[{}]}",
                methodName, networkElementId, networkElementType);
        boolean responseOk = commandCredentialsDelete(networkElementId).isCommandSuccessful();
        String logInfo = String.format(responseOk ? "%n Delete Security Function - node %s\n"
                : "%nDelete Security Functio Failed on %s", networkElementId);
        Assertions.assertThat(responseOk).as(logInfo).isTrue();
        LOGGER.info(logInfo);

        //CREATE
        LOGGER.trace("{} - Credential Create Parameters:\n\tNetwork Element ID -> {}[{}]\n\tUser Name --> {} [{}]}",
                methodName, networkElementId, networkElementType, UserCredentialType.SECURE_USER.name(),
                UserCredentialType.SECURE_USER.getUserNameField());
        responseOk = commandCredentialsCreate(node).isCommandSuccessful();
        logInfo = String.format(responseOk ? "\n Create Security Function - node %s\n"
                : "\nCreate Security Functio Failed on %s", networkElementId);
        Assertions.assertThat(responseOk).as(logInfo).isTrue();
        LOGGER.info(logInfo);
    }

    // ************************************************************************
    // * Additional Methods to execute functions
    // ************************************************************************

    /**
     * <pre>
     * <b>Name</b>: commandCredentialsGet            <i>[public]</i>
     * <b>Description</b>: This method execute command to Get Credential on
     * a specified Node.
     * </pre>
     *
     * @param nodeName Nietwork element Id for node
     * @return Get command Result (EnmCliResponse).
     */
    public EnmCliResponse commandCredentialsGet(final String nodeName) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final RestImpl restImpl = provider.get();
        final String command = String.format(Command.SECADM_GET, nodeName);
        LOGGER.trace("{} - Sending REST command (1):\n\t --> <{}>", methodName, command);
        return restImpl.sendCommand(command);
    }

    /**
     * <pre>
     * <b>Name</b>: commandCredentialsDelete            <i>[public]</i>
     * <b>Description</b>: This method execute command to Delete Credential on
     * a specified Node.
     * </pre>
     *
     * @param nodeName Nietwork element Id for node
     * @return Get command Result (EnmCliResponse).
     */
    public EnmCliResponse commandCredentialsDelete(final String nodeName) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final RestImpl restImpl = provider.get();
        final String command = String.format(Command.CMEDIT_DELETE_SECURITY_FUNCTION, nodeName);
        LOGGER.trace("{} - Sending REST command (2):\n\t --> <{}>", methodName, command);
        return restImpl.sendCommand(command);
    }

    /**
     * <pre>
     * <b>Name</b>: commandCredentialsCreate            <i>[public]</i>
     * <b>Description</b>: This method execute command to Create Credential on
     * a specified Node.
     * </pre>
     *
     * @param node contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @return Get command Result (EnmCliResponse).
     */
    public EnmCliResponse commandCredentialsCreate(final DataRecord node) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final RestImpl restImpl = provider.get();
        final String command = prepareSecurityCommand(Command.SECADM_CREATE_SECURITY, false, node, null);
        LOGGER.trace("{} - Sending REST command (3):\n\t --> <{}>", methodName, command);
        return restImpl.sendCommand(command);
    }

    /**
     * <pre>
     * <b>Name</b>: commandCredentialsUpdate            <i>[public]</i>
     * <b>Description</b>: This method execute command to Update Credential value for
     * Selected Node.
     * </pre>
     *
     * @param node contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @return - EnmCliResponse command result (EnmCliResponse).
     */
    public EnmCliResponse commandCredentialsUpdate(final DataRecord node) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final RestImpl restImpl = provider.get();
        final DataRecord configuredData = getGenericCredential(node, false, "user name", "user password");
        final String command = prepareSecurityCommand(Command.SECADM_UPDATE_GENERIC, false, node, configuredData);
        LOGGER.trace("{} - Sending REST command (4):\n\t --> <{}>", methodName, command);
        return restImpl.sendCommand(command);
    }

    /**
     * Execute credentials ldap update.
     *
     * @param node contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @param ldapStatus String
     * @return EnmCliResponse command result
     */
    public EnmCliResponse commandCredentialsUpdateLdap(final DataRecord node, final String ldapStatus) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final RestImpl restImpl = provider.get();
        final DataRecord configuredData = getGenericCredential(node, false, "user name", "user password");
        final String command = prepareSecurityCommand(Command.SECADM_UPDATE_GENERIC,
                CredentialMngTestSteps.Param.ENABLE.equalsIgnoreCase(ldapStatus) ? true : false, node, configuredData);
        LOGGER.trace("{} - Sending REST command (4b):\n\t --> <{}>", methodName, command);
        return restImpl.sendCommand(command);
    }

    /**
     * <pre>
     * <b>Name</b>: commandGetSecurityInfo            <i>[private]</i>
     * <b>Description</b>: This method execute command to Get Credential info for
     * Selected Node.
     * </pre>
     *
     * @param value Datarecord to use for getting Security Info
     * @return Security Info Response (EnmCliResponse).
     */
    private EnmCliResponse commandGetSecurityInfo(final DataRecord value) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final RestImpl restImpl = provider.get();
        final String nodeName = value.getFieldValue(NETWORKELEMENTID);
        final String command = String.format(CMEDIT_GET, nodeName);
        LOGGER.trace("{} - Sending REST command (5):\n\t --> <{}>", methodName, command);
        return restImpl.sendCommand(command);
    }

    /**
     * <pre>
     * <b>Name</b>: prepareSecurityCommand            <i>[private]</i>
     * <b>Description</b>: This method should be use to create an appropriate command
     *   with correct number of parameters depending on Command and DataSource.
     * </pre>
     *
     * @param genericCommandToPrepare generic Command to Use
     * @param ldapEnabled flag to enable/disable LDAP
     * @param nodeData Node Datarecord element
     * @param configuredNodeData Node Datarecord element from ENM configuration
     * @return - prepared SECADM command.
     */
    private String prepareSecurityCommand(final String genericCommandToPrepare, final boolean ldapEnabled,
            final DataRecord nodeData, final DataRecord configuredNodeData) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

        // Check if exist root User: if exist check if it's different from previous one
        String rootUserData = "";
        String rootUsername = getSelectedField(ROOT_USER.getUserNameField(), nodeData, configuredNodeData);
        String rootUserPassword = getSelectedField(ROOT_USER.getUserPasswordField(), nodeData, configuredNodeData);
        if (!rootUsername.isEmpty()) {
            rootUserData = String.format(Command.SECADM_ROOTUSER,rootUsername, rootUserPassword);
            LOGGER.trace("{} - Setting of 'Root User' data\n\t --> {}", methodName, rootUserData);
        }

        // Check if exist secure User: if exist check if it's different from previous one
        String secureUserData = "";
        String secureUsername = getSelectedField(UserCredentialType.SECURE_USER.getUserNameField(), nodeData, configuredNodeData);
        String secureUserPassword = getSelectedField(UserCredentialType.SECURE_USER.getUserPasswordField(), nodeData, configuredNodeData);
        if (!secureUsername.isEmpty()) {
            secureUserData = String.format(Command.SECADM_SECUREUSER,secureUsername, secureUserPassword);
            LOGGER.trace("{} - Setting of 'Secure User' data\n\t --> {}", methodName, secureUserData);
        }

        // Check if exist normal User: if exist check if it's different from previous one
        String normalUserData = "";
        String normalUsername = getSelectedField(NORMAL_USER.getUserNameField(), nodeData, configuredNodeData);
        String normalUserPassword = getSelectedField(NORMAL_USER.getUserPasswordField(), nodeData, configuredNodeData);
        if (!normalUsername.isEmpty()) {
            normalUserData = String.format(Command.SECADM_NORMALUSER,normalUsername, normalUserPassword);
            LOGGER.trace("{} - Setting of 'Normal User' data\n\t --> {}", methodName, normalUserData);
        }

        // Set LDAP configuration
        final String ldapConfData = String.format(Command.SECADM_LDAPUSER,
                ldapEnabled ? CredentialMngTestSteps.Param.ENABLE : CredentialMngTestSteps.Param.DISABLE);
        LOGGER.trace("{} - Setting 'LDAP conf' Data\n\t --> {}", methodName, ldapConfData);

        // Check if exist normal User: if exist check if it's different from previous one
        String nodecliUserData = "";
        String nodeCliUsername = getSelectedField(NODECLI_USER.getUserNameField(), nodeData, configuredNodeData);
        String nodeCliUserPassword = getSelectedField(NODECLI_USER.getUserPasswordField(), nodeData, configuredNodeData);
        if (!nodeCliUsername.isEmpty()) {
            nodecliUserData = String.format(Command.SECADM_NODECLIUSER,nodeCliUsername, nodeCliUserPassword);
            LOGGER.trace("{} - Setting of 'Normal User' data\n\t --> {}", methodName, nodecliUserData);
        }

        return String.format(genericCommandToPrepare,
                rootUserData, secureUserData, nodecliUserData, normalUserData, ldapConfData, nodeData.getFieldValue(NETWORKELEMENTID));
    }

    final String getSelectedField(final String fieldName, final DataRecord datatoSet, final DataRecord dataToCompare) {
        if(datatoSet.getFieldValue(fieldName) == null || datatoSet.getFieldValue(fieldName).toString().isEmpty()
                || "Not Configured".equalsIgnoreCase(datatoSet.getFieldValue(fieldName).toString())) {
            return "";
        }
        final String dataValue1 = datatoSet.getFieldValue(fieldName);

        if(dataToCompare == null || dataToCompare.getFieldValue(fieldName) == null
                || dataToCompare.getFieldValue(fieldName).toString().isEmpty()) {
            return dataValue1;
        }
        final String dataValue2 = dataToCompare.getFieldValue(fieldName);
        if(!dataValue1.equals(dataValue2)) {
            return dataValue1;
        }

        return "";
    }

    private DataRecord getGenericCredential(final DataRecord node, final boolean getFromCmEdit, final String fieldNameToGet,
            final String fieldPasswordToGet) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

        // Execute selected command to get Security Infos and Check response
        final String networkElementId = node.getFieldValue(NETWORKELEMENTID);
        final String networkElementType = node.getFieldValue(NETWORKELEMENTTYPE);
        final EnmCliResponse response = getFromCmEdit ? commandGetSecurityInfo(node) : commandCredentialsGet(networkElementId);
        final String commandType = getFromCmEdit ? "cmedit" : "secadm";
        LOGGER.debug("{} - Get Credential info for node {} [{}] ({} command):\n\tCommand Response Status --> {}\n\t{}",
                methodName, networkElementId, networkElementType, commandType, response.isCommandSuccessful(), response);
        Assertions.assertThat(response.isCommandSuccessful())
                .as(String.format("Credentials get failure: %s [%s]", networkElementId, networkElementType))
                .isTrue();

        // Prepare Data to set info in DataRecord
        final Map<String, Object> data = Maps.newHashMap(node.getAllFields());
        final List<Map<String, String>> retValue = response.getAllAtributesPerObjectSingleTableView();
        final StringBuilder loggerMessage = new StringBuilder();
        LOGGER.trace("{} - Get Credential infos for node {} [{}] Using '{}' --> {} users (end with -> '{}'/'{}')",
                methodName, networkElementId, networkElementType, commandType,
                retValue.size(), fieldNameToGet, fieldPasswordToGet);
        loggerMessage.append(String.format("%s - List of configured User/Password for node %s[%s]",
                methodName, networkElementId, networkElementType));
        int elementCount = 0;
        for (final Map<String, String> mapElement : retValue) {
            for (final Map.Entry<String, String> mapEntry : mapElement.entrySet()) {
                if (!mapEntry.getKey().toLowerCase().endsWith(fieldNameToGet) && !mapEntry.getKey().toLowerCase().endsWith(fieldPasswordToGet)) {
                    continue;
                }

                final String dataRecordKey;
                final String dataRecordValue;
                if (mapElement.get(mapEntry.getKey()).split(":").length >= 2) {
                    dataRecordKey = mapElement.get(mapEntry.getKey()).split(":")[0];
                    dataRecordValue = mapElement.get(mapEntry.getKey()).split(":")[1];
                } else {
                    dataRecordKey = mapEntry.getKey();
                    dataRecordValue = mapElement.get(mapEntry.getKey());
                }
                final String dataRecordToShow = mapEntry.getKey().toLowerCase().endsWith("password")
                        ? getPasswordValue(dataRecordValue) : dataRecordValue;
                loggerMessage.append(System.lineSeparator()).append("\t")
                        .append(String.format("Key Name -> %s, key value --> %s", dataRecordKey, dataRecordToShow));

                elementCount++;
                data.put(dataRecordKey, dataRecordValue);
            }
        }
        final DataRecord newNode = new DataRecordImpl(data);
        LOGGER.trace(loggerMessage.toString());
        LOGGER.debug("{} - Added User Parameters to dataRecord ({} elements):\n\t--> {}", methodName, elementCount, newNode);
        return newNode;
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
        public static final String CRED_UPDATE = "credentialsUpdate";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CRED_GET = "credentialsGet";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CRED_UPDATE_LDAP = "credentialsUpdateLdap";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String NODE_CRED_GET = "nodeCredentialGet";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String DELETE_NODECLI_USER = "DeleteNodeCliUser";

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
        public static final String USER_TYPE = "userTypeParam";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String USER = "userParam";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String PASSWD = "passwdParam";

        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LDAP = "ldap";

        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String USERNAME = "User Name";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String USERPASSW = "User Password";

        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String ENABLE = "enable";

        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String DISABLE = "disable";

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
        static final String SECADM_GET = "secadm credentials get --plaintext show --nodelist %s";
        static final String CMEDIT_GET = "cmedit get NetworkElement=%s,SecurityFunction=1,NetworkElementSecurity=1 --table";
        static final String CMEDIT_DELETE_SECURITY_FUNCTION = "cmedit delete NetworkElement=%s,SecurityFunction=1,NetworkElementSecurity=1";

        // Generic Update/Create Command Formatter.
        static final String SECADM_UPDATE_GENERIC = "secadm credentials update %s%s%s%s%s--nodelist %s";
        static final String SECADM_CREATE_SECURITY = "secadm credentials create %s%s%s%s%s--nodelist %s";
        static final String SECADM_SECUREUSER = "--secureusername %s --secureuserpassword %s ";
        static final String SECADM_ROOTUSER = "--rootusername %s --rootuserpassword %s ";
        static final String SECADM_NORMALUSER = "--normalusername %s --normaluserpassword %s ";
        static final String SECADM_NODECLIUSER = "--nodecliusername %s --nodecliuserpassword %s ";
        static final String SECADM_LDAPUSER = "--ldapuser %s ";

        private Command() {
        }
    }
}
