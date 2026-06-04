# Prueba Técnica: Sistema Global de Reservas de Aparcamiento de Alta Disponibilidad

## 1. Contexto del Problema

Nuestra compañía está expandiendo su presencia global. Necesitamos diseñar e implementar el núcleo de nuestra nueva plataforma de Reservas de Aparcamiento en Tiempo Real. El sistema debe ser capaz de gestionar millones de plazas de aparcamiento distribuidas en múltiples ciudades y aeropuertos, soportando picos de tráﬁco masivos (por ejemplo, la apertura de reservas para la temporada de vacaciones o eventos masivos).

El sistema debe lidiar con problemas complejos de concurrencia (evitar la sobreventa de una misma plaza), latencia mínima en la búsqueda, resiliencia ante caídas de proveedores de pago y consistencia de datos en entornos distribuidos.

## 2. Requisitos Técnicos y de Arquitectura

Para demostrar el nivel de Arquitecto, la solución propuesta debe abordar y resolver los siguientes pilares:

### A. Arquitectura y Patrones Enterprise

- Desacoplamiento y Escalabilidad: Diseño basado en microservicios o arquitectura modular integrable en entornos distribuidos (ej. Hexagonal Architecture o Clean Architecture en los servicios core).

- Gestión de Concurrencia: Estrategia clara para evitar la sobreventa de plazas de
  aparcamiento simultáneas (Optimistic/Pessimistic Locking, Distributed Locks con
  Redis, etc.).

- Consistencia Eventual: Uso de patrones como Saga Pattern o Outbox Pattern para la comunicación asíncrona entre el servicio de reservas y el servicio de pagos/notificaciones.

### B. Cloud Native e Infraestructura

- Resiliencia: Implementación de patrones de tolerancia a fallos (Circuit Breaker, Rate Limiting, Retry) para proteger el sistema de degradaciones en cascada.

- Observabilidad: El sistema debe estar preparado para ser monitorizado (puntos de entrada para Health Checks, métricas básicas y trazabilidad distribuida).

### C. Seguridad

- Autenticación y Autorización: Implementación de un ﬂujo seguro para usuarios ﬁnales y administradores (ej. simulación de OAuth2/OIDC, JWT ﬁrmados, RBAC).

- Protección de Datos: Garantizar que los datos sensibles (información de pago, datos personales) se manejen siguiendo buenas prácticas de seguridad.

## 3. El Entregable (Requisitos Obligatorios)

El candidato deberá entregar un Monorepo en un repositorio Git público que contenga:

- 1. Backend: Desarrollado en tecnología Java (Spring Boot 4.x, Quarkus o Micronaut).
- 2. Frontend: Desarrollado en Angular o React, que permita al menos:
  - Visualizar la disponibilidad de parkings en tiempo real.
  - Realizar una reserva de plaza.
  - Ver el estado de las reservas del usuario.
- 3. Documentación (/docs): \* Diagrama de arquitectura del sistema completo (C4 Model recomendado: Contexto y Contenedores).
  - Justiﬁcación de las decisiones tecnológicas (bases de datos elegidas, gestión de estado, etc.).
  - Manual de uso de Inteligencia Artiﬁcial (ver apartado 5).

- 4. Despliegue Local Uniﬁcado: Un único archivo docker-compose.yml en la raíz del proyecto. Al ejecutar docker compose up, se debe levantar todo el ecosistema: Frontend, Backend, Bases de Datos, Brokers de mensajería (si aplican) y componentes de infraestructura necesarios para que la solución sea 100%
     funcional.

## 4. El Reto de Negocio a Implementar (Alcance de la Demo)

No se pide el sistema global completo, pero la demo debe ser un Mínimo Producto Viable Arquitectónico (MVP) que incluya:

- Servicio de Catálogo/Disponibilidad: Consulta rápida de parkings y plazas libres.

- Servicio de Reservas: Flujo de creación, confirmación y cancelación de una reserva.

- Simulador de Pagos: Un componente asíncrono que procese el pago de la reserva y transicione el estado de la misma (Pendiente -> Conﬁrmada / Rechazada).

## 5. Uso de Herramientas de IA

Se permite y se fomenta el uso de herramientas de IA (GitHub Copilot, ChatGPT, Claude, etc.). Sin embargo, para evaluar la capacidad senior del candidato, es obligatorio incluir
en la documentación:

- Relación de herramientas: Qué herramientas se usaron y para qué (ej. ChatGPT para generar el boilerplate de Angular, Copilot para los tests unitarios).

- Flujos y Prompts: Ejemplos de los prompts más relevantes utilizados para resolver problemas complejos de la prueba.

- Reﬂexión crítica: Qué código o diseño sugerido por la IA fue rechazado por el candidato y por qué razones arquitectónicas o de seguridad.

## 6. La Defensa Técnica (Evaluación en Vivo)

La entrevista de defensa durará aproximadamente 60 minutos y se estructurará de la siguiente manera:

- 1. Arquitectura y Decisiones (30 min): El candidato expondrá el diagrama de su solución y defenderá por qué eligió ciertas tecnologías, patrones de diseño y estrategias de escalabilidad.

- 2. Cambios sobre la base de código (30 min): Se plantearán cambios en caliente sobre su propio código para evaluar el nivel de comprensión de la solución.
