Phone Down — Product Requirements & Android Architecture Master Spec

1. Executive Summary

Phone Down is a calm, premium, minimal Android focus timer where a focus session only progresses when the user physically places their phone face down. The app intentionally avoids task lists, project management, noisy gamification, and productivity clutter. Its core behavioral insight is simple: the phone is usually the distraction, so the act of putting it down becomes the start of focus.

The product combines a minimal Pomodoro-like timer with sensor-based enforcement, session quality tracking, and deep analytics. Users start a session, place the phone face down, and the app begins timing only after the phone remains face down and stable for a short arming period. If the user picks up or meaningfully moves the phone, the timer pauses and interruption penalties are applied.

The app should feel like a premium wellness/focus product: neutral, elegant, monochrome, system-theme aware, and emotionally calm. It should not shame users. It should not feel like a game. The enforcement can be strict, but the language and design should remain soft, clear, and mature.

Core tagline:

Focus starts when the phone goes down.

⸻

2. Product Vision

Phone Down exists to help knowledge workers create uninterrupted focus sessions by making focus physically conditional: the session only counts while the phone is surrendered face down.

The app should become the simplest ritual for deep work:

1. Open app.
2. Choose duration.
3. Tap Start.
4. Put phone face down.
5. Work.
6. Return to see what happened.

The user should never feel like they are managing a productivity system. They should feel like they are entering a quiet focus state.

2.1 Product Positioning

Phone Down is not a task manager. It is not a notes app. It is not a habit tracker. It is not a full personal operating system.

It is a minimal focus ritual with honest analytics.

2.2 Product Promise

Phone Down helps users build focus by removing the most common source of interruption: the phone itself.

2.3 Differentiation

Most Pomodoro apps passively run a timer. Phone Down actively validates that the user has put the phone away.

The core differentiation is not the timer. It is the physical rule:

No face-down phone, no running timer.

⸻

3. Target Users

3.1 Primary Users

* Students
* Software developers
* Designers
* Writers
* Researchers
* Founders
* Corporate professionals
* Exam-prep candidates
* Remote workers
* Anyone doing mentally demanding work

3.2 User Mindset

The target user wants fewer distractions but does not want a complicated productivity system. They want a simple ritual that creates accountability without requiring planning overhead.

They may already know about Pomodoro, deep work, digital minimalism, or attention management, but the app should not require those concepts to be understood.

3.3 Jobs to Be Done

When I sit down to work or study, I want to put my phone away and start a focus session, so that I can work without constantly checking it.

When I finish a session, I want to see how focused I actually was, so that I can improve my focus habits over time.

When I look at my week or month, I want to understand when I focus best, how often I get interrupted, and whether I am becoming more consistent.

⸻

4. Core Principles

4.1 Minimal by Default

The app should present only what is necessary at the moment. The main screen should not contain task lists, categories, projects, calendars, feeds, or social elements.

4.2 Strict Mechanism, Calm Language

The session rules can be strict, but the copy should remain neutral.

Use:

* “Session interrupted.”
* “Focus paused.”
* “Return phone down to continue.”
* “Not enough focus time to count.”

Avoid:

* “You failed.”
* “Discipline broken.”
* “Bad session.”
* “You lost.”

4.3 Honest Analytics

The app should distinguish between:

* Started sessions
* Completed sessions
* Clean sessions
* Partial sessions
* Broken sessions
* Invalidated sessions

A session should not be falsely celebrated if the user repeatedly picked up the phone.

4.4 Offline-First, Cloud-Optional

The app should work fully without login for free users. Google login and Google Drive backup should be available in V1, primarily for Pro users. The core timer should never require a network connection.

4.5 Privacy-Respecting

Core focus data should be stored locally. Cloud backup should be opt-in. The app should avoid ads and avoid unnecessary tracking.

4.6 Premium Wellness Aesthetic

The design should feel like a calm, premium tool. Think ChatGPT + Headspace rather than gamer productivity or aggressive self-improvement.

⸻

5. MVP Scope

5.1 V1 Launch Scope

V1 launch should include:

* Focus timer
* Face-down detection
* 3-second arming period
* Foreground service during active sessions
* Sound and haptic feedback
* Session lifecycle tracking
* Interruption and penalty logic
* Early-ending classification
* Local Room database
* Focus tab
* Insights tab
* Settings tab
* Basic onboarding
* Today and 7-day analytics
* Streaks
* Focus Quality score
* Google Sign-In
* Google Drive backup/restore for Pro
* Google Play Billing
* Free vs Pro paywall foundation
* System theme support
* Monochrome design system

5.2 V1.1 Scope

V1.1 should include:

* Widgets
* Google Calendar sync
* Data export
* Month/year analytics
* Heatmap polish
* Deeper advanced insights
* More refined Pro paywall surfaces

5.3 Non-Goals for V1

V1 should not include:

* Task lists
* Project management
* Tags/categories
* Notes after sessions
* Social sharing
* Leaderboards
* Public profiles
* Team features
* AI coaching
* Wear OS support
* Digital Wellbeing integration
* Complex Pomodoro cycles
* Break timers
* Mandatory login
* Ads

⸻

6. App Name and Brand

6.1 Name

Phone Down

6.2 Tagline

Focus starts when the phone goes down.

6.3 Brand Personality

* Calm
* Premium
* Minimal
* Neutral
* Trustworthy
* Elegant
* Universal
* Honest

6.4 App Icon Direction

Recommended direction:

A minimal phone silhouette placed face down, using a monochrome or near-monochrome design. The icon should be recognizable at small sizes and should not use loud gradients or cartoon imagery.

⸻

7. User Experience Overview

7.1 Main Navigation

Use 3 tabs:

1. Focus
2. Insights
3. Settings

7.2 Primary User Flow

Open app
→ select duration
→ tap Start
→ place phone face down
→ app detects valid face-down state
→ app waits 3 seconds
→ haptic/sound confirms start
→ timer runs
→ phone pickup pauses timer and applies interruption rules
→ session completes or user ends early
→ session is logged
→ insights update

7.3 First Launch Flow

Recommended onboarding:

1. Card 1: “Start a focus session.”
2. Card 2: “Place your phone face down.”
3. Card 3: “Pickups pause your session and affect your Focus Quality.”
4. Optional permissions explanation.
5. Enter app.

Avoid asking too many setup questions during onboarding. Default duration should be 25 minutes. User can change settings later.

⸻

8. Focus Tab Specification

8.1 Purpose

The Focus tab is the core of the app. It should be extremely minimal.

8.2 Default State

Display:

* Large selected duration
* Start button
* Today summary
* Duration selector access

Example:

25:00
Start
Today
1h 20m · 3 sessions · 2 clean

8.3 Duration Presets

Default presets:

* 10 minutes
* 15 minutes
* 25 minutes
* 45 minutes
* 60 minutes
* Custom

Default selected duration: 25 minutes.

8.4 Duration Interaction

User should be able to tap the duration or a subtle selector to change duration. Avoid cluttering the main screen with too many controls.

Possible interaction:

* Tap duration → bottom sheet opens
* Bottom sheet shows presets and custom duration option

8.5 Waiting State

After user taps Start:

Place phone down
to begin.

The app should now listen for valid face-down state.

8.6 Arming State

Once phone is face down and valid:

Hold still…
3

Then 2, then 1. After 3 seconds, timer starts.

8.7 Active State

When active, the phone is usually face down. The screen may dim or eventually turn off naturally.

If visible before screen-off:

Focus active

The timer can remain visible but should not encourage checking.

8.8 Interrupted State

When phone is picked up or invalid movement is detected:

Focus paused
Return phone down to continue.

If penalty applies:

Session interrupted
Penalty applied.

8.9 Completed State

If clean:

Clean session completed
25 minutes

If completed with interruptions:

Session completed
2 interruptions

8.10 Early End Flow

If user picks up the phone and taps End before completion:

Show a confirmation only if needed:

End this session?
Current progress will be saved as partial.

Buttons:

* End Session
* Continue

For very short sessions under 20%, copy:

Not enough focus time to count.

⸻

9. Insights Tab Specification

9.1 Purpose

Insights should help users understand focus patterns without making the app feel like a spreadsheet.

9.2 Free Insights

Free users get:

* Today focus time
* Today sessions
* Today clean sessions
* Today interruptions
* Current streak
* Last 7 days summary
* Basic session history
* Basic Focus Quality for today

9.3 Pro Insights

Pro users get:

* Unlimited history
* Monthly analytics
* Yearly analytics
* GitHub-style focus heatmap
* Best focus hour
* Best day of week
* Weekday vs weekend comparison
* Completion rate trend
* Clean ratio trend
* Interruption trend
* Focus Quality history
* Longest clean session
* Average session length over time
* Data export

9.4 Insights Sections

Recommended layout:

1. Today Summary
2. Focus Quality
3. Last 7 Days
4. Streak
5. Session History
6. Advanced Insights, Pro-gated

9.5 Session History Card

Each session card should show:

* Start time
* Duration
* Status
* Clean/interrupted/broken label
* Interruption count
* Optional penalty time

Example:

9:30 AM · 25 min
Completed · Clean

or

2:10 PM · 18 min
Partial · 1 interruption

⸻

10. Settings Tab Specification

10.1 Settings Sections

Recommended sections:

1. Timer
2. Feedback
3. Account & Backup
4. Pro
5. Privacy
6. About

10.2 Timer Settings

* Default duration
* Custom duration management
* Daily goal, if included later

10.3 Feedback Settings

* Sounds on/off
* Haptics on/off

10.4 Account & Backup

* Sign in with Google
* Backup status
* Last backup time
* Restore from backup
* Auto-backup toggle, Pro only

10.5 Pro Section

* Upgrade to Pro
* Restore purchases
* Manage subscription

10.6 Privacy Section

* Local data explanation
* Cloud backup explanation
* Export data, Pro
* Delete all data

⸻

11. Session Lifecycle

11.1 Session States

enum class SessionState {
    CREATED,
    WAITING_FOR_PHONE_DOWN,
    ARMING,
    ACTIVE,
    PAUSED_BY_PICKUP,
    PAUSED_BY_CALL,
    COMPLETED,
    ENDED_EARLY,
    INVALIDATED,
    BROKEN,
    ABANDONED
}

11.2 State Descriptions

State	Description
Created	User tapped Start; session entity may be initialized.
Waiting for phone down	App is waiting for valid face-down orientation.
Arming	Phone is face down; app waits 3 seconds for stability.
Active	Timer is progressing.
Paused by pickup	Phone orientation/movement became invalid.
Paused by call	Incoming or active call paused the session.
Completed	Planned focus duration achieved.
Ended early	User manually ended before completion.
Invalidated	Too little valid focus time to count.
Broken	Too many or too long interruptions.
Abandoned	App killed, phone restarted, or session could not be recovered.

11.3 Completion Categories

enum class SessionResult {
    CLEAN_COMPLETED,
    COMPLETED_WITH_INTERRUPTION,
    PARTIAL,
    STRONG_PARTIAL,
    INVALIDATED,
    BROKEN,
    ABANDONED
}

⸻

12. Face-Down Detection Logic

12.1 Valid Focus Condition

A focus session progresses only when:

Phone is face down
+ mostly horizontal
+ within acceptable movement tolerance
+ not in a call pause state
+ foreground session is active

12.2 Invalid Conditions

Invalid conditions include:

* Phone face up
* Phone vertical
* Phone in hand
* Strong movement
* Walking-like movement pattern
* Moving vehicle-like instability
* App force close
* Device restart
* Sensor unavailable

12.3 Sensor Inputs

Use Android SensorManager.

Primary sensors:

* Accelerometer
* Rotation vector, if available

Optional sensors:

* Gyroscope, only if needed
* Proximity sensor, not required for V1
* Light sensor, not required for V1

12.4 Recommended Detection Approach

Use accelerometer gravity vector and/or rotation vector to determine orientation.

Conceptual logic:

If device z-axis indicates screen is facing downward
AND pitch/roll are within horizontal tolerance
AND movement variance is below threshold
THEN face-down valid
ELSE invalid

12.5 Stability Window

Before starting a session:

* Require valid face-down state for 3 continuous seconds.
* If invalid state occurs during arming, reset arming countdown.

12.6 Movement Tolerance

Tiny movements should not count as interruptions.

Do not penalize:

* Minor table vibrations
* Notification vibration
* Small sensor drift
* Very brief accidental bumps

Penalize:

* Phone lifted
* Phone turned face up
* Phone significantly tilted
* Phone moved repeatedly or strongly
* Phone remains invalid beyond grace period

12.7 Pocket Rejection

Pocket mode should not count as valid focus in V1. The app should prefer stable horizontal face-down placement on a surface.

If phone appears face down but is moving continuously, treat as invalid.

12.8 Calibration

No manual calibration in V1.

Future optional setting:

* Detection sensitivity: Relaxed / Standard

Default: Standard.

⸻

13. Timer and Penalty System

13.1 Timer Principle

The focus timer progresses only during valid focus state.

When invalid state is detected:

* Timer pauses immediately.
* Interruption tracking starts.
* Grace period begins.

13.2 Grace Period

Grace period: 5 seconds.

If phone returns to valid face-down state within 5 seconds:

* No time penalty.
* Clean status is lost.
* Minor interruption is recorded.
* Session resumes after valid stable state is restored.

13.3 Penalty Interruption

If phone remains invalid for more than 5 seconds:

* Timer remains paused.
* Add 1 minute penalty to required session time.
* Record one penalty interruption.
* Clean status is lost.

13.4 Broken Session Conditions

A session becomes Broken if:

* Phone remains invalid for more than 60 continuous seconds.
* User accumulates 3 penalty interruptions.
* App is force-closed during active session.
* Device restart occurs during active session.

13.5 Broken Session Behavior

When session becomes Broken:

* User can continue focusing.
* Session remains marked Broken.
* Valid focus time continues to accumulate.
* Session does not count as clean.
* Session may count toward total focus time if enough valid focus was completed.

13.6 Early End Rules

If user ends session before planned completion:

Completed Valid Focus %	Classification	Count Toward Focus Time?	Count as Completed?
0–20%	Invalidated	No	No
21–79%	Partial	Yes	No
80–99%	Strong Partial	Yes	No
100%+	Completed	Yes	Yes

13.7 Clean Session Rules

A clean session requires:

* Planned duration completed
* Zero pickups
* Zero minor interruptions
* Zero penalty interruptions
* No call pause
* No manual early end
* No app kill or restart

13.8 Completed With Interruption

A session is completed with interruption if:

* Planned duration is completed
* One or more minor/penalty interruptions occurred
* Session did not cross broken thresholds

⸻

14. Calls, Notifications, and Edge Cases

14.1 Incoming Calls

Incoming calls pause the session.

Rules:

* Timer pauses while phone is ringing or call is active.
* No direct penalty while call state is active.
* Clean status is lost.
* If call is short, user can return phone down and continue.
* If call is long, session is marked interrupted by call but not necessarily broken.

14.2 Notifications

Regular notifications should not affect the session unless they cause the user to pick up or move the phone.

14.3 Alarms

Alarms do not automatically break the session. If user picks up the phone, normal interruption rules apply.

14.4 Force Close

If the app is force-closed during an active session:

* Mark session as Abandoned or Broken.
* Do not count as clean.
* Persist enough session state to classify it on next launch.

14.5 Device Restart

If device restarts during active session:

* Mark session as Abandoned.
* Do not count as clean.

14.6 Battery Death

If the device powers off during session:

* Mark session as Abandoned.

14.7 System Time Changes

Use SystemClock.elapsedRealtime() for active session timing. Do not rely only on wall-clock time. If wall-clock time changes suspiciously during session, preserve monotonic timing.

14.8 Sensor Unavailable

If required sensors are unavailable:

* Session cannot start.
* Show clear message:

Phone Down needs motion sensors to detect when your phone is face down.

⸻

15. Analytics Specification

15.1 Core Metrics

Track:

* Total focus time
* Completed sessions
* Clean sessions
* Partial sessions
* Broken sessions
* Invalidated sessions
* Abandoned sessions
* Interruption count
* Penalty count
* Penalty time
* Average session duration
* Longest session
* Longest clean session
* Completion rate
* Clean session ratio
* Focus Quality
* Streak
* Best focus hour
* Best weekday
* Weekday vs weekend focus

15.2 Time Windows

Support:

* Today
* Last 7 days
* Month
* Year
* All time

Free users should see today and last 7 days. Pro users should see all windows.

15.3 Focus Quality Formula

Focus Quality should be a score from 0 to 100.

Initial formula:

Focus Quality =
40% Completion Rate
+ 25% Clean Session Ratio
+ 20% Focus Volume
+ 15% Interruption Control

15.4 Focus Quality Labels

Score	Label
90–100	Deep
75–89	Focused
60–74	Steady
40–59	Fragmented
0–39	Scattered

15.5 Streak Definition

A focus day counts if the user completes at least one session.

Future setting may allow streaks based on daily focus target.

15.6 Heatmap

Pro feature.

GitHub-style grid showing valid focus time or completed sessions per day.

Intensity based on focus minutes, not raw app opens.

15.7 Best Focus Hour

Calculate by aggregating valid focus minutes by hour of day.

Example insight:

Your strongest focus window is usually 9–11 AM.

15.8 Best Day of Week

Calculate average valid focus minutes and completion rate by weekday.

15.9 Completion Rate

Completed sessions / Started sessions

Exclude sessions that never reached active state.

15.10 Clean Ratio

Clean completed sessions / Completed sessions

⸻

16. Free vs Pro Feature Split

16.1 Free Features

* Unlimited focus sessions
* Face-down enforcement
* Preset durations
* Basic custom duration support, optional
* Today stats
* Last 7 days stats
* Basic session history
* Basic streak
* Local storage
* Sound/haptics
* System theme

16.2 Pro Features

* Unlimited analytics history
* Advanced insights
* Monthly/yearly analytics
* Heatmap
* Best hour/day insights
* Focus Quality trends
* Completion/clean/interruption trends
* Google Drive backup/restore
* Data export
* Calendar sync, V1.1
* Widgets, V1.1
* Custom advanced duration presets
* Yearly report, future

16.3 Pricing Recommendation

India launch pricing:

* ₹99/month
* ₹699/year
* ₹1,999 lifetime early supporter

Later pricing:

* ₹129/month
* ₹999/year
* ₹2,999 lifetime

Global pricing:

* $1.99–2.99/month
* $14.99–19.99/year
* $39.99–59.99 lifetime

16.4 Ads

No ads. Ads conflict with the product’s premium, calm, distraction-free positioning.

⸻

17. Android Technical Architecture

17.1 Recommended Stack

* Kotlin
* Jetpack Compose
* Material 3, heavily customized
* Room
* DataStore
* Hilt
* Kotlin Coroutines
* Flow
* Foreground Service
* SensorManager
* WorkManager
* Google Sign-In
* Google Drive API
* Google Play Billing Library
* Vico for Compose charts

17.2 Architecture Style

Use clean, multi-module architecture from day one.

Principles:

* UI renders state.
* ViewModels coordinate UI logic.
* Domain layer owns business rules.
* Repositories abstract data sources.
* Sensor logic is isolated.
* Timer/session engine is testable without Android UI.
* Compose screens should not contain business logic.

17.3 Module Structure

Recommended modules:

:app
:core:common
:core:model
:core:designsystem
:core:database
:core:datastore
:core:sensors
:core:notifications
:core:billing
:core:auth
:core:backup
:core:charts
:domain:session
:domain:insights
:feature:onboarding
:feature:focus
:feature:insights
:feature:settings
:feature:account
:feature:pro

17.4 Module Responsibilities

:app

* Application class
* Navigation host
* DI setup
* App-level theme
* MainActivity

:core:common

* Utility classes
* Result wrappers
* Dispatchers
* Time abstractions
* Error types

:core:model

Shared models:

* FocusSession
* SessionState
* SessionResult
* PenaltyEvent
* FocusQuality
* UserSettings

:core:designsystem

* Theme
* Typography
* Spacing
* Components
* Buttons
* Cards
* Chart wrappers
* Motion constants

:core:database

* Room database
* DAOs
* Entities
* Mappers

:core:datastore

* User preferences
* Default duration
* Sound/haptic settings
* Onboarding completion
* Backup settings

:core:sensors

* FaceDownDetector
* Sensor abstractions
* Movement classifier
* Orientation evaluator

:core:notifications

* Foreground service notification
* Completion notification
* Notification channels

:core:billing

* Play Billing integration
* Product details
* Purchase state
* Entitlement repository

:core:auth

* Google Sign-In
* Account state
* Auth token handling

:core:backup

* Google Drive backup
* Restore logic
* Serialization
* Backup scheduling

:core:charts

* Vico chart wrappers
* Heatmap component

:domain:session

* Start session use case
* End session use case
* Apply interruption use case
* Session state machine
* Penalty rules
* Session classification

:domain:insights

* Daily aggregation
* Weekly aggregation
* Focus Quality calculation
* Streak calculation
* Best hour/day calculation

:feature:focus

* Focus screen
* Duration selector
* Active session UI
* Completion UI

:feature:insights

* Insights screen
* Today card
* Week chart
* Session history
* Pro advanced insights

:feature:settings

* Settings screen
* Timer preferences
* Sound/haptics
* Privacy controls

:feature:account

* Google account screen
* Backup status
* Restore flow

:feature:pro

* Paywall
* Purchase management
* Restore purchases

⸻

18. Data Model

18.1 FocusSession

data class FocusSession(
    val id: String,
    val plannedDurationSeconds: Long,
    val requiredDurationSeconds: Long,
    val validFocusSeconds: Long,
    val actualElapsedSeconds: Long,
    val penaltySeconds: Long,
    val interruptionCount: Int,
    val minorInterruptionCount: Int,
    val penaltyInterruptionCount: Int,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val startElapsedRealtime: Long,
    val endElapsedRealtime: Long?,
    val state: SessionState,
    val result: SessionResult?,
    val clean: Boolean,
    val broken: Boolean,
    val callInterrupted: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

18.2 PenaltyEvent

data class PenaltyEvent(
    val id: String,
    val sessionId: String,
    val type: PenaltyEventType,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val durationSeconds: Long,
    val penaltySeconds: Long
)

18.3 PenaltyEventType

enum class PenaltyEventType {
    MINOR_PICKUP,
    PENALTY_PICKUP,
    LONG_PICKUP,
    CALL_PAUSE,
    FORCE_CLOSE,
    DEVICE_RESTART,
    MANUAL_END
}

18.4 UserSettings

data class UserSettings(
    val defaultDurationSeconds: Long,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val onboardingCompleted: Boolean,
    val autoBackupEnabled: Boolean,
    val themeMode: ThemeMode
)

18.5 Entitlement

data class Entitlement(
    val isPro: Boolean,
    val source: EntitlementSource?,
    val expiryEpochMillis: Long?
)

⸻

19. Foreground Service Design

19.1 Purpose

The foreground service ensures active sessions continue reliably when the app is backgrounded or the screen turns off.

19.2 Responsibilities

* Maintain active session timer
* Listen to sensor state
* Apply interruption events
* Update notification
* Persist session state
* Trigger completion feedback

19.3 Notification

Persistent notification:

Phone Down
Focus active · 18 min left

Actions:

* End Session

Avoid pause/add-time actions in V1.

19.4 Timing Source

Use:

SystemClock.elapsedRealtime()

Use wall-clock time only for display, history grouping, and analytics dates.

⸻

20. Backup and Restore Design

20.1 Backup Strategy

Use Google Drive app data folder for V1 Pro backup.

Backup should include:

* Sessions
* Penalty events
* Settings
* Entitlement-independent local metadata

Do not backup raw billing entitlement as source of truth. Billing should be restored through Play Billing.

20.2 Backup Format

Use versioned JSON.

Example:

{
  "schemaVersion": 1,
  "exportedAt": 1760000000000,
  "sessions": [],
  "penaltyEvents": [],
  "settings": {}
}

20.3 Backup Triggers

* Manual backup
* Auto backup after session completion, Pro only
* Scheduled daily backup, Pro only

20.4 Restore Behavior

Restore should:

* Validate schema version
* Merge sessions by ID
* Avoid duplicates
* Preserve newer local records when conflict occurs
* Show summary after restore

⸻

21. Billing Design

21.1 Products

Recommended Play Billing products:

Subscriptions:

* phone_down_pro_monthly
* phone_down_pro_yearly

One-time product:

* phone_down_pro_lifetime

21.2 Entitlement Resolution

User is Pro if:

* Active monthly subscription, or
* Active yearly subscription, or
* Lifetime purchase owned

21.3 Restore Purchases

Settings should include:

* Restore purchases
* Manage subscription

21.4 Paywall Surfaces

Paywall appears when user attempts to access:

* Advanced analytics beyond 7 days
* Heatmap
* Backup/restore
* Export
* Widgets, V1.1
* Calendar sync, V1.1

Avoid showing paywall before the user experiences the core timer.

⸻

22. Charting and Insights Implementation

22.1 Recommended Library

Use Vico for Compose charts.

22.2 Vico Use Cases

* Last 7 days focus time
* Monthly focus trend
* Completion rate trend
* Interruption trend
* Best hour bar chart
* Weekday comparison

22.3 Custom Compose Charts

Build custom lightweight components for:

* GitHub-style heatmap
* Focus Quality card
* Streak dots

⸻

23. Privacy and Security

23.1 Local-First Privacy

The app should work offline. Session data should be stored locally unless user enables cloud backup.

23.2 No Ads

No advertising SDKs.

23.3 Google Account Usage

Google login should be used only for:

* Drive backup/restore
* Calendar sync, later
* Account identity for Pro backup features

23.4 Data Deletion

Settings should include:

* Delete local data
* Delete cloud backup, Pro if implemented

⸻

24. Design System

24.1 Theme

Follow system theme by default.

Modes:

* Light
* Dark
* System

User-facing theme setting can be added later if needed.

24.2 Visual Style

* Monochrome
* No accent color
* Pure flat surfaces
* Large typography
* Soft rounded cards
* Thin dividers
* Calm motion
* No loud gradients
* No gamified colors

24.3 Typography

Use large numeric typography for timer. Prefer clean, modern, highly legible type.

24.4 Motion

Animations should be:

* Slow
* Calm
* Elegant
* Functional

Avoid springy, playful, or game-like animation.

24.5 Sound and Haptics

Events:

Event	Feedback
Phone down detected	Tiny haptic
Timer starts	Soft chime + haptic
Phone picked up	Warning haptic
Session completed	Calm chime + longer haptic
Session broken	Low soft haptic

Respect user sound settings and system silent mode where appropriate.

⸻

25. Testing Strategy

25.1 Unit Tests

Test:

* Penalty rules
* Session state transitions
* Early end classification
* Focus Quality calculation
* Streak calculation
* Best hour/day aggregation
* Backup serialization

25.2 Sensor Tests

Use abstraction around sensor inputs to simulate:

* Face down stable
* Face up
* Vertical
* Minor bump
* Pickup
* Long pickup
* Continuous movement
* Arming reset

25.3 Foreground Service Tests

Test:

* Service starts on session start
* Notification appears
* Timer continues with screen off
* Session persists after process recreation
* Force-close classification where possible

25.4 UI Tests

Test:

* Start flow
* Duration selection
* Waiting state
* Completion state
* Early end flow
* Insights rendering
* Paywall access

25.5 Manual Device Testing

Test on multiple devices:

* Pixel device
* Samsung device
* OnePlus/Realme device
* Low-end Android device
* Android 12+
* Android 14/15+

Specific manual scenarios:

* Phone face down on desk
* Phone face down on bed
* Phone face down while charging
* Tiny table bump
* Incoming call
* Screen off
* Battery saver
* App backgrounded
* Force close
* Device restart

⸻

26. MVP Development Roadmap

Phase 1 — Foundation

* Project setup
* Multi-module architecture
* Design system
* Navigation
* Room database
* DataStore

Phase 2 — Core Session Engine

* Session model
* Timer engine
* State machine
* Penalty logic
* Unit tests

Phase 3 — Sensor Engine

* SensorManager integration
* Face-down detector
* Movement tolerance
* Arming logic
* Sensor simulation tests

Phase 4 — Foreground Service

* Active session service
* Notification channel
* Persistent notification
* Completion feedback
* Screen-off testing

Phase 5 — Focus UI

* Focus screen
* Duration selector
* Waiting/arming/interrupted/completed states
* Early end flow

Phase 6 — Insights

* Today stats
* Last 7 days stats
* Session history
* Streak
* Focus Quality
* Basic Vico charts

Phase 7 — Settings

* Timer settings
* Sound/haptic toggles
* Privacy/delete data
* About screen

Phase 8 — Auth, Billing, Backup

* Google Sign-In
* Play Billing
* Pro entitlement
* Drive backup
* Restore flow
* Paywall

Phase 9 — Polish and QA

* Animations
* Empty states
* Error states
* Sensor edge cases
* Battery testing
* Play Store readiness

Phase 10 — Launch

* Store listing
* Screenshots
* Privacy policy
* Internal testing
* Closed testing
* Production release

⸻

27. Future Enhancements

Potential future features:

* Widgets
* Calendar sync
* Data export
* Yearly focus report
* Focus intention, optional
* Detection sensitivity setting
* Advanced insights/recommendations
* Custom session goals
* Focus reminders
* Desktop companion, far future
* Wear OS, far future

⸻

28. Open Questions

These are not blockers for V1 planning, but should be resolved during implementation:

1. Should custom durations be free or Pro?
2. Should backup run after every session or once daily by default?
3. Should free users see 7 days or 14 days of analytics?
4. Should Focus Quality require a daily goal or use adaptive scoring?
5. Should call interruptions become Broken after a long duration, or remain separate?
6. Should phone-down detection use only accelerometer first, or rotation vector as primary when available?
7. Should the app dim the screen immediately after arming, or let the system handle screen timeout?
8. Should completion sound play if the phone is in silent mode?
9. Should the first Play Store launch include lifetime Pro from day one?
10. Should calendar sync and widgets be V1.1 or pulled into V1 if development goes smoothly?

⸻

29. Recommended Final V1 Definition

The recommended V1 is:

A polished Android app where users can start a focus session, place their phone face down, and build clean focus time with honest analytics. The app is offline-first, login-optional, premium in design, and monetized through advanced analytics and backup features rather than limiting the core focus experience.

The V1 should be commercially credible but not overextended. The core must feel reliable before any advanced feature matters.

Priority order:

1. Sensor reliability
2. Timer/session correctness
3. Clean UX
4. Useful analytics
5. Backup and Pro system
6. Visual polish

If the face-down mechanism feels magical and reliable, the app has a real identity.