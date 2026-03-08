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
import { SocioService } from '../../../services/socio.service';
import { SocioRequest } from '../../../models/socio.model';

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
    MatSnackBarModule
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
    private snackBar: MatSnackBar
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

  private initForm(): void {
    this.socioForm = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3)]],
      cpf: ['', [Validators.required, Validators.pattern(/^\d{3}\.\d{3}\.\d{3}-\d{2}$/)]],
      matricula: ['', [Validators.required]],
      rg: [''],
      dataNascimento: [''],
      telefone: ['', [Validators.pattern(/^\(\d{2}\)\s\d{4,5}-\d{4}$/)]],
      email: ['', [Validators.email]],
      endereco: [''],
      cidade: [''],
      estado: [''],
      cep: ['', [Validators.pattern(/^\d{5}-\d{3}$/)]],
      profissao: ['']
    });
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
          telefone: socio.telefone,
          email: socio.email,
          endereco: socio.endereco,
          cidade: socio.cidade,
          estado: socio.estado,
          cep: socio.cep,
          profissao: socio.profissao
        });
        this.loading = false;
      },
      error: (error) => {
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
      next: () => {
        const message = this.isEditMode ? 'Sócio atualizado com sucesso' : 'Sócio cadastrado com sucesso';
        this.snackBar.open(message, 'Fechar', { duration: 3000 });
        this.router.navigate(['/socios']);
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

  onCancel(): void {
    this.router.navigate(['/socios']);
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
    if (field.errors['pattern']) {
      if (fieldName === 'cpf') return 'CPF inválido (formato: 000.000.000-00)';
      if (fieldName === 'telefone') return 'Telefone inválido (formato: (00) 00000-0000)';
      if (fieldName === 'cep') return 'CEP inválido (formato: 00000-000)';
    }
    if (field.errors['email']) {
      return 'E-mail inválido';
    }

    return 'Campo inválido';
  }
}
