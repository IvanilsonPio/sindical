import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-alterar-senha',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  template: `
    <div class="page-container">
      <mat-card class="form-card">
        <mat-card-header>
          <mat-card-title>Alterar Senha</mat-card-title>
          <mat-card-subtitle>Preencha os campos para definir uma nova senha</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="salvar()" class="form">
            <mat-form-field appearance="outline">
              <mat-label>Senha atual</mat-label>
              <input matInput [type]="hide.atual ? 'password' : 'text'" formControlName="senhaAtual">
              <button mat-icon-button matSuffix type="button" (click)="hide.atual = !hide.atual">
                <mat-icon>{{ hide.atual ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="form.get('senhaAtual')?.hasError('required')">Senha atual é obrigatória</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Nova senha</mat-label>
              <input matInput [type]="hide.nova ? 'password' : 'text'" formControlName="novaSenha">
              <button mat-icon-button matSuffix type="button" (click)="hide.nova = !hide.nova">
                <mat-icon>{{ hide.nova ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="form.get('novaSenha')?.hasError('required')">Nova senha é obrigatória</mat-error>
              <mat-error *ngIf="form.get('novaSenha')?.hasError('minlength')">Mínimo 6 caracteres</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Confirmar nova senha</mat-label>
              <input matInput [type]="hide.confirmacao ? 'password' : 'text'" formControlName="confirmacaoSenha">
              <button mat-icon-button matSuffix type="button" (click)="hide.confirmacao = !hide.confirmacao">
                <mat-icon>{{ hide.confirmacao ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="form.get('confirmacaoSenha')?.hasError('required')">Confirmação é obrigatória</mat-error>
              <mat-error *ngIf="form.hasError('mismatch') && form.get('confirmacaoSenha')?.touched">
                As senhas não conferem
              </mat-error>
            </mat-form-field>

            <div class="actions">
              <button mat-button type="button" (click)="voltar()">Cancelar</button>
              <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || loading">
                {{ loading ? 'Salvando...' : 'Salvar nova senha' }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .page-container { padding: 2rem; display: flex; justify-content: center; }
    .form-card { width: 100%; max-width: 480px; }
    .form { display: flex; flex-direction: column; gap: 1rem; padding-top: 1rem; }
    .actions { display: flex; justify-content: flex-end; gap: 1rem; }
  `]
})
export class AlterarSenhaComponent {
  form: FormGroup;
  loading = false;
  hide = { atual: true, nova: true, confirmacao: true };

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {
    this.form = this.fb.group({
      senhaAtual: ['', Validators.required],
      novaSenha: ['', [Validators.required, Validators.minLength(6)]],
      confirmacaoSenha: ['', Validators.required]
    }, { validators: this.senhasIguais });
  }

  senhasIguais(group: AbstractControl) {
    const nova = group.get('novaSenha')?.value;
    const confirmacao = group.get('confirmacaoSenha')?.value;
    return nova === confirmacao ? null : { mismatch: true };
  }

  salvar(): void {
    if (this.form.invalid) return;

    this.loading = true;
    const { senhaAtual, novaSenha, confirmacaoSenha } = this.form.value;

    this.usuarioService.alterarPropriaSenha(senhaAtual, novaSenha, confirmacaoSenha).subscribe({
      next: () => {
        this.snackBar.open('Senha alterada com sucesso', 'Fechar', { duration: 3000 });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.message || 'Erro ao alterar senha';
        this.snackBar.open(msg, 'Fechar', { duration: 4000 });
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/dashboard']);
  }
}
