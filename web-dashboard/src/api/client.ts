import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'

const BASE_URL = import.meta.env.VITE_API_URL || ''

export const api: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add auth token
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    
    // Check if error is 401 and we haven't already retried
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      
      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (refreshToken) {
           // Call refresh endpoint
           // We use axios directly to avoid interceptor loop, but we need the baseURL
           const response = await axios.post(`${BASE_URL}/v1/auth/refresh`, {
             refreshToken: refreshToken
           })
           
           const { accessToken, refreshToken: newRefreshToken } = response.data
           
           // Update storage
           localStorage.setItem('token', accessToken)
           localStorage.setItem('refreshToken', newRefreshToken)
           
           // Update header for retry
           originalRequest.headers.Authorization = `Bearer ${accessToken}`
           
           // Also update the store if possible, but store is not easily accessible here without circular dep risk
           // The store reads from localStorage on init/actions, but we should try to update it if we can import it.
           // Ideally, we'd import useAuthStore, but pinia might not be ready if client.ts is imported early.
           // Check if we can do this cleanly in Vue 3 + Pinia.
           // Usually it is fine to import the store here as long as app is created.
           // Be cautious. For now, localStorage update is critical.
           // The auth store logic reads local storage on actions, so it might desync slightly until reload/action.
           // Let's just update headers and localStorage.
           
           return api(originalRequest)
        }
      } catch (refreshError) {
        // Refresh failed, fallback to logout
        console.error("Token refresh failed", refreshError)
      }
    }
    
    // If 401 and retry failed or no refresh token -> logout
    if (error.response?.status === 401) {
      // Don't global logout if the 401 came from the login endpoint itself
      // This allows the login form to show "Invalid Credentials" instead of reloading
      if (!originalRequest.url?.includes('/auth/login')) {
         localStorage.removeItem('token')
         localStorage.removeItem('refreshToken')
         // Store intended destination for post-login redirect
         const currentPath = window.location.pathname + window.location.search
         if (currentPath !== '/login') {
           localStorage.setItem('redirectAfterLogin', currentPath)
         }
         window.location.href = '/login'
      }
    }
    
    return Promise.reject(error)
  }
)
