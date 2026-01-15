/**
 * WebSocket composable for real-time updates.
 *
 * Provides a reactive WebSocket connection with:
 * - Automatic reconnection with exponential backoff
 * - Connection state management
 * - Message subscription
 * - STOMP-like protocol support (for Spring WebSocket)
 */

import { ref, computed, onUnmounted, type Ref } from 'vue'

export type ConnectionState = 'connecting' | 'connected' | 'disconnected' | 'error'

export interface WebSocketMessage<T = unknown> {
  type: string
  payload: T
  timestamp: number
}

export interface UseWebSocketOptions {
  url: string
  autoConnect?: boolean
  reconnect?: boolean
  reconnectDelay?: number
  maxReconnectDelay?: number
  maxReconnectAttempts?: number
  onMessage?: (message: WebSocketMessage) => void
  onConnect?: () => void
  onDisconnect?: () => void
  onError?: (error: Event) => void
}

const DEFAULT_RECONNECT_DELAY = 1000
const DEFAULT_MAX_RECONNECT_DELAY = 30000
const DEFAULT_MAX_RECONNECT_ATTEMPTS = 10

/**
 * WebSocket composable for real-time updates.
 *
 * @example
 * ```vue
 * <script setup>
 * import { useWebSocket } from '@/composables/useWebSocket'
 *
 * const { state, connect, disconnect, subscribe } = useWebSocket({
 *   url: 'ws://localhost:8080/ws',
 *   onConnect: () => console.log('Connected'),
 * })
 *
 * // Subscribe to loot award events
 * const unsubscribe = subscribe('loot-awarded', (data) => {
 *   console.log('New loot:', data)
 * })
 *
 * onUnmounted(() => unsubscribe())
 * </script>
 * ```
 */
export function useWebSocket(options: UseWebSocketOptions) {
  const {
    url,
    autoConnect = true,
    reconnect = true,
    reconnectDelay = DEFAULT_RECONNECT_DELAY,
    maxReconnectDelay = DEFAULT_MAX_RECONNECT_DELAY,
    maxReconnectAttempts = DEFAULT_MAX_RECONNECT_ATTEMPTS,
    onMessage,
    onConnect,
    onDisconnect,
    onError,
  } = options

  const state = ref<ConnectionState>('disconnected')
  const reconnectAttempts = ref(0)
  const lastMessage = ref<WebSocketMessage | null>(null)

  let socket: WebSocket | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  const subscriptions = new Map<string, Set<(payload: unknown) => void>>()

  const isConnected = computed(() => state.value === 'connected')
  const isConnecting = computed(() => state.value === 'connecting')

  /**
   * Connect to the WebSocket server.
   */
  function connect(): void {
    if (socket?.readyState === WebSocket.OPEN || state.value === 'connecting') {
      return
    }

    state.value = 'connecting'
    clearReconnectTimer()

    try {
      socket = new WebSocket(url)

      socket.onopen = () => {
        state.value = 'connected'
        reconnectAttempts.value = 0
        onConnect?.()
      }

      socket.onclose = () => {
        state.value = 'disconnected'
        onDisconnect?.()
        attemptReconnect()
      }

      socket.onerror = (event) => {
        state.value = 'error'
        onError?.(event)
      }

      socket.onmessage = (event) => {
        try {
          const message = parseMessage(event.data)
          lastMessage.value = message
          onMessage?.(message)

          // Dispatch to subscriptions
          const handlers = subscriptions.get(message.type)
          if (handlers) {
            handlers.forEach((handler) => handler(message.payload))
          }

          // Also dispatch to wildcard subscribers
          const wildcardHandlers = subscriptions.get('*')
          if (wildcardHandlers) {
            wildcardHandlers.forEach((handler) => handler(message))
          }
        } catch (e) {
          console.error('Failed to parse WebSocket message:', e)
        }
      }
    } catch (e) {
      state.value = 'error'
      console.error('Failed to create WebSocket connection:', e)
    }
  }

  /**
   * Disconnect from the WebSocket server.
   */
  function disconnect(): void {
    clearReconnectTimer()
    reconnectAttempts.value = maxReconnectAttempts // Prevent reconnection

    if (socket) {
      socket.close()
      socket = null
    }

    state.value = 'disconnected'
  }

  /**
   * Send a message to the server.
   */
  function send(type: string, payload: unknown): boolean {
    if (socket?.readyState !== WebSocket.OPEN) {
      console.warn('WebSocket is not connected')
      return false
    }

    const message: WebSocketMessage = {
      type,
      payload,
      timestamp: Date.now(),
    }

    socket.send(JSON.stringify(message))
    return true
  }

  /**
   * Subscribe to messages of a specific type.
   *
   * @param type - Message type to subscribe to (use '*' for all messages)
   * @param handler - Callback function
   * @returns Unsubscribe function
   */
  function subscribe<T = unknown>(
    type: string,
    handler: (payload: T) => void
  ): () => void {
    if (!subscriptions.has(type)) {
      subscriptions.set(type, new Set())
    }

    subscriptions.get(type)!.add(handler as (payload: unknown) => void)

    return () => {
      const handlers = subscriptions.get(type)
      if (handlers) {
        handlers.delete(handler as (payload: unknown) => void)
        if (handlers.size === 0) {
          subscriptions.delete(type)
        }
      }
    }
  }

  /**
   * Parse incoming message.
   */
  function parseMessage(data: string): WebSocketMessage {
    const parsed = JSON.parse(data)

    // Handle both simple and complex message formats
    if (parsed.type && parsed.payload !== undefined) {
      return {
        type: parsed.type,
        payload: parsed.payload,
        timestamp: parsed.timestamp ?? Date.now(),
      }
    }

    // Fallback for simple messages
    return {
      type: 'message',
      payload: parsed,
      timestamp: Date.now(),
    }
  }

  /**
   * Attempt to reconnect with exponential backoff.
   */
  function attemptReconnect(): void {
    if (!reconnect || reconnectAttempts.value >= maxReconnectAttempts) {
      return
    }

    reconnectAttempts.value++
    const delay = Math.min(
      reconnectDelay * Math.pow(2, reconnectAttempts.value - 1),
      maxReconnectDelay
    )

    reconnectTimer = setTimeout(() => {
      connect()
    }, delay)
  }

  /**
   * Clear reconnection timer.
   */
  function clearReconnectTimer(): void {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  // Auto-connect on mount
  if (autoConnect) {
    connect()
  }

  // Cleanup on unmount
  onUnmounted(() => {
    disconnect()
    subscriptions.clear()
  })

  return {
    state,
    isConnected,
    isConnecting,
    lastMessage,
    reconnectAttempts,
    connect,
    disconnect,
    send,
    subscribe,
  }
}

/**
 * Create a shared WebSocket connection instance.
 * Use this for a singleton connection across the app.
 */
let sharedInstance: ReturnType<typeof useWebSocket> | null = null

export function useSharedWebSocket(options?: Partial<UseWebSocketOptions>) {
  if (!sharedInstance) {
    const defaultUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws`
    sharedInstance = useWebSocket({
      url: options?.url ?? defaultUrl,
      autoConnect: options?.autoConnect ?? false,
      ...options,
    })
  }

  return sharedInstance
}

export default useWebSocket
