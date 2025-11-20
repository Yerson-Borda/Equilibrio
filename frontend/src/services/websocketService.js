class WebSocketService {
    constructor() {
        this.socket = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000;
        this.eventListeners = new Map();
        this.userId = null;
    }

    connect(userId) {
        if (!userId) {
            console.error('User ID is required to connect WebSocket');
            return;
        }

        this.userId = userId;

        // Close existing connection if any
        if (this.socket) {
            this.socket.close();
        }

        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const url = `${protocol}//${window.location.host}/ws/${userId}`;

        try {
            this.socket = new WebSocket(url);

            this.socket.onopen = () => {
                console.log('✅ WebSocket connected successfully for user:', userId);
                this.reconnectAttempts = 0;
                this.dispatchEvent('connected', { userId });
            };

            this.socket.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    console.log('📨 WebSocket message received:', message);
                    this.dispatchEvent(message.event, message.data);
                } catch (error) {
                    console.error('❌ Error parsing WebSocket message:', error);
                }
            };

            this.socket.onclose = (event) => {
                console.log('🔌 WebSocket disconnected:', event.code, event.reason);
                this.dispatchEvent('disconnected', { code: event.code, reason: event.reason });
                this.handleReconnection();
            };

            this.socket.onerror = (error) => {
                console.error('❌ WebSocket error:', error);
                this.dispatchEvent('error', { error });
            };

        } catch (error) {
            console.error('❌ Failed to create WebSocket connection:', error);
            this.handleReconnection();
        }
    }

    handleReconnection() {
        if (this.reconnectAttempts < this.maxReconnectAttempts && this.userId) {
            this.reconnectAttempts++;
            console.log(`🔄 Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

            setTimeout(() => {
                this.connect(this.userId);
            }, this.reconnectInterval * this.reconnectAttempts);
        } else {
            console.error('❌ Max reconnection attempts reached');
            this.dispatchEvent('reconnection_failed', {});
        }
    }

    disconnect() {
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
        this.reconnectAttempts = 0;
        this.userId = null;
    }

    addEventListener(event, callback) {
        if (!this.eventListeners.has(event)) {
            this.eventListeners.set(event, []);
        }
        this.eventListeners.get(event).push(callback);
    }

    removeEventListener(event, callback) {
        if (this.eventListeners.has(event)) {
            const listeners = this.eventListeners.get(event);
            const index = listeners.indexOf(callback);
            if (index > -1) {
                listeners.splice(index, 1);
            }
        }
    }

    dispatchEvent(event, data) {
        if (this.eventListeners.has(event)) {
            this.eventListeners.get(event).forEach(callback => {
                try {
                    callback(data);
                } catch (error) {
                    console.error(`❌ Error in event listener for ${event}:`, error);
                }
            });
        }
    }

    isConnected() {
        return this.socket && this.socket.readyState === WebSocket.OPEN;
    }

    // Send message to server (if needed)
    sendMessage(event, data) {
        if (this.isConnected()) {
            const message = {
                event: event,
                data: data,
                timestamp: new Date().toISOString()
            };
            this.socket.send(JSON.stringify(message));
        } else {
            console.warn('⚠️ WebSocket not connected, cannot send message');
        }
    }
}

export const webSocketService = new WebSocketService();