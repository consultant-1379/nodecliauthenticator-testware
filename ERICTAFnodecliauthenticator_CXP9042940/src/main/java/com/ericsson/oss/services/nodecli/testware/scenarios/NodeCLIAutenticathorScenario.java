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

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.backupNodeCliUser;
import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.compareDataSourceSize;
import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.distributeValues;
import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.nodeCliLoggerConfiguration;
import static com.ericsson.oss.services.nodecli.operators.utility.BasicUtility.nodeCliUserConfiguration;
import static com.ericsson.oss.services.nodecli.operators.utility.DataSourceName.LDAPNODESTOADD;
import static com.ericsson.oss.services.nodecli.testware.constant.Constants.Status.DISABLED;
import static com.ericsson.oss.services.nodecli.testware.constant.Constants.Status.ENABLED;
import static com.ericsson.oss.services.nodecli.testware.datasource.DatasourcesNames.INPUT_DATASOURCE;
import static com.ericsson.oss.services.nodecli.testware.predicate.PredicateExtended.ldapManagedPredicate;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.SYNCED_NODES;
import static com.ericsson.oss.testware.teststeps.EnmLogTestStep.DataSourceName.ENM_LOG_DATASOURCE;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.annotations.TestOptions;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodecli.operators.flows.CmSyncFlows;
import com.ericsson.oss.services.nodecli.operators.flows.CredentialMngFlow;
import com.ericsson.oss.services.nodecli.operators.flows.EnmLogCheckFlow;
import com.ericsson.oss.services.nodecli.operators.flows.LdapMngFlow;
import com.ericsson.oss.services.nodecli.operators.flows.SsoMngFlows;
import com.ericsson.oss.services.nodecli.operators.flows.UiFlows;
import com.ericsson.oss.services.nodecli.operators.flows.UtilityFlow;
import com.ericsson.oss.services.nodecli.operators.teststeps.CredentialMngTestSteps;
import com.ericsson.oss.services.nodecli.operators.teststeps.SsoMngTestSteps;
import com.ericsson.oss.services.nodecli.operators.utility.BasicUtility;
import com.ericsson.oss.services.nodecli.testware.constant.Constants;
import com.ericsson.oss.services.nodecli.testware.predicate.FilterMvel;
import com.ericsson.oss.services.nodecli.testware.predicate.PredicateExtended;
import com.ericsson.oss.testware.scenario.PrintDatasourceHelper;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutRestFlows;
import com.google.common.collect.Iterables;

/**
 * <pre>
 * <b>Class Name</b>: NodeCLIAutenticathorScenario
 * <b>Description</b>: The class contains the test and preparation (before and after)
 *  scenarios for the NodeCliAuthenticator functions.
 * </pre>
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class NodeCLIAutenticathorScenario extends SetupAndTeardownScenario {

    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final int MAX_NODES_PARALLEL_EXECUTION = DataHandler.getConfiguration().getProperty("max.netsim.vUser.number", 4, Integer.class);

    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String TESTCASE_LOG_MESSAGE = "\n{} ---> ### LDAP '{}' -- SSO '{}' ###"
            + "\n\tTest Case ID: {}\n\tTest Case Title: {}";
    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String INPUT_DATASOURCE_DESCRIPTION = "Input DataSource for DataDrivenScenario %s scenario.";
    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String NO_DATARECORD_AVAILABLE_FOR_SCENARIO_MESSAGE = "No DataRecord Available for '%s' scenario [%d/%d].";
    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String MR_SUBSTITUTION_TAG = "<MR_NAME_SUBSTITUTE>";
    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String NOT_NODECLIUSER_TAG = "Not_NodeCliUser";
    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String NODECLIUSER_TAG = "NodeCliUser";
    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String DEFAULT_TAG = "default";

    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String NODETYPE_FILTER_PROPERTYNAME = "nodeCLIAuthentication.nodeTypes";

    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String NODETYPE_FILTERED_DEFAULTVALUE = "EPG,VEPG,EPG-OI,vEPG-OI" + ","
            + "vCSCF,vBGF,vMTAS,vSBG" + ","
            + "RadioNode" + ","
            + "PCC,PCG,SC,CCRC,CCPC,CCES,CCSM,CCDM";
    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String TEAMNAME_DEFAULTVALUE = "Hydra";

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeCLIAutenticathorScenario.class);
    private static final String NODESYNC_AWAIT_SHORT_SEC = DataHandler.getConfiguration().getProperty("nodeCliSync.await.atmost.short", "20",
            String.class);
    private static final String NODESYNC_AWAIT_LONG_SEC = DataHandler.getConfiguration().getProperty("nodeCliSync.await.atmost.long", "40",
            String.class);

    // Injection Section
    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected LoginLogoutRestFlows loginLogoutRestFlows;

    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected UiFlows uiFlows;

    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected SsoMngFlows ssoMngFlows;

    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected LdapMngFlow ldapMngFlow;

    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected CredentialMngFlow credentialMngFlow;

    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected UtilityFlow utilityFlow;

    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected EnmLogCheckFlow logCheckFlow;
    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected CmSyncFlows cmSyncFlows;
    @Inject
    private CredentialMngTestSteps credentialMngTestSteps;

    /**
     * <pre>
     * <b>Name</b>: beforeClass                      <i>public</i>
     * <b>Description</b>: This method, executed before instantiating the class, is used
     *  to read the LDAP capabilities of the nodes and to write them in the
     *  #ADDED_NODES datasource.
     * </pre>
     */
    @BeforeClass(groups = { "Functional", "NSS", "RFA250", "ENM_EXTERNAL_TESTWARE" })
    public void beforeClass() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String className = Thread.currentThread().getStackTrace()[1].getClassName();
        LOGGER.info(" **** Execution of {} method - START **** {}", methodName, SEPARATOR);
        final int size = distributeValues(Iterables.size(context.dataSource(SYNCED_NODES)), MAX_NODES_PARALLEL_EXECUTION);
        backupNodeCliUser(SYNCED_NODES, context.dataSource(SYNCED_NODES));
        printDatasourceHelper.setSeparateUpperOutput(true).setSeparateLowerOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.DEBUG)
                .printDataSource(SYNCED_NODES, "Added Nodes in " + methodName + "method (No LDAP info)");

        // execute operation to get Node Type Capability
        final TestScenario scenario = scenario("Before Class '" + className + "' Scenario ")
                .addFlow(loginLogoutRestFlows.loginDefaultUser())
                .addFlow(cmSyncFlows.getCmSync(NODESYNC_AWAIT_SHORT_SEC)
                        .beforeFlow(BasicUtility.setupDataSource(SYNCED_NODES, false, true, true))
                        .beforeFlow(UtilityFlow.printDataSource(SYNCED_NODES, "Before GET cm sync", PrintDatasourceHelper.LoggerLevel.TRACE))
                        .afterFlow(UtilityFlow.printDataSource(SYNCED_NODES, "After GET cm sync", PrintDatasourceHelper.LoggerLevel.TRACE))
                        .withDataSources(dataSource(SYNCED_NODES).bindTo(ADDED_NODES).allowEmpty())
                        .withExceptionHandler(ScenarioExceptionHandler.LOGONLY))
                .addFlow(flow("Realign Added Nodes Flow")
                        .beforeFlow(BasicUtility.setupDataSource(SYNCED_NODES, false, true, true))
                        .beforeFlow(UtilityFlow.printDataSource(SYNCED_NODES, "Before REALIGN added nodes", PrintDatasourceHelper.LoggerLevel.TRACE))
                        .afterFlow(UtilityFlow.printDataSource(SYNCED_NODES, "After REALIGN added nodes", PrintDatasourceHelper.LoggerLevel.TRACE))
                        .afterFlow(UtilityFlow.setupDataSource(LDAPNODESTOADD, SYNCED_NODES, true, false, false))
                        .addSubFlow(utilityFlow.realignAddedNodes())
                        .withDataSources(dataSource(SYNCED_NODES).bindTo(ADDED_NODES).allowEmpty()))
                .addFlow(loginLogoutRestFlows.logout())
                .withDefaultVusers(size)
                .build();
        final TestScenarioRunner runner = SetupAndTearDownUtil.getScenarioRunner();
        runner.start(scenario);

        printDatasourceHelper.setSeparateUpperOutput(true).setSeparateLowerOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                .printDataSource(SYNCED_NODES, "Added Nodes in " + methodName + "method (With LDAP info)");
        LOGGER.info(" **** Execution of {} method -  END  **** {}", methodName, SEPARATOR);
    }

    /**
     * <pre>
     * <b>Name</b>: afterClass                      <i>public</i>
     * <b>Description</b>: This method, executed at the end of use of this class, is used
     *  to restore original configuration.
     * </pre>
     */
    @AfterClass(alwaysRun = true, groups = { "Functional", "NSS", "RFA250", "ENM_EXTERNAL_TESTWARE" })
    public void afterClass() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        final String className = Thread.currentThread().getStackTrace()[1].getClassName();
        LOGGER.trace("{} - After execution of {} class", methodName, className);

        final int size = distributeValues(Iterables.size(context.dataSource(SYNCED_NODES)), MAX_NODES_PARALLEL_EXECUTION);
        final TestScenario scenario = scenario("After Class '" + className + "' Scenario ")
                .addFlow(loginLogoutRestFlows.loginDefaultUser())
                .addFlow(cmSyncFlows.getCmSync(NODESYNC_AWAIT_SHORT_SEC)
                        .beforeFlow(BasicUtility.setupDataSource(SYNCED_NODES, false, true, true))
                        .withDataSources(dataSource(SYNCED_NODES).withFilter(PredicateExtended.realNodePredicate()).bindTo(ADDED_NODES).allowEmpty())
                        .withExceptionHandler(ScenarioExceptionHandler.LOGONLY))
                .addFlow(credentialMngFlow.deleteNodeCliUserInfo()
                        .beforeFlow(BasicUtility.setupDataSource(SYNCED_NODES, false, true, true))
                        .withDataSources(dataSource(SYNCED_NODES).withFilter(PredicateExtended.realNodePredicate()).bindTo(ADDED_NODES).allowEmpty()))
                .addFlow(credentialMngFlow.updateCredentialLdap(CredentialMngTestSteps.Param.DISABLE)
                        .withDataSources(dataSource(SYNCED_NODES).withFilter(PredicateExtended.realNodePredicate())
                                .withFilter(FilterMvel.SETAUTHENTICATION).bindTo(ADDED_NODES).allowEmpty()))
                .addFlow(ssoMngFlows.setVerifySso(SsoMngTestSteps.Command.DISABLED)
                        .beforeFlow(BasicUtility.setupDataSource(SYNCED_NODES, false, true, true))
                        .withDataSources(dataSource(SYNCED_NODES).withFilter(PredicateExtended.realNodePredicate()).bindTo(ADDED_NODES).allowEmpty()))
                .addFlow(loginLogoutRestFlows.logout())
                .withDefaultVusers(size).build();
        final TestScenarioRunner runner = SetupAndTearDownUtil.getScenarioRunner();
        runner.start(scenario);
    }

    /**
     * <pre>
     * <b>Name</b>: ldapDisabledSooDisabledScenario <i>[public]</i>
     * <b>Description</b>: Scenario with Ldap Disabled and Soo Disabled.
     * </pre>
     */
    @TestOptions(timeout = "1200000")
    @Test(enabled = true, groups = { "Functional", "NSS", "RFA250", "ENM_EXTERNAL_TESTWARE" })
    @TestSuite
    public void ldapDisabledSooDisabledScenario() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info(TESTCASE_LOG_MESSAGE, methodName, DISABLED, DISABLED, getLdapDisabledSooDisabledNormalUserTestId(),
                getLdapDisabledSooDisabledNormalUserTitle().replace(MR_SUBSTITUTION_TAG, Constants.MR_MAP_REFERENCE.get(DEFAULT_TAG)));
        final TestScenario scenario = ldapDisableSooDisable(NOT_NODECLIUSER_TAG);
        final TestScenarioRunner runner = SetupAndTearDownUtil.getScenarioRunner();
        runner.start(scenario);
    }

    /**
     * <pre>
     * <b>Name</b>: ldapDisabledSooDisabledNodeCliUserScenario <i>[public]</i>
     * <b>Description</b>: Scenario with Ldap Disabled and Soo Disabled.
     * </pre>
     */
    @TestOptions(timeout = "1200000")
    @Test(enabled = true, groups = { "Functional", "NSS", "RFA250", "ENM_EXTERNAL_TESTWARE" })
    @TestSuite
    public void ldapDisabledSooDisabledNodeCliUserScenario() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info(TESTCASE_LOG_MESSAGE, methodName, DISABLED, DISABLED, getLdapDisabledSooDisabledNodecliUserTestId(),
                getLdapDisabledSooDisabledNodecliUserTitle().replace(MR_SUBSTITUTION_TAG, Constants.MR_MAP_REFERENCE.get(DEFAULT_TAG)));
        final TestScenario scenario = ldapDisableSooDisable(NODECLIUSER_TAG);
        final TestScenarioRunner runner = SetupAndTearDownUtil.getScenarioRunner();
        runner.start(scenario);
    }

    /**
     * <pre>
     * <b>Name</b>: ldapDisabledSooEnabledScenario <i>[public]</i>
     * <b>Description</b>: Scenario with Ldap Disabled and Soo Enabled.
     * </pre>
     */
    @Test(enabled = true, groups = { "Functional", "NSS", "RFA250", "ENM_EXTERNAL_TESTWARE" })
    @TestSuite
    @TestOptions(timeout = "1200000")
    public void ldapDisabledSooEnabledScenario() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info(TESTCASE_LOG_MESSAGE, methodName, DISABLED, ENABLED,
                getLdapDisabledSooEnabledTestId(),
                        getLdapDisabledSooEnabledTitle().replace(MR_SUBSTITUTION_TAG, Constants.MR_MAP_REFERENCE.get(DEFAULT_TAG)));
        final TestScenario scenario = ldapDisableSooEnable();
        final TestScenarioRunner runner = SetupAndTearDownUtil.getScenarioRunner();
        runner.start(scenario);
    }

    /**
     * <pre>
     * <b>Name</b>: ldapEnabledSooDisabledScenario <i>[public]</i>
     * <b>Description</b>: Scenario with Ldap Enabled and Soo Disabled.
     * </pre>
     */
    @Test(enabled = true, groups = { "Functional", "NSS", "RFA250", "ENM_EXTERNAL_TESTWARE" })
    @TestSuite
    @TestOptions(timeout = "1200000")
    public void ldapEnabledSooDisabledScenario() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info(TESTCASE_LOG_MESSAGE, methodName, ENABLED, DISABLED,
                getLdapEnabledSooDisabledTestId(),
                        getLdapEnabledSooDisabledTitle().replace(MR_SUBSTITUTION_TAG, Constants.MR_MAP_REFERENCE.get(DEFAULT_TAG)));
        final TestScenario scenario = ldapEnableSooDisable();
        final TestScenarioRunner runner = SetupAndTearDownUtil.getScenarioRunner();
        runner.start(scenario);
    }

    /**
     * <pre>
     * <b>Name</b>: ldapEnabledSooEnabledScenario <i>[public]</i>
     * <b>Description</b>: Scenario with Ldap Enabled and Soo Enbled.
     * </pre>
     */
    @Test(enabled = true, groups = { "Functional", "NSS", "RFA250", "ENM_EXTERNAL_TESTWARE" })
    @TestSuite
    @TestOptions(timeout = "1200000")
    public void ldapEnabledSooEnabledScenario() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info(TESTCASE_LOG_MESSAGE, methodName, ENABLED, ENABLED,
                getLdapEnabledSooEnabledTestId(),
                        getLdapEnabledSooEnabledTitle().replace(MR_SUBSTITUTION_TAG, Constants.MR_MAP_REFERENCE.get(DEFAULT_TAG)));
        final TestScenario scenario = ldapEnableSooEnable();
        final TestScenarioRunner runner = SetupAndTearDownUtil.getScenarioRunner();
        runner.start(scenario);
    }

    // --------------------------------------------------------------------------
    //           Implementation of previously used scenarios
    // --------------------------------------------------------------------------
    @SuppressWarnings("checkstyle:JavadocMethod")
    protected TestScenario ldapDisableSooDisable(final String userToUse) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.trace("{} (With '{}') - Original DataSource [1] (Available Users Size --> {}, Added Node Size --> {})",
                methodName, userToUse, Iterables.size(context.dataSource(AVAILABLE_USERS)), Iterables.size(context.dataSource(SYNCED_NODES)));
        Assertions.assertThat(userToUse.toLowerCase())
                .as(String.format("ldapDisableSooDisable - Wrong UserType selected --> %s.",
                        userToUse)).isIn(NOT_NODECLIUSER_TAG.toLowerCase(), NODECLIUSER_TAG.toLowerCase());

        Assertions.assertThat(compareDataSourceSize(AVAILABLE_USERS, SYNCED_NODES))
                .as(String.format("ldapDisableSooDisable - Datasource '%s'(%s) is not '>=' than '%s'(%s).",
                        AVAILABLE_USERS, Iterables.size(context.dataSource(AVAILABLE_USERS)),
                        SYNCED_NODES, Iterables.size(context.dataSource(SYNCED_NODES)))).isTrue();

        final TestDataSource<DataRecord> merged = TafDataSources.merge(context.dataSource(AVAILABLE_USERS), context.dataSource(SYNCED_NODES));
        LOGGER.trace("{} - Before Update DataSource [1] (DataSource Size --> {})",
                methodName, Iterables.size(merged));
        final boolean isNormaluserTest = userToUse == NOT_NODECLIUSER_TAG;
        final String actualTestId = isNormaluserTest ?  getLdapDisabledSooDisabledNormalUserTestId() : getLdapDisabledSooDisabledNodecliUserTestId();
        final String actualTesttitle = isNormaluserTest ?  getLdapDisabledSooDisabledNormalUserTitle() : getLdapDisabledSooDisabledNodecliUserTitle();

        dataDrivenDataSource(INPUT_DATASOURCE, actualTestId, actualTesttitle, merged);
        if (!isNormaluserTest) {
            nodeCliUserConfiguration(INPUT_DATASOURCE, context.dataSource(INPUT_DATASOURCE));
        }
        LOGGER.trace("{} - Added Test Case ID and Test Title [1] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        nodeCliLoggerConfiguration(INPUT_DATASOURCE, context.dataSource(INPUT_DATASOURCE));
        LOGGER.trace("{} - Added Node Cli Logger Configuration [1] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        // Non dovrebbe essere necessario percè vengono recuperate le informazioni e messe nel DataSource, direttamente da ENM
        // nodeCliUserConfiguration(INPUT_DATASOURCE, context.dataSource(INPUT_DATASOURCE));
        LOGGER.trace("{} - Added Node Cli User Configuration [1] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        printDatasourceHelper.setSeparateUpperOutput(true).setSeparateLowerOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                .printDataSource(INPUT_DATASOURCE, String.format(INPUT_DATASOURCE_DESCRIPTION, methodName + " (" + actualTestId + ")"));
        LOGGER.trace(getMvalFilterAppliedMessage(methodName, new String[] {
                FilterMvel.SETAUTHENTICATION, FilterMvel.NETWORKELEMENTID, FilterMvel.AND_OPERATOR}));

        // Take the size of the datasource and adapt it to the maximum execution parallelism (Redistribuite).
        final int dataSourceSize = Iterables.size(context.dataSource(INPUT_DATASOURCE));
        final int dataRercordCount = distributeValues(dataSourceSize, MAX_NODES_PARALLEL_EXECUTION);

        //        Assertions.assertThat(dataRercordCount)
        //                .as(String.format(NO_DATARECORD_AVAILABLE_FOR_SCENARIO_MESSAGE,
        //                        methodName, dataSourceSize, dataRercordCount)).isGreaterThan(0);
        if (dataRercordCount == 0) {
            LOGGER.warn("{} [1]", String.format(NO_DATARECORD_AVAILABLE_FOR_SCENARIO_MESSAGE,
                    methodName, dataSourceSize, dataRercordCount));
        }

        // Create DataDriven scenario and Build it.
        return dataDrivenScenario(actualTestId)
                // Since these SSO and LDAP configuration operations are OOS of our MR and since the expected roles
                // are not sufficient for all nodes, it was decided to perform them with the default user.
                // .addFlow(loginLogoutRestFlows.loginBuilder())
                .addFlow(loginLogoutRestFlows.loginDefaultUser())
                .addFlow(cmSyncFlows.getCmSync(NODESYNC_AWAIT_SHORT_SEC))
                .addFlow(isNormaluserTest
                        ? utilityFlow.emptyFlow("Skip NodeCliUser configuration") : credentialMngFlow.setNodeCliUser())
                .addFlow(isNormaluserTest
                        ? utilityFlow.emptyFlow("Skip NodeCliUser Sync Wait") : cmSyncFlows.getCmSync(NODESYNC_AWAIT_LONG_SEC))
                .addFlow(ldapMngFlow.setAuthenticationDisabled().withDataSources(dataSource(INPUT_DATASOURCE)
                        .withFilter(FilterMvel.NETWORKELEMENTID + FilterMvel.AND_OPERATOR + FilterMvel.SETAUTHENTICATION)
                        .bindTo(ADDED_NODES).allowEmpty()))
                .addFlow(credentialMngFlow.updateCredentialLdap("disable").withDataSources(dataSource(INPUT_DATASOURCE)
                        .withFilter(FilterMvel.NETWORKELEMENTID + FilterMvel.AND_OPERATOR + FilterMvel.SETAUTHENTICATION)
                        .bindTo(ADDED_NODES).allowEmpty()))
                .addFlow(setVerifySsoDisabled())
                .addFlow(flow(String.format("Get Credential Data [1] - %s", actualTestId))
                        .pause(10, TimeUnit.SECONDS)
                        .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.CRED_GET))
                        .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.NODE_CRED_GET)))
                .addFlow(loginLogoutRestFlows.logout())
                .addFlow(uiFlows.searchAndLaunchParallelNodeCLI(dataRercordCount)
                        .pause(15, TimeUnit.SECONDS))
                .addFlow(logCheckFlow.checKLoggedUserFlow())
                .withScenarioDataSources(dataSource(INPUT_DATASOURCE)
                        .bindTo(AVAILABLE_USERS).bindTo(ADDED_NODES).bindTo(ENM_LOG_DATASOURCE).allowEmpty())
                .doParallel(dataRercordCount).build();
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    protected TestScenario ldapDisableSooEnable() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.trace("{} - Original DataSource [2] (Available Users Size --> {}, Added Node Size --> {})",
                methodName, Iterables.size(context.dataSource(AVAILABLE_USERS)), Iterables.size(context.dataSource(SYNCED_NODES)));
        Assertions.assertThat(compareDataSourceSize(AVAILABLE_USERS, SYNCED_NODES))
                .as(String.format("ldapDisableSooEnable - Datasource '%s'(%s) is not '>=' than '%s'(%s).",
                        AVAILABLE_USERS, Iterables.size(context.dataSource(AVAILABLE_USERS)),
                        SYNCED_NODES, Iterables.size(context.dataSource(SYNCED_NODES)))).isTrue();

        final TestDataSource<DataRecord> merged = TafDataSources.merge(context.dataSource(AVAILABLE_USERS), context.dataSource(SYNCED_NODES));
        dataDrivenDataSource(INPUT_DATASOURCE, getLdapDisabledSooEnabledTestId(), getLdapDisabledSooEnabledTitle(), merged);
        LOGGER.trace("{} - Added Test Case ID and Test Title [2] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        nodeCliLoggerConfiguration(INPUT_DATASOURCE, context.dataSource(INPUT_DATASOURCE));
        LOGGER.trace("{} - Added Node Cli Configuration [2] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        LOGGER.trace("{} - Added Node Cli User Configuration [2] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        printDatasourceHelper.setSeparateUpperOutput(true).setSeparateLowerOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                .printDataSource(INPUT_DATASOURCE, String.format(INPUT_DATASOURCE_DESCRIPTION, methodName));
        LOGGER.trace(getMvalFilterAppliedMessage(methodName,
                new String[] {FilterMvel.NETWORKELEMENTID + FilterMvel.AND_OPERATOR + FilterMvel.SETAUTHENTICATION}));

        // Take the size of the datasource and adapt it to the maximum execution parallelism (Redistribuite).
        final int dataSourceSize = Iterables.size(context.dataSource(INPUT_DATASOURCE));
        final int dataRercordCount = distributeValues(dataSourceSize, MAX_NODES_PARALLEL_EXECUTION);

        //        Assertions.assertThat(dataRercordCount)
        //                .as(String.format(NO_DATARECORD_AVAILABLE_FOR_SCENARIO_MESSAGE,
        //                        methodName, dataSourceSize, dataRercordCount)).isGreaterThan(0);
        if (dataRercordCount == 0) {
            LOGGER.warn("{} [2]", String.format(NO_DATARECORD_AVAILABLE_FOR_SCENARIO_MESSAGE,
                    methodName, dataSourceSize, dataRercordCount));
        }

        // Create DataDriven scenario and Build it.
        return dataDrivenScenario(getLdapDisabledSooEnabledTestId())
                // Since these SSO and LDAP configuration operations are OOS of our MR and since the expected roles
                // are not sufficient for all nodes, it was decided to perform them with the default user.
                // .addFlow(loginLogoutRestFlows.loginBuilder())
                .addFlow(loginLogoutRestFlows.loginDefaultUser())
                .addFlow(cmSyncFlows.getCmSync(NODESYNC_AWAIT_SHORT_SEC))
                .addFlow(ldapMngFlow.setAuthenticationDisabled().withDataSources(dataSource(SYNCED_NODES)
                        .withFilter(FilterMvel.NETWORKELEMENTID + FilterMvel.AND_OPERATOR + FilterMvel.SETAUTHENTICATION)
                        .bindTo(ADDED_NODES).allowEmpty()))
                .addFlow(credentialMngFlow.updateCredentialLdap("disable").withDataSources(dataSource(SYNCED_NODES)
                        .withFilter(FilterMvel.NETWORKELEMENTID + FilterMvel.AND_OPERATOR + FilterMvel.SETAUTHENTICATION)
                        .bindTo(ADDED_NODES).allowEmpty()))
                .addFlow(setVerifySsoEnabled())
                .addFlow(flow(String.format("Get Credential Data [2] - %s", getLdapDisabledSooEnabledTestId()))
                        .pause(10, TimeUnit.SECONDS)
                        .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.CRED_GET))
                        .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.NODE_CRED_GET)))
                .addFlow(loginLogoutRestFlows.logoutBuilder())
                .addFlow(uiFlows.searchAndLaunchParallelNodeCLI(dataRercordCount)
                        .pause(15, TimeUnit.SECONDS))
                .addFlow(logCheckFlow.checKLoggedUserFlow())
                .withScenarioDataSources(dataSource(INPUT_DATASOURCE)
                        .bindTo(AVAILABLE_USERS).bindTo(ADDED_NODES).bindTo(ENM_LOG_DATASOURCE).allowEmpty())
                .doParallel(dataRercordCount).build();
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    protected TestScenario ldapEnableSooDisable() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.trace("{} - Original DataSource [3] (Available Users Size --> {}, Added Node Size --> {})",
                methodName, Iterables.size(context.dataSource(AVAILABLE_USERS)), Iterables.size(context.dataSource(SYNCED_NODES)));
        Assertions.assertThat(compareDataSourceSize(AVAILABLE_USERS, SYNCED_NODES))
                .as(String.format("ldapEnableSooDisable - Datasource '%s'(%s) is not '>=' than '%s'(%s).",
                        AVAILABLE_USERS, Iterables.size(context.dataSource(AVAILABLE_USERS)),
                        SYNCED_NODES, Iterables.size(context.dataSource(SYNCED_NODES)))).isTrue();

        final TestDataSource<DataRecord> merged = TafDataSources.merge(context.dataSource(AVAILABLE_USERS), context.dataSource(SYNCED_NODES));
        LOGGER.trace("{} - Before Update DataSource [3] (DataSource Size --> {})", methodName, Iterables.size(merged));

        dataDrivenDataSource(INPUT_DATASOURCE, getLdapEnabledSooDisabledTestId(), getLdapEnabledSooDisabledTitle(), merged);
        LOGGER.trace("{} - Added Test Case ID and Test Title [3] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        nodeCliLoggerConfiguration(INPUT_DATASOURCE, context.dataSource(INPUT_DATASOURCE));
        LOGGER.trace("{} - Added Node Cli Configuration [3] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        LOGGER.trace("{} - Added Node Cli User Configuration [3] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        printDatasourceHelper.setSeparateUpperOutput(true).setSeparateLowerOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                .printDataSource(INPUT_DATASOURCE, String.format(INPUT_DATASOURCE_DESCRIPTION, methodName));
        LOGGER.trace(getMvalFilterAppliedMessage(methodName, new String[] {FilterMvel.NETWORKELEMENTID}));

        // Take the size of the datasource and adapt it to the maximum execution parallelism (Redistribuite).
        final int dataSourceSize = Iterables.size(context.dataSource(INPUT_DATASOURCE));
        final int dataRercordCount = distributeValues(dataSourceSize, MAX_NODES_PARALLEL_EXECUTION);

        //        Assertions.assertThat(dataRercordCount)
        //                .as(String.format(NO_DATARECORD_AVAILABLE_FOR_SCENARIO_MESSAGE,
        //                        methodName, dataSourceSize, dataRercordCount)).isGreaterThan(0);
        if (dataRercordCount == 0) {
            LOGGER.warn("{} [3]", String.format(NO_DATARECORD_AVAILABLE_FOR_SCENARIO_MESSAGE,
                    methodName, dataSourceSize, dataRercordCount));
        }

        // Create DataDriven scenario and Build it.
        return dataDrivenScenario(getLdapEnabledSooDisabledTestId())
                // Since these SSO and LDAP configuration operations are OOS of our MR and since the expected roles
                // are not sufficient for all nodes, it was decided to perform them with the default user.
                // .addFlow(loginLogoutRestFlows.loginBuilder())
                .addFlow(loginLogoutRestFlows.loginDefaultUser())
                .addFlow(cmSyncFlows.getCmSync(NODESYNC_AWAIT_SHORT_SEC))
                .addFlow(ldapMngFlow.setAuthenticationEnabled()
                        .withDataSources(dataSource(SYNCED_NODES).withFilter(FilterMvel.NETWORKELEMENTID).bindTo(ADDED_NODES)))
                .addFlow(credentialMngFlow.updateCredentialLdap("enable")
                        .withDataSources(dataSource(SYNCED_NODES).withFilter(FilterMvel.NETWORKELEMENTID).bindTo(ADDED_NODES)))
                .addFlow(setVerifySsoDisabled())
                .addFlow(flow(String.format("Get Credential Data [3] - %s", getLdapEnabledSooDisabledTestId()))
                        .pause(10, TimeUnit.SECONDS)
                        .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.CRED_GET))
                        .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.NODE_CRED_GET)))
                .addFlow(loginLogoutRestFlows.logout())
                .addFlow(uiFlows.searchAndLaunchParallelNodeCLI(dataRercordCount)
                        .pause(15, TimeUnit.SECONDS))
                .addFlow(logCheckFlow.checKLoggedUserFlow())
                .withScenarioDataSources(
                        dataSource(INPUT_DATASOURCE).withFilter(ldapManagedPredicate())
                                .bindTo(AVAILABLE_USERS).bindTo(ADDED_NODES).bindTo(ENM_LOG_DATASOURCE).allowEmpty())
                .doParallel(dataRercordCount).build();
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    protected TestScenario ldapEnableSooEnable() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.trace("{} - Original DataSource [4] (Available Users Size --> {}, Added Node Size --> {})",
                methodName, Iterables.size(context.dataSource(AVAILABLE_USERS)), Iterables.size(context.dataSource(SYNCED_NODES)));
        Assertions.assertThat(compareDataSourceSize(AVAILABLE_USERS, SYNCED_NODES))
                .as(String.format("ldapEnableSooEnable - Datasource '%s'(%s) is not '>=' than '%s'(%s).",
                        AVAILABLE_USERS, Iterables.size(context.dataSource(AVAILABLE_USERS)),
                        SYNCED_NODES, Iterables.size(context.dataSource(SYNCED_NODES)))).isTrue();

        final TestDataSource<DataRecord> merged = TafDataSources.merge(context.dataSource(AVAILABLE_USERS), context.dataSource(SYNCED_NODES));
        LOGGER.trace("{} - Before Update DataSource [4] (DataSource Size --> {})",
                methodName, Iterables.size(merged));
        dataDrivenDataSource(INPUT_DATASOURCE, getLdapEnabledSooEnabledTestId(), getLdapEnabledSooEnabledTitle(), merged);
        LOGGER.trace("{} - Added Test Case ID and Test Title [4] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        nodeCliLoggerConfiguration(INPUT_DATASOURCE, context.dataSource(INPUT_DATASOURCE));
        LOGGER.trace("{} - Added Node Cli Configuration [4] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        LOGGER.trace("{} - Added Node Cli User Configuration [4] (DataSource Size --> {})",
                methodName, Iterables.size(context.dataSource(INPUT_DATASOURCE)));
        printDatasourceHelper.setSeparateUpperOutput(true).setSeparateLowerOutput(true).setLogLevel(PrintDatasourceHelper.LoggerLevel.INFO)
                .printDataSource(INPUT_DATASOURCE, String.format(INPUT_DATASOURCE_DESCRIPTION, methodName));
        LOGGER.trace(getMvalFilterAppliedMessage(methodName, new String[] {FilterMvel.NETWORKELEMENTID}));

        // Take the size of the datasource and adapt it to the maximum execution parallelism (Redistribuite).
        final int dataSourceSize = Iterables.size(context.dataSource(INPUT_DATASOURCE));
        final int dataRercordCount = distributeValues(dataSourceSize, MAX_NODES_PARALLEL_EXECUTION);

        //        Assertions.assertThat(dataRercordCount)
        //                .as(String.format(NO_DATARECORD_AVAILABLE_FOR_SCENARIO_MESSAGE,
        //                        methodName, dataSourceSize, dataRercordCount)).isGreaterThan(0);
        if (dataRercordCount == 0) {
            LOGGER.warn("{} [4]", String.format(NO_DATARECORD_AVAILABLE_FOR_SCENARIO_MESSAGE,
                    methodName, dataSourceSize, dataRercordCount));
        }

        // Create DataDriven scenario and Build it.
        return dataDrivenScenario(getLdapEnabledSooEnabledTestId())
                // Since these SSO and LDAP configuration operations are OOS of our MR and since the expected roles
                // are not sufficient for all nodes, it was decided to perform them with the default user.
                // .addFlow(loginLogoutRestFlows.loginBuilder())
                .addFlow(loginLogoutRestFlows.loginDefaultUser())
                .addFlow(cmSyncFlows.getCmSync(NODESYNC_AWAIT_SHORT_SEC))
                .addFlow(ldapMngFlow.setAuthenticationEnabled()
                        .withDataSources(dataSource(SYNCED_NODES).withFilter(FilterMvel.NETWORKELEMENTID)))
                .addFlow(credentialMngFlow.updateCredentialLdap("enable")
                        .withDataSources(dataSource(SYNCED_NODES).withFilter(FilterMvel.NETWORKELEMENTID)))
                .addFlow(setVerifySsoEnabled())
                .addFlow(flow(String.format("Get Credential Data [4] - %s", getLdapDisabledSooEnabledTestId()))
                        .pause(1, TimeUnit.SECONDS)
                        .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.CRED_GET))
                        .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.NODE_CRED_GET)))
                .addFlow(loginLogoutRestFlows.logout())
                .addFlow(uiFlows.searchAndLaunchParallelNodeCLI(dataRercordCount)
                        .pause(15, TimeUnit.SECONDS))
                .addFlow(logCheckFlow.checKLoggedUserFlow())
                .withScenarioDataSources(dataSource(INPUT_DATASOURCE).withFilter(ldapManagedPredicate())
                        .bindTo(AVAILABLE_USERS).bindTo(ADDED_NODES).bindTo(ENM_LOG_DATASOURCE).allowEmpty())
                .doParallel(dataRercordCount).build();
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    protected TestStepFlowBuilder setVerifySsoDisabled() {
        return ssoMngFlows.setVerifySso(SsoMngTestSteps.Command.DISABLED);
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    protected TestStepFlowBuilder setVerifySsoEnabled() {
        return ssoMngFlows.setVerifySso(SsoMngTestSteps.Command.ENABLED);
    }

    // ------------------------------------------------------------------------
    // Functions to get TestId and Title for Scenarios.
    // ------------------------------------------------------------------------

    /**
     * <pre>
     * <b>Name</b>: getLdapDisabledSooDisabledSecureUserTestId       <i>[public]</i>
     * <b>Description</b>: This method return 'Test Case ID' for
     *    LDAP <u>disable</u> SSO <u>disable</u> scenario with 'SecureUser' <i>NodeCli</i> Login,
     *    as stored in TMS web application.
     * </pre>
     * @return TestId value
     */
    protected String getLdapDisabledSooDisabledNormalUserTestId() {
        return "TC0001_1 LdapDisableSsoDisable";
    }

    /**
     * <pre>
     * <b>Name</b>: getLdapDisabledSooDisabledSecureUserTitle       <i>[public]</i>
     * <b>Description</b>: SThis method return 'Test Case Title' for
     *    LDAP <u>disable</u> SSO <u>disable</u> scenario with 'SecureUser' <i>NodeCli</i> Login,
     *    as stored in TMS web application.
     * </pre>
     * @return Title value
     */
    protected String getLdapDisabledSooDisabledNormalUserTitle() {
        return MR_SUBSTITUTION_TAG + "Launch Node CLI with: LDAP --> Disable, SSO --> Disable";
    }

    /**
     * <pre>
     * <b>Name</b>: getLdapDisabledSooDisabledNodecliUserTestId       <i>[public]</i>
     * <b>Description</b>: This method return 'Test Case ID' for
     *    LDAP <u>disable</u> SSO <u>disable</u> scenario with 'NodeCliUser' <i>NodeCli</i> Login,
     *    as stored in TMS web application.
     * </pre>
     * @return TestId value
     */
    protected String getLdapDisabledSooDisabledNodecliUserTestId() {
        return "TC0001_2 LdapDisableSsoDisable NodeCliUser";
    }

    /**
     * <pre>
     * <b>Name</b>: getLdapDisabledSooDisabledNodecliUserTitle       <i>[public]</i>
     * <b>Description</b>: SThis method return 'Test Case Title' for
     *    LDAP <u>disable</u> SSO <u>disable</u> scenario with 'NodeCliUser' <i>NodeCli</i> Login,
     *    as stored in TMS web application.
     * </pre>
     * @return Title value
     */
    protected String getLdapDisabledSooDisabledNodecliUserTitle() {
        return MR_SUBSTITUTION_TAG + "Launch Node CLI with: LDAP --> Disable, SSO --> Disable (With 'NodeCliUser')";
    }

    /**
     * <pre>
     * <b>Name</b>: getLdapDisabledSooDisabledTestId       <i>[public]</i>
     * <b>Description</b>: SThis method return 'Test Case ID' for
     *    LDAP <u>disable</u> SSO <u>enable</u> scenario, as stored in TMS web application.
     * </pre>
     * @return TestId value
     */
    protected String getLdapDisabledSooEnabledTestId() {
        return "TC0002 LdapDisableSsoEnable";
    }

    /**
     * <pre>
     * <b>Name</b>: ggetLdapDisabledSooEnabledTitle       <i>[public]</i>
     * <b>Description</b>: SThis method return 'Test Case Title' for
     *    LDAP <u>disable</u> SSO <u>enable</u> scenario.
     * </pre>
     * @return Title value
     */
    protected String getLdapDisabledSooEnabledTitle() {
        return MR_SUBSTITUTION_TAG + "Launch Node CLI with: LDAP --> Disable, SSO --> Enable.";
    }

    /**
     * <pre>
     * <b>Name</b>: getLdapDisabledSooDisabledTestId       <i>[public]</i>
     * <b>Description</b>: SThis method return 'Test Case ID' for
     *    LDAP <u>enable</u> SSO <u>disable</u> scenario, as stored in TMS web application.
     * </pre>
     * @return TestId value
     */
    protected String getLdapEnabledSooDisabledTestId() {
        return "TC0003 LdapEnableSsoDisable";
    }

    /**
     * <pre>
     * <b>Name</b>: getLdapEnabledSooDisabledTitle       <i>[public]</i>
     * <b>Description</b>: SThis method return 'Test Case Title' for
     *    LDAP <u>enable</u> SSO <u>disable</u> scenario.
     * </pre>
     * @return Title value
     */
    protected String getLdapEnabledSooDisabledTitle() {
        return MR_SUBSTITUTION_TAG + "Launch Node CLI with: LDAP --> Enable, SSO --> Disable.";
    }

    /**
     * <pre>
     * <b>Name</b>: getLdapDisabledSooDisabledTestId       <i>[public]</i>
     * <b>Description</b>: SThis method return 'Test Case ID' for
     *    LDAP <u>enable</u> SSO <u>enable</u> scenario, as stored in TMS web application.
     * </pre>
     * @return TestId value
     */
    protected String getLdapEnabledSooEnabledTestId() {
        return "TC0004 LdapEnableSsoEnable";
    }

    /**
     * <pre>
     * <b>Name</b>: getLdapEnabledSooDisabledTitle       <i>[public]</i>
     * <b>Description</b>: SThis method return 'Test Case Title' for
     *    LDAP <u>enable</u> SSO <u>enable</u> scenario.
     * </pre>
     * @return Title value
     */
    protected String getLdapEnabledSooEnabledTitle() {
        return MR_SUBSTITUTION_TAG + "Launch Node CLI with: LDAP --> Enable, SSO --> Enable.";
    }

    /**
     * <pre>
     * <b>Name</b>: getMvalFilterAppliedMessage       <i>[protected]</i>
     * <b>Description</b>: SThis method return a String with a message to display in LOG..
     * </pre>
     * @param methodName caller method Name
     * @param mVelFilterList list of mVal predicates
     * @return LOG message to use
     */
    protected String getMvalFilterAppliedMessage(final String methodName, final String... mVelFilterList) {
        final StringBuilder messageToSend = new StringBuilder();
        messageToSend.append(methodName);
        messageToSend.append(" - ");
        messageToSend.append("Filter used in this Scenario:").append(System.lineSeparator());
        int mValCount = 1;
        for (final String singleMval : mVelFilterList) {
            messageToSend.append("\tFilter MVAL ");
            messageToSend.append(mValCount++);
            messageToSend.append(" --> ");
            messageToSend.append("<").append(singleMval).append(">");
        }
        return messageToSend.toString();
    }
}
