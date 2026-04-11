package com.lumiyaviewer.lumiya.ui.inventory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import com.lumiyaviewer.lumiya.R;
import com.lumiyaviewer.lumiya.slproto.SLAgentCircuit;
import com.lumiyaviewer.lumiya.slproto.users.manager.UserManager;
import java.util.UUID;
import javax.annotation.Nullable;
import okhttp3.MediaType;

/* JADX INFO: loaded from: classes.dex */
public class UploadImageAsyncTask extends AsyncTask<UploadImageParams, Void, UploadImageResult> {
    private static final MediaType MEDIA_TYPE_JP2 = MediaType.parse("image/jp2");
    private final UUID agentUUID;
    private final Context context;
    private ProgressDialog progressDialog = null;

    protected static class UploadImageResult {

        @Nullable
        public final String errorMessage;
        public final boolean success;

        private UploadImageResult(boolean z, @Nullable String str) {
            this.success = z;
            this.errorMessage = str;
        }

        /* synthetic */ UploadImageResult(boolean z, String str, UploadImageResult uploadImageResult) {
            this(z, str);
        }
    }

    public UploadImageAsyncTask(Context context, UUID uuid) {
        this.context = context;
        this.agentUUID = uuid;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Removed duplicated region for block: B:83:0x0253  */
    @Override // android.os.AsyncTask
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public com.lumiyaviewer.lumiya.ui.inventory.UploadImageAsyncTask.UploadImageResult doInBackground(com.lumiyaviewer.lumiya.ui.inventory.UploadImageParams... r19) throws java.lang.Throwable {
        /*
            Method dump skipped, instruction units count: 598
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lumiyaviewer.lumiya.ui.inventory.UploadImageAsyncTask.doInBackground(com.lumiyaviewer.lumiya.ui.inventory.UploadImageParams[]):com.lumiyaviewer.lumiya.ui.inventory.UploadImageAsyncTask$UploadImageResult");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(UploadImageResult uploadImageResult) {
        UUID uuidFindSpecialFolder;
        super.onPostExecute(uploadImageResult);
        if (this.progressDialog != null) {
            this.progressDialog.cancel();
            this.progressDialog = null;
        }
        if (!(uploadImageResult != null ? uploadImageResult.success : false)) {
            String string = uploadImageResult != null ? uploadImageResult.errorMessage : null;
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            if (string == null) {
                string = this.context.getString(R.string.failed_to_upload_picture);
            }
            builder.setMessage(string).setCancelable(true).setNegativeButton("Dismiss", new DialogInterface.OnClickListener() { // from class: com.lumiyaviewer.lumiya.ui.inventory.-$Lambda$zdXuDSYysFkzF70MB3S4I5y4LlM
                private final /* synthetic */ void $m$0(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }

                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    $m$0(dialogInterface, i);
                }
            }).create().show();
            return;
        }
        UserManager userManager = UserManager.getUserManager(this.agentUUID);
        SLAgentCircuit activeAgentCircuit = userManager != null ? userManager.getActiveAgentCircuit() : null;
        if (activeAgentCircuit == null || (uuidFindSpecialFolder = activeAgentCircuit.getModules().inventory.findSpecialFolder(0)) == null) {
            return;
        }
        userManager.getInventoryManager().requestFolderUpdate(uuidFindSpecialFolder);
    }

    @Override // android.os.AsyncTask
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog = new ProgressDialog(this.context);
        this.progressDialog.setMessage(this.context.getString(R.string.uploading_picture));
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.show();
    }
}
