import { Component, Inject, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon aria-hidden="true">warning</mat-icon>
      {{ data.title }}
    </h2>
    <mat-dialog-content>
      <p role="alert">{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()" aria-label="Cancelar ação" #cancelButton>
        {{ data.cancelText || 'Cancelar' }}
      </button>
      <button mat-raised-button color="warn" (click)="onConfirm()" aria-label="Confirmar ação" #confirmButton>
        {{ data.confirmText || 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
    }

    mat-icon {
      color: #f44336;
    }

    mat-dialog-content {
      padding: 20px 0;
    }

    mat-dialog-actions {
      padding: 8px 0 0 0;
      margin: 0;
    }

    /* Visible focus indicators for keyboard navigation */
    button:focus {
      outline: 2px solid #1976d2;
      outline-offset: 2px;
    }

    button[color="warn"]:focus {
      outline-color: #f44336;
    }
  `]
})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData
  ) {}

  /**
   * Handle keyboard events for dialog navigation
   * Enter key confirms the action
   * Escape key cancels the action
   */
  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.onConfirm();
    } else if (event.key === 'Escape') {
      event.preventDefault();
      this.onCancel();
    }
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
