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

import static java.util.concurrent.TimeUnit.SECONDS;

import static com.ericsson.cifwk.taf.ui.UiToolkit.pause;
import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.takeLocalScreenshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.oss.services.nodecli.operators.views.ActionBarViewModel;
import com.ericsson.oss.testware.enmbase.data.NodeType;

/**
 * <pre>
 * <b>Name</b>: NodeCliUiOperator      <i>[public (Class)]</i>
 * <b>Description</b>: This class contains the methods needed to perform
 * <i>UI window management</i> operations for the NodeCli.
 * </pre>
 */
@Operator
public class NodeCliUiOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeCliUiOperator.class);
    private static final int BROWSER_TIMEOUT_SEC = 10;
    private static final String NEWLINE = "\n";
    private static int numberOfParallelCLIs;
    private static final int NODECLI_LOGIN_RETRY_COUNT = DataHandler.getConfiguration().getProperty("nodecli.timeout.retry", 5, Integer.class);

    private int actCliNumber;

    private UiComponent shellUIcomp;
    private UiComponent textArea;
    private ActionBarViewModel nodeViewModel;

    /**
     * <pre>
     * <b>Name</b>: getCliShell      <i>[public]</i>
     * <b>Description</b>: The function returns the reference to the graphical
     * shell object (Node Cli).
     * </pre>
     * @param currentBrowserTab Reference to the browser TAB used.
     * @return - Node Shell component
     */
    public UiComponent getCliShell(final BrowserTab currentBrowserTab) {
        LOGGER.trace("View Model Status:\n\tShell Component --> {}\n\tView Model --> {}",
                this.shellUIcomp, this.nodeViewModel);
        if (this.shellUIcomp == null || this.nodeViewModel == null || this.textArea == null) {
            this.nodeViewModel = currentBrowserTab.getView(ActionBarViewModel.class);
            this.shellUIcomp = nodeViewModel.getShell();
            this.textArea = nodeViewModel.getTextArea();
        }
        takeLocalScreenshot(currentBrowserTab, "Get Cli shell for Node");
        return this.shellUIcomp;
    }

    /**
     * <pre>
     * <b>Name</b>: loginToCliShell      <i>[public]</i>
     * <b>Description</b>: This method is used to access the <I>graphical CLI</I> from the
     * current Browser Tab.
     *  Once the shell is opened, the method checks the correct prompt for the
     * selected node type and returns the result of the check..
     * </pre>
     * @param nodeId Network element ID string
     * @param nodeType Node Type String
     * @param currentBrowserTab Current Browser Tab selected.
     * @return boolean result for login to None Cli (expected Prompt).
     */
    public boolean loginToCliShell(final String nodeId, final String nodeType, final BrowserTab currentBrowserTab) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        currentBrowserTab.waitUntilComponentIsDisplayed(getCliShell(currentBrowserTab), SECONDS.toMillis(20));
        takeLocalScreenshot(currentBrowserTab, String.format("Login to Cli Shell for %s(%s) node", nodeId, nodeType));

        final String nodePrompt = String.format(getPromptNodeType(nodeType), nodeId);
        final List<String> cliTextContent = getCliLines();
        final String lastRow = cliTextContent.get(cliTextContent.size() - 1).replace(NEWLINE, "");
        final boolean contains = lastRow.indexOf(nodePrompt) != -1;
        takeLocalScreenshot(currentBrowserTab, String.format("Check if NodeCli promt is '%s'", nodePrompt));

        LOGGER.debug("{} - Check Node Cli prompt for (1) {}[{}] node:\n\tBrowser TAB title --> {}\n\t"
                + "Expected Prompt --> {}\n\tReceived Prompt --> {}\n\tResult --> {}",
                methodName, nodeId, nodeType, currentBrowserTab.getTitle(), nodePrompt, lastRow, contains);
        return contains;
    }

    /**
     * <pre>
     * <b>Name</b>: loginAndCheckNodeCliShell      <i>[public]</i>
     * <b>Description</b>: This method is used to access the <i>graphical CLI</i> of the node
     * from the current Browser TAB.
     *  Once the shell has been opened, the method checks whether it has opened
     * with the <i>current user</i>: this check is notified only with a <u>Warning message</u>,
     * since NetSim does not report on the shell the user name with which the CLI
     * is connected.
     *  A second check is on the prompt that should appear, which depends on the
     * type of node being used: in this case the check returns the result to the
     * caller.
     * </pre>
     *
     * @param nodeId Network element ID string
     * @param nodeType Node Type String
     * @param userName Expected USER to see in CLI shell output (warn message if not present)
     * @param currentBrowserTab Current Browser Tab selected.
     * @return - boolean result for login to None Cli (expected Prompt).
     */
    public boolean loginAndCheckNodeCliShell(final String nodeId, final String nodeType, final String userName, final BrowserTab currentBrowserTab) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        boolean checkResult = false;
        int retryCount = NODECLI_LOGIN_RETRY_COUNT;
        LOGGER.debug("{} - Input parameters:\n\tNode Type -> '{}'\n\tNetwork Element ID --> '{}'\n\tUser Name --> '{}'"
                + "\n\tBrowser TAB title --> {} \n\t[{}]",
                methodName, nodeType, nodeId, userName, currentBrowserTab.getTitle(), currentBrowserTab.getCurrentUrl());
        takeLocalScreenshot(currentBrowserTab, String.format("Get NodeCli shell for '%s'[%s] node", nodeId, nodeType));
        currentBrowserTab.waitUntilComponentIsDisplayed(getCliShell(currentBrowserTab), SECONDS.toMillis(20));
        final String nodePrompt = String.format(getPromptNodeType(nodeType), nodeId);

        List<String> cliTextContent = new ArrayList<>(0);
        do {
            cliTextContent = getCliLines();
            retryCount--;
            pause(SECONDS.toMillis(5));
        }
        while (cliTextContent.size() == 0 && retryCount > 0);
        takeLocalScreenshot(currentBrowserTab, String.format("Check NodeCli Text size -->  '%s'", cliTextContent.size()));

        // Check if there is some lines in Node Cli Output
        if (cliTextContent.size() > 0) {        // Check for User (
            final String lastRow = cliTextContent.get(cliTextContent.size() - 1).replace(NEWLINE, "");
            final boolean checkUserResult = lastRow.indexOf(userName) != -1;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} - Check Node Cli for (2) {}[{}] node:\n\tBrowser TAB title --> {}\n\t"
                                + "Expected User --> '{}'\n\tReceived lines --> {}\n\tResult --> {}", methodName, nodeId, nodeType,
                        currentBrowserTab.getTitle(), userName, String.join("[CR]", cliTextContent), checkUserResult);
            }
            if (!checkUserResult) {
                LOGGER.warn("{} - Check Node Cli for {}[{}] node and {} user:\n\t =====>>> No user in CLI output !\n\t"
                        + "NB. Username is not available with NetSim !!", methodName, nodeId, nodeType, userName);
            }

            // Check for Prompt
            checkResult = lastRow.indexOf(nodePrompt) != -1;
            LOGGER.debug("{} - Check Node Cli prompt for (3) {}[{}] node:\n\tBrowser TAB title --> {}\n\t"
                            + "Expected Prompt --> {}\n\tReceived Prompt --> {}\n\tResult --> {}", methodName, nodeId, nodeType,
                    currentBrowserTab.getTitle(),
                    nodePrompt, lastRow, checkResult);
        } else {
            LOGGER.debug("{} - Check Node Cli prompt for (4) {}[{}] node:\n\tBrowser TAB title --> {}\n\t"
                            + "Node Cli Output line count --> {}\n\tResult --> {}", methodName, nodeId, nodeType,
                    currentBrowserTab.getTitle(),
                    nodePrompt, cliTextContent.size(), checkResult);
            checkResult = false;
        }
        return checkResult;
    }

    /**
     * <pre>
     * <b>Name</b>: exitFromCliShell      <i>[public]</i>
     * <b>Description</b>: This method closes the CLI shell.
     * </pre>
     * @param nodeId Network element ID string
     * @param nodeType Node Type String
     * @param isConnected flag to say if Node CLI is connected
     * @return Exit operation result (false if not connected)
     */
    public boolean exitFromCliShell(final boolean isConnected, final String nodeId, final String nodeType) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (isConnected) {
            LOGGER.trace("{} - Exit from Node CLI shell: Node --> {}[{}]", methodName, nodeId, nodeType);
            shellUIcomp.sendKeys(Pattern.EXIT);
            shellUIcomp.sendKeys(Keys.ENTER);
            return true;
        }
        LOGGER.warn("{} - Node CLI is NOT connected: Node --> {}[{}]", methodName, nodeId, nodeType);
        return false;
    }

    /**
     * <pre>
     * <b>Name</b>: loginToCliShellWithWrongPwd      <i>[public]</i>
     * <b>Description</b>: This method tries to connect to the node's CLI shell
     * incorrectly (wrong password) and parses the displayed message.
     * </pre>
     *
     * @param nodeId Network element ID string
     * @param nodeType Node Type String
     * @param currentBrowserTab Current Browser Tab selected.
     * @return boolean result for login to None Cli (Wrong Password).
     */
    public boolean loginToCliShellWithWrongPwd(final String nodeId, final String nodeType, final BrowserTab currentBrowserTab) {
        currentBrowserTab.waitUntilComponentIsDisplayed(getCliShell(currentBrowserTab), SECONDS.toMillis(20));
        final String nodePrompt = String.format(getPromptNodeType(nodeType), nodeId);
        boolean loginShellResult = false;
        if (validatePatternInWholeCli(Pattern.TELNET_CONNECTION)) {
            loginShellResult = validatePatternInWholeCli(Pattern.ALL_CONNECTION_FAILED);
        } else if (validatePatternInWholeCli(Pattern.SSH_CONNECTION)) {
            loginShellResult = validatePatternInWholeCli(Pattern.SSH_AUTHENTICATION_FAILURE)
                    || !validatePatternInWholeCli(nodePrompt);
        } else {
            loginShellResult = validatePatternInWholeCli(Pattern.ALL_CONNECTION_FAILED);
        }
        return loginShellResult;
    }

    /**
     * <pre>
     * <b>Name</b>: loginToCLIShell      <i>[public]</i>
     * <b>Description</b>: This method sequentially performs the opening and closing
     * operations of the node shell CLI.
     * </pre>
     *
     * @param nodeId Network element ID string
     * @param nodeType Node Type String
     * @param currentBrowserTab Current Browser Tab selected.
     * @param cliUserName used to check ClI user you've logged in.
     * @return Operation result
     */
    public boolean loginToCLIShell(final String nodeId, final String nodeType, final BrowserTab currentBrowserTab, final String cliUserName) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.trace("{} - Login to Node Cli Shell for Node: {}[{}] (browser tab title: {} [{}])",
                methodName, nodeId, nodeType, currentBrowserTab.getTitle(), currentBrowserTab.getCurrentUrl());
        takeLocalScreenshot(currentBrowserTab, String.format("Before login to Node CLI shell for %s(%s) node", nodeId, nodeType));
        final boolean isConenect = loginAndCheckNodeCliShell(nodeId, nodeType, cliUserName, currentBrowserTab);
        takeLocalScreenshot(currentBrowserTab, String.format("Logged in --> Node CLI shell for %s(%s) node",
                isConenect ? "" : "Not", nodeId, nodeType));
        return isConenect;
    }

    /**
     * <pre>
     * <b>Name</b>: exitFromOpenedCliShell      <i>[public]</i>
     * <b>Description</b>: This method performs the closing operations of the node
     *    shell CLI.
     * </pre>
     *
     * @param isCliOpen Status of Previous operation (shell was opened
     * @param nodeId Network element ID string
     * @param nodeType Node Type String
     * @param currentBrowserTab Current Browser Tab selected.
     * @return closing result
     */
    public boolean exitFromOpenedCliShell(final boolean isCliOpen, final String nodeId, final String nodeType, final BrowserTab currentBrowserTab) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info("{} - Login to Node CLI is {} (Node: {} [{}])", methodName, isCliOpen ? "DONE" : "FAILED", nodeId, nodeType);
        final boolean exitShellTesult = exitFromCliShell(isCliOpen, nodeId, nodeType);
        takeLocalScreenshot(currentBrowserTab, String.format("Logged out --> Node CLI shell for %s(%s) node", nodeId, nodeType));
        return exitShellTesult;
    }

    /**
     * <pre>
     * <b>Name</b>: wait4CLIsToBeReady      <i>[public]</i>
     * <b>Description</b>: This method checks the number of node shell CLI instances
     * that have been opened.
     * </pre>
     *
     * @param wait4Browsers Number of Browser to be open,
     */
    public void wait4CLIsToBeReady(final int wait4Browsers) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        actCliNumber = ++numberOfParallelCLIs;
        LOGGER.trace("{} - Node Cli UI Object Number -----> {}/{}}", methodName, actCliNumber, numberOfParallelCLIs);
        int time = BROWSER_TIMEOUT_SEC;
        while (numberOfParallelCLIs != wait4Browsers && time-- > 0) {
            pause(SECONDS.toMillis(1));
        }
    }

    /**
     * <pre>
     * <b>Name</b>: wait4CLIsToBeReady      <i>[public]</i>
     * <b>Description</b>: This method checks the number of node shell CLI instances
     * that have been closed.
     * </pre>
     *
     */
    public void wait4CLIsToBeDone() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        long time = BROWSER_TIMEOUT_SEC;
        LOGGER.trace("{} - Node Cli UI Object Number -----> {}/{}}", methodName, actCliNumber, numberOfParallelCLIs);
        numberOfParallelCLIs--;
        while (numberOfParallelCLIs != 0 && time-- > 0) {
            pause(1000L);
        }
    }

    // ************************************************************************
    // | Private Methods.
    // ************************************************************************

    /**
     * <pre>
     * <b>Name</b>: validatePatternInWholeCLI      <i>[private]</i>
     * <b>Description</b>: .
     * </pre>
     *
     * @param pattern Pattern to compare for NodeCli.
     * @return - Compare Result
     */
    private boolean validatePatternInWholeCli(final String pattern) {
        // Get all Node Cli Output
        final List<String> lines = Arrays.asList(getCliLines().get(0).split("\\r?\\n"));

        for (final String singleLine: lines) {
            if (singleLine.replace(NEWLINE, "").matches(pattern)) {
                return true;
            }
        }
        LOGGER.debug("Pattern NOT found: Expected --> {}", pattern);
        LOGGER.trace("Node Cli Console output: \n{}", lines);
        return false;
    }

    /**
     * <pre>
     * <b>Name</b>: getCliLines      <i>[private]</i>
     * <b>Description</b>:  This method is used to return the contents of the Node Cli
     * shell, verifying the existence of the shell itself and the text displayed
     * in it.
     * </pre>
     *
     * @return Node Cli output lines
     */
    private List<String> getCliLines() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (this.textArea == null) {
            LOGGER.error("{} - There is NO 'shellUIcomp'!", methodName);
            return new ArrayList<>(0);
        }

        // Check Children object presence
        final List<UiComponent> uicDivs = textArea.getChildren();
        if (uicDivs == null) {
            LOGGER.error("{} - There is NO 'div' TAG!", methodName);
            return new ArrayList<>(0);
        }

        // Wait for shell message (about 6 seconds)
        pause(SECONDS.toMillis(6));
        final List<String> vmAndCliText = new ArrayList<>();
        for (final UiComponent child : uicDivs) {
            final String val = child.getText().trim();
            if (!val.isEmpty()) {
                vmAndCliText.add(val);
            }
        }

        // Debug purpose: prind Node Cli Text
        if (LOGGER.isTraceEnabled()) {
            final StringBuilder nodeCliContent = new StringBuilder();
            for (final String row : vmAndCliText) {
                nodeCliContent.append(String.format("---| %s", row)).append(NEWLINE);
            }
            LOGGER.trace("{} - Node Cli Output ---\n{}", methodName, nodeCliContent);
        }
        return vmAndCliText;
    }

    /**
     * <pre>
     * <b>Name</b>: PromptNodeType      <i>[protected-package enum]</i>
     * <b>Description</b>: This enumerative contains the definition of the nodes used
     * (and their real names); allows you to retrieve the expected prompt from
     * the node CLI for each of them..
     * </pre>
     */
    private String getPromptNodeType(final String selectedNodeType) {
        LOGGER.trace("Get Node Type for Prompt: {} --> {}", selectedNodeType, NodeType.getType(selectedNodeType));
        switch (NodeType.getType(selectedNodeType)) {
            case EPG:
            case VEPG:
                return "*%s>";
            case EPG_OI:
            case VEPG_OI:
                return "[local]router6000>";
            case VBGF:
            case VCSCF:
            case RADIO_NODE:
            case FIVEG_RADIO_NODE:
            case MSRBS_V1:
            case PCC:
            case PCG:
            case CCDM:
            case CCRC:
            case CCSM:
            case CCPC:
                return ">";
            case VMTAS:
                return "#";
            default:
                return null;
        }
    }

    /**
     * <pre>
     * <b>Name</b>: Pattern      <i>[protected-package class]</i>
     * <b>Description</b>: .
     * </pre>
     */
    static final class Pattern {
        static final String CONNCLOSED = "(.*Connection closed.*)|(.*Connection refused.*)";
        static final String ALL_CONNECTION_FAILED = "(.*All supported CLI connection attempts failed.*)|(.*Error: Login failed.*)";
        static final String SSH_AUTHENTICATION_FAILURE = ".*SSH authentication failure.*";
        static final String TELNET_CONNECTION = ".*Opening Telnet connection.*";
        static final String SSH_CONNECTION = ".*Opening SSH connection.*";
        static final String SESSION_ALREADY_EXISTS = "(.*An open session already exists.*)|(.*Only one session allowed.*)";
        private static final String EXIT = "exit";

        private Pattern() {
        }
    }
}
