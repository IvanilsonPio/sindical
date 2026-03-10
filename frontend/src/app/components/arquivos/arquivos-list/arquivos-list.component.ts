import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { PastaService, Pasta } from '../../../services/pasta.service';
import { ArquivoGeralService, ArquivoGeral } from '../../../services/arquivo-geral.service';

@Component({
  selector: 'app-arquivos-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatChipsModule
  ],
  templateUrl: './arquivos-list.component.html',
  styleUrls: ['./arquivos-list.component.css']
})
export class ArquivosListComponent implements OnInit {
  pastas: Pasta[] = [];
  arquivos: ArquivoGeral[] = [];
  pastaAtual: Pasta | null = null;
  caminhoBreadcrumb: Pasta[] = [];
  loading = false;

  constructor(
    private pastaService: PastaService,
    private arquivoService: ArquivoGeralService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.carregarRaiz();
  }

  carregarRaiz(): void {
    this.loading = true;
    this.pastaAtual = null;
    this.caminhoBreadcrumb = [];
    
    this.pastaService.listarPastasRaiz().subscribe({
      next: (pastas) => {
        this.pastas = pastas;
        this.carregarArquivos();
      },
      error: (error) => {
        this.snackBar.open('Erro ao carregar pastas', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  carregarArquivos(): void {
    const pastaId = this.pastaAtual?.id;
    
    this.arquivoService.listarArquivos(pastaId).subscribe({
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

  abrirPasta(pasta: Pasta): void {
    this.loading = true;
    this.pastaAtual = pasta;
    
    this.pastaService.listarSubpastas(pasta.id).subscribe({
      next: (subpastas) => {
        this.pastas = subpastas;
        this.carregarArquivos();
      },
      error: (error) => {
        this.snackBar.open('Erro ao abrir pasta', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  navegarParaPasta(index: number): void {
    if (index === -1) {
      this.carregarRaiz();
    } else {
      const pasta = this.caminhoBreadcrumb[index];
      this.caminhoBreadcrumb = this.caminhoBreadcrumb.slice(0, index);
      this.caminhoBreadcrumb.push(pasta);
      this.abrirPasta(pasta);
    }
  }

  navegarParaPastaInicial(pasta: Pasta): void {
    this.loading = true;
    this.pastaAtual = pasta;
    this.caminhoBreadcrumb.push(pasta);
    
    this.pastaService.listarSubpastas(pasta.id).subscribe({
      next: (subpastas) => {
        this.pastas = subpastas;
        this.carregarArquivos();
      },
      error: (error) => {
        this.snackBar.open('Erro ao abrir pasta', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onFileSelected(event: any): void {
    const files: File[] = Array.from(event.target.files);
    if (files.length === 0) return;

    this.loading = true;
    const pastaId = this.pastaAtual?.id;
    
    this.arquivoService.uploadArquivos(files, pastaId).subscribe({
      next: (arquivosNovos) => {
        // Adiciona os novos arquivos à lista existente
        this.arquivos = [...this.arquivos, ...arquivosNovos];
        
        this.snackBar.open(`${arquivosNovos.length} arquivo(s) enviado(s) com sucesso`, 'Fechar', { duration: 3000 });
        this.loading = false;
        
        // Limpa o input para permitir upload do mesmo arquivo novamente
        event.target.value = '';
      },
      error: (error) => {
        // Se houver erro, não adiciona nada à lista e mostra mensagem detalhada
        let mensagemErro = 'Erro ao enviar arquivos';
        
        if (error.error?.message) {
          mensagemErro = error.error.message;
        } else if (error.message) {
          mensagemErro = error.message;
        }
        
        this.snackBar.open(mensagemErro, 'Fechar', { duration: 5000 });
        this.loading = false;
        event.target.value = '';
      }
    });
  }

  downloadArquivo(arquivo: ArquivoGeral): void {
    this.arquivoService.downloadArquivo(arquivo.id, arquivo.nomeOriginal);
  }

  excluirArquivo(arquivo: ArquivoGeral): void {
    if (!confirm(`Deseja realmente excluir o arquivo "${arquivo.nomeOriginal}"?`)) {
      return;
    }

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

  criarPasta(): void {
    const nome = prompt('Nome da nova pasta:');
    if (!nome) return;

    this.loading = true;
    const request = {
      nome,
      pastaPaiId: this.pastaAtual?.id
    };

    this.pastaService.criarPasta(request).subscribe({
      next: (pasta) => {
        this.snackBar.open('Pasta criada com sucesso', 'Fechar', { duration: 3000 });
        if (this.pastaAtual) {
          this.navegarParaPastaInicial(this.pastaAtual);
        } else {
          this.carregarRaiz();
        }
      },
      error: (error) => {
        this.snackBar.open('Erro ao criar pasta', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  excluirPasta(pasta: Pasta): void {
    if (!confirm(`Deseja realmente excluir a pasta "${pasta.nome}"?`)) {
      return;
    }

    this.loading = true;
    this.pastaService.excluirPasta(pasta.id).subscribe({
      next: () => {
        this.snackBar.open('Pasta excluída com sucesso', 'Fechar', { duration: 3000 });
        if (this.pastaAtual) {
          this.navegarParaPastaInicial(this.pastaAtual);
        } else {
          this.carregarRaiz();
        }
      },
      error: (error) => {
        this.snackBar.open('Erro ao excluir pasta', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  getIconeArquivo(tipoConteudo: string): string {
    if (tipoConteudo.includes('pdf')) return 'picture_as_pdf';
    if (tipoConteudo.includes('image')) return 'image';
    if (tipoConteudo.includes('word') || tipoConteudo.includes('document')) return 'description';
    if (tipoConteudo.includes('excel') || tipoConteudo.includes('spreadsheet')) return 'table_chart';
    return 'insert_drive_file';
  }
}
