import { ComponentFixture, TestBed, fakeAsync, tick, flush } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { SocioFormComponent } from './socio-form.component';
import { SocioService } from '../../../services/socio.service';
import { Socio, SocioRequest } from '../../../models/socio.model';
import { StatusSocio } from '../../../models/enums';

describe('SocioFormComponent', () => {
  let component: SocioFormComponent;
  let fixture: ComponentFixture<SocioFormComponent>;
  let socioService: jasmine.SpyObj<SocioService>;
  let router: jasmine.SpyObj<Router>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;
  let activatedRoute: any;

  const mockSocio: Socio = {
    id: 1,
    nome: 'João Silva',
    cpf: '123.456.789-00',
    matricula: 'MAT001',
    rg: '12345678',
    dataNascimento: '1980-01-01',
    telefone: '(11) 98765-4321',
    email: 'joao@example.com',
    endereco: 'Rua Teste, 123',
    cidade: 'São Paulo',
    estado: 'SP',
    cep: '12345-678',
    profissao: 'Agricultor',
    status: StatusSocio.ATIVO,
    criadoEm: '2024-01-01T00:00:00',
    atualizadoEm: '2024-01-01T00:00:00'
  };

  beforeEach(async () => {
    const socioServiceSpy = jasmine.createSpyObj('SocioService', [
      'buscarSocio',
      'criarSocio',
      'atualizarSocio'
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);
    snackBarSpy.open.and.returnValue({ dismiss: () => {} } as any);

    activatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy('get').and.returnValue(null)
        }
      }
    };

    await TestBed.configureTestingModule({
      imports: [
        SocioFormComponent,
        ReactiveFormsModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: SocioService, useValue: socioServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: ActivatedRoute, useValue: activatedRoute }
      ]
    }).compileComponents();

    socioService = TestBed.inject(SocioService) as jasmine.SpyObj<SocioService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    snackBar = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;

    fixture = TestBed.createComponent(SocioFormComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Initialization', () => {
    it('should initialize form with empty values in create mode', () => {
      fixture.detectChanges();
      
      expect(component.isEditMode).toBeFalse();
      expect(component.socioForm.get('nome')?.value).toBe('');
      expect(component.socioForm.get('cpf')?.value).toBe('');
      expect(component.socioForm.get('matricula')?.value).toBe('');
    });

    it('should load socio data in edit mode', () => {
      activatedRoute.snapshot.paramMap.get.and.returnValue('1');
      socioService.buscarSocio.and.returnValue(of(mockSocio));

      fixture.detectChanges();

      expect(component.isEditMode).toBeTrue();
      expect(component.socioId).toBe(1);
      expect(socioService.buscarSocio).toHaveBeenCalledWith(1);
      expect(component.socioForm.get('nome')?.value).toBe('João Silva');
      expect(component.socioForm.get('cpf')?.value).toBe('123.456.789-00');
    });

    it('should handle error when loading socio fails', fakeAsync(() => {
      activatedRoute.snapshot.paramMap.get.and.returnValue('1');
      socioService.buscarSocio.and.returnValue(throwError(() => new Error('Not found')));

      fixture.detectChanges();
      tick();

      expect(router.navigate).toHaveBeenCalledWith(['/socios']);
      
      flush();
    }));
  });

  describe('Form Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should require nome field', () => {
      const nome = component.socioForm.get('nome');
      nome?.setValue('');
      expect(nome?.hasError('required')).toBeTrue();
    });

    it('should validate nome minimum length', () => {
      const nome = component.socioForm.get('nome');
      nome?.setValue('Jo');
      expect(nome?.hasError('minlength')).toBeTrue();
    });

    it('should require cpf field', () => {
      const cpf = component.socioForm.get('cpf');
      cpf?.setValue('');
      expect(cpf?.hasError('required')).toBeTrue();
    });

    it('should validate cpf pattern', () => {
      const cpf = component.socioForm.get('cpf');
      cpf?.setValue('12345678900');
      expect(cpf?.hasError('pattern')).toBeTrue();
      
      cpf?.setValue('123.456.789-00');
      expect(cpf?.hasError('pattern')).toBeFalse();
    });

    it('should validate email format', () => {
      const email = component.socioForm.get('email');
      email?.setValue('invalid-email');
      expect(email?.hasError('email')).toBeTrue();
      
      email?.setValue('valid@example.com');
      expect(email?.hasError('email')).toBeFalse();
    });

    it('should validate telefone pattern', () => {
      const telefone = component.socioForm.get('telefone');
      telefone?.setValue('11987654321');
      expect(telefone?.hasError('pattern')).toBeTrue();
      
      telefone?.setValue('(11) 98765-4321');
      expect(telefone?.hasError('pattern')).toBeFalse();
    });

    it('should validate cep pattern', () => {
      const cep = component.socioForm.get('cep');
      cep?.setValue('12345678');
      expect(cep?.hasError('pattern')).toBeTrue();
      
      cep?.setValue('12345-678');
      expect(cep?.hasError('pattern')).toBeFalse();
    });
  });

  describe('Input Masks', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should apply CPF mask', () => {
      const event = { target: { value: '12345678900' } } as any;
      component.applyCpfMask(event);
      expect(component.socioForm.get('cpf')?.value).toBe('123.456.789-00');
    });

    it('should apply telefone mask for 10 digits', () => {
      const event = { target: { value: '1198765432' } } as any;
      component.applyTelefoneMask(event);
      expect(component.socioForm.get('telefone')?.value).toBe('(11) 9876-5432');
    });

    it('should apply telefone mask for 11 digits', () => {
      const event = { target: { value: '11987654321' } } as any;
      component.applyTelefoneMask(event);
      expect(component.socioForm.get('telefone')?.value).toBe('(11) 98765-4321');
    });

    it('should apply CEP mask', () => {
      const event = { target: { value: '12345678' } } as any;
      component.applyCepMask(event);
      expect(component.socioForm.get('cep')?.value).toBe('12345-678');
    });
  });

  describe('Photo Upload', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should handle valid photo selection', () => {
      const file = new File([''], 'photo.jpg', { type: 'image/jpeg' });
      const event = { target: { files: [file] } } as any;
      
      component.onPhotoSelected(event);
      
      expect(component.selectedPhoto).toBe(file);
    });

    it('should reject non-image files', () => {
      const file = new File([''], 'document.pdf', { type: 'application/pdf' });
      const event = { target: { files: [file] } } as any;
      
      component.onPhotoSelected(event);
      
      expect(component.selectedPhoto).toBeUndefined();
    });

    it('should reject files larger than 5MB', () => {
      const file = new File(['x'.repeat(6 * 1024 * 1024)], 'large.jpg', { type: 'image/jpeg' });
      const event = { target: { files: [file] } } as any;
      
      component.onPhotoSelected(event);
      
      expect(component.selectedPhoto).toBeUndefined();
    });

    it('should remove photo', () => {
      component.selectedPhoto = new File([''], 'photo.jpg', { type: 'image/jpeg' });
      component.photoPreview = 'data:image/jpeg;base64,test';
      
      component.removePhoto();
      
      expect(component.selectedPhoto).toBeUndefined();
      expect(component.photoPreview).toBeUndefined();
    });
  });

  describe('Form Submission', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should not submit invalid form', () => {
      component.socioForm.patchValue({
        nome: '',
        cpf: '',
        matricula: ''
      });

      component.onSubmit();

      expect(socioService.criarSocio).not.toHaveBeenCalled();
      expect(component.socioForm.invalid).toBeTrue();
    });

    it('should create new socio successfully', fakeAsync(() => {
      const socioRequest: SocioRequest = {
        nome: 'João Silva',
        cpf: '123.456.789-00',
        matricula: 'MAT001'
      };

      component.socioForm.patchValue(socioRequest);
      socioService.criarSocio.and.returnValue(of(mockSocio));

      component.onSubmit();
      tick();

      expect(socioService.criarSocio).toHaveBeenCalledWith(jasmine.objectContaining(socioRequest));
      expect(router.navigate).toHaveBeenCalledWith(['/socios']);
      
      flush();
    }));

    it('should update existing socio successfully', fakeAsync(() => {
      component.isEditMode = true;
      component.socioId = 1;

      const socioRequest: SocioRequest = {
        nome: 'João Silva Updated',
        cpf: '123.456.789-00',
        matricula: 'MAT001'
      };

      component.socioForm.patchValue(socioRequest);
      socioService.atualizarSocio.and.returnValue(of(mockSocio));

      component.onSubmit();
      tick();

      expect(socioService.atualizarSocio).toHaveBeenCalledWith(1, jasmine.objectContaining(socioRequest));
      expect(router.navigate).toHaveBeenCalledWith(['/socios']);
      
      flush();
    }));

    it('should handle submission error', fakeAsync(() => {
      component.socioForm.patchValue({
        nome: 'João Silva',
        cpf: '123.456.789-00',
        matricula: 'MAT001'
      });

      const error = { error: { message: 'CPF já cadastrado' } };
      socioService.criarSocio.and.returnValue(throwError(() => error));

      component.onSubmit();
      tick();

      expect(router.navigate).not.toHaveBeenCalled();
      expect(component.loading).toBeFalse();
      
      flush();
    }));
  });

  describe('Navigation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should navigate back to list on cancel', () => {
      component.onCancel();
      expect(router.navigate).toHaveBeenCalledWith(['/socios']);
    });
  });

  describe('Error Messages', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should return correct error message for required field', () => {
      const nome = component.socioForm.get('nome');
      nome?.setValue('');
      nome?.markAsTouched();
      
      expect(component.getErrorMessage('nome')).toBe('Este campo é obrigatório');
    });

    it('should return correct error message for minlength', () => {
      const nome = component.socioForm.get('nome');
      nome?.setValue('Jo');
      nome?.markAsTouched();
      
      expect(component.getErrorMessage('nome')).toContain('Mínimo de');
    });

    it('should return correct error message for invalid CPF', () => {
      const cpf = component.socioForm.get('cpf');
      cpf?.setValue('12345678900');
      cpf?.markAsTouched();
      
      expect(component.getErrorMessage('cpf')).toBe('CPF inválido (formato: 000.000.000-00)');
    });

    it('should return correct error message for invalid email', () => {
      const email = component.socioForm.get('email');
      email?.setValue('invalid');
      email?.markAsTouched();
      
      expect(component.getErrorMessage('email')).toBe('E-mail inválido');
    });

    it('should return empty string for untouched field', () => {
      const nome = component.socioForm.get('nome');
      nome?.setValue('');
      
      expect(component.getErrorMessage('nome')).toBe('');
    });
  });

  describe('Success Confirmation and Navigation (Task 6.6)', () => {
    it('should show success toast "Dados atualizados com sucesso" after updating socio', fakeAsync(() => {
      // Requirement 2.11
      activatedRoute.snapshot.paramMap.get.and.returnValue('1');
      socioService.buscarSocio.and.returnValue(of(mockSocio));
      socioService.atualizarSocio.and.returnValue(of(mockSocio));

      fixture.detectChanges();
      tick();

      component.socioForm.patchValue({
        nome: 'João Silva Updated',
        cpf: '123.456.789-00',
        matricula: 'MAT001'
      });

      component.onSubmit();
      tick();

      expect(snackBar.open).toHaveBeenCalledWith('Dados atualizados com sucesso', 'Fechar', { duration: 3000 });
    }));

    it('should navigate to socios list after successful save', fakeAsync(() => {
      // Requirement 2.12
      socioService.criarSocio.and.returnValue(of(mockSocio));

      fixture.detectChanges();

      component.socioForm.patchValue({
        nome: 'João Silva',
        cpf: '123.456.789-00',
        matricula: 'MAT001'
      });

      component.onSubmit();
      tick();

      expect(router.navigate).toHaveBeenCalledWith(['/socios']);
    }));

    it('should navigate immediately when canceling without changes', () => {
      // Requirement 2.13 - no changes
      fixture.detectChanges();

      component.onCancel();

      expect(router.navigate).toHaveBeenCalledWith(['/socios']);
    });

    it('should detect unsaved changes', () => {
      // Requirement 2.13
      fixture.detectChanges();

      // Initially no changes
      expect(component['hasUnsavedChanges']()).toBeFalse();

      // Make a change
      component.socioForm.patchValue({ nome: 'Changed Name' });

      expect(component['hasUnsavedChanges']()).toBeTrue();
    });
  });
});
