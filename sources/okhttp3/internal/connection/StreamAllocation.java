package okhttp3.internal.connection;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import okhttp3.Address;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Route;
import okhttp3.internal.Internal;
import okhttp3.internal.http.HttpCodec;
import okhttp3.internal.http1.Http1Codec;
import okhttp3.internal.http2.ConnectionShutdownException;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.Http2Codec;
import okhttp3.internal.http2.StreamResetException;

/* JADX INFO: loaded from: classes.dex */
public final class StreamAllocation {
    static final /* synthetic */ boolean $assertionsDisabled;
    public final Address address;
    private final Object callStackTrace;
    private boolean canceled;
    private HttpCodec codec;
    private RealConnection connection;
    private final ConnectionPool connectionPool;
    private int refusedStreamCount;
    private boolean released;
    private Route route;
    private final RouteSelector routeSelector;

    public static final class StreamAllocationReference extends WeakReference<StreamAllocation> {
        public final Object callStackTrace;

        StreamAllocationReference(StreamAllocation streamAllocation, Object obj) {
            super(streamAllocation);
            this.callStackTrace = obj;
        }
    }

    static {
        $assertionsDisabled = StreamAllocation.class.desiredAssertionStatus() ? false : true;
    }

    public StreamAllocation(ConnectionPool connectionPool, Address address, Object obj) {
        this.connectionPool = connectionPool;
        this.address = address;
        this.routeSelector = new RouteSelector(address, routeDatabase());
        this.callStackTrace = obj;
    }

    /* JADX WARN: Removed duplicated region for block: B:15:0x0017  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x001b  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void deallocate(boolean r7, boolean r8, boolean r9) {
        /*
            r6 = this;
            r0 = 0
            okhttp3.ConnectionPool r1 = r6.connectionPool
            monitor-enter(r1)
            if (r9 != 0) goto L10
        L6:
            if (r8 != 0) goto L17
        L8:
            okhttp3.internal.connection.RealConnection r2 = r6.connection     // Catch: java.lang.Throwable -> L14
            if (r2 != 0) goto L1b
        Lc:
            monitor-exit(r1)     // Catch: java.lang.Throwable -> L14
            if (r0 != 0) goto L5c
        Lf:
            return
        L10:
            r2 = 0
            r6.codec = r2     // Catch: java.lang.Throwable -> L14
            goto L6
        L14:
            r0 = move-exception
            monitor-exit(r1)     // Catch: java.lang.Throwable -> L14
            throw r0
        L17:
            r2 = 1
            r6.released = r2     // Catch: java.lang.Throwable -> L14
            goto L8
        L1b:
            if (r7 != 0) goto L38
        L1d:
            okhttp3.internal.http.HttpCodec r2 = r6.codec     // Catch: java.lang.Throwable -> L14
            if (r2 != 0) goto Lc
            boolean r2 = r6.released     // Catch: java.lang.Throwable -> L14
            if (r2 == 0) goto L3e
        L25:
            okhttp3.internal.connection.RealConnection r2 = r6.connection     // Catch: java.lang.Throwable -> L14
            r6.release(r2)     // Catch: java.lang.Throwable -> L14
            okhttp3.internal.connection.RealConnection r2 = r6.connection     // Catch: java.lang.Throwable -> L14
            java.util.List<java.lang.ref.Reference<okhttp3.internal.connection.StreamAllocation>> r2 = r2.allocations     // Catch: java.lang.Throwable -> L14
            boolean r2 = r2.isEmpty()     // Catch: java.lang.Throwable -> L14
            if (r2 != 0) goto L45
        L34:
            r2 = 0
            r6.connection = r2     // Catch: java.lang.Throwable -> L14
            goto Lc
        L38:
            okhttp3.internal.connection.RealConnection r2 = r6.connection     // Catch: java.lang.Throwable -> L14
            r3 = 1
            r2.noNewStreams = r3     // Catch: java.lang.Throwable -> L14
            goto L1d
        L3e:
            okhttp3.internal.connection.RealConnection r2 = r6.connection     // Catch: java.lang.Throwable -> L14
            boolean r2 = r2.noNewStreams     // Catch: java.lang.Throwable -> L14
            if (r2 != 0) goto L25
            goto Lc
        L45:
            okhttp3.internal.connection.RealConnection r2 = r6.connection     // Catch: java.lang.Throwable -> L14
            long r4 = java.lang.System.nanoTime()     // Catch: java.lang.Throwable -> L14
            r2.idleAtNanos = r4     // Catch: java.lang.Throwable -> L14
            okhttp3.internal.Internal r2 = okhttp3.internal.Internal.instance     // Catch: java.lang.Throwable -> L14
            okhttp3.ConnectionPool r3 = r6.connectionPool     // Catch: java.lang.Throwable -> L14
            okhttp3.internal.connection.RealConnection r4 = r6.connection     // Catch: java.lang.Throwable -> L14
            boolean r2 = r2.connectionBecameIdle(r3, r4)     // Catch: java.lang.Throwable -> L14
            if (r2 == 0) goto L34
            okhttp3.internal.connection.RealConnection r0 = r6.connection     // Catch: java.lang.Throwable -> L14
            goto L34
        L5c:
            java.net.Socket r0 = r0.socket()
            okhttp3.internal.Util.closeQuietly(r0)
            goto Lf
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.StreamAllocation.deallocate(boolean, boolean, boolean):void");
    }

    private RealConnection findConnection(int i, int i2, int i3, boolean z) throws Throwable {
        Route route;
        synchronized (this.connectionPool) {
            if (this.released) {
                throw new IllegalStateException("released");
            }
            if (this.codec != null) {
                throw new IllegalStateException("codec != null");
            }
            if (this.canceled) {
                throw new IOException("Canceled");
            }
            RealConnection realConnection = this.connection;
            if (realConnection != null && !realConnection.noNewStreams) {
                return realConnection;
            }
            RealConnection realConnection2 = Internal.instance.get(this.connectionPool, this.address, this);
            if (realConnection2 != null) {
                this.connection = realConnection2;
                return realConnection2;
            }
            Route route2 = this.route;
            if (route2 != null) {
                route = route2;
            } else {
                Route next = this.routeSelector.next();
                synchronized (this.connectionPool) {
                    this.route = next;
                    this.refusedStreamCount = 0;
                }
                route = next;
            }
            RealConnection realConnection3 = new RealConnection(route);
            synchronized (this.connectionPool) {
                acquire(realConnection3);
                Internal.instance.put(this.connectionPool, realConnection3);
                this.connection = realConnection3;
                if (this.canceled) {
                    throw new IOException("Canceled");
                }
            }
            realConnection3.connect(i, i2, i3, this.address.connectionSpecs(), z);
            routeDatabase().connected(realConnection3.route());
            return realConnection3;
        }
    }

    private RealConnection findHealthyConnection(int i, int i2, int i3, boolean z, boolean z2) throws Throwable {
        while (true) {
            RealConnection realConnectionFindConnection = findConnection(i, i2, i3, z);
            synchronized (this.connectionPool) {
                if (realConnectionFindConnection.successCount == 0) {
                    return realConnectionFindConnection;
                }
                if (realConnectionFindConnection.isHealthy(z2)) {
                    return realConnectionFindConnection;
                }
                noNewStreams();
            }
        }
    }

    private void release(RealConnection realConnection) {
        int size = realConnection.allocations.size();
        for (int i = 0; i < size; i++) {
            if (realConnection.allocations.get(i).get() == this) {
                realConnection.allocations.remove(i);
                return;
            }
        }
        throw new IllegalStateException();
    }

    private RouteDatabase routeDatabase() {
        return Internal.instance.routeDatabase(this.connectionPool);
    }

    public void acquire(RealConnection realConnection) {
        if (!$assertionsDisabled && !Thread.holdsLock(this.connectionPool)) {
            throw new AssertionError();
        }
        realConnection.allocations.add(new StreamAllocationReference(this, this.callStackTrace));
    }

    public void cancel() {
        HttpCodec httpCodec;
        RealConnection realConnection;
        synchronized (this.connectionPool) {
            this.canceled = true;
            httpCodec = this.codec;
            realConnection = this.connection;
        }
        if (httpCodec != null) {
            httpCodec.cancel();
        } else {
            if (realConnection == null) {
                return;
            }
            realConnection.cancel();
        }
    }

    public HttpCodec codec() {
        HttpCodec httpCodec;
        synchronized (this.connectionPool) {
            httpCodec = this.codec;
        }
        return httpCodec;
    }

    public synchronized RealConnection connection() {
        return this.connection;
    }

    public boolean hasMoreRoutes() {
        return this.route != null || this.routeSelector.hasNext();
    }

    public HttpCodec newStream(OkHttpClient okHttpClient, boolean z) throws Throwable {
        HttpCodec http2Codec;
        int iConnectTimeoutMillis = okHttpClient.connectTimeoutMillis();
        int timeoutMillis = okHttpClient.readTimeoutMillis();
        int iWriteTimeoutMillis = okHttpClient.writeTimeoutMillis();
        try {
            RealConnection realConnectionFindHealthyConnection = findHealthyConnection(iConnectTimeoutMillis, timeoutMillis, iWriteTimeoutMillis, okHttpClient.retryOnConnectionFailure(), z);
            if (realConnectionFindHealthyConnection.http2Connection == null) {
                realConnectionFindHealthyConnection.socket().setSoTimeout(timeoutMillis);
                realConnectionFindHealthyConnection.source.timeout().timeout(timeoutMillis, TimeUnit.MILLISECONDS);
                realConnectionFindHealthyConnection.sink.timeout().timeout(iWriteTimeoutMillis, TimeUnit.MILLISECONDS);
                http2Codec = new Http1Codec(okHttpClient, this, realConnectionFindHealthyConnection.source, realConnectionFindHealthyConnection.sink);
            } else {
                http2Codec = new Http2Codec(okHttpClient, this, realConnectionFindHealthyConnection.http2Connection);
            }
            synchronized (this.connectionPool) {
                this.codec = http2Codec;
            }
            return http2Codec;
        } catch (IOException e) {
            throw new RouteException(e);
        }
    }

    public void noNewStreams() {
        deallocate(true, false, false);
    }

    public void release() {
        deallocate(false, true, false);
    }

    public void streamFailed(IOException iOException) {
        boolean z;
        synchronized (this.connectionPool) {
            if (iOException instanceof StreamResetException) {
                StreamResetException streamResetException = (StreamResetException) iOException;
                if (streamResetException.errorCode == ErrorCode.REFUSED_STREAM) {
                    this.refusedStreamCount++;
                }
                if (streamResetException.errorCode == ErrorCode.REFUSED_STREAM && this.refusedStreamCount <= 1) {
                    z = false;
                } else {
                    this.route = null;
                    z = true;
                }
            } else if ((this.connection == null || this.connection.isMultiplexed()) && !(iOException instanceof ConnectionShutdownException)) {
                z = false;
            } else if (this.connection.successCount != 0) {
                z = true;
            } else {
                if (this.route != null && iOException != null) {
                    this.routeSelector.connectFailed(this.route, iOException);
                }
                this.route = null;
                z = true;
            }
        }
        deallocate(z, false, true);
    }

    public void streamFinished(boolean z, HttpCodec httpCodec) {
        synchronized (this.connectionPool) {
            if (httpCodec != null) {
                if (httpCodec == this.codec) {
                    if (!z) {
                        this.connection.successCount++;
                    }
                }
            }
            throw new IllegalStateException("expected " + this.codec + " but was " + httpCodec);
        }
        deallocate(z, false, true);
    }

    public String toString() {
        return this.address.toString();
    }
}
