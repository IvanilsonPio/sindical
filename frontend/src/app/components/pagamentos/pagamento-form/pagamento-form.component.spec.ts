import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';

import { PagamentoFormComponent } from './pagamento-form.component';
import { PagamentoService } from '../../../services/pagamento.service';
import { SocioService } from '../../../services/socio.service';
import { Socio } from '../../../models/socio.model';
import { Pagamento } from '../../../models/pagamento.model';
import { StatusSocio, StatusPagamento } from '../../../models/enums';

describe('PagamentoFormComponent', () => {
  let component: PagamentoFormComponent;
  let fixture: ComponentFixture<PagamentoFormComponent>;
  let mockPagamentoService: jasmine.SpyObj<PagamentoService>;
  let mockSocioService: jasmine.SpyObj<SocioService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  const mockSocios: Socio[] = [
    {
      id: 1,
      nome: 'João Silva',
      cpf: '12345678901',
      matricula: 'MAT001',
      status: StatusSocio.ATIVO,
      criadoEm: '2024-01-01T00:00:00',
      atualizadoEm: '2024-01-01T00:00:00'
    },
    {
      id: 2,
      nome: 'Maria Santos',
      cpf: '98765432109',
      matricula: 'MAT002',
      status: StatusSocio.ATIVO,
      criadoEm: '2024-01-01T00:00:00',
      atualizadoEm: '2024-01-01T00:00:00'
    }
  ];

  const mockPagamento: Pagamento = {
    id: 1,
    socioId: 1,
    socioNome: 'João Silva',
    socioCpf: '12345678901',
    valor: 50.00,
    mes: 1,
    ano: 2024,
    dataPagamento: '2024-01-15',
    numeroRecibo: 'REC-2024-001',
    status: StatusPagamento.PAGO,
    criadoEm: '2024-01-15T10:00:00',
    atualizadoEm: '2024-01-15T10:00:00'
  };

  beforeEach(async () => {
    mockPagamentoService = jasmine.createSpyObj('PagamentoService', [
      'registrarPagamento',
      'gerarRecibo'
    ]);
    mockSocioService = jasmine.createSpyObj('SocioService', ['listarSocios']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        PagamentoFormComponent,
        ReactiveFormsModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: PagamentoService, useValue: mockPagamentoService },
        { provide: SocioService, useValue: mockSocioService },
        { provide: Router, useValue: mockRouter },
        { provide: MatSnackBar, useValue: mockSnackBar }
      ]
    }).compileComponents();

    mockSocioService.listarSocios.and.returnValue(
      of({ 
        content: mockSocios, 
        totalElements: 2, 
        totalPages: 1, 
        number: 0, 
        size: 1000,
        first: true,
        last: true
      })
    );

    fixture = TestBed.createComponent(PagamentoFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    const today = new Date();
    const currentMonth = today.getMonth() + 1;
    const currentYear = today.getFullYear();

    expect(component.pagamentoForm.get('mes')?.value).toBe(currentMonth);
    expect(component.pagamentoForm.get('ano')?.value).toBe(currentYear);
    expect(component.pagamentoForm.get('dataPagamento')?.value).toBeTruthy();
  });

  it('should load socios on init', () => {
    expect(mockSocioService.listarSocios).toHaveBeenCalledWith({ page: 0, size: 1000 });
    expect(component.socios.length).toBe(2);
  });

  it('should filter socios based on search term', () => {
    component.socios = mockSocios;
    const filtered = component['filterSocios']('João');
    expect(filtered.length).toBe(1);
    expect(filtered[0].nome).toBe('João Silva');
  });

  it('should filter socios by CPF', () => {
    component.socios = mockSocios;
    const filtered = component['filterSocios']('987');
    expect(filtered.length).toBe(1);
    expect(filtered[0].cpf).toBe('98765432109');
  });

  it('should set socioId when socio is selected', () => {
    component.onSocioSelected(mockSocios[0]);
    expect(component.pagamentoForm.get('socioId')?.value).toBe(1);
    expect(component.selectedSocio).toEqual(mockSocios[0]);
  });

  it('should clear socioId when user types in search field', () => {
    component.pagamentoForm.patchValue({ socioId: 1, socioSearch: 'test' });
    component.onSocioInputChange();
    expect(component.pagamentoForm.get('socioId')?.value).toBeNull();
    expect(component.selectedSocio).toBeUndefined();
  });

  it('should display socio name and CPF in autocomplete', () => {
    const display = component.displaySocio(mockSocios[0]);
    expect(display).toContain('João Silva');
    expect(display).toContain('123.456.789-01');
  });

  it('should validate required fields', () => {
    component.pagamentoForm.patchValue({
      socioSearch: '',
      socioId: null,
      mes: null,
      ano: null,
      dataPagamento: null,
      valor: null
    });

    expect(component.pagamentoForm.valid).toBeFalse();
    expect(component.pagamentoForm.get('socioSearch')?.hasError('required')).toBeTrue();
    expect(component.pagamentoForm.get('socioId')?.hasError('required')).toBeTrue();
    expect(component.pagamentoForm.get('mes')?.hasError('required')).toBeTrue();
    expect(component.pagamentoForm.get('ano')?.hasError('required')).toBeTrue();
    expect(component.pagamentoForm.get('dataPagamento')?.hasError('required')).toBeTrue();
    expect(component.pagamentoForm.get('valor')?.hasError('required')).toBeTrue();
  });

  it('should validate valor minimum', () => {
    component.pagamentoForm.patchValue({ valor: 0 });
    expect(component.pagamentoForm.get('valor')?.hasError('min')).toBeTrue();
  });

  it('should validate mes range', () => {
    component.pagamentoForm.patchValue({ mes: 0 });
    expect(component.pagamentoForm.get('mes')?.hasError('min')).toBeTrue();

    component.pagamentoForm.patchValue({ mes: 13 });
    expect(component.pagamentoForm.get('mes')?.hasError('max')).toBeTrue();
  });

  it('should validate ano minimum', () => {
    component.pagamentoForm.patchValue({ ano: 2019 });
    expect(component.pagamentoForm.get('ano')?.hasError('min')).toBeTrue();
  });

  it('should not submit if form is invalid', () => {
    // Make form invalid by clearing required fields
    component.pagamentoForm.patchValue({
      socioSearch: '',
      socioId: null,
      mes: null,
      ano: null,
      dataPagamento: null,
      valor: null
    });

    component.onSubmit();

    expect(component.pagamentoForm.invalid).toBeTrue();
    expect(mockPagamentoService.registrarPagamento).not.toHaveBeenCalled();
  });

  it('should not submit if socio is not selected', () => {
    // Form is valid except socioId is null
    component.pagamentoForm.patchValue({
      socioSearch: 'João',  // Has text but no actual socio selected
      socioId: null,
      mes: 1,
      ano: 2024,
      dataPagamento: new Date(),
      valor: 50
    });

    component.onSubmit();

    expect(mockPagamentoService.registrarPagamento).not.toHaveBeenCalled();
  });

  it('should register payment and generate receipt on successful submit', (done) => {
    const blob = new Blob(['test'], { type: 'application/pdf' });
    mockPagamentoService.registrarPagamento.and.returnValue(of(mockPagamento));
    mockPagamentoService.gerarRecibo.and.returnValue(of(blob));

    const testDate = new Date('2024-01-15T12:00:00Z');
    component.pagamentoForm.patchValue({
      socioSearch: mockSocios[0],
      socioId: 1,
      mes: 1,
      ano: 2024,
      dataPagamento: testDate,
      valor: 50,
      observacoes: 'Teste'
    });
    component.selectedSocio = mockSocios[0];

    component.onSubmit();

    // Check that loading is set to true initially
    setTimeout(() => {
      // The date formatting depends on timezone, so we just check it was called
      expect(mockPagamentoService.registrarPagamento).toHaveBeenCalledWith(
        jasmine.objectContaining({
          socioId: 1,
          mes: 1,
          ano: 2024,
          valor: 50,
          observacoes: 'Teste'
        })
      );

      expect(mockPagamentoService.gerarRecibo).toHaveBeenCalledWith(1);
      done();
    }, 100);
  });

  it('should handle payment registration error', () => {
    const error = { error: { message: 'Erro ao registrar' }, status: 500 };
    mockPagamentoService.registrarPagamento.and.returnValue(throwError(() => error));

    component.pagamentoForm.patchValue({
      socioSearch: mockSocios[0],
      socioId: 1,
      mes: 1,
      ano: 2024,
      dataPagamento: new Date(),
      valor: 50
    });
    component.selectedSocio = mockSocios[0];

    component.onSubmit();

    expect(component.loading).toBeFalse();
  });

  it('should handle duplicate payment error', () => {
    const error = { status: 409 };
    mockPagamentoService.registrarPagamento.and.returnValue(throwError(() => error));

    component.pagamentoForm.patchValue({
      socioSearch: mockSocios[0],
      socioId: 1,
      mes: 1,
      ano: 2024,
      dataPagamento: new Date(),
      valor: 50
    });
    component.selectedSocio = mockSocios[0];

    component.onSubmit();

    expect(mockPagamentoService.registrarPagamento).toHaveBeenCalled();
  });

  it('should navigate to payments list on cancel', () => {
    component.onCancel();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/pagamentos']);
  });

  it('should format CPF correctly', () => {
    const formatted = component['formatCpf']('12345678901');
    expect(formatted).toBe('123.456.789-01');
  });

  it('should format date to ISO correctly', () => {
    const date = new Date('2024-01-15T12:00:00Z');
    const formatted = component['formatDateToISO'](date);
    // Check that it's a valid ISO date format (YYYY-MM-DD)
    expect(formatted).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    expect(formatted).toContain('2024-01');
  });

  it('should return correct error messages', () => {
    component.pagamentoForm.get('socioSearch')?.markAsTouched();
    component.pagamentoForm.get('socioSearch')?.setErrors({ required: true });
    expect(component.getErrorMessage('socioSearch')).toBe('Este campo é obrigatório');

    component.pagamentoForm.get('valor')?.markAsTouched();
    component.pagamentoForm.get('valor')?.setErrors({ min: { min: 0.01 } });
    expect(component.getErrorMessage('valor')).toBe('O valor deve ser maior que zero');

    component.pagamentoForm.get('mes')?.markAsTouched();
    component.pagamentoForm.get('mes')?.setErrors({ min: { min: 1 } });
    expect(component.getErrorMessage('mes')).toBe('Mês inválido');

    component.pagamentoForm.get('ano')?.markAsTouched();
    component.pagamentoForm.get('ano')?.setErrors({ min: { min: 2020 } });
    expect(component.getErrorMessage('ano')).toBe('Ano deve ser 2020 ou posterior');
  });

  it('should get month name correctly', () => {
    expect(component.getMesNome(1)).toBe('Janeiro');
    expect(component.getMesNome(12)).toBe('Dezembro');
    expect(component.getMesNome(13)).toBe('');
  });

  it('should generate years array from 2020 to current year', () => {
    const currentYear = new Date().getFullYear();
    expect(component.anos.length).toBe(currentYear - 2020 + 1);
    expect(component.anos[0]).toBe(2020);
    expect(component.anos[component.anos.length - 1]).toBe(currentYear);
  });
});
