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

import static com.ericsson.oss.services.nodecli.operators.teststeps.SsoMngTestSteps.StepIds.SSO_GET;
import static com.ericsson.oss.services.nodecli.operators.teststeps.SsoMngTestSteps.StepIds.SSO_SET_VERIFY;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;

import javax.inject.Inject;
import javax.inject.Provider;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.OptionalValue;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.oss.services.nodecli.operators.operators.SsoOperator;
import com.ericsson.oss.services.nodecli.operators.utility.SsoOperation;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.nodesecurity.utils.SecurityUtil;

/**
 * <pre>
 * <b>Class Name</b>: SsoMngTestSteps
 * <b>Description</b>: This class provide Test Step to execute SSO enable/disable
 * operations (and Check).
 * </pre>
 */
public class SsoMngTestSteps extends BaseTestStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(SsoMngTestSteps.class);

    @Inject
    private Provider<SsoOperator> provider;

    /**
     * <pre>
     * <b>Name</b>: setVerifySso      <i>[public]</i>
     * <b>Description</b>: This TestStep perform <u>SSO</u> <i>SET</i> operation and <i>GET</i> <u>SSO</u> status.
     * These are executed steps:
     *  - Set SSO status and check REST response
     *  - Get SSO status and check expected Value
     * </pre>
     *
     * @param value
     *         contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @param ssoValue
     *         Parameter to set/reset SSO function
     * @param expectedMessage
     *         Optional parameter to check SSO command return message
     */
    @TestStep(id = SSO_SET_VERIFY)
    public void setVerifySso(@Input(ADDED_NODES) final DataRecord value, @Input(Parameter.SSO_SET_VERIFY) final String ssoValue,
            @Input(Parameter.SSO_SET_EXP_MSG) @OptionalValue() final String expectedMessage) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        SecurityUtil.checkDataSource(value, ADDED_NODES);
        EnmCliResponse response = null;

        // Execute SSO operation
        switch (ssoValue) {
            case Command.DISABLED:
                response = ssoCommand(value, SsoOperation.DISABLE);
                break;
            case Command.ENABLED:
                response = ssoCommand(value, SsoOperation.ENABLE);
                break;
            default:
                break;
        }

        // check ssoCommand correcy response
        boolean retValOk = containsString(response, "SSO attribute updated successfully");
        LOGGER.debug("{} - Response of set SSO Operation ({}) --> {}\n\t[{}] ", methodName, ssoValue, retValOk, response);
        Assertions.assertThat(retValOk).as(String.format("SSO set failed %s", response != null ? response.toString() : "")).isTrue();

        // Get SSO status and compare with expected one
        response = ssoCommand(value, SsoOperation.GET);
        final String checkGet = (expectedMessage == null) ? ssoValue : expectedMessage;
        LOGGER.debug("{} - Response of get SSO Operation ({}) --> {}\n\tExpected Response --> [{}]\n\tReceived Response --> [{}] ", methodName,
                SsoOperation.GET, retValOk, checkGet, response);
        retValOk = containsString(response, checkGet);
        Assertions.assertThat(retValOk).as(String.format("SSO get failed %s", response.toString())).isTrue();

        LOGGER.info("{} - SSO set  is successful", methodName);
    }

    /**
     * <pre>
     * <b>Name</b>: getSso      <i>[public]</i>
     * <b>Description</b>: This TestStep perform <u>SSO</u> <i>GET</i> operation and return command Result.
     * </pre>
     *
     * @param value
     *         contains the values of {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#ADDED_NODES} DataRecord
     * @param expectedMessage
     *         Optional parameter to check SSO command return message
     *
     * @return - 'EnmCliResponse' for SSO GET command
     */
    @TestStep(id = SSO_GET)
    public EnmCliResponse getSso(@Input(ADDED_NODES) final DataRecord value,
            @Input(Parameter.SSO_SET_EXP_MSG) @OptionalValue() final String expectedMessage) {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        SecurityUtil.checkDataSource(value, ADDED_NODES);

        // Get SSO status and compare with expected one
        final EnmCliResponse response = ssoCommand(value, SsoOperation.GET);
        LOGGER.debug("{} - Response of get SSO Operation ({})\n\tExpected Response --> [{}]\n\tReceived Response --> [{}] ", methodName,
                SsoOperation.GET, expectedMessage, response);
        if (expectedMessage != null) {
            final boolean retValOk = containsString(response, expectedMessage);
            Assertions.assertThat(retValOk).as(String.format("SSO get failed %s", response.toString())).isTrue();
        }
        LOGGER.info("{} - SSO get  is successful", methodName);
        return response;
    }

    // ************************************************************************
    // * Private methods...
    // ************************************************************************

    /**
     * <pre>
     * <b>Name</b>: ssoCommand      <i>[private]</i>
     * <b>Description</b>: This private method execute SSO operation on selected node
     * with selected value..
     * </pre>
     *
     * @param value
     *         DataRecord element with node to use.
     * @param ssoOperation
     *         Operation to perform (enable/disable/get)
     *
     * @return EnmCliResponse command result.
     */
    private EnmCliResponse ssoCommand(final DataRecord value, final SsoOperation ssoOperation) {
        final SsoOperator ssoOperator = provider.get();
        return ssoOperator.executeSsoCommand(value, ssoOperation);
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
        public static final String SSO_SET_VERIFY = "setVerifySSO";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String SSO_GET = "getSSO";

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
    public static final class Parameter {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String SSO_SET_VERIFY = "SsoSetVerify";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String SSO_SET_EXP_MSG = "SsoSetExpMsg";

        private Parameter() {
        }
    }

    /**
     * <pre>
     * <b>Class Name</b>: Command
     * <b>Description</b>: This subclass contains commands to execute with <i>SSO</i>
     * command executor.
     * </pre>
     */
    public static final class Command {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String ENABLED = "ENABLED";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String DISABLED = "DISABLED";

        private Command() {
        }
    }
}
