package com.lumiyaviewer.lumiya.slproto.https;

import android.support.v4.os.EnvironmentCompat;
import com.google.common.net.HttpHeaders;
import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDStreamingParser;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDXMLException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/* JADX INFO: loaded from: classes.dex */
public class LLSDStreamingXMLRequest {
    private static final MediaType MEDIA_TYPE_LLSD_XML = MediaType.parse("application/llsd+xml");
    private final AtomicReference<Call> callRef = new AtomicReference<>(null);

    public void InterruptRequest() {
        Call call = this.callRef.get();
        if (call != null) {
            try {
                call.cancel();
            } catch (Exception e) {
                Debug.Warning(e);
            }
        }
    }

    public void PerformRequest(String str, LLSDNode lLSDNode, LLSDStreamingParser.LLSDContentHandler lLSDContentHandler) throws IOException, LLSDXMLException {
        Request.Builder builderHeader = new Request.Builder().url(str).header(HttpHeaders.ACCEPT, "application/llsd+binary;q=0.5,application/llsd+xml;q=0.1");
        if (lLSDNode != null) {
            builderHeader.post(RequestBody.create(MEDIA_TYPE_LLSD_XML, lLSDNode.serializeToXML()));
        }
        Call callNewCall = SLHTTPSConnection.getOkHttpClient().newCall(builderHeader.build());
        this.callRef.set(callNewCall);
        try {
            Response responseExecute = callNewCall.execute();
            if (responseExecute == null) {
                throw new IOException("Null response");
            }
            try {
                if (!responseExecute.isSuccessful()) {
                    throw new IOException("Error response: " + responseExecute.code());
                }
                LLSDStreamingParser.parseAny(responseExecute.body().byteStream(), responseExecute.header(HttpHeaders.CONTENT_TYPE, EnvironmentCompat.MEDIA_UNKNOWN), lLSDContentHandler);
            } finally {
                responseExecute.close();
                this.callRef.set(null);
            }
        } catch (LLSDXMLException e) {
            this.callRef.set(null);
            throw e;
        } catch (IOException e2) {
            this.callRef.set(null);
            throw e2;
        } catch (Exception e3) {
            this.callRef.set(null);
            throw new IOException(e3.getMessage(), e3);
        }
    }
}
