import { CargaService } from './carga.service';

describe('CargaService', () => {
  it('está cargando mientras haya peticiones sin terminar', () => {
    const carga = new CargaService();
    expect(carga.cargando()).toBe(false);
    carga.iniciar();
    carga.iniciar();
    carga.terminar();
    expect(carga.cargando()).toBe(true);
    carga.terminar();
    expect(carga.cargando()).toBe(false);
  });

  it('nunca baja de cero', () => {
    const carga = new CargaService();
    carga.terminar();
    carga.iniciar();
    expect(carga.cargando()).toBe(true);
  });
});
