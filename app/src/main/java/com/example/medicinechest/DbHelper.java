package com.example.medicinechest;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.SQLException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "medicinechestDB.db";//Имя базы данных
    private static String DATABASE_PATH = "";
    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private boolean mNeedUpdate = false;
    private static final int DATABASE_VERSION = 4;

    //Конструктор
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DATABASE_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;
        copyDataBase();
        this.getReadableDatabase();
    }

    public void updateDataBase() throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
            if (dbFile.exists())
                dbFile.delete();
            copyDataBase();
            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DATABASE_NAME);
        /*InputStream mInput = mContext.getResources().openRawResource(R.raw.info_large);*/
        OutputStream mOutput = new FileOutputStream(DATABASE_PATH + DATABASE_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public boolean openDataBase() throws SQLException {
        mDataBase = SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    //Название таблицы и названия её столбцов(Создается при запуске программы)
    public static final String TABLE_USER_SCHEDULE = "userschedule";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_MEDICATION_ID = "medication_id";
    public static final String COLUMN_INTAKE_TIME = "intake_time";
    public static final String COLUMN_MEDICATION_COUNT = "medicationcount";

    //Вызывается при создании базы данных
    @Override
    public void onCreate(SQLiteDatabase db) {
        /*// Создание таблицы userschedule
        String createTableuserschedule = "CREATE TABLE IF NOT EXISTS userschedule (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "medication_id INTEGER," +
                "intake_time TEXT," +
                "medicationcount INTEGER)";
        db.execSQL(createTableuserschedule);

        // Вставка начальной записи
        ContentValues values = new ContentValues();
        values.put("name", "Новый пользователь");

        db.insert("userschedule", null, values);*/
    }

    //Вызывается при обновлении базы данных (изменении версии)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            mNeedUpdate = true;
    }
}
