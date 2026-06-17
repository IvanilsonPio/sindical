import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface InputDialogData {
  title: string;
  label: string;
  placeholder?: string;
  confirmText?: string;
  cancelText?: string;
}

@Component({
  selector: 'app-input-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" (ngSubmit)="confirmar()" class="form">
        <mat-form-field appearance="outline">
          <mat-label>{{ data.label }}</mat-label>
          <input matInput formControlName="valor" [placeholder]="data.placeholder || ''" autofocus>
          <mat-error *ngIf="form.get('valor')?.hasError('required')">Campo obrigatório</mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="cancelar()">{{ data.cancelText || 'Cancelar' }}</button>
      <button mat-raised-button color="primary" [disabled]="form.invalid" (click)="confirmar()">
        {{ data.confirmText || 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`.form { padding-top: 0.5rem; min-width: 320px; }`]
})
export class InputDialogComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<InputDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: InputDialogData
  ) {
    this.form = this.fb.group({
      valor: ['', Validators.required]
    });
  }

  confirmar(): void {
    if (this.form.invalid) return;
    this.dialogRef.close(this.form.value.valor);
  }

  cancelar(): void {
    this.dialogRef.close(null);
  }
}
