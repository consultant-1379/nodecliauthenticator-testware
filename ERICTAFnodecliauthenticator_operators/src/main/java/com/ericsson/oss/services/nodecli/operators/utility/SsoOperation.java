/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.nodecli.operators.utility;

/**
 * Enum for SSO Operation.
 */
@SuppressWarnings({ "PMD.LawOfDemeter" })
public enum SsoOperation {
    @SuppressWarnings("checkstyle:JavadocVariable") ENABLE("ENABLE"),
    @SuppressWarnings("checkstyle:JavadocVariable") DISABLE("DISABLE"),

    @SuppressWarnings("checkstyle:JavadocVariable") GET("GET");

    private final String operation;

    SsoOperation(final String operation) {
        this.operation = operation;
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    public static SsoOperation getType(final String name) {
        final SsoOperation[] arr = values();
        final int len = arr.length;
        for (int i = 0; i < len; ++i) {
            final SsoOperation type = arr[i];
            if (type.operation.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant " + name);
    }

    @Override
    public String toString() {
        return operation;
    }

}
