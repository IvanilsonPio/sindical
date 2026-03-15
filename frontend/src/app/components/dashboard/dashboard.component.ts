import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatGridListModule } from '@angular/material/grid-list';
import { AuthService } from '../../services/auth.service';

interface NavigationCard {
  title: string;
  description: string;
  icon: string;
  route: string;
  color: string;
  adminOnly?: boolean;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatGridListModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {
  isAdmin: boolean;

  allCards: NavigationCard[] = [
    {
      title: 'Sócios',
      description: 'Gerenciar cadastro de sócios do sindicato',
      icon: 'people',
      route: '/socios',
      color: '#1976d2'
    },
    {
      title: 'Pagamentos',
      description: 'Registrar e consultar pagamentos mensais',
      icon: 'payment',
      route: '/pagamentos',
      color: '#388e3c'
    },
    {
      title: 'Arquivos',
      description: 'Gerenciar documentos dos sócios',
      icon: 'folder',
      route: '/arquivos',
      color: '#f57c00'
    },
    {
      title: 'Usuários',
      description: 'Gerenciar usuários e permissões de acesso',
      icon: 'manage_accounts',
      route: '/usuarios',
      color: '#7b1fa2',
      adminOnly: true
    }
  ];

  get navigationCards(): NavigationCard[] {
    return this.allCards.filter(c => !c.adminOnly || this.isAdmin);
  }

  constructor(private router: Router, private authService: AuthService) {
    this.isAdmin = this.authService.isAdmin();
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
}
