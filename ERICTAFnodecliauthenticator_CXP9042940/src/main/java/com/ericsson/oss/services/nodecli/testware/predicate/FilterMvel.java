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

package com.ericsson.oss.services.nodecli.testware.predicate;

import static com.ericsson.oss.services.nodecli.testware.datasource.DatasourcesNames.INPUT_DATASOURCE;

import com.ericsson.oss.services.nodecli.operators.teststeps.LdapMngTestStep;
import com.ericsson.oss.services.nodecli.operators.utility.UserCredentialType;

/**
 * <pre>
 * <b>Name</b>: FilterMvel      <i>[public (Class)]</i>
 * <b>Description</b>: This class contains mVEL language sorts to make filters
 * on nodes and users.
 * </pre>
 */
public final class FilterMvel {
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String LDAPMANAGED = String.format("%s == '%s'", "ldapManaged", "true");
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String SETAUTHENTICATION = String.format("%s != ''", LdapMngTestStep.Param.AUTHENTICATIONFDN);
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String SETLDAP = String.format("%s != ''", LdapMngTestStep.Param.LDAP_FDN);
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String NETWORKELEMENTID = String.format("%s == %s", "networkElementId", INPUT_DATASOURCE + ".networkElementId");
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String SECURE_USER = String.format("%s == '%s'",
            UserCredentialType.NODECLI_USER.getUserNameField(), UserCredentialType.NODECLI_USER.getDefaultUser());
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String NODECLI_USER = String.format("%s != '%s'",
                    UserCredentialType.NODECLI_USER.getUserNameField(), UserCredentialType.NODECLI_USER.getDefaultUser());

    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String OR_OPERATOR = " || ";
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String AND_OPERATOR = " && ";

    private FilterMvel() { }
}
