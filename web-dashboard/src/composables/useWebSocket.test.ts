import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useWebSocket } from './useWebSocket'
import { nextTick } from 'vue'

// Mock WebSocket
class MockWebSocket {
  static CONNECTING = 0
  static OPEN = 1
  static CLOSING = 2
  static CLOSED = 3

  url: string
  readyState: number = MockWebSocket.CONNECTING
  onopen: (() => void) | null = null
  onclose: (() => void) | null = null
  onerror: ((event: Event) => void) | null = null
  onmessage: ((event: MessageEvent) => void) | null = null

  constructor(url: string) {
    this.url = url
  }

  close() {
    this.readyState = MockWebSocket.CLOSED
    this.onclose?.()
  }

  send = vi.fn()

  // Test helpers
  simulateOpen() {
    this.readyState = MockWebSocket.OPEN
    this.onopen?.()
  }

  simulateMessage(data: unknown) {
    this.onmessage?.({ data: JSON.stringify(data) } as MessageEvent)
  }

  simulateError() {
    this.onerror?.(new Event('error'))
  }
}

let mockWebSocketInstance: MockWebSocket | null = null

describe('useWebSocket', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    mockWebSocketInstance = null

    // Mock global WebSocket
    vi.stubGlobal('WebSocket', class extends MockWebSocket {
      constructor(url: string) {
        super(url)
        mockWebSocketInstance = this
      }
    })
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
  })

  describe('connection', () => {
    it('should connect to WebSocket server', () => {
      const { state } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        autoConnect: true,
      })

      expect(state.value).toBe('connecting')
      expect(mockWebSocketInstance).not.toBeNull()
      expect(mockWebSocketInstance?.url).toBe('ws://localhost:8080/ws')
    })

    it('should update state to connected on open', async () => {
      const onConnect = vi.fn()
      const { state, isConnected } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        onConnect,
      })

      mockWebSocketInstance?.simulateOpen()
      await nextTick()

      expect(state.value).toBe('connected')
      expect(isConnected.value).toBe(true)
      expect(onConnect).toHaveBeenCalled()
    })

    it('should not auto-connect when autoConnect is false', () => {
      const { state } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        autoConnect: false,
      })

      expect(state.value).toBe('disconnected')
      expect(mockWebSocketInstance).toBeNull()
    })

    it('should manually connect', () => {
      const { connect, state } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        autoConnect: false,
      })

      expect(state.value).toBe('disconnected')

      connect()

      expect(state.value).toBe('connecting')
      expect(mockWebSocketInstance).not.toBeNull()
    })
  })

  describe('disconnection', () => {
    it('should disconnect from WebSocket server', () => {
      const onDisconnect = vi.fn()
      const { disconnect, state } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        onDisconnect,
        reconnect: false,
      })

      mockWebSocketInstance?.simulateOpen()
      disconnect()

      expect(state.value).toBe('disconnected')
    })

    it('should call onDisconnect callback', () => {
      const onDisconnect = vi.fn()
      const { state } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        onDisconnect,
        reconnect: false,
      })

      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.close()

      expect(onDisconnect).toHaveBeenCalled()
    })
  })

  describe('messaging', () => {
    it('should send messages', () => {
      const { send } = useWebSocket({
        url: 'ws://localhost:8080/ws',
      })

      mockWebSocketInstance?.simulateOpen()
      const result = send('test-type', { data: 'test' })

      expect(result).toBe(true)
      expect(mockWebSocketInstance?.send).toHaveBeenCalledWith(
        expect.stringContaining('"type":"test-type"')
      )
    })

    it('should not send when disconnected', () => {
      const { send } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        autoConnect: false,
      })

      const result = send('test-type', { data: 'test' })

      expect(result).toBe(false)
    })

    it('should receive and parse messages', () => {
      const onMessage = vi.fn()
      useWebSocket({
        url: 'ws://localhost:8080/ws',
        onMessage,
      })

      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.simulateMessage({
        type: 'test-type',
        payload: { data: 'test' },
      })

      expect(onMessage).toHaveBeenCalledWith(
        expect.objectContaining({
          type: 'test-type',
          payload: { data: 'test' },
        })
      )
    })

    it('should update lastMessage on receive', () => {
      const { lastMessage } = useWebSocket({
        url: 'ws://localhost:8080/ws',
      })

      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.simulateMessage({
        type: 'update',
        payload: { id: 1 },
      })

      expect(lastMessage.value).toEqual(
        expect.objectContaining({
          type: 'update',
          payload: { id: 1 },
        })
      )
    })
  })

  describe('subscriptions', () => {
    it('should subscribe to specific message types', () => {
      const handler = vi.fn()
      const { subscribe } = useWebSocket({
        url: 'ws://localhost:8080/ws',
      })

      subscribe('loot-awarded', handler)
      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.simulateMessage({
        type: 'loot-awarded',
        payload: { itemId: 123 },
      })

      expect(handler).toHaveBeenCalledWith({ itemId: 123 })
    })

    it('should not call handler for different message types', () => {
      const handler = vi.fn()
      const { subscribe } = useWebSocket({
        url: 'ws://localhost:8080/ws',
      })

      subscribe('loot-awarded', handler)
      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.simulateMessage({
        type: 'other-type',
        payload: { data: 'test' },
      })

      expect(handler).not.toHaveBeenCalled()
    })

    it('should support wildcard subscriptions', () => {
      const handler = vi.fn()
      const { subscribe } = useWebSocket({
        url: 'ws://localhost:8080/ws',
      })

      subscribe('*', handler)
      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.simulateMessage({
        type: 'any-type',
        payload: { data: 'test' },
      })

      expect(handler).toHaveBeenCalled()
    })

    it('should unsubscribe correctly', () => {
      const handler = vi.fn()
      const { subscribe } = useWebSocket({
        url: 'ws://localhost:8080/ws',
      })

      const unsubscribe = subscribe('test', handler)
      unsubscribe()

      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.simulateMessage({
        type: 'test',
        payload: {},
      })

      expect(handler).not.toHaveBeenCalled()
    })
  })

  describe('reconnection', () => {
    it('should attempt to reconnect on disconnect', () => {
      const { reconnectAttempts } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        reconnect: true,
        reconnectDelay: 1000,
      })

      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.close()

      expect(reconnectAttempts.value).toBe(1)

      vi.advanceTimersByTime(1000)

      // Should have created a new connection attempt
      expect(mockWebSocketInstance).not.toBeNull()
    })

    it('should use exponential backoff for reconnection', () => {
      const { reconnectAttempts } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        reconnect: true,
        reconnectDelay: 1000,
      })

      // First disconnect
      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.close()
      expect(reconnectAttempts.value).toBe(1)

      // Wait for first reconnect (1000ms)
      vi.advanceTimersByTime(1000)
      mockWebSocketInstance?.close()
      expect(reconnectAttempts.value).toBe(2)

      // Wait for second reconnect (2000ms)
      vi.advanceTimersByTime(2000)
      mockWebSocketInstance?.close()
      expect(reconnectAttempts.value).toBe(3)
    })

    it('should not reconnect when reconnect is false', () => {
      useWebSocket({
        url: 'ws://localhost:8080/ws',
        reconnect: false,
      })

      mockWebSocketInstance?.simulateOpen()
      const firstInstance = mockWebSocketInstance
      mockWebSocketInstance?.close()

      vi.advanceTimersByTime(10000)

      // Should still be the same closed instance
      expect(mockWebSocketInstance).toBe(firstInstance)
    })

    it('should stop reconnecting after max attempts', () => {
      const { reconnectAttempts } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        reconnect: true,
        reconnectDelay: 100,
        maxReconnectAttempts: 3,
      })

      // Trigger 3 disconnections
      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.close()
      vi.advanceTimersByTime(100)
      mockWebSocketInstance?.close()
      vi.advanceTimersByTime(200)
      mockWebSocketInstance?.close()
      vi.advanceTimersByTime(400)

      expect(reconnectAttempts.value).toBe(3)

      // Should not attempt more reconnections
      mockWebSocketInstance?.close()
      vi.advanceTimersByTime(10000)
      expect(reconnectAttempts.value).toBe(3)
    })

    it('should reset reconnect attempts on successful connection', () => {
      const { reconnectAttempts } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        reconnect: true,
        reconnectDelay: 100,
      })

      mockWebSocketInstance?.simulateOpen()
      mockWebSocketInstance?.close()
      vi.advanceTimersByTime(100)
      expect(reconnectAttempts.value).toBe(1)

      mockWebSocketInstance?.simulateOpen()
      expect(reconnectAttempts.value).toBe(0)
    })
  })

  describe('error handling', () => {
    it('should update state to error on WebSocket error', () => {
      const onError = vi.fn()
      const { state } = useWebSocket({
        url: 'ws://localhost:8080/ws',
        onError,
      })

      mockWebSocketInstance?.simulateError()

      expect(state.value).toBe('error')
      expect(onError).toHaveBeenCalled()
    })
  })
})
