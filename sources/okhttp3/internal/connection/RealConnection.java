package okhttp3.internal.connection;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import java.lang.ref.Reference;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import okhttp3.Address;
import okhttp3.CertificatePinner;
import okhttp3.Connection;
import okhttp3.ConnectionSpec;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Route;
import okhttp3.internal.Util;
import okhttp3.internal.Version;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.Http2Connection;
import okhttp3.internal.http2.Http2Stream;
import okhttp3.internal.platform.Platform;
import okhttp3.internal.tls.OkHostnameVerifier;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/* JADX INFO: loaded from: classes.dex */
public final class RealConnection extends Http2Connection.Listener implements Connection {
    public int allocationLimit;
    private Handshake handshake;
    public volatile Http2Connection http2Connection;
    public boolean noNewStreams;
    private Protocol protocol;
    private Socket rawSocket;
    private final Route route;
    public BufferedSink sink;
    public Socket socket;
    public BufferedSource source;
    public int successCount;
    public final List<Reference<StreamAllocation>> allocations = new ArrayList();
    public long idleAtNanos = Long.MAX_VALUE;

    public RealConnection(Route route) {
        this.route = route;
    }

    private void buildConnection(int i, int i2, int i3, ConnectionSpecSelector connectionSpecSelector) throws Throwable {
        connectSocket(i, i2);
        establishProtocol(i2, i3, connectionSpecSelector);
    }

    private void buildTunneledConnection(int i, int i2, int i3, ConnectionSpecSelector connectionSpecSelector) throws Throwable {
        Request requestCreateTunnelRequest = createTunnelRequest();
        HttpUrl httpUrlUrl = requestCreateTunnelRequest.url();
        int i4 = 0;
        while (true) {
            i4++;
            if (i4 > 21) {
                throw new ProtocolException("Too many tunnel connections attempted: 21");
            }
            connectSocket(i, i2);
            requestCreateTunnelRequest = createTunnel(i2, i3, requestCreateTunnelRequest, httpUrlUrl);
            if (requestCreateTunnelRequest == null) {
                establishProtocol(i2, i3, connectionSpecSelector);
                return;
            }
            Util.closeQuietly(this.rawSocket);
            this.rawSocket = null;
            this.sink = null;
            this.source = null;
        }
    }

    private void connectSocket(int i, int i2) throws IOException {
        Proxy proxy = this.route.proxy();
        this.rawSocket = (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP) ? this.route.address().socketFactory().createSocket() : new Socket(proxy);
        this.rawSocket.setSoTimeout(i2);
        try {
            Platform.get().connectSocket(this.rawSocket, this.route.socketAddress(), i);
            this.source = Okio.buffer(Okio.source(this.rawSocket));
            this.sink = Okio.buffer(Okio.sink(this.rawSocket));
        } catch (ConnectException e) {
            ConnectException connectException = new ConnectException("Failed to connect to " + this.route.socketAddress());
            connectException.initCause(e);
            throw connectException;
        }
    }

    private void connectTls(int i, int i2, ConnectionSpecSelector connectionSpecSelector) throws Throwable {
        SSLSocket sSLSocket = null;
        Address address = this.route.address();
        try {
            try {
                SSLSocket sSLSocket2 = (SSLSocket) address.sslSocketFactory().createSocket(this.rawSocket, address.url().host(), address.url().port(), true);
                try {
                    ConnectionSpec connectionSpecConfigureSecureSocket = connectionSpecSelector.configureSecureSocket(sSLSocket2);
                    if (connectionSpecConfigureSecureSocket.supportsTlsExtensions()) {
                        Platform.get().configureTlsExtensions(sSLSocket2, address.url().host(), address.protocols());
                    }
                    sSLSocket2.startHandshake();
                    Handshake handshake = Handshake.get(sSLSocket2.getSession());
                    if (!address.hostnameVerifier().verify(address.url().host(), sSLSocket2.getSession())) {
                        X509Certificate x509Certificate = (X509Certificate) handshake.peerCertificates().get(0);
                        throw new SSLPeerUnverifiedException("Hostname " + address.url().host() + " not verified:\n    certificate: " + CertificatePinner.pin(x509Certificate) + "\n    DN: " + x509Certificate.getSubjectDN().getName() + "\n    subjectAltNames: " + OkHostnameVerifier.allSubjectAltNames(x509Certificate));
                    }
                    address.certificatePinner().check(address.url().host(), handshake.peerCertificates());
                    String selectedProtocol = connectionSpecConfigureSecureSocket.supportsTlsExtensions() ? Platform.get().getSelectedProtocol(sSLSocket2) : null;
                    this.socket = sSLSocket2;
                    this.source = Okio.buffer(Okio.source(this.socket));
                    this.sink = Okio.buffer(Okio.sink(this.socket));
                    this.handshake = handshake;
                    this.protocol = selectedProtocol == null ? Protocol.HTTP_1_1 : Protocol.get(selectedProtocol);
                    if (sSLSocket2 == null) {
                        return;
                    }
                    Platform.get().afterHandshake(sSLSocket2);
                } catch (AssertionError e) {
                    e = e;
                    if (!Util.isAndroidGetsocknameError(e)) {
                        throw e;
                    }
                    throw new IOException(e);
                } catch (Throwable th) {
                    sSLSocket = sSLSocket2;
                    th = th;
                    if (sSLSocket != null) {
                        Platform.get().afterHandshake(sSLSocket);
                    }
                    Util.closeQuietly((Socket) sSLSocket);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (AssertionError e2) {
            e = e2;
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Failed to find switch 'out' block (already processed)
        	at jadx.core.dex.visitors.regions.maker.SwitchRegionMaker.calcSwitchOut(SwitchRegionMaker.java:217)
        	at jadx.core.dex.visitors.regions.maker.SwitchRegionMaker.process(SwitchRegionMaker.java:68)
        	at jadx.core.dex.visitors.regions.maker.RegionMaker.traverse(RegionMaker.java:112)
        	at jadx.core.dex.visitors.regions.maker.RegionMaker.makeRegion(RegionMaker.java:66)
        	at jadx.core.dex.visitors.regions.maker.LoopRegionMaker.process(LoopRegionMaker.java:104)
        	at jadx.core.dex.visitors.regions.maker.RegionMaker.traverse(RegionMaker.java:89)
        	at jadx.core.dex.visitors.regions.maker.RegionMaker.makeRegion(RegionMaker.java:66)
        	at jadx.core.dex.visitors.regions.maker.RegionMaker.makeMthRegion(RegionMaker.java:48)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:25)
        */
    private okhttp3.Request createTunnel(int r10, int r11, okhttp3.Request r12, okhttp3.HttpUrl r13) throws java.io.IOException {
        /*
            Method dump skipped, instruction units count: 240
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealConnection.createTunnel(int, int, okhttp3.Request, okhttp3.HttpUrl):okhttp3.Request");
    }

    private Request createTunnelRequest() {
        return new Request.Builder().url(this.route.address().url()).header(HttpHeaders.HOST, Util.hostHeader(this.route.address().url(), true)).header("Proxy-Connection", "Keep-Alive").header(HttpHeaders.USER_AGENT, Version.userAgent()).build();
    }

    private void establishProtocol(int i, int i2, ConnectionSpecSelector connectionSpecSelector) throws Throwable {
        if (this.route.address().sslSocketFactory() == null) {
            this.protocol = Protocol.HTTP_1_1;
            this.socket = this.rawSocket;
        } else {
            connectTls(i, i2, connectionSpecSelector);
        }
        if (this.protocol != Protocol.HTTP_2) {
            this.allocationLimit = 1;
            return;
        }
        this.socket.setSoTimeout(0);
        Http2Connection http2ConnectionBuild = new Http2Connection.Builder(true).socket(this.socket, this.route.address().url().host(), this.source, this.sink).listener(this).build();
        http2ConnectionBuild.start();
        this.allocationLimit = http2ConnectionBuild.maxConcurrentStreams();
        this.http2Connection = http2ConnectionBuild;
    }

    public void cancel() {
        Util.closeQuietly(this.rawSocket);
    }

    public void connect(int i, int i2, int i3, List<ConnectionSpec> list, boolean z) throws Throwable {
        RouteException routeException;
        if (this.protocol != null) {
            throw new IllegalStateException("already connected");
        }
        ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(list);
        if (this.route.address().sslSocketFactory() != null) {
            routeException = null;
        } else {
            if (!list.contains(ConnectionSpec.CLEARTEXT)) {
                throw new RouteException(new UnknownServiceException("CLEARTEXT communication not enabled for client"));
            }
            String strHost = this.route.address().url().host();
            if (!Platform.get().isCleartextTrafficPermitted(strHost)) {
                throw new RouteException(new UnknownServiceException("CLEARTEXT communication to " + strHost + " not permitted by network security policy"));
            }
            routeException = null;
        }
        while (this.protocol == null) {
            try {
                if (this.route.requiresTunnel()) {
                    buildTunneledConnection(i, i2, i3, connectionSpecSelector);
                } else {
                    buildConnection(i, i2, i3, connectionSpecSelector);
                }
            } catch (IOException e) {
                Util.closeQuietly(this.socket);
                Util.closeQuietly(this.rawSocket);
                this.socket = null;
                this.rawSocket = null;
                this.source = null;
                this.sink = null;
                this.handshake = null;
                this.protocol = null;
                if (routeException != null) {
                    routeException.addConnectException(e);
                } else {
                    routeException = new RouteException(e);
                }
                if (!z) {
                    throw routeException;
                }
                if (!connectionSpecSelector.connectionFailed(e)) {
                    throw routeException;
                }
            }
        }
    }

    @Override // okhttp3.Connection
    public Handshake handshake() {
        return this.handshake;
    }

    public boolean isHealthy(boolean z) {
        if (this.socket.isClosed() || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) {
            return false;
        }
        if (this.http2Connection != null) {
            return !this.http2Connection.isShutdown();
        }
        if (z) {
            try {
                int soTimeout = this.socket.getSoTimeout();
                try {
                    this.socket.setSoTimeout(1);
                    return !this.source.exhausted();
                } finally {
                    this.socket.setSoTimeout(soTimeout);
                }
            } catch (SocketTimeoutException e) {
            } catch (IOException e2) {
                return false;
            }
        }
        return true;
    }

    public boolean isMultiplexed() {
        return this.http2Connection != null;
    }

    @Override // okhttp3.internal.http2.Http2Connection.Listener
    public void onSettings(Http2Connection http2Connection) {
        this.allocationLimit = http2Connection.maxConcurrentStreams();
    }

    @Override // okhttp3.internal.http2.Http2Connection.Listener
    public void onStream(Http2Stream http2Stream) throws IOException {
        http2Stream.close(ErrorCode.REFUSED_STREAM);
    }

    @Override // okhttp3.Connection
    public Protocol protocol() {
        return this.http2Connection != null ? Protocol.HTTP_2 : this.protocol == null ? Protocol.HTTP_1_1 : this.protocol;
    }

    @Override // okhttp3.Connection
    public Route route() {
        return this.route;
    }

    @Override // okhttp3.Connection
    public Socket socket() {
        return this.socket;
    }

    public String toString() {
        return "Connection{" + this.route.address().url().host() + ":" + this.route.address().url().port() + ", proxy=" + this.route.proxy() + " hostAddress=" + this.route.socketAddress() + " cipherSuite=" + (this.handshake == null ? "none" : this.handshake.cipherSuite()) + " protocol=" + this.protocol + '}';
    }
}
