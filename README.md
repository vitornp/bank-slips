# Geração de boletos

## Pré-requisitos
* JDK 8+
* Maven

## Como rodar o projeto local

Rodar o comando:
```bash
mvn spring-boot:run
```

Após rodar o comando acessar [API de health](http://localhost:9999/actuator/health) e verificar o retorno

```json
{
    "status": "UP"
}
```

## Como manipular boletos

Basta acessar o [Swagger](http://localhost:9009/rest/swagger-ui.html) que é possível fazer as simulações necessárias das APIs
