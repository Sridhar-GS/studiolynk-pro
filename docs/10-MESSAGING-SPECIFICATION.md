# Messaging Specification

## Scope
Requirement-specific text chat between Studio and Freelancer.

## Transport
WebSocket/STOMP.

## Conversation
One work requirement can have a conversation between the Studio and each requested Freelancer.

Messages:
- id
- requirement id
- conversation id
- sender
- content
- timestamp
- read state

## Availability
Messaging is available after a request is sent, allowing negotiation before acceptance.

## Privacy
Message contents are private to conversation participants.
Private event-person information must not be injected into the conversation automatically before confirmation.

## UI
- Conversation list
- Message history
- Composer
- Sending state
- Connection state
- Unread indicator
- Timestamp
