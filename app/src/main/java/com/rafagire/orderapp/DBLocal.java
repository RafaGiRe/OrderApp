package com.rafagire.orderapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBLocal extends SQLiteOpenHelper implements DBAccess{

    //ATTRIBUTES
    private SQLiteDatabase db;
    private String tableName = "OrderApp_Products";
    private String sqlCreateTable = "CREATE TABLE " + tableName + " (id INTEGER PRIMARY KEY, type TEXT NOT NULL, name TEXT NOT NULL, price FLOAT)";
    private String sqlDeleteTable = "DROP TABLE IF EXISTS " + tableName;

    //CONSTRUCTOR
    public DBLocal(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    //METHODS
    public void onCreate(SQLiteDatabase db) {
        //Create the DB if it doesn't exist
        db.execSQL(sqlCreateTable);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Me la cargo entera
        //db.execSQL(sqlDeleteTable);

        //La vuelvo a crear
        //db.execSQL(sqlCreateTable2);
    }

    public void createTable(){
        db = this.getWritableDatabase();

        db.execSQL(sqlCreateTable);
    }

    public void deleteTable(){
        db = this.getWritableDatabase();

        db.execSQL(sqlDeleteTable);
    }


    public void add(Product product) throws DBException{
        if (findById(product.id) == null) {
            try {
                db = this.getWritableDatabase();

                if (db != null) {
                    ContentValues newRegister = new ContentValues();
                    newRegister.put("id", product.id);
                    newRegister.put("type", product.type);
                    newRegister.put("name", product.name);
                    newRegister.put("price", product.price);

                    db.insert(tableName, null, newRegister);
                    db.close();
                }
            }catch(Exception e){
                if (db != null)
                    db.close();
                throw new DBException(e);
            }
        }
        else{
            throw new DBException("Product ID already used");
        }
    }

    public void update(Product product) throws DBException {
        if (findById(product.id) != null) {
            try {
                db = this.getWritableDatabase();

                if (db != null) {
                    ContentValues updateRegister = new ContentValues();
                    updateRegister.put("type", product.type);
                    updateRegister.put("name", product.name);
                    updateRegister.put("price", product.price);

                    db.update(tableName, updateRegister, "id="+product.id, null);
                    db.close();
                }
            }catch(Exception e){
                if (db != null)
                    db.close();
                throw new DBException(e);
            }
        }
        else{
            throw new DBException("Product not found");
        }
    }

    public void remove(int id) throws DBException {
        if (findById(id) != null) {
            try {
                db = this.getWritableDatabase();

                if (db != null) {
                    db.delete(tableName, "id=" + id, null);
                    db.close();
                }
            }catch(Exception e){
                if (db != null)
                    db.close();
                throw new DBException(e);
            }
        }
        else{
            throw new DBException("Product not found");
        }
    }

    public Product findById(int id) throws DBException {
        Product product = null;

        try {
            db = this.getReadableDatabase();
            if (db != null) {
                String sqlSelId = "SELECT * FROM " + tableName + " WHERE id = " + id;
                Cursor c = db.rawQuery(sqlSelId, null);
                if (c.moveToFirst()) {
                    product = new Product();
                    product.id = c.getInt(0);
                    product.type = c.getString(1);
                    product.name = c.getString(2);
                    product.price = c.getFloat(3);
                }
                db.close();
            }
        }catch(Exception e){
            if(db!=null)
                db.close();
            throw new DBException(e);
        }
        return product;
    }

    public List<Product> findByType(String type) throws DBException {
        List<Product> list = null;

        try {
            db = this.getReadableDatabase();

            if (db != null) {
                String sqlSelType = "SELECT * FROM " + tableName + " WHERE type LIKE '" + type + "'";

                Cursor c = db.rawQuery(sqlSelType, null);
                if (c.moveToFirst()) {
                    list = new ArrayList<Product>();
                    do {
                        Product product = new Product();
                        product.id = c.getInt(0);
                        product.type = c.getString(1);
                        product.name = c.getString(2);
                        product.price = c.getFloat(3);
                        list.add(product);
                    } while (c.moveToNext());
                }
                db.close();
            }
        }catch(Exception e){
            if(db!=null)
                db.close();
            throw new DBException(e);
        }
        return list;
    }

    public List<Product> findByName(String name) throws DBException {
        List<Product> list = null;

        try {
            db = this.getReadableDatabase();

            if (db != null) {
                String sqlSelType = "SELECT * FROM " + tableName + " WHERE name LIKE '" + name + "'";

                Cursor c = db.rawQuery(sqlSelType, null);
                if (c.moveToFirst()) {
                    list = new ArrayList<Product>();
                    do {
                        Product product = new Product();
                        product.id = c.getInt(0);
                        product.type = c.getString(1);
                        product.name = c.getString(2);
                        product.price = c.getFloat(3);
                        list.add(product);
                    } while (c.moveToNext());
                }
                db.close();
            }
        }catch(Exception e){
            if(db!=null)
                db.close();
            throw new DBException(e);
        }
        return list;
    }

    public List<Product> findAll() throws DBException {
        List<Product> all = null;

        try {
            db = this.getReadableDatabase();

            if (db != null) {
                String sqlSelId = "SELECT * FROM " + tableName;
                Cursor c = db.rawQuery(sqlSelId, null);
                if (c.moveToFirst()) {
                    all = new ArrayList<Product>();
                    do {
                        Product product = new Product();
                        product.id = c.getInt(0);
                        product.type = c.getString(1);
                        product.name = c.getString(2);
                        product.price = c.getFloat(3);
                        all.add(product);
                    } while (c.moveToNext());
                }
                db.close();
            }
        }catch(Exception e){
            if(db!=null)
                db.close();
            throw new DBException(e);
        }
        return all;
    }

}
