import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { SesionService } from '../../../core/auth/sesion.service';
import { Rol } from '../../../shared/models';
import { EnConstruccion } from '../../../shared/components/en-construccion/en-construccion';

const INICIO_POR_ROL: Record<Rol, string> = {
  HUESPED: '/huesped',
  RECEPCIONISTA: '/recepcion',
  ADMINISTRADOR: '/admin',
};

@Component({
  selector: 'app-login',
  imports: [EnConstruccion, MatButtonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  /**
   * TEMPORAL: entra con un rol sin pasar por el backend, para navegar el cascarón.
   * Se reemplaza por AutenticacionApi.login() en la tarjeta de login.
   */
  protected entrarComo(rol: Rol): void {
    this.sesion.iniciar({ token: 'demo', usuario: rol.toLowerCase(), nombre: 'Demo', rol });
    this.router.navigateByUrl(INICIO_POR_ROL[rol]);
  }
}
