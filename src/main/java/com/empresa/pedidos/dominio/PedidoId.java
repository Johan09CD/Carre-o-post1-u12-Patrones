package com.empresa.pedidos.dominio;

public class PedidoId {
    private final Long valor;

    public PedidoId(Long valor) {
        this.valor = valor;
    }

    public Long valor() {
        return valor;
    }
}
