import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { Inject } from '@angular/core';
import { UsuarioService, UsuarioAdmin, UsuarioRequest } from '../../services/usuario.service';
import { ConfirmDialogComponent } from '../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-usuarios',
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
  template: `
    <div class="page-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <h1>Gestão de Usuários</h1>
              <button mat-raised-button color="primary" (click)="abrirFormulario()">
                <mat-icon>person_add</mat-icon>
                Novo Usuário
              </button>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <table mat-table [dataSource]="usuarios" class="tabela">
            <ng-container matColumnDef="nome">
              <th mat-header-cell *matHeaderCellDef>Nome</th>
              <td mat-cell *matCellDef="let u">{{ u.nome }}</td>
            </ng-container>

            <ng-container matColumnDef="username">
              <th mat-header-cell *matHeaderCellDef>Username</th>
              <td mat-cell *matCellDef="let u">{{ u.username }}</td>
            </ng-container>

            <ng-container matColumnDef="role">
              <th mat-header-cell *matHeaderCellDef>Perfil</th>
              <td mat-cell *matCellDef="let u">
                <mat-chip [color]="u.role === 'ADMIN' ? 'primary' : 'accent'" highlighted>
                  {{ u.role === 'ADMIN' ? 'Administrador' : 'Operador' }}
                </mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let u">
                <mat-chip [color]="u.status === 'ATIVO' ? 'primary' : 'warn'" highlighted>
                  {{ u.status === 'ATIVO' ? 'Ativo' : 'Inativo' }}
                </mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="acoes">
              <th mat-header-cell *matHeaderCellDef>Ações</th>
              <td mat-cell *matCellDef="let u">
                <button mat-icon-button matTooltip="Editar" (click)="abrirFormulario(u)">
                  <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button
                  [matTooltip]="u.status === 'ATIVO' ? 'Desativar' : 'Ativar'"
                  (click)="alterarStatus(u)">
                  <mat-icon>{{ u.status === 'ATIVO' ? 'block' : 'check_circle' }}</mat-icon>
                </button>
                <button mat-icon-button matTooltip="Resetar senha" (click)="resetarSenha(u)">
                  <mat-icon>lock_reset</mat-icon>
                </button>
                <button mat-icon-button matTooltip="Excluir" color="warn" (click)="excluir(u)">
                  <mat-icon>delete</mat-icon>
                </button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr mat-row *matRowDef="let row; columns: colunas"></tr>
          </table>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .page-container { padding: 2rem; }
    .header-content { display: flex; justify-content: space-between; align-items: center; width: 100%; }
    .header-content h1 { margin: 0; }
    .tabela { width: 100%; }
  `]
})
export class UsuariosComponent implements OnInit {
  usuarios: UsuarioAdmin[] = [];
  colunas = ['nome', 'username', 'role', 'status', 'acoes'];

  constructor(
    private usuarioService: UsuarioService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.usuarioService.listar().subscribe({
      next: (usuarios) => this.usuarios = usuarios,
      error: () => this.snackBar.open('Erro ao carregar usuários', 'Fechar', { duration: 3000 })
    });
  }

  abrirFormulario(usuario?: UsuarioAdmin): void {
    const ref = this.dialog.open(UsuarioFormDialogComponent, {
      width: '480px',
      data: usuario || null
    });

    ref.afterClosed().subscribe(result => {
      if (result) this.carregar();
    });
  }

  alterarStatus(usuario: UsuarioAdmin): void {
    const novoStatus = usuario.status === 'ATIVO' ? 'INATIVO' : 'ATIVO';
    const acao = novoStatus === 'ATIVO' ? 'ativar' : 'desativar';

    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: novoStatus === 'ATIVO' ? 'Ativar Usuário' : 'Desativar Usuário',
        message: `Deseja realmente ${acao} o usuário "${usuario.nome}"?`,
        confirmText: novoStatus === 'ATIVO' ? 'Ativar' : 'Desativar'
      }
    });

    ref.afterClosed().subscribe(confirmado => {
      if (!confirmado) return;
      this.usuarioService.alterarStatus(usuario.id, novoStatus).subscribe({
        next: () => {
          this.snackBar.open(`Usuário ${acao === 'ativar' ? 'ativado' : 'desativado'} com sucesso`, 'Fechar', { duration: 3000 });
          this.carregar();
        },
        error: (err) => {
          const msg = err.error?.message || `Erro ao ${acao} usuário`;
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
        }
      });
    });
  }

  excluir(usuario: UsuarioAdmin): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Excluir Usuário',
        message: `Deseja realmente excluir o usuário "${usuario.nome}"?`,
        confirmText: 'Excluir'
      }
    });

    ref.afterClosed().subscribe(confirmado => {
      if (!confirmado) return;
      this.usuarioService.excluir(usuario.id).subscribe({
        next: () => {
          this.snackBar.open('Usuário excluído com sucesso', 'Fechar', { duration: 3000 });
          this.carregar();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erro ao excluir usuário';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
        }
      });
    });
  }

  resetarSenha(usuario: UsuarioAdmin): void {
    const ref = this.dialog.open(ResetarSenhaDialogComponent, {
      width: '400px',
      data: usuario
    });
    ref.afterClosed().subscribe(result => {
      if (result) this.snackBar.open('Senha redefinida com sucesso', 'Fechar', { duration: 3000 });
    });
  }
}

// Dialog de formulário
@Component({
  selector: 'app-usuario-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data ? 'Editar Usuário' : 'Novo Usuário' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="form">
        <mat-form-field appearance="outline">
          <mat-label>Nome</mat-label>
          <input matInput formControlName="nome">
          <mat-error *ngIf="form.get('nome')?.hasError('required')">Nome é obrigatório</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Username</mat-label>
          <input matInput formControlName="username">
          <mat-error *ngIf="form.get('username')?.hasError('required')">Username é obrigatório</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>{{ data ? 'Nova Senha (deixe em branco para manter)' : 'Senha' }}</mat-label>
          <input matInput type="password" formControlName="password">
          <mat-error *ngIf="form.get('password')?.hasError('minlength')">Mínimo 6 caracteres</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>E-mail</mat-label>
          <input matInput type="email" formControlName="email">
          <mat-error *ngIf="form.get('email')?.hasError('email')">E-mail inválido</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Perfil</mat-label>
          <mat-select formControlName="role">
            <mat-option value="ADMIN">Administrador</mat-option>
            <mat-option value="OPERADOR">Operador</mat-option>
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-raised-button color="primary" [disabled]="form.invalid" (click)="salvar()">
        Salvar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`.form { display: flex; flex-direction: column; gap: 1rem; padding-top: 0.5rem; min-width: 400px; }`]
})
export class UsuarioFormDialogComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<UsuarioFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UsuarioAdmin | null
  ) {
    this.form = this.fb.group({
      nome: [data?.nome || '', Validators.required],
      username: [data?.username || '', Validators.required],
      password: ['', data ? [] : [Validators.required, Validators.minLength(6)]],
      email: [data?.email || '', [Validators.email]],
      role: [data?.role || 'OPERADOR', Validators.required]
    });
  }

  salvar(): void {
    if (this.form.invalid) return;

    const request: UsuarioRequest = this.form.value;

    const op = this.data
      ? this.usuarioService.atualizar(this.data.id, request)
      : this.usuarioService.criar(request);

    op.subscribe({
      next: () => {
        this.snackBar.open(
          this.data ? 'Usuário atualizado com sucesso' : 'Usuário criado com sucesso',
          'Fechar', { duration: 3000 }
        );
        this.dialogRef.close(true);
      },
      error: (err) => {
        const msg = err.error?.message || 'Erro ao salvar usuário';
        this.snackBar.open(msg, 'Fechar', { duration: 4000 });
      }
    });
  }
}

// Dialog de reset de senha (admin)
@Component({
  selector: 'app-resetar-senha-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Redefinir Senha — {{ data.nome }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="form">
        <mat-form-field appearance="outline">
          <mat-label>Nova senha</mat-label>
          <input matInput type="password" formControlName="novaSenha">
          <mat-error *ngIf="form.get('novaSenha')?.hasError('required')">Obrigatório</mat-error>
          <mat-error *ngIf="form.get('novaSenha')?.hasError('minlength')">Mínimo 6 caracteres</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Confirmar nova senha</mat-label>
          <input matInput type="password" formControlName="confirmacao">
          <mat-error *ngIf="form.hasError('mismatch') && form.get('confirmacao')?.touched">
            As senhas não conferem
          </mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-raised-button color="warn" [disabled]="form.invalid" (click)="salvar()">
        Redefinir
      </button>
    </mat-dialog-actions>
  `,
  styles: [`.form { display: flex; flex-direction: column; gap: 1rem; padding-top: 0.5rem; min-width: 320px; }`]
})
export class ResetarSenhaDialogComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<ResetarSenhaDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UsuarioAdmin
  ) {
    this.form = this.fb.group({
      novaSenha: ['', [Validators.required, Validators.minLength(6)]],
      confirmacao: ['', Validators.required]
    }, { validators: (g) => g.get('novaSenha')?.value === g.get('confirmacao')?.value ? null : { mismatch: true } });
  }

  salvar(): void {
    if (this.form.invalid) return;

    this.usuarioService.resetarSenha(this.data.id, this.form.value.novaSenha).subscribe({
      next: () => this.dialogRef.close(true),
      error: (err) => {
        const msg = err.error?.message || 'Erro ao redefinir senha';
        this.snackBar.open(msg, 'Fechar', { duration: 4000 });
      }
    });
  }
}
