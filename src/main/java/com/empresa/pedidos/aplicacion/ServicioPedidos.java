package com.empresa.pedidos.aplicacion;

import com.empresa.pedidos.adaptadores.procesadores.ProcesadorPedidoFactory;
import com.empresa.pedidos.dominio.Pedido;
import com.empresa.pedidos.dominio.puertos.RepositorioPedidos;
import org.springframework.stereotype.Service;

@Service
public class ServicioPedidos {
    private final ProcesadorPedidoFactory factory;
    private final RepositorioPedidos repositorio;

    public ServicioPedidos(ProcesadorPedidoFactory factory, RepositorioPedidos repositorio) {
        this.factory = factory;
        this.repositorio = repositorio;
    }

    public Pedido procesar(Pedido pedido) {
        factory.obtener(pedido.getTipo()).procesar(pedido);
        return repositorio.guardar(pedido);
    }
}
