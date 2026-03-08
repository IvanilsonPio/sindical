import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, startWith, map } from 'rxjs/operators';

import { PagamentoService } from '../../../services/pagamento.service';
import { SocioService } from '../../../services/socio.service';
import { PagamentoRequest } from '../../../models/pagamento.model';
import { Socio } from '../../../models/socio.model';

@Component({
  selector: 'app-pagamento-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule,
    MatAutocompleteModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './pagamento-form.component.html',
  styleUrls: ['./pagamento-form.component.scss']
})
export class PagamentoFormComponent implements OnInit {
  pagamentoForm!: FormGroup;
  loading = false;
  socios: Socio[] = [];
  filteredSocios$!: Observable<Socio[]>;
  selectedSocio?: Socio;
  
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
  maxDate = new Date();

  constructor(
    private fb: FormBuilder,
    private pagamentoService: PagamentoService,
    private socioService: SocioService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    // Generate years from 2020 to current year
    const currentYear = new Date().getFullYear();
    for (let year = 2020; year <= currentYear; year++) {
      this.anos.push(year);
    }
  }

  ngOnInit(): void {
    this.initForm();
    this.loadSocios();
    this.setupSocioAutocomplete();
  }

  private initForm(): void {
    const today = new Date();
    const currentMonth = today.getMonth() + 1;
    const currentYear = today.getFullYear();
    
    this.pagamentoForm = this.fb.group({
      socioSearch: ['', Validators.required],
      socioId: [null, Validators.required],
      mes: [currentMonth, [Validators.required, Validators.min(1), Validators.max(12)]],
      ano: [currentYear, [Validators.required, Validators.min(2020)]],
      dataPagamento: [today, Validators.required],
      valor: ['', [Validators.required, Validators.min(0.01)]],
      observacoes: ['']
    });
  }

  private loadSocios(): void {
    this.socioService.listarSocios({ page: 0, size: 1000 }).subscribe({
      next: (response) => {
        this.socios = response.content;
      },
      error: (error) => {
        console.error('Erro ao carregar sócios:', error);
        this.snackBar.open('Erro ao carregar lista de sócios', 'Fechar', { duration: 3000 });
      }
    });
  }

  private setupSocioAutocomplete(): void {
    this.filteredSocios$ = this.pagamentoForm.get('socioSearch')!.valueChanges.pipe(
      startWith(''),
      debounceTime(300),
      distinctUntilChanged(),
      map(value => {
        const searchTerm = typeof value === 'string' ? value : value?.nome || '';
        return this.filterSocios(searchTerm);
      })
    );
  }

  private filterSocios(searchTerm: string): Socio[] {
    if (!searchTerm) {
      return this.socios.slice(0, 10);
    }
    
    const term = searchTerm.toLowerCase();
    return this.socios
      .filter(socio => 
        socio.nome.toLowerCase().includes(term) ||
        socio.cpf.includes(term) ||
        socio.matricula.toLowerCase().includes(term)
      )
      .slice(0, 10);
  }

  displaySocio(socio: Socio | null): string {
    if (!socio) return '';
    return `${socio.nome} - CPF: ${this.formatCpf(socio.cpf)}`;
  }

  onSocioSelected(socio: Socio): void {
    this.selectedSocio = socio;
    this.pagamentoForm.patchValue({
      socioId: socio.id
    });
  }

  onSocioInputChange(): void {
    // Clear socioId if user types something different
    const currentValue = this.pagamentoForm.get('socioSearch')?.value;
    if (typeof currentValue === 'string') {
      this.pagamentoForm.patchValue({
        socioId: null
      });
      this.selectedSocio = undefined;
    }
  }

  applyValorMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');
    
    if (value) {
      const numValue = parseInt(value, 10) / 100;
      this.pagamentoForm.patchValue({ valor: numValue }, { emitEvent: false });
      input.value = this.formatValor(numValue);
    }
  }

  onSubmit(): void {
    if (this.pagamentoForm.invalid) {
      this.pagamentoForm.markAllAsTouched();
      this.snackBar.open('Por favor, preencha todos os campos obrigatórios corretamente', 'Fechar', { duration: 3000 });
      return;
    }

    // Validate that a socio was actually selected
    if (!this.pagamentoForm.get('socioId')?.value) {
      this.snackBar.open('Por favor, selecione um sócio da lista', 'Fechar', { duration: 3000 });
      return;
    }

    this.loading = true;
    
    const dataPagamento = this.pagamentoForm.get('dataPagamento')?.value;
    const formattedDate = this.formatDateToISO(dataPagamento);
    
    const pagamentoData: PagamentoRequest = {
      socioId: this.pagamentoForm.get('socioId')?.value,
      mes: this.pagamentoForm.get('mes')?.value,
      ano: this.pagamentoForm.get('ano')?.value,
      dataPagamento: formattedDate,
      valor: this.pagamentoForm.get('valor')?.value,
      observacoes: this.pagamentoForm.get('observacoes')?.value || undefined
    };

    this.pagamentoService.registrarPagamento(pagamentoData).subscribe({
      next: (pagamento) => {
        this.snackBar.open('Pagamento registrado com sucesso! Gerando recibo...', 'Fechar', { duration: 3000 });
        
        // Automatically generate and download receipt
        this.gerarRecibo(pagamento.id);
      },
      error: (error) => {
        let errorMessage = 'Erro ao registrar pagamento';
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.status === 409) {
          errorMessage = 'Já existe um pagamento registrado para este sócio neste período';
        }
        this.snackBar.open(errorMessage, 'Fechar', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  private gerarRecibo(pagamentoId: number): void {
    this.pagamentoService.gerarRecibo(pagamentoId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `recibo-pagamento-${pagamentoId}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        
        this.snackBar.open('Recibo gerado e baixado com sucesso!', 'Fechar', { duration: 3000 });
        this.loading = false;
        
        // Navigate back to payments list after successful registration
        setTimeout(() => {
          this.router.navigate(['/pagamentos']);
        }, 1500);
      },
      error: (error) => {
        console.error('Erro ao gerar recibo:', error);
        this.snackBar.open('Pagamento registrado, mas houve erro ao gerar o recibo', 'Fechar', { duration: 5000 });
        this.loading = false;
        
        // Still navigate back even if receipt generation failed
        setTimeout(() => {
          this.router.navigate(['/pagamentos']);
        }, 2000);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/pagamentos']);
  }

  private formatDateToISO(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private formatCpf(cpf: string): string {
    if (!cpf) return '';
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

  private formatValor(valor: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.pagamentoForm.get(fieldName);
    if (!field || !field.errors || !field.touched) {
      return '';
    }

    if (field.errors['required']) {
      return 'Este campo é obrigatório';
    }
    if (field.errors['min']) {
      if (fieldName === 'valor') return 'O valor deve ser maior que zero';
      if (fieldName === 'mes') return 'Mês inválido';
      if (fieldName === 'ano') return 'Ano deve ser 2020 ou posterior';
    }
    if (field.errors['max']) {
      if (fieldName === 'mes') return 'Mês inválido';
    }

    return 'Campo inválido';
  }

  getMesNome(mes: number): string {
    return this.meses.find(m => m.value === mes)?.label || '';
  }
}
