package android.support.v4.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

/* JADX INFO: loaded from: classes.dex */
@RequiresApi(19)
class DocumentsContractApi19 {
    private static final int FLAG_VIRTUAL_DOCUMENT = 512;
    private static final String TAG = "DocumentFile";

    DocumentsContractApi19() {
    }

    public static boolean canRead(Context context, Uri uri) {
        return context.checkCallingOrSelfUriPermission(uri, 1) == 0 && !TextUtils.isEmpty(getRawType(context, uri));
    }

    public static boolean canWrite(Context context, Uri uri) {
        if (context.checkCallingOrSelfUriPermission(uri, 2) != 0) {
            return false;
        }
        String rawType = getRawType(context, uri);
        int iQueryForInt = queryForInt(context, uri, "flags", 0);
        if (TextUtils.isEmpty(rawType)) {
            return false;
        }
        if ((iQueryForInt & 4) != 0) {
            return true;
        }
        if ("vnd.android.document/directory".equals(rawType) && (iQueryForInt & 8) != 0) {
            return true;
        }
        return (TextUtils.isEmpty(rawType) || (iQueryForInt & 2) == 0) ? false : true;
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

    public static boolean delete(Context context, Uri uri) {
        try {
            return DocumentsContract.deleteDocument(context.getContentResolver(), uri);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean exists(Context context, Uri uri) throws Throwable {
        Cursor cursorQuery;
        try {
            cursorQuery = context.getContentResolver().query(uri, new String[]{"document_id"}, null, null, null);
        } catch (Exception e) {
            e = e;
            cursorQuery = null;
        } catch (Throwable th) {
            th = th;
            cursorQuery = null;
            closeQuietly(cursorQuery);
            throw th;
        }
        try {
            try {
                boolean z = cursorQuery.getCount() > 0;
                closeQuietly(cursorQuery);
                return z;
            } catch (Throwable th2) {
                th = th2;
                closeQuietly(cursorQuery);
                throw th;
            }
        } catch (Exception e2) {
            e = e2;
            Log.w(TAG, "Failed query: " + e);
            closeQuietly(cursorQuery);
            return false;
        }
    }

    public static long getFlags(Context context, Uri uri) {
        return queryForLong(context, uri, "flags", 0L);
    }

    public static String getName(Context context, Uri uri) {
        return queryForString(context, uri, "_display_name", null);
    }

    private static String getRawType(Context context, Uri uri) {
        return queryForString(context, uri, "mime_type", null);
    }

    public static String getType(Context context, Uri uri) {
        String rawType = getRawType(context, uri);
        if ("vnd.android.document/directory".equals(rawType)) {
            return null;
        }
        return rawType;
    }

    public static boolean isDirectory(Context context, Uri uri) {
        return "vnd.android.document/directory".equals(getRawType(context, uri));
    }

    public static boolean isDocumentUri(Context context, Uri uri) {
        return DocumentsContract.isDocumentUri(context, uri);
    }

    public static boolean isFile(Context context, Uri uri) {
        String rawType = getRawType(context, uri);
        return ("vnd.android.document/directory".equals(rawType) || TextUtils.isEmpty(rawType)) ? false : true;
    }

    public static boolean isVirtual(Context context, Uri uri) {
        return isDocumentUri(context, uri) && (getFlags(context, uri) & 512) != 0;
    }

    public static long lastModified(Context context, Uri uri) {
        return queryForLong(context, uri, "last_modified", 0L);
    }

    public static long length(Context context, Uri uri) {
        return queryForLong(context, uri, "_size", 0L);
    }

    private static int queryForInt(Context context, Uri uri, String str, int i) {
        return (int) queryForLong(context, uri, str, i);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0 */
    /* JADX WARN: Type inference failed for: r1v1 */
    /* JADX WARN: Type inference failed for: r1v3, types: [java.lang.AutoCloseable] */
    private static long queryForLong(Context context, Uri uri, String str, long j) throws Throwable {
        Cursor cursorQuery;
        ?? r1 = 1;
        try {
            try {
                cursorQuery = context.getContentResolver().query(uri, new String[]{str}, null, null, null);
            } catch (Exception e) {
                e = e;
                cursorQuery = null;
            } catch (Throwable th) {
                th = th;
                r1 = 0;
                closeQuietly(r1);
                throw th;
            }
            try {
                if (!cursorQuery.moveToFirst() || cursorQuery.isNull(0)) {
                    closeQuietly(cursorQuery);
                    return j;
                }
                long j2 = cursorQuery.getLong(0);
                closeQuietly(cursorQuery);
                return j2;
            } catch (Exception e2) {
                e = e2;
                Log.w(TAG, "Failed query: " + e);
                closeQuietly(cursorQuery);
                return j;
            }
        } catch (Throwable th2) {
            th = th2;
            closeQuietly(r1);
            throw th;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0 */
    /* JADX WARN: Type inference failed for: r1v1 */
    /* JADX WARN: Type inference failed for: r1v3, types: [java.lang.AutoCloseable] */
    private static String queryForString(Context context, Uri uri, String str, String str2) throws Throwable {
        Cursor cursorQuery;
        ?? r1 = 1;
        try {
            try {
                cursorQuery = context.getContentResolver().query(uri, new String[]{str}, null, null, null);
            } catch (Exception e) {
                e = e;
                cursorQuery = null;
            } catch (Throwable th) {
                th = th;
                r1 = 0;
                closeQuietly(r1);
                throw th;
            }
            try {
                if (!cursorQuery.moveToFirst() || cursorQuery.isNull(0)) {
                    closeQuietly(cursorQuery);
                    return str2;
                }
                String string = cursorQuery.getString(0);
                closeQuietly(cursorQuery);
                return string;
            } catch (Exception e2) {
                e = e2;
                Log.w(TAG, "Failed query: " + e);
                closeQuietly(cursorQuery);
                return str2;
            }
        } catch (Throwable th2) {
            th = th2;
            closeQuietly(r1);
            throw th;
        }
    }
}
