import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';

export const routes: Routes = [
  { 
    path: 'login', 
    component: LoginComponent 
  },
  { 
    path: 'dashboard', 
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'socios', 
    loadChildren: () => import('./components/socios/socios.routes').then(m => m.SOCIOS_ROUTES),
    canActivate: [authGuard]
  },
  { 
    path: 'pagamentos', 
    loadChildren: () => import('./components/pagamentos/pagamentos.routes').then(m => m.PAGAMENTOS_ROUTES),
    canActivate: [authGuard]
  },
  { 
    path: 'arquivos', 
    loadChildren: () => import('./components/arquivos/arquivos.routes').then(m => m.ARQUIVOS_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'usuarios',
    loadComponent: () => import('./components/usuarios/usuarios.component').then(m => m.UsuariosComponent),
    canActivate: [authGuard, adminGuard]
  },
  { 
    path: '', 
    redirectTo: '/dashboard', 
    pathMatch: 'full' 
  },
  { 
    path: '**', 
    redirectTo: '/dashboard' 
  }
];
