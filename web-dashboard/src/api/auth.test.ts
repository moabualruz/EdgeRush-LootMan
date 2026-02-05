import { describe, it, expect, vi, beforeEach } from 'vitest'
import { authApi } from './auth'
import { api } from './client'

// Mock the API client
vi.mock('./client', () => ({
  api: {
    post: vi.fn(),
  },
}))

describe('authApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('forgotPassword', () => {
    it('should call the forgot-password endpoint with email', async () => {
      vi.mocked(api.post).mockResolvedValueOnce({ data: { message: 'Email sent' } })

      await authApi.forgotPassword('user@example.com')

      expect(api.post).toHaveBeenCalledWith('/v1/auth/forgot-password', {
        email: 'user@example.com',
      })
    })

    it('should propagate errors from the API', async () => {
      const error = new Error('User not found')
      vi.mocked(api.post).mockRejectedValueOnce(error)

      await expect(authApi.forgotPassword('unknown@example.com')).rejects.toThrow('User not found')
    })
  })

  describe('resetPassword', () => {
    it('should call the reset-password endpoint with token and new password', async () => {
      vi.mocked(api.post).mockResolvedValueOnce({ data: { message: 'Password reset' } })

      await authApi.resetPassword('valid-token-123', 'newSecurePassword')

      expect(api.post).toHaveBeenCalledWith('/v1/auth/reset-password', {
        token: 'valid-token-123',
        newPassword: 'newSecurePassword',
      })
    })

    it('should propagate errors for invalid or expired tokens', async () => {
      const error = new Error('Token expired')
      vi.mocked(api.post).mockRejectedValueOnce(error)

      await expect(authApi.resetPassword('expired-token', 'password')).rejects.toThrow('Token expired')
    })
  })
})
