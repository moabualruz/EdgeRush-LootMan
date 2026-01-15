import { api } from './client'

export interface SubmitSimulationRequest {
  characterRealm: string
  characterClass: string
  characterSpec: string
  characterLevel?: number
  characterRace?: string
  iterations?: number
  fightLengthSeconds?: number
}

export interface SimulationRequestDto {
  id: number | null
  characterName: string
  characterRealm: string
  guildId: string
  status: SimulationStatus
  submittedAt: string
  completedAt: string | null
  errorMessage: string | null
  resultCount: number
}

export interface SimulationResultDto {
  itemId: number
  itemName: string
  slot: string
  dpsGain: number
  percentGain: number
  isUpgrade: boolean
  normalizedValue: number
  simulatedAt: string
}

export interface SimulationResultsResponse {
  guildId: string
  characterName: string
  characterRealm: string
  results: SimulationResultDto[]
  retrievedAt: string
}

export interface ExecutionSummaryResponse {
  executedCount: number
  executedAt: string
}

export interface SimulationStatusResponse {
  status: string
  pendingSimulations: number
  endpoints: Record<string, string>
}

export type SimulationStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'

export const simulationApi = {
  /**
   * Submit a new simulation request for a character.
   */
  async submitSimulation(
    guildId: string,
    characterName: string,
    request: SubmitSimulationRequest
  ): Promise<SimulationRequestDto> {
    const response = await api.post<SimulationRequestDto>(
      `/api/v1/simulation/guilds/${guildId}/characters/${characterName}`,
      request
    )
    return response.data
  },

  /**
   * Get the status of a specific simulation request.
   */
  async getSimulationStatus(requestId: number): Promise<SimulationRequestDto> {
    const response = await api.get<SimulationRequestDto>(
      `/api/v1/simulation/requests/${requestId}`
    )
    return response.data
  },

  /**
   * Get simulation results for a character.
   */
  async getSimulationResults(
    guildId: string,
    characterName: string,
    characterRealm: string
  ): Promise<SimulationResultsResponse> {
    const response = await api.get<SimulationResultsResponse>(
      `/api/v1/simulation/guilds/${guildId}/characters/${characterName}/realms/${characterRealm}/results`
    )
    return response.data
  },

  /**
   * Get pending simulations for a guild.
   */
  async getPendingSimulations(guildId: string): Promise<SimulationRequestDto[]> {
    const response = await api.get<SimulationRequestDto[]>(
      `/api/v1/simulation/guilds/${guildId}/pending`
    )
    return response.data
  },

  /**
   * Trigger execution of all pending simulations.
   */
  async executePendingSimulations(): Promise<ExecutionSummaryResponse> {
    const response = await api.post<ExecutionSummaryResponse>(
      '/api/v1/simulation/execute-pending'
    )
    return response.data
  },

  /**
   * Get simulation service status.
   */
  async getServiceStatus(): Promise<SimulationStatusResponse> {
    const response = await api.get<SimulationStatusResponse>(
      '/api/v1/simulation/status'
    )
    return response.data
  },
}
