## Purpose

Defines the aggregate detection policy that turns binary sensor activity into a final alarm only when configured device, occurrence, and time-window conditions are satisfied.

## ADDED Requirements

### Requirement: Uniform sensor-event treatment
The system SHALL process every accepted binary sensor event through the same aggregate detection policy regardless of its platform or source classification. Event platform metadata MUST NOT grant an event a direct or higher-priority path to final alarm activation.

#### Scenario: SENSOR_ALERT event is aggregated normally
- **WHEN** an event with platform `SENSOR_ALERT` is received
- **THEN** the system records it as input to aggregate detection using the same rules as every other binary sensor event

#### Scenario: Platform does not bypass aggregation
- **WHEN** an event's platform has a value that was previously treated as important
- **THEN** that value alone does not activate the final alarm

### Requirement: Aggregate-only alarm activation
The system SHALL activate the final alarm only when at least one configured aggregate rule is satisfied. Each rule SHALL evaluate the required number of distinct devices, the required number of computed alarm occurrences, and its configured time window; no individual event SHALL bypass those conditions.

#### Scenario: A configured aggregate rule is satisfied
- **WHEN** the recorded activity contains the configured minimum distinct devices and computed occurrences within the rule's time window
- **THEN** the system activates the final alarm and identifies the contributing devices and satisfied configuration

#### Scenario: Aggregate conditions are not satisfied
- **WHEN** recorded activity does not satisfy every condition of any configured aggregate rule
- **THEN** the system keeps or sets the final alarm to `OFF`

#### Scenario: A single important-platform event is insufficient
- **WHEN** one `SENSOR_ALERT` event is received and it does not cause an aggregate rule to qualify
- **THEN** the system does not activate the final alarm

### Requirement: Occurrence derivation from device state history
The system SHALL maintain state-transition history per sensor entity and derive alarm occurrences from that history using the configured alarm-duration interval. An `ON` transition SHALL contribute an occurrence, a continuously active state SHALL contribute further occurrences at that interval, and an `OFF` transition SHALL end the active interval.

#### Scenario: Sensor transitions to ON
- **WHEN** a sensor entity transitions from an inactive state to `ON`
- **THEN** aggregate detection includes an occurrence for that entity at the transition time

#### Scenario: Sensor remains ON
- **WHEN** a sensor entity remains continuously `ON` for one or more configured alarm-duration intervals
- **THEN** aggregate detection includes the additional interval occurrences when evaluating aggregate rules

#### Scenario: Sensor transitions to OFF
- **WHEN** a sensor entity transitions to `OFF`
- **THEN** aggregate detection stops deriving continuous-state occurrences after that transition

### Requirement: Time-based alarm reevaluation
The system SHALL reevaluate aggregate rules when sensor events arrive and during periodic refresh. Activity outside a rule's time window SHALL not contribute to that rule, and the final alarm SHALL return to `OFF` when no rule remains satisfied.

#### Scenario: Recent activity expires
- **WHEN** previously qualifying occurrences age outside all applicable configured time windows
- **THEN** the next reevaluation sets the final alarm to `OFF`

#### Scenario: New event changes aggregate state
- **WHEN** a newly received event causes an aggregate rule to become satisfied or cease to be satisfied
- **THEN** the system publishes the corresponding updated final alarm state
