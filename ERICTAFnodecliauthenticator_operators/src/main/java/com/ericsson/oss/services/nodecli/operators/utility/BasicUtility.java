/*
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson  2021
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 */

package com.ericsson.oss.services.nodecli.operators.utility;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.resetDataSource;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_ID;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_TITLE;
import static com.ericsson.oss.testware.scenario.ScenarioUtilities.getPasswordValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * <pre>
 * <b>Class Name</b>: NewBasicUtility
 * <b>Description</b>: This class contains some useful utility for all class for
 * this testware..
 * </pre>
 */
@SuppressWarnings("checkstyle:JavadocType")
public class BasicUtility {
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String SHOW_DATA_DATASOURCE_FIELDNAME = "ShowDataDatasourceFieldname";
    private static final Boolean TAKE_SCREENSHOT_ALWAIS =
            DataHandler.getConfiguration().getProperty("ui.verbose.screenshot", false, Boolean.class);
    private static final Boolean SHOW_PASSWORD_FLAG = DataHandler.getConfiguration().getProperty("log.password.show.enable", false, Boolean.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicUtility.class);
    private static final String NODECLIUSER_BACKUP = "backupOfNodeCliUser";
    private static final String NODECLIPASS_BACKUP = "backupOfnodeCliPassword";

    private static final String PROPERTY_BACKUP_SUFFIX = ".backup";

    @SuppressWarnings("checkstyle:JavadocVariable")
    @Inject
    protected TestContext context;

    /**
     * <pre>
     * <b>Name</b>: dataDrivenDataSource            <i>[public]</i>
     * <b>Description</b>: This method (and the following one) is used to create a
     * datasource, whose name is indicated by the first parameter, containing the
     * fields 'TEST_CASE_ID' and 'TEST_CASE_TITLE' used by the
     * <i>datadrivenscenario</i>.
     * The second of these two methods (with a different signature) creates the
     * datasource starting from an empty one.
     * </pre>
     *
     * @param dataSourceNew Name of the DataSource being created
     * @param testId        Test Case ID to put in DataRecords.
     * @param testName      Test Case Name to put in DataRecords.
     * @param values        Source DataSource
     */
    public static void dataDrivenDataSource(final String dataSourceNew, final String testId, final String testName,
            final Iterable<? extends DataRecord> values) {
        TafTestContext.getContext().removeDataSource(dataSourceNew);
        for (final DataRecord value : values) {
            final String additionalTitle = value.getFieldValue(SHOW_DATA_DATASOURCE_FIELDNAME) == null ? "" :
                    " (" + value.getFieldValue(SHOW_DATA_DATASOURCE_FIELDNAME) + ")";
            TafTestContext.getContext().dataSource(dataSourceNew).addRecord()
                    .setFields(value).setField(TEST_CASE_ID, testId)
                    .setField(TEST_CASE_TITLE, testName + additionalTitle);
        }
        TafTestContext.getContext().addDataSource(dataSourceNew, shared(TafTestContext.getContext().dataSource(dataSourceNew)));
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    public static void dataDrivenDataSource(final String dataSourceNew, final String testId, final String testName) {
        final TestDataSource<DataRecord> creatingDataSource = TestDataSourceFactory.createDataSource();
        creatingDataSource.addRecord();
        dataDrivenDataSource(dataSourceNew, testId, testName, creatingDataSource);
    }

    /**
     * <pre>
     * <b>Name</b>: compareDataSourceSize            <i>[public]</i>
     * <b>Description</b>: These four functions, with different signatures, are used to
     *   compare the size of the datasources provided, verifying their equality
     *   (equalSize == true) or that the size of the first is greater than that
     *   of the second (equalSize == false): if the flag is not defined, it means
     *   'false' .
     * The names of the datasource (Context DataSources) or of the DataSource
     *   (TestDataSource) objects can be provided.
     * </pre>
     *
     * @param dataSourceName1 Datasource 1 to compare (context datasource or TestDataSource)
     * @param dataSourceName2 Datasource 2 to compare (context datasource or TestDataSource)
     * @param equalSize equality flag
     * @return . check result
     */
    public static boolean compareDataSourceSize(final String dataSourceName1, final String dataSourceName2, final boolean equalSize)  {
        final int datasourceSize1 = Iterables.size(TafTestContext.getContext().dataSource(dataSourceName1));
        final int datasourceSize2 = Iterables.size(TafTestContext.getContext().dataSource(dataSourceName2));
        return compareSize(datasourceSize1, datasourceSize2, equalSize);
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    public static boolean compareDataSourceSize(final String dataSourceName1, final String dataSourceName2)  {
        return compareDataSourceSize(dataSourceName1, dataSourceName2, false);
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    public static boolean compareDataSourceSize(final TestDataSource<DataRecord> dataSource1, final TestDataSource<DataRecord> dataSource2,
            final boolean equalSize) {
        final int datasourceSize1 = Iterables.size(dataSource1);
        final int datasourceSize2 = Iterables.size(dataSource2);
        return compareSize(datasourceSize1, datasourceSize2, equalSize);
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    public static boolean compareDataSourceSize(final TestDataSource<DataRecord> dataSource1, final TestDataSource<DataRecord> dataSource2) {
        return compareDataSourceSize(dataSource1, dataSource2, false);
    }

    private static boolean compareSize(final int size1, final int size2, final boolean strictEqual) {
        LOGGER.trace("Comparing DataSources Size:\n\tDataSource 1 --> {}, DataSource 2 --> {}, Equality Flag --> {}", size1, size2, strictEqual);
        return strictEqual ? size1 == size2 : size1 >= size2;
    }

    /**
     * <pre>
     * <b>Name</b>: setupDataSource            <i>[public]</i>
     * <b>Description</b>: This static and 'runnable' method is used to update the
     *   contents of a DataSource and/or change its properties.
     * </pre>
     *
     * @param sourceDataSource Datasource used for Realigment operation (if omitted we configure only Target DataSource)
     * @param targetDataSource Datasource where setup operation should be performed
     * @param removeOriginalDatasource if set remove original Datasource at end of runnable.
     * @param setShared if Set configure Target DataSource with 'Shared' option.
     * @param setReset  if Set reset Target DataSource.
     * @return this Runnable object
     */
    public static Runnable setupDataSource(final String sourceDataSource, final String targetDataSource,
            final Boolean removeOriginalDatasource, final boolean setShared, final Boolean setReset) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext localContext = TafTestContext.getContext();
                LOGGER.trace("Runnable Setup DataSource: source -> '{}', target -> '{}', rempve original -> '{}', shared -> '{}', reset -> '{}'",
                        sourceDataSource, targetDataSource, removeOriginalDatasource, setShared, setReset);
                if (!sourceDataSource.isEmpty()) {
                    LOGGER.trace("Creating New DataSource from <{}> to <{}>", sourceDataSource, targetDataSource);
                    localContext.removeDataSource(targetDataSource);
                    if (setShared) {
                        LOGGER.trace("Creating DataSource <{}> to 'SHARED'", targetDataSource);
                        localContext.addDataSource(targetDataSource, TafDataSources.shared(localContext.dataSource(sourceDataSource)));
                    } else {
                        localContext.addDataSource(targetDataSource, localContext.dataSource(sourceDataSource));
                    }
                    if (removeOriginalDatasource) {
                        LOGGER.trace("Remove DataSource <{}>", sourceDataSource);
                        localContext.removeDataSource(sourceDataSource);
                    }
                } else if (setShared) {
                        LOGGER.trace("Updating DataSource <{}> to 'SHARED'", targetDataSource);
                        localContext.addDataSource(targetDataSource, TafDataSources.shared(localContext.dataSource(targetDataSource)));
                }
                if (setReset && !targetDataSource.isEmpty()) {
                    LOGGER.trace("Reset DataSource <{}>", targetDataSource);
                    resetDataSource(targetDataSource);
                }
            }
        };
    }

    @SuppressWarnings("checkstyle:JavadocMethod")
    public static Runnable setupDataSource(final String targetDataSource,
            final Boolean removeOriginalDatasource, final boolean setShared, final Boolean setReset) {
        return setupDataSource("", targetDataSource, removeOriginalDatasource, setShared, setReset);
    }

    /**
     * <pre>
     * <b>Method Name</b>: takeFmScreenshot           <i>[public]</i>
     * <b>Description</b>: This static method is used to concentrate the calls to take
     *   the screenshot of the UI with the aim of filtering the number of
     *   screenshots taken: the property '{@link #TAKE_SCREENSHOT_ALWAIS}' allows you to
     *   avoid non-essential ones.
     * </pre>
     * @param tool Browser Tool to use
     * @param title Screenshot title (for Allure Report)
     */
    public static void takeLocalScreenshot(final BrowserTab tool, final String title) {
        takeLocalScreenshot(tool, title, TAKE_SCREENSHOT_ALWAIS);
    }

    /**
     * <pre>
     * <b>Method Name</b>: takeLocalScreenshot           <i>[public]</i>
     * <b>Description</b>: This static method is used to concentrate the calls to take
     *   the screenshot of the UI with the aim of filtering the number of
     *   screenshots taken: the parameter 'force' allows you to
     *   avoid non-essential ones.
     * </pre>
     * @param tool Browser Tool to use
     * @param title Screenshot title (for Allure Report)
     * @param force Flag to force Screenshot (otherwise Skip)
     */
    public static void takeLocalScreenshot(final BrowserTab tool, final String title, final Boolean force) {
        if (force) {
            final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
            final LocalDateTime now = LocalDateTime.now();
            final String screenShotName = title + " - [" + dtf.format(now) + "]";
            LOGGER.trace("Take New Screenshot with name --> {}", screenShotName);
            tool.takeScreenshot(screenShotName);
        }
    }

    /**
     * <pre>
     * <b>Method Name</b>: assertWithScreenShot           <i>[public]</i>
     * <b>Description</b>: This method implements a customization of the Assert for
     *   operations on the <i>GUI</i>: when the condition is <u>false</u> (failure), before
     *   generating the consequent Assert, a screenshot is created to facilitate
     *   the investigation operations on the failure.
     * In conjunction with the '{@link #takeLocalScreenshot(BrowserTab, String)}' method the
     *   screen capture is also conditioned on the 'ui.verbose.screenshot'
     *   ({@link #TAKE_SCREENSHOT_ALWAIS}) flag.
     * </pre>
     * @param tool Browser Tab Tool object
     * @param assertDescription Description to put in Assert Stantment
     * @param condition True condition: assertion rise if it became false.
     */
    @SuppressWarnings("checkstyle:JavadocMethod")
    public static void assertWithScreenShot(final BrowserTab tool, final String assertDescription, final Boolean condition) {
        takeLocalScreenshot(tool, assertDescription, !condition || TAKE_SCREENSHOT_ALWAIS);
        Assertions.assertThat(condition).as(assertDescription).isTrue();
    }

    /**
     * <pre>
     * Name: nodeCliLoggerConfiguration()       [public]
     * Description: This method is used to add the fields necessary for the
     *   configuration of the function that deals with the download of the ENM
     *   LOGs to the records of the DataSource supplied..
     * </pre>
     * @param dataSourceNew Name of New DataSource
     * @param values Original DataSource to modify
     */
    public static void nodeCliLoggerConfiguration(final String dataSourceNew, final Iterable<? extends DataRecord> values) {
        TafTestContext.getContext().removeDataSource(dataSourceNew);
        LOGGER.trace("Update DataSource to use NodeCli logger");
        for (final DataRecord value : values) {
            TafTestContext.getContext().dataSource(dataSourceNew).addRecord()
                    .setFields(value)
                    .setField("numLogEntry", 25)
                    .setField("mergeFlag", true)
                    .setField("sortFlag", true)
                    .setField("skipHostLabel", true)
                    .setField("listOfFilterToApply", value.getFieldValue("ipAddress"))
                    .setField("listOfBlade", "nodeCli");
        }
        TafTestContext.getContext().addDataSource(dataSourceNew, shared(TafTestContext.getContext().dataSource(dataSourceNew)));
    }

    /**
     * <pre>
     * Name: backupNodeCliUser()       [public]
     * Description: This method is used to make a backup copy of the
     *   'nodeCliUserName' and 'nodeCliUserPassword' fields if present in
     *   the original DataSource.
     * </pre>
     * @param dataSourceNew Name of New DataSource
     * @param values Original DataSource to modify
     */
    public static void backupNodeCliUser(final String dataSourceNew, final Iterable<? extends DataRecord> values) {
        TafTestContext.getContext().removeDataSource(dataSourceNew);
        LOGGER.trace("Backup of NodeCliUser to use in next operations");
        for (final DataRecord value : values) {
            final String nodeCliUserBackup =  value.getFieldValue(UserCredentialType.NODECLI_USER.getUserNameField()) != null
                    ?  value.getFieldValue(UserCredentialType.NODECLI_USER.getUserNameField()) : "";
            final String nodeCliPassBackup =  value.getFieldValue(UserCredentialType.NODECLI_USER.getUserPasswordField()) != null
                    ?  value.getFieldValue(UserCredentialType.NODECLI_USER.getUserPasswordField()) : "";
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("NodeCliUser backup values:\n\tNodeCliUser value : '{}' --> '{}'\n\tNodeCliPass value:  '{}' --> '{}'",
                        value.getFieldValue(UserCredentialType.NODECLI_USER.getUserNameField()), NODECLIUSER_BACKUP,
                        getPasswordValue(value.getFieldValue(UserCredentialType.NODECLI_USER.getUserPasswordField())), NODECLIPASS_BACKUP);
            }
            TafTestContext.getContext().dataSource(dataSourceNew).addRecord().setFields(value)
                    .setField(NODECLIPASS_BACKUP, nodeCliPassBackup)
                    .setField(NODECLIUSER_BACKUP, nodeCliUserBackup);
        }
        TafTestContext.getContext().addDataSource(dataSourceNew, shared(TafTestContext.getContext().dataSource(dataSourceNew)));
    }

    /**
     * <pre>
     * Name: nodeCliUserConfiguration()       [public]
     * Description: This method is used to add the fields necessary for the
     *   configuration of NodeCli User.
     * </pre>
     * @param dataSourceNew Name of New DataSource
     * @param values Original DataSource to modify
     */
    public static void nodeCliUserConfiguration(final String dataSourceNew, final Iterable<? extends DataRecord> values) {
        TafTestContext.getContext().removeDataSource(dataSourceNew);
        for (final DataRecord value : values) {
            //nodeCliUser & NodeCliPassword
            final Map<String, Object> data = Maps.newHashMap(value.getAllFields());
            final String newUserName;
            final String newUserPassword;
            final String dsNodeCliUserBackup = value.getFieldValue(NODECLIUSER_BACKUP);
            final String dsNodeCliPassBackup = value.getFieldValue(NODECLIPASS_BACKUP);
            LOGGER.trace("Backup NodeCliUser values: Name --> {}, Password --> {}",
                    dsNodeCliUserBackup, SHOW_PASSWORD_FLAG ? dsNodeCliPassBackup : "******");
            if (dsNodeCliUserBackup == null || dsNodeCliUserBackup.isEmpty() || dsNodeCliPassBackup == null || dsNodeCliPassBackup.isEmpty()) {
                newUserName = "NodeCli_" + data.get("username");
                newUserPassword = "NodeCli_" + data.get("password");
            } else {
                newUserName = value.getFieldValue(NODECLIUSER_BACKUP);
                newUserPassword = value.getFieldValue(NODECLIPASS_BACKUP);
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Update DataRecord With NodeCli Data:\n\t'{}': <{}> --> <{}>\n\t'{}': <{}> --> <{}>",
                        UserCredentialType.NODECLI_USER.getUserNameField(),
                        value.getFieldValue(UserCredentialType.NODECLI_USER.getUserNameField()), newUserName,
                        UserCredentialType.NODECLI_USER.getUserPasswordField(),
                        SHOW_PASSWORD_FLAG ? value.getFieldValue(UserCredentialType.NODECLI_USER.getUserPasswordField()) : "******",
                        SHOW_PASSWORD_FLAG ? newUserPassword : "***  ***");
            }
            data.put(UserCredentialType.NODECLI_USER.getUserNameField(), newUserName);
            data.put(UserCredentialType.NODECLI_USER.getUserPasswordField(), newUserPassword);
            final DataRecord newNode = new DataRecordImpl(data);
            TafTestContext.getContext().dataSource(dataSourceNew).addRecord()
                    .setFields(value).setFields(newNode);
        }
        TafTestContext.getContext().addDataSource(dataSourceNew, shared(TafTestContext.getContext().dataSource(dataSourceNew)));
    }

    /**
     * <pre>
     * <b>Name</b>: reduceSyncRetryProperty            <i>[protected]</i>
     * <b>Description</b>: This function is used to reduce the number of retries and the
     *   time interval for the node synchronization check.
     * </pre>
     */
    public void reduceSyncRetryProperty() {
        final List<String> syncRetryPropertyArray = prepareSyncRetryPropertyArray();
        final List<String> syncIntervalPropertyArray = prepareSyncIntervalPropertyArray();
        final Integer reducedCount = DataHandler.getConfiguration().getProperty("sync.retry.allNodes", 60, Integer.class);
        final Integer reducedInterval = DataHandler.getConfiguration().getProperty("sync.interval.allNodes", 5000, Integer.class);
        LOGGER.trace("Reducing Sync Timeout for all kind of nodes:\n\t --> sync.retry = {}\n\t --> sync.interval = {}",
                reducedCount, reducedInterval);
        for (final String propertyToModify : syncRetryPropertyArray) {
            DataHandler.getConfiguration().setProperty(propertyToModify + PROPERTY_BACKUP_SUFFIX,
                    DataHandler.getConfiguration().getProperty(propertyToModify));
            DataHandler.getConfiguration().setProperty(propertyToModify, reducedCount);
        }
        for (final String propertyToModify : syncIntervalPropertyArray) {
            DataHandler.getConfiguration().setProperty(propertyToModify + PROPERTY_BACKUP_SUFFIX,
                    DataHandler.getConfiguration().getProperty(propertyToModify));
            DataHandler.getConfiguration().setProperty(propertyToModify, reducedInterval);
        }
    }

    /**
     * <pre>
     * <b>Name</b>: reduceSyncRetryProperty            <i>[protected]</i>
     * <b>Description</b>: This function is used to reduce the number of retries and the
     *   time interval for the node synchronization check.
     * </pre>
     */
    public void restoreSyncRetryProperty() {
        final List<String> syncRetryPropertyArray = prepareSyncRetryPropertyArray();
        final List<String> syncIntervalPropertyArray = prepareSyncIntervalPropertyArray();
        LOGGER.trace("Restoring Sync Timeout for all kind of nodes");

        for (final String propertyToModify : syncRetryPropertyArray) {
            DataHandler.getConfiguration().setProperty(propertyToModify,
                    DataHandler.getConfiguration().getProperty(propertyToModify + PROPERTY_BACKUP_SUFFIX));
        }
        for (final String propertyToModify : syncIntervalPropertyArray) {
            DataHandler.getConfiguration().setProperty(propertyToModify,
                    DataHandler.getConfiguration().getProperty(propertyToModify + PROPERTY_BACKUP_SUFFIX));
        }
    }

    // ************************************************************************
    //                          Protected Methods
    // ************************************************************************

    /**
     * <pre>
     * <b>Name</b>: getNetsimProperties            <i>[protected]</i>
     * <b>Description</b>: This function returns the properties defined for NetSim,
     *   searching for them in the RUN environment.
     * </pre>
     *
     * @return property object for NetSim
     */
    protected final Properties getNetsimProperties() {
        LOGGER.info("Get Netsim information from Property");
        final Properties allProperties = TafConfigurationProvider.provide().getProperties();
        final Properties netSimProperty = new Properties();
        final Set<String> keys = allProperties.stringPropertyNames();
        int propertyCount = 0;
        for (final String singleKey : keys) {
            propertyCount++;
            if (singleKey.endsWith(".type") && !singleKey.contains(".node.") && allProperties.getProperty(singleKey).contentEquals("netsim")) {
                final String getThisRootProp = singleKey.substring(0, singleKey.lastIndexOf("."));
                final String propertyValue = allProperties.getProperty(singleKey);
                LOGGER.debug("{}) Find NetSim Property: {}={}", propertyCount, singleKey, propertyValue);
                for (final String singleInnerKey : keys) {
                    if (singleInnerKey.startsWith(getThisRootProp)) {
                        netSimProperty.setProperty(singleInnerKey, allProperties.getProperty(singleInnerKey));
                    }
                }
            }
        }
        return netSimProperty;
    }

    /**
     * <pre>
     * <b>Name</b>: getpriorityIp            <i>[protected]</i>
     * <b>Description</b>: This Method return Ip Address available.
     * </pre>
     * @param hostObject Host to use
     * @return IpAddress to use
     */
    protected String getpriorityIp(final Host hostObject) {
        return hostObject.getIp().isEmpty() ? hostObject.getIpv6() : hostObject.getIp();
    }

    /**
     * <pre>
     * <b>Name</b>: distributeValues            <i>[public]</i>
     * <b>Description</b>: This method serves to distribute (make homogeneous) the
     * number of elements for each 'slot', in relation to a maximum number of
     * elements acceptable for each slot.
     * </pre>
     *
     * @param valueToDistribute Number of objects to 'distribute'
     * @param maxSlotCount Maximum number of items for each 'slot'
     * @return - Number of elements distributed per 'slot'
     */
    public static int distributeValues(final int valueToDistribute, final int maxSlotCount) {
        LOGGER.trace("Item to distribute: {}, Max Slot Count: {}", valueToDistribute, maxSlotCount);
        final int passCount = (int) Math.ceil((float) valueToDistribute / maxSlotCount);
        final int itemPerPass = (int) Math.ceil((float) valueToDistribute / passCount);
        LOGGER.trace("Parallelism Count: {}/{}, Pass Count: {}", itemPerPass, maxSlotCount, passCount);
        return itemPerPass;
    }

    private static ArrayList<String> prepareSyncRetryPropertyArray() {
        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("node.sync.repeat");
        arrayList.add("node.cbpoi.sync.retries");
        arrayList.add("node.comecim.sync.retries");
        arrayList.add("node.cpp.sync.retries");
        arrayList.add("node.cpp.sync.retries.sl2");
        arrayList.add("CUDB.sync.repeat");
        arrayList.add("GenericESA.sync.repeat");
        arrayList.add("node.cpp.sync.retries");
        arrayList.add("node.cpp.sync.retries.sl2");
        arrayList.add("VCUDB.sync.repeat");

        return arrayList;
    }

    private static ArrayList<String> prepareSyncIntervalPropertyArray() {
        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("node.sync.interval");
        arrayList.add("node.cbpoi.sync.timeout");
        arrayList.add("node.comecim.sync.timeout");
        arrayList.add("node.cpp.sync.timeout");
        arrayList.add("node.cpp.sync.timeout.sl2");
        arrayList.add("CUDB.sync.interval");
        arrayList.add("GenericESA.sync.interval");
        arrayList.add("node.cpp.sync.timeout");
        arrayList.add("node.cpp.sync.timeout.sl2");
        arrayList.add("VCUDB.sync.interval");

        return arrayList;
    }

    /**
     * <pre>
     * <b>Class Name</b>: NetsimInfoDatafield
     * <b>Description</b>: This subclass contains local parameters names for
     * this TestStep class.
     * </pre>
     */
    public static final class NetsimInfoDatafield {
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String NETSIM_INFO_FIELD_NETSIMHOST = "hostName";
        @SuppressWarnings("checkstyle:JavadocVariable")
        public static final String NETSIM_INFO_FIELD_SIMULNAME = "simulation";

        private NetsimInfoDatafield() {
        }
    }
}
