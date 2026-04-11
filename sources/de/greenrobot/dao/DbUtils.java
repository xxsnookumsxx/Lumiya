package de.greenrobot.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* JADX INFO: loaded from: classes.dex */
public class DbUtils {
    public static int copyAllBytes(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bArr = new byte[4096];
        int i = 0;
        while (true) {
            int i2 = inputStream.read(bArr);
            if (i2 == -1) {
                return i;
            }
            outputStream.write(bArr, 0, i2);
            i += i2;
        }
    }

    public static int executeSqlScript(Context context, SQLiteDatabase sQLiteDatabase, String str) throws IOException {
        return executeSqlScript(context, sQLiteDatabase, str, true);
    }

    public static int executeSqlScript(Context context, SQLiteDatabase sQLiteDatabase, String str, boolean z) throws IOException {
        String[] strArrSplit = new String(readAsset(context, str), "UTF-8").split(";(\\s)*[\n\r]");
        int iExecuteSqlStatements = !z ? executeSqlStatements(sQLiteDatabase, strArrSplit) : executeSqlStatementsInTx(sQLiteDatabase, strArrSplit);
        DaoLog.i("Executed " + iExecuteSqlStatements + " statements from SQL script '" + str + "'");
        return iExecuteSqlStatements;
    }

    public static int executeSqlStatements(SQLiteDatabase sQLiteDatabase, String[] strArr) {
        int i = 0;
        for (String str : strArr) {
            String strTrim = str.trim();
            if (strTrim.length() > 0) {
                sQLiteDatabase.execSQL(strTrim);
                i++;
            }
        }
        return i;
    }

    public static int executeSqlStatementsInTx(SQLiteDatabase sQLiteDatabase, String[] strArr) {
        sQLiteDatabase.beginTransaction();
        try {
            int iExecuteSqlStatements = executeSqlStatements(sQLiteDatabase, strArr);
            sQLiteDatabase.setTransactionSuccessful();
            return iExecuteSqlStatements;
        } finally {
            sQLiteDatabase.endTransaction();
        }
    }

    public static void logTableDump(SQLiteDatabase sQLiteDatabase, String str) {
        Cursor cursorQuery = sQLiteDatabase.query(str, null, null, null, null, null, null);
        try {
            DaoLog.d(DatabaseUtils.dumpCursorToString(cursorQuery));
        } finally {
            cursorQuery.close();
        }
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        copyAllBytes(inputStream, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] readAsset(Context context, String str) throws IOException {
        InputStream inputStreamOpen = context.getResources().getAssets().open(str);
        try {
            return readAllBytes(inputStreamOpen);
        } finally {
            inputStreamOpen.close();
        }
    }

    public static void vacuum(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("VACUUM");
    }
}
