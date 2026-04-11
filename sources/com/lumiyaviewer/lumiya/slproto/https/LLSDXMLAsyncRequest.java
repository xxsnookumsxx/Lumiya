package com.lumiyaviewer.lumiya.slproto.https;

import com.lumiyaviewer.lumiya.Debug;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDNode;
import com.lumiyaviewer.lumiya.slproto.llsd.LLSDXMLException;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class LLSDXMLAsyncRequest {

    public interface LLSDXMLResultListener {
        void onLLSDXMLResult(LLSDNode lLSDNode);
    }

    public LLSDXMLAsyncRequest(final String str, final LLSDNode lLSDNode, final LLSDXMLResultListener lLSDXMLResultListener) {
        new Thread(new Runnable() { // from class: com.lumiyaviewer.lumiya.slproto.https.LLSDXMLAsyncRequest.1
            @Override // java.lang.Runnable
            public void run() {
                LLSDNode lLSDNodePerformRequest;
                try {
                    lLSDNodePerformRequest = new LLSDXMLRequest().PerformRequest(str, lLSDNode);
                } catch (LLSDXMLException e) {
                    Debug.Warning(e);
                    lLSDNodePerformRequest = null;
                } catch (IOException e2) {
                    Debug.Warning(e2);
                    lLSDNodePerformRequest = null;
                }
                lLSDXMLResultListener.onLLSDXMLResult(lLSDNodePerformRequest);
            }
        }).start();
    }
}
