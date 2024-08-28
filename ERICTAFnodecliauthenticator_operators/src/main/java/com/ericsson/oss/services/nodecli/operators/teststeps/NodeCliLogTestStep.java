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

package com.ericsson.oss.services.nodecli.operators.teststeps;

import static java.lang.Math.min;

import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_ID;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.oss.services.nodecli.operators.utility.UserCredentialType;
import com.ericsson.oss.testware.enmbase.data.NetworkNode;

/**
 * <pre>
 * <b>Class Name</b>: NodeCliLogTestStep
 * <b>Description</b>: This class contains TestSteps to evaluate Content of NodeCli Log.
 * </pre>
 */
public class NodeCliLogTestStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeCliLogTestStep.class);
    private static final int NODECLI_LOG_TIME_INTERVAL_MINUTES = DataHandler.getConfiguration().getProperty("log.nodecli.timeinterval.minutes", 30,
            Integer.class);
    private static final String NODECLI_LOG_SUCCESS_PATTERN_ACCESS = DataHandler.getConfiguration().getProperty("log.nodecli.accessPattern.success",
            "Session opened successfully", String.class);
    private static final Pattern DATETIMEPATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}");
    private static final DateTimeFormatter DATETIMEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String DISABLE_PATTERN = "disable";
    private static final String ENABLE_PATTERN = "enable";

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link StepIds#CHECK_NODECLI_LOGGED_USER}
     * <b>Name</b>: checkNodeCliLoggedUser      <i>[public]</i>
     * <b>Description</b>: This Test Step is used to compare the <i>UserName</i> expected for
     *   the specific <u>TestCase</u> with the one used to access the NodeCli by reading
     *   the Log of the NodeCli hosts.
     * In case of test failure (the two usernames are different) then the
     *   TestStep provides for the 'print' (LOG) of the last lines of the nodecli
     *   host LOG (merged Mode).
     * </pre>
     *
     * @param testCaseId
     *         testCaseId Lable of selected Test Case: inside it there is status of LDAP/SSO.
     * @param node
     *         node DataRecord with selected Node.
     * @param user
     *         user DataRecord with User for selected node.
     * @param logFromHosts
     *         List of Strings with records from Logs.
     */
    @TestStep(id = StepIds.CHECK_NODECLI_LOGGED_USER)
    public void checkNodeCliLoggedUser(@Input(TEST_CASE_ID) final String testCaseId, @Input(ADDED_NODES) final NetworkNode node,
            @Input(AVAILABLE_USERS) final User user, @Input(Param.LOG_FROM_NODECLI_PARAM) final List<String> logFromHosts) {

        // TODO - Verificare se il LOG malformato dipende dal Log Collector oppure dalla testware
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info("{} - Checking NodeCli Logged User:\n\tTest Case --> {}\n\tNode Selected --> {} <{}> [{}]\n\tENM User --> {}",
                methodName, testCaseId, node.getNetworkElementId(), node.getIpAddress(), node.getNodeType(), user.getUsername());

        // Getting Expected UserName
        final String expectedUser = getExpectedNodeCliUser(testCaseId, user, node);
        LOGGER.debug("{} - Expected UserName for '{}' is --> {}", methodName, testCaseId, expectedUser);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("{} - Print List Content: \n{}\n{}\n{}\n", methodName, new String(new char[80]).replace('\0', '-'),
                    String.join("\n\n", logFromHosts), new String(new char[80]).replace('\0', '-'));
        }

        // First Record Should be Remote Date7Time Value, last record should Be LOGs: get test interval for LOG record extraction
        final String remoteDateTimeString = logFromHosts.get(0);
        final Matcher matcher = DATETIMEPATTERN.matcher(remoteDateTimeString);
        LocalDateTime remoteDateObject = null;
        LocalDateTime rewindDateObject = null;
        if (matcher.find()) {
            final String dateTimeValue = matcher.group();
            remoteDateObject = LocalDateTime.parse(dateTimeValue, DATETIMEFORMATTER);
            rewindDateObject = LocalDateTime.parse(dateTimeValue, DATETIMEFORMATTER).minusMinutes(NODECLI_LOG_TIME_INTERVAL_MINUTES);
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("{} - Logger filtering Information: \n\tRemote Date And Time --> {}\n\tLog interval Time --> {} minutes"
                            + "\n\tStart LOG at --> {}\n\tStop LOG at ---> {}\n\t- IP address --> '{}'\n\t- User name --> '{}'\n\t- Success Pattern"
                            + " --> '{}' ",
                    methodName, remoteDateTimeString, NODECLI_LOG_TIME_INTERVAL_MINUTES, DATETIMEFORMATTER.format(rewindDateObject),
                    DATETIMEFORMATTER.format(remoteDateObject), node.getIpAddress(), expectedUser, NODECLI_LOG_SUCCESS_PATTERN_ACCESS);
        }

        final List<String> originalLogList = Arrays.asList(logFromHosts.get(logFromHosts.size() - 1).split("\\R"));

        // Filter List with values: ipAddress, expectedUser and 'Session opened successfully'
        final Pattern dateTimePredicate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2},\\d{3}");
        final Predicate<String> nodeIpAddressPredicate = s -> s.contains(node.getIpAddress());
        final Predicate<String> expectedUserPredicate = s -> s.contains(expectedUser);
        final Predicate<String> successPredicate = s -> s.contains(NODECLI_LOG_SUCCESS_PATTERN_ACCESS);
        final List<String> validRecords = originalLogList.stream()
                .filter(dateTimePredicate.asPredicate().and(nodeIpAddressPredicate).and(expectedUserPredicate).and(successPredicate)).sorted()
                .filter(isAfterDateTime(rewindDateObject))
                .collect(Collectors.toList());

        LOGGER.trace("{} - Size of Log List: {} --> {} --> {}", methodName, logFromHosts.size(), originalLogList.size(),
                validRecords.size());

        if (validRecords.size() > 0) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("{} - Record(s) Found [{}/{}]\n\tLog Records from ---> <{}> to --> <{}>\n\tIpAddress --> {} ({} [{}])"
                                + "\n\tExpected User --> {}\n\t| {}", methodName, originalLogList.size(), validRecords.size(),
                        DATETIMEFORMATTER.format(rewindDateObject), DATETIMEFORMATTER.format(remoteDateObject), node.getIpAddress(),
                        node.getNetworkElementId(), node.getNodeType(), expectedUser, String.join("\n\t| ", validRecords));
            }
        } else {
            final String nodeIpAddress = node.getIpAddress();
            final String nodeName = node.getNetworkElementId();
            final String nodeType = node.getNodeType();
            final String logRecords = String.join("\n\t| ", originalLogList);
            LOGGER.error("{} -- Logged User ({}) Not Found in {} ({} [{}]) connection\n  Log Records:\n\t| {}",
                    methodName, expectedUser, nodeIpAddress, nodeName, nodeType, logRecords);
        }
        // No Record found with Expected Values
        Assertions.assertThat(validRecords.size())
                .as(String.format("No LOG record found with User --> %s, IP address --> %s and  Result --> %s, after this Date/Time %s",
                        expectedUser, node.getIpAddress(), "Session opened successfully", DATETIMEFORMATTER.format(rewindDateObject)))
                .isGreaterThan(0);
    }

    // ***********************************************************************
    // * Protected Functions
    // ************************************************************************

    /**
     * <pre>
     * <b>Name</b>: getExpectedNodeCliUser            <i>[protected]</i>
     * <b>Description</b>: This function is used to determine which user is expected for
     *   the <i>CLI</i> connection to the node: this information is determined by the
     *   <i>TestCase identifier</i> which <u>must</u> appear in the following form:
     *               LDAP<<i>enable</i>/<i>disable</i>>SSO<<i>enable</i>/<i>disable</i>>.
     *   <b>N.B.</b> Node Datasource should be updated with ldapAppUserName,
     *        secureUserName and NodeCliUserName.
     * </pre>
     *
     * @param testCaseId
     *         Test Case identifier to select userName
     * @param user
     *         User Object to use.
     * @param node
     *         NetworkNode object to use.
     *
     * @return - Expected UserName
     */
    // TODO - implement Double Role Management (With Property) to implement Machine state for Ueer Selection
    protected static String getExpectedNodeCliUser(final String testCaseId, final User user, final NetworkNode node) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String normalizedTestId = normalizedTestCaseId(testCaseId);
        LOGGER.debug("{} - Getting Node Cli User from:\n\tTest Case Id --> {}\n\tNormalized TestCase --> '{}'",
                methodName, testCaseId, normalizedTestId);
        String userName = "";
        switch (normalizedTestId) {
            case "ldapdisablessodisable":                                  // This mean LDAP Disable SSO Disable
                LOGGER.trace("{} - Case 'ldapdisablessodisable'", methodName);
                // TODO - Implement State Machine for UserName Selection (USERSELECTION_OLDIMPLEMENTATION)
                userName = getNodeSecureUserName(node);
                break;
            case "ldapdisablessoenable":                                   // This mean LDAP Disable SSO Enable
                LOGGER.trace("{} - Case 'ldapdisablessoenable'", methodName);
                userName = user.getUsername();
                break;
            case "ldapenablessodisable":                                   // This mean LDAP Enable SSO Disable
                LOGGER.trace("{} - Case 'ldapenablessodisable'", methodName);
                userName = getLdapUserName(node);
                break;
            case "ldapenablessoenable":                                    // This mean LDAP Enable SSO Enable
                LOGGER.trace("{} - Case 'ldapenablessoenable'", methodName);
                userName = user.getUsername();
                break;
            default:
                break;
        }
        LOGGER.trace("{} - Expected UserName --> {}\n\t", methodName, userName);
        return userName;
    }

    // ************************************************************************
    // * Protected Functions
    // ************************************************************************

    private static String normalizedTestCaseId(final String testCaseId) {
        // Removing unnecessary parts of the TestId
        LOGGER.trace("[0] Normalized Test Case ID --> testCaseId: {}", testCaseId);
        String cleanTestId = testCaseId.toLowerCase().substring(testCaseId.toLowerCase().indexOf("ldap"));

        // get Last Index of Enable/disable in pattern
        final int lastDisable = cleanTestId.lastIndexOf(DISABLE_PATTERN);
        final int lastEnable = cleanTestId.lastIndexOf(ENABLE_PATTERN);
        final int indexToUse = lastDisable > lastEnable ? lastDisable + DISABLE_PATTERN.length() + 1 : lastEnable + ENABLE_PATTERN.length() + 1;
        LOGGER.trace("[1] Normalized Test Case ID --> index: {}, {}, {}, {}", lastDisable, lastEnable, indexToUse, cleanTestId.length());
        cleanTestId = (cleanTestId.substring(0, min(indexToUse, cleanTestId.length()))).replaceAll("\\s+", "");
        LOGGER.trace("[2] Normalized Test Case ID --> cleanTestId: {}", cleanTestId);

        // Removed Unnecessary characters
        LOGGER.trace(" Transformed TestId: '{}' --> '{}'", testCaseId, cleanTestId);
        return cleanTestId;
    }

    // TODO - Fix this piece of code to meet new requrement:
    //  https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/ENMRASecurity/RA+Security+Requirements+Details?src=breadcrumbs-parent
    private static String getNodeSecureUserName(final NetworkNode node) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("{} - Get Node User Names:\n\t{} --> {}\n\t{} --> {}\n\t{} --> {}\n\t{} --> {}\n\t{} --> {}\n\t{} --> {}",
                    methodName,
                    UserCredentialType.ENM_USER.name(), node.getFieldValue(UserCredentialType.ENM_USER.getUserNameField()),
                    UserCredentialType.ROOT_USER.name(), node.getFieldValue(UserCredentialType.ROOT_USER.getUserNameField()),
                    UserCredentialType.SECURE_USER.name(), node.getFieldValue(UserCredentialType.SECURE_USER.getUserNameField()),
                    UserCredentialType.NORMAL_USER.name(), node.getFieldValue(UserCredentialType.NORMAL_USER.getUserNameField()),
                    UserCredentialType.NODECLI_USER.name(), node.getFieldValue(UserCredentialType.NODECLI_USER.getUserNameField()),
                    UserCredentialType.LDAP_USER.name(), node.getFieldValue(UserCredentialType.LDAP_USER.getUserNameField()));
        }
        final String nodeCliUser = node.getFieldValue(UserCredentialType.NODECLI_USER.getUserNameField());
        if (nodeCliUser != null && !nodeCliUser.isEmpty() && !UserCredentialType.NODECLI_USER.getDefaultUser().equalsIgnoreCase(nodeCliUser)) {
            return nodeCliUser;
        }
        return node.getFieldValue(UserCredentialType.SECURE_USER.getUserNameField());
    }

    private static String getLdapUserName(final NetworkNode node) {
        return node.getFieldValue(UserCredentialType.LDAP_USER.getUserNameField());
    }

    private static Predicate<String> isAfterDateTime(final LocalDateTime fromDateTime) {
        return new Predicate<String>() {
            @Override
            public boolean test(final String logRecordItem) {
                LOGGER.trace("Check Instance: {} --> {} ", fromDateTime, logRecordItem);
                final Matcher matcher = DATETIMEPATTERN.matcher(logRecordItem);
                if (matcher.find()) {
                    final String dateTimeValue = matcher.group();
                    final LocalDateTime recordDateTimeValue = LocalDateTime.parse(dateTimeValue, DATETIMEFORMATTER);
                    return recordDateTimeValue.isAfter(fromDateTime);
                }
                return false;
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
    public static final class StepIds {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CHECK_NODECLI_LOGGED_USER = "Check User in NodeCli Log";

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
        public static final String LOG_FROM_NODECLI_PARAM = "LogFromNodecliHostData";

        private Param() {
        }
    }
}
