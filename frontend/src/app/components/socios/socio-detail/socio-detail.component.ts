import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { SocioService } from '../../../services/socio.service';
import { SocioDetalhadoResponse } from '../../../models/socio.model';
import { HttpErrorResponse } from '@angular/common/http';
import { ArquivoManagerComponent } from '../../arquivos/arquivo-manager/arquivo-manager.component';
import { Subscription, filter } from 'rxjs';

@Component({
  selector: 'app-socio-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatChipsModule,
    MatTooltipModule,
    MatSnackBarModule,
    ArquivoManagerComponent
  ],
  templateUrl: './socio-detail.component.html',
  styleUrls: ['./socio-detail.component.scss']
})
export class SocioDetailComponent implements OnInit, OnDestroy {
  socioId: number | null = null;
  socio: SocioDetalhadoResponse | null = null;
  loading = false;
  error: string | null = null;
  private navigationSubscription?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private socioService: SocioService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.socioId = parseInt(id, 10);
      this.loadSocio(this.socioId);
    }

    // Subscribe to navigation events to reload data when returning from edit
    // Requirements: 4.4 - Reload socio data after returning from edit
    this.navigationSubscription = this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd)
      )
      .subscribe((event: any) => {
        // Check if we're navigating to this detail page
        const currentUrl = event.urlAfterRedirects || event.url;
        if (this.socioId && currentUrl.includes(`/socios/${this.socioId}/detalhes`)) {
          // Reload socio data
          this.loadSocio(this.socioId);
        }
      });
  }

  ngOnDestroy(): void {
    // Clean up subscription to prevent memory leaks
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }

  loadSocio(id: number): void {
    this.loading = true;
    this.error = null;

    this.socioService.getSocioDetalhado(id).subscribe({
      next: (socio) => {
        this.socio = socio;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.handleError(error);
      }
    });
  }

  private handleError(error: HttpErrorResponse): void {
    if (error.status === 404) {
      this.error = 'Sócio não encontrado';
      this.snackBar.open('Sócio não encontrado', 'Fechar', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      // Redirect to list after 3 seconds
      setTimeout(() => {
        this.router.navigate(['/socios']);
      }, 3000);
    } else if (error.status === 0) {
      this.error = 'Erro ao comunicar com o servidor. Verifique sua conexão';
      this.snackBar.open(this.error, 'Fechar', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
    } else if (error.status >= 500) {
      this.error = 'Erro no servidor. Tente novamente mais tarde';
      this.snackBar.open(this.error, 'Fechar', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
    } else {
      this.error = error.error?.message || 'Erro ao carregar dados do sócio';
      this.snackBar.open(this.error || 'Erro desconhecido', 'Fechar', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
    }
  }

  navigateToEdit(): void {
    if (this.socioId) {
      this.router.navigate(['/socios', this.socioId, 'editar']);
    }
  }

  close(): void {
    this.router.navigate(['/socios']);
  }

  /**
   * Handle file changes (upload/deletion) by reloading socio data
   * This ensures the file list in socio.arquivos is updated
   * Requirements: 4.5 - Reload file list after upload/deletion
   */
  onArquivosChanged(): void {
    if (this.socioId) {
      this.loadSocio(this.socioId);
    }
  }

  /**
   * Get label for operation type
   * Requirements: 5.5, 5.6 - Display operation type in history
   */
  getOperacaoLabel(operacao: string): string {
    const labels: { [key: string]: string } = {
      'CREATE': 'Criação',
      'UPDATE': 'Atualização',
      'DELETE': 'Exclusão'
    };
    return labels[operacao] || operacao;
  }

  /**
   * Convert camposAlterados object to array for iteration
   * Requirements: 5.5, 5.6 - Display changed fields in history
   */
  getChangedFieldsArray(camposAlterados: { [key: string]: any }): any[] {
    if (!camposAlterados) {
      return [];
    }
    return Object.keys(camposAlterados).map(key => ({
      nomeCampo: key,
      valorAnterior: camposAlterados[key].valorAnterior,
      valorNovo: camposAlterados[key].valorNovo
    }));
  }

  /**
   * Get user-friendly label for field name
   * Requirements: 5.5, 5.6 - Display field names in Portuguese
   */
  getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      'nome': 'Nome',
      'cpf': 'CPF',
      'matricula': 'Matrícula',
      'rg': 'RG',
      'dataNascimento': 'Data de Nascimento',
      'estadoCivil': 'Estado Civil',
      'cep': 'CEP',
      'endereco': 'Endereço',
      'numero': 'Número',
      'complemento': 'Complemento',
      'bairro': 'Bairro',
      'cidade': 'Cidade',
      'estado': 'Estado',
      'telefone': 'Telefone',
      'celular': 'Celular',
      'email': 'Email',
      'profissao': 'Profissão',
      'status': 'Status'
    };
    return labels[fieldName] || fieldName;
  }
}
