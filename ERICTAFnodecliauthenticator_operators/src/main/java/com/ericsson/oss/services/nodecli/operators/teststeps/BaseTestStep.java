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

import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.RowCell;
import com.ericsson.oss.services.scriptengine.spi.dtos.RowDto;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;

/**
 * <pre>
 * <b>Class Name</b>: BaseTestStep
 * <b>Description</b>: This abstract class contains methods common for all TestSteps
 * operations.
 * </pre>
 */
public abstract class BaseTestStep {

    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String NETWORKELEMENTID = "networkElementId";
    @SuppressWarnings("checkstyle:JavadocVariable")
    protected static final String NETWORKELEMENTTYPE = "nodeType";

    /**
     * <pre>
     * <b>Name</b>: containsString            <i>[public]</i>
     * <b>Description</b>: This Common method check if any of the AbstractDtos contain
     * the String passed into this method.
     * </pre>
     *
     * @param response EnmCliResponse to check
     * @param entry string to search
     * @return true if 'entry' is in response
     */
    public boolean containsString(final EnmCliResponse response, final String entry) {
        for (final AbstractDto dto : response.getAllDtos()) {
            if (dto instanceof LineDto) {
                final LineDto lineDto = (LineDto) dto;
                if (lineDto.getValue() != null && lineDto.getValue().toLowerCase().contains(entry.toLowerCase())) {
                    return true;
                }
            } else if (dto instanceof RowDto) {
                final RowDto rowDto = (RowDto) dto;
                for (final RowCell cell : rowDto.getElements()) {
                    if (cell.getValue().contains(entry)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
