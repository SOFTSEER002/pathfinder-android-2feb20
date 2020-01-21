package com.fox.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public final static String DB_NAME = "product.db";
    public final static String TABLE_NAME = "product_table";
    public final static String PRODUCT_NAME = "product_name";
    public final static String PRODUCT_PRICE = "product_price";
    public final static String BARCODE = "barcode";
    SQLiteDatabase database;

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        database = db;
        db.execSQL("CREATE TABLE " + TABLE_NAME + "( integer id primary key, product_name text,barcode text, product_price text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String columnExists(String barcode) {
        String sql = "SELECT * FROM product_table WHERE barcode='" + barcode + "'";
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        cursor.moveToFirst();
        return cursor.getString(1);
        // cursor.getInt(0) is 1 if column with value exists
//        if (cursor.getInt(0) == 1) {
//            String productName = cursor.getString(1);
//            cursor.close();
//            return true;
//        } else {
//            cursor.close();
//            return false;
//        }


    }

    void insertProductData(String product_name, String barcode, String product_price) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PRODUCT_NAME, product_name);
        contentValues.put(BARCODE, barcode);
        contentValues.put(PRODUCT_PRICE, product_price);
        database.insert(TABLE_NAME, null, contentValues);

    }
}
