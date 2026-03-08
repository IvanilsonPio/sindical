import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-arquivos-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <h1>Gestão de Arquivos</h1>
      <p>Funcionalidade em desenvolvimento...</p>
    </div>
  `,
  styles: [`
    .page-container {
      padding: 2rem;
    }
  `]
})
export class ArquivosListComponent {}
