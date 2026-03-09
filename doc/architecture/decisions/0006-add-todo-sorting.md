# 5. Todo Sorting Strategy

Date: 2026-03-09

## Status

Accepted

## Context

Todos need to be displayed in a logical order: incomplete tasks first (what needs doing), then completed tasks. Within each group, newer tasks are more relevant than older ones.
I need to decide where to implement sorting: client-side (frontend) or server-side (backend database).

## Decision

Implement **client-side sorting** in the frontend (React).

Sort order:
1. Incomplete tasks first (`completed = false` before `completed = true`)
2. Within each group: newest first (descending by ID)

```javascript
const sortTodos = (todoList) => {
  return [...todoList].sort((a, b) => {
    if (a.completed !== b.completed) {
      return a.completed ? 1 : -1;
    }
    return b.id - a.id; // Newest first
  });
};
```

## Alternatives Considered
**Backend database sorting** (`ORDER BY completed ASC, id DESC`): Rejected because it's unnecessary optimization at current scale (<100 todos per user). Database sorting is beneficial for large datasets (1000+ items) or when using pagination, but adds complexity and reduces flexibility for my use case. 

**User-configurable sort preferences** (storing preference in User table): Rejected as premature. No user demand for custom sorting yet. Would require database migration, API endpoints, and UI controls. Can add later if users request it.

**Manual drag-and-drop ordering** (storing `displayOrder` per todo): Rejected as too complex for MVP. Requires drag-and-drop library, handling position updates on every reorder, and edge cases (deleting/inserting items). Consider only if users specifically request manual prioritization.

## Consequences

**Positive:**
- Simple implementation (no backend changes)
- Instant feedback when completing/uncompleting todos (no server round-trip)
- Fast performance at current scale (<100 todos per user)

**Negative:**
- Doesn't scale well beyond ~1000 todos per user
- Not suitable for pagination (each page would sort independently)
- Sorting logic runs on every state change (add/update/delete)

**Migration Path:**
Move to backend sorting (`ORDER BY completed ASC, id DESC`) when:
- Average todos per user exceeds 500
- Adding pagination
- User feedback shows value in drag-and-drop or additional sorting mechanism.

**Trade-off:** Chose simplicity over premature database optimization.