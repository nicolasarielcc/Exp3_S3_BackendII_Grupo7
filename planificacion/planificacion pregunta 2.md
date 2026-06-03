En éste archivo documentaremos la guía de configuración y explicación de la implementación. 

# Pregunta 2: Guía de Configuración y Explicación de la Implementación
**Proyecto:** MiniMarket Plus  
**Fase:** Implementación del Framework de Seguridad

A continuación, se detalla la guía paso a paso de cómo se integrará el framework Spring Security con JWT en la arquitectura del backend de MiniMarket Plus para mitigar las vulnerabilidades identificadas.

---

## 1. Preparación del Proyecto y Configuración del Entorno

### 1.1 Descarga e Inicialización
Se tomará el código base proporcionado del backend de "MiniMarket Plus" (que actualmente carece de restricciones de seguridad) y se importará en el entorno de desarrollo (IDE como IntelliJ IDEA o Eclipse).

### 1.2 Configuración de Dependencias (pom.xml)
Para habilitar el framework de seguridad y la gestión de tokens, inyectaremos las siguientes dependencias en nuestro gestor de paquetes Maven:
* `spring-boot-starter-security`: Proveerá el núcleo de autenticación, interceptores y control de acceso.
* `spring-boot-starter-web`: Necesario para exponer nuestros controladores REST (endpoints).
* `jjwt` (Java JWT): Librería estándar que utilizaremos para la creación, firma y validación de los tokens (requerirá las dependencias `jjwt-api`, `jjwt-impl`, y `jjwt-jackson`).

---

## 2. Implementación del Módulo de Autenticación

### 2.1 Servicio de Detalles de Usuario (`UserDetailsServiceImpl`)
Crearemos una clase que implemente la interfaz `UserDetailsService` de Spring Security. 
* **Aplicación al proyecto:** Este servicio se conectará con nuestro `UsuarioRepository` para buscar en la base de datos de MiniMarket Plus si el usuario (rut o correo) existe.
* Mapearemos los roles definidos en la Fase 1 (`ROLE_CLIENTE`, `ROLE_EMPLEADO`, `ROLE_ADMIN`) a la clase `GrantedAuthority` de Spring para que el framework los entienda.

### 2.2 Encriptación de Contraseñas (`BCrypt`)
Para cumplir con la normativa de confidencialidad de datos (identificada en la Fase 1), no guardaremos contraseñas en texto plano.
* **Aplicación al proyecto:** Se definirá un `@Bean` de `BCryptPasswordEncoder`. Al registrar un nuevo usuario (cliente o empleado), su contraseña pasará por este codificador antes de guardarse en la BD. Al hacer login, Spring Security usará este mismo bean para comparar la contraseña ingresada con el hash de la BD.

---

## 3. Implementación del Sistema JWT (Stateless)

Dado que elegimos JWT por la necesidad de escalabilidad a 50 sucursales, implementaremos los siguientes componentes:

### 3.1 Utilidad JWT (`JwtUtil`)
Crearemos una clase `@Component` responsable de toda la lógica criptográfica del token:
* **`generateToken()`**: Tomará los datos del usuario autenticado (username y rol) y generará un token firmado con una clave secreta fuerte (SecretKey). Se configurará un tiempo de expiración (ej. 2 horas) para minimizar la exposición de la sesión.
* **`validateToken()`**: Verificará que la firma criptográfica sea correcta y que el token no haya expirado.
* **`extractUsername()` / `extractClaims()`**: Leerá la carga útil (payload) del token sin necesidad de consultar la base de datos.

### 3.2 Filtro de Intercepción (`JwtRequestFilter`)
Crearemos un filtro que extienda de `OncePerRequestFilter`.
* **Aplicación al proyecto:** Este filtro interceptará **todas** las peticiones HTTP (compras, inventario, reportes) que lleguen a los microservicios de MiniMarket.
* Extraerá el encabezado `Authorization: Bearer <token>`.
* Si el token es válido, extraerá el usuario y sus roles, y establecerá el `SecurityContextHolder` para indicar a Spring Security que el usuario actual está autorizado a operar.

---

## 4. Autorización Basada en Roles (RBAC)

Aquí es donde aplicaremos la matriz de accesos definida en la Pregunta 1.

### 4.1 Configuración Global (`SecurityConfig`)
Crearemos la clase de configuración de seguridad principal (`@EnableWebSecurity`):
* **Desactivación de CSRF:** Como nuestra API es REST y usa JWT en lugar de cookies de sesión, deshabilitaremos CSRF (`csrf().disable()`), justificando técnicamente que la vulnerabilidad no aplica a este esquema.
* **Gestión de Sesiones:** Configuraremos `SessionCreationPolicy.STATELESS` para asegurar que el servidor no guarde memoria de la sesión, forzando a que cada request de MiniMarket traiga su JWT válido.
* **Rutas Públicas:** Se dejarán abiertos endpoints como `/auth/login` y `/api/productos/public` (para que cualquiera vea el catálogo).

### 4.2 Restricción de Endpoints (`@PreAuthorize`)
Protegeremos los Controladores (Controllers) usando anotaciones a nivel de método:
* `@PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")`: Aplicado al método `realizarCompra()`.
* `@PreAuthorize("hasRole('EMPLEADO') or hasRole('ADMIN')")`: Aplicado al método `actualizarStock()` y `procesarDevolucion()`.
* `@PreAuthorize("hasRole('ADMIN')")`: Aplicado a los métodos de gestión de usuarios (`listarUsuarios()`, que antes era vulnerable a acceso no autorizado) y reportes financieros.

---

## 5. Plan de Pruebas Iniciales (Validación de Seguridad)

Para garantizar que la implementación funciona correctamente frente a los requerimientos del cliente, se realizarán las siguientes pruebas funcionales (usando Postman/Insomnia):

1.  **Prueba de Autenticación (Login):** Enviar credenciales válidas al endpoint `/auth/login` y comprobar que el backend retorna un Token JWT correctamente firmado.
2.  **Prueba de Aislamiento de Roles (Control de Acceso):**
    * *Test 1:* Iniciar sesión como `CLIENTE`, tomar el token, e intentar acceder a `/api/inventario`. El sistema debe retornar un HTTP 403 (Forbidden).
    * *Test 2:* Iniciar sesión como `EMPLEADO` e intentar acceder a `/api/usuarios/modificar-rol`. El sistema debe retornar HTTP 403 (Forbidden).
    * *Test 3:* Iniciar sesión como `ADMIN` y acceder exitosamente (HTTP 200) a todas las rutas anteriores.
3.  **Prueba de Sesión Expirada:** Modificar el código temporalmente para que el JWT expire en 1 minuto. Iniciar sesión, esperar 61 segundos, e intentar hacer una petición. El sistema debe denegar el acceso (HTTP 401 Unauthorized).