# Shopping Session — Feature Input & Decision Analysis

> **Propósito**: Este documento es el input para la spec `002-shopping-session`.  
> Contiene el diseño original, análisis de alternativas y puntos de decisión que deben
> resolverse antes de redactar la especificación técnica definitiva.

---

## Diseño del sistema

El sistema de reserva de plazas de aparcamiento deberá, a partir de una ubicación y
fechas de entrada/salida, ofrecer un listado de parkings disponibles con una tarifa
cerrada.

El usuario podrá seleccionar una de estas opciones para comenzar con el proceso de
selección.

---

## Flujo original (propuesto)

1. El usuario realiza una búsqueda con:
   - Ubicación (ciudad, aeropuerto, parking, etc.)
   - DateTime de entrada / salida
   - Lista de características deseadas (tags)

2. El **Discovery API** resuelve los candidatos por localización *(sin comprobar plazas
   disponibles)* y emite un evento `SearchRequested`.

3. El **reservation-service** recibe el evento con los candidatos y crea *holds*
   (bloqueos temporales) sobre una plaza de cada candidato.

4. El **pricing-service** calcula la tarifa para cada hold (días, disponibilidad,
   tipo de plaza, etc.).

5. Los resultados con precio cerrado se devuelven al cliente vía SSE.

6. El cliente elige un parking; los holds de los otros candidatos se liberan.

---

## Análisis de alternativas y puntos de decisión

### D-001 · ¿Nuevo servicio "pricing-service" o responsabilidad distribuida?

**Contexto**: La arquitectura actual tiene `catalog-service`, `reservation-service` y
`payment-service`. El flujo propuesto introduce un `pricing-service` que no existe.

| Opción | Pros | Contras |
|--------|------|---------|
| **A — Nuevo microservicio `pricing-service`** | Separación limpia de responsabilidad de precio; facilita estrategias dinámicas (yield management) | Añade un servicio nuevo a operar; latencia extra en el flujo de búsqueda; complejidad de deploy |
| **B — Pricing en `catalog-service`** | Sin servicio nuevo; el catálogo ya conoce tarifas base y disponibilidad | Mezcla responsabilidades; dificulta futura lógica dinámica de precio |
| **C — Pricing en `reservation-service`** | Precio calculado en el mismo contexto donde se crea el hold | Acoplamiento fuerte entre reserva y lógica de tarifa |

**✅ DECISIÓN**: Opción A — crear nuevo microservicio `pricing-service`, integrado
mediante **Kafka** (comunicación asíncrona). El `reservation-service` publicará un evento
de solicitud de precio (`PriceQuoteRequestedEvent`) y el `pricing-service` responderá con
un `PriceQuoteResolvedEvent`. Esto implica:
- Añadir `pricing-service` al monorepo Gradle como nuevo subproyecto.
- Añadir tópicos Kafka: `parking.pricing.requests` y `parking.pricing.results`.
- El flow de búsqueda es necesariamente asíncrono (refuerza SSE o polling en D-004).

---

### D-002 · Mecanismo de hold — ¿cuándo y cuántos bloqueos crear?

**Contexto**: El flujo propuesto crea holds en *todos* los candidatos cuando se
devuelven los resultados de búsqueda. Esto implica:

- Un usuario puede bloquear decenas de plazas simultáneamente sin intención de reservar.
- Con tráfico alto, la disponibilidad aparente colapsa (plazas bloqueadas que nunca se
  confirman).
- Las plazas bloqueadas durante la búsqueda requieren TTL agresivo o liberación explícita.

| Opción | Descripción | Trade-off |
|--------|-------------|-----------|
| **A — Hold en búsqueda (propuesto)** | Hold en un spot por cada candidato al mostrar resultados | Precio cerrado desde el primer momento; colapso de disponibilidad bajo carga |
| **B — Hold solo al seleccionar** | El hold se crea cuando el usuario hace clic en "Reservar éste" | Sin bloqueos especulativos; precio puede cambiar entre búsqueda y selección (race condition) |
| **C — Hold con página de confirmación** | Hold al entrar al paso de confirmación (checkout); TTL corto (5 min) | Balance razonable: bloqueo breve, precio garantizado durante el checkout |

**✅ DECISIÓN**: Opción B — la búsqueda devuelve **precio estimado** (calculado por
`pricing-service` en background, sin crear holds). El hold se crea únicamente cuando
el usuario selecciona un parking concreto. Consecuencias:
- La búsqueda es ligera y no bloquea inventario.
- El precio mostrado en resultados es orientativo; el precio firme se obtiene al crear el hold.
- Se simplifica D-007 (solo hay un hold activo por sesión en lugar de N simultáneos).
- El flujo de búsqueda puede ser **síncrono** (resuelve D-004 → opción B/C).

---

### D-003 · Identificador de sesión de búsqueda (Shopping Session ID)

**Contexto**: Para poder liberar los holds de los candidatos no seleccionados, el
sistema necesita correlacionar todos los holds creados para una misma búsqueda.

**Opciones**:
- **A — `searchSessionId` generado por el backend**: El `catalog-service` o
  `reservation-service` genera un UUID al inicio de la búsqueda; todos los holds llevan
  ese identificador.
- **B — `searchSessionId` generado por el cliente**: El frontend genera el UUID y lo
  incluye en la request; más simple, pero requiere validación.
- **C — Sesión autenticada (sub JWT)**: Los holds se asocian al `userId` (no a una
  sesión de búsqueda). Problema: un usuario solo puede tener una búsqueda activa a la vez.

**✅ DECISIÓN**: Opción A — el backend genera el `searchSessionId` (UUID) al procesar
la primera búsqueda y lo devuelve en la respuesta. El frontend lo incluye en
peticiones posteriores (selección, hold, confirmación). Esto permite:
- Sesiones anónimas sin dependencia del JWT.
- Trazabilidad completa de la sesión de compra en los logs.
- Correlación del evento `SearchRequestedEvent` con los holds y el precio estimado.

---

### D-004 · Flujo síncrono vs. asíncrono (evento `SearchRequested`)

**Contexto**: El flujo propuesto emite un evento `SearchRequested` que el
`reservation-service` consume asincrónicamente. Esto implica que los resultados no están
disponibles de forma inmediata — de ahí el SSE para empujar actualizaciones al cliente.

| Opción | Descripción | Latencia percibida | Complejidad |
|--------|-------------|-------------------|-------------|
| **A — Async con evento + SSE (propuesto)** | `catalog-service` emite evento; `reservation-service` crea holds; resultados llegan por SSE | Alta (usuario espera "cargando") | Alta (SSE channel, correlación, timeout) |
| **B — Síncrono REST en dos pasos** | Paso 1: `GET /catalog/search` devuelve candidatos sin precio (< 100 ms, caché). Paso 2: `POST /shopping-sessions` crea la sesión con holds y devuelve precios | Baja (resultados inmediatos, precio en segundo request) | Media |
| **C — Síncrono con precio estimado** | `GET /catalog/search` devuelve candidatos con precio estimado calculado en `catalog-service` (sin hold). Hold solo al confirmar selección | Muy baja (una sola llamada) | Baja |

**✅ DECISIÓN**: Opción C — flujo **síncrono de una sola llamada**. El endpoint
`GET /catalog/search` devuelve los candidatos con precio estimado incluido. El precio
estimado lo calcula el `catalog-service` usando la tarifa base del parking y el nº de
días (sin invocar a `pricing-service` en tiempo de búsqueda). El `pricing-service` entra
en juego solo cuando el usuario selecciona un parking y se crea el hold. Consecuencias:
- No se emite `SearchRequested` como evento Kafka en el flujo de búsqueda.
- SSE queda reservado para el stream de disponibilidad en tiempo real (`GET /availability/stream`), no para resultados de búsqueda (simplifica D-006).
- El `searchSessionId` se genera y devuelve en la respuesta de `GET /catalog/search`.

---

### D-005 · Axon 5.x vs. Axon 4.10+ (versión a usar)

**Contexto**: El documento de instrucciones menciona "Axon 5.x" pero la arquitectura
existente (`plan.md`, `research.md`) usa **Axon 4.10+**. Axon 5 es una reescritura
con API incompatible.

| Versión | Estado (Jun 2026) | API | Compatibilidad Spring Boot 4 |
|---------|------------------|-----|------------------------------|
| **Axon 4.10+** | GA, estable | Conocida, usada en spec-001 | Sí (4.10+) |
| **Axon 5.x** | Release Candidate / Early GA | Reescritura; API diferente | A confirmar |

**Riesgo de Axon 5**: Si el proyecto ya tiene `catalog-service` implementado con Axon
4.10+, migrar a Axon 5 en la spec-002 introduce inconsistencia entre servicios y riesgo
de regresión.

**✅ DECISIÓN**: Se adopta **Axon 5.x** en toda la plataforma. Esto implica:
- Migrar `catalog-service` de Axon 4.10+ a Axon 5.x (breaking change — tarea a incluir
  en la spec-002 o en una tarea de migración previa).
- `reservation-service` y `pricing-service` se implementan directamente con Axon 5.x.
- Verificar compatibilidad de `axon-spring-boot-starter` 5.x con Spring Boot 4.0.6 antes
  de iniciar la implementación; bloquear la versión exacta en el BOM raíz (`build.gradle`).
- Revisar cambios de API en SAGA (`@SagaEventHandler`), `CommandGateway`, `DeadlineManager`
  y el modelo de persistencia de sagas respecto a Axon 4.x.

---

### D-006 · Canal de comunicación cliente-servidor — SSE vs. alternativas

**Contexto**: El documento propone SSE para devolver resultados de búsqueda al cliente.

| Canal | Casos de uso | Pros | Contras |
|-------|-------------|------|---------|
| **SSE** | Push unidireccional server→client | Simple, HTTP/1.1, auto-reconexión | Solo server→client; una conexión por tab |
| **WebSocket** | Bidireccional (chat, colaboración) | Full-duplex | Más complejo; no necesario si solo se empujan actualizaciones |
| **REST Polling** | Estado de reserva asíncrona | Stateless, simple | Latencia de polling; carga innecesaria |

**✅ DECISIÓN**: Opción A — **SSE**. Uso acotado al contexto donde aplica tras las
decisiones D-002/D-004:
- `GET /catalog/availability/stream` — stream de disponibilidad en tiempo real (ya
  definido en `catalog-api.md`). Notifica cambios de `availableSpots` a los clientes que
  tienen la página de resultados abierta.
- **No se usa SSE** para el flujo de búsqueda (síncrono por D-004) ni para el estado de
  la reserva (el cliente hará polling a `GET /reservations/{id}`).
- El `catalog-service` expone el endpoint SSE mediante Spring WebFlux `ServerSentEvent<T>`
  o `SseEmitter` (Spring MVC); decisión de implementación a fijar en la spec.

---

### D-007 · Liberación de holds al seleccionar — mecanismo

Si se decide crear holds (opciones A o B de D-002), hay que definir cómo se liberan los
no seleccionados:

| Mecanismo | Descripción |
|-----------|-------------|
| **TTL automático** | Los holds expiran por TTL (Redis) sin acción del cliente. Riesgo: ventana de indisponibilidad hasta el TTL |
| **Liberación explícita al seleccionar** | `POST /shopping-sessions/{id}/select` libera los demás holds. Requiere que el cliente llame al endpoint |
| **Liberación explícita al abandonar** | `DELETE /shopping-sessions/{id}` libera todos los holds. Requiere gestión del ciclo de vida de sesión desde el frontend |
| **Combinación TTL + liberación explícita** | TTL como fallback; liberación inmediata cuando el cliente actúa | Recomendado |

---

## Consideraciones de implementación (originales)

- Usaremos **Axon 5.x** en toda la plataforma; `catalog-service` será migrado de 4.10+ a 5.x.
- La comunicación con el cliente usará **SSE** únicamente para el stream de disponibilidad
  en tiempo real (`GET /catalog/availability/stream`). El flujo de búsqueda es síncrono REST.

---

## Preguntas abiertas para la sesión de refinamiento

| ID | Pregunta | Impacto |
|----|----------|---------|
| ~~Q1~~ | ~~¿La lógica de pricing en MVP es simple (días × tarifa) o requiere yield management?~~ | ✅ Resuelto → nuevo `pricing-service` vía Kafka (D-001) |
| ~~Q2~~ | ~~¿El usuario debe ver precio garantizado en los resultados de búsqueda, o es suficiente precio estimado?~~ | ✅ Resuelto → precio estimado en búsqueda, hold al seleccionar (D-002) |
| ~~Q3~~ | ~~¿La búsqueda con holds puede ser anónima, o requiere estar autenticado?~~ | ✅ Resuelto → `searchSessionId` generado por backend, soporta sesiones anónimas (D-003) |
| ~~Q4~~ | ~~¿Flujo síncrono o asíncrono para la búsqueda?~~ | ✅ Resuelto → síncrono, una llamada, precio estimado en `catalog-service` (D-004) |
| ~~Q4~~ | ~~¿Se mantiene Axon 4.10+ o se adopta Axon 5.x?~~ | ✅ Resuelto → Axon 5.x en toda la plataforma, migrar `catalog-service` (D-005) |
| ~~Q5~~ | ~~¿SSE, WebSocket o polling para comunicación con el cliente?~~ | ✅ Resuelto → SSE solo para stream de disponibilidad; búsqueda síncrona REST (D-006) |
| ~~Q6~~ | ~~¿Cuál es el TTL máximo aceptable para un hold?~~ | ✅ Resuelto → **10 minutos**, configurable vía `reservation.hold.ttl-minutes` |
| ~~Q7~~ | ~~¿Cuántos resultados máximos se muestran en una búsqueda?~~ | ✅ Resuelto → configurable vía `catalog.search.max-results` (default a definir en spec) |
| ~~Q8~~ | ~~¿El "Discovery API" es el `catalog-service` existente o un servicio nuevo?~~ | ✅ Resuelto → es el `catalog-service` existente; no se añade ningún módulo nuevo |

