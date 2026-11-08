import { Routes } from '@angular/router';
import { rolGuard, sinSesionGuard } from './core/auth/auth.guard';
import { Layout } from './layout/layout';

export const routes: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      {
        path: 'inicio',
        loadComponent: () => import('./features/publico/inicio/inicio').then((m) => m.Inicio),
      },
      {
        path: 'login',
        canMatch: [sinSesionGuard],
        loadComponent: () => import('./features/publico/login/login').then((m) => m.Login),
      },
      {
        path: 'registro',
        canMatch: [sinSesionGuard],
        loadComponent: () => import('./features/publico/registro/registro').then((m) => m.Registro),
      },
      {
        path: 'huesped',
        canMatch: [rolGuard('HUESPED')],
        loadChildren: () =>
          import('./features/huesped/huesped.routes').then((m) => m.HUESPED_ROUTES),
      },
      {
        path: 'recepcion',
        canMatch: [rolGuard('RECEPCIONISTA')],
        loadChildren: () =>
          import('./features/recepcion/recepcion.routes').then((m) => m.RECEPCION_ROUTES),
      },
      {
        path: 'admin',
        canMatch: [rolGuard('ADMINISTRADOR')],
        loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
