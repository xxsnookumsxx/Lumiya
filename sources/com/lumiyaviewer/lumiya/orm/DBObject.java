package com.lumiyaviewer.lumiya.orm;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcelable;
import java.nio.ByteBuffer;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public abstract class DBObject implements Parcelable {
    protected long _id;

    public static class DatabaseBindingException extends Exception {
        public DatabaseBindingException(Class<?> cls, String str) {
            super("Failed to bind " + cls.getSimpleName() + ": " + str);
        }

        public DatabaseBindingException(String str) {
            super(str);
        }
    }

    public DBObject() {
        this._id = 0L;
    }

    public DBObject(Cursor cursor) {
        loadFromCursor(cursor);
    }

    public DBObject(SQLiteDatabase sQLiteDatabase, long j) throws DatabaseBindingException {
        if (sQLiteDatabase == null) {
            throw new DatabaseBindingException(getClass(), "database not opened.");
        }
        Cursor cursorQuery = sQLiteDatabase.query(getTableName(), getFieldNames(), "_id = ?", new String[]{Long.toString(j)}, null, null, null);
        if (!cursorQuery.moveToFirst()) {
            cursorQuery.close();
            throw new DatabaseBindingException(getClass(), "not found: _id = " + j);
        }
        loadFromCursor(cursorQuery);
        cursorQuery.close();
    }

    protected UUID UUIDfromBlob(byte[] bArr) {
        ByteBuffer byteBufferWrap = ByteBuffer.wrap(bArr);
        return new UUID(byteBufferWrap.getLong(), byteBufferWrap.getLong());
    }

    protected byte[] UUIDtoBlob(UUID uuid) {
        ByteBuffer byteBufferWrap = ByteBuffer.wrap(new byte[16]);
        byteBufferWrap.putLong(uuid.getMostSignificantBits());
        byteBufferWrap.putLong(uuid.getLeastSignificantBits());
        return byteBufferWrap.array();
    }

    public abstract void bindInsertOrUpdate(SQLiteStatement sQLiteStatement);

    public void delete(SQLiteDatabase sQLiteDatabase) throws DatabaseBindingException {
        if (sQLiteDatabase == null) {
            throw new DatabaseBindingException(getClass(), "database not opened.");
        }
        if (this._id != 0) {
            sQLiteDatabase.delete(getTableName(), "_id = ?", new String[]{Long.toString(this._id)});
        }
    }

    public abstract ContentValues getContentValues();

    protected abstract String[] getFieldNames();

    public long getId() {
        return this._id;
    }

    protected abstract String getTableName();

    public abstract void loadFromCursor(Cursor cursor);

    public void reload(SQLiteDatabase sQLiteDatabase) throws DatabaseBindingException {
        if (sQLiteDatabase == null) {
            throw new DatabaseBindingException(getClass(), "database not opened.");
        }
        if (this._id != 0) {
            Cursor cursorQuery = sQLiteDatabase.query(getTableName(), getFieldNames(), "_id = ?", new String[]{Long.toString(this._id)}, null, null, null);
            if (cursorQuery.moveToFirst()) {
                loadFromCursor(cursorQuery);
            }
            cursorQuery.close();
        }
    }

    public void resetId() {
        this._id = 0L;
    }

    public void save(SQLiteDatabase sQLiteDatabase) throws DatabaseBindingException {
        if (sQLiteDatabase == null) {
            throw new DatabaseBindingException(getClass(), "database not opened.");
        }
        String tableName = getTableName();
        ContentValues contentValues = getContentValues();
        try {
            if (this._id != 0) {
                sQLiteDatabase.update(tableName, contentValues, "_id = ?", new String[]{Long.toString(this._id)});
            } else {
                this._id = sQLiteDatabase.insert(tableName, null, contentValues);
            }
        } catch (SQLiteException e) {
            DatabaseBindingException databaseBindingException = new DatabaseBindingException(getClass(), "insert or update failed");
            databaseBindingException.initCause(e);
            throw databaseBindingException;
        }
    }

    protected void updateOrInsert(SQLiteDatabase sQLiteDatabase, String str, String[] strArr) throws DatabaseBindingException {
        if (sQLiteDatabase == null) {
            throw new DatabaseBindingException(getClass(), "database not opened.");
        }
        String tableName = getTableName();
        ContentValues contentValues = getContentValues();
        try {
            if (sQLiteDatabase.update(tableName, contentValues, str, strArr) == 0) {
                this._id = sQLiteDatabase.insert(tableName, null, contentValues);
            }
        } catch (SQLiteException e) {
            DatabaseBindingException databaseBindingException = new DatabaseBindingException(getClass(), "insert or update failed");
            databaseBindingException.initCause(e);
            throw databaseBindingException;
        }
    }

    protected void updateOrInsert(SQLiteStatement sQLiteStatement, SQLiteStatement sQLiteStatement2) throws DatabaseBindingException {
        try {
            bindInsertOrUpdate(sQLiteStatement);
            if (sQLiteStatement.executeUpdateDelete() == 0) {
                bindInsertOrUpdate(sQLiteStatement2);
                this._id = sQLiteStatement2.executeInsert();
            }
        } catch (SQLiteException e) {
            DatabaseBindingException databaseBindingException = new DatabaseBindingException(getClass(), "insert or update failed");
            databaseBindingException.initCause(e);
            throw databaseBindingException;
        }
    }
}
