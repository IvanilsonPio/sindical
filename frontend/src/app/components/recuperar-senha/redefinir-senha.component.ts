import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-redefinir-senha',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  template: `
    <div class="container">
      <mat-card class="card">
        <mat-card-header>
          <mat-card-title>Redefinir Senha</mat-card-title>
          <mat-card-subtitle>Crie uma nova senha para sua conta</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <div *ngIf="!token" class="error-box">
            <mat-icon>error</mat-icon>
            <p>Link inválido. Solicite uma nova recuperação de senha.</p>
          </div>

          <div *ngIf="concluido" class="success-box">
            <mat-icon>check_circle</mat-icon>
            <p>Senha redefinida com sucesso! Você já pode fazer login.</p>
          </div>

          <form *ngIf="token && !concluido" [formGroup]="form" (ngSubmit)="salvar()" class="form">
            <mat-form-field appearance="outline">
              <mat-label>Nova senha</mat-label>
              <input matInput [type]="hide.nova ? 'password' : 'text'" formControlName="novaSenha">
              <button mat-icon-button matSuffix type="button" (click)="hide.nova = !hide.nova">
                <mat-icon>{{ hide.nova ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="form.get('novaSenha')?.hasError('required')">Obrigatório</mat-error>
              <mat-error *ngIf="form.get('novaSenha')?.hasError('minlength')">Mínimo 6 caracteres</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Confirmar nova senha</mat-label>
              <input matInput [type]="hide.confirmacao ? 'password' : 'text'" formControlName="confirmacao">
              <button mat-icon-button matSuffix type="button" (click)="hide.confirmacao = !hide.confirmacao">
                <mat-icon>{{ hide.confirmacao ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="form.hasError('mismatch') && form.get('confirmacao')?.touched">
                As senhas não conferem
              </mat-error>
            </mat-form-field>

            <div *ngIf="erro" class="error-box">
              <mat-icon>error</mat-icon>
              <p>{{ erro }}</p>
            </div>

            <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || loading">
              {{ loading ? 'Salvando...' : 'Redefinir senha' }}
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
    .success-box mat-icon { color: #388e3c; }
    .success-box p { margin: 0; color: #1b5e20; }
    .error-box { display: flex; align-items: flex-start; gap: 0.75rem; padding: 1rem; background: #ffebee; border-radius: 4px; margin-top: 1rem; }
    .error-box mat-icon { color: #c62828; }
    .error-box p { margin: 0; color: #b71c1c; }
  `]
})
export class RedefinirSenhaComponent implements OnInit {
  form: FormGroup;
  token: string | null = null;
  loading = false;
  concluido = false;
  erro = '';
  hide = { nova: true, confirmacao: true };

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private usuarioService: UsuarioService
  ) {
    this.form = this.fb.group({
      novaSenha: ['', [Validators.required, Validators.minLength(6)]],
      confirmacao: ['', Validators.required]
    }, { validators: (g: AbstractControl) =>
      g.get('novaSenha')?.value === g.get('confirmacao')?.value ? null : { mismatch: true }
    });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token');
  }

  salvar(): void {
    if (this.form.invalid || !this.token) return;
    this.loading = true;
    this.erro = '';

    this.usuarioService.redefinirSenhaComToken(this.token, this.form.value.novaSenha).subscribe({
      next: () => {
        this.concluido = true;
        this.loading = false;
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: (err) => {
        this.erro = err.error?.message || 'Token inválido ou expirado. Solicite uma nova recuperação.';
        this.loading = false;
      }
    });
  }
}
