# Availability Specification

## Rolling window
The freelancer always manages the next 10 calendar days relative to the application date/time.

Each date has:
- status: AVAILABLE | BUSY | NOT_SET
- start time
- end time when AVAILABLE

## Example
```text
Day 1  AVAILABLE 09:00-17:00
Day 2  BUSY
Day 3  AVAILABLE 10:00-18:00
Day 4  NOT_SET
...
Day 10 AVAILABLE 09:00-13:00
```

## Studio filtering
For requested date/time inside the next 10 days:
- AVAILABLE only.
- Requested interval must fit/overlap according to the approved booking rule.
- BUSY excluded.
- NOT_SET excluded.

For dates beyond ten days:
- Matching freelancers may be shown.
- Availability is explicitly `NOT_SET / UNKNOWN`.
- Never present them as available.

## Full/Half day
Studio selects Full Day or Half Day in the requirement.
Freelancer maintains one or more daily availability intervals according to the implementation decision.
Pricing is displayed according to selected day type.

## Double booking
Before confirmation:
- Check existing confirmed work intervals.
- Reject confirmation if overlapping.
- Recheck transactionally on confirmation to avoid race conditions.

## After confirmation
The relevant availability interval must no longer be selectable for another confirmed work.

## UI
Use a clear 10-day calendar/list.
Available days allow time editing.
Busy and Not Set have distinct visual states.
