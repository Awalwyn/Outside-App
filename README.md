## Features Implemented

### Authentication System
- **User Registration** - POST '/api/user/register'
  - Email and username uniqueness validation
  - BCrypt password hashing
  - Input validation (email required, username required, password > 8 chars)
  - Returns 201 Created with UserDTO (passwordHash never exposed to client)
  - Returns 409 Conflict if email or username already exists
  - Returns 400 Bad Request for invalid input
 
- **User Login** - POST '/api/user/login'
  - Email and password authentication
  - Bcrypt password verification
  - Returns 200 ok with UserDTO if credentials valid
  - Returns 401 Unauthorized if email doesnt exst or password wrong
  - Generic error message (doesn't leak if email exists)

 ### Architecture
 - **DTO Patterns**: UserDTO for all HTTP responses
 - **Exception Handling**: Service throws exceptions, Controller catches and convers to HTTP responses
 - **Password Security**: BCrypt hashing for passwords
 - **Input Validation**: Server-side validation in controller before calling service

### Testing
- **Unit Tests**: JUnit 5 tests for UserService (testslogic in isolation)
- **Integration Tests**: Tests for UserRepository with actual database (tests JPA query derivation)
- **API Tests**: Postman tests for all endpoints (14 test cases covering success and error scenarios)
  - Registration: valid input, duplicate email, duplicate username, missing fields, invalid password length
  - Login: correct credentials, wrong password, non-existent user, missing fields
  - User retrieval: valid ID, non-existent ID
  - Verified correct HTTP status codes (201, 400, 401, 404, 409) and JSON responses
