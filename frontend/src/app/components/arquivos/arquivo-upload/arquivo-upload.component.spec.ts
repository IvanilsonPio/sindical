import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { ArquivoUploadComponent } from './arquivo-upload.component';
import { ArquivoService } from '../../../services/arquivo.service';
import { Arquivo } from '../../../models/arquivo.model';

describe('ArquivoUploadComponent', () => {
  let component: ArquivoUploadComponent;
  let fixture: ComponentFixture<ArquivoUploadComponent>;
  let arquivoService: jasmine.SpyObj<ArquivoService>;

  beforeEach(async () => {
    const arquivoServiceSpy = jasmine.createSpyObj('ArquivoService', ['uploadArquivos']);

    await TestBed.configureTestingModule({
      imports: [
        ArquivoUploadComponent,
        HttpClientTestingModule,
        MatSnackBarModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: ArquivoService, useValue: arquivoServiceSpy }
      ]
    }).compileComponents();

    arquivoService = TestBed.inject(ArquivoService) as jasmine.SpyObj<ArquivoService>;
    fixture = TestBed.createComponent(ArquivoUploadComponent);
    component = fixture.componentInstance;
    component.socioId = 1;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('File Validation', () => {
    it('should reject files larger than 10 MB', () => {
      const largeFile = new File(['x'.repeat(11 * 1024 * 1024)], 'large.pdf', { type: 'application/pdf' });
      const event = { target: { files: [largeFile] } } as any;

      component.onFileSelected(event);

      expect(component.selectedFiles.length).toBe(0);
    });

    it('should reject empty files', () => {
      const emptyFile = new File([], 'empty.pdf', { type: 'application/pdf' });
      const event = { target: { files: [emptyFile] } } as any;

      component.onFileSelected(event);

      expect(component.selectedFiles.length).toBe(0);
    });

    it('should reject files with invalid types', () => {
      const invalidFile = new File(['content'], 'file.exe', { type: 'application/x-msdownload' });
      const event = { target: { files: [invalidFile] } } as any;

      component.onFileSelected(event);

      expect(component.selectedFiles.length).toBe(0);
    });

    it('should accept valid PDF files', () => {
      const validFile = new File(['content'], 'document.pdf', { type: 'application/pdf' });
      const event = { target: { files: [validFile] } } as any;

      component.onFileSelected(event);

      expect(component.selectedFiles.length).toBe(1);
      expect(component.selectedFiles[0].file.name).toBe('document.pdf');
    });

    it('should accept valid image files', () => {
      const validFile = new File(['content'], 'image.jpg', { type: 'image/jpeg' });
      const event = { target: { files: [validFile] } } as any;

      component.onFileSelected(event);

      expect(component.selectedFiles.length).toBe(1);
      expect(component.selectedFiles[0].file.name).toBe('image.jpg');
    });

    it('should accept multiple valid files', () => {
      const file1 = new File(['content1'], 'doc1.pdf', { type: 'application/pdf' });
      const file2 = new File(['content2'], 'image.png', { type: 'image/png' });
      const event = { target: { files: [file1, file2] } } as any;

      component.onFileSelected(event);

      expect(component.selectedFiles.length).toBe(2);
    });
  });

  describe('File Management', () => {
    beforeEach(() => {
      const file1 = new File(['content1'], 'doc1.pdf', { type: 'application/pdf' });
      const file2 = new File(['content2'], 'doc2.pdf', { type: 'application/pdf' });
      const event = { target: { files: [file1, file2] } } as any;
      component.onFileSelected(event);
    });

    it('should remove file at specified index', () => {
      expect(component.selectedFiles.length).toBe(2);

      component.removeFile(0);

      expect(component.selectedFiles.length).toBe(1);
      expect(component.selectedFiles[0].file.name).toBe('doc2.pdf');
    });

    it('should clear all files', () => {
      expect(component.selectedFiles.length).toBe(2);

      component.clearAll();

      expect(component.selectedFiles.length).toBe(0);
    });
  });

  describe('File Upload', () => {
    it('should upload files successfully', () => {
      const file = new File(['content'], 'doc.pdf', { type: 'application/pdf' });
      const event = { target: { files: [file] } } as any;
      component.onFileSelected(event);

      const mockArquivos: Arquivo[] = [{
        id: 1,
        socioId: 1,
        socioNome: 'Test Socio',
        nomeOriginal: 'doc.pdf',
        nomeArquivo: 'stored-doc.pdf',
        tipoConteudo: 'application/pdf',
        tamanho: 1024,
        tamanhoFormatado: '1.00 KB',
        criadoEm: '2024-01-01T00:00:00'
      }];

      arquivoService.uploadArquivos.and.returnValue(of(mockArquivos));

      component.uploadFiles();

      expect(arquivoService.uploadArquivos).toHaveBeenCalledWith(1, [file]);
      expect(component.selectedFiles.length).toBe(0);
      expect(component.uploading).toBe(false);
    });

    it('should handle upload errors', () => {
      const file = new File(['content'], 'doc.pdf', { type: 'application/pdf' });
      const event = { target: { files: [file] } } as any;
      component.onFileSelected(event);

      arquivoService.uploadArquivos.and.returnValue(
        throwError(() => ({ error: { message: 'Upload failed' } }))
      );

      component.uploadFiles();

      expect(component.uploading).toBe(false);
      expect(component.selectedFiles.length).toBe(1); // Files should remain
    });

    it('should emit uploadComplete event on successful upload', (done) => {
      const file = new File(['content'], 'doc.pdf', { type: 'application/pdf' });
      const event = { target: { files: [file] } } as any;
      component.onFileSelected(event);

      const mockArquivos: Arquivo[] = [{
        id: 1,
        socioId: 1,
        socioNome: 'Test Socio',
        nomeOriginal: 'doc.pdf',
        nomeArquivo: 'stored-doc.pdf',
        tipoConteudo: 'application/pdf',
        tamanho: 1024,
        tamanhoFormatado: '1.00 KB',
        criadoEm: '2024-01-01T00:00:00'
      }];

      arquivoService.uploadArquivos.and.returnValue(of(mockArquivos));

      component.uploadComplete.subscribe((arquivos) => {
        expect(arquivos).toEqual(mockArquivos);
        done();
      });

      component.uploadFiles();
    });

    it('should not upload if no socioId is provided', () => {
      component.socioId = null as any;
      const file = new File(['content'], 'doc.pdf', { type: 'application/pdf' });
      const event = { target: { files: [file] } } as any;
      component.onFileSelected(event);

      component.uploadFiles();

      expect(arquivoService.uploadArquivos).not.toHaveBeenCalled();
    });

    it('should not upload if no files are selected', () => {
      component.uploadFiles();

      expect(arquivoService.uploadArquivos).not.toHaveBeenCalled();
    });
  });

  describe('Drag and Drop', () => {
    it('should set isDragging to true on dragover', () => {
      const event = new DragEvent('dragover');
      spyOn(event, 'preventDefault');
      spyOn(event, 'stopPropagation');

      component.onDragOver(event);

      expect(component.isDragging).toBe(true);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
    });

    it('should set isDragging to false on dragleave', () => {
      component.isDragging = true;
      const event = new DragEvent('dragleave');
      spyOn(event, 'preventDefault');
      spyOn(event, 'stopPropagation');

      component.onDragLeave(event);

      expect(component.isDragging).toBe(false);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
    });

    it('should handle file drop', () => {
      const file = new File(['content'], 'doc.pdf', { type: 'application/pdf' });
      const dataTransfer = new DataTransfer();
      dataTransfer.items.add(file);
      
      const event = new DragEvent('drop', { dataTransfer });
      spyOn(event, 'preventDefault');
      spyOn(event, 'stopPropagation');

      component.onDrop(event);

      expect(component.isDragging).toBe(false);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(component.selectedFiles.length).toBe(1);
    });
  });

  describe('Utility Methods', () => {
    it('should format file size correctly', () => {
      expect(component.formatFileSize(500)).toBe('500 B');
      expect(component.formatFileSize(1024)).toBe('1.00 KB');
      expect(component.formatFileSize(1024 * 1024)).toBe('1.00 MB');
      expect(component.formatFileSize(5 * 1024 * 1024)).toBe('5.00 MB');
    });

    it('should return correct icon for file types', () => {
      expect(component.getFileIcon(new File([], 'test.pdf', { type: 'application/pdf' }))).toBe('picture_as_pdf');
      expect(component.getFileIcon(new File([], 'test.jpg', { type: 'image/jpeg' }))).toBe('image');
      expect(component.getFileIcon(new File([], 'test.doc', { type: 'application/msword' }))).toBe('description');
      expect(component.getFileIcon(new File([], 'test.xls', { type: 'application/vnd.ms-excel' }))).toBe('table_chart');
      expect(component.getFileIcon(new File([], 'test.zip', { type: 'application/zip' }))).toBe('folder_zip');
      expect(component.getFileIcon(new File([], 'test.txt', { type: 'text/plain' }))).toBe('insert_drive_file');
    });
  });
});
