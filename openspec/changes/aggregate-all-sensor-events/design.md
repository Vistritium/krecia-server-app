## Context

See `proposal.md` for the policy motivation and `specs/aggregate-alarm-detection/spec.md` for the behavioral contract.

The detector currently classifies a binary state event as strong when its `platform` is `SENSOR_ALERT` and weak otherwise. The detector actor sends every event to both specialized detectors: each accepts its own category, the weak detector applies the aggregate formula, and the strong detector reports an active device directly from its latest state. Their outputs are combined before the final alarm device is updated.

The repository history contains the earlier single-manager structure, but the current code also includes useful cleanup performed alongside the strong-alert feature. Project instructions prohibit writing or running tests, so implementation verification must use inspection and non-test compilation only.

## Goals / Non-Goals

**Goals:**

- Establish one authoritative event-history and aggregate-calculation path for every binary sensor event.
- Make event platform metadata irrelevant to alarm priority while keeping the field compatible with existing payloads.
- Preserve the current aggregation formula, configuration values, periodic reevaluation, alarm publication, and notification integrations.
- Leave the detector model and names neutral rather than retaining obsolete `Weak` terminology.

**Non-Goals:**

- Changing device-count, occurrence-count, time-window, or sustained-event interval configuration.
- Changing MQTT topics, event JSON structure, web routes, final alarm publication, or notification delivery.
- Adding persistence for detector history or retaining in-memory history across a process restart.
- Reclassifying selected platforms through a replacement priority mechanism.

## Decisions

### Apply a focused semantic rollback instead of reverting the entire historical commit

The implementation will reverse the strong-versus-weak policy while retaining unrelated cleanup from the current version. The earlier implementation is a behavioral reference, not a source snapshot that must be restored byte for byte.

Alternative considered: revert commit `7e96418` wholesale. This would recover the earlier behavior quickly, but it would also restore obsolete imports and dead experimental actor code and discard neutral conveniences added in that commit. A focused change better represents the new policy decision.

### Use one aggregate detector manager

The detector actor will own one manager that records every `BinaryStateEvent` by entity and asks one formula for the resulting state. Both event-driven and periodic-refresh paths will read from this same manager. The dedicated strong-alert detector and multi-detector result combination will be removed.

Alternative considered: keep both detector instances but configure the strong detector to stop triggering directly. That leaves category-specific structure with no valid policy distinction and creates a future risk that the bypass is accidentally restored.

### Remove priority behavior from the event model

`BinaryStateEvent` will retain `platform` as input metadata, but it will no longer expose alert-strength classification or strong/weak predicates. This keeps MQTT payload compatibility while making it impossible for detector orchestration to use the old shortcut accidentally.

Alternative considered: retain the classification helpers and simply ignore them. Keeping unused policy concepts in the input model would misrepresent the new rule and invite divergent behavior later.

### Reuse the current aggregate formula unchanged

The current weak-alert formula already models the desired device-count, occurrence-count, time-window, and sustained-`ON` behavior. It will be renamed to a neutral aggregate detector formula and supplied to the unified manager without changing its calculations or configured thresholds.

Alternative considered: rewrite the aggregation algorithm while consolidating the pipeline. That would expand the behavioral surface and make it harder to distinguish this policy change from formula changes.

### Derive final alarm state exclusively from aggregate results

The unified manager will return the contributing device identifiers and satisfied configuration names produced by the aggregate formula. The detector actor will continue setting the alarm `ON` when the resulting contributing-device list is non-empty and `OFF` otherwise. No event source will write directly to the final alarm path.

## Risks / Trade-offs

- [A formerly immediate `SENSOR_ALERT` signal may no longer activate an alarm] → This is the intended policy change; make it prominent in deployment review and preserve the existing aggregate thresholds exactly.
- [An alarm active only through the strong-alert path can turn `OFF` after deployment] → Deploy with awareness that detector state is in memory and verify the published alarm state after restart.
- [Renaming the weak-specific classes can leave stale references] → Update production references as one atomic change and verify with non-test compilation plus repository-wide symbol searches.
- [A broad historical revert could reintroduce unrelated obsolete code] → Use the parent version only as a semantic reference and review the final diff against the focused decisions above.

## Migration Plan

1. Deploy the unified detector implementation as one application version; no data migration or dependency rollout is required.
2. Restart the service so the detector actor starts with one empty aggregate history, consistent with its existing in-memory lifecycle.
3. Confirm that alarm publication initializes normally and that subsequent `SENSOR_ALERT` events appear in aggregate detector state without bypassing its rules.
4. If rollback is required, redeploy the current strong-alert-capable application version; there is no stored detector data to transform in either direction.
