package com.sample.gmaps;

import static com.sample.gmaps.Constants.LOCATION_TABLE_NAME;

public class LocationDBColNames {
    public static final String ID = "Id";
    public static final String NAME = "Name";
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";
    public static final String CATEGORY = "Category";


    /*public static final String LOCATION_SQL_SELECT_QUERY = "SELECT * FROM "
            + LOCATION_TABLE_NAME + " DESC LIMIT " + 30;

    public static final String LOCATION_SQL_DELETE_QUERY = "DELETE FROM "
            + LOCATION_TABLE_NAME +
            " WHERE " + LocationDBColNames.ID + " NOT IN " + "(" + "SELECT " + LocationDBColNames.ID + " from " + LOCATION_TABLE_NAME +
            " ORDER BY " + LocationDBColNames.ID + " DESC " +
            " limit " + 30 + " )";*/

}
