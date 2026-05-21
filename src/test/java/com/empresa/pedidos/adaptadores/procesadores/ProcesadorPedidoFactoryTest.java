package com.empresa.pedidos.adaptadores.procesadores;

import com.empresa.pedidos.dominio.TipoPedido;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ProcesadorPedidoFactoryTest {

    @Test
    void debeRetornarProcesadorCorrectoPorTipo() {
        ProcesadorPedidoFactory factory = new ProcesadorPedidoFactory(List.of(
                new ProcesadorPedidoEstandar(),
                new ProcesadorPedidoExpress(),
                new ProcesadorPedidoInternacional()
        ));

        assertInstanceOf(ProcesadorPedidoEstandar.class, factory.obtener(TipoPedido.ESTANDAR));
        assertInstanceOf(ProcesadorPedidoExpress.class, factory.obtener(TipoPedido.EXPRESS));
        assertInstanceOf(ProcesadorPedidoInternacional.class, factory.obtener(TipoPedido.INTERNACIONAL));
    }
}
