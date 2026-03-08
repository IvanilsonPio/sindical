import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { provideRouter } from '@angular/router';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have navigation cards', () => {
    expect(component.navigationCards).toBeDefined();
    expect(component.navigationCards.length).toBe(3);
  });

  it('should have correct navigation card titles', () => {
    const titles = component.navigationCards.map(card => card.title);
    expect(titles).toContain('Sócios');
    expect(titles).toContain('Pagamentos');
    expect(titles).toContain('Arquivos');
  });

  it('should navigate to correct route when card is clicked', () => {
    const navigateSpy = spyOn(router, 'navigate');
    const testRoute = '/socios';
    
    component.navigateTo(testRoute);
    
    expect(navigateSpy).toHaveBeenCalledWith([testRoute]);
  });

  it('should render navigation cards in template', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const cards = compiled.querySelectorAll('.navigation-card');
    
    expect(cards.length).toBe(3);
  });

  it('should display card titles in template', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const cardTitles = compiled.querySelectorAll('.navigation-card h2');
    
    expect(cardTitles.length).toBe(3);
    expect(cardTitles[0].textContent).toContain('Sócios');
    expect(cardTitles[1].textContent).toContain('Pagamentos');
    expect(cardTitles[2].textContent).toContain('Arquivos');
  });
});
