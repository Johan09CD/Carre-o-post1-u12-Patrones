package com.empresa.pedidos.adaptadores.facade;

import com.empresa.pedidos.aplicacion.ServicioPedidos;
import com.empresa.pedidos.dominio.Pedido;
import com.empresa.pedidos.dominio.PedidoId;
import com.empresa.pedidos.dominio.PedidoProcesadoEvent;
import com.empresa.pedidos.dominio.puertos.RepositorioPedidos;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FachadaPedidos {
    private final ServicioPedidos servicioPedidos;
    private final RepositorioPedidos repositorio;
    private final ApplicationEventPublisher publisher;

    public FachadaPedidos(ServicioPedidos servicioPedidos,
                          RepositorioPedidos repositorio,
                          ApplicationEventPublisher publisher) {
        this.servicioPedidos = servicioPedidos;
        this.repositorio = repositorio;
        this.publisher = publisher;
    }

    public Pedido crearPedido(Pedido pedido) {
        Pedido guardado = servicioPedidos.procesar(pedido);
        publisher.publishEvent(new PedidoProcesadoEvent(guardado));
        return guardado;
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return repositorio.buscarPorId(new PedidoId(id));
    }
}
