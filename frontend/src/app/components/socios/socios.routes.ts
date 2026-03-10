import { Routes } from '@angular/router';
import { SociosListComponent } from './socios-list/socios-list.component';
import { SocioFormComponent } from './socio-form/socio-form.component';
import { SocioDetailComponent } from './socio-detail/socio-detail.component';

export const SOCIOS_ROUTES: Routes = [
  {
    path: '',
    component: SociosListComponent
  },
  {
    path: 'novo',
    component: SocioFormComponent
  },
  {
    path: ':id/detalhes',
    component: SocioDetailComponent
  },
  {
    path: ':id/editar',
    component: SocioFormComponent
  }
];
