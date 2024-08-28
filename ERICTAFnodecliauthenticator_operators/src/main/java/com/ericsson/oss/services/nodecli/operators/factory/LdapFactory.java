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

package com.ericsson.oss.services.nodecli.operators.factory;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.oss.testware.nodesecurity.operators.factory.CredentialFactory;

/**
 * <pre>
 * <b>Class Name</b>: LdapFactory
 * <b>Description</b>: TThis class is used for configuring the LDAP function.
 * </pre>
 */
public final class LdapFactory {
    @Inject
    private CredentialFactory credentialFactory;

    /**
     * <pre>
     * <b>Name</b>: ldapConfigure            <i>[public]</i>
     * <b>Description</b>: The method is used for creating the 'secadm' command
     * for configuring the LDAP function.
     * </pre>
     *
     * @param value ldapConfigure Value
     * @return the command string
     */
    public String ldapConfigure(final DataRecord value) {
        return String.format(Commands.LDAP_CONFIGURE_COMMAND, credentialFactory.getTargetFileCmd(value)
        );
    }

    private static final class Commands {
        static final String LDAP_CONFIGURE_COMMAND = "secadm ldap configure -xf %s";
    }
}
