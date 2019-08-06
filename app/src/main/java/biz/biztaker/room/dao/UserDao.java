package biz.biztaker.room.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import biz.biztaker.room.entity.User;

/**
 * Created by sagar on 20/02/18.
 */

@Dao
public interface UserDao {

    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user")
    Cursor getCursorAll();

    @Query("SELECT * FROM user WHERE id = :userId")
    User getUserById(Long userId);

    @Query("SELECT * FROM user WHERE email = :email")
    User getUserByEmail(String email);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertUsers(List<User> userList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertUser(User user);
}
