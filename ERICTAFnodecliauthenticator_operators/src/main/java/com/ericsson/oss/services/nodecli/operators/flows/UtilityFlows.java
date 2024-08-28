/*
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson  2019
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *
 */

package com.ericsson.oss.services.nodecli.operators.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.NODES_TO_ADD;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.SYNCED_NODES;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_DELETE;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodecli.operators.teststeps.NetSimTestStep;
import com.ericsson.oss.services.nodecli.operators.utility.BasicUtility;
import com.ericsson.oss.testware.nodeintegration.flows.NodeIntegrationFlows;
import com.ericsson.oss.testware.scenario.PrintDatasourceHelper;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutRestFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.google.common.base.Predicate;

/**
 * <pre>
 * <b>Class Name</b>: UtilityFlows
 * <b>Description</b>: This class of flows contains some to perform some operations
 * shared and common to several other flows / scenarios.
 * </pre>
 */
public class UtilityFlows extends BasicUtility {
    @Inject
    private UserManagementTestFlows userManagementTestFlows;
    @Inject
    private LoginLogoutRestFlows loginLogoutRestFlows;
    @Inject
    private NetSimTestStep networkElementTestSteps;
    @Inject
    private NodeIntegrationFlows nodeIntegrationFlows;
    @Inject
    private NetsimEnmFlow netsimEnmFlow;
    @Inject
    private PrintDatasourceHelper printDatasourceHelper;

    /**
     * <pre>
     * <b>Name</b>: createUsers            <i>[public]</i>
     * <b>Description</b>: This flow is a shell for the user creation operation that
     * adds the parallelization of the operation.
     * </pre>
     *
     * @param vUser number of parallel execution.
     * @param skipCreateUser Flag to skip User Creation.
     * @return a Test Step flow
     */
    public TestStepFlowBuilder createUsers(final int vUser, final boolean skipCreateUser) {
        final TestStepFlowBuilder createUser = skipCreateUser ? flow("Skip Create User flow") : userManagementTestFlows.createUser();
        return flow("Create Users")
                .addSubFlow(createUser)
                .withVusers(vUser);
    }

    /**
     * <pre>
     * <b>Name</b>: deleteUsers            <i>[public]</i>
     * <b>Description</b>: This flow is a shell for the user deletion operation that
     * adds the parallelization of the operation.
     * </pre>
     *
     * @param vUser number of parallel execution.
     * @param skipDeleteUser Flag to skip User Deletion.
     * @return a Test Step flow
     */
    public TestStepFlowBuilder deleteUsers(final int vUser, final boolean skipDeleteUser) {
        final TestStepFlowBuilder deleteUser = skipDeleteUser ? flow("Skip Delete User flow") : userManagementTestFlows.deleteUser();
        return flow("Delete Users")
                .beforeFlow(BasicUtility.setupDataSource(AVAILABLE_USERS, USERS_TO_DELETE, false, true, true))
                .addSubFlow(deleteUser)
                .withVusers(vUser);
    }

    /**
     * <pre>
     * <b>Name</b>: login            <i>[public]</i>
     * <b>Description</b>: This flow is a shell for the user login operation that adds
     * operation parallelization and filtering.
     * </pre>
     *
     * @param userFilter Predicate to select users for login
     * @param vUser number of parallel execution.
     * @return a Test Step flow
     */
    public TestStepFlow login(final Predicate userFilter, final int vUser) {
        return loginLogoutRestFlows.loginBuilder()
                .beforeFlow(BasicUtility.setupDataSource(AVAILABLE_USERS, false, true, true))
                .withDataSources(dataSource(AVAILABLE_USERS)
                        .withFilter(userFilter))
                .withVusers(vUser)
                .build();
    }

    /**
     * <pre>
     * <b>Name</b>: logout            <i>[public]</i>
     * <b>Description</b>: This flow is a shell for the user logout operation that adds
     * operation parallelization and filtering.
     * </pre>
     *
     * @param userFilter Predicate to select users for logout
     * @param vUser number of parallel execution.
     * @return a Test Step flow
     */
    public TestStepFlow logout(final Predicate userFilter, final int vUser) {
        return loginLogoutRestFlows.logoutBuilder()
                .beforeFlow(BasicUtility.setupDataSource(AVAILABLE_USERS, false, true, true))
                .withDataSources(dataSource(AVAILABLE_USERS)
                        .withFilter(userFilter))
                .withVusers(vUser)
                .build();
    }

    /**
     * <pre>
     * <b>Name</b>: loginDefaultUser            <i>[public]</i>
     * <b>Description</b>: This flow is a shell for the defaultUser login operation
     *    that adds operation parallelization.
     * </pre>
     *
     * @param vUser number of parallel execution.
     * @return a Test Step flow
     */
    public TestStepFlowBuilder loginDefaultUser(final int vUser) {
        return flow("Login Default User").addSubFlow(loginLogoutRestFlows.loginDefaultUser()).withVusers(vUser);
    }

    /**
     * <pre>
     * <b>Name</b>: logoutDefaultUser            <i>[public]</i>
     * <b>Description</b>: This flow is a shell for the defaultUser logout operation
     *    that adds operation parallelization.
     * </pre>
     *
     *
     * @param vUser number of parallel execution.
     * @return a Test Step flow
     */
    public TestStepFlowBuilder logoutDefaultUser(final int vUser) {
        return flow("Logout Default User").addSubFlow(loginLogoutRestFlows.logout()).withVusers(vUser);
    }

    /**
     * <pre>
     * <b>Name</b>: startNetsimNodes            <i>[public]</i>
     * <b>Description</b>: This flow is a shell for the start of NetSim nodes.
     *  - adds the parallelization of the operation
     *  - adds the filter on the nodes to start
     *  - hides any exceptions (assert)
     * </pre>
     *
     * @param nodeFilter Predicate to select Netsim nodes to start.
     * @param vUser number of parallel execution.
     * @return a Test Step flow
     */
    public TestStepFlow startNetsimNodes(final Predicate nodeFilter, final int vUser) {
        return flow("Start Netsim Nodes")
                .beforeFlow(BasicUtility.setupDataSource(NODES_TO_ADD, false, true, true))
                .addTestStep(annotatedMethod(networkElementTestSteps, NetSimTestStep.StepIds.TEST_STEP_NETSIM_NODE_START))
                .withDataSources(dataSource(NODES_TO_ADD)
                        .withFilter(nodeFilter).allowEmpty())
                .withExceptionHandler(ScenarioExceptionHandler.LOGONLY)
                .withVusers(vUser)
                .build();
    }

    /**
     * <pre>
     * <b>Name</b>: createNodes            <i>[public]</i>
     * <b>Description</b>: This flow is a shell for the creation of NetSim nodes.
     *  - adds the parallelization of the operation
     *  - adds the filter on the nodes to add
     *  - hides any exceptions (assert)
     * </pre>
     *
     * @param nodeFilter Predicate to select nodes to create.
     * @param vUser number of parallel execution.
     * @return a Test Step flow
     */
    public TestStepFlow createNodes(final Predicate nodeFilter, final int vUser) {
        return nodeIntegrationFlows.addNodeBuilder()
                .beforeFlow(BasicUtility.setupDataSource(NODES_TO_ADD, false, true, true))
                .withDataSources(dataSource(NODES_TO_ADD)
                        .withFilter(nodeFilter).allowEmpty())
                .withExceptionHandler(ScenarioExceptionHandler.LOGONLY)
                .withVusers(vUser)
                .build();
    }

    /**
     * <pre>
     * <b>Name</b>: syncNodes            <i>[public]</i>
     * <b>Description</b>: This flow is a shell for the sync of NetSim nodes.
     *  - adds the parallelization of the operation
     *  - adds the filter on the nodes to synv
     *  - hides any exceptions (assert)
     * </pre>
     *
     * @param nodeFilter Predicate to select nodes to sync.
     * @param vUser number of parallel execution.
     * @return a Test Step flow
     */
    public TestStepFlow syncNodes(final Predicate nodeFilter, final int vUser) {
        return nodeIntegrationFlows.syncNodeBuilder()
                .beforeFlow(BasicUtility.setupDataSource(NODES_TO_ADD, false, true, true))
                .afterFlow(BasicUtility.setupDataSource(SYNCED_NODES, false, true, true))
                // .afterFlow(BasicUtility.setupDataSource(SYNCED_NODES, ADDED_NODES, false, true, true))
                .withDataSources(dataSource(NODES_TO_ADD)
                        .withFilter(nodeFilter).allowEmpty())
                .withExceptionHandler(ScenarioExceptionHandler.LOGONLY)
                .withVusers(vUser)
                .build();
    }

    /**
     * <pre>
     * <b>Name</b>: deleteNodes            <i>[public]</i>
     * <b>Description</b>: This flow is a shell for the delete of NetSim nodes.
     *  - adds the parallelization of the operation
     *  - adds the filter on the nodes to synv
     *  - hides any exceptions (assert)
     * </pre>
     *
     * @param nodeFilter Predicate to select nodes to delete.
     * @param vUser number of parallel execution.
     * @return a Test Step flow
     */
    public TestStepFlow deleteNodes(final Predicate nodeFilter, final int vUser) {
        return nodeIntegrationFlows.deleteNodeBuilder()
                .beforeFlow(BasicUtility.setupDataSource(ADDED_NODES, false, true, true))
                .withDataSources(dataSource(ADDED_NODES)
                        .withFilter(nodeFilter).bindTo(ADDED_NODES).allowEmpty())
                .withExceptionHandler(ScenarioExceptionHandler.LOGONLY)
                .withVusers(vUser)
                .build();
    }

    /**
     * Flow for clear node netsim passwd.
     *
     * @param vUser the user
     * @return a TestStepFlow
     */
    /**
     * <pre>
     * <b>Name</b>: resetUserOnNodes            <i>[public]</i>
     * <b>Description</b>: This TestStep flow can be use to Reset User (put in default
     *   mode) netsim Node object..
     * </pre>
     * @param user Predicate to filter used Datasource
     * @param vUser Parallel execution count
     * @return A teststep flow
     */
    public TestStepFlow resetUserOnNodes(final Predicate user, final int vUser) {
        return netsimEnmFlow.resetUserOnNodes()
                .beforeFlow(BasicUtility.setupDataSource(NODES_TO_ADD, false, true, true))
                .withDataSources(dataSource(NODES_TO_ADD).withFilter(user).allowEmpty()).withVusers(vUser).build();
    }

    /**
     * <pre>
     * <b>Name</b>: emptyFlow            <i>[public]</i>
     * <b>Description</b>: This test flow should be use to made empty flow to skip
     *   operations: it could be a flow with a parametrized wait time.
     * </pre>
     * @param flowName Name of this flow
     * @param waitForSeconds (Optional) wait time (seconds).
     * @return A teststep flow
     */
    public TestStepFlowBuilder emptyFlow(final String flowName, final int waitForSeconds) {
        return flow(flowName).pause(waitForSeconds, TimeUnit.SECONDS);
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    public TestStepFlowBuilder emptyFlow(final String flowName) {
        final int defaultWaitTime = 1;
        return emptyFlow(flowName, defaultWaitTime);
    }
}
