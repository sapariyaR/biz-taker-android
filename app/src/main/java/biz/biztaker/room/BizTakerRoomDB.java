package biz.biztaker.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import biz.biztaker.room.dao.UserDao;
import biz.biztaker.room.entity.User;


/**
 * Created by sagar on 20/02/18.
 */
@Database(entities = {User.class}, version = 1)
public abstract class BizTakerRoomDB extends RoomDatabase {

    public static final String DATABASE_NAME = "BizTakerDB";

    public abstract UserDao userDao();
}
