import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { SocioService } from '../../../services/socio.service';
import { SocioRequest } from '../../../models/socio.model';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-socio-form',
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
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './socio-form.component.html',
  styleUrls: ['./socio-form.component.scss']
})
export class SocioFormComponent implements OnInit {
  socioForm!: FormGroup;
  isEditMode = false;
  socioId?: number;
  loading = false;
  photoPreview?: string;
  selectedPhoto?: File;
  private initialFormValue: any;

  estados = [
    'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
    'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
    'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
  ];

  constructor(
    private fb: FormBuilder,
    private socioService: SocioService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.initForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.socioId = +id;
      this.loadSocio(this.socioId);
    }
  }

  /**
   * Custom validator for Brazilian CPF format
   * Validates format: XXX.XXX.XXX-XX (11 digits)
   * Requirements: 2.3
   */
  static validateCPF(control: any): { [key: string]: any } | null {
    if (!control.value) {
      return null; // Don't validate empty values (use Validators.required for that)
    }

    const cpf = control.value.replace(/\D/g, '');

    // Check if has 11 digits
    if (cpf.length !== 11) {
      return { cpfInvalid: { message: 'CPF deve conter 11 dígitos' } };
    }

    // Check if all digits are the same (invalid CPF)
    if (/^(\d)\1{10}$/.test(cpf)) {
      return { cpfInvalid: { message: 'CPF inválido' } };
    }

    // Validate CPF check digits
    let sum = 0;
    let remainder;

    // Validate first check digit
    for (let i = 1; i <= 9; i++) {
      sum += parseInt(cpf.substring(i - 1, i)) * (11 - i);
    }
    remainder = (sum * 10) % 11;
    if (remainder === 10 || remainder === 11) {
      remainder = 0;
    }
    if (remainder !== parseInt(cpf.substring(9, 10))) {
      return { cpfInvalid: { message: 'CPF inválido' } };
    }

    // Validate second check digit
    sum = 0;
    for (let i = 1; i <= 10; i++) {
      sum += parseInt(cpf.substring(i - 1, i)) * (12 - i);
    }
    remainder = (sum * 10) % 11;
    if (remainder === 10 || remainder === 11) {
      remainder = 0;
    }
    if (remainder !== parseInt(cpf.substring(10, 11))) {
      return { cpfInvalid: { message: 'CPF inválido' } };
    }

    return null;
  }

  /**
   * Custom validator for Brazilian phone format
   * Validates format: (XX) XXXXX-XXXX or (XX) XXXX-XXXX
   * Requirements: 2.4
   */
  static validateTelefone(control: any): { [key: string]: any } | null {
    if (!control.value) {
      return null; // Don't validate empty values
    }

    const telefone = control.value.replace(/\D/g, '');

    // Check if has 10 or 11 digits (with area code)
    if (telefone.length < 10 || telefone.length > 11) {
      return { telefoneInvalid: { message: 'Telefone deve conter 10 ou 11 dígitos' } };
    }

    // Check if area code is valid (11-99)
    const areaCode = parseInt(telefone.substring(0, 2));
    if (areaCode < 11 || areaCode > 99) {
      return { telefoneInvalid: { message: 'DDD inválido' } };
    }

    // Check if first digit of phone number is valid (2-9 for landline, 9 for mobile)
    const firstDigit = parseInt(telefone.substring(2, 3));
    if (telefone.length === 11 && firstDigit !== 9) {
      return { telefoneInvalid: { message: 'Celular deve começar com 9' } };
    }
    if (telefone.length === 10 && (firstDigit < 2 || firstDigit > 5)) {
      return { telefoneInvalid: { message: 'Telefone fixo inválido' } };
    }

    return null;
  }

  /**
   * Custom validator for Brazilian CEP format
   * Validates format: XXXXX-XXX (8 digits)
   * Requirements: 2.5
   */
  static validateCEP(control: any): { [key: string]: any } | null {
    if (!control.value) {
      return null; // Don't validate empty values
    }

    const cep = control.value.replace(/\D/g, '');

    // Check if has exactly 8 digits
    if (cep.length !== 8) {
      return { cepInvalid: { message: 'CEP deve conter 8 dígitos' } };
    }

    // Check if all digits are the same (unlikely valid CEP)
    if (/^(\d)\1{7}$/.test(cep)) {
      return { cepInvalid: { message: 'CEP inválido' } };
    }

    return null;
  }

  /**
   * Custom validator for email format (RFC 5322 compliant)
   * Requirements: 2.6
   */
  static validateEmail(control: any): { [key: string]: any } | null {
    if (!control.value) {
      return null; // Don't validate empty values
    }

    // RFC 5322 compliant email regex (simplified but comprehensive)
    const emailRegex = /^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;

    if (!emailRegex.test(control.value)) {
      return { emailInvalid: { message: 'E-mail inválido' } };
    }

    // Additional checks
    const email = control.value.toLowerCase();

    // Check for consecutive dots
    if (email.includes('..')) {
      return { emailInvalid: { message: 'E-mail não pode conter pontos consecutivos' } };
    }

    // Check if starts or ends with dot
    const localPart = email.split('@')[0];
    if (localPart.startsWith('.') || localPart.endsWith('.')) {
      return { emailInvalid: { message: 'E-mail inválido' } };
    }

    return null;
  }

  private initForm(): void {
    this.socioForm = this.fb.group({
      // Dados pessoais
      nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      cpf: ['', [Validators.required, SocioFormComponent.validateCPF]],
      matricula: ['', [Validators.required, Validators.maxLength(20)]],
      rg: ['', [Validators.maxLength(20)]],
      dataNascimento: [''],
      estadoCivil: ['', [Validators.maxLength(20)]],
      
      // Endereço completo
      cep: ['', [SocioFormComponent.validateCEP]],
      endereco: ['', [Validators.maxLength(255)]],
      numero: ['', [Validators.maxLength(10)]],
      complemento: ['', [Validators.maxLength(100)]],
      bairro: ['', [Validators.maxLength(100)]],
      cidade: ['', [Validators.maxLength(100)]],
      estado: ['', [Validators.maxLength(2)]],
      
      // Contato
      telefone: ['', [SocioFormComponent.validateTelefone]],
      celular: ['', [SocioFormComponent.validateTelefone]],
      email: ['', [SocioFormComponent.validateEmail, Validators.maxLength(100)]],
      
      // Outros
      profissao: ['']
    });

    // Store initial form value for comparison
    this.initialFormValue = this.socioForm.value;
  }

  private loadSocio(id: number): void {
    this.loading = true;
    this.socioService.buscarSocio(id).subscribe({
      next: (socio) => {
        this.socioForm.patchValue({
          nome: socio.nome,
          cpf: socio.cpf,
          matricula: socio.matricula,
          rg: socio.rg,
          dataNascimento: socio.dataNascimento,
          estadoCivil: socio.estadoCivil,
          cep: socio.cep,
          endereco: socio.endereco,
          numero: socio.numero,
          complemento: socio.complemento,
          bairro: socio.bairro,
          cidade: socio.cidade,
          estado: socio.estado,
          telefone: socio.telefone,
          celular: socio.celular,
          email: socio.email,
          profissao: socio.profissao
        });
        // Update initial form value after loading data
        this.initialFormValue = this.socioForm.value;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Erro ao carregar sócio', 'Fechar', { duration: 3000 });
        this.loading = false;
        this.router.navigate(['/socios']);
      }
    });
  }

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      // Validate file type
      if (!file.type.startsWith('image/')) {
        this.snackBar.open('Por favor, selecione uma imagem válida', 'Fechar', { duration: 3000 });
        return;
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.snackBar.open('A imagem deve ter no máximo 5MB', 'Fechar', { duration: 3000 });
        return;
      }

      this.selectedPhoto = file;

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.photoPreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  removePhoto(): void {
    this.selectedPhoto = undefined;
    this.photoPreview = undefined;
  }

  applyCpfMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');

    if (value.length <= 11) {
      value = value.replace(/(\d{3})(\d)/, '$1.$2');
      value = value.replace(/(\d{3})(\d)/, '$1.$2');
      value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    }

    this.socioForm.patchValue({ cpf: value }, { emitEvent: false });
  }

  applyTelefoneMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');

    if (value.length <= 11) {
      if (value.length <= 10) {
        value = value.replace(/(\d{2})(\d)/, '($1) $2');
        value = value.replace(/(\d{4})(\d)/, '$1-$2');
      } else {
        value = value.replace(/(\d{2})(\d)/, '($1) $2');
        value = value.replace(/(\d{5})(\d)/, '$1-$2');
      }
    }

    this.socioForm.patchValue({ telefone: value }, { emitEvent: false });
  }

  applyCelularMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');

    if (value.length <= 11) {
      if (value.length <= 10) {
        value = value.replace(/(\d{2})(\d)/, '($1) $2');
        value = value.replace(/(\d{4})(\d)/, '$1-$2');
      } else {
        value = value.replace(/(\d{2})(\d)/, '($1) $2');
        value = value.replace(/(\d{5})(\d)/, '$1-$2');
      }
    }

    this.socioForm.patchValue({ celular: value }, { emitEvent: false });
  }

  applyCepMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');

    if (value.length <= 8) {
      value = value.replace(/(\d{5})(\d)/, '$1-$2');
    }

    this.socioForm.patchValue({ cep: value }, { emitEvent: false });
  }

  onSubmit(): void {
    if (this.socioForm.invalid) {
      this.socioForm.markAllAsTouched();
      this.snackBar.open('Por favor, preencha todos os campos obrigatórios corretamente', 'Fechar', { duration: 3000 });
      return;
    }

    this.loading = true;
    const socioData: SocioRequest = this.socioForm.value;

    const operation = this.isEditMode
      ? this.socioService.atualizarSocio(this.socioId!, socioData)
      : this.socioService.criarSocio(socioData);

    operation.subscribe({
      next: (response) => {
        // Requirements 2.11, 2.12: Show success message and navigate
        const message = this.isEditMode ? 'Dados atualizados com sucesso' : 'Sócio cadastrado com sucesso';
        this.snackBar.open(message, 'Fechar', { duration: 3000 });
        
        // Navigate to detail view after update, or list after creation
        if (this.isEditMode && this.socioId) {
          this.router.navigate(['/socios', this.socioId, 'detalhes']);
        } else {
          // For new socio, navigate to its detail view if we have the ID
          const newSocioId = response?.id || response;
          if (newSocioId) {
            this.router.navigate(['/socios', newSocioId, 'detalhes']);
          } else {
            this.router.navigate(['/socios']);
          }
        }
      },
      error: (error) => {
        let errorMessage = 'Erro ao salvar sócio';
        if (error.error?.message) {
          errorMessage = error.error.message;
        }
        this.snackBar.open(errorMessage, 'Fechar', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  /**
   * Check if form has unsaved changes
   * Requirements: 2.13
   */
  private hasUnsavedChanges(): boolean {
    return JSON.stringify(this.socioForm.value) !== JSON.stringify(this.initialFormValue);
  }

  /**
   * Handle cancel action with confirmation if there are unsaved changes
   * Requirements: 2.13, 4.3
   */
  onCancel(): void {
    // Check if there are unsaved changes
    if (this.hasUnsavedChanges()) {
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        width: '400px',
        data: {
          title: 'Descartar alterações?',
          message: 'Você tem alterações não salvas. Deseja realmente descartar essas alterações?',
          confirmText: 'Descartar',
          cancelText: 'Continuar editando'
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          // User confirmed, navigate away
          this.navigateBack();
        }
        // If result is false or undefined, do nothing (stay on form)
      });
    } else {
      // No changes, navigate away immediately
      this.navigateBack();
    }
  }

  /**
   * Navigate back to appropriate view
   * If in edit mode, return to detail view
   * If creating new socio, return to list
   * Requirements: 4.3
   */
  private navigateBack(): void {
    if (this.isEditMode && this.socioId) {
      // Return to detail view when editing
      this.router.navigate(['/socios', this.socioId, 'detalhes']);
    } else {
      // Return to list when creating new socio
      this.router.navigate(['/socios']);
    }
  }

  getErrorMessage(fieldName: string): string {
    const field = this.socioForm.get(fieldName);
    if (!field || !field.errors || !field.touched) {
      return '';
    }

    if (field.errors['required']) {
      return 'Este campo é obrigatório';
    }
    if (field.errors['minlength']) {
      return `Mínimo de ${field.errors['minlength'].requiredLength} caracteres`;
    }
    if (field.errors['maxlength']) {
      return `Máximo de ${field.errors['maxlength'].requiredLength} caracteres`;
    }
    if (field.errors['cpfInvalid']) {
      return field.errors['cpfInvalid'].message || 'CPF inválido';
    }
    if (field.errors['telefoneInvalid']) {
      return field.errors['telefoneInvalid'].message || 'Telefone inválido';
    }
    if (field.errors['cepInvalid']) {
      return field.errors['cepInvalid'].message || 'CEP inválido';
    }
    if (field.errors['emailInvalid']) {
      return field.errors['emailInvalid'].message || 'E-mail inválido';
    }
    if (field.errors['pattern']) {
      if (fieldName === 'cpf') return 'CPF inválido (formato: 000.000.000-00)';
      if (fieldName === 'telefone' || fieldName === 'celular') return 'Telefone inválido (formato: (00) 00000-0000)';
      if (fieldName === 'cep') return 'CEP inválido (formato: 00000-000)';
    }
    if (field.errors['email']) {
      return 'E-mail inválido';
    }

    return 'Campo inválido';
  }
}
