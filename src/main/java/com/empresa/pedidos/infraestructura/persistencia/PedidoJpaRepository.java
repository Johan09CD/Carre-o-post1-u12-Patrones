package com.empresa.pedidos.infraestructura.persistencia;

import com.empresa.pedidos.dominio.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoJpaRepository extends JpaRepository<Pedido, Long> {
}
