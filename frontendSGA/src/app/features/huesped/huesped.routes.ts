import { Routes } from '@angular/router';

export const HUESPED_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'disponibilidad' },
  { path: 'perfil', loadComponent: () => import('./perfil/perfil').then((m) => m.Perfil) },
  {
    path: 'disponibilidad',
    loadComponent: () => import('./disponibilidad/disponibilidad').then((m) => m.Disponibilidad),
  },
  {
    path: 'nueva-reserva/:codigoApartamento',
    loadComponent: () => import('./nueva-reserva/nueva-reserva').then((m) => m.NuevaReserva),
  },
  {
    path: 'reservas',
    loadComponent: () => import('./mis-reservas/mis-reservas').then((m) => m.MisReservas),
  },
  {
    path: 'reservas/:codigo',
    loadComponent: () =>
      import('./detalle-reserva/detalle-reserva').then((m) => m.DetalleReserva),
  },
];
