# Kanban API

## Descripción
API REST para autenticación y gestión de tableros y tareas tipo Kanban. Expone endpoints bajo `/api/v1`: registro, inicio y renovación de sesión; CRUD de tableros; creación, consulta, actualización, cambio de estado y borrado lógico de tareas; y administración de usuarios para el rol `ADMIN`.

Las solicitudes autenticadas usan JWT. Los endpoints de tableros y tareas limitan los datos al propietario para usuarios `USER`; los administradores disponen de acceso global. Las operaciones de persistencia se ejecutan mediante procedimientos almacenados MySQL. Las listas de tareas y usuarios aceptan paginación (`page`, `size`, máximo 50). Las tareas permiten filtrar por tablero, estado, prioridad, asignado, texto y fechas; el ordenamiento está restringido a columnas permitidas por el procedimiento.

### Flujo de la API
1. `POST /api/v1/auth/register` valida los datos, cifra la contraseña con BCrypt y crea un usuario `USER` mediante `sp_user_create`.
2. `POST /api/v1/auth/login` autentica el usuario y contraseña contra MySQL y devuelve un token de acceso (15 minutos) y uno de renovación (7 días).
3. El cliente envía el token de acceso en `Authorization: Bearer <token>`. Un filtro JWT verifica la firma y vigencia, carga la identidad y el rol, y Spring Security protege los endpoints.
4. Los controladores validan DTOs y llaman procedimientos almacenados para leer o modificar datos. La capa SQL aplica la propiedad, los filtros, el soft delete y el orden seguro.
5. `POST /api/v1/auth/refresh` acepta un refresh token vigente y emite un nuevo par de tokens. Los errores de validación y acceso usan respuestas JSON.

## Stack
- Java 17 y Spring Boot 3.3
- Maven, Spring Web, Spring Security 6 y Jakarta Validation
- JJWT 0.12 para tokens JWT y BCrypt para contraseñas
- Spring JDBC (`JdbcTemplate`) y procedimientos almacenados; no usa JPA/Hibernate
- MySQL 8, Lombok y springdoc OpenAPI/Swagger UI

## Prerrequisitos
- JDK 17 y Maven 3.6 o posterior
- MySQL 8
- Git Bash, WSL o un cliente `mysql` para cargar el script SQL

Variables configurables (valores predeterminados):

| Variable | Predeterminado | Uso |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/kanban_db?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC` | URL MySQL |
| `DB_USERNAME` | `root` | Usuario MySQL |
| `DB_PASSWORD` | vacío | Contraseña MySQL |
| `PORT` | `8081` | Puerto HTTP |
| `JWT_SECRET` | valor de desarrollo incluido en `application.yml` | Secreto HS256; configurar uno propio de al menos 32 bytes en otros entornos |
| `JWT_ACCESS_MS` | `900000` | Vigencia del token de acceso en milisegundos |
| `JWT_REFRESH_MS` | `604800000` | Vigencia del refresh token en milisegundos |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Orígenes CORS separados por comas |

## Ejecución
1. Crea las tablas, índices, procedimientos y usuarios demo:
   ```sh
   mysql -u root -p < db/schema.sql
   ```
   En PowerShell: `Get-Content db/schema.sql | mysql -u root -p`.
2. Configura `DB_USERNAME` y `DB_PASSWORD` para tu instalación de MySQL. Cambia `JWT_SECRET` por un secreto propio de al menos 32 bytes.
3. Desde la raíz del proyecto, inicia la API:
   ```sh
   mvn spring-boot:run
   ```
   Ejecuta la suite con `mvn test`.
4. Abre Swagger UI en [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html). La especificación OpenAPI está en `/v3/api-docs`.

El seed incluye `admin` y `demo`; ambos usan `Admin123!`.

### Ejemplo de uso
```sh
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin123!"}'

curl 'http://localhost:8081/api/v1/tasks?status=TODO&priority=HIGH&page=0&size=10' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```
