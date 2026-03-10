# Error Interceptor Implementation

## Overview

The error interceptor provides global HTTP error handling for the frontend application, implementing requirements 6.3 and 6.4 from the spec.

## Features

### 1. Network Error Handling (Status 0)

Handles network connectivity issues when the server is unreachable:

```typescript
if (error.status === 0) {
  // Creates user-friendly error response
  message: 'Erro ao comunicar com o servidor. Verifique sua conexão'
}
```

**Use Cases:**
- Server is down
- Network connection lost
- CORS issues
- DNS resolution failures

### 2. Server Error Handling (Status 500+)

Handles all server-side errors with descriptive messages:

```typescript
if (error.status >= 500) {
  // Preserves backend error message if available
  // Otherwise provides default message
  message: 'Erro no servidor. Tente novamente mais tarde'
}
```

**Covered Status Codes:**
- 500 Internal Server Error
- 502 Bad Gateway
- 503 Service Unavailable
- 504 Gateway Timeout

### 3. Session Expiration Handling

Automatically handles expired sessions:

```typescript
if (errorResponse?.errorCode === ErrorCode.SESSION_EXPIRED) {
  localStorage.removeItem('token');
  router.navigate(['/login'], {
    queryParams: { sessionExpired: 'true' }
  });
}
```

### 4. Structured Error Logging

All errors are logged with structured information:

```typescript
console.error('HTTP Error Intercepted:', {
  url: req.url,
  method: req.method,
  status: error.status,
  errorCode: errorResponse?.errorCode,
  message: errorResponse?.message,
  timestamp: new Date().toISOString()
});
```

## Error Flow

```
HTTP Request → Error Occurs → Error Interceptor
                                    ↓
                    ┌───────────────┴───────────────┐
                    ↓                               ↓
            Status 0 (Network)              Status 500+ (Server)
                    ↓                               ↓
        Create friendly error           Preserve backend message
                    ↓                               ↓
                    └───────────────┬───────────────┘
                                    ↓
                        Log structured error
                                    ↓
                        Check for session expired
                                    ↓
                        Re-throw for component
```

## Integration with Error Service

The interceptor works in conjunction with `ErrorService` to provide:

1. **Consistent error messages** across the application
2. **Field-level validation errors** for forms
3. **User-friendly error display** in components

Example usage in components:

```typescript
this.socioService.getSocioDetalhado(id).subscribe({
  next: (socio) => this.socio = socio,
  error: (error: HttpErrorResponse) => {
    // Error already logged by interceptor
    this.error = this.errorService.getErrorMessage(error);
  }
});
```

## Testing

Comprehensive test coverage includes:

- ✅ Network errors (status 0)
- ✅ Server errors (500, 502, 503)
- ✅ Session expiration handling
- ✅ Error logging with structured data
- ✅ Preservation of backend error messages
- ✅ Other HTTP errors (400, 404, 409)

Run tests:
```bash
npm test -- --include='**/error.interceptor.spec.ts'
```

## Requirements Validation

### Requirement 6.3: Error Communication Handling

✅ **Implemented**: Network errors (status 0) are caught and transformed into user-friendly messages:
- "Erro ao comunicar com o servidor. Verifique sua conexão"

### Requirement 6.4: Error Loading Handling

✅ **Implemented**: Server errors (500+) are caught and provide descriptive messages:
- Preserves backend error messages when available
- Provides default message: "Erro no servidor. Tente novamente mais tarde"
- Logs all errors with structured information for debugging

## Configuration

The interceptor is registered in `app.config.ts`:

```typescript
provideHttpClient(
  withInterceptors([
    cacheInterceptor,
    jwtInterceptor,
    errorInterceptor  // ← Global error handling
  ])
)
```

## Error Response Format

All errors follow the standardized `ErrorResponse` interface:

```typescript
interface ErrorResponse {
  errorCode: string;
  message: string;
  timestamp: string;
  path?: string;
  status?: number;
  fieldErrors?: FieldError[];
  details?: Record<string, any>;
}
```

## Best Practices

1. **Always use ErrorService** in components to extract user-friendly messages
2. **Don't suppress errors** - let them propagate to components for proper handling
3. **Log context** - include relevant information in error logs
4. **Provide actionable messages** - tell users what they can do to resolve the issue

## Future Enhancements

- [ ] Send errors to external logging service (e.g., Sentry)
- [ ] Retry logic for transient network errors
- [ ] Offline mode detection and handling
- [ ] Error analytics and monitoring dashboard
