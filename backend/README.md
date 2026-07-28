# Plataforma de Trueque Digital - MarketExchange

## CS 2031 Desarrollo Basado en Plataforma

**Integrantes:**
- Romero Padilla, Luis Anthony
- Nunayalle Brañes, Llorent Eloy
- Vargas Iglesias, Hanks Jean Pierce

## Índice
- [Introducción](#introducción)
- [Identificación del Problema o Necesidad](#identificación-del-problema-o-necesidad)
- [Descripción de la Solución](#descripción-de-la-solución)
- [Modelo de Entidades](#modelo-de-entidades)
- [Testing y Manejo de Errores](#testing-y-manejo-de-errores)
- [Medidas de Seguridad Implementadas](#medidas-de-seguridad-implementadas)
- [Eventos y Asincronía](#eventos-y-asincronía)
- [GitHub](#github)
- [Conclusión](#conclusión)
- [Apéndices](#apéndices)

## Introducción

### Contexto
En un mundo cada vez más preocupado por los efectos del consumismo desmedido, el intercambio de objetos emerge como una solución sostenible y consciente. MarketExchange es una plataforma diseñada para responder a esta necesidad, permitiendo a los usuarios intercambiar bienes de manera directa y eficiente. El proyecto se enfoca en mejorar las interacciones entre los usuarios, proporcionando una interfaz intuitiva que facilita la negociación y cierre de acuerdos de manera transparente y segura. Además, cuenta con un sistema de evaluación, donde los participantes pueden calificar sus experiencias y garantizar la calidad de los productos intercambiados, fomentando la confianza dentro de la comunidad. MarketExchange no solo propone una alternativa al consumo tradicional, sino que impulsa un modelo de economía circular, prolongando la vida útil de los objetos a través del intercambio y el reuso.

### Objetivos del Proyecto
- Facilitar el intercambio directo de bienes entre usuarios.
- Proporcionar un sistema seguro y transparente de acuerdos de intercambio.
- Implementar un sistema de retroalimentación que permita evaluar a los usuarios después de cada intercambio.
- Fomentar la confianza y seguridad en los acuerdos entre usuarios.

## Identificación del Problema o Necesidad

### Descripción del Problema
El sistema busca resolver la necesidad de una plataforma eficiente y segura para el intercambio de bienes entre usuarios sin involucrar dinero. En un contexto donde el consumismo y el desperdicio de objetos es una preocupación creciente, muchas personas prefieren intercambiar productos que ya no necesitan por aquellos que les son útiles. Sin embargo, los intercambios tradicionales enfrentan desafíos como la falta de confianza entre las partes, la dificultad para coordinar envíos y la falta de un sistema formal de retroalimentación sobre la calidad del intercambio.

### Justificación
El aumento del consumismo ha generado la necesidad de explorar alternativas más sostenibles. Muchos productos en buen estado pierden su valor en el mercado tradicional, mientras que otras personas podrían necesitarlos. MarketExchange ofrece una plataforma digital que facilita el trueque directo entre usuarios, permitiendo intercambiar productos sin dinero de por medio, fomentando así una economía circular.


## Descripción de la Solución

### Actores del Negocio
1. **Usuario registrado**: Persona que se registra en la plataforma para intercambiar productos.
2. **Administrador**: Persona encargada de gestionar la plataforma y velar por el cumplimiento de las políticas de uso.
3. **Participante en un intercambio**: Usuario que publica o recibe solicitudes de intercambio.

### Funcionalidades Clave
1. **Publicación de productos para intercambio**: Los usuarios pueden publicar los productos que desean intercambiar, especificando condiciones y características.
2. **Sistema de acuerdos**: Los usuarios pueden negociar y acordar un intercambio de manera directa.
3. **Sistema de retroalimentación**: Después de cada intercambio, los usuarios pueden dejar comentarios y calificaciones para evaluar la calidad del trueque.
4. **Historial de intercambios**: Cada usuario tiene acceso a un historial de los intercambios que ha realizado en la plataforma.
5. **Notificaciones de intercambios**: Los usuarios reciben notificaciones sobre el estado de sus intercambios.


### Entidades del Negocio
1. **Usuario**
2. **Shipment**
3. **Raiting**
4. **Item**
5. **Category** 
6. **Agreement**

### Casos de uso del Negocio
1.Publicación de productos para intercambio: Los usuarios pueden publicar productos que desean intercambiar, especificando detalles como la descripción, estado y condiciones del producto. Esto les permite mostrar lo que ofrecen a otros usuarios interesados en intercambiar.

2.Negociación de acuerdos: Los usuarios pueden negociar directamente a través de la plataforma, estableciendo términos para los intercambios de manera transparente. Las negociaciones pueden incluir la selección de productos a intercambiar y la confirmación de las condiciones.

2.Sistema de retroalimentación: Tras cada intercambio, los usuarios tienen la opción de calificar y dejar comentarios sobre la experiencia. Este sistema de retroalimentación ayuda a mantener la confianza en la plataforma y asegura que los intercambios sean satisfactorios para ambas partes.

4.Historial de intercambios: Cada usuario puede acceder a su historial de intercambios, donde se registran todos los acuerdos completados. Esto permite a los usuarios revisar y rastrear sus actividades de trueque, lo que fomenta un mayor control sobre sus transacciones.

5.Sistema de notificaciones: La plataforma notifica a los usuarios sobre el estado de sus intercambios, desde la confirmación de un acuerdo hasta la actualización del estado del envío (Shipment), asegurando que los usuarios estén siempre al tanto del proceso.



### Tecnologías Utilizadas
**Lenguajes de Programación:
Java: Utilizado para desarrollar la lógica de negocio y las funcionalidades principales del backend.
Frameworks:

**Spring Boot: Implementado en el backend para manejar el flujo de datos y la interacción con las entidades de la plataforma.
Bootstrap: Usado para crear una interfaz de usuario moderna y responsiva, facilitando la interacción intuitiva entre los usuarios.
Bases de Datos:

**PostgreSQL: Utilizada para almacenar toda la información relacionada con los usuarios, productos, intercambios y calificaciones.
APIs Externas:


## Modelo de Entidades

### Diagrama de Entidades
![Diagrama de Entidades](./imagen.jpg)

### Descripción de Entidades


### 1.Usuario
Representa a los usuarios del sistema en la plataforma MarketExchange.


| **Atributo**        | **Tipo de Variable**   | **Descripción**                                               | **Criterios Limitantes**                                               |
|---------------------|------------------------|---------------------------------------------------------------|------------------------------------------------------------------------|
| `id`                | `Long`                 | Identificador único del usuario.                              | Debe ser único y generado automáticamente.                             |
| `firstname`         | `String`               | Nombre del usuario.                                           | No puede estar vacío, máximo 50 caracteres.                            |
| `lastname`          | `String`               | Apellido del usuario.                                         | No puede estar vacío, máximo 50 caracteres.                            |
| `email`             | `String`               | Correo electrónico del usuario.                               | Debe tener un formato válido de email, debe ser único.                 |
| `password`          | `String`               | Contraseña del usuario para autenticación.                    | Mínimo 8 caracteres, debe estar encriptada.                            |
| `phone`             | `String`               | Número de teléfono del usuario.                               | Longitud entre 7 y 15 dígitos.                                         |
| `address`           | `String`               | Dirección del usuario.                                        | No puede estar vacía, máximo 100 caracteres.                           |
| `role`              | `Enum (ADMIN, USER)`   | Rol del usuario dentro del sistema.                           | Debe ser uno de los valores permitidos: `ADMIN`, `USER`.               |
| `createdAt`         | `LocalDateTime`        | Fecha y hora de creación del usuario.                         | No puede ser nula, se genera automáticamente al momento de registro.   |

---

#### Métodos del endpoint `/usuarios`:

| **Método**           | **Ruta**          | **Roles Permitidos** | **Descripción**                                                   | **Excepciones**                                         | **Métodos de Service**                             |
|----------------------|-------------------|----------------------|-------------------------------------------------------------------|---------------------------------------------------------|----------------------------------------------------|
| `GET`                | `/{id}`           | `ADMIN`              | Devuelve el `UsuarioResponseDto` del usuario por su id.           | `ResourceNotFoundException`                             | `buscarUsuarioPorId(Long id)`                     |
| `GET`                | `/listar`         | `ADMIN`              | Devuelve la lista de todos los usuarios en formato `UsuarioResponseDto`. | -                                                       | `listarUsuarios()`                                |
| `PUT`                | `/{id}`           | `ADMIN`, `USER`      | Actualiza los datos del usuario identificado por su id.           | `ResourceNotFoundException`, `UnauthorizeOperationException`, `InvalidUserFieldException` | `actualizarUsuario(Long id, UsuarioRequestDto dto)`|
| `DELETE`             | `/{id}`           | `ADMIN`, `USER`      | Elimina el usuario identificado por su id.                       | `ResourceNotFoundException`, `UnauthorizeOperationException` | `eliminarUsuario(Long id)`                        |
| `GET`                | `/me`             | `USER`               | Devuelve el `UsuarioResponseDto` del usuario autenticado.         | `ResourceNotFoundException`, `UnauthorizeOperationException` | `getUsuarioOwnInfo()`                             |

---

#### Métodos adicionales del service:

| **Método**                | **Descripción**                                                                                   |
|---------------------------|---------------------------------------------------------------------------------------------------|
| `registrarUsuario`         | Crea un nuevo usuario validando los datos de entrada y disparando el evento `UsuarioCreadoEvent`. |
| `getUsuarioOwnInfo`        | Devuelve la información del usuario autenticado basado en el contexto de seguridad.              |

### 2.Item
Representa los ítems que los usuarios pueden intercambiar en la plataforma MarketExchange.

| **Atributo**        | **Tipo de Variable**   | **Descripción**                                               | **Criterios Limitantes**                                               |
|---------------------|------------------------|---------------------------------------------------------------|------------------------------------------------------------------------|
| `id`                | `Long`                 | Identificador único del ítem.                                  | Debe ser único y generado automáticamente.                             |
| `name`              | `String`               | Nombre del ítem.                                               | No puede estar vacío, máximo 100 caracteres.                           |
| `description`       | `String`               | Descripción del ítem.                                          | No puede estar vacía, máximo 255 caracteres.                           |
| `category`          | `Category`             | Categoría a la que pertenece el ítem.                          | No puede ser nula, debe estar referenciada a una categoría existente.  |
| `condition`         | `Enum (NEW, USED)`     | Condición del ítem (Nuevo o Usado).                            | No puede ser nula, debe ser `NEW` o `USED`.                            |
| `status`            | `Enum (PENDING, APPROVED, REJECTED)` | Estado del ítem en la plataforma.                               | Valores permitidos: `PENDING`, `APPROVED`, `REJECTED`.                 |
| `usuario`           | `Usuario`              | Usuario que está intercambiando el ítem.                       | No puede ser nulo, debe estar referenciado a un usuario existente.      |
| `createdAt`         | `LocalDateTime`        | Fecha y hora de creación del ítem.                             | Generado automáticamente al crear el ítem.                             |
| `updatedAt`         | `LocalDateTime`        | Fecha y hora de la última actualización del ítem.              | Actualizado automáticamente en cada modificación.                      |

---

#### Métodos del endpoint `/item`:

| **Método**           | **Ruta**                  | **Roles Permitidos** | **Descripción**                                                   | **Excepciones**                                         | **Métodos de Service**                              |
|----------------------|---------------------------|----------------------|-------------------------------------------------------------------|---------------------------------------------------------|-----------------------------------------------------|
| `POST`               | `/`                       | `USER`               | Crea un nuevo ítem.                                               | `ResourceNotFoundException`                             | `createItem(ItemRequestDto dto)`                    |
| `POST`               | `/{itemId}/approve`        | `ADMIN`              | Aprueba o rechaza un ítem.                                        | `ResourceNotFoundException`                             | `approveItem(Long itemId, Boolean approve)`         |
| `PUT`                | `/{itemId}`               | `USER`, `ADMIN`      | Actualiza un ítem existente.                                      | `ResourceNotFoundException`, `UnauthorizeOperationException` | `updateItem(Long itemId, ItemRequestDto dto)`    |
| `GET`                | `/{itemId}`               | `USER`, `ADMIN`      | Devuelve el `ItemResponseDto` del ítem por su id.                 | `ResourceNotFoundException`                             | `getItemById(Long itemId)`                          |
| `GET`                | `/`                       | `ADMIN`              | Devuelve la lista de todos los ítems en formato `ItemResponseDto`. | -                                                       | `getAllItems()`                                     |
| `DELETE`             | `/{itemId}`               | `USER`, `ADMIN`      | Elimina un ítem identificado por su id.                           | `ResourceNotFoundException`, `UnauthorizeOperationException` | `deleteItem(Long itemId)`                        |
| `GET`                | `/category/{categoryId}`  | `USER`, `ADMIN`      | Devuelve los ítems que pertenecen a una categoría específica.      | `ResourceNotFoundException`                             | `getItemsByCategory(Long categoryId)`               |
| `GET`                | `/user/{userId}`          | `USER`, `ADMIN`      | Devuelve los ítems que pertenecen a un usuario específico.         | `ResourceNotFoundException`                             | `getItemsByUser(Long userId)`                       |
| `GET`                | `/mine`                   | `USER`               | Devuelve los ítems del usuario autenticado.                       | `UnauthorizeOperationException`                         | `getUserItems()`                                    |

---

#### Métodos adicionales del service:

| **Método**                | **Descripción**                                                                                   |
|---------------------------|---------------------------------------------------------------------------------------------------|
| `approveItem`             | Cambia el estado de un ítem a `APPROVED` o `REJECTED` según la aprobación del administrador.      |
| `getItemsByCategory`      | Devuelve la lista de ítems filtrados por la categoría seleccionada.                               |
| `getItemsByUser`          | Devuelve la lista de ítems creados por un usuario específico.                                     |
| `getUserItems`            | Devuelve la lista de ítems creados por el usuario autenticado en la sesión.                      |


---

### 3.Category
Representa las categorías a las que pertenecen los ítems en la plataforma MarketExchange. Cada categoría puede contener múltiples ítems.

| **Atributo**        | **Tipo de Variable**   | **Descripción**                                               | **Criterios Limitantes**                                               |
|---------------------|------------------------|---------------------------------------------------------------|------------------------------------------------------------------------|
| `id`                | `Long`                 | Identificador único de la categoría.                           | Generado automáticamente.                                              |
| `name`              | `String`               | Nombre de la categoría.                                        | No puede estar vacío, máximo 100 caracteres.                           |
| `description`       | `String`               | Descripción de la categoría.                                   | No puede tener más de 255 caracteres.                                  |
| `items`             | `List<Item>`           | Lista de ítems asociados a la categoría.                       | Puede ser una lista vacía si no hay ítems asociados a esta categoría.   |

---

#### Métodos del endpoint `/category`:

| **Método**           | **Ruta**               | **Roles Permitidos** | **Descripción**                                                   | **Excepciones**                                         | **Métodos de Service**                              |
|----------------------|------------------------|----------------------|-------------------------------------------------------------------|---------------------------------------------------------|-----------------------------------------------------|
| `POST`               | `/`                    | `ADMIN`              | Crea una nueva categoría.                                          | `ResourceNotFoundException`                             | `createCategory(CategoryRequestDto dto)`            |
| `GET`                | `/`                    | `USER`, `ADMIN`      | Devuelve la lista de todas las categorías en formato `CategoryResponseDto`. | -                                               | `getAllCategories()`                               |
| `GET`                | `/{id}`                | `USER`, `ADMIN`      | Devuelve el `CategoryResponseDto` de una categoría por su id.      | `ResourceNotFoundException`                             | `getCategoryById(Long id)`                          |
| `PUT`                | `/{id}`                | `ADMIN`              | Actualiza una categoría por su id.                                 | `ResourceNotFoundException`                             | `updateCategory(Long id, CategoryRequestDto dto)`   |
| `DELETE`             | `/{id}`                | `ADMIN`              | Elimina una categoría por su id.                                   | `ResourceNotFoundException`                             | `deleteCategory(Long id)`                           |

---

#### Métodos adicionales del service:

| **Método**               | **Descripción**                                                                 |
|--------------------------|---------------------------------------------------------------------------------|
| `getAllCategories`        | Devuelve la lista de todas las categorías en la plataforma.                     |
| `getCategoryById`         | Devuelve una categoría específica junto con los ítems asociados.                |
| `createCategory`          | Crea una nueva categoría en la plataforma.                                      |
| `updateCategory`          | Actualiza los detalles de una categoría existente.                              |
| `deleteCategory`          | Elimina una categoría de la plataforma.                                         |
---

### 5.Agreement
Representa los acuerdos de intercambio entre dos usuarios en la plataforma MarketExchange. Un acuerdo tiene un estado que puede ser `PENDING`, `ACCEPTED` o `REJECTED`.

| **Atributo**        | **Tipo de Variable**   | **Descripción**                                               | **Criterios Limitantes**                                               |
|---------------------|------------------------|---------------------------------------------------------------|------------------------------------------------------------------------|
| `id`                | `Long`                 | Identificador único del acuerdo.                               | Generado automáticamente.                                              |
| `version`           | `Integer`              | Control de versiones para concurrencia optimista.              | -                                                                      |
| `state`             | `Enum(State)`          | Estado del acuerdo (PENDING, ACCEPTED, REJECTED).              | No puede ser nulo. Valores permitidos: `PENDING`, `ACCEPTED`, `REJECTED`.|
| `shipment`          | `Shipment`             | Información de envío relacionada con el acuerdo.               | -                                                                      |
| `item_ini`          | `Item`                 | Ítem ofrecido por el iniciador del acuerdo.                    | No puede ser nulo.                                                     |
| `item_fin`          | `Item`                 | Ítem ofrecido por el receptor del acuerdo.                     | No puede ser nulo.                                                     |
| `initiator`         | `Usuario`              | Usuario que inicia el acuerdo.                                 | No puede ser nulo.                                                     |
| `recipient`         | `Usuario`              | Usuario que recibe la propuesta de acuerdo.                    | No puede ser nulo.                                                     |
| `createdAt`         | `LocalDateTime`        | Fecha de creación del acuerdo.                                 | Generado automáticamente.                                              |
| `updatedAt`         | `LocalDateTime`        | Fecha de última actualización del acuerdo.                     | Actualizado automáticamente en cada modificación.                      |

---

#### Métodos del endpoint `/agreements`:

| **Método**           | **Ruta**               | **Roles Permitidos** | **Descripción**                                                   | **Excepciones**                                         | **Métodos de Service**                             |
|----------------------|------------------------|----------------------|-------------------------------------------------------------------|---------------------------------------------------------|----------------------------------------------------|
| `POST`               | `/`                    | `USER`, `ADMIN`      | Crea un nuevo acuerdo entre dos usuarios con los ítems especificados. | `ResourceNotFoundException`, `IllegalArgumentException`  | `createAgreement(AgreementRequestDto dto)`         |
| `GET`                | `/`                    | `USER`, `ADMIN`      | Devuelve la lista de todos los acuerdos en formato `AgreementResponseDto`. | -                                                      | `getAllAgreements()`                               |
| `GET`                | `/{id}`                | `USER`, `ADMIN`      | Devuelve el `AgreementResponseDto` de un acuerdo por su id.        | `ResourceNotFoundException`                             | `getAgreementById(Long id)`                        |
| `PUT`                | `/{id}`                | `USER`, `ADMIN`      | Actualiza un acuerdo existente por su id.                          | `ResourceNotFoundException`, `IllegalArgumentException` | `updateAgreement(Long id, AgreementRequestDto dto)`|
| `PUT`                | `/{id}/accept`         | `USER`, `ADMIN`      | Acepta un acuerdo en estado `PENDING`.                             | `ResourceNotFoundException`, `IllegalArgumentException` | `acceptAgreement(Long id)`                         |
| `PUT`                | `/{id}/reject`         | `USER`, `ADMIN`      | Rechaza un acuerdo en estado `PENDING`.                            | `ResourceNotFoundException`, `IllegalArgumentException` | `rejectAgreement(Long id)`                         |
| `DELETE`             | `/{id}`                | `USER`, `ADMIN`      | Elimina un acuerdo por su id.                                      | `ResourceNotFoundException`                             | `deleteAgreement(Long id)`                         |

---

#### Métodos adicionales del service:

| **Método**               | **Descripción**                                                                 |
|--------------------------|---------------------------------------------------------------------------------|
| `getAllAgreements`        | Devuelve la lista de todos los acuerdos en la plataforma.                       |
| `getAgreementById`        | Devuelve un acuerdo específico por su id.                                       |
| `createAgreement`         | Crea un nuevo acuerdo entre dos usuarios, validando los ítems ofrecidos.        |
| `updateAgreement`         | Actualiza los detalles de un acuerdo existente.                                 |
| `acceptAgreement`         | Cambia el estado del acuerdo a `ACCEPTED` y genera un `Shipment` asociado.      |
| `rejectAgreement`         | Cambia el estado del acuerdo a `REJECTED`.                                       |
| `deleteAgreement`         | Elimina un acuerdo de la plataforma.                                            |

---

### 5. Shipment
Representa los envíos asociados a un acuerdo de intercambio entre dos usuarios en la plataforma MarketExchange. Un envío incluye las direcciones de ambas partes (iniciador y receptor) y una fecha de entrega futura.

| **Atributo**         | **Tipo de Variable**   | **Descripción**                                               | **Criterios Limitantes**                                               |
|----------------------|------------------------|---------------------------------------------------------------|------------------------------------------------------------------------|
| `id`                 | `Long`                 | Identificador único del envío.                                 | Generado automáticamente.                                              |
| `agreement`          | `Agreement`            | Acuerdo asociado con el envío.                                 | No puede ser nulo.                                                     |
| `initiatorAddress`   | `String`               | Dirección del iniciador del acuerdo.                           | No puede estar vacío.                                                  |
| `receiveAddress`     | `String`               | Dirección del receptor del acuerdo.                            | No puede estar vacío.                                                  |
| `deliveryDate`       | `LocalDateTime`        | Fecha de entrega del envío.                                    | Debe ser una fecha futura.                                             |

---

#### Métodos del endpoint `/shipments`:

| **Método**           | **Ruta**               | **Roles Permitidos** | **Descripción**                                                   | **Excepciones**                                         | **Métodos de Service**                              |
|----------------------|------------------------|----------------------|-------------------------------------------------------------------|---------------------------------------------------------|-----------------------------------------------------|
| `POST`               | `/`                    | `ADMIN`, `USER`      | Crea un nuevo envío asociado a un acuerdo aceptado.                | `ResourceNotFoundException`, `IllegalStateException`     | `createShipmentForAgreement(Agreement agreement)`   |
| `GET`                | `/`                    | `ADMIN`, `USER`      | Devuelve la lista de todos los envíos en formato `ShipmentResponseDto`. | -                                                      | `getAllShipments()`                                |
| `GET`                | `/{id}`                | `ADMIN`, `USER`      | Devuelve el `ShipmentResponseDto` de un envío por su id.           | `RuntimeException`                                      | `getShipmentById(Long id)`                          |
| `PUT`                | `/{id}`                | `ADMIN`              | Actualiza un envío existente por su id.                            | `RuntimeException`                                      | `updateShipment(Long id, ShipmentRequestDto dto)`   |
| `DELETE`             | `/{id}`                | `ADMIN`              | Elimina un envío por su id.                                        | `RuntimeException`                                      | `deleteShipment(Long id)`                           |

---

#### Métodos adicionales del service:

| **Método**                   | **Descripción**                                                                     |
|------------------------------|-------------------------------------------------------------------------------------|
| `getAllShipments`            | Devuelve la lista de todos los envíos en la plataforma.                              |
| `createShipmentForAgreement` | Crea un envío para un acuerdo que ha sido aceptado, generando una fecha de entrega.  |
| `getShipmentById`            | Devuelve un envío específico por su id.                                              |
| `updateShipment`             | Actualiza los detalles de un envío existente.                                        |
| `deleteShipment`             | Elimina un envío de la plataforma.                                                   |

---
### 6.Rating
Representa las calificaciones y comentarios dejados por los usuarios después de completar un intercambio en la plataforma MarketExchange. Las calificaciones permiten evaluar la experiencia del intercambio entre dos usuarios.

| **Atributo**         | **Tipo de Variable**   | **Descripción**                                               | **Criterios Limitantes**                                               |
|----------------------|------------------------|---------------------------------------------------------------|------------------------------------------------------------------------|
| `id`                 | `Long`                 | Identificador único de la calificación.                        | Generado automáticamente.                                              |
| `rating`             | `int`                  | Valor numérico de la calificación entre 1 y 5.                 | Debe estar entre 1 y 5.                                                |
| `comment`            | `String`               | Comentario descriptivo del intercambio.                        | No puede estar vacío, máximo 500 caracteres.                           |
| `raterUsuario`       | `Usuario`              | Usuario que realiza la calificación.                           | No puede ser nulo.                                                     |
| `usuario`            | `Usuario`              | Usuario que recibe la calificación.                            | No puede ser nulo.                                                     |
| `createdAt`          | `LocalDateTime`        | Fecha y hora en que se realizó la calificación.                | Se genera automáticamente al momento de crear la calificación.         |

---

#### Métodos del endpoint `/ratings`:

| **Método**           | **Ruta**               | **Roles Permitidos** | **Descripción**                                                   | **Excepciones**                                         | **Métodos de Service**                              |
|----------------------|------------------------|----------------------|-------------------------------------------------------------------|---------------------------------------------------------|-----------------------------------------------------|
| `POST`               | `/crear`               | `USER`               | Crea una nueva calificación para un usuario.                      | `RuntimeException`, `ResourceNotFoundException`          | `crearRating(RatingRequestDto dto)`                 |
| `GET`                | `/listar`              | `USER`, `ADMIN`      | Devuelve la lista de todas las calificaciones.                    | -                                                       | `listarRatings()`                                   |
| `GET`                | `/usuario/{usuarioId}` | `USER`, `ADMIN`      | Devuelve todas las calificaciones de un usuario específico.        | `ResourceNotFoundException`                              | `obtenerRatingsPorUsuario(Long usuarioId)`          |
| `DELETE`             | `/{id}`                | `USER`, `ADMIN`      | Elimina una calificación por su id.                                | `ResourceNotFoundException`, `UnauthorizeOperationException` | `deleteItem(Long id)`                               |

---

#### Métodos adicionales del service:

| **Método**                    | **Descripción**                                                                     |
|-------------------------------|-------------------------------------------------------------------------------------|
| `crearRating`                 | Crea una nueva calificación para un intercambio y la almacena en la base de datos.   |
| `listarRatings`               | Devuelve una lista de todas las calificaciones realizadas en la plataforma.          |
| `obtenerRatingsPorUsuario`    | Devuelve todas las calificaciones recibidas por un usuario específico.               |
| `deleteItem`                  | Elimina una calificación existente si el usuario tiene permisos para hacerlo.        |


## Testing y Manejo de Errores

### Niveles de Testing Realizados

#### Pruebas Unitarias
Las pruebas unitarias se han desarrollado para cada controlador y endpoint de la aplicación.

| **Caso de prueba**              | **Descripción**                                                                                          | **Criterio de éxito**                                                                                      |
|---------------------------------|----------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| Usuario realiza un acuerdo      | Simulación de un usuario iniciando un acuerdo de intercambio. Se evalúa la creación correcta del acuerdo. | El acuerdo se crea correctamente y se refleja en la respuesta con el estado inicial "PENDING".              |
| Usuario acepta un acuerdo       | Simulación de aceptación de un acuerdo por el otro usuario involucrado en el intercambio.                 | El acuerdo cambia de estado a "ACCEPTED" y el sistema genera un `Shipment` automáticamente.                 |
| Usuario crea un ítem            | Simulación de la creación de un ítem para intercambio.                                                    | El ítem se crea correctamente y se refleja en la lista del usuario.                                          |
| Usuario califica a otro usuario | Simulación de la creación de una calificación después de un intercambio.                                  | La calificación se guarda y se refleja en la lista de calificaciones del usuario receptor.                   |

#### Tipo de pruebas

| **Tipo de prueba**    | **Descripción**                                                                                              | **Criterio de éxito**                                                                                      |
|----------------------|--------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| Pruebas de respuesta  | Se evaluará el código de respuesta HTTP y, en caso exista, el `ResponseEntity` del endpoint.                   | El código de respuesta HTTP y el contenido del `ResponseEntity` son los esperados.                            |
| Pruebas de DTOs       | Se evaluará la validez de los datos enviados en las solicitudes POST y las respuestas GET.                     | La aplicación maneja correctamente los DTOs, permitiendo solo datos válidos y retornando respuestas correctas. |
| Manejo de excepciones | Se evaluará el código de respuesta HTTP en situaciones de errores forzados.                                   | Los endpoints responden con las excepciones esperadas en situaciones de error.                               |
| Pruebas de seguridad  | Se evaluará que solo los usuarios autorizados puedan acceder a los endpoints protegidos.                      | Los endpoints responden con las excepciones de seguridad correspondientes según los permisos asignados.      |

### Pruebas de Sistema
Estas pruebas simulan el flujo completo de procesos de negocio.

| **Nombre de prueba**              | **Descripción**                                                                                                                                                                                                                          |
|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Usuario inicia un intercambio     | Simulación del flujo desde que un usuario propone un acuerdo de intercambio hasta que el otro usuario acepta el acuerdo. Se evalúan los cambios de estado y la creación de envíos asociados.                                                |
| Usuario califica a otro usuario   | Simulación del flujo de calificación después de completar un intercambio. Se evalúa que la calificación se guarde y aparezca correctamente en el perfil del usuario receptor.                                                              |
| Seguridad en el acceso a datos    | Evaluación de los permisos de acceso de usuarios regulares y administradores. Se prueba que los usuarios solo puedan acceder a su propia información y que los administradores puedan ver todos los datos.                                   |

### Resultados
Se detectaron y corrigieron errores relacionados con la actualización de acuerdos y la generación de envíos automáticos. El manejo global de excepciones asegura una experiencia de usuario coherente, con respuestas claras y útiles ante errores, y los administradores pueden revisar detalles de los problemas en los registros del sistema.

## Medidas de Seguridad Implementadas

### Seguridad de Datos

- **Cifrado de contraseñas**: Se utiliza bcrypt para el hash de contraseñas, garantizando que no se almacenen en texto plano.
- **Autenticación**: Se implementa JWT (JSON Web Tokens) para autenticar a los usuarios.
- **Autorización**: Permisos basados en roles (`ADMIN`, `USER`) aseguran que cada usuario solo pueda acceder a las funcionalidades que le corresponden.

### Prevención de Vulnerabilidades

- **Protección contra inyección SQL**: Se utilizan consultas preparadas para evitar ataques de inyección SQL.
- **Mitigación de XSS y CSRF**: Se emplean tokens CSRF y sanitización de entradas de usuario para prevenir ataques de scripting y falsificación de solicitudes.

## Eventos y Asincronía
El sistema implementa eventos asincrónicos para gestionar procesos en segundo plano sin interrumpir la experiencia del usuario. Algunos ejemplos incluyen:

- **Procesamiento de pedidos**: Los pedidos se gestionan en segundo plano, actualizando los estados sin afectar la usabilidad de la aplicación.
- **Notificaciones de estado**: Se generan eventos para actualizar el estado de los pedidos y notificar a los usuarios sobre cambios importantes.

## GitHub

### GitHub Projects
Se utilizó GitHub Projects para gestionar tareas y asignarlas a los miembros del equipo, con seguimiento del progreso. Se asignaron prioridades a los issues para definir cuáles eran críticas (CRITICAL), importantes (HIGH), medias (MEDIUM), bajas (LOW) u opcionales (OPTIONAL).

### GitHub Actions
Se configuró un flujo de CI/CD con GitHub Actions para ejecutar pruebas automáticas y realizar despliegues en un servidor de prueba, garantizando la estabilidad y calidad del código antes de su integración en producción.

## Conclusión

### Logros del Proyecto
Este proyecto ha permitido implementar una plataforma de trueque eficiente, donde los usuarios pueden intercambiar bienes, calificar sus transacciones y mantener la seguridad de sus datos mediante autenticación y permisos adecuados.

### Aprendizajes Clave
- Profundización en la integración de eventos asincrónicos para mejorar la fluidez de los procesos en segundo plano.
- Optimización del flujo de CI/CD para asegurar entregas continuas sin interrupciones.

### Trabajo Futuro
- Implementación de notificaciones en tiempo real para mejorar la experiencia de los usuarios y meseros.
- Optimización del sistema de reseñas para incluir encuestas más detalladas.

## Apéndices

### Licencia
Este proyecto está bajo la licencia MIT.

### Referencias
- Documentación de Node.js: [https://nodejs.org/](https://nodejs.org/)
- Documentación de MySQL: [https://dev.mysql.com/doc/](https://dev.mysql.com/doc/)
