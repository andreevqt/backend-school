# Вступительное задание в Осеннюю Школу Бэкенд Разработки Яндекса 2022

[![CI](https://github.com/andreevqt/backend-school/actions/workflows/ci.yml/badge.svg)](https://github.com/andreevqt/backend-school/actions/workflows/ci.yml)

## О проекте в этом репозитории
Мое старое тестовое в ШБР 2022 — rest api для сервиса хранения файлов.

Оргинальное [ТЗ](Task.md), [OpenAPI](openapi.yaml) спека. Для храненения каталога используется Closure table. Наверное, это не самое лучшее решение, но такой подход был реализован ради академических целей.

**Стек**: Spring Boot, hibernate/hibernate envers, mysql

[Задание](Task.md)
## Установка
### Docker
Перед запуском, вам нужно установить Docker.
Затем:
1. Переместитесь в директорию приложения
2. `docker-compose up` — соберет и запустит сервис по адресу `http://localhost:80`
3. или `docker-compose up -d` — соберет и запустит сервис в фоновом режиме`

### Локально
1. Убедитесь что в системе установлен [mysql](https://www.mysql.com/), и создана пустая бд(по умолчанию rest)
2. Переместитесь в директорию приложения
3. `./mvnw spring-boot:run -Drun.arguments=--spring.datasource.url=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>,--spring.datasource.username=<USERNAME>,--spring.datasource.password=<PASSWORD>,server.port=<PORT>` - по умолчанию `url=jdbc:mysql://localhost:3306/rest`, `user=root`, `password=root`, `port=80`

Скомпилировать JAR-файл можно командой: `./mvnw clean package` и для запуска `cd target && java -jar app.jar`, параметры можно передать через [applicaion.properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html) в директории с файлом или через параметры командной строки с префиксом -D, например, `java -jar -D--spring.datasource.password=root ./app.jar `.
### Тесты
В директории с приложением введите - `./mvnw test`
