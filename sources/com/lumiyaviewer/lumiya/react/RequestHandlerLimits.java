package com.lumiyaviewer.lumiya.react;

/* JADX INFO: loaded from: classes.dex */
public interface RequestHandlerLimits {
    int getMaxRequestsInFlight();

    long getRequestTimeout();

    boolean isRequestCancellable();
}
