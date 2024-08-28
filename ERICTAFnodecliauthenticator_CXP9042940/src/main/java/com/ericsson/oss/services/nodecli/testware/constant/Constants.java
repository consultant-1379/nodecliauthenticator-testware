/*
 *     * -----------------------------------------------------------------------------
 *     *******************************************************************************
 *     * COPYRIGHT Ericsson 2023
 *     *
 *     * The copyright to the computer program(s) herein is the property of
 *     * Ericsson Inc. The programs may be used and/or copied only with written
 *     * permission from Ericsson Inc. or in accordance with the terms and
 *     * conditions stipulated in the agreement/contract under which the
 *     * program(s) have been supplied.
 *     *******************************************************************************
 *     *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.nodecli.testware.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Set of constants.
 */
@SuppressWarnings({ "checkstyle:HideUtilityClassConstructor", "checkstyle:JavadocType" })
public final class Constants {

    /**
     * <pre>
     * <b>Class Name</b>: MR_MAP_REFERENCE
     * <b>Description</b>: This Static Map is used to have a reference to the MR used by
     *   the testware in relation to the type of node used.
     * </pre>
     */
    public static final Map<String, String> MR_MAP_REFERENCE = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            final String mr61703 = "MR61703 - ";
            final String mr79472 = "MR79472 - ";
            final String mr79471 = "MR79471 - ";
            put("EPG", mr61703);
            put("VEPG", mr61703);
            put("EPG-OI", mr61703);
            put("vEPG-OI", mr61703);
            put("vCSCF", mr61703);
            put("vBGF", mr61703);
            put("vMTAS", mr61703);
            put("vSBG", mr61703);
            put("RadioNode", mr79472);
            put("5GRadioNode", mr79472);
            put("MSRBS_V1", mr79472);
            put("PCC", mr79471);
            put("PCG", mr79471);
            put("SC", mr79471);
            put("CCRC", mr79471);
            put("CCPC", mr79471);
            put("CCES", mr79471);
            put("CCSM", mr79471);
            put("CCDM", mr79471);
            put("default", "");            // Add here all MR for this Testware
        }
    });

    public final class Status {
        // Local Constant definition.
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String ENABLED = "enabled";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String DISABLED = "disabled";

        private Status() {
        }
    }

    public final class Profiles {
        @SuppressWarnings("checkstyle:JavadocVariable")

        public static final String PROFILE_MAINTRACK = "maintrack";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String PROFILE_TDM_INFO = "tdminfo";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String PROFILE_HYDRA = "hydra";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String PROFILE_REAL_NODE = "realnode";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String NO_PROFILE = "noprofile";

        private Profiles() {
        }
    }
}
