/*
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson  2020
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
import static com.ericsson.cifwk.taf.scenario.TestScenarios.fromTestStepResult;
import static com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep.DataSource.LDAP_DATASOURCE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.nodesecurity.steps.CertificateIssueTestSteps.DataSource.CERT_ISSUE_REISSUE_DATASOURCE;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodecli.operators.teststeps.CredentialMngTestSteps;
import com.ericsson.oss.services.nodecli.operators.teststeps.IssueMngTestSteps;
import com.ericsson.oss.services.nodecli.operators.teststeps.JobIdMonitorMngTestSteps;
import com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep;
import com.ericsson.oss.testware.scenario.PrintDatasourceHelper;

/**
 * <pre>
 * <b>Class Name</b>: UtilityFlow
 * <b>Description</b>: This class contains common streams for node and LDAP and
 * credential management functions..
 * </pre>
 */
public class UtilityFlow extends UtilityFlows {
    @Inject
    private LdapMngTestStep ldapMngTestStep;
    @Inject
    private IssueMngTestSteps issueMngTestSteps;
    @Inject
    private JobIdMonitorMngTestSteps jobIdMonitorMngTestSteps;
    @Inject
    private CredentialMngTestSteps credentialMngTestSteps;

    /**
     * <pre>
     * <b>Name</b>: realignAddedNodes            <i>[public]</i>
     * <b>Description</b>: This method creates the flow builder for the node credential
     * realignment operation.
     * </pre>
     * @return a Test Step Flow Builder.
     */
    public TestStepFlowBuilder realignAddedNodes() {
        return flow("UtilityFlow")
                .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.CRED_GET))
                .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.NODE_CRED_GET)
                        .withParameter(ADDED_NODES, fromTestStepResult(CredentialMngTestSteps.StepIds.CRED_GET)))
                .addTestStep(annotatedMethod(ldapMngTestStep, LdapMngTestStep.StepIds.LDAP_GET_ADMINISTRATIVESTATE)
                        .withParameter(ADDED_NODES, fromTestStepResult(CredentialMngTestSteps.StepIds.NODE_CRED_GET)))
                .addTestStep(annotatedMethod(ldapMngTestStep, LdapMngTestStep.StepIds.LDAP_GET)
                        .withParameter(ADDED_NODES, fromTestStepResult(LdapMngTestStep.StepIds.LDAP_GET_ADMINISTRATIVESTATE)))
/*                .addTestStep(annotatedMethod(issueMngTestSteps, IssueMngTestSteps.StepIds.CERTIFICATE_ISSUE_BASE)
                        .withParameter(ADDED_NODES, fromTestStepResult(LdapMngTestStep.StepIds.LDAP_GET)))
                .addTestStep(annotatedMethod(jobIdMonitorMngTestSteps, JobIdMonitorMngTestSteps.JOB_ID_MONITOR)
                        .withParameter(ADDED_NODES, fromTestStepResult(IssueMngTestSteps.StepIds.CERTIFICATE_ISSUE_BASE)))
                .addTestStep(annotatedMethod(ldapMngTestStep, LdapMngTestStep.StepIds.LDAP_BASE)
                        .withParameter(ADDED_NODES, fromTestStepResult(JobIdMonitorMngTestSteps.JOB_ID_MONITOR)))
                .addTestStep(annotatedMethod(jobIdMonitorMngTestSteps, JobIdMonitorMngTestSteps.JOB_ID_MONITOR)
                        .withParameter(ADDED_NODES, fromTestStepResult(LdapMngTestStep.StepIds.LDAP_BASE)))
                .withDataSources(dataSource(CERT_ISSUE_REISSUE_DATASOURCE), dataSource(LDAP_DATASOURCE))*/;
    }

    /**
     * <pre>
     * <b>Name</b>: printDataSource            <i>[public]</i>
     * <b>Description</b>: This runnable function can be use to show DataSource Content
     *   selected by input parameter.
     * </pre>
     *
     * @param dataSourceName Context DataSource Name
     * @param description Text to add to Print DataSource Logger
     * @param loggerLevel Text to add to Print DataSource Logger
     * @return runnable object for DataSource Print
     */
    public static Runnable printDataSource(final String dataSourceName, final String description,
            final PrintDatasourceHelper.LoggerLevel loggerLevel) {
        return new Runnable() {
            @Override
            public void run() {
                final PrintDatasourceHelper localDataSource = new PrintDatasourceHelper();
                localDataSource.setShowFieldCount(true).setShowFieldList(true).setShowRecordCount(true).setSeparateOutput(true).setLogLevel(
                        PrintDatasourceHelper.LoggerLevel.INFO);
                localDataSource.printDataSource(TafTestContext.getContext().dataSource(dataSourceName),
                        description);
            }
        };
    }
}
