# READIFY — Secure Library Management System

## 1. What Is Readify?

Readify is a **backend API** for a library. It runs on a server and lets frontend apps (web or mobile) talk to it over HTTP. The API handles everything a real library needs: user accounts, book catalog, inventory tracking, and loan management.

The word "API" means **Application Programming Interface**. Think of it as a waiter in a restaurant. The frontend (customer) asks for something, and the API (waiter) goes to the kitchen (database) and brings back the result.

**Swagger UI (interactive API tester):** `http://localhost:8081/swagger-ui.html`

---

## 2. Technology Stack — What We Use and Why

| Technology | What It Does | Why We Chose It |
|---|---|---|
| **Java 17** | The programming language | Industry standard, strongly typed, runs everywhere |
| **Spring Boot 3.2.6** | The main framework | Makes building APIs fast, handles boilerplate code |
| **Spring Security** | Handles login and permissions | Most trusted security framework for Java |
| **Spring Data JPA** | Talks to the database | Lets us write Java objects instead of SQL queries |
| **PostgreSQL** | The database | Powerful, free, handles complex queries well |
| **JWT (JSON Web Token)** | Keeps users logged in | Stateless — server does not need to remember sessions |
| **OAuth2** | Google Sign-In | Users can log in with their Google account |
| **BCrypt** | Encrypts passwords | Industry standard — passwords are never stored as plain text |
| **Spring Boot Mail** | Sends emails | Needed for password reset and email verification |
| **SpringDoc OpenAPI** | Auto-generates API docs | Swagger UI lets anyone test the API in a browser |
| **Maven** | Manages dependencies | Downloads and manages all libraries the project needs |
| **Hibernate** | JPA implementation | Translates Java objects into database tables automatically |

---

## 3. Architecture — How the System Is Built

### 3.1 The Big Picture

We use a **layered architecture**. This means the code is split into layers, and each layer only talks to the layer below it. This makes the code easier to understand, test, and change.

```
Client (Frontend App)
        │
        ▼
Security Layer (checks who you are and what you can do)
        │
        ▼
Controller Layer (receives requests, sends responses)
        │
        ▼
Service Layer (contains the business rules and logic)
        │
        ▼
Repository Layer (talks to the database)
        │
        ▼
PostgreSQL Database (stores all the data)
```

**Why layered architecture?** If we need to change the database, we only change the repository layer. If we need to change a business rule, we only change the service layer. The other layers are not affected. This is called **separation of concerns**.

### 3.2 Package Structure — How the Code Is Organized

Each feature of the system lives in its own package (folder). This is called **feature-based packaging**. It makes it easy to find code related to a specific feature.

```
com.readify
│
├── auth/          → Everything about user accounts (login, register, password reset)
├── book/          → Everything about books (catalog, search)
├── author/        → Everything about authors
├── category/      → Everything about book categories
├── inventory/     → Everything about physical book copies
├── loan/          → Everything about borrowing and returning books
├── oauth2/        → Everything about Google Sign-In
├── security/      → Everything about JWT tokens and security filters
├── common/        → Shared code used by all features (errors, enums, config)
└── config/        → App-wide configuration (Swagger setup)
```

Inside each feature package, the code follows the same pattern:

```
feature/
├── controller/   → Receives HTTP requests, returns HTTP responses
├── dto/          → Data Transfer Objects (shapes of data going in and out)
├── entity/       → Database table definitions (Java classes that become tables)
├── repository/   → Database query methods
└── service/      → Business logic (the rules of the system)
```

**What is a DTO?** A Data Transfer Object is a simple class that defines what data the API accepts or returns. For example, `LoginRequest` has an `email` and `password` field. This is different from the `User` entity which has 15+ fields. We do not want to expose all user data in every request, so DTOs give us control over what goes in and out.

---

## 4. Database Design — How Data Is Stored

### 4.1 Tables and Relationships

The database has **6 tables** that are connected to each other through **foreign keys**. A foreign key is a column that points to a row in another table. This is how tables are linked.

```
┌─────────┐       ┌─────────┐       ┌───────────┐
│  users   │       │  books   │       │  authors  │
└────┬─────┘       └────┬─────┘       └─────┬─────┘
     │                  │                   │
     │            author_id   category_id   │
     │            (points to authors)  (points to categories)
     │                  │                   │
     │                  ▼                   ▼
     │            ┌───────────┐
     │            │ categories│
     │            └───────────┘
     │
     │ 1 user can have many loans
     │
     ├──────────────┐
     │              │
     ▼              ▼
┌──────────┐   ┌──────────────┐
│  loans    │   │ book_copies  │
└──────────┘   └──────────────┘
     │              │
     │   loan points to a specific copy
     └──────────────┘
```

### 4.2 What Each Table Stores

**users** — Stores every person who has an account
- Identity: name, email, password (encrypted)
- Account state: status (active/inactive/suspended), enabled, role (member/librarian/admin/super admin)
- Google login: googleId, avatarUrl, googleLinked, provider
- Email verification: emailVerified, emailVerificationToken, emailVerificationExpires
- Password reset: passwordResetToken, passwordResetExpires
- The `email` column is unique — no two users can have the same email
- The `password` column uses `@JsonIgnore` so passwords are never included in API responses

**books** — Stores the book catalog (not physical copies)
- Info: title, isbn (unique), description, publishedYear, language, publisher
- Relationships: points to one `author` and one `category`

**authors** — Stores author information
- Info: fullName, biography

**categories** — Stores book genres/sections
- Info: name (unique), description

**book_copies** — Stores each physical copy of a book
- One book can have many copies (a library might own 3 copies of "Harry Potter")
- Info: copyCode (unique barcode), conditionNote, shelfLocation, status
- Status can be: AVAILABLE, BORROWED, RESERVED, LOST, DAMAGED, UNDER_REPAIR
- Points to one `book` (which book this copy is)

**loans** — Records every borrowing transaction
- Links a `user` (the borrower) to a `book_copy` (the specific copy they borrowed)
- Info: issueDate, dueDate, returnDate, status, renewalCount
- Status can be: ACTIVE, RETURNED, OVERDUE, RENEWED, CANCELLED

### 4.3 Why This Design?

- **Separating books from copies**: A book is the concept (title, author). A copy is the physical item you can hold. This matters because a library has multiple copies of the same book, and each copy has its own status.
- **Separating authors and categories**: Instead of storing author name inside the book table, we store it in its own table. This avoids duplication. If an author's name changes, we update it in one place, not in every book.
- **Loan as a separate table**: Loans connect users to copies. This lets us track the full history of who borrowed what and when.

---

## 5. Authentication — How Users Prove Who They Are

### 5.1 Two Ways to Log In

**Way 1: Email and Password (traditional)**

1. User sends email and password to `/api/v1/auth/login`
2. Spring Security checks if the email and password match a user in the database
3. If they match, the server creates a **JWT token** and sends it back
4. The frontend saves this token and includes it in every future request
5. The server reads the token on every request to know who the user is

**Way 2: Google Sign-In (OAuth2)**

1. User clicks "Sign in with Google"
2. The browser goes to Google's login page
3. User logs into Google and approves the app
4. Google sends the user back to our API with their profile info
5. Our API finds the user in the database (or creates a new account)
6. The server creates a **JWT token** and sends it back
7. From this point on, it works exactly like Way 1

### 5.2 What Is a JWT Token?

JWT stands for **JSON Web Token**. It is a string that contains encoded data about the user. Think of it like a hotel keycard:

- When you check in (login), the hotel gives you a keycard (JWT)
- The keycard proves you are a guest
- You show the keycard every time you enter a room (make an API request)
- The keycard expires after a certain time (our tokens last 24 hours)
- You do not need to go back to the front desk every time (the server does not need to store sessions)

**Why JWT instead of sessions?** Traditional web apps use sessions — the server remembers who you are in its memory. But this does not scale well. If you have 10 servers, they all need to share session data. With JWT, the token itself contains all the info the server needs. Any server can verify the token without looking up a session. This is called **stateless authentication**.

**How is the token secure?** The token is signed with a secret key using the HS256 algorithm. Only the server knows the secret key. If someone changes the token, the signature breaks and the server rejects it. The token is not encrypted (anyone can read it), but it cannot be tampered with.

### 5.3 What Is BCrypt?

BCrypt is a **password hashing algorithm**. It turns a password like "mypassword123" into a string like "$2a$10$N9qo8uLOickgx2ZMRZoMy...". This process is one-way — you cannot reverse it back to the original password.

**Why not just encrypt passwords?** Encryption is two-way — you can decrypt it back. If someone steals the encryption key, they get all passwords. Hashing is one-way. Even if someone steals the database, they cannot get the original passwords.

BCrypt also adds a **salt** — random data added before hashing. This means two users with the same password get different hashes. An attacker cannot use "rainbow tables" (pre-computed lists of common passwords and their hashes).

### 5.4 How the Security Filter Chain Works

Every HTTP request goes through a **chain of filters** before it reaches our code. Each filter checks something different:

1. **CORS filter** — Checks if the request comes from an allowed origin
2. **OAuth2 filters** — Handle the Google Sign-In redirect flow
3. **JWT filter** — Reads the token from the `Authorization` header, validates it, and sets the user identity
4. **Session filter** — Ensures no HTTP session is created (we are stateless)
5. **Authorization filter** — Checks if the authenticated user has the right role for this endpoint

If any filter fails, the request is rejected with an error (401 Unauthorized or 403 Forbidden).

### 5.5 Public vs Protected Endpoints

Not every endpoint requires a login. We follow the **principle of least privilege** — give access only where needed:

**Public (no login needed):**
- Login and register endpoints
- Password reset and email verification endpoints
- Browsing books, authors, categories (read-only)
- Google OAuth2 flow endpoints
- Swagger API documentation

**Protected (login required):**
- Creating, updating, or deleting books/authors/categories (admin only)
- Borrowing, returning, renewing books (member or above)
- Viewing loans (your own loans as member, all loans as librarian+)
- Managing users (admin only)

---

## 6. Role-Based Access Control — Who Can Do What

### 6.1 The Four Roles

We have a **hierarchy of roles**. Each higher role includes all the permissions of the roles below it.

| Role | Who They Are | What They Can Do |
|---|---|---|
| **MEMBER** | Regular library user | Borrow, return, renew books. View their own loans. |
| **LIBRARIAN** | Library staff | Everything a member can do, PLUS: manage book copies, view all loans, view overdue loans, issue books on behalf of members. |
| **ADMIN** | System administrator | Everything a librarian can do, PLUS: add/edit/delete books, authors, categories. View all users. Change user roles. |
| **SUPER_ADMIN** | Highest authority | Everything an admin can do, PLUS: full unrestricted access. |

### 6.2 How Roles Are Enforced

We use Spring Security's `@PreAuthorize` annotation on controller methods. For example:

```java
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public ResponseEntity<?> createBook(...) { ... }
```

This means: only users with the ADMIN or SUPER_ADMIN role can call this method. If a MEMBER tries to create a book, they get a **403 Forbidden** error.

**Why not just check roles manually?** Using annotations keeps the security rules right next to the code they protect. It is easy to read and hard to forget. Spring Security handles the checking automatically.

### 6.3 The Theory Behind RBAC

**RBAC** stands for Role-Based Access Control. The idea is simple: instead of giving permissions to individual users, you give permissions to roles, and then assign roles to users. This scales much better. If you have 500 members, you do not need to set 500 permission entries — you just give them the MEMBER role.

---

## 7. Business Rules — The Logic of the Library

### 7.1 Loan Rules

The `LoanService` enforces these rules when a user borrows a book:

| Rule | Value | Why |
|---|---|---|
| Default loan period | 14 days | Standard library practice — enough time to read a book |
| Default renewal period | 7 days | Shorter than the original loan — encourages returning |
| Maximum active loans | 3 per member | Prevents hoarding — fair access for all members |
| Maximum renewals | 2 per loan | Stops someone from keeping a book forever |
| Custom loan days | 1–30 | Flexibility for special cases, but bounded to prevent abuse |
| Overdue loans cannot be renewed | — | Penalty for late returns — encourages on-time returns |
| Returned loans cannot be changed | — | Data integrity — history should not be modified |
| Member must be ACTIVE | — | Suspended or inactive users lose borrowing privileges |
| Book copy must be AVAILABLE | — | Cannot borrow a copy that someone else already has |

### 7.2 Loan Lifecycle

A loan goes through different statuses during its life:

```
User borrows a book
        │
        ▼
    ACTIVE (the book is with the user)
        │
   ┌────┴────────────────────┐
   │                         │
   │ Renew (up to 2 times)   │ Due date passes
   │                         │
   ▼                         ▼
RENEWED                   OVERDUE
   │                         │
   │    User returns book     │
   │                         │
   ▼                         ▼
RETURNED                 RETURNED
```

When a book is borrowed, the copy status changes from AVAILABLE to BORROWED. When it is returned, the copy goes back to AVAILABLE. This is how we know which books are on the shelf.

### 7.3 Password Reset Flow

This is how a user resets their password when they forget it:

1. User enters their email on the "Forgot Password" page
2. The API generates a **random token** (UUID) and saves it to the user's record with a 1-hour expiry
3. The API sends an email containing a link with the token (e.g., `/reset-password?token=abc123`)
4. User clicks the link and enters a new password
5. The API checks: does this token exist? Is it not expired? If yes, update the password
6. The token is cleared from the database so it cannot be used again

**Why use a token instead of just sending a new password?**
- Sending passwords by email is insecure — emails can be intercepted
- A token is temporary and single-use — even if someone gets the email later, the token has expired
- The user chooses their own new password — we never generate passwords for them

### 7.4 Email Verification Flow

When a user registers, their email is not verified yet. They need to prove they own the email:

1. User registers — `emailVerified` is set to `false`
2. The API generates a verification token (UUID, expires in 24 hours)
3. The API sends an email with a verification link
4. User clicks the link
5. The API sets `emailVerified = true` and `enabled = true`
6. If the token expired, the API generates a new one and resends the email automatically

**Why verify email?** It proves the user actually owns the email they signed up with. This prevents spam accounts and ensures we can reach the user for important notifications.

### 7.5 Google OAuth2 Flow

OAuth2 is a protocol that lets users log in through a trusted provider (Google) without creating a new password:

1. Frontend asks our API for the Google authorization URL
2. Frontend redirects the user to Google's login page
3. User logs into Google and clicks "Allow" to share their profile with our app
4. Google redirects back to our API with the user's profile data (name, email, picture)
5. Our API checks: does this email already exist in our database?
   - **Yes** → Generate a JWT token for the existing user
   - **No** → Create a new user account automatically, then generate a JWT token
6. Frontend receives the JWT token and uses it for all future requests

**Why OAuth2?** Users do not need to create and remember yet another password. Google handles the security. We get verified email addresses. It reduces friction in the signup process.

---

## 8. Error Handling — How the API Deals with Problems

### 8.1 The Exception Hierarchy

We have a custom set of exceptions that map directly to HTTP status codes. This gives clear, consistent error responses:

```
ReadifyException (the base exception)
├── ResourceNotFoundException     → 404 (you asked for something that does not exist)
├── DuplicateResourceException    → 409 (you tried to create something that already exists)
├── BusinessRuleException         → 400 (you broke a business rule)
└── UnauthorizedAccessException   → 403 (you do not have permission)
```

### 8.2 The Global Exception Handler

We use a `@RestControllerAdvice` class called `GlobalExceptionHandler`. This is like a **catch-all net**. When any controller throws an exception, this class catches it and turns it into a proper HTTP response.

**Why a global handler?** Without it, every controller method would need try-catch blocks. That is repetitive and error-prone. With a global handler, controllers just throw exceptions, and the handler converts them to consistent error responses.

### 8.3 What Errors Look Like

Every error response follows the same format:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with id: 5",
  "path": "/api/v1/books/5"
}
```

For validation errors, there is an extra `details` array:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "path": "/api/v1/auth/register",
  "details": ["Email is required", "Password must be at least 6 characters"]
}
```

### 8.4 HTTP Status Codes We Use

| Code | Name | Meaning |
|---|---|---|
| 200 | OK | The request succeeded |
| 400 | Bad Request | The request data is invalid or a business rule was broken |
| 401 | Unauthorized | You are not logged in (missing or invalid JWT token) |
| 403 | Forbidden | You are logged in but do not have the right role |
| 404 | Not Found | The requested resource does not exist |
| 409 | Conflict | The resource already exists (duplicate email, ISBN, etc.) |
| 500 | Internal Server Error | Something unexpected went wrong on the server |

---

## 9. API Endpoints — Every Route in the System

All endpoints start with `/api/v1`. The `v1` means "version 1" — if we make breaking changes in the future, we can create `/api/v2` without breaking existing clients.

### 9.1 Authentication (`/api/v1/auth`)

| Method | Path | Who Can Use It | What It Does |
|---|---|---|---|
| POST | `/login` | Anyone | Log in with email and password, get a JWT token |
| POST | `/register` | Anyone | Create a new member account |
| GET | `/me` | Logged-in users | Get your profile info |
| POST | `/forgot-password` | Anyone | Request a password reset email |
| POST | `/reset-password` | Anyone | Set a new password using the reset token |

### 9.2 Email Verification (`/api/v1/auth`)

| Method | Path | Who Can Use It | What It Does |
|---|---|---|---|
| GET | `/verify-email?token=xxx` | Anyone | Verify your email address |
| POST | `/resend-verification` | Anyone | Get a new verification email |
| GET | `/verification-status?email=xxx` | Anyone | Check if an email is verified |

### 9.3 Google OAuth2 (`/api/v1/auth/oauth2`)

| Method | Path | Who Can Use It | What It Does |
|---|---|---|---|
| GET | `/google/authorization-url` | Anyone | Get the Google login URL |
| GET | `/success` | Google callback | Handles successful Google login |
| POST | `/link-google` | Logged-in users | Connect a Google account to your account |
| POST | `/unlink-google` | Logged-in users | Remove the Google connection from your account |
| POST | `/resend-verification` | Logged-in users | Resend verification email |

### 9.4 Books (`/api/v1/books`)

| Method | Path | Who Can Use It | What It Does |
|---|---|---|---|
| GET | `/` | Anyone | List all books |
| GET | `/{id}` | Anyone | Get one book by its ID |
| GET | `/search?query=xxx` | Anyone | Search books by title or ISBN |
| GET | `/category/{categoryId}` | Anyone | Get all books in a category |
| GET | `/author/{authorId}` | Anyone | Get all books by an author |
| GET | `/available` | Anyone | Get books that have available copies |
| POST | `/` | Admin+ | Add a new book |
| PUT | `/{id}` | Admin+ | Update a book's info |
| DELETE | `/{id}` | Admin+ | Remove a book |

### 9.5 Authors (`/api/v1/authors`)

| Method | Path | Who Can Use It | What It Does |
|---|---|---|---|
| GET | `/` | Anyone | List all authors |
| GET | `/{id}` | Anyone | Get one author by ID |
| POST | `/` | Admin+ | Add a new author |
| PUT | `/{id}` | Admin+ | Update an author |
| DELETE | `/{id}` | Admin+ | Remove an author |

### 9.6 Categories (`/api/v1/categories`)

| Method | Path | Who Can Use It | What It Does |
|---|---|---|---|
| GET | `/` | Anyone | List all categories |
| GET | `/{id}` | Anyone | Get one category by ID |
| POST | `/` | Admin+ | Add a new category |
| PUT | `/{id}` | Admin+ | Update a category |
| DELETE | `/{id}` | Admin+ | Remove a category |

### 9.7 Book Copies (Inventory)

| Method | Path | Who Can Use It | What It Does |
|---|---|---|---|
| GET | `/api/v1/books/{bookId}/copies` | Anyone | List all copies of a book |
| GET | `/api/v1/books/{bookId}/copies/available` | Anyone | List available copies |
| POST | `/api/v1/copies` | Librarian+ | Add a new physical copy |
| PATCH | `/api/v1/copies/{id}/status` | Librarian+ | Change a copy's status |

### 9.8 Loans (`/api/v1/loans`)

| Method | Path | Who Can Use It | What It Does |
|---|---|---|---|
| POST | `/borrow` | Member+ | Borrow a book copy |
| GET | `/` | Librarian+ | List all loans in the system |
| GET | `/me` | Member+ | List your own loans |
| GET | `/overdue` | Librarian+ | List all overdue loans |
| POST | `/{loanId}/return` | Member+ | Return a borrowed book |
| POST | `/{loanId}/renew` | Member+ | Extend a loan's due date |

### 9.9 User Management (`/api/v1/users`)

| Method | Path | Who Can Use It | What It Does |
|---|---|---|---|
| GET | `/` | Admin+ | List all users |
| PATCH | `/{id}/role` | Admin+ | Change a user's role |

### 9.10 System

| Method | Path | Who Can Use It | What It Does |
|---|---|---|---|
| GET | `/api/v1/` | Anyone | Welcome message |
| GET | `/api/v1/version` | Anyone | API version info |
| GET | `/api/v1/health` | Anyone | Health check |

---

## 10. Data Transfer Objects (DTOs) — The Shape of Data

DTOs define what data the API accepts and returns. They are important because:

- **Security**: We never expose the full `User` entity (which includes the password hash) in responses
- **Validation**: We can add rules like "email must be valid" or "password must be at least 6 characters"
- **Clarity**: The frontend knows exactly what fields to send and expect

### 10.1 Auth DTOs

**LoginRequest** — What you send to log in
- `email` (required, must be a valid email format)
- `password` (required)

**RegisterRequest** — What you send to create an account
- `fullName` (required)
- `email` (required, valid email)
- `password` (required, at least 6 characters)

**AuthResponse** — What you get back after login
- `token` (the JWT token), `email`, `role`, `provider`, `emailVerified`, `message`, `avatarUrl`

**ForgotPasswordRequest** — What you send to request a reset
- `email` (required, valid email)

**ResetPasswordRequest** — What you send to set a new password
- `resetToken` (required)
- `newPassword` (required, at least 6 characters)

**UpdateUserRoleRequest** — What an admin sends to change a role
- `role` (required, one of: ROLE_MEMBER, ROLE_LIBRARIAN, ROLE_ADMIN, ROLE_SUPER_ADMIN)

### 10.2 Book DTOs

**BookRequest** — What you send to create or update a book
- `title` (required), `isbn` (required, unique), `description`, `publishedYear`, `language`, `publisher`
- `authorId` (required), `categoryId` (required)

### 10.3 Loan DTOs

**BorrowBookRequest** — What you send to borrow a book
- `bookCopyId` (required — which copy you want)
- `memberId` (optional — staff can borrow on behalf of a member)
- `loanDays` (optional, 1–30, default 14)

**RenewLoanRequest** — What you send to renew a loan
- `extraDays` (optional, 1–30, default 7)

---

## 11. Enumerations — Fixed Lists of Values

Enumerations (enums) are fixed sets of options. They prevent invalid data. For example, a loan status can only be one of the five defined values — nothing else.

### 11.1 UserRole — Who the user is in the system

| Value | Meaning |
|---|---|
| `ROLE_MEMBER` | A regular library member who borrows books |
| `ROLE_LIBRARIAN` | Staff who manage copies and view all loans |
| `ROLE_ADMIN` | Administrator who manages books and users |
| `ROLE_SUPER_ADMIN` | Highest level with full access |

### 11.2 UserStatus — The state of a user's account

| Value | Meaning |
|---|---|
| `ACTIVE` | Account is working normally |
| `INACTIVE` | Account is not active |
| `SUSPENDED` | Account is blocked (e.g., too many overdue books) |

### 11.3 AuthProvider — How the user registered

| Value | Meaning |
|---|---|
| `LOCAL` | Registered with email and password directly |
| `GOOGLE` | Registered through Google Sign-In |
| `FACEBOOK` | Reserved for future use |
| `GITHUB` | Reserved for future use |

### 11.4 BookCopyStatus — The state of a physical copy

| Value | Meaning |
|---|---|
| `AVAILABLE` | On the shelf, ready to be borrowed |
| `BORROWED` | Currently checked out by someone |
| `RESERVED` | Saved for a specific member |
| `LOST` | Cannot be found |
| `DAMAGED` | In bad condition, not available |
| `UNDER_REPAIR` | Being fixed |

### 11.5 LoanStatus — The state of a borrowing transaction

| Value | Meaning |
|---|---|
| `ACTIVE` | The book is currently with the borrower |
| `RETURNED` | The book has been returned |
| `OVERDUE` | The due date has passed and the book is not returned |
| `RENEWED` | The loan period has been extended |
| `CANCELLED` | The loan was cancelled before the book was taken |

---

## 12. Configuration — How the App Is Set Up

### 12.1 Key Settings

| Setting | Value | Why |
|---|---|---|
| Server port | 8081 | Avoids conflict with common ports like 8080 |
| Database | PostgreSQL on port 1201 | Separate port avoids conflict with default 5432 |
| JPA ddl-auto | `update` | Hibernate auto-creates/updates tables when the app starts |
| JWT expiry | 24 hours | Long enough for a work session, short enough for security |
| JWT algorithm | HS256 | Standard HMAC-SHA256 — secure and widely supported |
| Password encoder | BCrypt | Industry standard for password hashing |
| Session policy | STATELESS | No server-side sessions — everything is in the JWT |
| SMTP | Gmail on port 587 | Sends emails using TLS encryption |
| Password reset expiry | 1 hour | Short window for security |
| Email verification expiry | 24 hours | Reasonable time for users to check their email |

### 12.2 Maven Dependencies — What Libraries We Need

| Library | Why We Need It |
|---|---|
| `spring-boot-starter-web` | Lets us build REST APIs with an embedded Tomcat server |
| `spring-boot-starter-security` | Adds authentication and authorization |
| `spring-boot-starter-data-jpa` | Lets us map Java objects to database tables (ORM) |
| `spring-boot-starter-validation` | Validates request data (e.g., @NotBlank, @Email) |
| `spring-boot-starter-oauth2-client` | Handles the Google Sign-In flow |
| `spring-boot-starter-oauth2-resource-server` | Lets the API act as an OAuth2 resource server |
| `spring-boot-starter-mail` | Sends emails via SMTP |
| `springdoc-openapi-starter-webmvc-ui` | Auto-generates Swagger API documentation |
| `postgresql` | The database driver for PostgreSQL |
| `jjwt-api`, `jjwt-impl`, `jjwt-jackson` | Creates and validates JWT tokens |
| `h2` | In-memory database for testing |

---

## 13. How to Run the Project

### 13.1 Prerequisites
- Java 17 or higher
- Maven 3.9 or higher
- PostgreSQL running on port 1201
- Database name: `postgres`, user: `postgres`

### 13.2 Start the Application
```bash
cd D:\Documents\Readify
mvn spring-boot:run
```

### 13.3 Verify It Works
- Application runs at: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- API Docs (JSON): `http://localhost:8081/v3/api-docs`

### 13.4 Quick Test Commands
```bash
# Register a new user
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"fullName\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"password123\"}"

# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\",\"password\":\"password123\"}"

# Get your profile (replace TOKEN with the JWT from login)
curl http://localhost:8081/api/v1/auth/me \
  -H "Authorization: Bearer TOKEN"
```
