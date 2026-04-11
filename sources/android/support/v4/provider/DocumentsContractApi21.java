package android.support.v4.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
@RequiresApi(21)
class DocumentsContractApi21 {
    private static final String TAG = "DocumentFile";

    DocumentsContractApi21() {
    }

    private static void closeQuietly(AutoCloseable autoCloseable) {
        if (autoCloseable == null) {
            return;
        }
        try {
            autoCloseable.close();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e2) {
        }
    }

    public static Uri createDirectory(Context context, Uri uri, String str) {
        return createFile(context, uri, "vnd.android.document/directory", str);
    }

    public static Uri createFile(Context context, Uri uri, String str, String str2) {
        try {
            return DocumentsContract.createDocument(context.getContentResolver(), uri, str, str2);
        } catch (Exception e) {
            return null;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [android.content.ContentResolver] */
    /* JADX WARN: Type inference failed for: r1v1, types: [android.net.Uri] */
    /* JADX WARN: Type inference failed for: r1v2 */
    /* JADX WARN: Type inference failed for: r1v4, types: [java.lang.AutoCloseable] */
    public static Uri[] listFiles(Context context, Uri uri) throws Throwable {
        Cursor cursorQuery;
        ?? contentResolver = context.getContentResolver();
        ?? BuildChildDocumentsUriUsingTree = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));
        ArrayList arrayList = new ArrayList();
        try {
            try {
                cursorQuery = contentResolver.query(BuildChildDocumentsUriUsingTree, new String[]{"document_id"}, null, null, null);
                while (cursorQuery.moveToNext()) {
                    try {
                        arrayList.add(DocumentsContract.buildDocumentUriUsingTree(uri, cursorQuery.getString(0)));
                    } catch (Exception e) {
                        e = e;
                        Log.w(TAG, "Failed query: " + e);
                        closeQuietly(cursorQuery);
                    }
                }
                closeQuietly(cursorQuery);
            } catch (Exception e2) {
                e = e2;
                cursorQuery = null;
            } catch (Throwable th) {
                th = th;
                BuildChildDocumentsUriUsingTree = 0;
                closeQuietly(BuildChildDocumentsUriUsingTree);
                throw th;
            }
            return (Uri[]) arrayList.toArray(new Uri[arrayList.size()]);
        } catch (Throwable th2) {
            th = th2;
            closeQuietly(BuildChildDocumentsUriUsingTree);
            throw th;
        }
    }

    public static Uri prepareTreeUri(Uri uri) {
        return DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
    }

    public static Uri renameTo(Context context, Uri uri, String str) {
        try {
            return DocumentsContract.renameDocument(context.getContentResolver(), uri, str);
        } catch (Exception e) {
            return null;
        }
    }
}
