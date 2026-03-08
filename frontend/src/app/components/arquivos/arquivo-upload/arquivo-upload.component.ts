import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpEventType, HttpResponse } from '@angular/common/http';

import { ArquivoService } from '../../../services/arquivo.service';
import { Arquivo } from '../../../models/arquivo.model';

interface FileWithProgress {
  file: File;
  progress: number;
  uploading: boolean;
  error?: string;
}

@Component({
  selector: 'app-arquivo-upload',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatSnackBarModule
  ],
  templateUrl: './arquivo-upload.component.html',
  styleUrls: ['./arquivo-upload.component.scss']
})
export class ArquivoUploadComponent {
  @Input() socioId!: number;
  @Output() uploadComplete = new EventEmitter<Arquivo[]>();
  
  selectedFiles: FileWithProgress[] = [];
  isDragging = false;
  uploading = false;
  
  // File validation constants (matching backend ArquivoConstants)
  private readonly MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
  private readonly ALLOWED_TYPES = [
    'application/pdf',
    'image/jpeg',
    'image/jpg',
    'image/png',
    'image/gif',
    'image/bmp',
    'image/webp',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-powerpoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'text/plain',
    'text/csv',
    'application/zip',
    'application/x-rar-compressed',
    'application/x-7z-compressed'
  ];
  
  private readonly ALLOWED_EXTENSIONS = [
    'pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'csv',
    'jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp',
    'zip', 'rar', '7z'
  ];

  constructor(
    private arquivoService: ArquivoService,
    private snackBar: MatSnackBar
  ) {}

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    const files = event.dataTransfer?.files;
    if (files) {
      this.handleFiles(Array.from(files));
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.handleFiles(Array.from(input.files));
    }
  }

  private handleFiles(files: File[]): void {
    for (const file of files) {
      const validation = this.validateFile(file);
      
      if (validation.valid) {
        this.selectedFiles.push({
          file,
          progress: 0,
          uploading: false
        });
      } else {
        this.snackBar.open(validation.error!, 'Fechar', { duration: 5000 });
      }
    }
  }

  private validateFile(file: File): { valid: boolean; error?: string } {
    // Check file size
    if (file.size > this.MAX_FILE_SIZE) {
      return {
        valid: false,
        error: `Arquivo "${file.name}" excede o tamanho máximo de 10 MB`
      };
    }

    // Check if file is empty
    if (file.size === 0) {
      return {
        valid: false,
        error: `Arquivo "${file.name}" está vazio`
      };
    }

    // Check file type
    if (!this.ALLOWED_TYPES.includes(file.type)) {
      // Also check by extension as fallback
      const extension = this.getFileExtension(file.name);
      if (!this.ALLOWED_EXTENSIONS.includes(extension)) {
        return {
          valid: false,
          error: `Tipo de arquivo "${file.name}" não permitido. Tipos aceitos: PDF, imagens, documentos Office, arquivos de texto e compactados`
        };
      }
    }

    return { valid: true };
  }

  private getFileExtension(filename: string): string {
    const parts = filename.split('.');
    return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : '';
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
  }

  uploadFiles(): void {
    if (!this.socioId) {
      this.snackBar.open('ID do sócio não informado', 'Fechar', { duration: 3000 });
      return;
    }

    if (this.selectedFiles.length === 0) {
      this.snackBar.open('Nenhum arquivo selecionado', 'Fechar', { duration: 3000 });
      return;
    }

    this.uploading = true;
    const filesToUpload = this.selectedFiles.map(f => f.file);

    this.arquivoService.uploadArquivos(this.socioId, filesToUpload).subscribe({
      next: (arquivos) => {
        this.snackBar.open(
          `${arquivos.length} arquivo(s) enviado(s) com sucesso!`,
          'Fechar',
          { duration: 3000 }
        );
        this.uploadComplete.emit(arquivos);
        this.selectedFiles = [];
        this.uploading = false;
      },
      error: (error) => {
        let errorMessage = 'Erro ao enviar arquivos';
        if (error.error?.message) {
          errorMessage = error.error.message;
        }
        this.snackBar.open(errorMessage, 'Fechar', { duration: 5000 });
        this.uploading = false;
      }
    });
  }

  clearAll(): void {
    this.selectedFiles = [];
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) {
      return bytes + ' B';
    } else if (bytes < 1024 * 1024) {
      return (bytes / 1024).toFixed(2) + ' KB';
    } else {
      return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    }
  }

  getFileIcon(file: File): string {
    const extension = this.getFileExtension(file.name);
    
    if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension)) {
      return 'image';
    } else if (extension === 'pdf') {
      return 'picture_as_pdf';
    } else if (['doc', 'docx'].includes(extension)) {
      return 'description';
    } else if (['xls', 'xlsx'].includes(extension)) {
      return 'table_chart';
    } else if (['ppt', 'pptx'].includes(extension)) {
      return 'slideshow';
    } else if (['zip', 'rar', '7z'].includes(extension)) {
      return 'folder_zip';
    } else {
      return 'insert_drive_file';
    }
  }
}
