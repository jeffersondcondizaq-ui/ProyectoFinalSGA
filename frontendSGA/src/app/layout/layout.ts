import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { SesionService } from '../core/auth/sesion.service';
import { MENU_POR_ROL, MENU_PUBLICO } from './menu';

@Component({
  selector: 'app-layout',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './layout.html',
  styleUrl: './layout.scss',
})
export class Layout {
  private readonly router = inject(Router);
  protected readonly sesion = inject(SesionService);

  protected readonly opened = signal(true);
  protected readonly menu = computed(() => {
    const rol = this.sesion.rol();
    return rol ? MENU_POR_ROL[rol] : MENU_PUBLICO;
  });

  protected cerrarSesion(): void {
    this.sesion.cerrar();
    this.router.navigate(['/inicio']);
  }
}
