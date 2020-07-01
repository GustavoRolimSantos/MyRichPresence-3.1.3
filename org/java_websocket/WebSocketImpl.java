package org.java_websocket;

import java.nio.*;
import java.nio.channels.*;
import org.java_websocket.server.*;
import org.java_websocket.drafts.*;
import java.util.concurrent.*;
import org.java_websocket.util.*;
import org.java_websocket.framing.*;
import java.io.*;
import org.java_websocket.exceptions.*;
import org.java_websocket.enums.*;
import java.util.*;
import org.java_websocket.handshake.*;
import java.net.*;
import org.slf4j.*;

public class WebSocketImpl implements WebSocket
{
    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_WSS_PORT = 443;
    public static final int RCVBUF = 16384;
    private static final Logger log;
    public final BlockingQueue<ByteBuffer> outQueue;
    public final BlockingQueue<ByteBuffer> inQueue;
    private final WebSocketListener wsl;
    private SelectionKey key;
    private ByteChannel channel;
    private WebSocketServer.WebSocketWorker workerThread;
    private boolean flushandclosestate;
    private volatile ReadyState readyState;
    private List<Draft> knownDrafts;
    private Draft draft;
    private Role role;
    private ByteBuffer tmpHandshakeBytes;
    private ClientHandshake handshakerequest;
    private String closemessage;
    private Integer closecode;
    private Boolean closedremotely;
    private String resourceDescriptor;
    private long lastPong;
    private final Object synchronizeWriteObject;
    private PingFrame pingFrame;
    private Object attachment;
    
    public WebSocketImpl(final WebSocketListener listener, final List<Draft> drafts) {
        this(listener, (Draft)null);
        this.role = Role.SERVER;
        if (drafts == null || drafts.isEmpty()) {
            (this.knownDrafts = new ArrayList<Draft>()).add(new Draft_6455());
        }
        else {
            this.knownDrafts = drafts;
        }
    }
    
    public WebSocketImpl(final WebSocketListener listener, final Draft draft) {
        this.flushandclosestate = false;
        this.readyState = ReadyState.NOT_YET_CONNECTED;
        this.draft = null;
        this.tmpHandshakeBytes = ByteBuffer.allocate(0);
        this.handshakerequest = null;
        this.closemessage = null;
        this.closecode = null;
        this.closedremotely = null;
        this.resourceDescriptor = null;
        this.lastPong = System.currentTimeMillis();
        this.synchronizeWriteObject = new Object();
        if (listener == null || (draft == null && this.role == Role.SERVER)) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        this.outQueue = new LinkedBlockingQueue<ByteBuffer>();
        this.inQueue = new LinkedBlockingQueue<ByteBuffer>();
        this.wsl = listener;
        this.role = Role.CLIENT;
        if (draft != null) {
            this.draft = draft.copyInstance();
        }
    }
    
    public void decode(final ByteBuffer socketBuffer) {
        assert socketBuffer.hasRemaining();
        WebSocketImpl.log.trace("process({}): ({})", (Object)socketBuffer.remaining(), (socketBuffer.remaining() > 1000) ? "too big to display" : new String(socketBuffer.array(), socketBuffer.position(), socketBuffer.remaining()));
        if (this.readyState != ReadyState.NOT_YET_CONNECTED) {
            if (this.readyState == ReadyState.OPEN) {
                this.decodeFrames(socketBuffer);
            }
        }
        else if (this.decodeHandshake(socketBuffer) && !this.isClosing() && !this.isClosed()) {
            assert !socketBuffer.hasRemaining();
            if (socketBuffer.hasRemaining()) {
                this.decodeFrames(socketBuffer);
            }
            else if (this.tmpHandshakeBytes.hasRemaining()) {
                this.decodeFrames(this.tmpHandshakeBytes);
            }
        }
    }
    
    private boolean decodeHandshake(final ByteBuffer socketBufferNew) {
        ByteBuffer socketBuffer;
        if (this.tmpHandshakeBytes.capacity() == 0) {
            socketBuffer = socketBufferNew;
        }
        else {
            if (this.tmpHandshakeBytes.remaining() < socketBufferNew.remaining()) {
                final ByteBuffer buf = ByteBuffer.allocate(this.tmpHandshakeBytes.capacity() + socketBufferNew.remaining());
                this.tmpHandshakeBytes.flip();
                buf.put(this.tmpHandshakeBytes);
                this.tmpHandshakeBytes = buf;
            }
            this.tmpHandshakeBytes.put(socketBufferNew);
            this.tmpHandshakeBytes.flip();
            socketBuffer = this.tmpHandshakeBytes;
        }
        socketBuffer.mark();
        try {
            try {
                if (this.role == Role.SERVER) {
                    if (this.draft == null) {
                        for (Draft d : this.knownDrafts) {
                            d = d.copyInstance();
                            try {
                                d.setParseMode(this.role);
                                socketBuffer.reset();
                                final Handshakedata tmphandshake = d.translateHandshake(socketBuffer);
                                if (!(tmphandshake instanceof ClientHandshake)) {
                                    WebSocketImpl.log.trace("Closing due to wrong handshake");
                                    this.closeConnectionDueToWrongHandshake(new InvalidDataException(1002, "wrong http function"));
                                    return false;
                                }
                                final ClientHandshake handshake = (ClientHandshake)tmphandshake;
                                final HandshakeState handshakestate = d.acceptHandshakeAsServer(handshake);
                                if (handshakestate == HandshakeState.MATCHED) {
                                    this.resourceDescriptor = handshake.getResourceDescriptor();
                                    ServerHandshakeBuilder response;
                                    try {
                                        response = this.wsl.onWebsocketHandshakeReceivedAsServer(this, d, handshake);
                                    }
                                    catch (InvalidDataException e) {
                                        WebSocketImpl.log.trace("Closing due to wrong handshake. Possible handshake rejection", e);
                                        this.closeConnectionDueToWrongHandshake(e);
                                        return false;
                                    }
                                    catch (RuntimeException e2) {
                                        WebSocketImpl.log.error("Closing due to internal server error", e2);
                                        this.wsl.onWebsocketError(this, e2);
                                        this.closeConnectionDueToInternalServerError(e2);
                                        return false;
                                    }
                                    this.write(d.createHandshake(d.postProcessHandshakeResponseAsServer(handshake, response)));
                                    this.draft = d;
                                    this.open(handshake);
                                    return true;
                                }
                                continue;
                            }
                            catch (InvalidHandshakeException ex) {}
                        }
                        if (this.draft == null) {
                            WebSocketImpl.log.trace("Closing due to protocol error: no draft matches");
                            this.closeConnectionDueToWrongHandshake(new InvalidDataException(1002, "no draft matches"));
                        }
                        return false;
                    }
                    final Handshakedata tmphandshake2 = this.draft.translateHandshake(socketBuffer);
                    if (!(tmphandshake2 instanceof ClientHandshake)) {
                        WebSocketImpl.log.trace("Closing due to protocol error: wrong http function");
                        this.flushAndClose(1002, "wrong http function", false);
                        return false;
                    }
                    final ClientHandshake handshake2 = (ClientHandshake)tmphandshake2;
                    final HandshakeState handshakestate = this.draft.acceptHandshakeAsServer(handshake2);
                    if (handshakestate == HandshakeState.MATCHED) {
                        this.open(handshake2);
                        return true;
                    }
                    WebSocketImpl.log.trace("Closing due to protocol error: the handshake did finally not match");
                    this.close(1002, "the handshake did finally not match");
                    return false;
                }
                else if (this.role == Role.CLIENT) {
                    this.draft.setParseMode(this.role);
                    final Handshakedata tmphandshake2 = this.draft.translateHandshake(socketBuffer);
                    if (!(tmphandshake2 instanceof ServerHandshake)) {
                        WebSocketImpl.log.trace("Closing due to protocol error: wrong http function");
                        this.flushAndClose(1002, "wrong http function", false);
                        return false;
                    }
                    final ServerHandshake handshake3 = (ServerHandshake)tmphandshake2;
                    final HandshakeState handshakestate = this.draft.acceptHandshakeAsClient(this.handshakerequest, handshake3);
                    if (handshakestate == HandshakeState.MATCHED) {
                        try {
                            this.wsl.onWebsocketHandshakeReceivedAsClient(this, this.handshakerequest, handshake3);
                        }
                        catch (InvalidDataException e3) {
                            WebSocketImpl.log.trace("Closing due to invalid data exception. Possible handshake rejection", e3);
                            this.flushAndClose(e3.getCloseCode(), e3.getMessage(), false);
                            return false;
                        }
                        catch (RuntimeException e4) {
                            WebSocketImpl.log.error("Closing since client was never connected", e4);
                            this.wsl.onWebsocketError(this, e4);
                            this.flushAndClose(-1, e4.getMessage(), false);
                            return false;
                        }
                        this.open(handshake3);
                        return true;
                    }
                    WebSocketImpl.log.trace("Closing due to protocol error: draft {} refuses handshake", this.draft);
                    this.close(1002, "draft " + this.draft + " refuses handshake");
                }
            }
            catch (InvalidHandshakeException e5) {
                WebSocketImpl.log.trace("Closing due to invalid handshake", e5);
                this.close(e5);
            }
        }
        catch (IncompleteHandshakeException e6) {
            if (this.tmpHandshakeBytes.capacity() == 0) {
                socketBuffer.reset();
                int newsize = e6.getPreferredSize();
                if (newsize == 0) {
                    newsize = socketBuffer.capacity() + 16;
                }
                else {
                    assert e6.getPreferredSize() >= socketBuffer.remaining();
                }
                (this.tmpHandshakeBytes = ByteBuffer.allocate(newsize)).put(socketBufferNew);
            }
            else {
                this.tmpHandshakeBytes.position(this.tmpHandshakeBytes.limit());
                this.tmpHandshakeBytes.limit(this.tmpHandshakeBytes.capacity());
            }
        }
        return false;
    }
    
    private void decodeFrames(final ByteBuffer socketBuffer) {
        try {
            final List<Framedata> frames = this.draft.translateFrame(socketBuffer);
            for (final Framedata f : frames) {
                WebSocketImpl.log.trace("matched frame: {}", f);
                this.draft.processFrame(this, f);
            }
        }
        catch (LimitExceededException e) {
            if (e.getLimit() == Integer.MAX_VALUE) {
                WebSocketImpl.log.error("Closing due to invalid size of frame", e);
                this.wsl.onWebsocketError(this, e);
            }
            this.close(e);
        }
        catch (InvalidDataException e2) {
            WebSocketImpl.log.error("Closing due to invalid data in frame", e2);
            this.wsl.onWebsocketError(this, e2);
            this.close(e2);
        }
    }
    
    private void closeConnectionDueToWrongHandshake(final InvalidDataException exception) {
        this.write(this.generateHttpResponseDueToError(404));
        this.flushAndClose(exception.getCloseCode(), exception.getMessage(), false);
    }
    
    private void closeConnectionDueToInternalServerError(final RuntimeException exception) {
        this.write(this.generateHttpResponseDueToError(500));
        this.flushAndClose(-1, exception.getMessage(), false);
    }
    
    private ByteBuffer generateHttpResponseDueToError(final int errorCode) {
        String errorCodeDescription = null;
        switch (errorCode) {
            case 404: {
                errorCodeDescription = "404 WebSocket Upgrade Failure";
                break;
            }
            default: {
                errorCodeDescription = "500 Internal Server Error";
                break;
            }
        }
        return ByteBuffer.wrap(Charsetfunctions.asciiBytes("HTTP/1.1 " + errorCodeDescription + "\r\nContent-Type: text/html\nServer: TooTallNate Java-WebSocket\r\nContent-Length: " + (48 + errorCodeDescription.length()) + "\r\n\r\n<html><head></head><body><h1>" + errorCodeDescription + "</h1></body></html>"));
    }
    
    public synchronized void close(final int code, final String message, final boolean remote) {
        if (this.readyState != ReadyState.CLOSING && this.readyState != ReadyState.CLOSED) {
            if (this.readyState == ReadyState.OPEN) {
                if (code == 1006) {
                    assert !remote;
                    this.readyState = ReadyState.CLOSING;
                    this.flushAndClose(code, message, false);
                    return;
                }
                else {
                    if (this.draft.getCloseHandshakeType() != CloseHandshakeType.NONE) {
                        try {
                            if (!remote) {
                                try {
                                    this.wsl.onWebsocketCloseInitiated(this, code, message);
                                }
                                catch (RuntimeException e) {
                                    this.wsl.onWebsocketError(this, e);
                                }
                            }
                            if (this.isOpen()) {
                                final CloseFrame closeFrame = new CloseFrame();
                                closeFrame.setReason(message);
                                closeFrame.setCode(code);
                                closeFrame.isValid();
                                this.sendFrame(closeFrame);
                            }
                        }
                        catch (InvalidDataException e2) {
                            WebSocketImpl.log.error("generated frame is invalid", e2);
                            this.wsl.onWebsocketError(this, e2);
                            this.flushAndClose(1006, "generated frame is invalid", false);
                        }
                    }
                    this.flushAndClose(code, message, remote);
                }
            }
            else if (code == -3) {
                assert remote;
                this.flushAndClose(-3, message, true);
            }
            else if (code == 1002) {
                this.flushAndClose(code, message, remote);
            }
            else {
                this.flushAndClose(-1, message, false);
            }
            this.readyState = ReadyState.CLOSING;
            this.tmpHandshakeBytes = null;
        }
    }
    
    @Override
    public void close(final int code, final String message) {
        this.close(code, message, false);
    }
    
    public synchronized void closeConnection(final int code, final String message, final boolean remote) {
        if (this.readyState == ReadyState.CLOSED) {
            return;
        }
        if (this.readyState == ReadyState.OPEN && code == 1006) {
            this.readyState = ReadyState.CLOSING;
        }
        if (this.key != null) {
            this.key.cancel();
        }
        if (this.channel != null) {
            try {
                this.channel.close();
            }
            catch (IOException e) {
                if (e.getMessage().equals("Broken pipe")) {
                    WebSocketImpl.log.trace("Caught IOException: Broken pipe during closeConnection()", e);
                }
                else {
                    WebSocketImpl.log.error("Exception during channel.close()", e);
                    this.wsl.onWebsocketError(this, e);
                }
            }
        }
        try {
            this.wsl.onWebsocketClose(this, code, message, remote);
        }
        catch (RuntimeException e2) {
            this.wsl.onWebsocketError(this, e2);
        }
        if (this.draft != null) {
            this.draft.reset();
        }
        this.handshakerequest = null;
        this.readyState = ReadyState.CLOSED;
    }
    
    protected void closeConnection(final int code, final boolean remote) {
        this.closeConnection(code, "", remote);
    }
    
    public void closeConnection() {
        if (this.closedremotely == null) {
            throw new IllegalStateException("this method must be used in conjunction with flushAndClose");
        }
        this.closeConnection(this.closecode, this.closemessage, this.closedremotely);
    }
    
    @Override
    public void closeConnection(final int code, final String message) {
        this.closeConnection(code, message, false);
    }
    
    public synchronized void flushAndClose(final int code, final String message, final boolean remote) {
        if (this.flushandclosestate) {
            return;
        }
        this.closecode = code;
        this.closemessage = message;
        this.closedremotely = remote;
        this.flushandclosestate = true;
        this.wsl.onWriteDemand(this);
        try {
            this.wsl.onWebsocketClosing(this, code, message, remote);
        }
        catch (RuntimeException e) {
            WebSocketImpl.log.error("Exception in onWebsocketClosing", e);
            this.wsl.onWebsocketError(this, e);
        }
        if (this.draft != null) {
            this.draft.reset();
        }
        this.handshakerequest = null;
    }
    
    public void eot() {
        if (this.readyState == ReadyState.NOT_YET_CONNECTED) {
            this.closeConnection(-1, true);
        }
        else if (this.flushandclosestate) {
            this.closeConnection(this.closecode, this.closemessage, this.closedremotely);
        }
        else if (this.draft.getCloseHandshakeType() == CloseHandshakeType.NONE) {
            this.closeConnection(1000, true);
        }
        else if (this.draft.getCloseHandshakeType() == CloseHandshakeType.ONEWAY) {
            if (this.role == Role.SERVER) {
                this.closeConnection(1006, true);
            }
            else {
                this.closeConnection(1000, true);
            }
        }
        else {
            this.closeConnection(1006, true);
        }
    }
    
    @Override
    public void close(final int code) {
        this.close(code, "", false);
    }
    
    public void close(final InvalidDataException e) {
        this.close(e.getCloseCode(), e.getMessage(), false);
    }
    
    @Override
    public void send(final String text) {
        if (text == null) {
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        }
        this.send(this.draft.createFrames(text, this.role == Role.CLIENT));
    }
    
    @Override
    public void send(final ByteBuffer bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        }
        this.send(this.draft.createFrames(bytes, this.role == Role.CLIENT));
    }
    
    @Override
    public void send(final byte[] bytes) {
        this.send(ByteBuffer.wrap(bytes));
    }
    
    private void send(final Collection<Framedata> frames) {
        if (!this.isOpen()) {
            throw new WebsocketNotConnectedException();
        }
        if (frames == null) {
            throw new IllegalArgumentException();
        }
        final ArrayList<ByteBuffer> outgoingFrames = new ArrayList<ByteBuffer>();
        for (final Framedata f : frames) {
            WebSocketImpl.log.trace("send frame: {}", f);
            outgoingFrames.add(this.draft.createBinaryFrame(f));
        }
        this.write(outgoingFrames);
    }
    
    @Override
    public void sendFragmentedFrame(final Opcode op, final ByteBuffer buffer, final boolean fin) {
        this.send(this.draft.continuousFrame(op, buffer, fin));
    }
    
    @Override
    public void sendFrame(final Collection<Framedata> frames) {
        this.send(frames);
    }
    
    @Override
    public void sendFrame(final Framedata framedata) {
        this.send(Collections.singletonList(framedata));
    }
    
    @Override
    public void sendPing() {
        if (this.pingFrame == null) {
            this.pingFrame = new PingFrame();
        }
        this.sendFrame(this.pingFrame);
    }
    
    @Override
    public boolean hasBufferedData() {
        return !this.outQueue.isEmpty();
    }
    
    public void startHandshake(final ClientHandshakeBuilder handshakedata) throws InvalidHandshakeException {
        this.handshakerequest = this.draft.postProcessHandshakeRequestAsClient(handshakedata);
        this.resourceDescriptor = handshakedata.getResourceDescriptor();
        assert this.resourceDescriptor != null;
        try {
            this.wsl.onWebsocketHandshakeSentAsClient(this, this.handshakerequest);
        }
        catch (InvalidDataException e2) {
            throw new InvalidHandshakeException("Handshake data rejected by client.");
        }
        catch (RuntimeException e) {
            WebSocketImpl.log.error("Exception in startHandshake", e);
            this.wsl.onWebsocketError(this, e);
            throw new InvalidHandshakeException("rejected because of " + e);
        }
        this.write(this.draft.createHandshake(this.handshakerequest));
    }
    
    private void write(final ByteBuffer buf) {
        WebSocketImpl.log.trace("write({}): {}", (Object)buf.remaining(), (buf.remaining() > 1000) ? "too big to display" : new String(buf.array()));
        this.outQueue.add(buf);
        this.wsl.onWriteDemand(this);
    }
    
    private void write(final List<ByteBuffer> bufs) {
        synchronized (this.synchronizeWriteObject) {
            for (final ByteBuffer b : bufs) {
                this.write(b);
            }
        }
    }
    
    private void open(final Handshakedata d) {
        WebSocketImpl.log.trace("open using draft: {}", this.draft);
        this.readyState = ReadyState.OPEN;
        try {
            this.wsl.onWebsocketOpen(this, d);
        }
        catch (RuntimeException e) {
            this.wsl.onWebsocketError(this, e);
        }
    }
    
    @Override
    public boolean isOpen() {
        return this.readyState == ReadyState.OPEN;
    }
    
    @Override
    public boolean isClosing() {
        return this.readyState == ReadyState.CLOSING;
    }
    
    @Override
    public boolean isFlushAndClose() {
        return this.flushandclosestate;
    }
    
    @Override
    public boolean isClosed() {
        return this.readyState == ReadyState.CLOSED;
    }
    
    @Override
    public ReadyState getReadyState() {
        return this.readyState;
    }
    
    public void setSelectionKey(final SelectionKey key) {
        this.key = key;
    }
    
    public SelectionKey getSelectionKey() {
        return this.key;
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
    
    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return this.wsl.getRemoteSocketAddress(this);
    }
    
    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return this.wsl.getLocalSocketAddress(this);
    }
    
    @Override
    public Draft getDraft() {
        return this.draft;
    }
    
    @Override
    public void close() {
        this.close(1000);
    }
    
    @Override
    public String getResourceDescriptor() {
        return this.resourceDescriptor;
    }
    
    long getLastPong() {
        return this.lastPong;
    }
    
    public void updateLastPong() {
        this.lastPong = System.currentTimeMillis();
    }
    
    public WebSocketListener getWebSocketListener() {
        return this.wsl;
    }
    
    @Override
    public <T> T getAttachment() {
        return (T)this.attachment;
    }
    
    @Override
    public <T> void setAttachment(final T attachment) {
        this.attachment = attachment;
    }
    
    public ByteChannel getChannel() {
        return this.channel;
    }
    
    public void setChannel(final ByteChannel channel) {
        this.channel = channel;
    }
    
    public WebSocketServer.WebSocketWorker getWorkerThread() {
        return this.workerThread;
    }
    
    public void setWorkerThread(final WebSocketServer.WebSocketWorker workerThread) {
        this.workerThread = workerThread;
    }
    
    static {
        log = LoggerFactory.getLogger(WebSocketImpl.class);
    }
}
