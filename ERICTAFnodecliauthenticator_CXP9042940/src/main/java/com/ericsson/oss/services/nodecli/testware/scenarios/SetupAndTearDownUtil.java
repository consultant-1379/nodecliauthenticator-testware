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

package com.ericsson.oss.services.nodecli.testware.scenarios;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_ID;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_TITLE;
import static com.ericsson.oss.services.nodecli.testware.scenarios.NodeCLIAutenticathorScenario.MR_SUBSTITUTION_TAG;
import static com.ericsson.oss.services.nodecli.testware.scenarios.SetupAndTeardownScenario.testwareTeamName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.impl.LoggingSecurityScenarioListener;
import com.ericsson.oss.services.nodecli.testware.constant.Constants;
import com.ericsson.oss.testware.scenario.PrintDatasourceHelper;
import com.ericsson.oss.testware.scenario.ScenarioUtilities;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * SetupAndTearDownUtil necessary operations that must be executed before and after every test suite.
 */
public abstract class SetupAndTearDownUtil extends ScenarioUtilities {

    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String SEPARATOR = System.lineSeparator();
    private static int numberOfUsers;
    private static int numberOfNodes;
    private static ITestContext suiteContext;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioUtilities.class);

    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected TestContext context;
    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected PrintDatasourceHelper printDatasourceHelper;

    /**
     * <pre>
     * <b>Name</b>: getSuiteContext                      <i>protected</i>
     * <b>Description</b>: This method is a getter that returns the 'ITestContext'
     *   object for the running suite.
     * </pre>
     * @return ItestContext Object
     */
    protected static ITestContext getSuiteContext() {
        return suiteContext;
    }

    /**
     * <pre>
     * <b>Name</b>: setSuiteContext                      <i>protected</i>
     * <b>Description</b>: This method is a setter that set the 'ITestContext'
     *   object for the running suite.
     * </pre>
     * @param suiteContextValue value to set.
     */
    protected static void setSuiteContext(final ITestContext suiteContextValue) {
        suiteContext = suiteContextValue;
    }

    /**
     * <pre>
     * <b>Name</b>: getScenarioRunner                      <i>public</i>
     * <b>Description</b>: This method return a common scenario runner for this
     *   testware.
     * </pre>
     * @return scenario Runner
     */
    public static TestScenarioRunner getScenarioRunner() {
        return runner()
                .withListener(new LoggingSecurityScenarioListener()).build();
    }

    /**
     * <pre>
     * <b>Name</b>: removeAndCreateTestDataSource                      <i>public</i>
     * <b>Description</b>: This method replace previous context Datasource (removing it)
     *   whith new one with New content.
     * </pre>
     *
     * @param dataSourceName DataSource Name
     * @param nodesFiltered DataSourceContent
     */
    public static void removeAndCreateTestDataSource(final String dataSourceName, final Iterable<DataRecord> nodesFiltered) {
        LOGGER.trace("Refresh (Remove and Recreate) '{}' datasource [size --> {}]", dataSourceName, Iterables.size(nodesFiltered));
        TafTestContext.getContext().removeDataSource(dataSourceName);
        final Iterator<DataRecord> localNameIterator = nodesFiltered.iterator();
        while (localNameIterator.hasNext()) {
            final DataRecord node = localNameIterator.next();
            TafTestContext.getContext().dataSource(dataSourceName).addRecord().setFields(node);
        }
    }

    /**
     * <pre>
     * <b>Name</b>: reduceDataSource            <i>[public]</i>
     * <b>Description</b>: This static method is used to eliminate dataRecords that have values from the selected field repeated in the datasource..
     * </pre>
     *
     * @param originalDataSource - Input DataSource (Working DataSource)
     * @param checkingField      - field to use for filtering
     * @return - filtered DataSource.
     */
    public static TestDataSource<DataRecord> reduceDataSource(final TestDataSource<DataRecord> originalDataSource,
            final String checkingField) {
        LOGGER.trace("Remove duplicated records: datasotce Size --> {}, field to check --> {}", Iterables.size(originalDataSource), checkingField);
        final List<String> keywordListValues = new ArrayList<>();
        final List<Map<String, Object>> reorderedDataSource = Lists.newArrayList();
        final Iterator<DataRecord> originalDatasourceIterator = originalDataSource.iterator();
        while (originalDatasourceIterator.hasNext()) {
            final DataRecord originalRecord = originalDatasourceIterator.next();
            // Please review the logic here
            if (!keywordListValues.contains(originalRecord.getFieldValue(checkingField))) {
                keywordListValues.add((String) originalRecord.getFieldValue(checkingField));
                reorderedDataSource.add(originalRecord.getAllFields());
            }
        }
        LOGGER.trace("Reduced datasouce size: {} --> {}", Iterables.size(originalDataSource), reorderedDataSource.size());
        return TestDataSourceFactory.createDataSource(reorderedDataSource);
    }

    /**
     * <pre>
     * Name: reorderDatasourceWithOriginAlone()       [public]
     * Description: This method could be used to reorder 'reorderingDataSource'
     *   with 'originalDataSource' sequence, using 'orderingKeyWord' keyword.
     * It follows each DataRecord of first DataSource (originalDataSource) search
     *   in second DataSource (reorderingDataSource) a record with same
     *   'orderingKeyWord' field and put this one in New DataSource
     *   (reorderedDataSource).
     * </pre>
     *
     * @param originalDataSource   DataSource from which the order of the records must be copied using the KeyWord #orderingKeyWord.
     * @param reorderingDataSource DataSource to reorder.
     * @param orderingKeyWord      Keyword to use for reordering (it must be unique)
     * @return Reordered DataSource
     */
    public static TestDataSource<DataRecord> reorderDatasourceWithOriginAlone(final TestDataSource<? extends DataRecord> originalDataSource,
            final TestDataSource<? extends DataRecord> reorderingDataSource, final String orderingKeyWord) {
        final List<Map<String, Object>> reorderedDataSource = Lists.newArrayList();
        final Iterator<? extends DataRecord> originalDatasourceIterator = originalDataSource.iterator();
        while (originalDatasourceIterator.hasNext()) {
            final DataRecord originalRecord = originalDatasourceIterator.next();
            final Iterator<? extends DataRecord> reorderingDatasourceIterator = reorderingDataSource.iterator();
            for (final DataRecord reorderingDataRecord : Lists.newArrayList(reorderingDatasourceIterator)) {
                if (reorderingDataRecord.getFieldValue(orderingKeyWord).equals(originalRecord.getFieldValue(orderingKeyWord))) {
                    reorderedDataSource.add(originalRecord.getAllFields());
                }
            }
        }
        return TestDataSourceFactory.createDataSource(reorderedDataSource);
    }

    /**
     * <pre>
     * Name: dataDrivenDataSource()       [public]
     * Description: This method should be use to create a DataDriven DataSource
     *   with Requested field for DataDriven Scenario Execution..
     * </pre>
     * @param dataSourceNew Name of New DataSource
     * @param testId Test Cas Identifier (The one stored in TMS)
     * @param testName Test Name
     * @param teamName Name of Team
     * @param values Original DataSource to modify
     */
    public static void dataDrivenDataSource(final String dataSourceNew, final String testId, final String testName, final String teamName,
            final Iterable<? extends DataRecord> values) {
        TafTestContext.getContext().removeDataSource(dataSourceNew);
        for (final DataRecord value : values) {
            final String valueToReplace = Constants.MR_MAP_REFERENCE.get(value.getFieldValue("nodeType"));
            final String testIdModified = testId.replace(MR_SUBSTITUTION_TAG, valueToReplace == null ? "default" : valueToReplace);
            final String titleModified = testName.replace(MR_SUBSTITUTION_TAG, valueToReplace == null ? "default" : valueToReplace);

            TafTestContext.getContext().dataSource(dataSourceNew).addRecord()
                    .setFields(value)
                    .setField(TEST_CASE_ID, testIdModified)
                    .setField(TEST_CASE_TITLE, titleModified)
                    .setField("teamName", teamName);
        }
        TafTestContext.getContext().addDataSource(dataSourceNew, shared(TafTestContext.getContext().dataSource(dataSourceNew)));
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    public static void dataDrivenDataSource(final String dataSourceNew, final String testId, final String testName,
            final Iterable<? extends DataRecord> values) {
        // The parameter '' is retrieved from the class '' and obtained from a merge between:
        //  -  suiteTeamName optionally present in the suite.xml file
        //  -  'testware.default.teamname' property
        //  -  default Team Name (if not present xml parameter or property)
        dataDrivenDataSource(dataSourceNew, testId, testName, testwareTeamName, values);
    }

    /**
     * <pre>
     * <b>Name</b>: createMvalFilterProperty            <i>[Package Protected]</i>
     * <b>Description</b>: This method is used to create an <i>mVAL</i> filter to select nodes
     * of the specified type.
     * The filter then created is stored in the system saving it as property
     * (<u>[SuiteName].filter</u>).
     * </pre>
     *
     * @param suiteNodeTypes List of node type to use (separated by commas)
     */
    void createFilterProperty(final String suiteNodeTypes) {
        // If mVAL filter is not defined (has higher priority than xml filter)
        final boolean mvalFilterPresent = !getFilterMval().isEmpty();
        final boolean suiteNodelistPresent = !suiteNodeTypes.isEmpty();
        LOGGER.trace("Checking for condition to generate mVAL filter:\n\tmVAL filter alreadyPresent --> {} [{}]\n\tNode list present --> {} [{}]\n\t"
                        + "Generating mVAL filter --> {}",
                mvalFilterPresent, getFilterMval(), suiteNodelistPresent, suiteNodeTypes, !mvalFilterPresent && suiteNodelistPresent);
        if (!mvalFilterPresent && suiteNodelistPresent) {
            LOGGER.trace("Node Type selected: [{}]", suiteNodeTypes);
            final String[] nodeTypeList = suiteNodeTypes.split(",");
            final String suiteName = (String) DataHandler.getConfiguration().getProperty("suites", String.class).replaceAll(".xml", "");
            final String propertyName = String.format("%s.%s", suiteName, "filter");
            LOGGER.trace("Suite Name: <{}>, Property Name: <{}>", suiteName, propertyName);
            final StringBuilder mvalStringBuilder = new StringBuilder();
            boolean firstItem = true;
            for (final String singleNodeType : nodeTypeList) {
                mvalStringBuilder.append(firstItem ? "" : " || ");
                mvalStringBuilder.append("nodeType");
                mvalStringBuilder.append(" == ");
                mvalStringBuilder.append(String.format("'%s'", singleNodeType));
                firstItem = false;
            }
            LOGGER.trace("Mval Property Value: <{}>", mvalStringBuilder.toString());
            TafConfigurationProvider.provide().setProperty(propertyName, mvalStringBuilder.toString());
        }
    }

    /**
     * <pre>
     * <b>Name</b>: getNumberOfNodes                      <i>public</i>
     * <b>Description</b>: This method is a getter that returns the number of user
     *   stored in 'numberOfNodes' field.
     * </pre>
     * @return the number of Nodes
     */
    public static int getNumberOfNodes() {
        return numberOfNodes;
    }

    /**
     * <pre>
     * <b>Name</b>: setNumberOfNodes                      <i>public</i>
     * <b>Description</b>: This method is a setter that set the 'numberOfNodes'
     *   field for this class.
     * </pre>
     * @param value to set
     */
    public static void setNumberOfNodes(final int value) {
        numberOfNodes = value;
    }

    /**
     * <pre>
     * <b>Name</b>: getNumberOfUsers                      <i>public</i>
     * <b>Description</b>: This method is a getter that returns the number of user
     *   stored in 'numberOfUser' field.
     * </pre>
     * @return the number of users
     */
    public static int getNumberOfUsers() {
        return numberOfUsers;
    }

    /**
     * <pre>
     * <b>Name</b>: setNumberOfUsers                      <i>public</i>
     * <b>Description</b>: This method is a setter that set the 'numberOfUser'
     *   field for this class.
     * </pre>
     * @param value to set
     */
    public static void setNumberOfUsers(final int value) {
        numberOfUsers = value;
    }

}
