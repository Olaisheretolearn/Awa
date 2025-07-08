# Roommate App – Backend Overview

This is the backend of a roommate management application designed to coordinate shared living activities such as tasks, bills, and communication.  
A companion blog documenting challenges and development notes is available at:  
👉 [melancholy-mornings.netlify.app](https://melancholy-mornings.netlify.app/)

## Features Implemented

- **Functional and Non-Functional Requirements** were defined before implementation.
- **Entity-Relationship Diagram** (ERD) designed using Lucidchart.
- **Core Modules Implemented**:
  - `User`: Registration, login, and user-room relationships.
  - `Room`: Room creation, joining via invite code.
  - `Task`: CRUD for tasks/chores, assignment, completion tracking, recurrence.
  - `ShoppingItem`: List creation, item updates, mark as bought, search.
  - `Bill`: Create, update, delete bills; mark as paid; split among users.
  - `Message`: In-app messaging between room members with basic reactions and attachments.
- **Error Handling**: Centralized exception handling using `@ControllerAdvice`.

## Status

- Backend development complete.(pending security impl)
- Frontend implementation planned.
- Additional features under consideration include:
  - Room-wide mood forecasting.
  - Optional MBTI-based roommate profiling.
