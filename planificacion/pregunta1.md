# Pregunta 1: Análisis y Selección de la Estrategia y Framework de Seguridad
**Proyecto:** MiniMarket Plus  
**Asignatura:** Desarrollo Backend II  
**Evaluación:** FASE 1 - Análisis y Planificación Estratégica (10 pts)

---

## PASO 1: Análisis de Estrategias de Implementación de Frameworks de Seguridad

### 1.1 Revisión de Requerimientos del Cliente y Análisis de Amenazas

#### Requerimientos Operacionales del Cliente - MINIMARKET PLUS

**Contexto Empresarial:**
- Cadena de 10 sucursales en expansión geográfica
- Plan de crecimiento a 50 sucursales en 3 años
- Plataforma de compras en línea nacional en desarrollo
- Múltiples usuarios concurrentes por sucursal
- Integración con sistemas de fidelización de clientes
- Gestión centralizada de inventario

**Necesidades de Acceso:**
- **Clientes:** Consultar productos, realizar compras, ver historial personal
- **Empleados:** Gestionar inventario local, procesar devoluciones, consultar ventas
- **Administradores:** Reportes globales, gestión de empleados, auditoría, configuración

---

### 1.2 Identificación de Principales Amenazas en MINIMARKET PLUS

| Amenaza | Descripción | Impacto | Estado | Riesgo | Norma |
|---------|-------------|---------|--------|--------|-------|
| **Acceso No Autorizado** | Usuarios accediendo a datos ajenos | Cliente ve datos de otros; escalación de privilegios; violación RGPD | SIN mitigación | CRÍTICO | Ley N° 19.628 |
| **SQL Injection** | Inyección SQL en parámetros | Lectura/modificación de BD completa | MITIGADO (JPA) | CONTROLADO | OWASP |
| **XSS** | Scripts en campos de texto | Robo de tokens JWT; phishing | SIN mitigación | ALTO | OWASP |
| **CSRF** | Solicitudes forjadas | Cambios no autorizados | MITIGADO (JWT) | BAJO | OWASP |
| **Credenciales Débiles** | Contraseñas sin validación | Fuerza bruta; compromiso de cuentas | SIN mitigación | ALTO | NIST |

**Código vulnerable actual:**
```java
@GetMapping("/api/usuarios")
public List<Usuario> listarUsuarios() {  // SIN @PreAuthorize
    return usuarioService.findAll();     // CUALQUIERA ve TODOS
}
```

---

---

## PASO 2: Definición de los Requerimientos de Seguridad

| ID | Requerimiento | Criticidad | Descripción |
|----|----------------|-----------|-------------|
| SEC-001 | Autenticación obligatoria | CRÍTICA | Validar identidad antes de acceso |
| SEC-002 | Control de acceso por rol | CRÍTICA | Autorización basada en ROLE_CLIENTE, ROLE_EMPLEADO, ROLE_ADMIN |
| SEC-003 | Cifrado de credenciales | CRÍTICA | BCrypt para contraseñas en BD |
| SEC-004 | Sesión con expiración | CRÍTICA | JWT con TTL; revocación de sesiones comprometidas |
| SEC-005 | Auditoría de accesos | ALTA | Registrar quién accedió a qué y cuándo |
| SEC-006 | Validación de entrada | ALTA | Prevenir XSS, SQL injection |
| SEC-007 | HTTPS/TLS | ALTA | Encriptación en tránsito |

**No Funcionales:** Escalabilidad (50 sucursales sin rediseño), disponibilidad (sin SPOF), rendimiento (<50ms por validación), interoperabilidad (integración LDAP futura).

**Cumplimiento Normativo (Ley N° 19.628 - Chile):**
- Confidencialidad: BCrypt + HTTPS
- Finalidad: RBAC con @PreAuthorize
- Minimización: JWT stateless con expiración automática

---

---

## PASO 3: Identificación de Usuarios y Roles

| Rol | Acceso Permitido | Restricciones |
|-----|------------------|----------------|
| **CLIENTE** (ROLE_CLIENTE) | Ver productos, comprar, ver compras propias, gestionar cuenta | No inventario, no datos ajenos, no reportes |
| **EMPLEADO** (ROLE_EMPLEADO) | Gestionar inventario local, procesar devoluciones, ver ventas sucursal | No precios globales, no reportes financieros, no modificar roles |
| **ADMIN** (ROLE_ADMIN) | Acceso total, crear/modificar/eliminar usuarios, reportes globales, configuración | Todos accesos auditados |

**Matriz de permisos:**

| Recurso | CLIENTE | EMPLEADO | ADMIN |
|---------|---------|----------|-------|
| Ver productos | ✓ | ✓ | ✓ |
| Comprar | ✓ | - | ✓ |
| Inventario | - | ✓ | ✓ |
| Ver usuarios | - | - | ✓ |
| Modificar roles | - | - | ✓ |

---

---

## PASO 4: Exploración de Estrategias de Autenticación

| Opción | Descripción | Ventajas | Desventajas | Viabilidad |
|--------|-------------|----------|-------------|-----------|
| **En Memoria** | `InMemoryUserDetailsManager` | Rápido, sin BD | Usuarios perdidos al reiniciar, no escalable | **NO** |
| **JDBC** | `JdbcUserDetailsManager` | Persistencia, escalable a miles | Stateful, problemas multi-sucursal, sincronización | **Parcial** |
| **LDAP** | `ldapAuthentication()` integrado | Gestión centralizada, empleados corporativos | Dependencia LDAP central, falla si cae, no para clientes | **Solo empleados** |
| **JWT** | Tokens stateless firmados | Stateless, escalable horizontalmente, multi-sucursal, móvil-friendly, sin carga BD | Revocación compleja, requiere blacklist | **ÓPTIMA** |

**Análisis comparativo detallado:**

| Criterio | En Memoria | JDBC | LDAP | JWT |
|----------|-----------|------|------|-----|
| Persistencia | No | Sí | Sí | Sí |
| Escalabilidad horizontal | No | Difícil | Difícil | Sí |
| Multi-sucursal | No | Requiere sync | LDAP central | Sí |
| Móvil | No | Cookies | Básico | Sí |
| Complejidad | Baja | Media | Alta | Media |
| Producción | No | Sí | Sí | Sí |

---

---

## PASO 5: Justificación de la Elección de JWT

**Razones principales por las que JWT es óptimo para MiniMarket:**

1. **Escalabilidad horizontal:** Crecimiento 10→50 sucursales sin sincronización de sesiones en servidor
2. **Multi-sucursal:** Cliente viaja entre sucursales, JWT válido en todas; tolerancia ante desconexiones
3. **Multi-plataforma:** Token en header HTTP funciona en web, móvil, apps nativas (vs cookies limitadas)
4. **Rendimiento:** Token self-contained evita búsqueda en BD por cada request (decodificación local <1ms)
5. **Estándar industria:** RFC 7519 oficial, usado por Google/Facebook/Amazon, librerías maduras (jjwt)
6. **Cumplimiento normativo:** Stateless + expiración automática minimiza riesgo de filtración (Ley N° 19.628)

**Implementación próximas fases:**
- Completar `JwtUtil.java` (generación, validación, expiración)
- Crear `JwtRequestFilter` para interceptar requests
- Implementar `POST /auth/login`
- Agregar `@PreAuthorize` en todos controladores
- Adicionar `jjwt` a pom.xml
