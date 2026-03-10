import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { of, Subject } from 'rxjs';
import { SocioDetailComponent } from './socio-detail.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { SocioService } from '../../../services/socio.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('SocioDetailComponent', () => {
  let component: SocioDetailComponent;
  let fixture: ComponentFixture<SocioDetailComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: any;
  let mockSocioService: jasmine.SpyObj<SocioService>;
  let routerEventsSubject: Subject<any>;

  beforeEach(async () => {
    routerEventsSubject = new Subject();
    mockRouter = jasmine.createSpyObj('Router', ['navigate'], {
      events: routerEventsSubject.asObservable()
    });
    mockActivatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue('1')
        }
      }
    };
    mockSocioService = jasmine.createSpyObj('SocioService', ['getSocioDetalhado']);
    mockSocioService.getSocioDetalhado.and.returnValue(of({
      id: 1,
      nome: 'Test Socio',
      cpf: '123.456.789-00',
      matricula: 'MAT001',
      status: 'ATIVO',
      pagamentos: [],
      arquivos: [],
      historico: []
    } as any));

    await TestBed.configureTestingModule({
      imports: [SocioDetailComponent, NoopAnimationsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: SocioService, useValue: mockSocioService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SocioDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should extract socioId from route params on init', () => {
    expect(component.socioId).toBe(1);
  });

  it('should load socio data on init', () => {
    expect(mockSocioService.getSocioDetalhado).toHaveBeenCalledWith(1);
    expect(component.socio).toBeTruthy();
  });

  it('should navigate to edit page when navigateToEdit is called', () => {
    component.socioId = 1;
    component.navigateToEdit();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/socios', 1, 'editar']);
  });

  it('should navigate to socios list when close is called', () => {
    component.close();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/socios']);
  });

  it('should reload socio data when returning from edit (navigation event)', () => {
    // Reset the spy call count
    mockSocioService.getSocioDetalhado.calls.reset();
    
    // Simulate navigation back to detail page
    routerEventsSubject.next(new NavigationEnd(1, '/socios/1/detalhes', '/socios/1/detalhes'));
    
    // Should reload socio data
    expect(mockSocioService.getSocioDetalhado).toHaveBeenCalledWith(1);
  });

  it('should not reload socio data for navigation to other pages', () => {
    // Reset the spy call count
    mockSocioService.getSocioDetalhado.calls.reset();
    
    // Simulate navigation to different page
    routerEventsSubject.next(new NavigationEnd(1, '/socios', '/socios'));
    
    // Should not reload socio data
    expect(mockSocioService.getSocioDetalhado).not.toHaveBeenCalled();
  });

  it('should reload socio data when arquivos change', () => {
    // Reset the spy call count
    mockSocioService.getSocioDetalhado.calls.reset();
    
    // Call the arquivos changed handler
    component.onArquivosChanged();
    
    // Should reload socio data
    expect(mockSocioService.getSocioDetalhado).toHaveBeenCalledWith(1);
  });

  it('should unsubscribe from navigation events on destroy', () => {
    const subscription = (component as any).navigationSubscription;
    spyOn(subscription, 'unsubscribe');
    
    component.ngOnDestroy();
    
    expect(subscription.unsubscribe).toHaveBeenCalled();
  });

  it('should display empty message when socio has no payments', () => {
    component.socio = {
      id: 1,
      nome: 'Test Socio',
      cpf: '123.456.789-00',
      matricula: 'MAT001',
      status: 'ATIVO',
      pagamentos: [],
      arquivos: []
    } as any;
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    const emptyMessages = compiled.querySelectorAll('.empty-message p');
    const paymentMessage = Array.from(emptyMessages).find((el: any) => 
      el.textContent.includes('Nenhum pagamento registrado')
    );
    
    expect(paymentMessage).toBeTruthy();
  });

  it('should not display empty message when socio has payments', () => {
    component.socio = {
      id: 1,
      nome: 'Test Socio',
      cpf: '123.456.789-00',
      matricula: 'MAT001',
      status: 'ATIVO',
      pagamentos: [
        {
          id: 1,
          mes: 1,
          ano: 2024,
          valor: 100.00,
          dataPagamento: '2024-01-15',
          numeroRecibo: 'REC001'
        }
      ],
      arquivos: []
    } as any;
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    const paymentsList = compiled.querySelector('.pagamentos-list');
    
    expect(paymentsList).toBeTruthy();
  });

  describe('History Display', () => {
    it('should display empty message when socio has no history', () => {
      component.socio = {
        id: 1,
        nome: 'Test Socio',
        cpf: '123.456.789-00',
        matricula: 'MAT001',
        status: 'ATIVO',
        pagamentos: [],
        arquivos: [],
        historico: []
      } as any;
      fixture.detectChanges();

      const compiled = fixture.nativeElement;
      const emptyMessages = compiled.querySelectorAll('.empty-message p');
      const historyMessage = Array.from(emptyMessages).find((el: any) => 
        el.textContent.includes('Nenhuma alteração registrada')
      );
      
      expect(historyMessage).toBeTruthy();
    });

    it('should display history items when socio has history', () => {
      component.socio = {
        id: 1,
        nome: 'Test Socio',
        cpf: '123.456.789-00',
        matricula: 'MAT001',
        status: 'ATIVO',
        pagamentos: [],
        arquivos: [],
        historico: [
          {
            id: 1,
            socioId: 1,
            usuario: 'admin',
            dataHora: '2024-01-15T10:30:00',
            operacao: 'UPDATE',
            camposAlterados: {
              'nome': {
                nomeCampo: 'nome',
                valorAnterior: 'Old Name',
                valorNovo: 'New Name'
              }
            }
          }
        ]
      } as any;
      fixture.detectChanges();

      const compiled = fixture.nativeElement;
      const historicoList = compiled.querySelector('.historico-list');
      
      expect(historicoList).toBeTruthy();
      expect(compiled.querySelector('.historico-item')).toBeTruthy();
    });

    it('should return correct operation label', () => {
      expect(component.getOperacaoLabel('CREATE')).toBe('Criação');
      expect(component.getOperacaoLabel('UPDATE')).toBe('Atualização');
      expect(component.getOperacaoLabel('DELETE')).toBe('Exclusão');
      expect(component.getOperacaoLabel('UNKNOWN')).toBe('UNKNOWN');
    });

    it('should convert camposAlterados object to array', () => {
      const camposAlterados = {
        'nome': {
          nomeCampo: 'nome',
          valorAnterior: 'Old Name',
          valorNovo: 'New Name'
        },
        'email': {
          nomeCampo: 'email',
          valorAnterior: 'old@email.com',
          valorNovo: 'new@email.com'
        }
      };

      const result = component.getChangedFieldsArray(camposAlterados);
      
      expect(result.length).toBe(2);
      expect(result[0].nomeCampo).toBe('nome');
      expect(result[1].nomeCampo).toBe('email');
    });

    it('should return empty array for null camposAlterados', () => {
      const result = component.getChangedFieldsArray(null as any);
      expect(result).toEqual([]);
    });

    it('should return correct field label', () => {
      expect(component.getFieldLabel('nome')).toBe('Nome');
      expect(component.getFieldLabel('cpf')).toBe('CPF');
      expect(component.getFieldLabel('email')).toBe('Email');
      expect(component.getFieldLabel('unknownField')).toBe('unknownField');
    });
  });
});
