import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'apartamentos' },
  {
    path: 'apartamentos',
    loadComponent: () => import('./apartamentos/apartamentos').then((m) => m.Apartamentos),
  },
  {
    path: 'temporadas',
    loadComponent: () => import('./temporadas/temporadas').then((m) => m.Temporadas),
  },
  { path: 'politica', loadComponent: () => import('./politica/politica').then((m) => m.Politica) },
  {
    path: 'empleados',
    loadComponent: () => import('./empleados/empleados').then((m) => m.Empleados),
  },
  { path: 'reportes', loadComponent: () => import('./reportes/reportes').then((m) => m.Reportes) },
];
