## 1. Unify Event and Aggregate Models

- [x] 1.1 Remove alert-strength classification and strong/weak predicates from `BinaryStateEvent` while preserving all serialized payload fields, including `platform`.
- [x] 1.2 Rename `WeakAlertsAlarmDetectorFormula` to the neutral `AlarmDetectorFormula` and update its dependency-injection provider without changing aggregation calculations or configured thresholds.
- [x] 1.3 Rename `WeakAlertsAlarmDetector` to the neutral `AlarmStateManager`, remove its weak-event filter, and make every binary sensor event update the shared per-device history.

## 2. Consolidate Detector Orchestration

- [x] 2.1 Replace the detector actor's weak/strong detector state with one `AlarmStateManager` and use its aggregate result for both incoming-event and periodic-refresh processing.
- [x] 2.2 Remove `StrongAlertsAlarmDetector` and the no-longer-needed multi-detector state-combination behavior so no event source can bypass aggregate detection.
- [x] 2.3 Search production sources for stale alert-strength, strong-detector, and weak-detector references and update or remove every remaining policy-specific reference.

## 3. Verify the Focused Policy Change

- [x] 3.1 Review the implementation diff against the aggregate-alarm-detection specification and confirm MQTT topics, event payload compatibility, aggregation calculations, configured thresholds, alarm publication, and notifications remain unchanged.
- [x] 3.2 Run non-test compilation only and resolve production compilation errors without writing or running tests, in accordance with repository instructions.
