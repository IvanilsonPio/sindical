import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';

import { ArquivoManagerComponent } from './arquivo-manager.component';
import { ArquivoService } from '../../../services/arquivo.service';
import { Arquivo } from '../../../models/arquivo.model';

describe('ArquivoManagerComponent', () => {
  let component: ArquivoManagerComponent;
  let arquivoService: jasmine.SpyObj<ArquivoService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;
  let dialog: jasmine.SpyObj<MatDialog>;

  const mockArquivos: Arquivo[] = [
    {
      id: 1,
      socioId: 1,
      socioNome: 'João Silva',
      nomeOriginal: 'documento.pdf',
      nomeArquivo: 'abc123.pdf',
      tipoConteudo: 'application/pdf',
      tamanho: 1024000,
      tamanhoFormatado: '1.00 MB',
      criadoEm: '2024-01-15T10:30:00'
    },
    {
      id: 2,
      socioId: 1,
      socioNome: 'João Silva',
      nomeOriginal: 'foto.jpg',
      nomeArquivo: 'def456.jpg',
      tipoConteudo: 'image/jpeg',
      tamanho: 512000,
      tamanhoFormatado: '500.00 KB',
      criadoEm: '2024-01-16T14:20:00'
    }
  ];

  beforeEach(() => {
    arquivoService = jasmine.createSpyObj('ArquivoService', [
      'listarArquivos',
      'downloadArquivo',
      'excluirArquivo'
    ]);

    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    dialog = jasmine.createSpyObj('MatDialog', ['open']);

    component = new ArquivoManagerComponent(arquivoService, snackBar, dialog);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load arquivos on init when socioId is provided', () => {
    component.socioId = 1;
    arquivoService.listarArquivos.and.returnValue(of(mockArquivos));

    component.ngOnInit();

    expect(arquivoService.listarArquivos).toHaveBeenCalledWith(1);
    expect(component.arquivos).toEqual(mockArquivos);
    expect(component.loading).toBe(false);
  });

  it('should not load arquivos on init when socioId is not provided', () => {
    component.ngOnInit();

    expect(arquivoService.listarArquivos).not.toHaveBeenCalled();
  });

  it('should handle error when loading arquivos', () => {
    component.socioId = 1;
    arquivoService.listarArquivos.and.returnValue(throwError(() => new Error('Error')));

    component.carregarArquivos();

    expect(component.loading).toBe(false);
    expect(component.arquivos).toEqual([]);
    expect(snackBar.open).toHaveBeenCalled();
  });

  it('should reload arquivos after upload complete', () => {
    component.socioId = 1;
    arquivoService.listarArquivos.and.returnValue(of(mockArquivos));

    component.onUploadComplete(mockArquivos);

    expect(arquivoService.listarArquivos).toHaveBeenCalledWith(1);
  });

  it('should download arquivo', () => {
    const mockBlob = new Blob(['test'], { type: 'application/pdf' });
    arquivoService.downloadArquivo.and.returnValue(of(mockBlob));
    
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    spyOn(window.URL, 'revokeObjectURL');
    
    const link = document.createElement('a');
    spyOn(document, 'createElement').and.returnValue(link);
    spyOn(link, 'click');

    component.downloadArquivo(mockArquivos[1]); // Use image file (id: 2)

    expect(arquivoService.downloadArquivo).toHaveBeenCalledWith(2);
    expect(link.download).toBe('foto.jpg');
    expect(link.click).toHaveBeenCalled();
    expect(snackBar.open).toHaveBeenCalled();
  });

  it('should handle error when downloading arquivo', () => {
    arquivoService.downloadArquivo.and.returnValue(throwError(() => new Error('Error')));

    component.downloadArquivo(mockArquivos[1]); // Use image file (id: 2)

    expect(arquivoService.downloadArquivo).toHaveBeenCalledWith(2);
    expect(snackBar.open).toHaveBeenCalled();
  });

  it('should delete arquivo after confirmation', () => {
    component.socioId = 1;
    
    // Mock dialog to return true (confirmed)
    const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
    dialogRefSpy.afterClosed.and.returnValue(of(true));
    dialog.open.and.returnValue(dialogRefSpy);
    
    arquivoService.excluirArquivo.and.returnValue(of(void 0));
    arquivoService.listarArquivos.and.returnValue(of([]));

    component.excluirArquivo(mockArquivos[1]); // Use image file (id: 2)

    expect(dialog.open).toHaveBeenCalled();
    expect(arquivoService.excluirArquivo).toHaveBeenCalledWith(2);
    expect(arquivoService.listarArquivos).toHaveBeenCalledWith(1);
    expect(snackBar.open).toHaveBeenCalledWith('Arquivo excluído com sucesso', 'Fechar', { duration: 3000 });
  });

  it('should not delete arquivo when confirmation is cancelled', () => {
    // Mock dialog to return false (cancelled)
    const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
    dialogRefSpy.afterClosed.and.returnValue(of(false));
    dialog.open.and.returnValue(dialogRefSpy);

    component.excluirArquivo(mockArquivos[1]); // Use image file (id: 2)

    expect(dialog.open).toHaveBeenCalled();
    expect(arquivoService.excluirArquivo).not.toHaveBeenCalled();
  });

  it('should handle error when deleting arquivo', () => {
    component.socioId = 1;
    
    // Mock dialog to return true (confirmed)
    const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
    dialogRefSpy.afterClosed.and.returnValue(of(true));
    dialog.open.and.returnValue(dialogRefSpy);
    
    const errorResponse = { error: { message: 'Erro ao excluir arquivo' } };
    arquivoService.excluirArquivo.and.returnValue(throwError(() => errorResponse));

    component.excluirArquivo(mockArquivos[0]); // Use PDF file (id: 1)

    expect(arquivoService.excluirArquivo).toHaveBeenCalledWith(1);
    expect(snackBar.open).toHaveBeenCalledWith('Erro ao excluir arquivo', 'Fechar', { duration: 5000 });
  });

  it('should identify image files correctly', () => {
    expect(component.isImage('image/jpeg')).toBe(true);
    expect(component.isImage('image/png')).toBe(true);
    expect(component.isImage('application/pdf')).toBe(false);
  });

  it('should return thumbnail URL for images', () => {
    const imageArquivo = mockArquivos[1]; // id: 2, image/jpeg
    const url = component.getThumbnailUrl(imageArquivo);
    
    expect(url).toBe('/api/arquivos/2/download');
  });

  it('should return null thumbnail URL for non-images', () => {
    const pdfArquivo = mockArquivos[0]; // id: 1, application/pdf
    const url = component.getThumbnailUrl(pdfArquivo);
    
    expect(url).toBeNull();
  });

  it('should return correct icon for different file types', () => {
    expect(component.getFileIcon('image/jpeg')).toBe('image');
    expect(component.getFileIcon('application/pdf')).toBe('picture_as_pdf');
    expect(component.getFileIcon('application/msword')).toBe('description');
    expect(component.getFileIcon('application/vnd.ms-excel')).toBe('table_chart');
    expect(component.getFileIcon('application/vnd.ms-powerpoint')).toBe('slideshow');
    expect(component.getFileIcon('application/zip')).toBe('folder_zip');
    expect(component.getFileIcon('text/plain')).toBe('text_snippet');
    expect(component.getFileIcon('application/octet-stream')).toBe('insert_drive_file');
  });

  it('should format date correctly', () => {
    const dateString = '2024-01-15T10:30:00';
    const formatted = component.formatDate(dateString);
    
    expect(formatted).toContain('15/01/2024');
    expect(formatted).toContain('10:30');
  });

  // Tests for Task 7.6: Sorting and Formatting

  it('should sort arquivos by date in descending order (most recent first)', () => {
    const unsortedArquivos: Arquivo[] = [
      {
        id: 1,
        socioId: 1,
        socioNome: 'João Silva',
        nomeOriginal: 'old.pdf',
        nomeArquivo: 'old.pdf',
        tipoConteudo: 'application/pdf',
        tamanho: 1024,
        tamanhoFormatado: '1 KB',
        criadoEm: '2024-01-10T10:00:00'
      },
      {
        id: 2,
        socioId: 1,
        socioNome: 'João Silva',
        nomeOriginal: 'newest.pdf',
        nomeArquivo: 'newest.pdf',
        tipoConteudo: 'application/pdf',
        tamanho: 2048,
        tamanhoFormatado: '2 KB',
        criadoEm: '2024-01-20T15:00:00'
      },
      {
        id: 3,
        socioId: 1,
        socioNome: 'João Silva',
        nomeOriginal: 'middle.pdf',
        nomeArquivo: 'middle.pdf',
        tipoConteudo: 'application/pdf',
        tamanho: 1536,
        tamanhoFormatado: '1.5 KB',
        criadoEm: '2024-01-15T12:00:00'
      }
    ];

    const sorted = component.sortArquivosByDate(unsortedArquivos);

    expect(sorted[0].id).toBe(2); // newest
    expect(sorted[1].id).toBe(3); // middle
    expect(sorted[2].id).toBe(1); // oldest
  });

  it('should format file size correctly for bytes', () => {
    expect(component.formatFileSize(0)).toBe('0 Bytes');
    expect(component.formatFileSize(500)).toBe('500 Bytes');
    expect(component.formatFileSize(1023)).toBe('1023 Bytes');
  });

  it('should format file size correctly for kilobytes', () => {
    expect(component.formatFileSize(1024)).toBe('1 KB');
    expect(component.formatFileSize(1536)).toBe('1.5 KB');
    expect(component.formatFileSize(10240)).toBe('10 KB');
  });

  it('should format file size correctly for megabytes', () => {
    expect(component.formatFileSize(1048576)).toBe('1 MB');
    expect(component.formatFileSize(1572864)).toBe('1.5 MB');
    expect(component.formatFileSize(10485760)).toBe('10 MB');
  });

  it('should format file size correctly for gigabytes', () => {
    expect(component.formatFileSize(1073741824)).toBe('1 GB');
    expect(component.formatFileSize(2147483648)).toBe('2 GB');
  });

  it('should apply sorting when loading arquivos', () => {
    component.socioId = 1;
    
    const unsortedArquivos: Arquivo[] = [
      { ...mockArquivos[0], criadoEm: '2024-01-10T10:00:00' },
      { ...mockArquivos[1], criadoEm: '2024-01-20T15:00:00' }
    ];
    
    arquivoService.listarArquivos.and.returnValue(of(unsortedArquivos));

    component.carregarArquivos();

    expect(component.arquivos[0].criadoEm).toBe('2024-01-20T15:00:00'); // Most recent first
    expect(component.arquivos[1].criadoEm).toBe('2024-01-10T10:00:00');
  });

  // Tests for Task 8.3: Automatic reload after changes

  it('should emit arquivosChanged event when arquivos are loaded', (done) => {
    component.socioId = 1;
    arquivoService.listarArquivos.and.returnValue(of(mockArquivos));

    component.arquivosChanged.subscribe(() => {
      expect(true).toBe(true); // Event was emitted
      done();
    });

    component.carregarArquivos();
  });

  it('should emit arquivosChanged event after successful upload', (done) => {
    component.socioId = 1;
    arquivoService.listarArquivos.and.returnValue(of(mockArquivos));

    component.arquivosChanged.subscribe(() => {
      expect(true).toBe(true); // Event was emitted
      done();
    });

    component.onUploadComplete(mockArquivos);
  });

  it('should emit arquivosChanged event after successful deletion', (done) => {
    component.socioId = 1;
    
    // Mock dialog to return true (confirmed)
    const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
    dialogRefSpy.afterClosed.and.returnValue(of(true));
    dialog.open.and.returnValue(dialogRefSpy);
    
    arquivoService.excluirArquivo.and.returnValue(of(void 0));
    arquivoService.listarArquivos.and.returnValue(of([]));

    component.arquivosChanged.subscribe(() => {
      expect(true).toBe(true); // Event was emitted
      done();
    });

    component.excluirArquivo(mockArquivos[0]);
  });

  it('should not emit arquivosChanged event when deletion is cancelled', (done) => {
    component.socioId = 1;
    
    // Mock dialog to return false (cancelled)
    const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
    dialogRefSpy.afterClosed.and.returnValue(of(false));
    dialog.open.and.returnValue(dialogRefSpy);

    let eventEmitted = false;
    component.arquivosChanged.subscribe(() => {
      eventEmitted = true;
    });

    component.excluirArquivo(mockArquivos[0]);

    // Wait a bit to ensure event is not emitted
    setTimeout(() => {
      expect(eventEmitted).toBe(false);
      done();
    }, 100);
  });

  it('should not emit arquivosChanged event when loading fails', (done) => {
    component.socioId = 1;
    arquivoService.listarArquivos.and.returnValue(throwError(() => new Error('Error')));

    let eventEmitted = false;
    component.arquivosChanged.subscribe(() => {
      eventEmitted = true;
    });

    component.carregarArquivos();

    // Wait a bit to ensure event is not emitted
    setTimeout(() => {
      expect(eventEmitted).toBe(false);
      done();
    }, 100);
  });
});
