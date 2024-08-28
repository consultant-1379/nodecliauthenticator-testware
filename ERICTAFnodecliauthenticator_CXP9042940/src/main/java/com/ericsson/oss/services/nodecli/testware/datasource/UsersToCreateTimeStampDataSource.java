/*
 * ------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.nodecli.testware.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.google.common.collect.Maps;

/**
 * <pre>
 * <b>Name</b>: UsersToCreateTimeStampDataSource      <i>[public (Class)]</i>
 * <b>Description</b>: This class is used to create the users datasource by processing
 * the original datasource ({@link #USER_PATH_TEMP}).
 * </pre>
 */
public class UsersToCreateTimeStampDataSource {

    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String NUM_OF_NODES = "nodes.amount";
    @SuppressWarnings("checkstyle:JavadocVariable")
    public static final String USER_PATH_TEMP = "usersToCreateTemp";
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersToCreateTimeStampDataSource.class);
    private static final Boolean USER_TIMESTAMP_FLAG = DataHandler.getConfiguration().getProperty("userCreate.timestamp.flag", true, Boolean.class);

    /**
     * <pre>
     * <b>Name</b>: createUser            <i>[public]</i>
     * <b>Description</b>: This method is used to create a list of users by cloning
     * those of the original list (USER_TO_CREATE), adding the TimeStamp to the
     * user's username. The number of clones depends on the quantity of nodes to
     * manage and must not exceed the maximum of the parallelism (vUSER) of the
     * testware.
     * </pre>
     *
     * @return - List of maps that contain the information necessary for the creation of users
     */
    @DataSource
    public List<Map<String, Object>> createUser() {
        final int numberOfUsers = USER_TIMESTAMP_FLAG ? Integer.parseInt(System.getProperty(NUM_OF_NODES)) : 1;
        LOGGER.debug("c{}, User Timestamp FLAG --> {}", numberOfUsers, USER_TIMESTAMP_FLAG);
        final List<Map<String, Object>> result = new ArrayList<>();
        final TestDataSource<DataRecord> userList = TafDataSources.fromTafDataProvider(USER_PATH_TEMP);
        for (final DataRecord next : userList) {
            for (int i = 0; i < numberOfUsers; i++) {
                final long nanoTime = System.nanoTime();
                final String user = USER_TIMESTAMP_FLAG
                        ? String.format("%s%07d", next.getFieldValue(UsersToCreateFields.USERNAME), nanoTime % 10000000)
                        : next.getFieldValue(UsersToCreateFields.USERNAME);
                LOGGER.debug("Username given while creating user {}", user);
                if (next.getFieldValue(UsersToCreateFields.ROLES) instanceof String[]) {
                    result.add(getUser(user, (String) next.getFieldValue(UsersToCreateFields.PASSWORD), String.format("%sfirstname", user),
                            String.format("%slastname", user), String.format("%s@test.com", user), true,
                            (String[]) next.getFieldValue(UsersToCreateFields.ROLES)));
                } else if (next.getFieldValue(UsersToCreateFields.ROLES) instanceof String) {
                    result.add(getUser(user, (String) next.getFieldValue(UsersToCreateFields.PASSWORD), String.format("%sfirstname", user),
                            String.format("%slastname", user), String.format("%s@test.com", user), true,
                            ((String) next.getFieldValue(UsersToCreateFields.ROLES)).split(",")));
                }
            }
        }
        return result;
    }

    /**
     * Returns the user.
     *
     * @param username
     *         the username
     * @param password
     *         the password
     * @param firstName
     *         the first name
     * @param lastName
     *         the last name
     * @param email
     *         the email
     * @param enabled
     *         user enable state
     * @param roles
     *         the user access rights
     *
     * @return the user
     */
    private Map<String, Object> getUser(final String username, final String password, final String firstName, final String lastName,
            final String email, final boolean enabled, final String... roles) {
        final Map<String, Object> user = Maps.newHashMap();
        user.put(UsersToCreateFields.USERNAME, username);
        user.put(UsersToCreateFields.PASSWORD, password);
        user.put(UsersToCreateFields.FIRSTNAME, firstName);
        user.put(UsersToCreateFields.LASTNAME, lastName);
        user.put(UsersToCreateFields.EMAIL, email);
        user.put(UsersToCreateFields.ROLES, roles);
        user.put(UsersToCreateFields.ENABLED, enabled);
        return user;
    }

    /**
     * <pre>
     * <b>Name</b>: UsersToCreateFields      <i>[private (Class)]</i>
     * <b>Description</b>:This class contain 'UsersToCreate' fields name,.
     * </pre>
     */
    static final class UsersToCreateFields {
        private static final String USERNAME = "username";
        private static final String PASSWORD = "password";
        private static final String FIRSTNAME = "firstName";
        private static final String LASTNAME = "lastName";
        private static final String EMAIL = "email";
        private static final String ROLES = "roles";
        private static final String ENABLED = "enabled";
    }
}
