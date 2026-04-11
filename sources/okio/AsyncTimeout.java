package okio;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: classes.dex */
public class AsyncTimeout extends Timeout {
    private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);
    private static final int TIMEOUT_WRITE_SIZE = 65536;
    private static AsyncTimeout head;
    private boolean inQueue;
    private AsyncTimeout next;
    private long timeoutAt;

    private static final class Watchdog extends Thread {
        public Watchdog() {
            super("Okio Watchdog");
            setDaemon(true);
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            AsyncTimeout asyncTimeoutAwaitTimeout;
            while (true) {
                synchronized (AsyncTimeout.class) {
                    asyncTimeoutAwaitTimeout = AsyncTimeout.awaitTimeout();
                    if (asyncTimeoutAwaitTimeout != null) {
                        if (asyncTimeoutAwaitTimeout == AsyncTimeout.head) {
                            AsyncTimeout unused = AsyncTimeout.head = null;
                            return;
                        }
                    }
                }
                asyncTimeoutAwaitTimeout.timedOut();
            }
        }
    }

    static AsyncTimeout awaitTimeout() throws InterruptedException {
        AsyncTimeout asyncTimeout = head.next;
        if (asyncTimeout == null) {
            long jNanoTime = System.nanoTime();
            AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS);
            if (head.next != null) {
                return null;
            }
            if (System.nanoTime() - jNanoTime < IDLE_TIMEOUT_NANOS) {
                return null;
            }
            return head;
        }
        long jRemainingNanos = asyncTimeout.remainingNanos(System.nanoTime());
        if (jRemainingNanos <= 0) {
            head.next = asyncTimeout.next;
            asyncTimeout.next = null;
            return asyncTimeout;
        }
        long j = jRemainingNanos / 1000000;
        AsyncTimeout.class.wait(j, (int) (jRemainingNanos - (j * 1000000)));
        return null;
    }

    private static synchronized boolean cancelScheduledTimeout(AsyncTimeout asyncTimeout) {
        for (AsyncTimeout asyncTimeout2 = head; asyncTimeout2 != null; asyncTimeout2 = asyncTimeout2.next) {
            if (asyncTimeout2.next == asyncTimeout) {
                asyncTimeout2.next = asyncTimeout.next;
                asyncTimeout.next = null;
                return false;
            }
        }
        return true;
    }

    private long remainingNanos(long j) {
        return this.timeoutAt - j;
    }

    private static synchronized void scheduleTimeout(AsyncTimeout asyncTimeout, long j, boolean z) {
        AsyncTimeout asyncTimeout2;
        if (head == null) {
            head = new AsyncTimeout();
            new Watchdog().start();
        }
        long jNanoTime = System.nanoTime();
        if (j != 0 && z) {
            asyncTimeout.timeoutAt = Math.min(j, asyncTimeout.deadlineNanoTime() - jNanoTime) + jNanoTime;
        } else if (j != 0) {
            asyncTimeout.timeoutAt = jNanoTime + j;
        } else {
            if (!z) {
                throw new AssertionError();
            }
            asyncTimeout.timeoutAt = asyncTimeout.deadlineNanoTime();
        }
        long jRemainingNanos = asyncTimeout.remainingNanos(jNanoTime);
        AsyncTimeout asyncTimeout3 = head;
        while (true) {
            asyncTimeout2 = asyncTimeout3;
            if (asyncTimeout2.next == null) {
                break;
            }
            if (!(jRemainingNanos >= asyncTimeout2.next.remainingNanos(jNanoTime))) {
                break;
            } else {
                asyncTimeout3 = asyncTimeout2.next;
            }
        }
        asyncTimeout.next = asyncTimeout2.next;
        asyncTimeout2.next = asyncTimeout;
        if (asyncTimeout2 == head) {
            AsyncTimeout.class.notify();
        }
    }

    public final void enter() {
        if (this.inQueue) {
            throw new IllegalStateException("Unbalanced enter/exit");
        }
        long jTimeoutNanos = timeoutNanos();
        boolean zHasDeadline = hasDeadline();
        if (jTimeoutNanos != 0 || zHasDeadline) {
            this.inQueue = true;
            scheduleTimeout(this, jTimeoutNanos, zHasDeadline);
        }
    }

    final IOException exit(IOException iOException) throws IOException {
        return exit() ? newTimeoutException(iOException) : iOException;
    }

    final void exit(boolean z) throws IOException {
        if (exit() && z) {
            throw newTimeoutException(null);
        }
    }

    public final boolean exit() {
        if (!this.inQueue) {
            return false;
        }
        this.inQueue = false;
        return cancelScheduledTimeout(this);
    }

    protected IOException newTimeoutException(IOException iOException) {
        InterruptedIOException interruptedIOException = new InterruptedIOException("timeout");
        if (iOException != null) {
            interruptedIOException.initCause(iOException);
        }
        return interruptedIOException;
    }

    public final Sink sink(final Sink sink) {
        return new Sink() { // from class: okio.AsyncTimeout.1
            @Override // okio.Sink, java.io.Closeable, java.lang.AutoCloseable
            public void close() throws IOException {
                AsyncTimeout.this.enter();
                try {
                    try {
                        sink.close();
                        AsyncTimeout.this.exit(true);
                    } catch (IOException e) {
                        throw AsyncTimeout.this.exit(e);
                    }
                } catch (Throwable th) {
                    AsyncTimeout.this.exit(false);
                    throw th;
                }
            }

            @Override // okio.Sink, java.io.Flushable
            public void flush() throws IOException {
                AsyncTimeout.this.enter();
                try {
                    try {
                        sink.flush();
                        AsyncTimeout.this.exit(true);
                    } catch (IOException e) {
                        throw AsyncTimeout.this.exit(e);
                    }
                } catch (Throwable th) {
                    AsyncTimeout.this.exit(false);
                    throw th;
                }
            }

            @Override // okio.Sink
            public Timeout timeout() {
                return AsyncTimeout.this;
            }

            public String toString() {
                return "AsyncTimeout.sink(" + sink + ")";
            }

            @Override // okio.Sink
            public void write(Buffer buffer, long j) throws IOException {
                Util.checkOffsetAndCount(buffer.size, 0L, j);
                long j2 = j;
                while (true) {
                    if (j2 <= 0) {
                        return;
                    }
                    Segment segment = buffer.head;
                    long j3 = 0;
                    while (true) {
                        if (j3 >= 65536) {
                            break;
                        }
                        long j4 = ((long) (buffer.head.limit - buffer.head.pos)) + j3;
                        if (!(j4 < j2)) {
                            j3 = j2;
                            break;
                        } else {
                            segment = segment.next;
                            j3 = j4;
                        }
                    }
                    AsyncTimeout.this.enter();
                    try {
                        try {
                            sink.write(buffer, j3);
                            j2 -= j3;
                            AsyncTimeout.this.exit(true);
                        } catch (IOException e) {
                            throw AsyncTimeout.this.exit(e);
                        }
                    } catch (Throwable th) {
                        AsyncTimeout.this.exit(false);
                        throw th;
                    }
                }
            }
        };
    }

    public final Source source(final Source source) {
        return new Source() { // from class: okio.AsyncTimeout.2
            @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
            public void close() throws IOException {
                try {
                    try {
                        source.close();
                        AsyncTimeout.this.exit(true);
                    } catch (IOException e) {
                        throw AsyncTimeout.this.exit(e);
                    }
                } catch (Throwable th) {
                    AsyncTimeout.this.exit(false);
                    throw th;
                }
            }

            @Override // okio.Source
            public long read(Buffer buffer, long j) throws IOException {
                AsyncTimeout.this.enter();
                try {
                    try {
                        long j2 = source.read(buffer, j);
                        AsyncTimeout.this.exit(true);
                        return j2;
                    } catch (IOException e) {
                        throw AsyncTimeout.this.exit(e);
                    }
                } catch (Throwable th) {
                    AsyncTimeout.this.exit(false);
                    throw th;
                }
            }

            @Override // okio.Source
            public Timeout timeout() {
                return AsyncTimeout.this;
            }

            public String toString() {
                return "AsyncTimeout.source(" + source + ")";
            }
        };
    }

    protected void timedOut() {
    }
}
