export function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

export function formatDateTime(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function formatRelativeTime(dateString: string): string {
  if (!dateString) return 'Never'
  const date = new Date(dateString)
  if (isNaN(date.getTime())) return 'Unknown'
  const now = new Date()
  const diffMs = date.getTime() - now.getTime()
  const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24))

  if (diffDays < 0) {
    return 'Expired'
  }

  if (diffDays === 0) {
    return 'Today'
  }

  if (diffDays === 1) {
    return 'Tomorrow'
  }

  if (diffDays < 7) {
    return `${diffDays} days`
  }

  if (diffDays < 30) {
    const weeks = Math.floor(diffDays / 7)
    return `${weeks} week${weeks > 1 ? 's' : ''}`
  }

  const months = Math.floor(diffDays / 30)
  return `${months} month${months > 1 ? 's' : ''}`
}

export function isExpired(dateString: string): boolean {
  return new Date(dateString) < new Date()
}
