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

package com.ericsson.oss.services.nodecli.operators.utility;

/**
 * <pre>
 * <b>Name</b>: UserCredentialType      <i>[public enum]</i>
 * <b>Description</b>: This enumerate contains the definition of the available
 *   UserCredential.
 * </pre>
 */
public enum UserCredentialType {

    /**
     * List of Enumerated Values for this object
     */
    ENM_USER, ROOT_USER, SECURE_USER, NORMAL_USER, NODECLI_USER, LDAP_USER;

    /**
     * <pre>
     * <b>Name</b>: getUserNameField            <i>[public]</i>
     * <b>Description</b>: This GET returns the DataSource field name for the
     *   specified Enumerative UserName.
     * </pre>
     * @return Field UserName for selected ENUM value
     */
    public String getUserNameField() {
        switch (this) {
            case ENM_USER:
                return "username";
            case ROOT_USER:
                return "rootUserName";
            case SECURE_USER:
                return "secureUserName";
            case NORMAL_USER:
                return "normalUserName";
            case NODECLI_USER:
                return "nodeCliUserName";
            case LDAP_USER:
                return "ldapApplicationUserName";
            default:
                return null;
        }
    }

    /**
     * <pre>
     * <b>Name</b>: getUserNameField            <i>[public]</i>
     * <b>Description</b>: This GET returns the DataSource field name for the
     *   specified Enumerative UserPassword.
     * </pre>
     * @return Field UserPassword for selected ENUM value
     */
    public String getUserPasswordField() {
        switch (this) {
            case ENM_USER:
                return "password";
            case ROOT_USER:
                return "rootUserPassword";
            case SECURE_USER:
                return "secureUserPassword";
            case NORMAL_USER:
                return "normalUserPassword";
            case NODECLI_USER:
                return "nodeCliUserPassword";
            case LDAP_USER:
                return "ldapApplicationUserPassword";
            default:
                return null;
        }
    }
    /**
     * <pre>
     * <b>Name</b>: getDefaultUser            <i>[public]</i>
     * <b>Description</b>: This GET returns default user for selected UserType
     * </pre>
     * @return Field UserPassword for selected ENUM value
     */
    public String getDefaultUser() {
        switch (this) {
            case ENM_USER:
                return null;
            case ROOT_USER:
                return null;
            case SECURE_USER:
                return null;
            case NORMAL_USER:
                return null;
            case NODECLI_USER:
                return "Not Configured";
            case LDAP_USER:
                return "ldapApplicationUser";
            default:
                return null;
        }
    }
}
