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
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { SocioService } from '../../../services/socio.service';
import { Socio, FiltroSocio } from '../../../models/socio.model';
import { ReciboHistoryDialogComponent } from '../../pagamentos/recibo-history-dialog/recibo-history-dialog.component';

@Component({
  selector: 'app-socios-list',
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
    MatDialogModule,
    ScrollingModule
  ],
  templateUrl: './socios-list.component.html',
  styleUrls: ['./socios-list.component.scss']
})
export class SociosListComponent implements OnInit {
  displayedColumns: string[] = ['matricula', 'nome', 'cpf', 'telefone', 'status', 'acoes'];
  dataSource = new MatTableDataSource<Socio>([]);
  
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  
  filterForm: FormGroup;
  loading = false;
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  
  // Virtual scrolling configuration
  useVirtualScroll = false; // Toggle between pagination and virtual scroll
  virtualScrollItemSize = 48; // Height of each row in pixels
  allSocios: Socio[] = []; // Store all loaded socios for virtual scroll

  constructor(
    private socioService: SocioService,
    private router: Router,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.filterForm = this.fb.group({
      termo: ['']
    });
  }

  ngOnInit(): void {
    this.loadSocios();
    this.setupFilterListener();
  }

  setupFilterListener(): void {
    this.filterForm.get('termo')?.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.pageIndex = 0;
        this.loadSocios();
      });
  }

  loadSocios(): void {
    this.loading = true;
    
    const filtros: FiltroSocio = {
      termo: this.filterForm.get('termo')?.value || undefined,
      page: this.pageIndex,
      size: this.useVirtualScroll ? 1000 : this.pageSize // Load more items for virtual scroll
    };

    this.socioService.listarSocios(filtros).subscribe({
      next: (response) => {
        if (this.useVirtualScroll) {
          this.allSocios = response.content;
        } else {
          this.dataSource.data = response.content;
        }
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar sócios:', error);
        this.snackBar.open('Erro ao carregar sócios', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  toggleVirtualScroll(): void {
    this.useVirtualScroll = !this.useVirtualScroll;
    this.pageIndex = 0;
    this.loadSocios();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadSocios();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.pageIndex = 0;
    this.loadSocios();
  }

  viewSocio(socio: Socio): void {
    this.router.navigate(['/socios', socio.id]);
  }

  editSocio(socio: Socio): void {
    this.router.navigate(['/socios', socio.id, 'editar']);
  }

  deleteSocio(socio: Socio): void {
    const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
      width: '400px',
      data: { nome: socio.nome }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading = true;
        this.socioService.excluirSocio(socio.id).subscribe({
          next: () => {
            this.snackBar.open('Sócio excluído com sucesso', 'Fechar', { duration: 3000 });
            this.loadSocios();
          },
          error: (error) => {
            console.error('Erro ao excluir sócio:', error);
            this.snackBar.open('Erro ao excluir sócio', 'Fechar', { duration: 3000 });
            this.loading = false;
          }
        });
      }
    });
  }

  createSocio(): void {
    this.router.navigate(['/socios/novo']);
  }

  viewReciboHistory(socio: Socio): void {
    this.dialog.open(ReciboHistoryDialogComponent, {
      width: '800px',
      data: {
        socioId: socio.id,
        socioNome: socio.nome
      }
    });
  }

  formatCpf(cpf: string): string {
    if (!cpf) return '';
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

  formatTelefone(telefone: string): string {
    if (!telefone) return '';
    const cleaned = telefone.replace(/\D/g, '');
    if (cleaned.length === 11) {
      return cleaned.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    } else if (cleaned.length === 10) {
      return cleaned.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
    }
    return telefone;
  }
}

// Confirm Delete Dialog Component
import { Component as NgComponent, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@NgComponent({
  selector: 'app-confirm-delete-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Confirmar Exclusão</h2>
    <mat-dialog-content>
      <p>Tem certeza que deseja excluir o sócio <strong>{{ data.nome }}</strong>?</p>
      <p class="warning">Esta ação não pode ser desfeita.</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button mat-raised-button color="warn" (click)="onConfirm()">Excluir</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .warning {
      color: #f44336;
      font-size: 0.9em;
      margin-top: 8px;
    }
  `]
})
export class ConfirmDeleteDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { nome: string }
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
