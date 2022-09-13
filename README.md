# Вступительное задание в Осеннюю Школу Бэкенд Разработки Яндекса 2022
## О проекте в этом репозитории
бэкенд для веб-сервиса хранения файлов, аналогичный сервису [Яндекс Диск](https://yandex.ru/disk)

[OpenAPI Спецификация](openapi.yaml)

[Задание](Task.md)
## Установка
### Docker
Перед запуском, вам нужно установить Docker на робчий компьютер.[Скачать установщик Docker Desktop](https://www.docker.com/get-started).
После установки, проверьте, что Docker работает, набрав `docker` в терминале.
После установки Docker
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

