if (typeof window !== 'undefined' && typeof window.global === 'undefined') {
  window.global = window;
}

import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * Resolves the appropriate WebSocket URL based on environment configuration,
 * automatically handling Render HTTPS/WSS, Vercel frontend, and local LAN environments.
 */
export function getWebSocketEndpoint() {
  // 1. Explicitly configured WebSocket URL in environment variables
  if (import.meta.env.VITE_WS_URL) {
    return import.meta.env.VITE_WS_URL;
  }

  // 2. Derive from configured REST API URL (e.g. https://mandiplus.onrender.com/api -> wss://mandiplus.onrender.com/ws-chat)
  const apiUrl = import.meta.env.VITE_API_URL;
  if (apiUrl && (apiUrl.startsWith('http://') || apiUrl.startsWith('https://'))) {
    try {
      const parsed = new URL(apiUrl);
      const isSecure = parsed.protocol === 'https:';
      const wsProto = isSecure ? 'wss:' : 'ws:';
      // Strip trailing /api if present and point to /ws-chat
      return `${wsProto}//${parsed.host}/ws-chat`;
    } catch (e) {
      console.warn('Failed to parse VITE_API_URL for WebSocket:', e);
    }
  }

  // 3. Fallback for local development or LAN testing
  const isHttps = typeof window !== 'undefined' && window.location.protocol === 'https:';
  const proto = isHttps ? 'wss:' : 'ws:';
  const host = (typeof window !== 'undefined' && window.location.hostname) || 'localhost';
  const port = import.meta.env.VITE_WS_PORT || '8082';
  return `${proto}//${host}:${port}/ws-chat`;
}

/**
 * Resolves the HTTP/HTTPS fallback URL for SockJS.
 */
export function getSockJsEndpoint() {
  const wsUrl = getWebSocketEndpoint();
  return wsUrl.replace(/^wss:/i, 'https:').replace(/^ws:/i, 'http:');
}

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = new Map();
    this.pendingSubscriptions = [];
    this.activeUser = null;
    this.reconnectAttempts = 0;
  }

  /**
   * Initializes STOMP connection with JWT authentication.
   */
  connect(token, user, onConnectCallback = null, onErrorCallback = null) {
    if (!token || !user) {
      console.warn('[STOMP] Connect skipped: missing token or user');
      return;
    }

    // If already connected with the same token, don't re-handshake
    if (this.client && this.connected && this.activeUser?.id === user.id) {
      if (onConnectCallback) onConnectCallback();
      return;
    }

    // Clean up any existing instance
    this.disconnect();

    this.activeUser = user;
    const wsUrl = getWebSocketEndpoint();
    const sockJsUrl = getSockJsEndpoint();

    console.log('[STOMP] Initializing connection to:', wsUrl, 'for user:', user.email);

    this.client = new Client({
      brokerURL: `${wsUrl}?token=${encodeURIComponent(token)}`,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
        token: token,
        userId: String(user.id)
      },
      // SockJS fallback factory if native WebSocket handshake fails
      webSocketFactory: () => {
        try {
          return new WebSocket(`${wsUrl}?token=${encodeURIComponent(token)}`);
        } catch (err) {
          console.warn('[STOMP] Native WebSocket failed, falling back to SockJS:', err);
          return new SockJS(`${sockJsUrl}?token=${encodeURIComponent(token)}`);
        }
      },
      reconnectDelay: 3500,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: (str) => {
        if (import.meta.env.DEV) {
          // Debug logs can be enabled if needed
        }
      }
    });

    this.client.onConnect = (frame) => {
      console.log('[STOMP] Connected successfully.');
      this.connected = true;
      this.reconnectAttempts = 0;

      // Broadcast connection event
      window.dispatchEvent(new CustomEvent('mandiplus-ws-status', { detail: { connected: true } }));

      // 1. Subscribe to private user queue (messages directed to this user's email/principal)
      this.subscribe('/user/queue/messages', (msg) => {
        this.handleIncomingChatMessage(msg);
      });

      // 2. Subscribe to user-specific topic (/topic/messages.{userId})
      this.subscribe(`/topic/messages.${user.id}`, (msg) => {
        this.handleIncomingChatMessage(msg);
      });

      // 3. Subscribe to real-time online presence
      this.subscribe('/topic/online-status', (statusUpdate) => {
        window.dispatchEvent(new CustomEvent('mandiplus-online-status', { detail: statusUpdate }));
      });

      // Process any queued pending subscriptions
      while (this.pendingSubscriptions.length > 0) {
        const { destination, callback } = this.pendingSubscriptions.shift();
        this.subscribe(destination, callback);
      }

      if (onConnectCallback) onConnectCallback(frame);
    };

    this.client.onDisconnect = () => {
      console.log('[STOMP] Disconnected.');
      this.connected = false;
      window.dispatchEvent(new CustomEvent('mandiplus-ws-status', { detail: { connected: false } }));
    };

    this.client.onStompError = (frame) => {
      console.error('[STOMP] Broker error:', frame.headers['message'], frame.body);
      this.connected = false;
      window.dispatchEvent(new CustomEvent('mandiplus-ws-status', { detail: { connected: false, error: frame.headers['message'] } }));
      if (onErrorCallback) onErrorCallback(frame);
    };

    this.client.onWebSocketClose = () => {
      this.connected = false;
      this.reconnectAttempts++;
      window.dispatchEvent(new CustomEvent('mandiplus-ws-status', { detail: { connected: false } }));
    };

    // Activate the client
    this.client.activate();
  }

  /**
   * Internal handler for chat messages received over STOMP.
   */
  handleIncomingChatMessage(msg) {
    if (!msg) return;

    // Dispatch global CustomEvent consumed by ChatPage, WhatsAppChatWidget, and AuthContext
    window.dispatchEvent(new CustomEvent('mandiplus-chat-message', { detail: msg }));
  }

  /**
   * Subscribes to a destination and parses JSON payloads automatically.
   */
  subscribe(destination, callback) {
    if (!this.client || !this.connected) {
      this.pendingSubscriptions.push({ destination, callback });
      return {
        unsubscribe: () => {
          this.pendingSubscriptions = this.pendingSubscriptions.filter(s => s.destination !== destination);
        }
      };
    }

    try {
      const sub = this.client.subscribe(destination, (message) => {
        try {
          const parsed = JSON.parse(message.body);
          callback(parsed, message);
        } catch (e) {
          callback(message.body, message);
        }
      });

      this.subscriptions.set(destination, sub);
      return sub;
    } catch (e) {
      console.warn(`[STOMP] Failed to subscribe to ${destination}:`, e);
      return { unsubscribe: () => {} };
    }
  }

  /**
   * Sends a message to an application destination (e.g. /app/chat.send).
   */
  send(destination, payload, headers = {}) {
    if (!this.client || !this.connected) {
      console.warn('[STOMP] Send dropped: client not connected.');
      return false;
    }

    try {
      const body = typeof payload === 'string' ? payload : JSON.stringify(payload);
      this.client.publish({
        destination,
        body,
        headers
      });
      return true;
    } catch (e) {
      console.error(`[STOMP] Failed to publish to ${destination}:`, e);
      return false;
    }
  }

  /**
   * Deactivates the STOMP connection cleanly on user logout.
   */
  disconnect() {
    if (this.client) {
      try {
        this.subscriptions.forEach((sub) => {
          try { sub.unsubscribe(); } catch (_) {}
        });
        this.subscriptions.clear();
        this.pendingSubscriptions = [];
        this.client.deactivate();
      } catch (e) {
        console.warn('[STOMP] Error during disconnect:', e);
      }
      this.client = null;
    }
    this.connected = false;
    this.activeUser = null;
  }

  isConnected() {
    return this.connected;
  }
}

export const websocketService = new WebSocketService();
export default websocketService;
