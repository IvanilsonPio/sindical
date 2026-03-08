import { Routes } from '@angular/router';
import { PagamentosListComponent } from './pagamentos-list/pagamentos-list.component';
import { PagamentoFormComponent } from './pagamento-form/pagamento-form.component';

export const PAGAMENTOS_ROUTES: Routes = [
  {
    path: '',
    component: PagamentosListComponent
  },
  {
    path: 'novo',
    component: PagamentoFormComponent
  }
];
