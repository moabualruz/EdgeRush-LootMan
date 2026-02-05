import { api } from './client'

export const authApi = {
  /**
   * Request a password reset email
   */
  async forgotPassword(email: string): Promise<void> {
    await api.post('/v1/auth/forgot-password', { email })
  },

  /**
   * Reset password using a token from email
   */
  async resetPassword(token: string, newPassword: string): Promise<void> {
    await api.post('/v1/auth/reset-password', { token, newPassword })
  },
}
