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

package com.ericsson.oss.services.nodecli.operators.teststeps;

import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_ID;
import static com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliUiTestStep.StepIds.ASSERT_LAUNCH_NODECLI_PRESENT;
import static com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliUiTestStep.StepIds.INIT_BROWSER_AND_LOGIN;
import static com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliUiTestStep.StepIds.LAUNCH_AND_VERIFY_CLI_PARALLEL;
import static com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliUiTestStep.StepIds.SEARCH_FOR_NODE_VUSERS;
import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.assertWithScreenShot;
import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.takeLocalScreenshot;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;

import javax.inject.Inject;
import javax.inject.Provider;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.ui.Browser;
import com.ericsson.cifwk.taf.ui.BrowserSetup;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.WaitTimedOutException;
import com.ericsson.oss.services.nodecli.operators.operators.NodeCliUiOperator;
import com.ericsson.oss.testware.enmbase.data.ENMUser;
import com.ericsson.oss.testware.enmbase.data.NetworkNode;
import com.ericsson.oss.testware.hostconfigurator.HostConfigurator;
import com.ericsson.oss.testware.networkexplorer.operators.NetworkExplorerOperator;
import com.ericsson.oss.testware.networkexplorer.utilities.BrowserDataHolder;
import com.ericsson.oss.testware.networkexplorer.viewmodels.regions.ResultsViewModel;
import com.ericsson.oss.testware.networkexplorer.viewmodels.uisdk.topsection.ActionBarViewModel;
import com.ericsson.oss.testware.security.authentication.operators.LoginLogoutUiOperator;
import com.ericsson.oss.testware.security.authentication.tool.TafToolProvider;

/**
 * <pre>
 * <b>Name</b>: NodeCliUiTestStep      <i>[public (Class)]</i>
 * <b>Description</b>: This subclass contains the test steps identifiers
 * contained in this class.
 * </pre>
 */
public class NodeCliUiTestStep extends BaseTestStep {
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String NODE_CLI_ACTION = "Launch Node CLI";
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String NODECLI_APPID = "/#nodecli";
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String NETWORKEXPLORERURL = "/#networkexplorer";
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String HTTPSURL = "https://";
    static final String CMEDIT_GET_NOTABLE = "cmedit get NetworkElement=%s,SecurityFunction=1,NetworkElementSecurity=1";
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeCliUiTestStep.class);
    @Inject
    private Provider<LoginLogoutUiOperator> loginLogoutUiOperator;
    @Inject
    private Provider<NetworkExplorerOperator> networkExplorerOperator;
    @Inject
    private Provider<NodeCliUiOperator> nodeCliUiOperator;
    @Inject
    private TafToolProvider tafToolProvider;
    @Inject
    private BrowserDataHolder browserDataHolder;

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliUiTestStep.StepIds#INIT_BROWSER_AND_LOGIN}
     * <b>Name</b>: initBrowserAndLogin      <i>[public]</i>
     * <b>Description</b>: This TestStep should be use to initialize Browser and login
     * to ENM with selected user..
     * </pre>
     *
     * @param user
     *         {@link com.ericsson.oss.testware.enmbase.data.ENMUser } represents a user specified in a row in input csv file
     */
    @TestStep(id = INIT_BROWSER_AND_LOGIN)
    public void initBrowserAndLogin(@Input(AVAILABLE_USERS) final ENMUser user) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info("{} - Init Browser and open ENM with User '{}')", methodName, user.getUsername());
        Browser browser = null;
        try {
            browser = loginLogoutUiOperator.get().login(tafToolProvider.getBrowser(), user.getUsername(), user.getPassword(), NETWORKEXPLORERURL);
            LOGGER.trace("{} - Browser opened with Login Operation");
        } catch (final Exception | Error  e) {
            LOGGER.error("{} - Exception caught");
            Assertions.assertThat(false).as(String.format("Exception/Error during Init of Browser (vUser ID --> %d, UserName --> %s):"
                    + "%nMessage -> %s%nStack Trace -->%n%s",
                    TafTestContext.getContext().getVUser(), user.getUsername(), e.getMessage(), e.getStackTrace())).isTrue();
        }
        LOGGER.trace("{} - No exception: check browser consistency");
        Assertions.assertThat(browser).as(String.format("Cannot Open Browser (vUser ID --> %d, UserName --> %s) [Browser not set --> 'null']",
                TafTestContext.getContext().getVUser(), user.getUsername())).isNotNull();

        // Configure Browser, compose expected URL and get Login URL
        LOGGER.trace("{} - Normal Operation");
        this.tafToolProvider.setToolToContext(browser);
        browserDataHolder.setContextBrowser(browser);
        browserDataHolder.setContextBrowserTab(browser.getCurrentWindow());
        browser.setSize(BrowserSetup.Resolution.RESOLUTION_1280x1024);
        final String expectedUrl = HTTPSURL.concat(HostConfigurator.getApache().getIp()).concat(NETWORKEXPLORERURL);
        final String actualUrl = browser.getCurrentWindow().getCurrentUrl();
        // Compare expected and actual URL
        LOGGER.debug("{} - Check if Actual URL is equal to Expected\n\t--> Expected: {}\n\t--> Actual:   {}", methodName, expectedUrl, actualUrl);
        Assertions.assertThat(expectedUrl)
                .as("Actual URL is not equal to Expected")
                .isEqualToIgnoringCase(actualUrl);
    }

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliUiTestStep.StepIds#ASSERT_LAUNCH_NODECLI_PRESENT}
     * <b>Name</b>: assertLaunchNodeCliButtonIsPresent      <i>[public]</i>
     * <b>Description</b>: This step test is used to verify if the button to launch
     * the nodeCli is present in the selected browser window.
     * </pre>
     */
    @TestStep(id = ASSERT_LAUNCH_NODECLI_PRESENT)
    public void assertLaunchNodeCliButtonIsPresent() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final BrowserTab browserTab = tafToolProvider.getCurrentBrowserTab();
        final ActionBarViewModel actionBarViewModel = browserTab.getView(ActionBarViewModel.class);
        boolean actionButtonDisplayed = false;
        boolean timeout = false;
        try {
            actionButtonDisplayed = actionBarViewModel.getActionButtonWithoutIconByName(NODE_CLI_ACTION).isDisplayed();
        } catch (final WaitTimedOutException exc) {
            timeout = true;
            LOGGER.error("{} - 'Launch Node CLI' failed due to tmeout --> {}\n", methodName, exc.getMessage());
        } finally {
            LOGGER.info("{} - Check 'Launch Node CLI' is Displayed --> {} (tmeout --> {})", methodName, actionButtonDisplayed, timeout);
            assertWithScreenShot(browserTab, String.format("'Launch Node CLI' was %sDisplayed (Browser ID --> %s)",
                    actionButtonDisplayed && !timeout ? "" : "NOT ", browserTab.hashCode()), actionButtonDisplayed && !timeout);
        }
    }

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliUiTestStep.StepIds#SEARCH_FOR_NODE_VUSERS}
     * <b>Name</b>: searchForNodeVUser      <i>[public]</i>
     * <b>Description</b>: This Test step is used to search, in the selected browser
     * window, for the node Indicated in the DataRecord received as a parameter.
     * </pre>
     *
     * @param node
     *         {@link com.ericsson.oss.testware.enmbase.data.NetworkNode } represents a node specified in a row in input csv file
     */
    @TestStep(id = SEARCH_FOR_NODE_VUSERS)
    public void searchForNodeVUser(@Input(ADDED_NODES) final NetworkNode node) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String nodeId = node.getNetworkElementId();
        final Browser browser = tafToolProvider.getBrowser();
        final ResultsViewModel resultsViewModel = browser.getCurrentWindow().getView(ResultsViewModel.class);
        LOGGER.info("{} - Search in Network Explorer Network with NodeId ==> {}", methodName, nodeId);
        networkExplorerOperator.get()
                .performSearch(String.format("select all objects of type NetworkElement where NetworkElement has attr name equal to %s", nodeId));
        resultsViewModel.waitUntilComponentIsDisplayed(resultsViewModel.getTableBody());
        networkExplorerOperator.get().selectObject(nodeId);
    }

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link StepIds#LAUNCH_AND_VERIFY_CLI_PARALLEL}
     * <b>Name</b>: launchCliShellAndVerifyWithVUsers      <i>[public]</i>
     * <b>Description</b>: This Test step has the purpose of launching the Node Cli on
     * the selected node and verifying the correct connection to the node itself.
     * </pre>
     *
     * @param vUsers
     *         Number of parallel execution
     * @param node
     *         {@link NetworkNode } represents a node specified in a row in input csv file
     * @param enmUser
     *         used to check Clu user you've logged.
     * @param testCaseId
     *         Executing Test Case.
     */
    @TestStep(id = LAUNCH_AND_VERIFY_CLI_PARALLEL)
    public void launchCliShellAndVerifyWithVUsers(@Input(Param.VUSER_PARAM) final int vUsers, @Input(ADDED_NODES) final NetworkNode node,
            @Input(AVAILABLE_USERS) final User enmUser, @Input(TEST_CASE_ID) final String testCaseId) {
        final String notValidIp = "0.0.0.0";
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String nodeId = node.getNetworkElementId();
        final String nodeType = node.getNodeType();
        final int vUserId = TafTestContext.getContext().getVUser();
        final String expectedUserName = NodeCliLogTestStep.getExpectedNodeCliUser(testCaseId, enmUser, node);
        LOGGER.info("{} - Executing '{}' test step:\n\tNetwork Element ID --> {}\n\tNode Type --> {}\n\tExecutng Test Case --> {}"
                + "\n\tExpected User Name --> {}\n\tVirtual User ID --> {}",
                methodName, LAUNCH_AND_VERIFY_CLI_PARALLEL, nodeId, nodeType, testCaseId, expectedUserName, vUserId);

        // Get Browser and Tab
        final Browser browser = tafToolProvider.getBrowser();
        final BrowserTab browserTab = browser.getCurrentWindow();
        final ActionBarViewModel actionBarViewModel = browserTab.getView(ActionBarViewModel.class);
        takeLocalScreenshot(browserTab, String.format("Select Browser Tab for %s(%s) node [User Id --> %s]", nodeId, nodeType, vUserId));

        // Get Node Cli Operator
        final NodeCliUiOperator cliUiOperator = nodeCliUiOperator.get();
        cliUiOperator.wait4CLIsToBeReady(vUsers);

        // Execution of Click on "Launch Node CLI" to open Node Cli
        actionBarViewModel.getActionButtonWithoutIconByName(NODE_CLI_ACTION).click();
        browser.switchWindow(switchToNodeCliBrowserWindow());
        final BrowserTab cliBrowserTab = browser.getCurrentWindow();
        if (LOGGER.isTraceEnabled()) {
            final StringBuilder listOfBrowserTab = new StringBuilder();
            for (final BrowserTab singleBrowserTab : browser.getAllOpenTabs()) {
                listOfBrowserTab.append(System.lineSeparator()).append("\t-> Title: ");
                listOfBrowserTab.append(singleBrowserTab.getTitle());
                listOfBrowserTab.append(System.lineSeparator()).append("\t-> Window Descriptor: ");
                listOfBrowserTab.append(singleBrowserTab.getWindowDescriptor());
                listOfBrowserTab.append(System.lineSeparator()).append("\t-> Current URL: ");
                listOfBrowserTab.append(singleBrowserTab.getCurrentUrl());
            }
            LOGGER.trace("{} - Browser Open tab ({}): {}",
                    methodName, browser.getAmountOfOpenTabs(), listOfBrowserTab);
        }
        takeLocalScreenshot(browserTab, String.format("Wait for Node Cli Window for %s(%s) node [User Id --> %s]", nodeId, nodeType, vUserId));

        final UiComponent shellWindow = cliUiOperator.getCliShell(cliBrowserTab);
        assertWithScreenShot(browserTab, String.format("Node Cli Shell not opened (Browser ID --> %s)", browser.hashCode()), shellWindow != null);

        // Check Node Cli status and close it
        final boolean openCliResult = cliUiOperator.loginToCLIShell(nodeId, nodeType, cliBrowserTab, expectedUserName);
        LOGGER.debug("{} - Login to Node CLI for node {} [{}] was --> {}",
                methodName, nodeId, nodeType, openCliResult ? "SUCCESS" : "FAILED");
        final boolean closeCliResult = cliUiOperator.exitFromOpenedCliShell(openCliResult, nodeId, nodeType, cliBrowserTab);
        cliUiOperator.wait4CLIsToBeDone();
        LOGGER.debug("{} - Closing Node CLI for node {} [{}] was --> {}",
                methodName, nodeId, nodeType, closeCliResult ? "SUCCESS" : "FAILED");
        assertWithScreenShot(browserTab, String.format("Node CLI for this browser was NOT opened  (Browser ID --> %s)",
                browser.hashCode()), openCliResult);
        assertWithScreenShot(browserTab, String.format("Node CLI for this browser was not closed (Browser ID --> %s)",
                browser.hashCode()), closeCliResult);
    }

    /**
     * <pre>
     * <b>Test Step Name</b>: {@link com.ericsson.oss.services.nodecli.operators.teststeps.NodeCliUiTestStep.StepIds#CLOSE_BROWSER}
     * <b>Name</b>: closeBrowser      <i>[public]</i>
     * <b>Description</b>: This TestStep should be use to Close Browser.
     * </pre>
     */
    @TestStep(id = StepIds.CLOSE_BROWSER)
    public void closeBrowser() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final Browser browser = tafToolProvider.getBrowser();
        LOGGER.info("{} - Closing Browser (ID --> {})", methodName, browser.hashCode());
        loginLogoutUiOperator.get().close(browser);
        LOGGER.trace("{} - Checking if Browser is closed --: {}", methodName, browser.isClosed());
        Assertions.assertThat(browser.isClosed()).as(String.format("Browser Closing Error (ID --> %d)", browser.hashCode())).isTrue();
    }

    // ************************************************************************
    // * Additional Methods to execute functions
    // ************************************************************************

    private BrowserTab switchToNodeCliBrowserWindow() {
        final Browser browser = tafToolProvider.getBrowser();
        browser.getCurrentWindow().waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return browser.getAmountOfOpenTabs() > 1;
            }
        });
        BrowserTab cliTab = null;

        // Loop through the open TABs looking for that of the Node CLI.
        for (final BrowserTab thisTab : browser.getAllOpenTabs()) {
            LOGGER.trace("Current Tab --> {} [{}]", thisTab.getCurrentUrl(), NODECLI_APPID);
            if (thisTab.getCurrentUrl().contains(NODECLI_APPID)) {
                cliTab = thisTab;
                break;
            }
        }

        // Check if the correct TAB was found
        Assertions.assertThat(cliTab).as(String.format("Did not find a new window with Node CLI (ID --> %d)", browser.hashCode())).isNotNull();
        LOGGER.debug("Switch current tab to {} [{}]", cliTab.getCurrentUrl(), browser.getAmountOfOpenTabs());
        return cliTab;
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
        public static final String INIT_BROWSER_AND_LOGIN = "Init browser and login";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String ASSERT_LAUNCH_NODECLI_PRESENT = "Check Launch Node Cli Button Presence";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String SEARCH_FOR_NODE_VUSERS = "Search for node with vUsers";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String LAUNCH_AND_VERIFY_CLI_PARALLEL = "Launch and verify CLI parallel";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CLOSE_BROWSER = "Close the browser";

        private StepIds() {}
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
        public static final String VUSER_PARAM = "vUser";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String CLIUSER_PARAM = "CliUserName";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String NODECLI_LOG_PARAM = "NodeCliLog";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String NODECLI_USER_PARAM = "NodeCliUser";

        private Param() {}
    }
}
