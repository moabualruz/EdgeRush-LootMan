import { api } from './client'

// --- NEW TYPES (Phase 3) ---
export const RecruitmentStatus = {
  PENDING: 'PENDING',
  SCREENED: 'SCREENED',
  INTERVIEW: 'INTERVIEW',
  TRIAL: 'TRIAL',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
  ARCHIVED: 'ARCHIVED',
  // Legacy mappings
  UNDER_REVIEW: 'SCREENED',
  APPROVED: 'ACCEPTED',
  WITHDRAWN: 'ARCHIVED'
} as const

export type RecruitmentStatus = typeof RecruitmentStatus[keyof typeof RecruitmentStatus]

export interface RecruitmentCharacter {
  name: string
  realm: string
  characterClass: string
  specialization: string
  itemLevel: number
  scores: {
    raiderIoScore: number | null
    bestParseAverage: number | null
  }
}

export interface CreateApplicationCommand {
  battleNetId: string
  discordId: string
  email: string
  characterName: string
  characterRealm: string
  characterClass: string
  specialization: string
  itemLevel: number
  raiderIoScore: number | null
  bestParseAverage: number | null
  age: number
  location: string
  timezone: string
  raidDaysAvailable: string[]
  previousGuilds: string
  reasonForLeaving: string
  whyThisGuild: string
}

export interface RecruitmentApplication {
  id: string
  guildId: string
  applicant: {
    battleNetId: string
    discordId: string
    email: string
    character: RecruitmentCharacter
  }
  details: {
    age: number
    location: string
    timezone: string
    raidDaysAvailable: string[]
    previousGuilds: string
    reasonForLeaving: string
    whyThisGuild: string
  }
  status: RecruitmentStatus
  review: {
    reviewedBy: string | null
    reviewedAt: string | null
  } | null
  timestamps: {
    createdAt: string
    updatedAt: string
  }
  comments?: RecruitmentComment[]
}

export interface RecruitmentComment {
  id: number
  applicationId: string
  authorId: number
  text: string
  createdAt: string
}


// --- LEGACY TYPES (For backward compatibility) ---
export type ApplicationStatus = string // Simplified, compatible with RecruitmentStatus keys ideally

export interface CharacterLookupResponse {
  name: string
  realm: string
  region: string
  characterClass: string
  specialization: string | null
  role: string | null
  itemLevel: number | null
  raiderIOScore: number | null
  profileUrl: string
}

export interface CharacterFullLookupResponse extends CharacterLookupResponse {
  bestParseAverage: number | null
  medianParseAverage: number | null
}

export interface SubmitApplicationRequest {
  battleNetId: string
  discordId: string
  email: string
  characterName: string
  characterRealm: string
  characterClass: string
  specialization: string
  itemLevel: number
  raiderIOScore: number | null
  bestParseAverage: number | null
  age: number
  location: string
  timezone: string
  raidDaysAvailable: string[]
  previousGuilds: string
  reasonForLeaving: string
  whyThisGuild: string
}

export interface ApplicationResponse extends RecruitmentApplication {
    // Map fields if needed, for now extending works if fields overlap enough or we cast
    // Legacy fields that might be missing in new model:
    // none critical?
}

export interface GetApplicationsOptions {
  status?: string // ApplicationStatus
  offset?: number
  limit?: number
}


// --- API CLIENT ---
export const recruitmentApi = {
    // --- NEW METHODS ---
  async searchCandidate(name: string, realm: string, region: string = 'eu'): Promise<RecruitmentCharacter> {
    const response = await api.get<RecruitmentCharacter>('/recruitment/candidates/search', {
      params: { name, realm, region }
    })
    return response.data
  },

  async createApplication(guildId: string, command: CreateApplicationCommand): Promise<RecruitmentApplication> {
    const response = await api.post<RecruitmentApplication>('/recruitment/applications', command, {
      params: { guildId }
    })
    return response.data
  },

  async getApplications(guildId: string, status?: RecruitmentStatus): Promise<RecruitmentApplication[]> {
    try {
      const response = await api.get<RecruitmentApplication[]>('/recruitment/applications', {
        params: { guildId, status }
      })
      return response.data
    } catch (error: any) {
      if (error.response && error.response.status === 404) {
        return []
      }
      throw error
    }
  },

  async getApplication(id: string): Promise<RecruitmentApplication> {
    const response = await api.get<RecruitmentApplication>(`/recruitment/applications/${id}`)
    return response.data
  },

  async updateStatus(id: string, status: RecruitmentStatus, reviewer: string): Promise<RecruitmentApplication> {
    const response = await api.put<RecruitmentApplication>(`/recruitment/applications/${id}/status`, null, {
      params: { status, reviewer }
    })
    return response.data
  },

  async addComment(id: string, authorId: number, text: string): Promise<RecruitmentComment> {
    const response = await api.post<RecruitmentComment>(`/recruitment/applications/${id}/comments`, text, {
        headers: { 'Content-Type': 'application/json' },
        params: { authorId }
    })
    return response.data
  },


  // --- LEGACY ADAPTER METHODS ---

  async lookupCharacterFull(region: string, realm: string, name: string): Promise<CharacterFullLookupResponse | null> {
      try {
        const candidate = await this.searchCandidate(name, realm, region);
        return {
            name: candidate.name,
            realm: candidate.realm,
            region: region,
            characterClass: candidate.characterClass, // Should map class name normalization if needed
            specialization: candidate.specialization,
            role: null,
            itemLevel: candidate.itemLevel,
            raiderIOScore: candidate.scores.raiderIoScore,
            bestParseAverage: candidate.scores.bestParseAverage,
            medianParseAverage: null,
            profileUrl: ""
        };
      } catch (e) {
          return null;
      }
  },

  async lookupCharacter(region: string, realm: string, name: string): Promise<CharacterLookupResponse | null> {
      try {
        const full = await this.lookupCharacterFull(region, realm, name);
        if (!full) return null;
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const { bestParseAverage, medianParseAverage, ...rest } = full;
        return rest;
      } catch { return null; }
  },

  async submitApplication(guildId: string, request: SubmitApplicationRequest): Promise<ApplicationResponse> {
       const command: CreateApplicationCommand = {
          battleNetId: request.battleNetId,
          discordId: request.discordId,
          email: request.email,
          characterName: request.characterName,
          characterRealm: request.characterRealm,
          characterClass: request.characterClass,
          specialization: request.specialization,
          itemLevel: request.itemLevel,
          raiderIoScore: request.raiderIOScore,
          bestParseAverage: request.bestParseAverage,
          age: request.age,
          location: request.location,
          timezone: request.timezone,
          raidDaysAvailable: request.raidDaysAvailable,
          previousGuilds: request.previousGuilds,
          reasonForLeaving: request.reasonForLeaving,
          whyThisGuild: request.whyThisGuild
       };
       return this.createApplication(guildId, command) as Promise<ApplicationResponse>;
  },

  async getApplicationsByGuild(guildId: string, options: GetApplicationsOptions = {}): Promise<ApplicationResponse[]> {
        // Map legacy status to new RecruitmentStatus if possible
        const status = options.status as RecruitmentStatus | undefined;
        return this.getApplications(guildId, status) as Promise<ApplicationResponse[]>;
  },
  
  // Stubs for other legacy methods to pass build
  async getPendingApplications(guildId: string): Promise<ApplicationResponse[]> {
      return this.getApplications(guildId, RecruitmentStatus.PENDING) as Promise<ApplicationResponse[]>;
  },

  async getApplicationById(applicationId: string): Promise<ApplicationResponse | null> {
      try {
        return (await this.getApplication(applicationId)) as ApplicationResponse;
      } catch { return null; }
  },

  async startReview(applicationId: string, reviewerId: string): Promise<ApplicationResponse> {
       return this.updateStatus(applicationId, RecruitmentStatus.INTERVIEW, reviewerId) as Promise<ApplicationResponse>;
  },

  async approveApplication(applicationId: string, reviewerId: string): Promise<ApplicationResponse> {
       return this.updateStatus(applicationId, RecruitmentStatus.ACCEPTED, reviewerId) as Promise<ApplicationResponse>;
  },

  async rejectApplication(applicationId: string, reviewerId: string): Promise<ApplicationResponse> {
       return this.updateStatus(applicationId, RecruitmentStatus.REJECTED, reviewerId) as Promise<ApplicationResponse>;
  },

  async withdrawApplication(applicationId: string): Promise<ApplicationResponse> {
      // Assuming 'System' or current user checks happen in backend
       return this.updateStatus(applicationId, RecruitmentStatus.ARCHIVED, "System") as Promise<ApplicationResponse>;
  },

  async countApplications(guildId: string, status?: string): Promise<number> {
       const apps = await this.getApplications(guildId, status as RecruitmentStatus);
       return apps.length;
  }
}
