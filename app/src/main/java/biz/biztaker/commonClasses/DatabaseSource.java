package biz.biztaker.commonClasses;

/**
 * Created by Anand Jakhaniya on 11-02-2018.
 * @author Anand Jakhaniya
 */

public interface DatabaseSource {

    String DATABASE_NAME = "bizTaker.db";
    int DATABASE_VERSION = 1;

    /**
     * Table name : Person Table
     */
    String USER_TABLE = "person";
    String RECORD_TABLE = "record";

    String COLUMN_ID = "id";
    String COLUMN_NAME = "name";
    String USER_EMAIL = "user_email";
    String USER_PASSWORD = "user_password";
}
