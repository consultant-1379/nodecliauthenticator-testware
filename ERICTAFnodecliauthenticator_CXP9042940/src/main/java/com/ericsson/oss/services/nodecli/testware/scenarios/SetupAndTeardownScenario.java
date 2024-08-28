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

package com.ericsson.oss.services.nodecli.testware.scenarios;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.copy;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromTafDataProvider;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.merge;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.shareDataSource;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.resetDataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.distributeValues;
import static com.ericsson.oss.services.nodecli.testware.constant.Constants.Profiles.PROFILE_HYDRA;
import static com.ericsson.oss.services.nodecli.testware.constant.Constants.Profiles.PROFILE_MAINTRACK;
import static com.ericsson.oss.services.nodecli.testware.constant.Constants.Profiles.PROFILE_REAL_NODE;
import static com.ericsson.oss.services.nodecli.testware.constant.Constants.Profiles.PROFILE_TDM_INFO;
import static com.ericsson.oss.services.nodecli.testware.scenarios.NodeCLIAutenticathorScenario.NODETYPE_FILTERED_DEFAULTVALUE;
import static com.ericsson.oss.services.nodecli.testware.scenarios.NodeCLIAutenticathorScenario.NODETYPE_FILTER_PROPERTYNAME;
import static com.ericsson.oss.services.nodecli.testware.scenarios.NodeCLIAutenticathorScenario.TEAMNAME_DEFAULTVALUE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.NODES_TO_ADD;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.SYNCED_NODES;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_DELETE;
import static com.ericsson.oss.testware.nodesecurity.steps.CertificateIssueTestSteps.DataSource.CERT_ISSUE_REISSUE_DATASOURCE;
import static com.google.common.collect.Iterables.filter;

import java.util.HashMap;
import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.pool.DataPoolStrategy;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.MapSource;
import com.ericsson.cifwk.taf.datasource.TafDataSourceFactory;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.UnknownDataSourceTypeException;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.api.TestScenarioBuilder;
import com.ericsson.oss.services.nodecli.operators.flows.UtilityFlows;
import com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep;
import com.ericsson.oss.services.nodecli.testware.datasource.UsersToCreateTimeStampDataSource;
import com.ericsson.oss.services.nodecli.testware.predicate.PredicateExtended;
import com.ericsson.oss.services.nodecli.testware.predicate.Predicates;
import com.ericsson.oss.testware.flow.PemKeyFlow;
import com.ericsson.oss.testware.network.operators.netsim.NetsimDataProvider;
import com.ericsson.oss.testware.scenario.PrintDatasourceHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

/**
 * <pre>
 * <b>Class Name</b>: SetupAndTeardownScenario
 * <b>Description</b>: The class contains necessary operations that must be executed
 *   before and after every test suite..
 * </pre>
 */
public class SetupAndTeardownScenario extends SetupAndTearDownUtil {
    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static String testwareTeamName = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupAndTeardownScenario.class);
    private static final String NO_DATA = "No Data in Data Source";
    private static final String CLASS_FIELD = "class";

    private static final String LOGGER_INFO_PARAM_ONLY = "\n\t%s: PARAM = %s\n";

    private static final int MAX_NODES_PARALLEL_EXECUTION = DataHandler.getConfiguration().getProperty("max.netsim.vUser.number", 4, Integer.class);
    private static final String NODE_TYPES = DataHandler.getConfiguration()
            .getProperty(NODETYPE_FILTER_PROPERTYNAME, NODETYPE_FILTERED_DEFAULTVALUE, String.class);
    private static final String DEFAULT_TEAMNAME = DataHandler.getConfiguration()
            .getProperty("testware.default.teamname", TEAMNAME_DEFAULTVALUE, String.class);
    private static final Boolean FORCE_NODE_CREATION = DataHandler.getConfiguration()
            .getProperty("force.node.creation.flag", false, Boolean.class);

    // Internal utilities
    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected PemKeyFlow pemKeyFlow;
    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected UtilityFlows utilityFlows;

    /**
     * <pre>
     * <b>Name</b>: onBeforeSuiteMethod                  <i>[protected]</i>
     * <b>Description</b>: SSequence of operations to be performed before the execution
     *   of the scenario.
     * </pre>
     *
     * @param suiteContext
     *         ITestContext object
     * @param customNodeDatasource
     *         Customized datasource name for Nodes from Suite XML file.
     * @param suiteTeamName
     *         Suite Team Name to Use.
     */
    protected void onBeforeSuiteMethod(final ITestContext suiteContext, final String customNodeDatasource, final String suiteTeamName) {
        setSuiteContext(suiteContext);
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info(" **** Execution of {} method - START **** {}", methodName, SEPARATOR);

        // Fetch current profile
        final TafConfiguration tafConfiguration = DataHandler.getConfiguration();
        final String profile = tafConfiguration.getProperty("taf.profiles", "", String.class);
        LOGGER.info("The Suite '{}' is running with profile: {}", getSuiteName(), profile);

        // Setting Default TeamName for Suite
        testwareTeamName = suiteTeamName == null || suiteTeamName.isEmpty() ? DEFAULT_TEAMNAME : suiteTeamName;

        // Initialize DataSources
        standardDataSourceFromProfileConfiguration(profile, customNodeDatasource);
        setupSpecificDataSource(profile);
        realignUserDataSource(profile);

        // BeforeSuiteScenarioBuilder
        utilityFlows.reduceSyncRetryProperty();
        final TestScenario scenario = beforeSuiteScenarioBuilder(profile).build();
        final TestScenarioRunner runner = SetupAndTearDownUtil.getScenarioRunner();
        runner.start(scenario);
        utilityFlows.restoreSyncRetryProperty();
        realignNodeDataSource(profile);
        Assertions.assertThat(Iterables.size(context.dataSource(SYNCED_NODES)))
                .as(String.format("No Synced Nodes are present (%d/%d)", Iterables.size(context.dataSource(ADDED_NODES)),
                        Iterables.size(context.dataSource(SYNCED_NODES)))).isNotEqualTo(0);
        LOGGER.info(" **** Execution of {} method -  END  **** {}", methodName, SEPARATOR);
    }

    private TestScenarioBuilder beforeSuiteScenarioBuilder(final String profile) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final int parallelExecutionNodes = distributeValues(getNumberOfNodes(), MAX_NODES_PARALLEL_EXECUTION);
        final int parallelExecutionUsers = distributeValues(getNumberOfUsers(), MAX_NODES_PARALLEL_EXECUTION);
        LOGGER.debug("{} - Executing Before Suite with [{} --> {}] profile and {} Nodes ({})/ {} Users ({})",
                methodName, profile, profile.toLowerCase(), getNumberOfNodes(), parallelExecutionNodes, getNumberOfUsers(), parallelExecutionUsers);
        return scenario("Before Suite Scenario ")
                .addFlow(pemKeyFlow.setPemKey())
                // TODO - Verify netsim host, simulation and nodes Availability)!
                .addFlow(utilityFlows.resetUserOnNodes(PredicateExtended.netsimNodePredicate(), parallelExecutionNodes))
                .addFlow(utilityFlows.createUsers(parallelExecutionUsers, PROFILE_REAL_NODE.equals(profile.toLowerCase()))
                        .afterFlow(resetDataSource(AVAILABLE_USERS)).afterFlow(shareDataSource(AVAILABLE_USERS)))
                .addFlow(utilityFlows.startNetsimNodes(PredicateExtended.netsimNodePredicate(), parallelExecutionNodes))
                // Since we are performing the setup operations (and not the tests) we access ENM with the 'ci-user'
                //   user to simplify the preparation of the environment.
                // .addFlow(utilityFlows.login(PredicateExtended.cmAdm(), getNumberOfNodes()))
                .addFlow(utilityFlows.loginDefaultUser(parallelExecutionNodes))
                .addFlow(utilityFlows.createNodes(PredicateExtended.netsimNodePredicate(), parallelExecutionNodes))
                .addFlow(utilityFlows.syncNodes(PredicateExtended.netsimNodePredicate(), parallelExecutionNodes))
                // Since we are performing the setup operations (and not the tests) we access ENM with the 'ci-user'
                //   user to simplify the preparation of the environment.
                // .addFlow(utilityFlows.logout(PredicateExtended.cmAdm(), getNumberOfNodes())).alwaysRun();
                .addFlow(utilityFlows.logoutDefaultUser(parallelExecutionNodes)).alwaysRun();
    }

    /**
     * <pre>
     * <b>Name</b>: onAfterSuiteMethod                  <i>[protected]</i>
     * <b>Description</b>: Sequence of operations to be performed after the execution of the scenario.
     * </pre>
     *
     * .
     */
    protected void onAfterSuiteMethod() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info(" **** Execution of {} method - START **** {}", methodName, SEPARATOR);
        final String profile = DataHandler.getConfiguration().getProperty("taf.profiles", "", String.class);
        Preconditions.checkArgument(context.dataSource(AVAILABLE_USERS).iterator().hasNext(), NO_DATA);
        teardownSpecificDataSource();
        final TestScenario scenario = afterSuiteScenarioBuilder(profile).build();
        final TestScenarioRunner runner = SetupAndTearDownUtil.getScenarioRunner();
        runner.start(scenario);
        LOGGER.info(" **** Execution of {} method -  END  **** {}", methodName, SEPARATOR);
    }

    private TestScenarioBuilder afterSuiteScenarioBuilder(final String profile) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final int parallelExecutionNodes = distributeValues(getNumberOfNodes(), MAX_NODES_PARALLEL_EXECUTION);
        final int parallelExecutionUsers = distributeValues(getNumberOfUsers(), MAX_NODES_PARALLEL_EXECUTION);
        LOGGER.debug("{} - Executing After Suite with [{} --> {}] profile and {} Nodes ({})/ {} Users ({})",
                methodName, profile, profile.toLowerCase(), getNumberOfNodes(), parallelExecutionNodes, getNumberOfUsers(), parallelExecutionUsers);
        return scenario("After Suite Scenario ")
                // Since we are performing the setup operations (and not the tests) we access ENM with the 'ci-user'
                //   user to simplify the preparation of the environment.
                // .addFlow(utilityFlows.login(PredicateExtended.cmAdm(), getNumberOfNodes()))
                .addFlow(utilityFlows.loginDefaultUser(parallelExecutionNodes))
                .addFlow(utilityFlows.deleteNodes(PredicateExtended.netsimNodePredicate(), parallelExecutionNodes))
                // Since we are performing the setup operations (and not the tests) we access ENM with the 'ci-user'
                //   user to simplify the preparation of the environment.
                // .addFlow(utilityFlows.logout(PredicateExtended.cmAdm(), getNumberOfNodes())).alwaysRun();
                .addFlow(utilityFlows.logoutDefaultUser(parallelExecutionNodes)).alwaysRun()
                .addFlow(utilityFlows.deleteUsers(parallelExecutionUsers, PROFILE_REAL_NODE.equals(profile.toLowerCase()))).alwaysRun();
    }

    /**
     * Meant to be overridden by child classes if more specific DataSources are needed.
     */
    private void setupSpecificDataSource(final String profile) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.debug("{} - Preparing Specific DataSource with [{} --> {}] profile", methodName, profile, profile.toLowerCase());
        final TestDataSource<DataRecord> issueDataSource = fromTafDataProvider("issueDataSource");
        context.addDataSource(CERT_ISSUE_REISSUE_DATASOURCE, issueDataSource);
        final TestDataSource<DataRecord> ldapsDataSource = fromTafDataProvider("ldapsDataSource");
        context.addDataSource(LdapMngTestStep.DataSource.LDAP_DATASOURCE, ldapsDataSource);
        printDatasourceHelper.setSeparateUpperOutput(true).setSeparateLowerOutput(false).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                .printDataSource(CERT_ISSUE_REISSUE_DATASOURCE, "Issue/Reissue DataSource for Nodes", false);
        printDatasourceHelper.setSeparateUpperOutput(false).setSeparateLowerOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                .printDataSource(LdapMngTestStep.DataSource.LDAP_DATASOURCE, "LDAP DataSource for Nodes");

        if (PROFILE_REAL_NODE.equals(profile.toLowerCase())) {
            final TestDataSource<DataRecord> availableUser = fromTafDataProvider("availableUsers");
            context.addDataSource(AVAILABLE_USERS, availableUser);
            printDatasourceHelper.setSeparateUpperOutput(true).setSeparateLowerOutput(false).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                    .printDataSource(AVAILABLE_USERS, "Available DataSource DataSource for " + PROFILE_REAL_NODE + " profile.");
        }
    }

    /**
     * Meant to be overridden by child classes if more specific DataSources are needed.
     */
    protected void teardownSpecificDataSource() {
    }

    /**
     * Method to fill in users and nodes datasources depending on the current profile.
     */
    private void standardDataSourceFromProfileConfiguration(final String profile, final String customNodeDatasource) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String nodeDatasourceName = customNodeDatasource == null || customNodeDatasource.isEmpty()
                ? NODES_TO_ADD : customNodeDatasource;
        final String nodesToAddFromDataprovider =
                DataHandler.getConfiguration().getProperty(String.format("dataprovider.%s.name", nodeDatasourceName), "---", String.class);
        final String tdmAppAddress =
                DataHandler.getConfiguration().getProperty("tdm.api.host", "Not Configured", String.class);
        if (FORCE_NODE_CREATION) {
            DataHandler.getConfiguration().setProperty("netsim.forced.change.ip", true);
        }
        LOGGER.debug("{} - Preparing Node DataSource\n\tSelected Profile: [{} --> {}]\n\tForce Node Creation --> {}"
                + "\n\tNode DataSource Name -> {} [{}] ({})\n\tTDM app Address: {}",
                methodName, profile.isEmpty() ? "'No Profile'" : profile, profile.toLowerCase(),
                FORCE_NODE_CREATION, nodeDatasourceName.isEmpty() ? "---" : nodeDatasourceName,
                nodesToAddFromDataprovider.isEmpty() ? "---" : nodesToAddFromDataprovider,
                customNodeDatasource.isEmpty() ? "---" : customNodeDatasource, tdmAppAddress);
        switch (profile.toLowerCase()) {
            case PROFILE_MAINTRACK:
                createNodesToAdd(nodeDatasourceName, true, false);
                break;
            case PROFILE_TDM_INFO:
            case PROFILE_HYDRA:
            case PROFILE_REAL_NODE:
                createNodesToAdd(nodeDatasourceName, false, FORCE_NODE_CREATION);
                break;
            default:
                createNodesToAdd(nodeDatasourceName, false, true);
                break;
        }
        int numOfRowint = Iterables.size(context.dataSource(NODES_TO_ADD));
        numOfRowint = (numOfRowint != 0) ? numOfRowint : 1;
        setNumberOfNodes(numOfRowint);
    }

    private void createNodesToAdd(final String dataSourceName, final boolean isMT, final boolean isKGB) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        TestDataSource<DataRecord> file = copy(fromTafDataProvider(dataSourceName));

        // if we're in KGB environment, node address changes each run, so we need to update them fron NetSim
        if (isKGB) {
            LOGGER.trace("{} - isKGB flag 'true': updating nodesToAdd from NetSim", methodName);
            file = TafDataSources.transform(file, NetsimDataProvider.updateDataSourceFromNetsim());
        }
        printDatasourceHelper.setSeparateUpperOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.DEBUG)
                .printDataSource(file, String.format("Local DataSource File (%s)", dataSourceName), false);

        /* Merging with Maintrack File */
        TestDataSource<DataRecord> nodesListReadFromDataProvider = null;
        if (isMT) {
            final HashMap configuration = new HashMap();
            configuration.put(CLASS_FIELD, "com.ericsson.oss.testware.network.operators.netsim.NetsimDataProvider");
            configuration.put("nodes.maintrack.id", getSuiteName());
            final MapSource configurationSource = new MapSource(configuration);
            try {
                nodesListReadFromDataProvider = TafDataSourceFactory.dataSourceOfType(CLASS_FIELD, configurationSource, DataPoolStrategy.STOP_ON_END);
                printDatasourceHelper.setSeparateUpperOutput(false).setSeparateLowerOutput(false)
                        .printDataSource(nodesListReadFromDataProvider, "Maint Track DataSource Items",false);
            } catch (final UnknownDataSourceTypeException e) {
                e.printStackTrace();
            }
            file = merge(file, nodesListReadFromDataProvider);
            printDatasourceHelper.setSeparateUpperOutput(false).printDataSource(file, "Local DataSource File merged with MT DataSource", false);
        }

        /* Filtering with MVAL Predicate */
        LOGGER.debug(String.format(LOGGER_INFO_PARAM_ONLY, "Local File filtered by 'MVEL'", getFilterMval()));
        file = TafDataSources.filter(file, mvelFilter(getFilterMval()));
        Assertions.assertThat(0)
                .as("local File filered with 'MVEL' failure (empty)").isNotEqualTo(Iterables.size(file));
        printDatasourceHelper.setSeparateUpperOutput(false).printDataSource(file, String.format("Local DataSource Filtered with MVAL"), false);

        /* Filtering With SUITE Name */
        Iterable<DataRecord> nodesFiltered;
        nodesFiltered = filter(file, Predicates.suiteNamePredicate(getSuiteName()));
        Assertions.assertThat(0)
                .as("Local File filtered by 'SUITE NAME' (empty)").isNotEqualTo(Iterables.size(nodesFiltered));
        printDatasourceHelper.printDataSource(nodesFiltered, String.format("Local DataSource Filtered with Suite Name --> %s", getSuiteName()),
                false);

        /* Filtering With 'correct' NodeType */
        nodesFiltered = filter(nodesFiltered, Predicates.nodeType(NODE_TYPES));
        Assertions.assertThat(0)
                .as("Local File filtered by 'NODETYPES' failure (empty)").isNotEqualTo(Iterables.size(nodesFiltered));
        printDatasourceHelper.printDataSource(nodesFiltered, String.format("Local DataSource Filtered with Node Type --> '%s'", NODE_TYPES), false);
        final int nodeSize = Iterables.size(nodesFiltered);
        SetupAndTearDownUtil.removeAndCreateTestDataSource(NODES_TO_ADD, nodesFiltered);
        System.setProperty(UsersToCreateTimeStampDataSource.NUM_OF_NODES, String.valueOf(nodeSize));
        printDatasourceHelper.setSeparateLowerOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                .setShowRecordCount(true).printDataSource(NODES_TO_ADD, "Nodes To Add DataSource with IP Updated (Context DataSource)");
    }

    /**
     * Method to fill in users and nodes datasources depending on the current profile.
     */
    private void realignNodeDataSource(final String profile) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.debug("{} - Realign Nodes DataSource with [{} --> {}] profile", methodName, profile, profile.toLowerCase());
        switch (profile.toLowerCase()) {
            case PROFILE_TDM_INFO:
            case PROFILE_HYDRA:
            case PROFILE_REAL_NODE:
                if (!context.doesDataSourceExist(SYNCED_NODES)
                        || Iterables.size(context.dataSource(SYNCED_NODES)) == 0) {
                    context.addDataSource(SYNCED_NODES, context.dataSource(NODES_TO_ADD));
                }
                if (!context.doesDataSourceExist(ADDED_NODES)
                        || Iterables.size(context.dataSource(ADDED_NODES)) == 0) {
                    context.addDataSource(ADDED_NODES, context.dataSource(NODES_TO_ADD));
                }
                break;
            default:
                break;
        }
        printDatasourceHelper.setSeparateUpperOutput(true).setSeparateLowerOutput(false).setLogLevel(PrintDatasourceHelper.LoggerLevel.DEBUG)
                .setShowRecordCount(true).printDataSource(NODES_TO_ADD, "Nodes To Add DataSource");
        printDatasourceHelper.setSeparateUpperOutput(false).setSeparateLowerOutput(false).setLogLevel(PrintDatasourceHelper.LoggerLevel.DEBUG)
                .setShowRecordCount(true).printDataSource(ADDED_NODES, "Added Nodes DataSource");
        printDatasourceHelper.setSeparateUpperOutput(false).setSeparateLowerOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.DEBUG)
                .setShowRecordCount(true).printDataSource(SYNCED_NODES, "Synced Nodes DataSource");
    }

    private void realignUserDataSource(final String profile) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.debug("{} - Realign Users DataSource with [{} --> {}] profile", methodName, profile, profile.toLowerCase());
        final int numUsers;
        switch (profile.toLowerCase()) {
            case PROFILE_REAL_NODE:
                final TestDataSource<DataRecord> avilableList = copy(fromTafDataProvider(AVAILABLE_USERS));
                numUsers = Iterables.size(avilableList);
                break;
            default:
                final TestDataSource<DataRecord> userList = copy(fromTafDataProvider(USERS_TO_CREATE));
                numUsers = Iterables.size(userList);
                context.addDataSource(USERS_TO_CREATE, shared(userList));
                context.addDataSource(USERS_TO_DELETE, context.dataSource(USERS_TO_CREATE));
                break;
        }
        setNumberOfUsers(numUsers);
        printDatasourceHelper.setSeparateUpperOutput(true).setSeparateLowerOutput(false).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                .printDataSource(USERS_TO_CREATE, "User To Create DataSource");
        printDatasourceHelper.setSeparateUpperOutput(false).setSeparateLowerOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                .printDataSource(USERS_TO_DELETE, "User To delete DataSource");
    }

    /**
     * <pre>
     * <b>Name</b>: onBeforeSuite            <i>[public]</i>
     * <b>Description</b>: Wrap method for setup operation.
     * </pre>
     *
     * @param suiteNodeTypes
     *         List of nodes type used in Suite Execution [optional]
     * @param nodesCustomDatasource
     *         Custom nodeDatasource name from suite XML file
     * @param suiteTeamName
     *         Suite Team Name to use
     * @param suiteContext
     *         Object with information for testware execution.
     */
    @BeforeSuite(groups = { "Functional", "NSS", "RFA250", "ARNL", "ENM_EXTERNAL_TESTWARE", "KGB" })
    @Parameters({ "SuiteNodeTypes", "datasource", "suiteTeamName"})
    public void onBeforeSuite(@Optional("") final String suiteNodeTypes, @Optional("") final String nodesCustomDatasource,
            @Optional("") final String suiteTeamName, final ITestContext suiteContext) {
        // Create mVEL filter (if suiteNodeTypes not empty)
        LOGGER.trace("Checking if present Node list from XML file --> '{}' [{}]", suiteNodeTypes, !suiteNodeTypes.isEmpty());
        if (!suiteNodeTypes.isEmpty()) {
            LOGGER.trace("Suite Node Type Not Empty...");
            createFilterProperty(suiteNodeTypes);
        }
        onBeforeSuiteMethod(suiteContext, nodesCustomDatasource, suiteTeamName);
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    @AfterSuite(alwaysRun = true, groups = { "Functional", "NSS", "RFA250", "ARNL", "ENM_EXTERNAL_TESTWARE", "KGB" })
    public void onAfterSuite() {
        onAfterSuiteMethod();
    }
}
