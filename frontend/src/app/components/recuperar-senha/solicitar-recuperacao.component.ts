import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-solicitar-recuperacao',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="container">
      <mat-card class="card">
        <mat-card-header>
          <mat-card-title>Recuperar Senha</mat-card-title>
          <mat-card-subtitle>Informe seu e-mail para receber o link de recuperação</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <div *ngIf="enviado" class="success-box">
            <mat-icon>check_circle</mat-icon>
            <p>Se o e-mail informado estiver cadastrado, você receberá as instruções em breve.</p>
          </div>

          <form *ngIf="!enviado" [formGroup]="form" (ngSubmit)="enviar()" class="form">
            <mat-form-field appearance="outline">
              <mat-label>E-mail</mat-label>
              <input matInput type="email" formControlName="email" autocomplete="email">
              <mat-error *ngIf="form.get('email')?.hasError('required')">E-mail é obrigatório</mat-error>
              <mat-error *ngIf="form.get('email')?.hasError('email')">E-mail inválido</mat-error>
            </mat-form-field>

            <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || loading">
              {{ loading ? 'Enviando...' : 'Enviar link de recuperação' }}
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <a mat-button routerLink="/login">Voltar ao login</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .container { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 1rem; }
    .card { width: 100%; max-width: 420px; }
    .form { display: flex; flex-direction: column; gap: 1rem; padding-top: 1rem; }
    .success-box { display: flex; align-items: flex-start; gap: 0.75rem; padding: 1rem; background: #e8f5e9; border-radius: 4px; margin-top: 1rem; }
    .success-box mat-icon { color: #388e3c; margin-top: 2px; }
    .success-box p { margin: 0; color: #1b5e20; }
  `]
})
export class SolicitarRecuperacaoComponent {
  form: FormGroup;
  loading = false;
  enviado = false;

  constructor(private fb: FormBuilder, private usuarioService: UsuarioService) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  enviar(): void {
    if (this.form.invalid) return;
    this.loading = true;

    this.usuarioService.solicitarRecuperacao(this.form.value.email).subscribe({
      next: () => {
        this.enviado = true;
        this.loading = false;
      },
      error: () => {
        // Mesmo em caso de erro, mostra a mesma mensagem por segurança
        this.enviado = true;
        this.loading = false;
      }
    });
  }
}
