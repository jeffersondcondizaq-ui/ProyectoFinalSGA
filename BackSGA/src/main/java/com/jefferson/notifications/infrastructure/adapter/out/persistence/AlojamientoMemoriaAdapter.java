package com.jefferson.notifications.infrastructure.adapter.out.persistence;
import com.jefferson.notifications.application.port.out.AlojamientoRepositoryPort;
import com.jefferson.notifications.domain.model.Alojamiento;
import org.springframework.stereotype.Component;

@Component
public class AlojamientoMemoriaAdapter implements AlojamientoRepositoryPort {
    private Alojamiento alojamiento = DatosSemilla.crear();
    @Override
    public Alojamiento obtener(){
        return alojamiento;
    }
    @Override
    public void guardar(Alojamiento alojamiento){
        this.alojamiento = alojamiento;
    }
}
