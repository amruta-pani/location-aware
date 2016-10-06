package amrutapani.location_aware;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Amruta Pani on 05-10-2016.
 */

public class VillageVisitDbHelper extends SQLiteOpenHelper implements BaseColumns {

    public static final String DB_NAME = "VillageVisit";
    public static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "VisitRecords";
    public static final String COLUMN_CO_NAME = "coName";
    public static final String COLUMN_VILLAGE_NAME = "villageName";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_VISIT_TIMESTAMP = "visitDate";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" + VillageVisitDbHelper._ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_CO_NAME + " TEXT, " + COLUMN_VILLAGE_NAME + " TEXT, " + COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " + COLUMN_ADDRESS + " TEXT, " + COLUMN_VISIT_TIMESTAMP + " TEXT )";

    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public VillageVisitDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
    }
}
