package com.lumiyaviewer.lumiya.ui.chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.dao.ChatMessage;
import com.lumiyaviewer.lumiya.react.UIThreadExecutor;
import com.lumiyaviewer.lumiya.slproto.chat.generic.SLChatEvent;
import com.lumiyaviewer.lumiya.slproto.users.ChatterID;
import com.lumiyaviewer.lumiya.slproto.users.ChatterNameRetriever;
import com.lumiyaviewer.lumiya.slproto.users.manager.UserManager;
import de.greenrobot.dao.query.LazyList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* JADX INFO: loaded from: classes.dex */
public class ExportChatHistoryTask extends AsyncTask<ChatterID, Void, ExportResult> implements DialogInterface.OnCancelListener {
    private static final String forbiddenChars = "./\\*?:\"'~";
    private final Context context;
    private ProgressDialog progressDialog;
    private final Lock nameReadyLock = new ReentrantLock();
    private final Condition nameReadyCondition = this.nameReadyLock.newCondition();
    private final AtomicBoolean isNameReady = new AtomicBoolean();
    private final AtomicReference<String> gotChatterName = new AtomicReference<>();
    private final ChatterNameRetriever.OnChatterNameUpdated onChatterNameUpdated = new ChatterNameRetriever.OnChatterNameUpdated() { // from class: com.lumiyaviewer.lumiya.ui.chat.-$Lambda$D705oXX7BTh_Xc4P_mIDvS9cOZI
        private final /* synthetic */ void $m$0(ChatterNameRetriever chatterNameRetriever) {
            ((ExportChatHistoryTask) this).m431x863366ea(chatterNameRetriever);
        }

        @Override // com.lumiyaviewer.lumiya.slproto.users.ChatterNameRetriever.OnChatterNameUpdated
        public final void onChatterNameUpdated(ChatterNameRetriever chatterNameRetriever) {
            $m$0(chatterNameRetriever);
        }
    };

    public ExportChatHistoryTask(Context context) {
        this.context = context;
    }

    private String sanitizeName(String str) {
        if (str == null) {
            str = "";
        }
        StringBuilder sb = new StringBuilder();
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char cCharAt = str.charAt(i);
            if (cCharAt < ' ') {
                cCharAt = ' ';
            }
            if (cCharAt > 127) {
                cCharAt = '_';
            }
            if (forbiddenChars.indexOf(cCharAt) < 0) {
                sb.append(cCharAt);
            }
        }
        String strTrim = sb.toString().trim();
        return strTrim.isEmpty() ? "Chat Log" : strTrim;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public ExportResult doInBackground(ChatterID... chatterIDArr) throws Throwable {
        ChatterID chatterID;
        UserManager userManager;
        File file;
        FileOutputStream fileOutputStream;
        LazyList<ChatMessage> messages;
        if (chatterIDArr.length != 1 || (userManager = (chatterID = chatterIDArr[0]).getUserManager()) == null) {
            return null;
        }
        ChatterNameRetriever chatterNameRetriever = new ChatterNameRetriever(chatterID, this.onChatterNameUpdated, UIThreadExecutor.getInstance());
        try {
            try {
                this.nameReadyLock.lock();
                while (!this.isNameReady.get() && (!isCancelled())) {
                    this.nameReadyCondition.await();
                }
                this.nameReadyLock.unlock();
                chatterNameRetriever.dispose();
                if (isCancelled()) {
                    return null;
                }
                File file2 = new File(Environment.getExternalStoragePublicDirectory(Build.VERSION.SDK_INT >= 19 ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS), "Lumiya");
                File file3 = new File(file2, "Chat Logs");
                File file4 = new File(file3, sanitizeName(this.gotChatterName.get()) + ".txt");
                DateFormat dateTimeInstance = DateFormat.getDateTimeInstance(3, 3);
                try {
                    file2.mkdirs();
                    file3.mkdirs();
                } catch (IOException e) {
                    Debug.Warning(e);
                    file = null;
                } catch (SecurityException e2) {
                    Debug.Warning(e2);
                    file = null;
                } catch (Exception e3) {
                    Debug.Warning(e3);
                    file = null;
                }
                try {
                    fileOutputStream = new FileOutputStream(file4, false);
                    try {
                        messages = userManager.getChatterList().getActiveChattersManager().getMessages(chatterID);
                        if (messages != null) {
                            try {
                                Iterator<ChatMessage> it = messages.iterator();
                                while (it.hasNext()) {
                                    SLChatEvent sLChatEventLoadFromDatabaseObject = SLChatEvent.loadFromDatabaseObject(it.next(), userManager.getUserID());
                                    if (sLChatEventLoadFromDatabaseObject != null) {
                                        fileOutputStream.write(("[" + dateTimeInstance.format(sLChatEventLoadFromDatabaseObject.getTimestamp()) + "] " + sLChatEventLoadFromDatabaseObject.getPlainTextMessage(this.context, userManager, false).toString() + "\n").getBytes());
                                    }
                                    if (isCancelled()) {
                                        break;
                                    }
                                }
                            } catch (Throwable th) {
                                th = th;
                                if (messages != null) {
                                    messages.close();
                                }
                                if (fileOutputStream != null) {
                                    fileOutputStream.close();
                                }
                                throw th;
                            }
                        }
                        if (messages != null) {
                            messages.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        file = file4;
                        if (!isCancelled()) {
                            if (file != null) {
                                return new ExportResult(file, null, null);
                            }
                            String str = sanitizeName(this.gotChatterName.get()) + ".txt";
                            StringBuilder sb = new StringBuilder();
                            LazyList<ChatMessage> messages2 = userManager.getChatterList().getActiveChattersManager().getMessages(chatterID);
                            if (messages2 != null) {
                                Iterator<ChatMessage> it2 = messages2.iterator();
                                while (it2.hasNext()) {
                                    SLChatEvent sLChatEventLoadFromDatabaseObject2 = SLChatEvent.loadFromDatabaseObject(it2.next(), userManager.getUserID());
                                    if (sLChatEventLoadFromDatabaseObject2 != null) {
                                        sb.append("[").append(dateTimeInstance.format(sLChatEventLoadFromDatabaseObject2.getTimestamp())).append("] ").append(sLChatEventLoadFromDatabaseObject2.getPlainTextMessage(this.context, userManager, false).toString()).append("\n");
                                    }
                                    if (isCancelled()) {
                                        break;
                                    }
                                }
                            }
                            String string = sb.toString();
                            if (!isCancelled()) {
                                return new ExportResult(null, string, str);
                            }
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        messages = null;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = null;
                    messages = null;
                }
            } catch (Throwable th4) {
                this.nameReadyLock.unlock();
                throw th4;
            }
        } catch (InterruptedException e4) {
            return null;
        }
    }

    /* JADX INFO: renamed from: lambda$-com_lumiyaviewer_lumiya_ui_chat_ExportChatHistoryTask_2020, reason: not valid java name */
    /* synthetic */ void m431x863366ea(ChatterNameRetriever chatterNameRetriever) {
        try {
            this.nameReadyLock.lock();
            this.isNameReady.set(true);
            this.gotChatterName.set(chatterNameRetriever.getResolvedName());
            this.nameReadyCondition.signal();
        } finally {
            this.nameReadyLock.unlock();
        }
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialogInterface) {
        cancel(false);
        try {
            this.nameReadyLock.lock();
            this.nameReadyCondition.signal();
        } finally {
            this.nameReadyLock.unlock();
        }
    }

    @Override // android.os.AsyncTask
    protected void onCancelled() {
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(ExportResult exportResult) {
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
        if (exportResult != null) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.SEND");
            intent.setType("text/plain");
            if (exportResult.outputFile != null) {
                Debug.Printf("Export: exported as stream %s", exportResult.outputFile);
                intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(exportResult.outputFile));
            } else {
                Debug.Printf("Export: exported as text, %d bytes", Integer.valueOf(exportResult.rawText.length()));
                intent.putExtra("android.intent.extra.TEXT", exportResult.rawText);
                intent.putExtra("android.intent.extra.SUBJECT", exportResult.rawTextTitle);
            }
            this.context.startActivity(Intent.createChooser(intent, this.context.getText(R.string.export_chat_history_to)));
        }
    }

    @Override // android.os.AsyncTask
    protected void onPreExecute() {
        this.progressDialog = ProgressDialog.show(this.context, this.context.getString(R.string.please_wait_title), this.context.getString(R.string.exporting_chat_history), true, true, this);
    }
}
