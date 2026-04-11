package okhttp3.internal.http;

import java.io.IOException;
import java.util.List;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.Version;
import okio.GzipSource;
import okio.Okio;

/* JADX INFO: loaded from: classes.dex */
public final class BridgeInterceptor implements Interceptor {
    private final CookieJar cookieJar;

    public BridgeInterceptor(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    private String cookieHeader(List<Cookie> list) {
        StringBuilder sb = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append("; ");
            }
            Cookie cookie = list.get(i);
            sb.append(cookie.name()).append('=').append(cookie.value());
        }
        return sb.toString();
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        boolean z = false;
        Request request = chain.request();
        Request.Builder builderNewBuilder = request.newBuilder();
        RequestBody requestBodyBody = request.body();
        if (requestBodyBody != null) {
            MediaType mediaTypeContentType = requestBodyBody.contentType();
            if (mediaTypeContentType != null) {
                builderNewBuilder.header(com.google.common.net.HttpHeaders.CONTENT_TYPE, mediaTypeContentType.toString());
            }
            long jContentLength = requestBodyBody.contentLength();
            if (jContentLength != -1) {
                builderNewBuilder.header(com.google.common.net.HttpHeaders.CONTENT_LENGTH, Long.toString(jContentLength));
                builderNewBuilder.removeHeader(com.google.common.net.HttpHeaders.TRANSFER_ENCODING);
            } else {
                builderNewBuilder.header(com.google.common.net.HttpHeaders.TRANSFER_ENCODING, "chunked");
                builderNewBuilder.removeHeader(com.google.common.net.HttpHeaders.CONTENT_LENGTH);
            }
        }
        if (request.header(com.google.common.net.HttpHeaders.HOST) == null) {
            builderNewBuilder.header(com.google.common.net.HttpHeaders.HOST, Util.hostHeader(request.url(), false));
        }
        if (request.header(com.google.common.net.HttpHeaders.CONNECTION) == null) {
            builderNewBuilder.header(com.google.common.net.HttpHeaders.CONNECTION, "Keep-Alive");
        }
        if (request.header(com.google.common.net.HttpHeaders.ACCEPT_ENCODING) == null) {
            z = true;
            builderNewBuilder.header(com.google.common.net.HttpHeaders.ACCEPT_ENCODING, "gzip");
        }
        List<Cookie> listLoadForRequest = this.cookieJar.loadForRequest(request.url());
        if (!listLoadForRequest.isEmpty()) {
            builderNewBuilder.header(com.google.common.net.HttpHeaders.COOKIE, cookieHeader(listLoadForRequest));
        }
        if (request.header(com.google.common.net.HttpHeaders.USER_AGENT) == null) {
            builderNewBuilder.header(com.google.common.net.HttpHeaders.USER_AGENT, Version.userAgent());
        }
        Response responseProceed = chain.proceed(builderNewBuilder.build());
        HttpHeaders.receiveHeaders(this.cookieJar, request.url(), responseProceed.headers());
        Response.Builder builderRequest = responseProceed.newBuilder().request(request);
        if (z && "gzip".equalsIgnoreCase(responseProceed.header(com.google.common.net.HttpHeaders.CONTENT_ENCODING)) && HttpHeaders.hasBody(responseProceed)) {
            GzipSource gzipSource = new GzipSource(responseProceed.body().source());
            Headers headersBuild = responseProceed.headers().newBuilder().removeAll(com.google.common.net.HttpHeaders.CONTENT_ENCODING).removeAll(com.google.common.net.HttpHeaders.CONTENT_LENGTH).build();
            builderRequest.headers(headersBuild);
            builderRequest.body(new RealResponseBody(headersBuild, Okio.buffer(gzipSource)));
        }
        return builderRequest.build();
    }
}
