package de.greenrobot.dao.test;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public abstract class AbstractDaoTestSinglePk<D extends AbstractDao<T, K>, T, K> extends AbstractDaoTest<D, T, K> {
    private Property pkColumn;
    protected Set<K> usedPks;

    public AbstractDaoTestSinglePk(Class<D> cls) {
        super(cls);
        this.usedPks = new HashSet();
    }

    protected abstract T createEntity(K k);

    protected T createEntityWithRandomPk() {
        return createEntity(nextPk());
    }

    protected abstract K createRandomPk();

    protected K nextPk() {
        for (int i = 0; i < 100000; i++) {
            K kCreateRandomPk = createRandomPk();
            if (this.usedPks.add(kCreateRandomPk)) {
                return kCreateRandomPk;
            }
        }
        throw new IllegalStateException("Could not find a new PK");
    }

    protected Cursor queryWithDummyColumnsInFront(int i, String str, K k) {
        StringBuilder sb = new StringBuilder("SELECT ");
        for (int i2 = 0; i2 < i; i2++) {
            sb.append(str).append(",");
        }
        SqlUtils.appendColumns(sb, "T", this.dao.getAllColumns()).append(" FROM ");
        sb.append(this.dao.getTablename()).append(" T");
        if (k != null) {
            sb.append(" WHERE ");
            assertEquals(1, this.dao.getPkColumns().length);
            sb.append(this.dao.getPkColumns()[0]).append("=");
            DatabaseUtils.appendValueToSql(sb, k);
        }
        Cursor cursorRawQuery = this.db.rawQuery(sb.toString(), null);
        assertTrue(cursorRawQuery.moveToFirst());
        for (int i3 = 0; i3 < i; i3++) {
            try {
                assertEquals(str, cursorRawQuery.getString(i3));
            } catch (RuntimeException e) {
                cursorRawQuery.close();
                throw e;
            }
        }
        if (k != null) {
            assertEquals(1, cursorRawQuery.getCount());
        }
        return cursorRawQuery;
    }

    protected void runLoadPkTest(int i) {
        K kNextPk = nextPk();
        this.dao.insert(createEntity(kNextPk));
        Cursor cursorQueryWithDummyColumnsInFront = queryWithDummyColumnsInFront(i, "42", kNextPk);
        try {
            assertEquals(kNextPk, this.daoAccess.readKey(cursorQueryWithDummyColumnsInFront, i));
        } finally {
            cursorQueryWithDummyColumnsInFront.close();
        }
    }

    @Override // de.greenrobot.dao.test.AbstractDaoTest, de.greenrobot.dao.test.DbTest
    protected void setUp() throws Exception {
        super.setUp();
        for (Property property : this.daoAccess.getProperties()) {
            if (property.primaryKey) {
                if (this.pkColumn != null) {
                    throw new RuntimeException("Test does not work with multiple PK columns");
                }
                this.pkColumn = property;
            }
        }
        if (this.pkColumn == null) {
            throw new RuntimeException("Test does not work without a PK column");
        }
    }

    public void testCount() {
        this.dao.deleteAll();
        assertEquals(0L, this.dao.count());
        this.dao.insert(createEntityWithRandomPk());
        assertEquals(1L, this.dao.count());
        this.dao.insert(createEntityWithRandomPk());
        assertEquals(2L, this.dao.count());
    }

    public void testDelete() {
        K kNextPk = nextPk();
        this.dao.deleteByKey(kNextPk);
        this.dao.insert(createEntity(kNextPk));
        assertNotNull(this.dao.load(kNextPk));
        this.dao.deleteByKey(kNextPk);
        assertNull(this.dao.load(kNextPk));
    }

    public void testDeleteAll() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            arrayList.add(createEntityWithRandomPk());
        }
        this.dao.insertInTx(arrayList);
        this.dao.deleteAll();
        assertEquals(0L, this.dao.count());
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            K key = this.daoAccess.getKey((T) it.next());
            assertNotNull(key);
            assertNull(this.dao.load(key));
        }
    }

    /* JADX WARN: Type inference incomplete: some casts might be missing */
    public void testDeleteByKeyInTx() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            arrayList.add(createEntityWithRandomPk());
        }
        this.dao.insertInTx(arrayList);
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(this.daoAccess.getKey((T) arrayList.get(0)));
        arrayList2.add(this.daoAccess.getKey((T) arrayList.get(3)));
        arrayList2.add(this.daoAccess.getKey((T) arrayList.get(4)));
        arrayList2.add(this.daoAccess.getKey((T) arrayList.get(8)));
        this.dao.deleteByKeyInTx(arrayList2);
        assertEquals(arrayList.size() - arrayList2.size(), this.dao.count());
        for (Object obj : arrayList2) {
            assertNotNull(obj);
            assertNull(this.dao.load(obj));
        }
    }

    public void testDeleteInTx() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            arrayList.add(createEntityWithRandomPk());
        }
        this.dao.insertInTx(arrayList);
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(arrayList.get(0));
        arrayList2.add(arrayList.get(3));
        arrayList2.add(arrayList.get(4));
        arrayList2.add(arrayList.get(8));
        this.dao.deleteInTx(arrayList2);
        assertEquals(arrayList.size() - arrayList2.size(), this.dao.count());
        Iterator it = arrayList2.iterator();
        while (it.hasNext()) {
            K key = this.daoAccess.getKey((T) it.next());
            assertNotNull(key);
            assertNull(this.dao.load(key));
        }
    }

    public void testInsertAndLoad() {
        K kNextPk = nextPk();
        T tCreateEntity = createEntity(kNextPk);
        this.dao.insert(tCreateEntity);
        assertEquals(kNextPk, this.daoAccess.getKey(tCreateEntity));
        Object objLoad = this.dao.load(kNextPk);
        assertNotNull(objLoad);
        assertEquals(this.daoAccess.getKey(tCreateEntity), this.daoAccess.getKey((T) objLoad));
    }

    public void testInsertInTx() {
        this.dao.deleteAll();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 20; i++) {
            arrayList.add(createEntityWithRandomPk());
        }
        this.dao.insertInTx(arrayList);
        assertEquals(arrayList.size(), this.dao.count());
    }

    public void testInsertOrReplaceInTx() {
        this.dao.deleteAll();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < 20; i++) {
            T tCreateEntityWithRandomPk = createEntityWithRandomPk();
            if (i % 2 == 0) {
                arrayList.add(tCreateEntityWithRandomPk);
            }
            arrayList2.add(tCreateEntityWithRandomPk);
        }
        this.dao.insertOrReplaceInTx(arrayList);
        this.dao.insertOrReplaceInTx(arrayList2);
        assertEquals(arrayList2.size(), this.dao.count());
    }

    public void testInsertOrReplaceTwice() {
        T tCreateEntityWithRandomPk = createEntityWithRandomPk();
        long jInsert = this.dao.insert(tCreateEntityWithRandomPk);
        long jInsertOrReplace = this.dao.insertOrReplace(tCreateEntityWithRandomPk);
        if (this.dao.getPkProperty().type != Long.class) {
            return;
        }
        assertEquals(jInsert, jInsertOrReplace);
    }

    public void testInsertTwice() {
        T tCreateEntity = createEntity(nextPk());
        this.dao.insert(tCreateEntity);
        try {
            this.dao.insert(tCreateEntity);
            fail("Inserting twice should not work");
        } catch (SQLException e) {
        }
    }

    public void testLoadAll() {
        this.dao.deleteAll();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 15; i++) {
            arrayList.add(createEntity(nextPk()));
        }
        this.dao.insertInTx(arrayList);
        assertEquals(arrayList.size(), this.dao.loadAll().size());
    }

    public void testLoadPk() {
        runLoadPkTest(0);
    }

    public void testLoadPkWithOffset() {
        runLoadPkTest(10);
    }

    public void testQuery() {
        this.dao.insert(createEntityWithRandomPk());
        K kNextPk = nextPk();
        this.dao.insert(createEntity(kNextPk));
        this.dao.insert(createEntityWithRandomPk());
        List<T> listQueryRaw = this.dao.queryRaw("WHERE " + this.dao.getPkColumns()[0] + "=?", kNextPk.toString());
        assertEquals(1, listQueryRaw.size());
        assertEquals(kNextPk, this.daoAccess.getKey(listQueryRaw.get(0)));
    }

    public void testReadWithOffset() {
        K kNextPk = nextPk();
        this.dao.insert(createEntity(kNextPk));
        Cursor cursorQueryWithDummyColumnsInFront = queryWithDummyColumnsInFront(5, "42", kNextPk);
        try {
            assertEquals(kNextPk, this.daoAccess.getKey(this.daoAccess.readEntity(cursorQueryWithDummyColumnsInFront, 5)));
        } finally {
            cursorQueryWithDummyColumnsInFront.close();
        }
    }

    public void testRowId() {
        assertTrue(this.dao.insert(createEntityWithRandomPk()) != this.dao.insert(createEntityWithRandomPk()));
    }

    public void testUpdate() {
        this.dao.deleteAll();
        T tCreateEntityWithRandomPk = createEntityWithRandomPk();
        this.dao.insert(tCreateEntityWithRandomPk);
        this.dao.update(tCreateEntityWithRandomPk);
        assertEquals(1L, this.dao.count());
    }
}
