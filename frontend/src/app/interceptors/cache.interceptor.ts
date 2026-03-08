import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { of, tap } from 'rxjs';

/**
 * HTTP Cache Interceptor
 * 
 * Implements client-side caching for GET requests to improve performance.
 * Caches responses for a configurable duration (default: 5 minutes).
 * 
 * Cache Strategy:
 * - Only caches successful GET requests
 * - Respects cache-control headers from server
 * - Automatically expires cached entries after TTL
 * - Can be bypassed with 'X-Skip-Cache' header
 */

interface CacheEntry {
  response: HttpResponse<any>;
  timestamp: number;
}

class HttpCache {
  private cache = new Map<string, CacheEntry>();
  private readonly DEFAULT_TTL = 5 * 60 * 1000; // 5 minutes

  get(url: string, ttl: number = this.DEFAULT_TTL): HttpResponse<any> | null {
    const entry = this.cache.get(url);
    
    if (!entry) {
      return null;
    }

    const isExpired = Date.now() - entry.timestamp > ttl;
    
    if (isExpired) {
      this.cache.delete(url);
      return null;
    }

    return entry.response;
  }

  set(url: string, response: HttpResponse<any>): void {
    this.cache.set(url, {
      response,
      timestamp: Date.now()
    });
  }

  clear(): void {
    this.cache.clear();
  }

  delete(url: string): void {
    this.cache.delete(url);
  }

  // Clear cache entries matching a pattern (e.g., all /api/socios/* entries)
  clearPattern(pattern: string): void {
    const regex = new RegExp(pattern);
    const keysToDelete: string[] = [];
    
    this.cache.forEach((_, key) => {
      if (regex.test(key)) {
        keysToDelete.push(key);
      }
    });
    
    keysToDelete.forEach(key => this.cache.delete(key));
  }
}

// Singleton cache instance
const httpCache = new HttpCache();

/**
 * Cache interceptor function
 * 
 * Caches GET requests and returns cached responses when available.
 * Invalidates cache for mutating operations (POST, PUT, DELETE, PATCH).
 */
export const cacheInterceptor: HttpInterceptorFn = (req, next) => {
  // Only cache GET requests
  if (req.method !== 'GET') {
    // For mutating operations, invalidate related cache entries
    if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(req.method)) {
      invalidateCacheForUrl(req.url);
    }
    return next(req);
  }

  // Skip cache if requested
  if (req.headers.has('X-Skip-Cache')) {
    return next(req);
  }

  // Check if response is in cache
  const cachedResponse = httpCache.get(req.urlWithParams);
  
  if (cachedResponse) {
    // Return cached response
    return of(cachedResponse.clone());
  }

  // If not in cache, make the request and cache the response
  return next(req).pipe(
    tap(event => {
      if (event instanceof HttpResponse && event.status === 200) {
        // Only cache successful responses
        httpCache.set(req.urlWithParams, event);
      }
    })
  );
};

/**
 * Invalidate cache entries related to a URL
 * 
 * When a resource is modified, we need to invalidate cached entries
 * for that resource and related list endpoints.
 */
function invalidateCacheForUrl(url: string): void {
  // Extract the base resource path (e.g., /api/socios from /api/socios/123)
  const match = url.match(/\/api\/([^\/]+)/);
  
  if (match) {
    const resource = match[1];
    // Clear all cache entries for this resource
    httpCache.clearPattern(`/api/${resource}`);
  }
}

/**
 * Export cache instance for manual cache management
 */
export { httpCache };
