package com.empresa.pedidos;

import com.empresa.pedidos.dominio.Pedido;
import com.empresa.pedidos.dominio.PedidoProcesadoEvent;
import com.empresa.pedidos.infraestructura.notificaciones.NotificacionEmail;
import com.empresa.pedidos.infraestructura.notificaciones.NotificacionLog;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
class PedidoProcesadoEventTests {

    @Autowired
    private ApplicationEventPublisher publisher;

    @SpyBean
    private NotificacionEmail notificacionEmail;

    @SpyBean
    private NotificacionLog notificacionLog;

    @Test
    void eventoSePublicaYEsEscuchado() {
        Pedido pedido = new Pedido();
        publisher.publishEvent(new PedidoProcesadoEvent(pedido));

        verify(notificacionEmail, timeout(1000)).notificar(ArgumentMatchers.any(PedidoProcesadoEvent.class));
        verify(notificacionLog, timeout(1000)).notificar(ArgumentMatchers.any(PedidoProcesadoEvent.class));
    }
}
