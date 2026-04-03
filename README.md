# Assembly Vote API

## Descrição
`Assembly Vote API` é uma aplicação de votação desenvolvida para avaliação ténica pro SICRED.  
Ela utiliza:

- **Spring Boot 3**
- **Oracle Database**
- **Redis**
- **Spring Data JPA, Flyway**
- **SpringDoc / Swagger UI** para documentação da API - [SwaggerUI](http://localhost:8080/swagger-ui/index.html#/)
- **Feign Client** - para integração com serviço de validação de membros

---

## 1. Pré-requisitos

Antes de rodar a aplicação, é necessário ter instalado:

- Docker e Docker Compose
- Java 21 ou superior
- Maven 3.8 ou superior
- IDE (opcional, ex: IntelliJ, VSCode)

## 1.1 Variáveis de Ambiente

### Banco de Dados Oracle
- DATASOURCE_URL=jdbc:oracle:thin:@//localhost:1521/assemblyvote
- DATASOURCE_USER=appuser
- DATASOURCE_PASSWORD=passwd

### Redis
- REDIS_HOST=localhost
- REDIS_PASSWORD=
- REDIS_PORT=6379

### Sessão e Scheduler
- SESSION_DURATION=60
- SCHEDULER_VOTE=10000

### Feign Client
- VALIDATE_MEMBER_URI=https://validate-member-deploy-render.onrender.com

**OBS.:**
     O servidor onde a aplicação de validação de cpf foi hospedados infelizmente se trata de uma
hospedagem free, logo seus recursos são limitados e baixos. A primeira chamada na requisição pode demorar um pouco.
Claramente em um ambiente de Prod isso não aconteceria visto os recursos utilizados.

---

## 2. Subindo o ambiente de banco e cache

A aplicação depende de **Oracle Database** e **Redis**. Use o Docker Compose fornecido:

```bash
docker-compose up -d

```

## 3. Subindo a aplicação
```bash
mvn clean spring-boot:run "-Dspring-boot.run.profiles=local"

