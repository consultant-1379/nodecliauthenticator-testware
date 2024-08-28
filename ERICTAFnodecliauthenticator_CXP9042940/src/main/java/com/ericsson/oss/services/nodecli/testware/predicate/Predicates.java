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

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.base.Predicate;

/**
 * <pre>
 * <b>Name</b>: Predicates      <i>[public (Class)]</i>
 * <b>Description</b>: This class contains the 'base' definitions and methods for the
 * predicates used by this testware.
 * </pre>
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class Predicates {
    private static final Logger LOGGER = LoggerFactory.getLogger(Predicates.class);
    private static final String USE_RECORD = "Use Datarecord";
    private static final String DONT_USE_RECORD = "Skip Datarecord";

    /**
     * <pre>
     * <b>Name</b>: nodeType            <i>[public]</i>
     * <b>Description</b>: Predicate used to select nodes based on the selected type (s).
     * </pre>
     *
     * @param nodeTypeList Value(s) of the field to compare (List of strings)
     * @return - Predicate for Data Evaluation
     */
    public static Predicate<DataRecord> nodeType(final String nodeTypeList) {
        return multiValuesPredicate(Fields.COLUMN_NAME_NODETYPE, Arrays.asList(nodeTypeList.split(",")));
    }

    /**
     * <pre>
     * <b>Name</b>: suiteNamePredicate            <i>[public]</i>
     * <b>Description</b>: This 'custom' predicate is used to select the datasource
     * files based on the Suite name. If the DataRecord does not have the
     * 'filled in' field, then the record is considered valid as it is intended
     * for all suites.
     * </pre>
     *
     * @param suiteName Expected Suite Name
     * @return - Predicate for Data Evaluation (Suite Names)
     */
    public static Predicate<DataRecord> suiteNamePredicate(final String suiteName) {
        return new Predicate<DataRecord>() {
            @Override
            public boolean apply(final DataRecord input) {
                String methodName = null;
                if (LOGGER.isTraceEnabled()) {
                    methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
                    LOGGER.trace("\n{}\n\tInput Values: Suite Column -> {}, Suite Name -> {}\n\tData Record -> {}",
                            methodName, Fields.COLUMN_NAME_SUITENAME, suiteName, input.toString());
                }

                // Check input values: if null or empty, return 'true'
                if (input == null) {
                    return true;
                }
                final Object value = input.getFieldValue(Fields.COLUMN_NAME_SUITENAME);
                if (value == null || value.toString().isEmpty()) {
                    return true;
                }

                // Check values of field (if it's not of type String, return 'true')
                if (value instanceof String) {
                    final List<String> suiteNameList = Arrays.asList(((String) value).split(","));
                    LOGGER.trace("\n{}\n\t--> Colum Name {}: Expected {} - Found {}", methodName, Fields.COLUMN_NAME_SUITENAME, suiteName, value);
                    return suiteNameList.contains(suiteName);
                }
                return  true;
            }
        };
    }

    /**
     * <pre>
     * <b>Name</b>: booleanAlwaysPredicete                 <i>[public]</i>
     * <b>Description</b>: This is a particular type of Predicate that is used to force
     *    the value of the check on the DataSource. It returns what is indicated
     *    in the parameter when the predicate itself is invoked.
     * </pre>
     *
     * @param forceFlag Flag to use to force predicate Result
     * @return - Predicate for Data Evaluation (Flag Value)
     */
    public static Predicate<DataRecord> booleanAlwaysPredicete(final Boolean forceFlag) {
        return new Predicate<DataRecord>() {
            @Override
            public boolean apply(@Nullable final DataRecord dataRecord) {
                return forceFlag;
            }
        };
    }

    /**
     * <pre>
     * <b>Name</b>: genericField            <i>[package protected]</i>
     * <b>Description</b>: This method represents, for backwards compatibility,
     * the envelope for the actual predicate.
     * </pre>
     *
     * @param columnName Column name of the DataSource to evaluate
     * @param columnValue Value of the field to compare
     * @return - Predicate for Data Evaluation
     */
    static Predicate<DataRecord> genericField(final String columnName, final String columnValue) {
        return singleValuePredicateImplement(columnName, columnValue, true, false, false);
    }

    /**
     * <pre>
     * <b>Name</b>: singleValuePredicate            <i>[package protected]</i>
     * <b>Description</b>: This method represents, for backwards compatibility,
     * the envelope for the actual predicate.
     * </pre>
     *
     * @param columnName Column name of the DataSource to evaluate
     * @param columnValue Value of the field to compare
     * @param isIncluded Flag to give result for Output result (Negative or positive logic)
     * @return - Predicate for Data Evaluation
     */
    static Predicate<DataRecord> singleValuePredicate(final String columnName, final String columnValue, final boolean isIncluded) {
        return singleValuePredicateImplement(columnName, columnValue, isIncluded, true, true);
    }

    /**
     * <pre>
     * <b>Name</b>: multiValuesPredicate            <i>[package protected]</i>
     * <b>Description</b>: This method is used to compare the contents of the selected
     * field with a set of values.
     * </pre>
     *
     * @param columnName Column name of the DataSource to evaluate
     * @param columnValues Value(s) of the field to compare (List of strings)
     * @param forcedTrueNotFound Flag to select boolean value for item not found (input parameter not valid)
     * @return - Predicate for Data Evaluation
     */
    static Predicate<DataRecord> multiValuesPredicate(final String columnName, final List<String> columnValues,
            final boolean forcedTrueNotFound) {
        return new Predicate<DataRecord>() {
            @Override
            public boolean apply(final DataRecord input) {
                return findValue(input, columnName, columnValues, forcedTrueNotFound);
            }
        };
    }

    /**
     * <pre>
     * <b>Name</b>: multiValuesPredicate            <i>[package protected]</i>
     * <b>Description</b>: This method is used to compare the contents of the selected
     * field with a set of values (different signature).
     * </pre>
     *
     * @param columnName Column name of the DataSource to evaluate
     * @param columnValues Value(s) of the field to compare (List of strings)
     * @return - Predicate for Data Evaluation
     */
    static Predicate<DataRecord> multiValuesPredicate(final String columnName, final List<String> columnValues) {
        return  multiValuesPredicate(columnName, columnValues, false);
    }

    /**
     * <pre>
     * <b>Name</b>: singleValuePredicate            <i>[package protected]</i>
     * <b>Description</b>: This predicate is used to compare the value of
     * the field of the input datasource with the expected one.
     * The flags supplied to the method are used to modulate the response
     * (null values and negated response).
     * </pre>
     *
     * @param columnName Column name of the DataSource to evaluate
     * @param columnValue Value of the field to compare
     * @param isIncluded Flag to give result for Output result (Negative or positive logic)
     * @param inputParameterNull Flag to give result for Data Input null.
     * @param dataValueNull Flag to give result for Data Value null.
     * @return - Predicate for Data Evaluation
     */
    private static Predicate<DataRecord> singleValuePredicateImplement(final String columnName, final String columnValue, final boolean isIncluded,
            final boolean inputParameterNull, final boolean dataValueNull) {
        return new Predicate<DataRecord>() {
            @Override
            public boolean apply(final DataRecord input) {
                String methodName = null;
                methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
                LOGGER.trace("\n{}\n\tInput Flags: Is included ({}), Parameter 'null' ({}), Value 'null' ({})\n\t--> {}", methodName, isIncluded,
                        inputParameterNull, dataValueNull, input.toString());
                final String dataValueNullOption = dataValueNull ?  USE_RECORD : DONT_USE_RECORD;

                // Check input parameters (Column name and input row)
                if ((input == null) || (columnName == null) || columnValue == null) {
                    LOGGER.trace("Predicate Return Value (1): {}", inputParameterNull, inputParameterNull ? USE_RECORD : DONT_USE_RECORD);
                    return inputParameterNull;
                }

                // Get value of input DataRecord column
                final Object value = input.getFieldValue(columnName);
                LOGGER.trace("\n{}\n\t--> Colum Name {}: Expected {} - Found {} [{}]", methodName, columnName, columnValue, value, isIncluded);

                // Check value Result (check for 'null')
                if (value == null) {
                    LOGGER.trace("Predicate Return Value (2): {} ({})", dataValueNull, dataValueNullOption);
                    return dataValueNull;
                }

                // Compare Datasource value with expected (The result is modulated by the value of the flag (xor))
                if (value instanceof String) {
                    final boolean returnValue = columnValue.equals((String) value) ^ !isIncluded;
                    LOGGER.trace("Predicate Return Value (3): {} ({})", returnValue, returnValue ? USE_RECORD : DONT_USE_RECORD);
                    return returnValue;
                }

                LOGGER.trace("Predicate Return Value (4): {} ({})", dataValueNull, dataValueNullOption);
                return dataValueNull;
            }
        };
    }

    private static boolean findValue(final DataRecord input, final String columnName, final List<String> columnValues,
            final boolean forcedTrueNotFound) {
        String methodName = null;
        methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.trace("\n{}\n\tInput Parameters: Column Name -> {}, Epexted Values -> {}, Flag Force True Not Found {}\n\t--> {}",
                methodName, columnName, columnValues, forcedTrueNotFound, input.toString());

        // Check input parameters (if null, return 'false')
        if ((input == null) || (columnName == null) || (columnValues == null) || columnValues.isEmpty()) {
            return false;
        }

        // Reading the contents of the selected column:
        final Object value = input.getFieldValue(columnName);
        LOGGER.trace("\n{}\n\tDataSource Record: {}", methodName, value.toString());

        // Piece of code for a workaround of a TDM problem
        if (value == null) {
            return forcedTrueNotFound;
        }

        // DataRecord field evaluation: Check 'value' object type and convert it into List<String>
        List<String> valueList = null;
        if (value instanceof String[]) {
            valueList = Arrays.asList((String[]) value);
        } else if (value instanceof String) {
            valueList = Arrays.asList(((String) value).split(","));
        } else {
            // This is used to reduce the quantity of LOG messages, however indicating its severity in the message.
            if (LOGGER.isTraceEnabled()) {
                LOGGER.warn("The type of object is not one of those expected [String or String[]) --> {}", value.getClass());
            }
            return false;
        }

        // Cycle to check if at least one of the required elements is present among those of the DataSource field.
        for (final String expectedSingleValue : columnValues) {
            for (final String fieldSingleValue : valueList) {
                if (expectedSingleValue.equals(fieldSingleValue)) {
                    LOGGER.trace("Expected value found: {} --> {}", expectedSingleValue, fieldSingleValue);
                    return true;
                }
            }
        }

        // This is used to reduce the quantity of LOG messages, however indicating its severity in the message.
        if (LOGGER.isTraceEnabled()) {
            LOGGER.warn("Expected value Not found: return {} value", false);
        }
        return false;
    }

    /**
     * <pre>
     * <b>Name</b>: Fields      <i>[package protected (Class)]</i>
     * <b>Description</b>: This class contains the definition of the 'base fields' costants.
     * </pre>
     */
    static final class Fields {
        static final String COLUMN_NAME_ADDREMODES = "addRemoveNodes";
        static final String COLUMN_NAME_NODETYPE = "nodeType";
        static final String COLUMN_NAME_SUITENAME = "suiteName";
        static final String COLUMN_NAME_ROLES = "roles";
        static final String COLUMN_NAME_NODEOPERTYPE = "nodeOperatorType";
        static final String COLUMN_NAME_TYPE = "type";
    }
}
