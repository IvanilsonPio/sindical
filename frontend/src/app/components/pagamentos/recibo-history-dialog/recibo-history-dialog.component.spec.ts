import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';

import { ReciboHistoryDialogComponent } from './recibo-history-dialog.component';
import { PagamentoService } from '../../../services/pagamento.service';
import { Pagamento } from '../../../models/pagamento.model';
import { StatusPagamento } from '../../../models/enums';

describe('ReciboHistoryDialogComponent', () => {
  let component: ReciboHistoryDialogComponent;
  let fixture: ComponentFixture<ReciboHistoryDialogComponent>;
  let pagamentoService: jasmine.SpyObj<PagamentoService>;
  let dialogRef: jasmine.SpyObj<MatDialogRef<ReciboHistoryDialogComponent>>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  const mockDialogData = {
    socioId: 1,
    socioNome: 'João Silva'
  };

  const mockRecibos: Pagamento[] = [
    {
      id: 1,
      socioId: 1,
      socioNome: 'João Silva',
      socioCpf: '12345678901',
      valor: 50.00,
      mes: 12,
      ano: 2025,
      dataPagamento: '2025-12-15',
      numeroRecibo: 'REC-2025-001',
      status: StatusPagamento.PAGO,
      criadoEm: '2025-12-15T10:00:00',
      atualizadoEm: '2025-12-15T10:00:00'
    },
    {
      id: 2,
      socioId: 1,
      socioNome: 'João Silva',
      socioCpf: '12345678901',
      valor: 50.00,
      mes: 11,
      ano: 2025,
      dataPagamento: '2025-11-15',
      numeroRecibo: 'REC-2025-002',
      status: StatusPagamento.PAGO,
      criadoEm: '2025-11-15T10:00:00',
      atualizadoEm: '2025-11-15T10:00:00'
    }
  ];

  beforeEach(async () => {
    const pagamentoServiceSpy = jasmine.createSpyObj('PagamentoService', [
      'listarRecibosPorSocio',
      'gerarRecibo',
      'gerarReciboSegundaVia'
    ]);
    const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        ReciboHistoryDialogComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: PagamentoService, useValue: pagamentoServiceSpy },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: MAT_DIALOG_DATA, useValue: mockDialogData }
      ]
    }).compileComponents();

    pagamentoService = TestBed.inject(PagamentoService) as jasmine.SpyObj<PagamentoService>;
    dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<ReciboHistoryDialogComponent>>;
    snackBar = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;
    
    fixture = TestBed.createComponent(ReciboHistoryDialogComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load recibos on init', () => {
    pagamentoService.listarRecibosPorSocio.and.returnValue(of(mockRecibos));
    
    fixture.detectChanges();
    
    expect(pagamentoService.listarRecibosPorSocio).toHaveBeenCalledWith(1);
    expect(component.recibos.length).toBe(2);
    expect(component.loading).toBeFalse();
  });

  it('should sort recibos by year and month descending', () => {
    pagamentoService.listarRecibosPorSocio.and.returnValue(of(mockRecibos));
    
    fixture.detectChanges();
    
    expect(component.recibos[0].mes).toBe(12);
    expect(component.recibos[1].mes).toBe(11);
  });

  it('should handle error when loading recibos', () => {
    pagamentoService.listarRecibosPorSocio.and.returnValue(
      throwError(() => new Error('Network error'))
    );
    
    fixture.detectChanges();
    
    expect(component.loading).toBeFalse();
    expect(component.recibos.length).toBe(0);
  });

  it('should view recibo', () => {
    const mockBlob = new Blob(['pdf content'], { type: 'application/pdf' });
    pagamentoService.gerarRecibo.and.returnValue(of(mockBlob));
    spyOn(window, 'open');
    
    component.viewRecibo(mockRecibos[0]);
    
    expect(pagamentoService.gerarRecibo).toHaveBeenCalledWith(1);
    expect(window.open).toHaveBeenCalled();
  });

  it('should download recibo', () => {
    const mockBlob = new Blob(['pdf content'], { type: 'application/pdf' });
    pagamentoService.gerarRecibo.and.returnValue(of(mockBlob));
    const link = document.createElement('a');
    spyOn(document, 'createElement').and.returnValue(link);
    spyOn(link, 'click');
    
    component.downloadRecibo(mockRecibos[0]);
    
    expect(pagamentoService.gerarRecibo).toHaveBeenCalledWith(1);
    expect(link.download).toBe('recibo-REC-2025-001.pdf');
    expect(link.click).toHaveBeenCalled();
  });

  it('should reprint recibo as segunda via', () => {
    const mockBlob = new Blob(['pdf content'], { type: 'application/pdf' });
    pagamentoService.gerarReciboSegundaVia.and.returnValue(of(mockBlob));
    const link = document.createElement('a');
    spyOn(document, 'createElement').and.returnValue(link);
    spyOn(link, 'click');
    
    component.reprintRecibo(mockRecibos[0]);
    
    expect(pagamentoService.gerarReciboSegundaVia).toHaveBeenCalledWith(1);
    expect(link.download).toBe('recibo-REC-2025-001-2via.pdf');
    expect(link.click).toHaveBeenCalled();
  });

  it('should format valor correctly', () => {
    const formatted = component.formatValor(50.00);
    expect(formatted).toContain('50');
  });

  it('should format periodo correctly', () => {
    const formatted = component.formatPeriodo(12, 2025);
    expect(formatted).toBe('Dezembro/2025');
  });

  it('should close dialog', () => {
    component.close();
    expect(dialogRef.close).toHaveBeenCalled();
  });
});
