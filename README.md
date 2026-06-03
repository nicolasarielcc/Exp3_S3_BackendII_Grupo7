# MiniMarket Plus — Seguridad y Pruebas

Este README documenta cómo ejecutar la aplicación localmente y validar (con Postman) la autenticación JWT, el hasheo de contraseñas, y el control de acceso por roles (`@PreAuthorize`).

## Requisitos

- Java 17
- Maven

## Ejecutar la aplicación

1. Desde la carpeta del proyecto:

```bash
mvn spring-boot:run
```

Si no tienes maven instalado

```bash
./mvnw spring-boot:run
```

2. La aplicación arrancará en `http://localhost:8080`.

## Inicializador de datos (seed)

Al iniciar la app se crea automáticamente (si no existe):

- Roles: `ADMIN`, `EMPLEADO`, `CLIENTE`
- Usuario administrador por defecto: `username=admin`, `password=admin123`

Esto facilita las pruebas iniciales. El usuario se crea con la contraseña hasheada (BCrypt).

## H2 Console (verificación de hashes)

1. Abrir `http://localhost:8080/h2-console`
2. JDBC URL: `jdbc:h2:mem:testdb`
3. User: `sa` — Password: (vacío)

En la tabla `USUARIO` podrás ver el campo `password` con el hash BCrypt.

## Endpoints relevantes

- `POST /auth/login` — Autenticación (devuelve JWT)
  - Body JSON: `{ "username": "admin", "password": "admin123" }`
- `GET /api/productos/public` — Catálogo público (sin auth)
- `GET /api/productos` — Listado de productos (EMPLEADO|ADMIN)
- `POST /api/productos` — Crear producto (EMPLEADO|ADMIN)
- `GET /api/usuarios` — Listar usuarios (ADMIN)
- `GET /api/inventario` — Listar inventario (EMPLEADO|ADMIN)
- `POST /api/carrito` — Acciones de carrito (CLIENTE|ADMIN)
- `GET /api/ventas` — Listar ventas (ADMIN)

> Nota: muchos endpoints están protegidos con `@PreAuthorize` en los controladores.

## Pruebas con Postman (paso a paso)

1. Login y obtener token

   - Request: `POST http://localhost:8080/auth/login`
   - Body:

   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```

   - La respuesta contiene el token JWT en `token`.

2. Probar endpoint público

   - Request: `GET http://localhost:8080/api/productos/public`
   - No requiere Authorization header — debe devolver 200 y listado.

3. Probar endpoint protegido (ej. listar usuarios)

   - Request: `GET http://localhost:8080/api/usuarios`
   - Header: `Authorization: Bearer <token>` (usar el token del login)
   - Con `admin` debe devolver 200; con un usuario sin rol ADMIN debe devolver 403.

4. Verificar hash de contraseña en H2

   - Abrir H2 Console (ver arriba) mientras la app corre.
   - Ejecutar: `SELECT id, username, password FROM USUARIO;` — verás el hash en `password`.

5. Validar expiración de token

   - Modifica temporalmente `jwt.expiration-ms` en `src/main/resources/application.properties` a `60000` (1 min), reinicia la app, genera token y espera >60s; las peticiones deben fallar con 401.

6. Validar `@PreAuthorize`

   - Crear/usar un usuario con rol `EMPLEADO` y probar crear producto (`POST /api/productos`) — si tiene rol EMPLEADO o ADMIN debe funcionar; si es CLIENTE o sin token, debe devolver 403.

## Ver token JWT

- Puedes pegar el token en https://jwt.io/ para inspeccionar `payload` y verificar `sub` y `roles`.

## Comandos útiles

Iniciar app:

```bash
mvn spring-boot:run
```

Ver logs:

```bash
mvn -q spring-boot:run
# o abrir target/classes/application.properties si necesitas cambiar tiempo de expiracion
```

## Notas finales

- El initializer crea un admin de pruebas; cambia la contraseña en producción.
- La clave JWT por defecto está en `application.properties`; reemplázala por una clave segura en deploy.

Si quieres, puedo añadir Postman Collection exportada con las peticiones preconfiguradas.
