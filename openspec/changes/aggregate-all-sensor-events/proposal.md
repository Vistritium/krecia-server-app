## Why

The system currently gives events whose platform is `SENSOR_ALERT` direct control over the final alarm state. The operating policy is changing so that every sensor event is evidence for aggregate detection, and only configured frequency and occurrence rules decide whether the final alarm is active.

## What Changes

- Remove the strong-versus-weak classification of binary state events based on their platform.
- Route every binary state event through one per-device event history and the aggregate alarm-detection formula.
- Prevent an individual event from activating the final alarm solely because it comes from `SENSOR_ALERT`.
- Continue using the existing configured device-count, occurrence-count, time-window, and sustained-event rules to determine the final alarm state.
- **BREAKING**: `SENSOR_ALERT` events will no longer cause immediate alarm activation; they must contribute to a qualifying aggregate pattern.

## Capabilities

### New Capabilities

- `aggregate-alarm-detection`: Defines how all binary sensor events contribute equally to per-device history and how configured aggregate rules determine the final alarm state.

### Modified Capabilities

- None. There are no existing OpenSpec capability specifications in this project.

## Impact

- Affects binary-event classification, detector orchestration, per-device event tracking, aggregate formula naming, and final alarm-state calculation under `src/main/scala/krecia/maciejnowicki/com`.
- Removes the dedicated strong-alert detector path while retaining existing MQTT subscriptions and event payload compatibility.
- Downstream alarm publishing and critical notifications remain unchanged, but they will receive fewer immediate alarm activations because all activations must first satisfy an aggregate configuration.
- No new runtime dependencies or external APIs are introduced.
