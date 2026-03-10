import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ConfirmDialogComponent, ConfirmDialogData } from './confirm-dialog.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('ConfirmDialogComponent', () => {
  let component: ConfirmDialogComponent;
  let fixture: ComponentFixture<ConfirmDialogComponent>;
  let dialogRef: jasmine.SpyObj<MatDialogRef<ConfirmDialogComponent>>;
  const mockData: ConfirmDialogData = {
    title: 'Test Title',
    message: 'Test Message',
    confirmText: 'Confirm',
    cancelText: 'Cancel'
  };

  beforeEach(async () => {
    const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [
        ConfirmDialogComponent,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: mockData }
      ]
    }).compileComponents();

    dialogRef = TestBed.inject(MatDialogRef) as jasmine.SpyObj<MatDialogRef<ConfirmDialogComponent>>;
    fixture = TestBed.createComponent(ConfirmDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display title and message', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('h2').textContent).toContain('Test Title');
    expect(compiled.querySelector('mat-dialog-content p').textContent).toBe('Test Message');
  });

  it('should close dialog with true when confirm is clicked', () => {
    component.onConfirm();
    expect(dialogRef.close).toHaveBeenCalledWith(true);
  });

  it('should close dialog with false when cancel is clicked', () => {
    component.onCancel();
    expect(dialogRef.close).toHaveBeenCalledWith(false);
  });

  it('should use default button texts when not provided', () => {
    const defaultData: ConfirmDialogData = {
      title: 'Test',
      message: 'Test'
    };
    
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [ConfirmDialogComponent, BrowserAnimationsModule],
      providers: [
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: defaultData }
      ]
    });

    const newFixture = TestBed.createComponent(ConfirmDialogComponent);
    newFixture.detectChanges();
    
    const compiled = newFixture.nativeElement;
    const buttons = compiled.querySelectorAll('button');
    expect(buttons[0].textContent.trim()).toBe('Cancelar');
    expect(buttons[1].textContent.trim()).toBe('Confirmar');
  });

  // Keyboard Navigation Tests
  describe('Keyboard Navigation', () => {
    it('should close dialog with true when Enter key is pressed', () => {
      const event = new KeyboardEvent('keydown', { key: 'Enter' });
      component.handleKeyboardEvent(event);
      expect(dialogRef.close).toHaveBeenCalledWith(true);
    });

    it('should close dialog with false when Escape key is pressed', () => {
      const event = new KeyboardEvent('keydown', { key: 'Escape' });
      component.handleKeyboardEvent(event);
      expect(dialogRef.close).toHaveBeenCalledWith(false);
    });

    it('should not close dialog when other keys are pressed', () => {
      const event = new KeyboardEvent('keydown', { key: 'a' });
      component.handleKeyboardEvent(event);
      expect(dialogRef.close).not.toHaveBeenCalled();
    });

    it('should have visible focus indicators on buttons', () => {
      const compiled = fixture.nativeElement;
      const buttons = compiled.querySelectorAll('button');
      
      // Check that buttons are focusable
      expect(buttons[0].tabIndex).toBeGreaterThanOrEqual(0);
      expect(buttons[1].tabIndex).toBeGreaterThanOrEqual(0);
    });

    it('should have proper ARIA labels for accessibility', () => {
      const compiled = fixture.nativeElement;
      const buttons = compiled.querySelectorAll('button');
      
      expect(buttons[0].getAttribute('aria-label')).toBe('Cancelar ação');
      expect(buttons[1].getAttribute('aria-label')).toBe('Confirmar ação');
    });

    it('should have role="alert" on message for screen readers', () => {
      const compiled = fixture.nativeElement;
      const message = compiled.querySelector('mat-dialog-content p');
      
      expect(message.getAttribute('role')).toBe('alert');
    });
  });
});
