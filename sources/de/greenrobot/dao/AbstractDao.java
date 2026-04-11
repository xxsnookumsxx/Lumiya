package de.greenrobot.dao;

import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import de.greenrobot.dao.identityscope.IdentityScope;
import de.greenrobot.dao.identityscope.IdentityScopeLong;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.internal.FastCursor;
import de.greenrobot.dao.internal.TableStatements;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public abstract class AbstractDao<T, K> {
    protected final DaoConfig config;
    protected final SQLiteDatabase db;
    protected IdentityScope<K, T> identityScope;
    protected IdentityScopeLong<T> identityScopeLong;
    protected final int pkOrdinal;
    protected final AbstractDaoSession session;
    protected TableStatements statements;

    public AbstractDao(DaoConfig daoConfig) {
        this(daoConfig, null);
    }

    public AbstractDao(DaoConfig daoConfig, AbstractDaoSession abstractDaoSession) {
        this.config = daoConfig;
        this.session = abstractDaoSession;
        this.db = daoConfig.db;
        this.identityScope = (IdentityScope<K, T>) daoConfig.getIdentityScope();
        if (this.identityScope instanceof IdentityScopeLong) {
            this.identityScopeLong = (IdentityScopeLong) this.identityScope;
        }
        this.statements = daoConfig.statements;
        this.pkOrdinal = daoConfig.pkProperty == null ? -1 : daoConfig.pkProperty.ordinal;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void deleteByKeyInsideSynchronized(K k, SQLiteStatement sQLiteStatement) {
        if (k instanceof Long) {
            sQLiteStatement.bindLong(1, ((Long) k).longValue());
        } else {
            if (k == 0) {
                throw new DaoException("Cannot delete entity, key is null");
            }
            sQLiteStatement.bindString(1, k.toString());
        }
        sQLiteStatement.execute();
    }

    /* JADX WARN: Finally extract failed */
    /* JADX WARN: Removed duplicated region for block: B:33:0x0060 A[Catch: all -> 0x005a, TRY_ENTER, TryCatch #2 {all -> 0x005a, blocks: (B:33:0x0060, B:34:0x0064, B:36:0x006a, B:38:0x0073, B:22:0x003f, B:23:0x0043, B:25:0x0049, B:27:0x0056), top: B:49:0x003f, outer: #0 }] */
    /* JADX WARN: Removed duplicated region for block: B:40:0x0077 A[Catch: all -> 0x0035, Merged into TryCatch #1 {all -> 0x0038, blocks: (B:3:0x000f, B:11:0x001d, B:43:0x0083, B:45:0x0087, B:18:0x0037, B:4:0x0010, B:8:0x0018, B:10:0x001c, B:40:0x0077, B:30:0x005b, B:32:0x005f, B:41:0x007d, B:15:0x002a, B:33:0x0060, B:34:0x0064, B:36:0x006a, B:38:0x0073, B:22:0x003f, B:23:0x0043, B:25:0x0049, B:27:0x0056), top: B:48:0x000f }, TRY_ENTER] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void deleteInTxInternal(java.lang.Iterable<T> r5, java.lang.Iterable<K> r6) {
        /*
            r4 = this;
            r0 = 0
            r4.assertSinglePk()
            de.greenrobot.dao.internal.TableStatements r1 = r4.statements
            android.database.sqlite.SQLiteStatement r1 = r1.getDeleteStatement()
            android.database.sqlite.SQLiteDatabase r2 = r4.db
            r2.beginTransaction()
            monitor-enter(r1)     // Catch: java.lang.Throwable -> L38
            de.greenrobot.dao.identityscope.IdentityScope<K, T> r2 = r4.identityScope     // Catch: java.lang.Throwable -> L35
            if (r2 != 0) goto L2a
        L14:
            if (r5 != 0) goto L3f
        L16:
            if (r6 != 0) goto L60
        L18:
            de.greenrobot.dao.identityscope.IdentityScope<K, T> r2 = r4.identityScope     // Catch: java.lang.Throwable -> L35
            if (r2 != 0) goto L77
        L1c:
            monitor-exit(r1)     // Catch: java.lang.Throwable -> L35
            android.database.sqlite.SQLiteDatabase r1 = r4.db     // Catch: java.lang.Throwable -> L38
            r1.setTransactionSuccessful()     // Catch: java.lang.Throwable -> L38
            if (r0 != 0) goto L83
        L24:
            android.database.sqlite.SQLiteDatabase r0 = r4.db
            r0.endTransaction()
            return
        L2a:
            de.greenrobot.dao.identityscope.IdentityScope<K, T> r0 = r4.identityScope     // Catch: java.lang.Throwable -> L35
            r0.lock()     // Catch: java.lang.Throwable -> L35
            java.util.ArrayList r0 = new java.util.ArrayList     // Catch: java.lang.Throwable -> L35
            r0.<init>()     // Catch: java.lang.Throwable -> L35
            goto L14
        L35:
            r0 = move-exception
            monitor-exit(r1)     // Catch: java.lang.Throwable -> L35
            throw r0     // Catch: java.lang.Throwable -> L38
        L38:
            r0 = move-exception
            android.database.sqlite.SQLiteDatabase r1 = r4.db
            r1.endTransaction()
            throw r0
        L3f:
            java.util.Iterator r2 = r5.iterator()     // Catch: java.lang.Throwable -> L5a
        L43:
            boolean r3 = r2.hasNext()     // Catch: java.lang.Throwable -> L5a
            if (r3 == 0) goto L16
            java.lang.Object r3 = r2.next()     // Catch: java.lang.Throwable -> L5a
            java.lang.Object r3 = r4.getKeyVerified(r3)     // Catch: java.lang.Throwable -> L5a
            r4.deleteByKeyInsideSynchronized(r3, r1)     // Catch: java.lang.Throwable -> L5a
            if (r0 == 0) goto L43
            r0.add(r3)     // Catch: java.lang.Throwable -> L5a
            goto L43
        L5a:
            r0 = move-exception
            de.greenrobot.dao.identityscope.IdentityScope<K, T> r2 = r4.identityScope     // Catch: java.lang.Throwable -> L35
            if (r2 != 0) goto L7d
        L5f:
            throw r0     // Catch: java.lang.Throwable -> L35
        L60:
            java.util.Iterator r2 = r6.iterator()     // Catch: java.lang.Throwable -> L5a
        L64:
            boolean r3 = r2.hasNext()     // Catch: java.lang.Throwable -> L5a
            if (r3 == 0) goto L18
            java.lang.Object r3 = r2.next()     // Catch: java.lang.Throwable -> L5a
            r4.deleteByKeyInsideSynchronized(r3, r1)     // Catch: java.lang.Throwable -> L5a
            if (r0 == 0) goto L64
            r0.add(r3)     // Catch: java.lang.Throwable -> L5a
            goto L64
        L77:
            de.greenrobot.dao.identityscope.IdentityScope<K, T> r2 = r4.identityScope     // Catch: java.lang.Throwable -> L35
            r2.unlock()     // Catch: java.lang.Throwable -> L35
            goto L1c
        L7d:
            de.greenrobot.dao.identityscope.IdentityScope<K, T> r2 = r4.identityScope     // Catch: java.lang.Throwable -> L35
            r2.unlock()     // Catch: java.lang.Throwable -> L35
            goto L5f
        L83:
            de.greenrobot.dao.identityscope.IdentityScope<K, T> r1 = r4.identityScope     // Catch: java.lang.Throwable -> L38
            if (r1 == 0) goto L24
            de.greenrobot.dao.identityscope.IdentityScope<K, T> r1 = r4.identityScope     // Catch: java.lang.Throwable -> L38
            r1.remove(r0)     // Catch: java.lang.Throwable -> L38
            goto L24
        */
        throw new UnsupportedOperationException("Method not decompiled: de.greenrobot.dao.AbstractDao.deleteInTxInternal(java.lang.Iterable, java.lang.Iterable):void");
    }

    private long executeInsert(T t, SQLiteStatement sQLiteStatement) {
        long jExecuteInsert;
        if (this.db.isDbLockedByCurrentThread()) {
            synchronized (sQLiteStatement) {
                bindValues(sQLiteStatement, t);
                jExecuteInsert = sQLiteStatement.executeInsert();
            }
        } else {
            this.db.beginTransaction();
            try {
                synchronized (sQLiteStatement) {
                    bindValues(sQLiteStatement, t);
                    jExecuteInsert = sQLiteStatement.executeInsert();
                }
                this.db.setTransactionSuccessful();
            } finally {
                this.db.endTransaction();
            }
        }
        updateKeyAfterInsertAndAttach(t, jExecuteInsert, true);
        return jExecuteInsert;
    }

    /* JADX WARN: Finally extract failed */
    private void executeInsertInTx(SQLiteStatement sQLiteStatement, Iterable<T> iterable, boolean z) {
        this.db.beginTransaction();
        try {
            synchronized (sQLiteStatement) {
                if (this.identityScope != null) {
                    this.identityScope.lock();
                }
                try {
                    for (T t : iterable) {
                        bindValues(sQLiteStatement, t);
                        if (z) {
                            updateKeyAfterInsertAndAttach(t, sQLiteStatement.executeInsert(), false);
                        } else {
                            sQLiteStatement.execute();
                        }
                    }
                    if (this.identityScope != null) {
                        this.identityScope.unlock();
                    }
                } catch (Throwable th) {
                    if (this.identityScope != null) {
                        this.identityScope.unlock();
                    }
                    throw th;
                }
            }
            this.db.setTransactionSuccessful();
        } finally {
            this.db.endTransaction();
        }
    }

    protected void assertSinglePk() {
        if (this.config.pkColumns.length != 1) {
            throw new DaoException(this + " (" + this.config.tablename + ") does not have a single-column primary key");
        }
    }

    protected void attachEntity(T t) {
    }

    protected final void attachEntity(K k, T t, boolean z) {
        attachEntity(t);
        if (this.identityScope == null || k == null) {
            return;
        }
        if (z) {
            this.identityScope.put(k, t);
        } else {
            this.identityScope.putNoLock(k, t);
        }
    }

    protected abstract void bindValues(SQLiteStatement sQLiteStatement, T t);

    public long count() {
        return DatabaseUtils.queryNumEntries(this.db, '\'' + this.config.tablename + '\'');
    }

    public void delete(T t) {
        assertSinglePk();
        deleteByKey(getKeyVerified(t));
    }

    public void deleteAll() {
        this.db.execSQL("DELETE FROM '" + this.config.tablename + "'");
        if (this.identityScope == null) {
            return;
        }
        this.identityScope.clear();
    }

    public void deleteByKey(K k) {
        assertSinglePk();
        SQLiteStatement deleteStatement = this.statements.getDeleteStatement();
        if (this.db.isDbLockedByCurrentThread()) {
            synchronized (deleteStatement) {
                deleteByKeyInsideSynchronized(k, deleteStatement);
            }
        } else {
            this.db.beginTransaction();
            try {
                synchronized (deleteStatement) {
                    deleteByKeyInsideSynchronized(k, deleteStatement);
                }
                this.db.setTransactionSuccessful();
            } finally {
                this.db.endTransaction();
            }
        }
        if (this.identityScope == null) {
            return;
        }
        this.identityScope.remove(k);
    }

    public void deleteByKeyInTx(Iterable<K> iterable) {
        deleteInTxInternal(null, iterable);
    }

    public void deleteByKeyInTx(K... kArr) {
        deleteInTxInternal(null, Arrays.asList(kArr));
    }

    public void deleteInTx(Iterable<T> iterable) {
        deleteInTxInternal(iterable, null);
    }

    public void deleteInTx(T... tArr) {
        deleteInTxInternal(Arrays.asList(tArr), null);
    }

    public boolean detach(T t) {
        if (this.identityScope == null) {
            return false;
        }
        return this.identityScope.detach(getKeyVerified(t), t);
    }

    public String[] getAllColumns() {
        return this.config.allColumns;
    }

    public SQLiteDatabase getDatabase() {
        return this.db;
    }

    protected abstract K getKey(T t);

    protected K getKeyVerified(T t) {
        K key = getKey(t);
        if (key != null) {
            return key;
        }
        if (t != null) {
            throw new DaoException("Entity has no key");
        }
        throw new NullPointerException("Entity may not be null");
    }

    public String[] getNonPkColumns() {
        return this.config.nonPkColumns;
    }

    public String[] getPkColumns() {
        return this.config.pkColumns;
    }

    public Property getPkProperty() {
        return this.config.pkProperty;
    }

    public Property[] getProperties() {
        return this.config.properties;
    }

    public AbstractDaoSession getSession() {
        return this.session;
    }

    TableStatements getStatements() {
        return this.config.statements;
    }

    public String getTablename() {
        return this.config.tablename;
    }

    public long insert(T t) {
        return executeInsert(t, this.statements.getInsertStatement());
    }

    public void insertInTx(Iterable<T> iterable) {
        insertInTx(iterable, isEntityUpdateable());
    }

    public void insertInTx(Iterable<T> iterable, boolean z) {
        executeInsertInTx(this.statements.getInsertStatement(), iterable, z);
    }

    public void insertInTx(T... tArr) {
        insertInTx(Arrays.asList(tArr), isEntityUpdateable());
    }

    public long insertOrReplace(T t) {
        return executeInsert(t, this.statements.getInsertOrReplaceStatement());
    }

    public void insertOrReplaceInTx(Iterable<T> iterable) {
        insertOrReplaceInTx(iterable, isEntityUpdateable());
    }

    public void insertOrReplaceInTx(Iterable<T> iterable, boolean z) {
        executeInsertInTx(this.statements.getInsertOrReplaceStatement(), iterable, z);
    }

    public void insertOrReplaceInTx(T... tArr) {
        insertOrReplaceInTx(Arrays.asList(tArr), isEntityUpdateable());
    }

    public long insertWithoutSettingPk(T t) {
        long jExecuteInsert;
        SQLiteStatement insertStatement = this.statements.getInsertStatement();
        if (this.db.isDbLockedByCurrentThread()) {
            synchronized (insertStatement) {
                bindValues(insertStatement, t);
                jExecuteInsert = insertStatement.executeInsert();
            }
        } else {
            this.db.beginTransaction();
            try {
                synchronized (insertStatement) {
                    bindValues(insertStatement, t);
                    jExecuteInsert = insertStatement.executeInsert();
                }
                this.db.setTransactionSuccessful();
            } finally {
                this.db.endTransaction();
            }
        }
        return jExecuteInsert;
    }

    protected abstract boolean isEntityUpdateable();

    public T load(K k) {
        T t;
        assertSinglePk();
        if (k != null) {
            return (this.identityScope == null || (t = this.identityScope.get(k)) == null) ? loadUniqueAndCloseCursor(this.db.rawQuery(this.statements.getSelectByKey(), new String[]{k.toString()})) : t;
        }
        return null;
    }

    public List<T> loadAll() {
        return loadAllAndCloseCursor(this.db.rawQuery(this.statements.getSelectAll(), null));
    }

    protected List<T> loadAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }

    protected List<T> loadAllFromCursor(Cursor cursor) {
        CursorWindow window;
        int count = cursor.getCount();
        ArrayList arrayList = new ArrayList(count);
        if ((cursor instanceof CrossProcessCursor) && (window = ((CrossProcessCursor) cursor).getWindow()) != null) {
            if (window.getNumRows() != count) {
                DaoLog.d("Window vs. result size: " + window.getNumRows() + "/" + count);
            } else {
                cursor = new FastCursor(window);
            }
        }
        if (cursor.moveToFirst()) {
            if (this.identityScope != null) {
                this.identityScope.lock();
                this.identityScope.reserveRoom(count);
            }
            do {
                try {
                    arrayList.add(loadCurrent(cursor, 0, false));
                } catch (Throwable th) {
                    if (this.identityScope != null) {
                        this.identityScope.unlock();
                    }
                    throw th;
                }
            } while (cursor.moveToNext());
            if (this.identityScope != null) {
                this.identityScope.unlock();
            }
        }
        return arrayList;
    }

    public T loadByRowId(long j) {
        return loadUniqueAndCloseCursor(this.db.rawQuery(this.statements.getSelectByRowId(), new String[]{Long.toString(j)}));
    }

    protected final T loadCurrent(Cursor cursor, int i, boolean z) {
        if (this.identityScopeLong != null) {
            if (i != 0 && cursor.isNull(this.pkOrdinal + i)) {
                return null;
            }
            long j = cursor.getLong(this.pkOrdinal + i);
            T t = !z ? this.identityScopeLong.get2NoLock(j) : this.identityScopeLong.get2(j);
            if (t != null) {
                return t;
            }
            T entity = readEntity(cursor, i);
            attachEntity(entity);
            if (z) {
                this.identityScopeLong.put2(j, entity);
            } else {
                this.identityScopeLong.put2NoLock(j, entity);
            }
            return entity;
        }
        if (this.identityScope == null) {
            if (i != 0 && readKey(cursor, i) == null) {
                return null;
            }
            T entity2 = readEntity(cursor, i);
            attachEntity(entity2);
            return entity2;
        }
        K key = readKey(cursor, i);
        if (i != 0 && key == null) {
            return null;
        }
        T noLock = !z ? this.identityScope.getNoLock(key) : this.identityScope.get(key);
        if (noLock != null) {
            return noLock;
        }
        T entity3 = readEntity(cursor, i);
        attachEntity(key, entity3, z);
        return entity3;
    }

    protected final <O> O loadCurrentOther(AbstractDao<O, ?> abstractDao, Cursor cursor, int i) {
        return abstractDao.loadCurrent(cursor, i, true);
    }

    protected T loadUnique(Cursor cursor) {
        if (!cursor.moveToFirst()) {
            return null;
        }
        if (cursor.isLast()) {
            return loadCurrent(cursor, 0, true);
        }
        throw new DaoException("Expected unique result, but count was " + cursor.getCount());
    }

    protected T loadUniqueAndCloseCursor(Cursor cursor) {
        try {
            return loadUnique(cursor);
        } finally {
            cursor.close();
        }
    }

    public QueryBuilder<T> queryBuilder() {
        return QueryBuilder.internalCreate(this);
    }

    public List<T> queryRaw(String str, String... strArr) {
        return loadAllAndCloseCursor(this.db.rawQuery(this.statements.getSelectAll() + str, strArr));
    }

    public Query<T> queryRawCreate(String str, Object... objArr) {
        return queryRawCreateListArgs(str, Arrays.asList(objArr));
    }

    public Query<T> queryRawCreateListArgs(String str, Collection<Object> collection) {
        return Query.internalCreate(this, this.statements.getSelectAll() + str, collection.toArray());
    }

    protected abstract T readEntity(Cursor cursor, int i);

    protected abstract void readEntity(Cursor cursor, T t, int i);

    protected abstract K readKey(Cursor cursor, int i);

    public void refresh(T t) {
        assertSinglePk();
        K keyVerified = getKeyVerified(t);
        Cursor cursorRawQuery = this.db.rawQuery(this.statements.getSelectByKey(), new String[]{keyVerified.toString()});
        try {
            if (!cursorRawQuery.moveToFirst()) {
                throw new DaoException("Entity does not exist in the database anymore: " + t.getClass() + " with key " + keyVerified);
            }
            if (!cursorRawQuery.isLast()) {
                throw new DaoException("Expected unique result, but count was " + cursorRawQuery.getCount());
            }
            readEntity(cursorRawQuery, t, 0);
            attachEntity(keyVerified, t, true);
        } finally {
            cursorRawQuery.close();
        }
    }

    public void update(T t) {
        assertSinglePk();
        SQLiteStatement updateStatement = this.statements.getUpdateStatement();
        if (this.db.isDbLockedByCurrentThread()) {
            synchronized (updateStatement) {
                updateInsideSynchronized(t, updateStatement, true);
            }
            return;
        }
        this.db.beginTransaction();
        try {
            synchronized (updateStatement) {
                updateInsideSynchronized(t, updateStatement, true);
            }
            this.db.setTransactionSuccessful();
        } finally {
            this.db.endTransaction();
        }
    }

    /* JADX WARN: Finally extract failed */
    public void updateInTx(Iterable<T> iterable) {
        SQLiteStatement updateStatement = this.statements.getUpdateStatement();
        this.db.beginTransaction();
        try {
            synchronized (updateStatement) {
                if (this.identityScope != null) {
                    this.identityScope.lock();
                }
                try {
                    Iterator<T> it = iterable.iterator();
                    while (it.hasNext()) {
                        updateInsideSynchronized(it.next(), updateStatement, false);
                    }
                    if (this.identityScope != null) {
                        this.identityScope.unlock();
                    }
                } catch (Throwable th) {
                    if (this.identityScope != null) {
                        this.identityScope.unlock();
                    }
                    throw th;
                }
            }
            this.db.setTransactionSuccessful();
            try {
                this.db.endTransaction();
            } catch (RuntimeException e) {
                if (0 == 0) {
                    throw e;
                }
                DaoLog.w("Could not end transaction (rethrowing initial exception)", e);
                throw null;
            }
        } catch (RuntimeException e2) {
            try {
                this.db.endTransaction();
            } catch (RuntimeException e3) {
                if (e2 == null) {
                    throw e3;
                }
                DaoLog.w("Could not end transaction (rethrowing initial exception)", e3);
                throw e2;
            }
        } catch (Throwable th2) {
            try {
                this.db.endTransaction();
                throw th2;
            } catch (RuntimeException e4) {
                if (0 == 0) {
                    throw e4;
                }
                DaoLog.w("Could not end transaction (rethrowing initial exception)", e4);
                throw null;
            }
        }
    }

    public void updateInTx(T... tArr) {
        updateInTx(Arrays.asList(tArr));
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected void updateInsideSynchronized(T t, SQLiteStatement sQLiteStatement, boolean z) {
        bindValues(sQLiteStatement, t);
        int length = this.config.allColumns.length + 1;
        Object key = getKey(t);
        if (key instanceof Long) {
            sQLiteStatement.bindLong(length, ((Long) key).longValue());
        } else {
            if (key == null) {
                throw new DaoException("Cannot update entity without key - was it inserted before?");
            }
            sQLiteStatement.bindString(length, key.toString());
        }
        sQLiteStatement.execute();
        attachEntity(key, t, z);
    }

    protected abstract K updateKeyAfterInsert(T t, long j);

    protected void updateKeyAfterInsertAndAttach(T t, long j, boolean z) {
        if (j != -1) {
            attachEntity(updateKeyAfterInsert(t, j), t, z);
        } else {
            DaoLog.w("Could not insert row (executeInsert returned -1)");
        }
    }
}
