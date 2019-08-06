package biz.biztaker.room;

import android.arch.persistence.room.Room;
import android.content.Context;

/**
 * Created by sagar on 20/02/18.
 */

public class BizTakerDatabase {

    private static BizTakerDatabase instance;
    private BizTakerRoomDB db;
    private BizTakerRoomDB uiDb;

    private BizTakerDatabase(Context context) {
        db = Room.databaseBuilder(context,
                BizTakerRoomDB.class, BizTakerRoomDB.DATABASE_NAME).fallbackToDestructiveMigration().build();

        uiDb = Room.databaseBuilder(context,
                BizTakerRoomDB.class, BizTakerRoomDB.DATABASE_NAME).allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }

    public static void init(Context context) {
        instance = new BizTakerDatabase(context);
    }

    public static BizTakerDatabase get() {
        if (instance == null) {
            IllegalStateException e = new IllegalStateException("BizTakerDatabase should be initialized by calling init()");
            //FirebaseCrash.report(e);
            throw e;
        }
        return instance;
    }

    public BizTakerRoomDB getDb() {
        return db;
    }

    public BizTakerRoomDB getUiDb() {
        return uiDb;
    }
}
