import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ArquivoService } from '../../../services/arquivo.service';
import { Arquivo } from '../../../models/arquivo.model';
import { ArquivoUploadComponent } from '../arquivo-upload/arquivo-upload.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-arquivo-manager',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    ArquivoUploadComponent
  ],
  templateUrl: './arquivo-manager.component.html',
  styleUrls: ['./arquivo-manager.component.scss']
})
export class ArquivoManagerComponent implements OnInit {
  @Input() socioId!: number;
  @Input() socioNome?: string;
  @Output() arquivosChanged = new EventEmitter<void>();
  
  arquivos: Arquivo[] = [];
  loading = false;
  displayedColumns: string[] = ['thumbnail', 'nomeOriginal', 'tipoConteudo', 'tamanho', 'criadoEm', 'acoes'];
  
  constructor(
    private arquivoService: ArquivoService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    if (this.socioId) {
      this.carregarArquivos();
    }
  }

  carregarArquivos(): void {
    this.loading = true;
    this.arquivoService.listarArquivos(this.socioId).subscribe({
      next: (arquivos) => {
        // Sort files by upload date in descending order (most recent first)
        this.arquivos = this.sortArquivosByDate(arquivos);
        this.loading = false;
        // Emit event to notify parent component that files have changed
        // Requirements: 4.5 - Reload file list after upload/deletion
        this.arquivosChanged.emit();
      },
      error: (error) => {
        this.snackBar.open('Erro ao carregar arquivos', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onUploadComplete(arquivos: Arquivo[]): void {
    // Reload file list after successful upload
    // Requirements: 4.5 - Reload file list after upload
    this.carregarArquivos();
  }

  viewFile(arquivo: Arquivo): void {
    // Check if file type is viewable in browser (PDF, images)
    const viewableTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    const isViewable = viewableTypes.includes(arquivo.tipoConteudo.toLowerCase());
    
    if (!isViewable) {
      this.snackBar.open('Este tipo de arquivo não pode ser visualizado no navegador', 'Fechar', { duration: 3000 });
      return;
    }
    
    this.arquivoService.downloadArquivo(arquivo.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        // Open in new tab for viewing
        window.open(url, '_blank');
        
        // Clean up after a delay to ensure the file loads
        setTimeout(() => {
          window.URL.revokeObjectURL(url);
        }, 100);
      },
      error: (error) => {
        this.snackBar.open('Erro ao visualizar arquivo', 'Fechar', { duration: 3000 });
      }
    });
  }

  downloadArquivo(arquivo: Arquivo): void {
    this.arquivoService.downloadArquivo(arquivo.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = arquivo.nomeOriginal;
        link.click();
        window.URL.revokeObjectURL(url);
        
        this.snackBar.open('Download iniciado', 'Fechar', { duration: 2000 });
      },
      error: (error) => {
        this.snackBar.open('Erro ao fazer download do arquivo', 'Fechar', { duration: 3000 });
      }
    });
  }

  excluirArquivo(arquivo: Arquivo): void {
    const dialogData: ConfirmDialogData = {
      title: 'Confirmar Exclusão',
      message: `Deseja realmente excluir o arquivo "${arquivo.nomeOriginal}"?`,
      confirmText: 'Excluir',
      cancelText: 'Cancelar'
    };

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.loading = true;
        this.arquivoService.excluirArquivo(arquivo.id).subscribe({
          next: () => {
            this.snackBar.open('Arquivo excluído com sucesso', 'Fechar', { duration: 3000 });
            // Reload file list after successful deletion
            // Requirements: 4.5 - Reload file list after deletion
            this.carregarArquivos();
          },
          error: (error) => {
            this.loading = false;
            const errorMessage = error?.error?.message || 'Erro ao excluir arquivo. Tente novamente';
            this.snackBar.open(errorMessage, 'Fechar', { duration: 5000 });
          }
        });
      }
    });
  }

  isImage(tipoConteudo: string): boolean {
    return tipoConteudo.startsWith('image/');
  }

  isViewable(tipoConteudo: string): boolean {
    const viewableTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    return viewableTypes.includes(tipoConteudo.toLowerCase());
  }

  getThumbnailUrl(arquivo: Arquivo): string | null {
    if (this.isImage(arquivo.tipoConteudo)) {
      // Return the download URL for images to display as thumbnail
      return `/api/arquivos/${arquivo.id}/download`;
    }
    return null;
  }

  getFileIcon(tipoConteudo: string): string {
    if (tipoConteudo.startsWith('image/')) {
      return 'image';
    } else if (tipoConteudo === 'application/pdf') {
      return 'picture_as_pdf';
    } else if (tipoConteudo.includes('word') || tipoConteudo.includes('document')) {
      return 'description';
    } else if (tipoConteudo.includes('excel') || tipoConteudo.includes('spreadsheet')) {
      return 'table_chart';
    } else if (tipoConteudo.includes('powerpoint') || tipoConteudo.includes('presentation')) {
      return 'slideshow';
    } else if (tipoConteudo.includes('zip') || tipoConteudo.includes('rar') || tipoConteudo.includes('compressed')) {
      return 'folder_zip';
    } else if (tipoConteudo.startsWith('text/')) {
      return 'text_snippet';
    } else {
      return 'insert_drive_file';
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Sort files by upload date in descending order (most recent first)
   * Validates: Requirements 3.18
   */
  sortArquivosByDate(arquivos: Arquivo[]): Arquivo[] {
    return arquivos.sort((a, b) => {
      const dateA = new Date(a.criadoEm).getTime();
      const dateB = new Date(b.criadoEm).getTime();
      return dateB - dateA; // Descending order
    });
  }

  /**
   * Format file size to human-readable format
   * Validates: Requirements 3.18
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  /**
   * Handle image load error by hiding the image element
   */
  onImageError(event: Event): void {
    const target = event.target as HTMLImageElement;
    if (target) {
      target.style.display = 'none';
    }
  }

}
