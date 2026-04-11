package okhttp3.internal.ws;

import com.google.common.net.HttpHeaders;
import java.io.Closeable;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.internal.Internal;
import okhttp3.internal.Util;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.ws.WebSocketReader;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

/* JADX INFO: loaded from: classes.dex */
public final class RealWebSocket implements WebSocket, WebSocketReader.FrameCallback {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final long CANCEL_AFTER_CLOSE_MILLIS = 60000;
    private static final long MAX_QUEUE_SIZE = 16777216;
    private static final List<Protocol> ONLY_HTTP1;
    private Call call;
    private ScheduledFuture<?> cancelFuture;
    private boolean enqueuedClose;
    private ScheduledExecutorService executor;
    private boolean failed;
    private final String key;
    final WebSocketListener listener;
    private final Request originalRequest;
    int pingCount;
    int pongCount;
    private long queueSize;
    private final Random random;
    private WebSocketReader reader;
    private String receivedCloseReason;
    private Streams streams;
    private WebSocketWriter writer;
    private final Runnable writerRunnable;
    private final ArrayDeque<ByteString> pongQueue = new ArrayDeque<>();
    private final ArrayDeque<Object> messageAndCloseQueue = new ArrayDeque<>();
    private int receivedCloseCode = -1;

    final class CancelRunnable implements Runnable {
        CancelRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            RealWebSocket.this.cancel();
        }
    }

    static final class ClientStreams extends Streams {
        private final StreamAllocation streamAllocation;

        ClientStreams(StreamAllocation streamAllocation) {
            super(true, streamAllocation.connection().source, streamAllocation.connection().sink);
            this.streamAllocation = streamAllocation;
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            this.streamAllocation.streamFinished(true, this.streamAllocation.codec());
        }
    }

    static final class Close {
        final long cancelAfterCloseMillis;
        final int code;
        final ByteString reason;

        Close(int i, ByteString byteString, long j) {
            this.code = i;
            this.reason = byteString;
            this.cancelAfterCloseMillis = j;
        }
    }

    static final class Message {
        final ByteString data;
        final int formatOpcode;

        Message(int i, ByteString byteString) {
            this.formatOpcode = i;
            this.data = byteString;
        }
    }

    private final class PingRunnable implements Runnable {
        private PingRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            RealWebSocket.this.writePingFrame();
        }
    }

    public static abstract class Streams implements Closeable {
        public final boolean client;
        public final BufferedSink sink;
        public final BufferedSource source;

        public Streams(boolean z, BufferedSource bufferedSource, BufferedSink bufferedSink) {
            this.client = z;
            this.source = bufferedSource;
            this.sink = bufferedSink;
        }
    }

    static {
        $assertionsDisabled = RealWebSocket.class.desiredAssertionStatus() ? false : true;
        ONLY_HTTP1 = Collections.singletonList(Protocol.HTTP_1_1);
    }

    public RealWebSocket(Request request, WebSocketListener webSocketListener, Random random) {
        if (!"GET".equals(request.method())) {
            throw new IllegalArgumentException("Request must be GET: " + request.method());
        }
        this.originalRequest = request;
        this.listener = webSocketListener;
        this.random = random;
        byte[] bArr = new byte[16];
        random.nextBytes(bArr);
        this.key = ByteString.of(bArr).base64();
        this.writerRunnable = new Runnable() { // from class: okhttp3.internal.ws.RealWebSocket.1
            @Override // java.lang.Runnable
            public void run() {
                do {
                    try {
                    } catch (IOException e) {
                        RealWebSocket.this.failWebSocket(e, null);
                        return;
                    }
                } while (RealWebSocket.this.writeOneFrame());
            }
        };
    }

    private void runWriter() {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        }
        if (this.executor == null) {
            return;
        }
        this.executor.execute(this.writerRunnable);
    }

    private synchronized boolean send(ByteString byteString, int i) {
        if (this.failed || this.enqueuedClose) {
            return false;
        }
        if (!(this.queueSize + ((long) byteString.size()) <= 16777216)) {
            close(1001, null);
            return false;
        }
        this.queueSize += (long) byteString.size();
        this.messageAndCloseQueue.add(new Message(i, byteString));
        runWriter();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void writePingFrame() {
        synchronized (this) {
            if (this.failed) {
                return;
            }
            WebSocketWriter webSocketWriter = this.writer;
            try {
                webSocketWriter.writePing(ByteString.EMPTY);
            } catch (IOException e) {
                failWebSocket(e, null);
            }
        }
    }

    @Override // okhttp3.WebSocket
    public void cancel() {
        this.call.cancel();
    }

    void checkResponse(Response response) throws ProtocolException {
        if (response.code() != 101) {
            throw new ProtocolException("Expected HTTP 101 response but was '" + response.code() + " " + response.message() + "'");
        }
        String strHeader = response.header(HttpHeaders.CONNECTION);
        if (!HttpHeaders.UPGRADE.equalsIgnoreCase(strHeader)) {
            throw new ProtocolException("Expected 'Connection' header value 'Upgrade' but was '" + strHeader + "'");
        }
        String strHeader2 = response.header(HttpHeaders.UPGRADE);
        if (!"websocket".equalsIgnoreCase(strHeader2)) {
            throw new ProtocolException("Expected 'Upgrade' header value 'websocket' but was '" + strHeader2 + "'");
        }
        String strHeader3 = response.header("Sec-WebSocket-Accept");
        String strBase64 = ByteString.encodeUtf8(this.key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").sha1().base64();
        if (!strBase64.equals(strHeader3)) {
            throw new ProtocolException("Expected 'Sec-WebSocket-Accept' header value '" + strBase64 + "' but was '" + strHeader3 + "'");
        }
    }

    @Override // okhttp3.WebSocket
    public boolean close(int i, String str) {
        return close(i, str, 60000L);
    }

    synchronized boolean close(int i, String str, long j) {
        ByteString byteStringEncodeUtf8 = null;
        synchronized (this) {
            WebSocketProtocol.validateCloseCode(i);
            if (str != null) {
                byteStringEncodeUtf8 = ByteString.encodeUtf8(str);
                if (!(((long) byteStringEncodeUtf8.size()) <= 123)) {
                    throw new IllegalArgumentException("reason.size() > 123: " + str);
                }
            }
            if (this.failed || this.enqueuedClose) {
                return false;
            }
            this.enqueuedClose = true;
            this.messageAndCloseQueue.add(new Close(i, byteStringEncodeUtf8, j));
            runWriter();
            return true;
        }
    }

    public void connect(OkHttpClient okHttpClient) {
        OkHttpClient okHttpClientBuild = okHttpClient.newBuilder().protocols(ONLY_HTTP1).build();
        final int iPingIntervalMillis = okHttpClientBuild.pingIntervalMillis();
        final Request requestBuild = this.originalRequest.newBuilder().header(HttpHeaders.UPGRADE, "websocket").header(HttpHeaders.CONNECTION, HttpHeaders.UPGRADE).header("Sec-WebSocket-Key", this.key).header("Sec-WebSocket-Version", "13").build();
        this.call = Internal.instance.newWebSocketCall(okHttpClientBuild, requestBuild);
        this.call.enqueue(new Callback() { // from class: okhttp3.internal.ws.RealWebSocket.2
            @Override // okhttp3.Callback
            public void onFailure(Call call, IOException iOException) {
                RealWebSocket.this.failWebSocket(iOException, null);
            }

            @Override // okhttp3.Callback
            public void onResponse(Call call, Response response) {
                try {
                    RealWebSocket.this.checkResponse(response);
                    StreamAllocation streamAllocation = Internal.instance.streamAllocation(call);
                    streamAllocation.noNewStreams();
                    ClientStreams clientStreams = new ClientStreams(streamAllocation);
                    try {
                        RealWebSocket.this.listener.onOpen(RealWebSocket.this, response);
                        RealWebSocket.this.initReaderAndWriter("OkHttp WebSocket " + requestBuild.url().redact(), iPingIntervalMillis, clientStreams);
                        streamAllocation.connection().socket().setSoTimeout(0);
                        RealWebSocket.this.loopReader();
                    } catch (Exception e) {
                        RealWebSocket.this.failWebSocket(e, null);
                    }
                } catch (ProtocolException e2) {
                    RealWebSocket.this.failWebSocket(e2, response);
                    Util.closeQuietly(response);
                }
            }
        });
    }

    void failWebSocket(Exception exc, Response response) {
        synchronized (this) {
            if (this.failed) {
                return;
            }
            this.failed = true;
            Streams streams = this.streams;
            this.streams = null;
            if (this.cancelFuture != null) {
                this.cancelFuture.cancel(false);
            }
            if (this.executor != null) {
                this.executor.shutdown();
            }
            try {
                this.listener.onFailure(this, exc, response);
            } finally {
                Util.closeQuietly(streams);
            }
        }
    }

    public void initReaderAndWriter(String str, long j, Streams streams) throws IOException {
        synchronized (this) {
            this.streams = streams;
            this.writer = new WebSocketWriter(streams.client, streams.sink, this.random);
            this.executor = new ScheduledThreadPoolExecutor(1, Util.threadFactory(str, false));
            if (j != 0) {
                this.executor.scheduleAtFixedRate(new PingRunnable(), j, j, TimeUnit.MILLISECONDS);
            }
            if (!this.messageAndCloseQueue.isEmpty()) {
                runWriter();
            }
        }
        this.reader = new WebSocketReader(streams.client, streams.source, this);
    }

    public void loopReader() throws IOException {
        while (this.receivedCloseCode == -1) {
            this.reader.processNextFrame();
        }
    }

    @Override // okhttp3.internal.ws.WebSocketReader.FrameCallback
    public void onReadClose(int i, String str) {
        Streams streams;
        if (i == -1) {
            throw new IllegalArgumentException();
        }
        synchronized (this) {
            if (this.receivedCloseCode != -1) {
                throw new IllegalStateException("already closed");
            }
            this.receivedCloseCode = i;
            this.receivedCloseReason = str;
            if (this.enqueuedClose && this.messageAndCloseQueue.isEmpty()) {
                Streams streams2 = this.streams;
                this.streams = null;
                if (this.cancelFuture != null) {
                    this.cancelFuture.cancel(false);
                }
                this.executor.shutdown();
                streams = streams2;
            } else {
                streams = null;
            }
        }
        try {
            this.listener.onClosing(this, i, str);
            if (streams != null) {
                this.listener.onClosed(this, i, str);
            }
        } finally {
            Util.closeQuietly(streams);
        }
    }

    @Override // okhttp3.internal.ws.WebSocketReader.FrameCallback
    public void onReadMessage(String str) throws IOException {
        this.listener.onMessage(this, str);
    }

    @Override // okhttp3.internal.ws.WebSocketReader.FrameCallback
    public void onReadMessage(ByteString byteString) throws IOException {
        this.listener.onMessage(this, byteString);
    }

    @Override // okhttp3.internal.ws.WebSocketReader.FrameCallback
    public synchronized void onReadPing(ByteString byteString) {
        if (this.failed || (this.enqueuedClose && this.messageAndCloseQueue.isEmpty())) {
            return;
        }
        this.pongQueue.add(byteString);
        runWriter();
        this.pingCount++;
    }

    @Override // okhttp3.internal.ws.WebSocketReader.FrameCallback
    public synchronized void onReadPong(ByteString byteString) {
        this.pongCount++;
    }

    synchronized int pingCount() {
        return this.pingCount;
    }

    synchronized boolean pong(ByteString byteString) {
        if (this.failed || (this.enqueuedClose && this.messageAndCloseQueue.isEmpty())) {
            return false;
        }
        this.pongQueue.add(byteString);
        runWriter();
        return true;
    }

    synchronized int pongCount() {
        return this.pongCount;
    }

    boolean processNextFrame() throws IOException {
        try {
            this.reader.processNextFrame();
            return this.receivedCloseCode == -1;
        } catch (Exception e) {
            failWebSocket(e, null);
            return false;
        }
    }

    @Override // okhttp3.WebSocket
    public synchronized long queueSize() {
        return this.queueSize;
    }

    @Override // okhttp3.WebSocket
    public Request request() {
        return this.originalRequest;
    }

    @Override // okhttp3.WebSocket
    public boolean send(String str) {
        if (str != null) {
            return send(ByteString.encodeUtf8(str), 1);
        }
        throw new NullPointerException("text == null");
    }

    @Override // okhttp3.WebSocket
    public boolean send(ByteString byteString) {
        if (byteString != null) {
            return send(byteString, 2);
        }
        throw new NullPointerException("bytes == null");
    }

    boolean writeOneFrame() throws IOException {
        int i;
        Object obj;
        Streams streams;
        String str = null;
        synchronized (this) {
            if (this.failed) {
                return false;
            }
            WebSocketWriter webSocketWriter = this.writer;
            ByteString byteStringPoll = this.pongQueue.poll();
            if (byteStringPoll != null) {
                streams = null;
                i = -1;
                obj = null;
            } else {
                Object objPoll = this.messageAndCloseQueue.poll();
                if (objPoll instanceof Close) {
                    int i2 = this.receivedCloseCode;
                    String str2 = this.receivedCloseReason;
                    if (i2 == -1) {
                        this.cancelFuture = this.executor.schedule(new CancelRunnable(), ((Close) objPoll).cancelAfterCloseMillis, TimeUnit.MILLISECONDS);
                        obj = objPoll;
                        streams = null;
                        str = str2;
                        i = i2;
                    } else {
                        Streams streams2 = this.streams;
                        this.streams = null;
                        this.executor.shutdown();
                        str = str2;
                        i = i2;
                        obj = objPoll;
                        streams = streams2;
                    }
                } else {
                    if (objPoll == null) {
                        return false;
                    }
                    i = -1;
                    obj = objPoll;
                    streams = null;
                }
            }
            try {
                if (byteStringPoll != null) {
                    webSocketWriter.writePong(byteStringPoll);
                } else if (obj instanceof Message) {
                    ByteString byteString = ((Message) obj).data;
                    BufferedSink bufferedSinkBuffer = Okio.buffer(webSocketWriter.newMessageSink(((Message) obj).formatOpcode, byteString.size()));
                    bufferedSinkBuffer.write(byteString);
                    bufferedSinkBuffer.close();
                    synchronized (this) {
                        this.queueSize -= (long) byteString.size();
                    }
                } else {
                    if (!(obj instanceof Close)) {
                        throw new AssertionError();
                    }
                    Close close = (Close) obj;
                    webSocketWriter.writeClose(close.code, close.reason);
                    if (streams != null) {
                        this.listener.onClosed(this, i, str);
                    }
                }
                return true;
            } finally {
                Util.closeQuietly(streams);
            }
        }
    }
}
