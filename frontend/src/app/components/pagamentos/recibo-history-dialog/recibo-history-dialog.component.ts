import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { PagamentoService } from '../../../services/pagamento.service';
import { Pagamento } from '../../../models/pagamento.model';
import { StatusPagamento } from '../../../models/enums';

export interface ReciboHistoryDialogData {
  socioId: number;
  socioNome: string;
}

@Component({
  selector: 'app-recibo-history-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './recibo-history-dialog.component.html',
  styleUrls: ['./recibo-history-dialog.component.scss']
})
export class ReciboHistoryDialogComponent implements OnInit {
  displayedColumns: string[] = ['numeroRecibo', 'periodo', 'valor', 'dataPagamento', 'status', 'acoes'];
  recibos: Pagamento[] = [];
  loading = false;

  meses = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
  ];

  constructor(
    public dialogRef: MatDialogRef<ReciboHistoryDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ReciboHistoryDialogData,
    private pagamentoService: PagamentoService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadRecibos();
  }

  loadRecibos(): void {
    this.loading = true;
    this.pagamentoService.listarRecibosPorSocio(this.data.socioId).subscribe({
      next: (recibos) => {
        this.recibos = recibos.sort((a, b) => {
          // Sort by year and month descending
          if (a.ano !== b.ano) return b.ano - a.ano;
          return b.mes - a.mes;
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar histórico de recibos:', error);
        this.snackBar.open('Erro ao carregar histórico de recibos', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  viewRecibo(pagamento: Pagamento): void {
    this.pagamentoService.gerarRecibo(pagamento.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Erro ao visualizar recibo:', error);
        this.snackBar.open('Erro ao visualizar recibo', 'Fechar', { duration: 3000 });
      }
    });
  }

  downloadRecibo(pagamento: Pagamento): void {
    this.pagamentoService.gerarRecibo(pagamento.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `recibo-${pagamento.numeroRecibo}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Recibo baixado com sucesso', 'Fechar', { duration: 2000 });
      },
      error: (error) => {
        console.error('Erro ao baixar recibo:', error);
        this.snackBar.open('Erro ao baixar recibo', 'Fechar', { duration: 3000 });
      }
    });
  }

  reprintRecibo(pagamento: Pagamento): void {
    this.pagamentoService.gerarReciboSegundaVia(pagamento.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `recibo-${pagamento.numeroRecibo}-2via.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Segunda via do recibo baixada com sucesso', 'Fechar', { duration: 2000 });
      },
      error: (error) => {
        console.error('Erro ao gerar segunda via:', error);
        this.snackBar.open('Erro ao gerar segunda via do recibo', 'Fechar', { duration: 3000 });
      }
    });
  }

  formatValor(valor: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  }

  formatData(data: string): string {
    if (!data) return '';
    const date = new Date(data);
    return date.toLocaleDateString('pt-BR');
  }

  formatPeriodo(mes: number, ano: number): string {
    return `${this.meses[mes - 1]}/${ano}`;
  }

  getStatusClass(status: StatusPagamento): string {
    return status === StatusPagamento.PAGO ? 'status-pago' : 'status-cancelado';
  }

  close(): void {
    this.dialogRef.close();
  }
}
