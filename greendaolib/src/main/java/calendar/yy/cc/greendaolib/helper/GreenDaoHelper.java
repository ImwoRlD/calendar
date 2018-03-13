package calendar.yy.cc.greendaolib.helper;

import android.content.Context;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;

import static calendar.yy.cc.greendaolib.bean.DaoMaster.createAllTables;

/**
 * Created by zpy on 2018/3/13.
 */

public class GreenDaoHelper extends DatabaseOpenHelper {

    public GreenDaoHelper(Context context, String name, int version) {
        super(context, name, version);
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
        createAllTables(db, true);
    }
}
