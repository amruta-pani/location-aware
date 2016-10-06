package amrutapani.location_aware;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amruta Pani on 05-10-2016.
 */

public class VisitRecordSource {

    private static VisitRecordSource visitRecordSource = null;
    String[] allColumns = {VillageVisitDbHelper._ID, VillageVisitDbHelper.COLUMN_CO_NAME, VillageVisitDbHelper.COLUMN_VILLAGE_NAME,
            VillageVisitDbHelper.COLUMN_LATITUDE, VillageVisitDbHelper.COLUMN_LONGITUDE, VillageVisitDbHelper.COLUMN_ADDRESS,
            VillageVisitDbHelper.COLUMN_VISIT_TIMESTAMP};
    private SQLiteDatabase database;
    private VillageVisitDbHelper dbHelper;

    private VisitRecordSource(Context context) {
        dbHelper = new VillageVisitDbHelper(context);
    }

    public static VisitRecordSource getVisitRecordSourceInstance(Context context) {
        if (visitRecordSource == null) {
            visitRecordSource = new VisitRecordSource(context);
        }
        return visitRecordSource;
    }

    public void open() throws SQLException {
        if (database == null) {
            database = dbHelper.getWritableDatabase();
        }
    }

    public void close() {
        database.close();
    }

    public long insertRecord(VisitRecord visitRecord) {
        ContentValues insertValues = new ContentValues();

        insertValues.put(VillageVisitDbHelper.COLUMN_CO_NAME, visitRecord.getCoName());
        insertValues.put(VillageVisitDbHelper.COLUMN_VILLAGE_NAME, visitRecord.getVillageName());
        insertValues.put(VillageVisitDbHelper.COLUMN_LATITUDE, visitRecord.getLatitude());
        insertValues.put(VillageVisitDbHelper.COLUMN_LONGITUDE, visitRecord.getLongitude());
        insertValues.put(VillageVisitDbHelper.COLUMN_ADDRESS, visitRecord.getAddress());
        insertValues.put(VillageVisitDbHelper.COLUMN_VISIT_TIMESTAMP, visitRecord.getVisitDate());

        return database.insert(VillageVisitDbHelper.TABLE_NAME, null, insertValues);
    }

    public int getCachedRecordCount() {
        final String countQuery = "SELECT COUNT(*) FROM " + VillageVisitDbHelper.TABLE_NAME;

        Cursor cursor = database.rawQuery(countQuery, null);
        cursor.moveToFirst();

        return Integer.parseInt(cursor.getString(0));
    }

    public List<VisitRecord> getAllVisitRecords() {

        List<VisitRecord> visitRecords = new ArrayList<VisitRecord>();

        Cursor cursor = database.query(VillageVisitDbHelper.TABLE_NAME, allColumns, null, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            VisitRecord visitRecord = new VisitRecord(cursor.getString(1), cursor.getString(2),
                    cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5), cursor.getString(6));
            visitRecord.setRecordId(cursor.getInt(0));
            visitRecords.add(visitRecord);
            cursor.moveToNext();
        }
        return visitRecords;
    }

    public void deleteVisitRecord(VisitRecord visitRecord) {
        database.delete(VillageVisitDbHelper.TABLE_NAME, VillageVisitDbHelper._ID + "=" + visitRecord.getRecordId(), null);
    }

}
