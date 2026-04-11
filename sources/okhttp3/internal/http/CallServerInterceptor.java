package okhttp3.internal.http;

import java.io.IOException;
import java.net.ProtocolException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.connection.StreamAllocation;
import okio.BufferedSink;
import okio.Okio;

/* JADX INFO: loaded from: classes.dex */
public final class CallServerInterceptor implements Interceptor {
    private final boolean forWebSocket;

    public CallServerInterceptor(boolean z) {
        this.forWebSocket = z;
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        HttpCodec httpCodecHttpStream = ((RealInterceptorChain) chain).httpStream();
        StreamAllocation streamAllocation = ((RealInterceptorChain) chain).streamAllocation();
        Request request = chain.request();
        long jCurrentTimeMillis = System.currentTimeMillis();
        httpCodecHttpStream.writeRequestHeaders(request);
        if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {
            BufferedSink bufferedSinkBuffer = Okio.buffer(httpCodecHttpStream.createRequestBody(request, request.body().contentLength()));
            request.body().writeTo(bufferedSinkBuffer);
            bufferedSinkBuffer.close();
        }
        httpCodecHttpStream.finishRequest();
        Response responseBuild = httpCodecHttpStream.readResponseHeaders().request(request).handshake(streamAllocation.connection().handshake()).sentRequestAtMillis(jCurrentTimeMillis).receivedResponseAtMillis(System.currentTimeMillis()).build();
        int iCode = responseBuild.code();
        Response responseBuild2 = (this.forWebSocket && iCode == 101) ? responseBuild.newBuilder().body(Util.EMPTY_RESPONSE).build() : responseBuild.newBuilder().body(httpCodecHttpStream.openResponseBody(responseBuild)).build();
        if ("close".equalsIgnoreCase(responseBuild2.request().header(com.google.common.net.HttpHeaders.CONNECTION)) || "close".equalsIgnoreCase(responseBuild2.header(com.google.common.net.HttpHeaders.CONNECTION))) {
            streamAllocation.noNewStreams();
        }
        if (iCode == 204 || iCode == 205) {
            if (!(responseBuild2.body().contentLength() <= 0)) {
                throw new ProtocolException("HTTP " + iCode + " had non-zero Content-Length: " + responseBuild2.body().contentLength());
            }
        }
        return responseBuild2;
    }
}
