# CarreñoJ-Post1-U12

**Patrones de Diseño de Software — Unidad 12: Integración de Patrones y Arquitecturas**
Universidad de Santander (UDES) · Ingeniería de Sistemas · 2026

---

## Objetivo

Implementar un sistema de gestión de pedidos en Spring Boot integrando cuatro patrones de diseño (**Factory**, **Strategy**, **Observer** y **Facade**), verificar el desacoplamiento entre capas con ArchUnit, y comparar las métricas de calidad antes y después de la integración usando SonarQube.

---

## Arquitectura del sistema

El sistema sigue una arquitectura **Hexagonal (Ports & Adapters)** con organización de paquetes *feature-first*. El dominio no conoce ningún detalle de infraestructura; la capa de aplicación orquesta mediante puertos (interfaces); los adaptadores implementan esos puertos.

```
src/main/java/com/empresa/pedidos/
├── PedidosApplication.java
├── dominio/
│   ├── Pedido.java
│   ├── PedidoId.java
│   ├── TipoPedido.java
│   ├── EstadoPedido.java
│   ├── PedidoProcesadoEvent.java
│   └── puertos/
│       ├── RepositorioPedidos.java
│       ├── ProcesadorPedido.java
│       └── ServicioNotificacion.java
├── aplicacion/
│   └── ServicioPedidos.java
├── infraestructura/
│   ├── persistencia/
│   │   ├── PedidoJpaRepository.java
│   │   └── RepositorioPedidosJpa.java
│   └── notificaciones/
│       ├── NotificacionEmail.java
│       └── NotificacionLog.java
└── adaptadores/
    ├── procesadores/
    │   ├── ProcesadorPedidoEstandar.java
    │   ├── ProcesadorPedidoExpress.java
    │   ├── ProcesadorPedidoInternacional.java
    │   └── ProcesadorPedidoFactory.java
    ├── facade/
    │   └── FachadaPedidos.java
    └── rest/
        └── PedidoController.java
```

---

## Patrones implementados

### 1. Strategy — `ProcesadorPedido`

**Problema que resuelve:** el servicio legacy tenía un bloque `if-else` con la lógica de cálculo de costo para cada tipo de pedido, generando CC = 4 y acoplamiento fuerte.

Se definió el puerto `ProcesadorPedido` con tres implementaciones, una por tipo:

```java
public interface ProcesadorPedido {
    TipoPedido getTipo();
    void procesar(Pedido pedido);
}

@Component
public class ProcesadorPedidoEstandar implements ProcesadorPedido {
    @Override public TipoPedido getTipo() { return TipoPedido.ESTANDAR; }
    @Override
    public void procesar(Pedido pedido) {
        pedido.setCosto(pedido.getSubtotal() * 1.1);
        pedido.setEstado(EstadoPedido.PROCESADO);
    }
}
// Express: subtotal * 1.3 | Internacional: subtotal * 1.5 + 25.0
```

**Beneficio:** cada algoritmo de cálculo está encapsulado en su propia clase. Agregar un nuevo tipo de pedido no modifica ninguna clase existente (Open/Closed).

---

### 2. Factory — `ProcesadorPedidoFactory`

**Problema que resuelve:** alguien tiene que seleccionar la Strategy correcta según el tipo de pedido sin exponer esa decisión al servicio de aplicación.

Spring inyecta automáticamente todas las implementaciones de `ProcesadorPedido` en una lista; la Factory las indexa por tipo:

```java
@Component
public class ProcesadorPedidoFactory {
    private final Map<TipoPedido, ProcesadorPedido> procesadores;

    public ProcesadorPedidoFactory(List<ProcesadorPedido> lista) {
        this.procesadores = lista.stream().collect(
            Collectors.toMap(ProcesadorPedido::getTipo, Function.identity())
        );
    }

    public ProcesadorPedido obtener(TipoPedido tipo) {
        return Optional.ofNullable(procesadores.get(tipo))
            .orElseThrow(() -> new IllegalArgumentException(
                "Tipo de pedido no soportado: " + tipo));
    }
}
```

**Beneficio:** `FachadaPedidos` no conoce los tipos concretos; delega la selección completamente a la Factory.

---

### 3. Observer — `PedidoProcesadoEvent` con Spring Events

**Problema que resuelve:** el servicio legacy invocaba `JavaMailSender` directamente, creando acoplamiento entre la lógica de negocio y el canal de notificación.

Se definió un evento de dominio y listeners independientes anotados con `@EventListener`:

```java
public record PedidoProcesadoEvent(Pedido pedido) {}

@Component
public class NotificacionEmail implements ServicioNotificacion {
    @EventListener
    @Override
    public void notificar(PedidoProcesadoEvent evento) {
        System.out.println("Email enviado para pedido: "
            + evento.pedido().getId());
    }
}

@Component
public class NotificacionLog implements ServicioNotificacion {
    @EventListener
    @Override
    public void notificar(PedidoProcesadoEvent evento) {
        log.info("Pedido procesado: {} - Costo: {}",
            evento.pedido().getId(), evento.pedido().getCosto());
    }
}
```

**Beneficio:** `FachadaPedidos` solo publica el evento; no sabe cuántos ni qué listeners existen. Agregar un nuevo canal (SMS, push) es crear un nuevo `@Component` sin tocar nada más.

---

### 4. Facade — `FachadaPedidos`

**Problema que resuelve:** el controlador REST no debería conocer la Factory, el repositorio ni el publisher; esa complejidad debe estar oculta detrás de una interfaz simple.

```java
@Service
public class FachadaPedidos {
    private final ProcesadorPedidoFactory factory;
    private final RepositorioPedidos repositorio;
    private final ApplicationEventPublisher publisher;

    public FachadaPedidos(ProcesadorPedidoFactory factory,
                          RepositorioPedidos repositorio,
                          ApplicationEventPublisher publisher) {
        this.factory     = factory;
        this.repositorio = repositorio;
        this.publisher   = publisher;
    }

    public Pedido crearPedido(Pedido pedido) {
        factory.obtener(pedido.getTipo()).procesar(pedido);
        var guardado = repositorio.guardar(pedido);
        publisher.publishEvent(new PedidoProcesadoEvent(guardado));
        return guardado;
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return repositorio.buscarPorId(new PedidoId(id));
    }
}
```

El controlador REST solo depende de `FachadaPedidos`:

```java
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final FachadaPedidos fachada;

    @PostMapping
    public ResponseEntity<Pedido> crear(@RequestBody Pedido pedido) {
        return ResponseEntity.ok(fachada.crearPedido(pedido));
    }
}
```

**Beneficio:** CC de `FachadaPedidos` = 1. El controlador queda con cero lógica de negocio.

---

## Métricas SonarQube — Antes vs. Después

| Métrica | Antes (Legacy) | Después (Refactorizado) | Mejora |
|---------|---------------|------------------------|--------|
| CC de `procesarPedido()` | 4 | 1 | ↓ 75 % |
| Cognitive Complexity | 6 | 0 | ↓ 100 % |
| Acoplamiento a `JavaMailSender` | Directo | Eliminado | ✅ |
| Acoplamiento a JPA desde aplicación | Directo | Por puerto | ✅ |
| Code Smells totales | _ver captura_ | _ver captura_ | ↓ notable |
| Cobertura de pruebas | — | > 80 % | ✅ |
| Quality Gate | — | **Passed** | ✅ |

> 📸 **Capturas del dashboard de SonarQube:**

**Antes de la refactorización:**

![SonarQube Antes](img/captura1.png)

**Después de la refactorización:**

![SonarQube Después](img/captura2.png)

**Quality Gate Passed:**

![Quality Gate](img/captura3.png)

---

## Prerrequisitos para ejecutar el proyecto

```bash
# Levantar SonarQube
docker run -d -p 9000:9000 sonarqube:lts-community

# Compilar, testear y analizar
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=pedidos-integrado \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=TU_TOKEN
```

- Java 17+
- Maven 3.9+
- Docker Desktop