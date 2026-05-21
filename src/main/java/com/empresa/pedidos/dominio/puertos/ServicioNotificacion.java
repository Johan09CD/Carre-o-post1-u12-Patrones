package com.empresa.pedidos.dominio.puertos;

import com.empresa.pedidos.dominio.PedidoProcesadoEvent;

public interface ServicioNotificacion {
    void notificar(PedidoProcesadoEvent evento);
}
