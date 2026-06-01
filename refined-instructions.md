# Instrucciones

## Instrucciones generales

Diseñar un servicio de gestión de reservas de parking basado en las siguientes instrucciones:

- La aplicación tendrá un conjunto de APIs REST basadas en Spring Boot 4.

- La aplicación definirá un frontend desarrollado con React para la búsqueda y gestión de reservas.

- La aplicación tendrá una pasarela de pago mockeada desarrollada con React.

- Las APIs de negocio usarán DDD implementado con AXON sin event sourcing. Los agregados se almacenarán en base de datos usando repositorios de spring.

- Se utilizará KeyCloak para la autenticación de usuarios.

- El sistema debe usar Arquitectura Hexagonal y Clean Code.

- El sistema usara el patrón SAGA para garantizar la coherencia eventual.

- El sistema deberá estar diseñado para casos masivos de concurrencia y picos de tráfico masivos (por ejemplo gestión de eventos de temporada).

- El sistema deberá estar diseñado para latencias mínimas.

- El sistema debe ser resilente a fallos estructurales.

- El sistema usará el patrón circuit breaker para protegerlo de degradaciones en cascada.

- Se determinará una estrategia para la concurrencia y la sobreventa de plazas de aparcamiento simultáneas (Optimistic/Pessimistic Locking, Distributed Locks con Redis, etc.).

- La información de los usuarios se considerará sensible y deberá estar convenientemente protegida en el sistema.

## Flujo de ejemplo

### Busqueda parkings disponibles

- El usuario realizará desde el frontend una búsqueda libre de texto que deberá ser resuelta por las diferentes entidades disponibles (ciudad, aparcamiento concreto, aeropuerto, etc).

Los datos de búsqueda serán:

  - Texto libre
  - Fecha y hora de entrada
  - Fecha de salida

El frontend usará una API de búsqueda geolocalizada que a partir de los datos de texto devuelva un listado de opciones.

Estas opciones contendrán:

  - Nombre del parking
  - Tarifa
  - Tags con las características del parking (entrada express, cancelación gratuita, etc)
  - Warning si quedan pocas plazas disponibles
  - Información para geolocalizar el parking para mostrarlo en un mapa

El usuario no tendrá que estar autenticado para realizar la busqueda.

### Comienzo de la reserva

A partir de las opciones sugeridas el usuario comenzará el proceso de realización de la reserva.

Las reservas tendrán una tarifa variable en función de la disponibilidad y se utilizará un sistema externo de cálculo de tarifa.

Al iniciar el proceso de reserva se establecerá un precio fijo con una ventana temporal configurable que devolverá el servicio de calculo de tarifa.

Si el usuario no está logeado se mostrará un formulario de alta de usuario con:

  - Nombre completo (un único campo) obligatorio
  - Email obligatorio
  - Matricula del vehiculo
  - Checkbox de si se requiere factura
  - Checkbox para aceptar publicidad
  - Checkbox para aceptar los terminos y condiciones y politicas de privacidad

Si el usuario está logeado simplemente aparecerá una pantalla con los datos de la reserva.

### Método de pago

Cuando el usuario haya sido creado se establecerán los métodos de pago. El frontal deberá consultar a la API de pagos las opciones disponibles que estarán mockeadas.

Al seleccionar un método de pago la aplicación redigirá a la pasarela. Desde ahí completaremos el pago y al hacerlo nos redirigirá de vuelta a la página de gestión de reserva.

Desde la página de reserva tendremos la información confirmada si el proceso ha sido correcto.

## Otras funcionalidades

El frontal deberá tener dos roles de usuario:

- Cliente
- Administrador

Los roles/grupos estarán definidos en Keycloak.

Los clientes podrán

- Consultar el historico de reservas
- Cancelar reservas que puedan ser cancelables (con o sin penalización)

El administrador podrá:

- Consultar el crud de parkings con la información disponible
- Consultar el estado de cada uno de ellos
- Consultar la información de las cancelaciones (sin datos sensibles del usuario)
