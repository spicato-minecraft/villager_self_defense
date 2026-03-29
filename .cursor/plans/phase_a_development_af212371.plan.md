---
name: Phase A Development
overview: "Phase A delivers Tier 0 from [VILLAGER_DEFENSE_IMPLEMENTATION.md](VILLAGER_DEFENSE_IMPLEMENTATION.md): server-authoritative mob-triggered defense with a top-priority Defense Brain activity, minimal server config, and a new [phaseATest.md](phaseATest.md) for environment setup and manual verification—grounded in [VILLAGER_DEFENSE_CORE.md](VILLAGER_DEFENSE_CORE.md) for priorities, eligibility, stand-down, and trading UI rules."
todos:
  - id: config-slice
    content: Add server config load/save + Tier 0 keys (stand-down, toggles); register from VillagerSelfDefense
    status: completed
  - id: damage-slice
    content: Single damage intake path; centralized defense eligibility (Tier 0 filters only inside)
    status: completed
  - id: state-standdown
    content: Per-villager defense state, target entity, stand-down on death + quiet window (configurable)
    status: completed
  - id: brain-activity
    content: Register Defense Activity at top priority; melee/approach tasks; no vanilla activity preempts while active
    status: completed
  - id: trading-ui
    content: Close merchant UI on defense entry; block reopen until stand-down (per CORE)
    status: completed
  - id: phaseATest-md
    content: "Create phaseATest.md: setup instructions, scenarios, pass/fail matrix for Phase A exit criteria"
    status: completed
isProject: false
---

