import { Routes } from '@angular/router';
import { SociosListComponent } from './socios-list/socios-list.component';
import { SocioFormComponent } from './socio-form/socio-form.component';

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
    path: 'editar/:id',
    component: SocioFormComponent
  }
];
