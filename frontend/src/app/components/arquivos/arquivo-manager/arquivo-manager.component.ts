import { Component, Input, OnInit } from '@angular/core';
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
        this.arquivos = arquivos;
        this.loading = false;
      },
      error: (error) => {
        this.snackBar.open('Erro ao carregar arquivos', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onUploadComplete(arquivos: Arquivo[]): void {
    this.carregarArquivos();
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
    if (confirm(`Tem certeza que deseja excluir o arquivo "${arquivo.nomeOriginal}"?`)) {
      this.arquivoService.excluirArquivo(arquivo.id).subscribe({
        next: () => {
          this.snackBar.open('Arquivo excluído com sucesso', 'Fechar', { duration: 3000 });
          this.carregarArquivos();
        },
        error: (error) => {
          this.snackBar.open('Erro ao excluir arquivo', 'Fechar', { duration: 3000 });
        }
      });
    }
  }

  isImage(tipoConteudo: string): boolean {
    return tipoConteudo.startsWith('image/');
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
}
