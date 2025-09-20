# Netflix Spring Boot API Documentation

## Overview

This document provides comprehensive documentation for the Spring Boot REST API demonstration project. The API showcases production-grade Spring Boot web development concepts for engineers transitioning from C/C++ to Java Spring ecosystem.

## Base URL

```
http://localhost:8080
```

## Authentication

Currently, no authentication is required. In production, you would typically use:
- JWT tokens
- OAuth 2.0
- API keys
- Session-based authentication

## Content Type

All requests and responses use `application/json` content type.

## API Endpoints

### User Management API

#### 1. Get All Users

**Endpoint:** `GET /api/v1/users`

**Description:** Retrieves all users from the system.

**Request:**
```http
GET /api/v1/users HTTP/1.1
Host: localhost:8080
Content-Type: application/json
```

**Response:**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@netflix.com"
    },
    {
      "id": 2,
      "name": "Jane Smith",
      "email": "jane.smith@netflix.com"
    }
  ],
  "timestamp": 1703123456789
}
```

**Status Codes:**
- `200 OK` - Success
- `500 Internal Server Error` - Server error

#### 2. Get User by ID

**Endpoint:** `GET /api/v1/users/{id}`

**Description:** Retrieves a specific user by their ID.

**Path Parameters:**
- `id` (Long, required) - User ID

**Request:**
```http
GET /api/v1/users/1 HTTP/1.1
Host: localhost:8080
Content-Type: application/json
```

**Response (Success):**
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@netflix.com"
  },
  "timestamp": 1703123456789
}
```

**Response (Not Found):**
```json
{
  "success": false,
  "message": "User not found",
  "data": null,
  "timestamp": 1703123456789
}
```

**Status Codes:**
- `200 OK` - Success
- `404 Not Found` - User not found
- `500 Internal Server Error` - Server error

#### 3. Create User

**Endpoint:** `POST /api/v1/users`

**Description:** Creates a new user in the system.

**Request Body:**
```json
{
  "name": "New User",
  "email": "new.user@netflix.com"
}
```

**Request:**
```http
POST /api/v1/users HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "name": "New User",
  "email": "new.user@netflix.com"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 1234567890,
    "name": "New User",
    "email": "new.user@netflix.com"
  },
  "timestamp": 1703123456789
}
```

**Response (Validation Error):**
```json
{
  "success": false,
  "message": "Name is required",
  "data": null,
  "timestamp": 1703123456789
}
```

**Status Codes:**
- `201 Created` - User created successfully
- `400 Bad Request` - Validation error
- `500 Internal Server Error` - Server error

#### 4. Update User

**Endpoint:** `PUT /api/v1/users/{id}`

**Description:** Updates an existing user.

**Path Parameters:**
- `id` (Long, required) - User ID

**Request Body:**
```json
{
  "name": "Updated User",
  "email": "updated@netflix.com"
}
```

**Request:**
```http
PUT /api/v1/users/1 HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "name": "Updated User",
  "email": "updated@netflix.com"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": 1,
    "name": "Updated User",
    "email": "updated@netflix.com"
  },
  "timestamp": 1703123456789
}
```

**Status Codes:**
- `200 OK` - User updated successfully
- `404 Not Found` - User not found
- `500 Internal Server Error` - Server error

#### 5. Delete User

**Endpoint:** `DELETE /api/v1/users/{id}`

**Description:** Deletes a user from the system.

**Path Parameters:**
- `id` (Long, required) - User ID

**Request:**
```http
DELETE /api/v1/users/1 HTTP/1.1
Host: localhost:8080
Content-Type: application/json
```

**Response (Success):**
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null,
  "timestamp": 1703123456789
}
```

**Status Codes:**
- `204 No Content` - User deleted successfully
- `404 Not Found` - User not found
- `500 Internal Server Error` - Server error

#### 6. Search Users

**Endpoint:** `GET /api/v1/users/search`

**Description:** Searches users by name and/or email.

**Query Parameters:**
- `name` (String, optional) - Name to search for
- `email` (String, optional) - Email to search for

**Request:**
```http
GET /api/v1/users/search?name=John&email=netflix.com HTTP/1.1
Host: localhost:8080
Content-Type: application/json
```

**Response:**
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@netflix.com"
    }
  ],
  "timestamp": 1703123456789
}
```

**Status Codes:**
- `200 OK` - Success
- `500 Internal Server Error` - Server error

### Spring Boot Annotations Demo API

#### 1. HTTP Method Examples

**Endpoints:**
- `GET /api/v1/annotations/get-example`
- `POST /api/v1/annotations/post-example`
- `PUT /api/v1/annotations/put-example`
- `DELETE /api/v1/annotations/delete-example`

**Description:** Demonstrates different HTTP methods and their usage.

#### 2. Path Variable Example

**Endpoint:** `GET /api/v1/annotations/path-variable/{id}`

**Description:** Demonstrates path variable extraction.

**Path Parameters:**
- `id` (Long, required) - Example ID

#### 3. Query Parameter Example

**Endpoint:** `GET /api/v1/annotations/request-param`

**Description:** Demonstrates query parameter extraction.

**Query Parameters:**
- `name` (String, required) - Example name
- `age` (Integer, optional) - Example age
- `city` (String, optional, default: "unknown") - Example city

#### 4. Request Body Example

**Endpoint:** `POST /api/v1/annotations/request-body`

**Description:** Demonstrates request body deserialization.

**Request Body:**
```json
{
  "key": "value",
  "data": "example"
}
```

#### 5. Header Example

**Endpoint:** `GET /api/v1/annotations/request-header`

**Description:** Demonstrates HTTP header extraction.

**Headers:**
- `User-Agent` (required) - Browser user agent
- `Authorization` (optional) - Authorization header

#### 6. Cookie Example

**Endpoint:** `GET /api/v1/annotations/cookie-value`

**Description:** Demonstrates cookie value extraction.

**Cookies:**
- `sessionId` (optional) - Session identifier

#### 7. Response Status Example

**Endpoint:** `GET /api/v1/annotations/response-status`

**Description:** Demonstrates custom HTTP status codes.

**Response:** `202 Accepted`

#### 8. Multipart Form Data Example

**Endpoint:** `POST /api/v1/annotations/request-part`

**Description:** Demonstrates file upload and multipart form data.

**Form Data:**
- `file` (MultipartFile, required) - File to upload
- `data` (String, required) - Additional data

#### 9. Multiple HTTP Methods Example

**Endpoint:** `GET|POST /api/v1/annotations/multiple-methods`

**Description:** Demonstrates handling multiple HTTP methods.

## Error Handling

### Error Response Format

All error responses follow this format:

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": 1703123456789
}
```

### Common Error Scenarios

1. **Validation Errors (400 Bad Request)**
   - Missing required fields
   - Invalid data format
   - Constraint violations

2. **Not Found Errors (404 Not Found)**
   - Resource not found
   - Invalid ID

3. **Server Errors (500 Internal Server Error)**
   - Unexpected server errors
   - Database connection issues
   - Internal processing errors

## Testing the API

### Using curl

#### Get all users
```bash
curl -X GET http://localhost:8080/api/v1/users
```

#### Get user by ID
```bash
curl -X GET http://localhost:8080/api/v1/users/1
```

#### Create user
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"name":"New User","email":"new.user@netflix.com"}'
```

#### Update user
```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated User","email":"updated@netflix.com"}'
```

#### Delete user
```bash
curl -X DELETE http://localhost:8080/api/v1/users/1
```

#### Search users
```bash
curl -X GET "http://localhost:8080/api/v1/users/search?name=John"
```

### Using Postman

1. Import the API collection (if available)
2. Set the base URL to `http://localhost:8080`
3. Use the provided examples for each endpoint

### Using HTTPie

#### Get all users
```bash
http GET localhost:8080/api/v1/users
```

#### Create user
```bash
http POST localhost:8080/api/v1/users name="New User" email="new.user@netflix.com"
```

## Health Checks

### Application Health

**Endpoint:** `GET /actuator/health`

**Description:** Returns the health status of the application.

**Response:**
```json
{
  "status": "UP"
}
```

### Application Info

**Endpoint:** `GET /actuator/info`

**Description:** Returns application information.

**Response:**
```json
{
  "app": {
    "name": "spring-framework-demo",
    "version": "1.0.0"
  }
}
```

## CORS Configuration

The API is configured to allow cross-origin requests from any origin for demonstration purposes. In production, you should restrict this to specific domains.

**Configuration:**
- Allowed Origins: `*`
- Allowed Methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- Allowed Headers: `*`
- Max Age: `3600` seconds

## Rate Limiting

Currently, no rate limiting is implemented. In production, you should consider implementing:
- Request rate limiting
- IP-based throttling
- User-based quotas
- API key rate limits

## Monitoring and Logging

### Logging

The application uses Spring Boot's default logging configuration with:
- Console output
- Log levels: INFO, WARN, ERROR
- Structured logging for better monitoring

### Metrics

Spring Boot Actuator provides various metrics:
- HTTP request metrics
- JVM metrics
- Application metrics
- Custom business metrics

## Security Considerations

### Current Implementation
- No authentication required
- CORS enabled for all origins
- No input validation beyond basic checks

### Production Recommendations
1. **Authentication & Authorization**
   - Implement JWT or OAuth 2.0
   - Role-based access control
   - API key authentication

2. **Input Validation**
   - Comprehensive input validation
   - SQL injection prevention
   - XSS protection

3. **Security Headers**
   - HTTPS enforcement
   - Security headers (HSTS, CSP, etc.)
   - CORS restrictions

4. **Rate Limiting**
   - Request throttling
   - DDoS protection
   - Resource quotas

## Performance Considerations

### Current Implementation
- In-memory data storage
- No caching
- No database optimization

### Production Recommendations
1. **Database**
   - Use proper database (PostgreSQL, MySQL, etc.)
   - Connection pooling
   - Query optimization

2. **Caching**
   - Redis for session storage
   - Application-level caching
   - CDN for static content

3. **Monitoring**
   - Application Performance Monitoring (APM)
   - Database monitoring
   - Infrastructure monitoring

## Deployment

### Local Development
```bash
mvn spring-boot:run
```

### Production Deployment
```bash
mvn clean package
java -jar target/spring-framework-demo-1.0.0.jar
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY target/spring-framework-demo-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Support

For questions or issues with this API:
1. Check the application logs
2. Verify the service is running on port 8080
3. Ensure all required dependencies are installed
4. Contact the Netflix SDE-2 team

---

**Author**: Netflix SDE-2 Team  
**Version**: 1.0.0  
**Last Updated**: 2024
