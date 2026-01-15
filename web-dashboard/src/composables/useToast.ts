/**
 * Toast notification composable.
 *
 * Provides a simple, reactive toast notification system with:
 * - Multiple toast types (success, error, warning, info)
 * - Auto-dismiss with configurable duration
 * - Manual dismissal
 * - Queue management with max visible toasts
 */

import { ref, computed, readonly } from 'vue'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface Toast {
  id: string
  type: ToastType
  title: string
  message?: string
  duration: number
  dismissible: boolean
  createdAt: number
}

export interface ToastOptions {
  title: string
  message?: string
  type?: ToastType
  duration?: number
  dismissible?: boolean
}

const DEFAULT_DURATION = 5000
const MAX_VISIBLE_TOASTS = 5

// Global toast state
const toasts = ref<Toast[]>([])
let toastIdCounter = 0

/**
 * Generate unique toast ID.
 */
function generateId(): string {
  return `toast-${++toastIdCounter}-${Date.now()}`
}

/**
 * Add a new toast notification.
 */
function addToast(options: ToastOptions): string {
  const toast: Toast = {
    id: generateId(),
    type: options.type ?? 'info',
    title: options.title,
    message: options.message,
    duration: options.duration ?? DEFAULT_DURATION,
    dismissible: options.dismissible ?? true,
    createdAt: Date.now(),
  }

  // Add to the end of the queue
  toasts.value.push(toast)

  // Remove oldest toasts if we exceed max
  while (toasts.value.length > MAX_VISIBLE_TOASTS) {
    toasts.value.shift()
  }

  // Auto-dismiss after duration
  if (toast.duration > 0) {
    setTimeout(() => {
      dismissToast(toast.id)
    }, toast.duration)
  }

  return toast.id
}

/**
 * Dismiss a toast by ID.
 */
function dismissToast(id: string): void {
  const index = toasts.value.findIndex((t) => t.id === id)
  if (index !== -1) {
    toasts.value.splice(index, 1)
  }
}

/**
 * Dismiss all toasts.
 */
function dismissAll(): void {
  toasts.value = []
}

/**
 * Composable for toast notifications.
 *
 * @example
 * ```vue
 * <script setup>
 * import { useToast } from '@/composables/useToast'
 *
 * const { success, error, toasts } = useToast()
 *
 * function handleSave() {
 *   try {
 *     // ... save logic
 *     success('Saved!', 'Your changes have been saved.')
 *   } catch (e) {
 *     error('Error', 'Failed to save changes.')
 *   }
 * }
 * </script>
 * ```
 */
export function useToast() {
  const visibleToasts = computed(() => toasts.value)

  /**
   * Show a success toast.
   */
  function success(title: string, message?: string, duration?: number): string {
    return addToast({ type: 'success', title, message, duration })
  }

  /**
   * Show an error toast.
   */
  function error(title: string, message?: string, duration?: number): string {
    return addToast({
      type: 'error',
      title,
      message,
      duration: duration ?? 8000, // Errors show longer by default
    })
  }

  /**
   * Show a warning toast.
   */
  function warning(title: string, message?: string, duration?: number): string {
    return addToast({ type: 'warning', title, message, duration })
  }

  /**
   * Show an info toast.
   */
  function info(title: string, message?: string, duration?: number): string {
    return addToast({ type: 'info', title, message, duration })
  }

  /**
   * Show a custom toast with full options.
   */
  function show(options: ToastOptions): string {
    return addToast(options)
  }

  return {
    toasts: readonly(visibleToasts),
    success,
    error,
    warning,
    info,
    show,
    dismiss: dismissToast,
    dismissAll,
  }
}

export default useToast
