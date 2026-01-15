# Application Portal Specification

## Overview

This document specifies the guild application and recruitment portal for EdgeRush LootMan. The portal streamlines the application process by automatically fetching character data from external APIs, eliminating manual URL copying and providing officers with rich data for decision-making.

## Research: Reference Application Form

Analysis of the Department of Death guild application form and similar guild applications:

### Common Fields (Keep)

| Field | Type | Auto-Fill Source |
|-------|------|-----------------|
| Name (real/preferred) | Text | Discord OAuth |
| Age | Number | Manual |
| Location/Timezone | Select | GeoIP suggestion |
| Character Name | Text | Battle.net OAuth |
| Realm | Select | Battle.net OAuth |
| Class/Spec | Display | Blizzard API |
| Item Level | Display | Blizzard API |
| M+ Score | Display | Raider.IO API |
| Warcraft Logs URL | Text (validated) | Warcraft Logs API |
| Raider.IO URL | Text (validated) | Auto-constructed |
| Raid Availability | Multi-select | Manual |
| Alt Characters | Multi-entry | Blizzard API (alts) |
| Previous Guild | Text | Manual |
| Reason for Leaving | Textarea | Manual |
| Why This Guild | Textarea | Manual |
| Hardware/Internet | Boolean | Manual |
| Discord ID | Text | Discord OAuth |

### Fields to Remove (Auto-Fetched Instead)

| Old Field | New Approach |
|-----------|--------------|
| Armory Link | Auto-fetch via Battle.net OAuth |
| Raider.IO Link | Auto-construct from character/realm |
| Logs Link | Still required, but validated and data fetched |
| Screenshots of UI | Optional attachment |

### Fields to Add (Enhanced)

| New Field | Purpose |
|-----------|---------|
| Best Mythic Boss | Dropdown from WCL data |
| Progression History | Auto from Raider.IO |
| Average Parse | Calculated from WCL |
| Death Rate | Calculated from WCL |
| Consumable Usage | Calculated from WCL |

---

## Application Form Design

### Step 1: Authentication

```
┌─────────────────────────────────────────────────────────────┐
│                  Apply to EdgeRush                          │
│                                                             │
│  To start your application, please connect your accounts:   │
│                                                             │
│  ┌───────────────────┐  ┌───────────────────┐              │
│  │  🎮 Battle.net    │  │  💬 Discord        │              │
│  │    Connected ✓    │  │    Connect →       │              │
│  └───────────────────┘  └───────────────────┘              │
│                                                             │
│  Battle.net provides character data automatically.          │
│  Discord is used for communication during review.           │
│                                                             │
│                    [Continue →]                             │
└─────────────────────────────────────────────────────────────┘
```

### Step 2: Character Selection

```
┌─────────────────────────────────────────────────────────────┐
│              Select Your Main Character                      │
│                                                             │
│  We found these characters on your Battle.net account:      │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ ◉ Playername - Twisting Nether                       │   │
│  │   Frost Mage • 489 ilvl • 3200 M+ Score              │   │
│  │   [View Armory] [View Logs] [View Raider.IO]         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ ○ Altname - Twisting Nether                          │   │
│  │   Blood Death Knight • 475 ilvl • 2800 M+ Score      │   │
│  │   [View Armory] [View Logs] [View Raider.IO]         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Character not listed? [Enter manually]                     │
│                                                             │
│                    [← Back] [Continue →]                    │
└─────────────────────────────────────────────────────────────┘
```

### Step 3: Verify Performance Data

```
┌─────────────────────────────────────────────────────────────┐
│              Verify Your Performance Data                    │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 📊 Warcraft Logs                                     │   │
│  │                                                       │   │
│  │ We'll fetch your logs automatically. If you have     │   │
│  │ private logs, please provide the URL:                │   │
│  │                                                       │   │
│  │ [https://www.warcraftlogs.com/character/...]         │   │
│  │                                                       │   │
│  │ ✓ Found 15 logs from current tier                    │   │
│  │ ✓ Average parse: 78th percentile                     │   │
│  │ ✓ Best kill: Mythic Fyrakk (89%)                     │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 🏃 Raider.IO                                         │   │
│  │                                                       │   │
│  │ ✓ M+ Score: 3,247                                    │   │
│  │ ✓ Best Keys: +25 average                             │   │
│  │ ✓ Season Best: +28 Throne of the Tides               │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│                    [← Back] [Continue →]                    │
└─────────────────────────────────────────────────────────────┘
```

### Step 4: About You

```
┌─────────────────────────────────────────────────────────────┐
│                     About You                                │
│                                                             │
│  Name (how you'd like to be called):                        │
│  [John                                             ]        │
│                                                             │
│  Age: *                                                     │
│  [25  ]                                                     │
│                                                             │
│  Location/Timezone: *                                       │
│  [Europe - CET (UTC+1)              ▼]                      │
│                                                             │
│  Raid Availability: *                                       │
│  Our raids are Wednesday, Sunday, Monday 22:00-01:00 CET    │
│  ☑ Wednesday     ☑ Sunday     ☑ Monday                     │
│                                                             │
│  Stable internet/hardware for progression raiding? *        │
│  ◉ Yes     ○ No                                            │
│                                                             │
│  Do you have any alts you'd be willing to swap to?         │
│  ☑ Yes, I have geared alts                                 │
│  Selected: Altname (Blood DK), Altname2 (Holy Paladin)     │
│                                                             │
│                    [← Back] [Continue →]                    │
└─────────────────────────────────────────────────────────────┘
```

### Step 5: Guild History

```
┌─────────────────────────────────────────────────────────────┐
│                   Guild History                              │
│                                                             │
│  Current/Previous Guild: *                                  │
│  [Example Guild - Stormrage                        ]        │
│                                                             │
│  Reason for leaving: *                                      │
│  [The guild disbanded after our GM quit. I'm looking   ]    │
│  [for a stable progression guild with similar raid     ]    │
│  [times. I was with them for 2 years and achieved CE   ]    │
│  [in 3 tiers.                                          ]    │
│                                                             │
│  Raid progression history: (auto-filled from Raider.IO)     │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ • Vault of the Incarnates: 8/8M (CE)                 │   │
│  │ • Aberrus: 9/9M (CE)                                 │   │
│  │ • Amirdrassil: 7/9M (current)                        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Logs from previous guilds (optional):                      │
│  [+ Add additional logs URL]                                │
│                                                             │
│                    [← Back] [Continue →]                    │
└─────────────────────────────────────────────────────────────┘
```

### Step 6: Motivation

```
┌─────────────────────────────────────────────────────────────┐
│                    Motivation                                │
│                                                             │
│  Why do you want to join EdgeRush? *                        │
│  [I've followed EdgeRush's progress for a while and    ]    │
│  [admire the consistent progression without extreme     ]    │
│  [hours. I'm looking for a guild that values both       ]    │
│  [performance and a healthy raid environment. Your      ]    │
│  [FLPS system also appeals to me as it removes drama    ]    │
│  [from loot distribution.                               ]    │
│                                                             │
│  What do you bring to a raid team? *                        │
│  [I'm a consistent performer who comes prepared with    ]    │
│  [consumables and knowledge. I regularly sim my gear    ]    │
│  [and stay current on theorycrafting. I'm also happy    ]    │
│  [to play different specs if needed for progression.    ]    │
│                                                             │
│  What are your goals for this tier? *                       │
│  [Achieve Cutting Edge and improve my overall play.     ]    │
│  [I want to be a reliable raider who contributes to     ]    │
│  [progression without causing issues.                   ]    │
│                                                             │
│                    [← Back] [Continue →]                    │
└─────────────────────────────────────────────────────────────┘
```

### Step 7: Review & Submit

```
┌─────────────────────────────────────────────────────────────┐
│                 Review Your Application                      │
│                                                             │
│  Character: Playername - Twisting Nether (Frost Mage)       │
│  Item Level: 489                                            │
│  M+ Score: 3,247                                            │
│  Average Parse: 78th percentile                             │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ ⚠️  Some things we noticed:                          │   │
│  │                                                       │   │
│  │ • Your attendance in previous guild was 89%          │   │
│  │   (We require 95%+ attendance)                        │   │
│  │                                                       │   │
│  │ • Your average deaths per fight is 1.2               │   │
│  │   (Slightly above our average of 0.8)                │   │
│  │                                                       │   │
│  │ These are not dealbreakers, but may come up in       │   │
│  │ your trial if accepted.                               │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ☑ I understand that trial period is 3 weeks minimum       │
│  ☑ I confirm all information provided is accurate          │
│                                                             │
│                    [← Back] [Submit Application]            │
└─────────────────────────────────────────────────────────────┘
```

---

## Officer Review Interface

### Application List View

```
┌─────────────────────────────────────────────────────────────┐
│  Applications                              [Filter ▼] [🔍]  │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 🔵 NEW  Playername (Frost Mage)         Jan 15, 2025 │   │
│  │ 489 ilvl • 3247 M+ • 78% avg parse                    │   │
│  │ "Looking for stable CE guild..."                      │   │
│  │ [View] [Approve] [Decline] [Request Info]             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ ⏳ PENDING  Othername (Holy Paladin)    Jan 14, 2025 │   │
│  │ 482 ilvl • 2900 M+ • 71% avg parse                    │   │
│  │ Waiting for: Additional logs                          │   │
│  │ [View] [Approve] [Decline] [Request Info]             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ ✅ APPROVED  Goodplayer (Fury Warrior)  Jan 12, 2025 │   │
│  │ Trial started: Jan 13, 2025                           │   │
│  │ Progress: Week 1/3 • 100% attendance • 0.7 deaths/pull│   │
│  │ [View] [End Trial] [Extend] [Promote]                 │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ ❌ DECLINED  Badfit (Arms Warrior)      Jan 10, 2025 │   │
│  │ Reason: Low parse percentiles, schedule conflict      │   │
│  │ [View] [Reopen]                                       │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Application Detail View

```
┌─────────────────────────────────────────────────────────────┐
│  Application: Playername                    [Close] [Print] │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────┐  Playername - Twisting Nether             │
│  │             │  Frost Mage (489 ilvl)                    │
│  │   [Avatar]  │  M+ Score: 3,247                          │
│  │             │  Applied: Jan 15, 2025                    │
│  └─────────────┘                                           │
│                                                             │
│  Quick Actions:                                             │
│  [✓ Approve] [✗ Decline] [📝 Request Info] [💬 Message]   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  📊 Performance Analysis                                    │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Parse Percentiles (Current Tier - Mythic)            │   │
│  │                                                       │   │
│  │ Fyrakk:      ████████████████████░░░░░ 85%           │   │
│  │ Tindral:     ███████████████████░░░░░░ 82%           │   │
│  │ Smolderon:   █████████████████░░░░░░░░ 75%           │   │
│  │ Volcoross:   ████████████████████░░░░░ 88%           │   │
│  │ Council:     ██████████████░░░░░░░░░░░ 68%           │   │
│  │ Larodar:     █████████████████░░░░░░░░ 76%           │   │
│  │ Nymue:       ████████████████████░░░░░ 84%           │   │
│  │                                                       │   │
│  │ Average: 78%   Median: 79%   Best: 88%               │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ ⚠️ Red Flags                                          │   │
│  │                                                       │   │
│  │ • Deaths per pull: 1.2 (guild avg: 0.8)              │   │
│  │   └─ Primary causes: Searing Aftermath (4),          │   │
│  │      Shadowflame (2), Falling (1)                     │   │
│  │                                                       │   │
│  │ • Consumable usage: 94% (expected: 100%)             │   │
│  │   └─ Missing food buff on 3 of 50 pulls              │   │
│  │                                                       │   │
│  │ ✓ No guild hopping (1 guild in 2 years)              │   │
│  │ ✓ Consistent raid attendance in previous guild        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  📝 Application Responses                                   │
│                                                             │
│  Why do you want to join EdgeRush?                          │
│  "I've followed EdgeRush's progress for a while and        │
│   admire the consistent progression without extreme         │
│   hours..."                                                 │
│   [Expand]                                                  │
│                                                             │
│  What do you bring to a raid team?                          │
│  "I'm a consistent performer who comes prepared..."        │
│   [Expand]                                                  │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  💬 Officer Notes (Private)                                 │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ OfficerName (Jan 15): Looks solid, deaths might be   │   │
│  │ a concern but otherwise good fit for our roster.      │   │
│  │                                                       │   │
│  │ OtherOfficer (Jan 15): Agreed, let's trial.          │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  Add note: [                                         ] [+]  │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  📋 External Links                                          │
│                                                             │
│  [Warcraft Logs] [Raider.IO] [Armory] [WoWProgress]        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Trial Dashboard

### Trial Overview

```
┌─────────────────────────────────────────────────────────────┐
│  Trials                                    Active: 3 / 5    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Playername (Frost Mage)                              │   │
│  │ Trial Week: 2 of 3   ████████████░░░░░░ 67%          │   │
│  │                                                       │   │
│  │ ┌───────────────────────────────────────────────┐   │   │
│  │ │ Metric         Target   Actual   Status       │   │   │
│  │ ├───────────────────────────────────────────────┤   │   │
│  │ │ Attendance     95%      100%     ✅ Pass      │   │   │
│  │ │ Avg Parse      70%      78%      ✅ Pass      │   │   │
│  │ │ Deaths/Pull    <1.0     0.8      ✅ Pass      │   │   │
│  │ │ Mechanics      90%      95%      ✅ Pass      │   │   │
│  │ │ Consumables    100%     100%     ✅ Pass      │   │   │
│  │ └───────────────────────────────────────────────┘   │   │
│  │                                                       │   │
│  │ 📊 Trend: Improving  📝 Notes: 2   [View Details]    │   │
│  │                                                       │   │
│  │ [Promote to Raider] [Extend Trial] [End Trial]        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Othername (Restoration Shaman)                       │   │
│  │ Trial Week: 1 of 3   ████░░░░░░░░░░░░░░ 33%          │   │
│  │                                                       │   │
│  │ ┌───────────────────────────────────────────────┐   │   │
│  │ │ Metric         Target   Actual   Status       │   │   │
│  │ ├───────────────────────────────────────────────┤   │   │
│  │ │ Attendance     95%      100%     ✅ Pass      │   │   │
│  │ │ Healing HPS    Guild%   105%     ✅ Pass      │   │   │
│  │ │ Deaths/Pull    <1.0     1.3      ⚠️ Warning   │   │   │
│  │ │ Dispels        Avg+     120%     ✅ Pass      │   │   │
│  │ └───────────────────────────────────────────────┘   │   │
│  │                                                       │   │
│  │ ⚠️ Death rate needs monitoring                        │   │
│  │                                                       │   │
│  │ [Promote to Raider] [Extend Trial] [End Trial]        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Data Models

### Application Entity

```kotlin
@Table("applications")
data class ApplicationEntity(
    @Id val id: Long? = null,
    val guildId: String,
    val status: ApplicationStatus,  // PENDING, APPROVED, DECLINED, WITHDRAWN

    // OAuth References
    val discordUserId: String,
    val battleNetId: String?,

    // Character Info (fetched from APIs)
    val characterName: String,
    val realm: String,
    val region: String,
    val characterClass: String,
    val specialization: String,
    val itemLevel: Int,
    val mythicPlusScore: Int?,

    // Performance Data (JSON from WCL/Raider.IO)
    @Column("performance_data")
    val performanceData: String,  // JSON

    // Form Responses
    val name: String,
    val age: Int,
    val timezone: String,
    val raidAvailability: String,  // JSON array
    val previousGuild: String,
    val reasonForLeaving: String,
    val whyThisGuild: String,
    val whatYouBring: String,
    val goals: String,
    val stableInternet: Boolean,

    // Alt characters (JSON array)
    val altCharacters: String?,

    // Metadata
    val submittedAt: Instant,
    val reviewedAt: Instant?,
    val reviewedBy: Long?,
    val declineReason: String?
)

enum class ApplicationStatus {
    PENDING,
    APPROVED,
    DECLINED,
    WITHDRAWN,
    INFO_REQUESTED
}
```

### Trial Entity

```kotlin
@Table("trials")
data class TrialEntity(
    @Id val id: Long? = null,
    val applicationId: Long,
    val raiderId: Long,
    val guildId: String,
    val status: TrialStatus,  // ACTIVE, PASSED, FAILED, EXTENDED

    val startDate: LocalDate,
    val endDate: LocalDate,
    val extensionCount: Int = 0,

    // Metrics tracked during trial
    val attendancePercent: Double?,
    val averageParse: Double?,
    val deathsPerPull: Double?,
    val mechanicsScore: Double?,

    // Resolution
    val resolvedAt: Instant?,
    val resolvedBy: Long?,
    val resolution: String?  // Notes on why passed/failed
)

enum class TrialStatus {
    ACTIVE,
    PASSED,
    FAILED,
    EXTENDED
}
```

### Application Note Entity

```kotlin
@Table("application_notes")
data class ApplicationNoteEntity(
    @Id val id: Long? = null,
    val applicationId: Long,
    val authorId: Long,
    val content: String,
    val createdAt: Instant,
    val isPrivate: Boolean = true
)
```

---

## API Endpoints

```kotlin
// ApplicationController.kt
@RestController
@RequestMapping("/api/applications")
class ApplicationController(
    private val applicationService: ApplicationService
) {
    // Public endpoints
    @PostMapping
    fun submitApplication(
        @RequestBody request: ApplicationSubmitRequest,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<ApplicationResponse>

    @GetMapping("/me")
    fun getMyApplication(
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<ApplicationResponse>

    // Officer endpoints
    @GetMapping
    @PreAuthorize("hasRole('OFFICER')")
    fun listApplications(
        @RequestParam status: ApplicationStatus?,
        pageable: Pageable
    ): ResponseEntity<Page<ApplicationSummaryResponse>>

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OFFICER')")
    fun getApplication(
        @PathVariable id: Long
    ): ResponseEntity<ApplicationDetailResponse>

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('OFFICER')")
    fun approveApplication(
        @PathVariable id: Long,
        @RequestBody request: ApproveRequest
    ): ResponseEntity<ApplicationResponse>

    @PostMapping("/{id}/decline")
    @PreAuthorize("hasRole('OFFICER')")
    fun declineApplication(
        @PathVariable id: Long,
        @RequestBody request: DeclineRequest
    ): ResponseEntity<ApplicationResponse>

    @PostMapping("/{id}/request-info")
    @PreAuthorize("hasRole('OFFICER')")
    fun requestMoreInfo(
        @PathVariable id: Long,
        @RequestBody request: RequestInfoRequest
    ): ResponseEntity<ApplicationResponse>

    @PostMapping("/{id}/notes")
    @PreAuthorize("hasRole('OFFICER')")
    fun addNote(
        @PathVariable id: Long,
        @RequestBody request: AddNoteRequest
    ): ResponseEntity<ApplicationNoteResponse>
}

// TrialController.kt
@RestController
@RequestMapping("/api/trials")
class TrialController(
    private val trialService: TrialService
) {
    @GetMapping
    @PreAuthorize("hasRole('OFFICER')")
    fun listTrials(
        @RequestParam status: TrialStatus?
    ): ResponseEntity<List<TrialResponse>>

    @GetMapping("/{id}")
    fun getTrial(
        @PathVariable id: Long
    ): ResponseEntity<TrialDetailResponse>

    @PostMapping("/{id}/promote")
    @PreAuthorize("hasRole('OFFICER')")
    fun promoteTrial(
        @PathVariable id: Long
    ): ResponseEntity<TrialResponse>

    @PostMapping("/{id}/extend")
    @PreAuthorize("hasRole('OFFICER')")
    fun extendTrial(
        @PathVariable id: Long,
        @RequestBody request: ExtendTrialRequest
    ): ResponseEntity<TrialResponse>

    @PostMapping("/{id}/end")
    @PreAuthorize("hasRole('OFFICER')")
    fun endTrial(
        @PathVariable id: Long,
        @RequestBody request: EndTrialRequest
    ): ResponseEntity<TrialResponse>
}
```

---

## Discord Integration

### Application Notifications

```kotlin
// ApplicationDiscordNotifier.kt
@Service
class ApplicationDiscordNotifier(
    private val discordClient: DiscordClient,
    private val guildConfigService: GuildConfigService
) {
    fun notifyNewApplication(application: ApplicationEntity) {
        val channel = guildConfigService.getApplicationsChannelId(application.guildId)

        val embed = EmbedBuilder()
            .setTitle("New Application: ${application.characterName}")
            .setColor(Color.BLUE)
            .addField("Class/Spec", "${application.characterClass} - ${application.specialization}", true)
            .addField("Item Level", application.itemLevel.toString(), true)
            .addField("M+ Score", application.mythicPlusScore?.toString() ?: "N/A", true)
            .addField("Average Parse", calculateAverageParse(application).toString() + "%", true)
            .setThumbnail(getClassIcon(application.characterClass))
            .setTimestamp(Instant.now())
            .build()

        discordClient.sendEmbed(channel, embed)
    }

    fun notifyApplicationDecision(application: ApplicationEntity, decision: String) {
        // DM the applicant
        discordClient.sendDM(
            application.discordUserId,
            "Your application to EdgeRush has been $decision. " +
            "Check the website for details."
        )
    }
}
```

### Application Commands

```kotlin
// ApplicationCommands.kt
@SlashCommand("application")
class ApplicationCommands(
    private val applicationService: ApplicationService
) {
    @SubCommand("status")
    fun checkStatus(event: SlashCommandEvent) {
        val discordId = event.user.id
        val application = applicationService.findByDiscordId(discordId)

        if (application != null) {
            event.reply(
                "Your application status: **${application.status}**\n" +
                "Submitted: ${application.submittedAt.formatRelative()}"
            ).setEphemeral(true).queue()
        } else {
            event.reply(
                "You don't have an active application. " +
                "Apply at: https://edgerush.gg/apply"
            ).setEphemeral(true).queue()
        }
    }

    @SubCommand("list")
    @RequireRole("Officer")
    fun listApplications(event: SlashCommandEvent) {
        val applications = applicationService.getPendingApplications()

        val embed = EmbedBuilder()
            .setTitle("Pending Applications (${applications.size})")
            .apply {
                applications.take(10).forEach { app ->
                    addField(
                        "${app.characterName} (${app.characterClass})",
                        "ilvl: ${app.itemLevel} | Parse: ${app.averageParse}% | Applied: ${app.submittedAt.formatRelative()}",
                        false
                    )
                }
            }
            .build()

        event.replyEmbeds(embed).setEphemeral(true).queue()
    }
}
```

---

## Implementation Phases

### Phase 1: Core Application Form
1. Create application entities and migrations
2. Implement OAuth flow (Discord + Battle.net)
3. Build character selection from Blizzard API
4. Create form steps with validation
5. Implement application submission

### Phase 2: Data Enrichment
1. Integrate Warcraft Logs data fetching
2. Integrate Raider.IO data fetching
3. Implement performance analysis calculations
4. Add red flag detection
5. Cache external API responses

### Phase 3: Officer Interface
1. Build application list view
2. Build application detail view
3. Implement approve/decline workflow
4. Add note system
5. Build analytics (application trends)

### Phase 4: Trial System
1. Create trial entities and migrations
2. Implement trial creation on approval
3. Build trial dashboard
4. Implement metric tracking
5. Add promote/extend/end workflows

### Phase 5: Discord Integration
1. Add new application notifications
2. Add decision notifications
3. Implement application commands
4. Add trial milestone notifications
5. Create applicant channels (optional)

### Phase 6: Polish
1. Email notifications (optional)
2. Application analytics
3. Bulk actions
4. Export functionality
5. Mobile optimization
