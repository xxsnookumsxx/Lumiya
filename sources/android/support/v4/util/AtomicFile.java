package android.support.v4.util;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class AtomicFile {
    private final File mBackupName;
    private final File mBaseName;

    public AtomicFile(File file) {
        this.mBaseName = file;
        this.mBackupName = new File(file.getPath() + ".bak");
    }

    static boolean sync(FileOutputStream fileOutputStream) {
        if (fileOutputStream == null) {
            return true;
        }
        try {
            fileOutputStream.getFD().sync();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void delete() {
        this.mBaseName.delete();
        this.mBackupName.delete();
    }

    public void failWrite(FileOutputStream fileOutputStream) {
        if (fileOutputStream == null) {
            return;
        }
        sync(fileOutputStream);
        try {
            fileOutputStream.close();
            this.mBaseName.delete();
            this.mBackupName.renameTo(this.mBaseName);
        } catch (IOException e) {
            Log.w("AtomicFile", "failWrite: Got exception:", e);
        }
    }

    public void finishWrite(FileOutputStream fileOutputStream) {
        if (fileOutputStream == null) {
            return;
        }
        sync(fileOutputStream);
        try {
            fileOutputStream.close();
            this.mBackupName.delete();
        } catch (IOException e) {
            Log.w("AtomicFile", "finishWrite: Got exception:", e);
        }
    }

    public File getBaseFile() {
        return this.mBaseName;
    }

    public FileInputStream openRead() throws FileNotFoundException {
        if (this.mBackupName.exists()) {
            this.mBaseName.delete();
            this.mBackupName.renameTo(this.mBaseName);
        }
        return new FileInputStream(this.mBaseName);
    }

    public byte[] readFully() throws IOException {
        int i = 0;
        FileInputStream fileInputStreamOpenRead = openRead();
        try {
            byte[] bArr = new byte[fileInputStreamOpenRead.available()];
            while (true) {
                int i2 = fileInputStreamOpenRead.read(bArr, i, bArr.length - i);
                if (i2 <= 0) {
                    return bArr;
                }
                int i3 = i2 + i;
                int iAvailable = fileInputStreamOpenRead.available();
                if (iAvailable > bArr.length - i3) {
                    byte[] bArr2 = new byte[iAvailable + i3];
                    System.arraycopy(bArr, 0, bArr2, 0, i3);
                    bArr = bArr2;
                }
                i = i3;
            }
        } finally {
            fileInputStreamOpenRead.close();
        }
    }

    public FileOutputStream startWrite() throws IOException {
        if (this.mBaseName.exists()) {
            if (this.mBackupName.exists()) {
                this.mBaseName.delete();
            } else if (!this.mBaseName.renameTo(this.mBackupName)) {
                Log.w("AtomicFile", "Couldn't rename file " + this.mBaseName + " to backup file " + this.mBackupName);
            }
        }
        try {
            return new FileOutputStream(this.mBaseName);
        } catch (FileNotFoundException e) {
            if (!this.mBaseName.getParentFile().mkdirs()) {
                throw new IOException("Couldn't create directory " + this.mBaseName);
            }
            try {
                return new FileOutputStream(this.mBaseName);
            } catch (FileNotFoundException e2) {
                throw new IOException("Couldn't create " + this.mBaseName);
            }
        }
    }
}
