# Proposals for team review: account roles, recommendations, offline behavior

**Status: PROPOSED — not decided.** Nothing in this file has been built. These are the suggested defaults from the
2026-11-26 audit of `react-native/` (commit `1b05bc3`) against Chapters 1 and 2 of the BASA-KITA paper. Each one
needs a yes, a change, or a no from the team before the matching work starts. Record the answer in the
"Team decision" line.

## 1. Account roles on Android

**What the paper says**
- Chapter 2 §2.8.1 Login and User Access: teachers and parents or guardians log in.
- Figure 13 text and the Level 0 DFD actor list: adults guide the learner on the learner's account; the actors are
  Learner, School Admin and Main System Admin.
- Use Case Diagram (Figure 15): School Admin generalizes Learner (every learner function, plus Submit Story).
  Log In opens the Learner, School Admin or Main System Admin dashboard.
- Chapter 1 Specific Objective 1.9: a Teacher's Dashboard that sends materials home.
- Data Dictionary Table 7 roles: `learner`, `school_admin`, `main_system_admin`.

**What the code does today:** `src/state/model.ts` uses `learner`, `admin` and `pending`.

**Proposed default**
- Use the Table 7 role names in the app.
- Android serves `learner` and `school_admin`, both with the learner features. Story submission and review stay on the website.
- `main_system_admin` signing in on Android sees a "please use the website" screen.
- Teacher assignments on mobile (Obj 1.9) wait until the team confirms who the teacher account is.

**Team decision:** _pending_

## 2. Story recommendations on Home

**What the paper says**
- Use Case 5 (View Recommended Stories): AI-based recommendations on Home, based on preferred language, reading level and reading activity.
- Chapter 2 §2.8.1 Personalized Recommendation Module: the criteria, data and method are left for the team to finalize.
- Level 1 DFD process 2.0: uses language, category, level and progress to pick stories.
- Chapter 1 Purpose and Specific Objective 1.6: adds responses, attempts and extra practice.

**What the code does today:** `recommendStories()` in `src/shared/learner.js` exists but is not called; Home shows
starter guidance and Continue Reading.

**Proposed default**
- Phase 1: rule-based ranking of approved stories using the DFD 2.0 inputs (preferred language, category, level,
  progress), with unread stories first and a short reason shown on each card.
- Phase 2: practice suggestions from quiz and letter-sound results (Obj 1.6).
- Question for the team: does "AI-based" in Use Case 5 require a trained model, or is a documented rule-based ranker acceptable?

**Team decision:** _pending_

## 3. Offline behavior

**What the paper says**
- Chapter 2 §2.7 Program Specification: the app works online.
- Chapter 1 Scope and Limitations: an internet connection is needed for assignments and progress synchronization;
  offline practice is not specified.

**What the code does today:** everything is saved on the phone with AsyncStorage (`src/state/AppState.tsx`); there is no backend sync yet.

**Proposed default**
- Sign-in and registration require a connection.
- Bundled stories stay readable offline after sign-in.
- Reading progress and activity are saved on the phone first and sent to the backend when the phone is back online.
- Show a small "offline, will sync later" note instead of an error.

**Team decision:** _pending_
