import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { PagamentoService } from '../../../services/pagamento.service';
import { Pagamento, FiltroPagamento } from '../../../models/pagamento.model';
import { StatusPagamento } from '../../../models/enums';
import { ReciboHistoryDialogComponent } from '../recibo-history-dialog/recibo-history-dialog.component';

@Component({
  selector: 'app-pagamentos-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatDialogModule,
    ScrollingModule
  ],
  templateUrl: './pagamentos-list.component.html',
  styleUrls: ['./pagamentos-list.component.scss']
})
export class PagamentosListComponent implements OnInit {
  displayedColumns: string[] = ['numeroRecibo', 'socioNome', 'socioCpf', 'periodo', 'valor', 'dataPagamento', 'status', 'acoes'];
  dataSource = new MatTableDataSource<Pagamento>([]);
  
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  
  filterForm: FormGroup;
  loading = false;
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  
  // Filter options
  meses = [
    { value: 1, label: 'Janeiro' },
    { value: 2, label: 'Fevereiro' },
    { value: 3, label: 'Março' },
    { value: 4, label: 'Abril' },
    { value: 5, label: 'Maio' },
    { value: 6, label: 'Junho' },
    { value: 7, label: 'Julho' },
    { value: 8, label: 'Agosto' },
    { value: 9, label: 'Setembro' },
    { value: 10, label: 'Outubro' },
    { value: 11, label: 'Novembro' },
    { value: 12, label: 'Dezembro' }
  ];
  
  anos: number[] = [];
  statusOptions = Object.values(StatusPagamento);

  constructor(
    private pagamentoService: PagamentoService,
    private router: Router,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.filterForm = this.fb.group({
      socioNome: [''],
      mes: [''],
      ano: [''],
      status: [''],
      apenasInadimplentes: [false]
    });
    
    // Generate years from 2020 to current year + 1
    const currentYear = new Date().getFullYear();
    for (let year = 2020; year <= currentYear + 1; year++) {
      this.anos.push(year);
    }
  }

  ngOnInit(): void {
    this.loadPagamentos();
    this.setupFilterListeners();
  }

  setupFilterListeners(): void {
    // Listen to socioNome changes with debounce
    this.filterForm.get('socioNome')?.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.pageIndex = 0;
        this.loadPagamentos();
      });
    
    // Listen to other filter changes immediately
    this.filterForm.get('mes')?.valueChanges.subscribe(() => {
      this.pageIndex = 0;
      this.loadPagamentos();
    });
    
    this.filterForm.get('ano')?.valueChanges.subscribe(() => {
      this.pageIndex = 0;
      this.loadPagamentos();
    });
    
    this.filterForm.get('status')?.valueChanges.subscribe(() => {
      this.pageIndex = 0;
      this.loadPagamentos();
    });
    
    this.filterForm.get('apenasInadimplentes')?.valueChanges.subscribe(() => {
      this.pageIndex = 0;
      this.loadPagamentos();
    });
  }

  loadPagamentos(): void {
    this.loading = true;
    
    const filtros: FiltroPagamento = {
      mes: this.filterForm.get('mes')?.value || undefined,
      ano: this.filterForm.get('ano')?.value || undefined,
      status: this.filterForm.get('status')?.value || undefined,
      page: this.pageIndex,
      size: this.pageSize
    };

    this.pagamentoService.listarPagamentos(filtros).subscribe({
      next: (response) => {
        let pagamentos = response.content;
        
        // Apply client-side filtering for socioNome (if backend doesn't support it)
        const socioNome = this.filterForm.get('socioNome')?.value;
        if (socioNome) {
          const termo = socioNome.toLowerCase();
          pagamentos = pagamentos.filter(p => 
            p.socioNome.toLowerCase().includes(termo) || 
            p.socioCpf.includes(termo)
          );
        }
        
        // Filter for inadimplentes (delinquent members)
        // Note: This is a simplified implementation. In a real scenario,
        // you would need backend support to identify truly delinquent members
        // (those who haven't paid for current or past months)
        const apenasInadimplentes = this.filterForm.get('apenasInadimplentes')?.value;
        if (apenasInadimplentes) {
          pagamentos = pagamentos.filter(p => p.status === StatusPagamento.CANCELADO);
        }
        
        this.dataSource.data = pagamentos;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar pagamentos:', error);
        this.snackBar.open('Erro ao carregar pagamentos', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPagamentos();
  }

  clearFilters(): void {
    this.filterForm.reset({
      socioNome: '',
      mes: '',
      ano: '',
      status: '',
      apenasInadimplentes: false
    });
    this.pageIndex = 0;
    this.loadPagamentos();
  }

  viewRecibo(pagamento: Pagamento): void {
    this.pagamentoService.gerarRecibo(pagamento.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Erro ao gerar recibo:', error);
        this.snackBar.open('Erro ao gerar recibo', 'Fechar', { duration: 3000 });
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

  viewReciboHistory(pagamento: Pagamento): void {
    this.dialog.open(ReciboHistoryDialogComponent, {
      width: '800px',
      data: {
        socioId: pagamento.socioId,
        socioNome: pagamento.socioNome
      }
    });
  }

  createPagamento(): void {
    this.router.navigate(['/pagamentos/novo']);
  }

  formatCpf(cpf: string): string {
    if (!cpf) return '';
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
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
    const mesNome = this.meses.find(m => m.value === mes)?.label || mes.toString();
    return `${mesNome}/${ano}`;
  }

  getStatusClass(status: StatusPagamento): string {
    return status === StatusPagamento.PAGO ? 'status-pago' : 'status-cancelado';
  }
}
