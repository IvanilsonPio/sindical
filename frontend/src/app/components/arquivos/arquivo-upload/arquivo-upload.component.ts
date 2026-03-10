import { Component, EventEmitter, Input, Output, HostListener } from '@angular/core';
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
  
  // File validation constants (Requirements 3.2, 3.3)
  private readonly MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
  private readonly ALLOWED_TYPES = [
    'application/pdf',
    'image/jpeg',
    'image/jpg',
    'image/png',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  ];
  
  private readonly ALLOWED_EXTENSIONS = [
    'pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png'
  ];

  constructor(
    private arquivoService: ArquivoService,
    private snackBar: MatSnackBar
  ) {}

  /**
   * Handle keyboard events for drop zone accessibility
   * Enter or Space key triggers file selection dialog
   */
  onDropZoneKeydown(event: KeyboardEvent, fileInput: HTMLInputElement): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      fileInput.click();
    }
  }

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
    // Requirement 3.3: Check file size (max 10MB)
    if (file.size > this.MAX_FILE_SIZE) {
      return {
        valid: false,
        error: `Arquivo excede o tamanho máximo permitido de 10MB` // Requirement 3.4
      };
    }

    // Check if file is empty
    if (file.size === 0) {
      return {
        valid: false,
        error: `Arquivo "${file.name}" está vazio`
      };
    }

    // Requirement 3.2: Check file type
    const extension = this.getFileExtension(file.name);
    const isValidType = this.ALLOWED_TYPES.includes(file.type);
    const isValidExtension = this.ALLOWED_EXTENSIONS.includes(extension);
    
    if (!isValidType && !isValidExtension) {
      return {
        valid: false,
        error: `Tipo de arquivo não permitido. Formatos aceitos: PDF, DOC, DOCX, JPG, JPEG, PNG` // Requirement 3.5
      };
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
    
    if (['jpg', 'jpeg', 'png'].includes(extension)) {
      return 'image';
    } else if (extension === 'pdf') {
      return 'picture_as_pdf';
    } else if (['doc', 'docx'].includes(extension)) {
      return 'description';
    } else {
      return 'insert_drive_file';
    }
  }
}
