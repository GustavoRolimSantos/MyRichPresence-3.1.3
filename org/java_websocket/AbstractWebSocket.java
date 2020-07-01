package org.java_websocket;

import java.util.*;
import org.slf4j.*;

public abstract class AbstractWebSocket extends WebSocketAdapter
{
    private static final Logger log;
    private boolean tcpNoDelay;
    private boolean reuseAddr;
    private Timer connectionLostTimer;
    private TimerTask connectionLostTimerTask;
    private int connectionLostTimeout;
    private boolean websocketRunning;
    private final Object syncConnectionLost;
    
    public AbstractWebSocket() {
        this.connectionLostTimeout = 60;
        this.websocketRunning = false;
        this.syncConnectionLost = new Object();
    }
    
    public int getConnectionLostTimeout() {
        synchronized (this.syncConnectionLost) {
            return this.connectionLostTimeout;
        }
    }
    
    public void setConnectionLostTimeout(final int connectionLostTimeout) {
        synchronized (this.syncConnectionLost) {
            this.connectionLostTimeout = connectionLostTimeout;
            if (this.connectionLostTimeout <= 0) {
                AbstractWebSocket.log.trace("Connection lost timer stopped");
                this.cancelConnectionLostTimer();
                return;
            }
            if (this.websocketRunning) {
                AbstractWebSocket.log.trace("Connection lost timer restarted");
                try {
                    final ArrayList<WebSocket> connections = new ArrayList<WebSocket>(this.getConnections());
                    for (final WebSocket conn : connections) {
                        if (conn instanceof WebSocketImpl) {
                            final WebSocketImpl webSocketImpl = (WebSocketImpl)conn;
                            webSocketImpl.updateLastPong();
                        }
                    }
                }
                catch (Exception e) {
                    AbstractWebSocket.log.error("Exception during connection lost restart", e);
                }
                this.restartConnectionLostTimer();
            }
        }
    }
    
    protected void stopConnectionLostTimer() {
        synchronized (this.syncConnectionLost) {
            if (this.connectionLostTimer != null || this.connectionLostTimerTask != null) {
                this.websocketRunning = false;
                AbstractWebSocket.log.trace("Connection lost timer stopped");
                this.cancelConnectionLostTimer();
            }
        }
    }
    
    protected void startConnectionLostTimer() {
        synchronized (this.syncConnectionLost) {
            if (this.connectionLostTimeout <= 0) {
                AbstractWebSocket.log.trace("Connection lost timer deactivated");
                return;
            }
            AbstractWebSocket.log.trace("Connection lost timer started");
            this.websocketRunning = true;
            this.restartConnectionLostTimer();
        }
    }
    
    private void restartConnectionLostTimer() {
        this.cancelConnectionLostTimer();
        this.connectionLostTimer = new Timer("WebSocketTimer");
        this.connectionLostTimerTask = new TimerTask() {
            private ArrayList<WebSocket> connections = new ArrayList<WebSocket>();
            
            @Override
            public void run() {
                this.connections.clear();
                try {
                    this.connections.addAll(AbstractWebSocket.this.getConnections());
                    final long current = System.currentTimeMillis() - AbstractWebSocket.this.connectionLostTimeout * 1500;
                    for (final WebSocket conn : this.connections) {
                        AbstractWebSocket.this.executeConnectionLostDetection(conn, current);
                    }
                }
                catch (Exception ex) {}
                this.connections.clear();
            }
        };
        this.connectionLostTimer.scheduleAtFixedRate(this.connectionLostTimerTask, 1000L * this.connectionLostTimeout, 1000L * this.connectionLostTimeout);
    }
    
    private void executeConnectionLostDetection(final WebSocket webSocket, final long current) {
        if (!(webSocket instanceof WebSocketImpl)) {
            return;
        }
        final WebSocketImpl webSocketImpl = (WebSocketImpl)webSocket;
        if (webSocketImpl.getLastPong() < current) {
            AbstractWebSocket.log.trace("Closing connection due to no pong received: {}", webSocketImpl);
            webSocketImpl.closeConnection(1006, "The connection was closed because the other endpoint did not respond with a pong in time. For more information check: https://github.com/TooTallNate/Java-WebSocket/wiki/Lost-connection-detection");
        }
        else if (webSocketImpl.isOpen()) {
            webSocketImpl.sendPing();
        }
        else {
            AbstractWebSocket.log.trace("Trying to ping a non open connection: {}", webSocketImpl);
        }
    }
    
    protected abstract Collection<WebSocket> getConnections();
    
    private void cancelConnectionLostTimer() {
        if (this.connectionLostTimer != null) {
            this.connectionLostTimer.cancel();
            this.connectionLostTimer = null;
        }
        if (this.connectionLostTimerTask != null) {
            this.connectionLostTimerTask.cancel();
            this.connectionLostTimerTask = null;
        }
    }
    
    public boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }
    
    public void setTcpNoDelay(final boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }
    
    public boolean isReuseAddr() {
        return this.reuseAddr;
    }
    
    public void setReuseAddr(final boolean reuseAddr) {
        this.reuseAddr = reuseAddr;
    }
    
    static {
        log = LoggerFactory.getLogger(AbstractWebSocket.class);
    }
}
