# IT Service Desk Management

Full-stack web portal for managing IT service requests.

| Layer | Tech |
|---|---|
| Presentation (UI) | React 18 + Vite |
| Presentation (API) | Spring Boot 3 REST controllers |
| Service layer | `RequestService`, `UserService` (business + authorization rules) |
| Data access layer | Spring Data JPA repositories, H2 (dev) / MySQL |
| Authentication | Username + password (BCrypt, Spring Security form login) **and** Google OAuth 2.0 / OpenID Connect |
| Authorization | Role-based: `USER` and `ADMIN` |

## Features
- **Users:** register with a username/password or sign in with Google, raise / view / edit / delete their own requests, reopen resolved ones, personal dashboard.
- **Admins:** see all requests, filter by status, mark in progress, resolve with a note, admin dashboard.
- Statuses: `OPEN → IN_PROGRESS → RESOLVED → REOPENED`.
- Users can never see or modify other users' requests (checked in the service layer).

## 1. Create Google OAuth credentials
1. Go to https://console.cloud.google.com/ → create/select a project.
2. **APIs & Services → OAuth consent screen**: configure (External), add your Google account as a test user.
3. **Credentials → Create credentials → OAuth client ID → Web application**.
4. **Authorized redirect URI:** `http://localhost:8080/login/oauth2/code/google`
5. Copy the Client ID and Client Secret.

## 2. Run the backend (Java 17+, Maven)
```bash
cd backend
export GOOGLE_CLIENT_ID=xxxx.apps.googleusercontent.com
export GOOGLE_CLIENT_SECRET=xxxx
export ADMIN_EMAILS=you@gmail.com,colleague@company.com   # these Google accounts become ADMIN
# Optional: username/password admin created at startup
export BOOTSTRAP_ADMIN_USER=admin
export BOOTSTRAP_ADMIN_PASSWORD=ChangeMe123!
mvn spring-boot:run
```
(Windows PowerShell: `$env:GOOGLE_CLIENT_ID="..."` etc.)

Backend runs on http://localhost:8080 using a local H2 file database (`backend/data/`).

### Use MySQL instead
```bash
export DB_URL=jdbc:mysql://localhost:3306/servicedesk?createDatabaseIfNotExist=true
export DB_USER=root DB_PASSWORD=secret
```

## 3. Run the frontend (Node 18+)
```bash
cd frontend
npm install
npm run dev
```
Open http://localhost:5173 → **Sign in with Google**.

## REST API
| Method | Path | Who | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | public | `{username, email, name, password}` |
| POST | `/api/login` | public | form fields `username` (or email), `password` |
| POST | `/api/logout` | any | end session |
| GET | `/api/me` | any | current user + role |
| GET/POST | `/api/requests` | user | list own / create |
| GET/PUT/DELETE | `/api/requests/{id}` | owner | view / update / delete |
| POST | `/api/requests/{id}/reopen` | owner | reopen a resolved request |
| GET | `/api/requests/stats` | user | counts by status |
| GET | `/api/admin/requests?status=` | admin | all requests |
| PUT | `/api/admin/requests/{id}/status` | admin | `{status, comment}` |
| GET | `/api/admin/stats` | admin | counts by status |

## Authentication details
- Two ways in, one session: username/password (`/api/login`) or Google (`/oauth2/authorization/google`). Both end up as the same user record, keyed by email.
- Passwords are hashed with BCrypt and never returned by the API. Sign in with either the username or the email.
- Self-registered users are always `USER`. Admin comes from `ADMIN_EMAILS` (Google, email verified by Google) or the `BOOTSTRAP_ADMIN_*` account.
- Anti-takeover rule: if someone self-registered with an email and the real owner later signs in with Google, the unverified password is removed and the account is tied to Google.

## Notes
- Login flow: React → `/oauth2/authorization/google` → Google → `/login/oauth2/code/google` → session cookie → redirect back to React.
- Config for deployment: set `FRONTEND_URL` (CORS + post-login redirect), `VITE_API_URL`, and add your production redirect URI in Google Console. Serve over HTTPS.
- CSRF is disabled because the API is JSON-only with CORS locked to `FRONTEND_URL`; enable CSRF tokens if you host the UI on a different site.
