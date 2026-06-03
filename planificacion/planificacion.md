# Plan de Desarrollo Extenso: Integración de Seguridad en Backend
**Proyecto:** MiniMarket Plus
**Asignatura:** Desarrollo Backend II

---

## 1. Visión General del Proyecto
El objetivo es modernizar la arquitectura backend de "MiniMarket Plus", una cadena con 10 sucursales en expansión. Se requiere implementar un sistema de seguridad robusto que soporte autenticación stateless (JWT), autorización basada en roles (Clientes, Empleados, Administradores) y protección contra vulnerabilidades conocidas, integrando además conceptos de servicios de autenticación externa (LDAPS/IDaaS).

---

## 2. Fases de Desarrollo

### FASE 1: Análisis y Planificación Estratégica (Evaluación: 10 pts)
**Objetivo:** Definir la arquitectura de seguridad y documentar las amenazas.
*   **Identificación de Amenazas:** 
    *   Analizar vulnerabilidades del código base (Acceso no autorizado, SQL Injection, XSS, CSRF).
    *   Documentar el impacto potencial de estas amenazas en la operación del minimarket (ej. alteración de inventario, filtración de datos de clientes).
*   **Diseño de Estrategia de Autenticación:**
    *   Justificar el uso de **JWT** como esquema principal debido a su naturaleza stateless, ideal para la escalabilidad requerida por las compras en línea y el sistema de fidelización.
    *   Definir el flujo de autenticación para los tres roles: `ROLE_CLIENTE`, `ROLE_EMPLEADO`, `ROLE_ADMIN`.
*   **Planificación de Interoperabilidad (LDAPS/IDaaS):**
    *   Diseñar cómo se podrían integrar sistemas legados de la empresa (LDAPS) para la autenticación interna de empleados o administradores.

### FASE 2: Configuración del Entorno de Desarrollo (Evaluación: 20 pts)
**Objetivo:** Preparar el proyecto Spring Boot descargado con las librerías necesarias.
*   **Gestión de Dependencias (pom.xml):**
    *   `spring-boot-starter-security`
    *   `spring-boot-starter-web`
    *   `jjwt` (Para la gestión de tokens JSON Web).
    *   Dependencias para LDAP si se implementa un servidor embebido (`spring-ldap-core`, `spring-security-ldap`, `unboundid-ldapsdk`).
*   **Configuración Base (`application.properties`):**
    *   Configurar conexión a la base de datos segura.
    *   (Opcional para alcance extra) Definir propiedades para el servidor LDAP embebido (`spring.ldap.embedded.port`, `spring.ldap.embedded.base-dn`).

### FASE 3: Implementación de Autenticación y Autorización (Evaluación: 30 pts)
**Objetivo:** Codificar los mecanismos de seguridad con Spring Security.
*   **Implementación de JWT (15 pts):**
    *   Desarrollar la clase `UserDetailsService` para conectar los usuarios de la base de datos con el contexto de seguridad.
    *   Configurar `BCryptPasswordEncoder` para el cifrado seguro de contraseñas.
    *   Crear la clase `JwtUtil` con métodos para: generar token, extraer claims, validar firma y comprobar expiración.
    *   Implementar `JwtRequestFilter` para interceptar cada petición HTTP y validar el token en el header `Authorization`.
*   **Gestión de Roles y Permisos (15 pts):**
    *   Habilitar seguridad a nivel de métodos con `@EnableGlobalMethodSecurity(prePostEnabled = true)`.
    *   Proteger los controladores REST. Ejemplos:
        *   `@PreAuthorize("hasRole('CLIENTE')")` para endpoints de consultas de stock y reservas.
        *   `@PreAuthorize("hasRole('EMPLEADO') or hasRole('ADMIN')")` para gestión de inventario y actualización de stock.
        *   Configurar `SecurityFilterChain` para definir rutas públicas (ej. `/auth/login`, `/auth/register`) y rutas privadas.

### FASE 4: Pruebas de Seguridad y Auditoría (Evaluación: 15 pts)
**Objetivo:** Validar la protección del sistema contra ataques.
*   **Pruebas Funcionales:**
    *   Ejecutar peticiones (usando Postman/Insomnia) con distintos tokens para verificar el correcto bloqueo/acceso según el rol.
*   **Mitigación de Amenazas Específicas:**
    *   **SQL Injection:** Verificar el uso de JPA/Hibernate u ORM parametrizado en los repositorios.
    *   **XSS:** Asegurar que las entradas de texto (ej. nombres de productos, comentarios) se estén sanitizando o que las respuestas JSON estén correctamente codificadas.
    *   **CSRF:** Configurar adecuadamente Spring Security (en arquitecturas REST puras con JWT, CSRF suele deshabilitarse `csrf().disable()` ya que no se usan cookies de sesión, pero esto debe estar **justificado técnicamente en el informe**).
*   **Documentación de Pruebas:**
    *   Capturar evidencias (pantallazos, logs) de los ataques simulados y cómo el backend los rechaza.

### FASE 5: Documentación Técnica y Despliegue (Evaluación: 25 pts)
**Objetivo:** Consolidar los entregables finales.
*   **Repositorio GitHub (10 pts):**
    *   Crear un repositorio estructurado (`/src`, `/docs`).
    *   Redactar un `README.md` técnico que explique cómo levantar el proyecto, configurar las variables de entorno y los endpoints principales.
    *   Asegurar un historial de commits limpio y descriptivo.
*   **Informe Final (15 pts):**
    *   **Capítulo 1:** Análisis de amenazas y justificación de JWT (y posible uso de LDAPS para empleados).
    *   **Capítulo 2:** Guía paso a paso de la configuración realizada en Spring Security y los filtros.
    *   **Capítulo 3:** Explicación técnica detallada de la protección contra XSS, CSRF y SQL Injection, apoyada por las pruebas de la Fase 4.

---

## 3. Checklist de Control de Calidad (Basado en Pauta)

- [ ] Analiza todas las amenazas relevantes con justificación técnica completa y contextualizada.
- [ ] Configura completamente el framework, integrando JWT, roles y control de acceso funcional.
- [ ] Implementa correctamente JWT: generación, validación y seguridad del token.
- [ ] Asigna roles y permisos a todos los endpoints según requerimientos y sin errores
- [ ] Prueba exhaustivamente la protección contra XSS, CSRF, y SQL Injection, documentando resultados completos.
- [ ] Sube el código completo a un repositorio bien estructurado y documentado.
- [ ] Elabora un informe completo y técnico con análisis, pasos de configuración y justificaciones.