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
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.services.nodecli.operators.teststeps.CredentialMngTestSteps;
import com.ericsson.oss.services.nodecli.operators.utility.UserCredentialType;
import com.ericsson.oss.testware.nodeintegration.teststeps.NodeIntegrationTestSteps;

/**
 * <pre>
 * <b>Class Name</b>: CredentialMngFlow
 * <b>Description</b>: This class contains Flows for Credential Management operation.
 * </pre>
 */
public class CredentialMngFlow {
    @Inject
    private CredentialMngTestSteps credentialMngTestSteps;
    @Inject
    private NodeIntegrationTestSteps nodeIntegrationTestSteps;

    /**
     * <pre>
     * <b>Name</b>: setSecureUser            <i>[public]</i>
     * <b>Description</b>: This Test Flow is used to modify the credentials for the
     * <i>SecureUser</i> by changing the <u>UserType</u>.
     * </pre>
     *
     * @return Test Flow builder
     */
    public TestStepFlowBuilder setSecureUser() {
        return flow("Set Secure User Info")
                .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.CRED_UPDATE)
                        .withParameter(CredentialMngTestSteps.Param.USER_TYPE, UserCredentialType.SECURE_USER))
                .pause(5, TimeUnit.SECONDS);
    }

    /**
     * <pre>
     * <b>Name</b>: setSecureUser            <i>[public]</i>
     * <b>Description</b>: This Test Flow is used to modify the credentials for the
     * <i>SecureUser</i> by changing the <u>UserType</u>,  <u>UserName</u> and  <u>UserPassword</u>.
     * </pre>
     * @param userName User Name to configure
     * @param userPasswd User Password to configure
     * @return Test Step Flow
     */
    public TestStepFlowBuilder setSecureUser(final String userName, final String userPasswd) {
        return flow("Set Secure User Info")
                .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.CRED_UPDATE)
                        .withParameter(CredentialMngTestSteps.Param.USER_TYPE, UserCredentialType.SECURE_USER)
                        .withParameter(CredentialMngTestSteps.Param.USER, userName)
                        .withParameter(CredentialMngTestSteps.Param.PASSWD, userPasswd))
                .pause(5, TimeUnit.SECONDS);
    }

    /**
     * <pre>
     * <b>Name</b>: setNodeCliUser            <i>[public]</i>
     * <b>Description</b>: This Test Flow is used to modify the credentials for the
     * <i>NodeCliUser</i> by changing the <u>UserType</u>.
     * </pre>
     * @return Test Step Flow
     */
    public TestStepFlowBuilder setNodeCliUser() {
        return flow("Set NodeCli User Info")
                .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.CRED_UPDATE)
                        .withParameter(CredentialMngTestSteps.Param.USER_TYPE, UserCredentialType.NODECLI_USER))
                .pause(5, TimeUnit.SECONDS);
    }

    /**
     * <pre>
     * <b>Name</b>: setNodeCliUser            <i>[public]</i>
     * <b>Description</b>: This Test Flow is used to modify the credentials for the
     * <i>NodeCliUser</i> by changing the <u>UserType</u>,  <u>UserName</u> and  <u>UserPassword</u>.
     * </pre>
     * @param userName User Name to configure
     * @param userPasswd User Password to configure
     * @return - Test Step Flow
     */
    public TestStepFlowBuilder setNodeCliUser(final String userName, final String userPasswd) {
        return flow("Set NodeCli User Info")
                .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.CRED_UPDATE)
                        .withParameter(CredentialMngTestSteps.Param.USER_TYPE, UserCredentialType.NODECLI_USER)
                        .withParameter(CredentialMngTestSteps.Param.USER, userName)
                        .withParameter(CredentialMngTestSteps.Param.PASSWD, userPasswd))
                .pause(5, TimeUnit.SECONDS);
    }

    /**
     * <pre>
     * <b>Name</b>: setNodeCliUser            <i>[public]</i>
     * <b>Description</b>: This Test Flow is used to modify the credentials for the
     * <i>LdapUser</i> by enable/disable it.
     * </pre>
     * @param ldapStatus value to Set for LDAP credentials.
     * @return - TestStep Flow
     */
    public TestStepFlowBuilder updateCredentialLdap(final String ldapStatus) {
        final String ldapParameter = CredentialMngTestSteps.Param.ENABLE.equalsIgnoreCase(ldapStatus)
                ? CredentialMngTestSteps.Param.ENABLE : CredentialMngTestSteps.Param.DISABLE;
        return flow("Update Credential Ldap enable")
                .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.CRED_UPDATE_LDAP)
                        .withParameter(CredentialMngTestSteps.Param.LDAP, ldapParameter))
                .pause(5, TimeUnit.SECONDS);
    }

    /**
     * <pre>
     * <b>Name</b>: deleteNodeCliUserInfo            <i>[public]</i>
     * <b>Description</b>: This Test Flow is used to Delete the NodeCli credentials for the
     * <i>NodeCliUser</i> deleting and then creating them.
     * </pre>
     * @return Test Step Flow
     */
    public TestStepFlowBuilder deleteNodeCliUserInfo() {
        return flow("Delete NodeCli User Info")
                .addTestStep(annotatedMethod(credentialMngTestSteps, CredentialMngTestSteps.StepIds.DELETE_NODECLI_USER));
    }
}
