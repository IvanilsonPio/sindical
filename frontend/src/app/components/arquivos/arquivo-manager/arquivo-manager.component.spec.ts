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

    component.downloadArquivo(mockArquivos[0]);

    expect(arquivoService.downloadArquivo).toHaveBeenCalledWith(1);
    expect(link.download).toBe('documento.pdf');
    expect(link.click).toHaveBeenCalled();
    expect(snackBar.open).toHaveBeenCalled();
  });

  it('should handle error when downloading arquivo', () => {
    arquivoService.downloadArquivo.and.returnValue(throwError(() => new Error('Error')));

    component.downloadArquivo(mockArquivos[0]);

    expect(arquivoService.downloadArquivo).toHaveBeenCalledWith(1);
    expect(snackBar.open).toHaveBeenCalled();
  });

  it('should delete arquivo after confirmation', () => {
    component.socioId = 1;
    spyOn(window, 'confirm').and.returnValue(true);
    arquivoService.excluirArquivo.and.returnValue(of(void 0));
    arquivoService.listarArquivos.and.returnValue(of([]));

    component.excluirArquivo(mockArquivos[0]);

    expect(window.confirm).toHaveBeenCalled();
    expect(arquivoService.excluirArquivo).toHaveBeenCalledWith(1);
    expect(arquivoService.listarArquivos).toHaveBeenCalledWith(1);
    expect(snackBar.open).toHaveBeenCalled();
  });

  it('should not delete arquivo when confirmation is cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.excluirArquivo(mockArquivos[0]);

    expect(window.confirm).toHaveBeenCalled();
    expect(arquivoService.excluirArquivo).not.toHaveBeenCalled();
  });

  it('should handle error when deleting arquivo', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    arquivoService.excluirArquivo.and.returnValue(throwError(() => new Error('Error')));

    component.excluirArquivo(mockArquivos[0]);

    expect(arquivoService.excluirArquivo).toHaveBeenCalledWith(1);
    expect(snackBar.open).toHaveBeenCalled();
  });

  it('should identify image files correctly', () => {
    expect(component.isImage('image/jpeg')).toBe(true);
    expect(component.isImage('image/png')).toBe(true);
    expect(component.isImage('application/pdf')).toBe(false);
  });

  it('should return thumbnail URL for images', () => {
    const imageArquivo = mockArquivos[1];
    const url = component.getThumbnailUrl(imageArquivo);
    
    expect(url).toBe('/api/arquivos/2/download');
  });

  it('should return null thumbnail URL for non-images', () => {
    const pdfArquivo = mockArquivos[0];
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
});
