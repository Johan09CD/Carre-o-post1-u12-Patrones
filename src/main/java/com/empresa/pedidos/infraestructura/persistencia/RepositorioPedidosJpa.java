package com.empresa.pedidos.infraestructura.persistencia;

import com.empresa.pedidos.dominio.Pedido;
import com.empresa.pedidos.dominio.PedidoId;
import com.empresa.pedidos.dominio.puertos.RepositorioPedidos;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RepositorioPedidosJpa implements RepositorioPedidos {
    private final PedidoJpaRepository repo;

    public RepositorioPedidosJpa(PedidoJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Pedido guardar(Pedido pedido) {
        return repo.save(pedido);
    }

    @Override
    public Optional<Pedido> buscarPorId(PedidoId id) {
        return repo.findById(id.valor());
    }
}
