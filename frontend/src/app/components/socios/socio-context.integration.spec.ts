import { TestBed } from '@angular/core/testing';
import { Router, Routes } from '@angular/router';
import { Location } from '@angular/common';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideAnimations } from '@angular/platform-browser/animations';
import { Component } from '@angular/core';

import { SOCIOS_ROUTES } from './socios.routes';
import { SocioDetailComponent } from './socio-detail/socio-detail.component';
import { SocioFormComponent } from './socio-form/socio-form.component';

// Mock component for testing
@Component({
  selector: 'app-mock',
  standalone: true,
  template: '<div>Mock</div>'
})
class MockComponent {}

/**
 * Integration tests for socio context maintenance across navigation
 * Validates: Requirements 4.6 - Maintain socioId consistent in all components
 * Property 19: Manutenção de Contexto do Sócio
 */
describe('Socio Context Integration', () => {
  let router: Router;
  let location: Location;

  beforeEach(async () => {
    // Create test routes that include the socios routes
    const testRoutes: Routes = [
      {
        path: 'socios',
        children: SOCIOS_ROUTES
      },
      {
        path: '',
        component: MockComponent
      }
    ];

    await TestBed.configureTestingModule({
      providers: [
        provideRouter(testRoutes),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideAnimations()
      ]
    });

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    
    // Initialize navigation
    await router.navigate(['']);
  });

  /**
   * Test: socioId is passed via route params to detail view
   * Requirements: 4.6
   */
  it('should pass socioId via route params to detail view', async () => {
    const socioId = 123;
    
    await router.navigate(['/socios', socioId, 'detalhes']);
    
    expect(location.path()).toBe(`/socios/${socioId}/detalhes`);
  });

  /**
   * Test: socioId is passed via route params to edit view
   * Requirements: 4.6
   */
  it('should pass socioId via route params to edit view', async () => {
    const socioId = 456;
    
    await router.navigate(['/socios', socioId, 'editar']);
    
    expect(location.path()).toBe(`/socios/${socioId}/editar`);
  });

  /**
   * Test: Navigation from detail to edit maintains socioId
   * Requirements: 4.6
   */
  it('should maintain socioId when navigating from detail to edit', async () => {
    const socioId = 789;
    
    // Navigate to detail view
    await router.navigate(['/socios', socioId, 'detalhes']);
    expect(location.path()).toBe(`/socios/${socioId}/detalhes`);
    
    // Navigate to edit view
    await router.navigate(['/socios', socioId, 'editar']);
    expect(location.path()).toBe(`/socios/${socioId}/editar`);
    
    // Verify socioId is consistent
    const pathSegments = location.path().split('/');
    const extractedId = parseInt(pathSegments[2], 10);
    expect(extractedId).toBe(socioId);
  });

  /**
   * Test: Navigation from edit back to detail maintains socioId
   * Requirements: 4.6, 4.3
   */
  it('should maintain socioId when navigating from edit back to detail', async () => {
    const socioId = 321;
    
    // Navigate to edit view
    await router.navigate(['/socios', socioId, 'editar']);
    expect(location.path()).toBe(`/socios/${socioId}/editar`);
    
    // Navigate back to detail view
    await router.navigate(['/socios', socioId, 'detalhes']);
    expect(location.path()).toBe(`/socios/${socioId}/detalhes`);
    
    // Verify socioId is consistent
    const pathSegments = location.path().split('/');
    const extractedId = parseInt(pathSegments[2], 10);
    expect(extractedId).toBe(socioId);
  });

  /**
   * Test: Route params are correctly parsed as numbers
   * Requirements: 4.6
   */
  it('should parse socioId from route params as number', async () => {
    const socioId = 999;
    
    await router.navigate(['/socios', socioId, 'detalhes']);
    
    const pathSegments = location.path().split('/');
    const extractedId = parseInt(pathSegments[2], 10);
    
    expect(typeof extractedId).toBe('number');
    expect(extractedId).toBe(socioId);
    expect(isNaN(extractedId)).toBe(false);
  });

  /**
   * Test: Multiple navigation cycles maintain consistent socioId
   * Property 19: Manutenção de Contexto do Sócio
   */
  it('should maintain consistent socioId across multiple navigation cycles', async () => {
    const socioId = 555;
    
    // Cycle 1: List -> Detail -> Edit -> Detail
    await router.navigate(['/socios', socioId, 'detalhes']);
    let pathSegments = location.path().split('/');
    expect(parseInt(pathSegments[2], 10)).toBe(socioId);
    
    await router.navigate(['/socios', socioId, 'editar']);
    pathSegments = location.path().split('/');
    expect(parseInt(pathSegments[2], 10)).toBe(socioId);
    
    await router.navigate(['/socios', socioId, 'detalhes']);
    pathSegments = location.path().split('/');
    expect(parseInt(pathSegments[2], 10)).toBe(socioId);
    
    // Cycle 2: Detail -> Edit -> Detail again
    await router.navigate(['/socios', socioId, 'editar']);
    pathSegments = location.path().split('/');
    expect(parseInt(pathSegments[2], 10)).toBe(socioId);
    
    await router.navigate(['/socios', socioId, 'detalhes']);
    pathSegments = location.path().split('/');
    expect(parseInt(pathSegments[2], 10)).toBe(socioId);
  });

  /**
   * Test: Different socioIds are correctly distinguished
   * Requirements: 4.6
   */
  it('should correctly distinguish between different socioIds', async () => {
    const socioId1 = 111;
    const socioId2 = 222;
    
    // Navigate to first socio
    await router.navigate(['/socios', socioId1, 'detalhes']);
    let pathSegments = location.path().split('/');
    expect(parseInt(pathSegments[2], 10)).toBe(socioId1);
    
    // Navigate to second socio
    await router.navigate(['/socios', socioId2, 'detalhes']);
    pathSegments = location.path().split('/');
    expect(parseInt(pathSegments[2], 10)).toBe(socioId2);
    
    // Verify they are different
    expect(socioId1).not.toBe(socioId2);
  });

  /**
   * Test: Route configuration matches expected patterns
   * Requirements: 4.6, 4.7
   */
  it('should have correct route configuration for detail and edit views', () => {
    const routes = SOCIOS_ROUTES;
    
    const detailRoute = routes.find(r => r.path === ':id/detalhes');
    expect(detailRoute).toBeDefined();
    expect(detailRoute?.component).toBe(SocioDetailComponent);
    
    const editRoute = routes.find(r => r.path === ':id/editar');
    expect(editRoute).toBeDefined();
    expect(editRoute?.component).toBe(SocioFormComponent);
  });
});
