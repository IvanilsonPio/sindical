import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { cacheInterceptor, httpCache } from './cache.interceptor';

describe('cacheInterceptor', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([cacheInterceptor])),
        provideHttpClientTesting()
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    httpCache.clear(); // Clear cache before each test
  });

  afterEach(() => {
    httpTesting.verify();
    httpCache.clear();
  });

  it('should cache GET requests', () => {
    const testUrl = '/api/socios/1';
    const testData = { id: 1, nome: 'Test Socio' };

    // First request - should hit the server
    httpClient.get(testUrl).subscribe(data => {
      expect(data).toEqual(testData);
    });

    const req1 = httpTesting.expectOne(testUrl);
    expect(req1.request.method).toBe('GET');
    req1.flush(testData);

    // Second request - should return cached response (no server call)
    httpClient.get(testUrl).subscribe(data => {
      expect(data).toEqual(testData);
    });

    // No additional request should be made (cached)
    httpTesting.expectNone(testUrl);
  });

  it('should not cache non-GET requests', () => {
    const testUrl = '/api/socios';
    const testData = { nome: 'New Socio' };

    // POST request should not be cached
    httpClient.post(testUrl, testData).subscribe();

    const req = httpTesting.expectOne(testUrl);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, ...testData });
  });

  it('should skip cache when X-Skip-Cache header is present', () => {
    const testUrl = '/api/socios/1';
    const testData = { id: 1, nome: 'Test Socio' };

    // Request with skip cache header
    httpClient.get(testUrl, {
      headers: { 'X-Skip-Cache': 'true' }
    }).subscribe();

    const req = httpTesting.expectOne(testUrl);
    expect(req.request.method).toBe('GET');
    req.flush(testData);
  });

  it('should invalidate cache on POST request', () => {
    const getUrl = '/api/socios';
    const postUrl = '/api/socios';
    const testData = [{ id: 1, nome: 'Test Socio' }];
    const newData = { nome: 'New Socio' };

    // First GET request - cache the response
    httpClient.get(getUrl).subscribe();
    let req = httpTesting.expectOne(getUrl);
    req.flush(testData);

    // POST request - should invalidate cache
    httpClient.post(postUrl, newData).subscribe();
    req = httpTesting.expectOne(postUrl);
    req.flush({ id: 2, ...newData });

    // Next GET request should hit server again (cache invalidated)
    httpClient.get(getUrl).subscribe();
    req = httpTesting.expectOne(getUrl);
    expect(req.request.method).toBe('GET');
    req.flush([...testData, { id: 2, ...newData }]);
  });

  it('should invalidate cache on PUT request', () => {
    const getUrl = '/api/socios/1';
    const putUrl = '/api/socios/1';
    const testData = { id: 1, nome: 'Test Socio' };
    const updatedData = { id: 1, nome: 'Updated Socio' };

    // First GET request - cache the response
    httpClient.get(getUrl).subscribe();
    let req = httpTesting.expectOne(getUrl);
    req.flush(testData);

    // PUT request - should invalidate cache
    httpClient.put(putUrl, updatedData).subscribe();
    req = httpTesting.expectOne(putUrl);
    req.flush(updatedData);

    // Next GET request should hit server again (cache invalidated)
    httpClient.get(getUrl).subscribe();
    req = httpTesting.expectOne(getUrl);
    expect(req.request.method).toBe('GET');
    req.flush(updatedData);
  });

  it('should invalidate cache on DELETE request', () => {
    const getUrl = '/api/socios';
    const deleteUrl = '/api/socios/1';
    const testData = [
      { id: 1, nome: 'Test Socio 1' },
      { id: 2, nome: 'Test Socio 2' }
    ];

    // First GET request - cache the response
    httpClient.get(getUrl).subscribe();
    let req = httpTesting.expectOne(getUrl);
    req.flush(testData);

    // DELETE request - should invalidate cache
    httpClient.delete(deleteUrl).subscribe();
    req = httpTesting.expectOne(deleteUrl);
    req.flush({});

    // Next GET request should hit server again (cache invalidated)
    httpClient.get(getUrl).subscribe();
    req = httpTesting.expectOne(getUrl);
    expect(req.request.method).toBe('GET');
    req.flush([testData[1]]);
  });

  it('should only cache successful responses (200)', () => {
    const testUrl = '/api/socios/999';

    // Request that returns 404
    httpClient.get(testUrl).subscribe({
      error: () => {
        // Expected error
      }
    });
    const req = httpTesting.expectOne(testUrl);
    req.flush('Not found', { status: 404, statusText: 'Not Found' });

    // Next request should hit server again (error not cached)
    httpClient.get(testUrl).subscribe({
      error: () => {
        // Expected error
      }
    });
    const req2 = httpTesting.expectOne(testUrl);
    expect(req2.request.method).toBe('GET');
    req2.flush('Not found', { status: 404, statusText: 'Not Found' });
  });
});

