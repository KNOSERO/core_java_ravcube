# Logger

## Wyjaśnienie

Biblioteka udostępnia jedną abstrakcję logowania, dzięki czemu moduły infrastrukturalne
nie zależą bezpośrednio od konkretnej biblioteki aplikacji.

Aplikacja korzysta z:

~~~text
moduł aplikacji
    -> LoggerFactory / Logger
    -> logger:core
    -> Spring Commons Logging
    -> backend logowania aplikacji
~~~

~~~mermaid
graph LR
    Consumer[Moduł aplikacji] --> LoggerApi[logger:api]
    Stream[stream] --> LoggerApi
    Event[event] --> LoggerApi
    LoggerCore[logger:core] --> LoggerApi
    LoggerCore --> Spring[Spring logging facade]
    Spring --> Runtime[Logger runtime aplikacji]
~~~

## Instalacja

Dla własnej biblioteki:

~~~kotlin
dependencies {
    implementation(project(":lib:logger:api"))
}
~~~

W aplikacji Spring z gotową implementacją:

~~~kotlin
dependencies {
    implementation(project(":lib:logger:api"))
    implementation(project(":lib:logger:core"))
}
~~~

Stream i Event dostarczają logger jako część własnej konfiguracji, więc aplikacja
korzystająca z tych modułów zwykle nie musi dodawać go ręcznie.

## Pierwsze użycie

~~~java
public final class ExampleComponent {
    private final Logger logger;

    public ExampleComponent(LoggerFactory loggerFactory) {
        this.logger = loggerFactory.getLogger(ExampleComponent.class);
    }

    public void run(String resourceName) {
        logger.info("Starting resource {}", resourceName);
    }
}
~~~

Logujemy przebieg, identyfikatory techniczne i błędy. Nie logujemy payloadów
biznesowych, tokenów, nagłówków autoryzacyjnych ani całych obiektów eventów.

## Własna implementacja

Aplikacja może dostarczyć własny bean LoggerFactory. Implementacja zachowuje
poziomy debug, info, warn i error. Domyślna implementacja używa Spring Commons
Logging, więc wybór backendu pozostaje po stronie aplikacji.

Logger i LoggerFactory z logger:api nie zawierają zależności od Springa.
