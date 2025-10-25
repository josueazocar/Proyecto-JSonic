<div align="center">
  <img src="https://github.com/user-attachments/assets/5c599c86-534b-4613-9ea2-1c4abb7047c3" width="700" alt="Gameplay Screenshot 1">
  <h1>J-Sonic</h1>
  <p>Juego fanmade multijugador (local y online) con temática ecológica, desarrollado con fines educativos usando Java y libGDX.</p>

  <p>
    <img src="https://img.shields.io/badge/status-terminado-green" alt="Estado del Proyecto">
    <img src="https://img.shields.io/badge/licencia-sin%20licencia-lightgrey" alt="Licencia">
    <img src="https://img.shields.io/badge/plataforma-desktop-blue" alt="Plataforma">
  </p>
</div>

<p align="center">
  <a href="#-acerca-del-proyecto">Acerca del Proyecto</a> •
  <a href="#-características-principales">Características</a> •
  <a href="#-stack-tecnológico">Stack Tecnológico</a> •
  <a href="#-primeros-pasos">Primeros Pasos</a> •
  <a href="#-desarrollo-y-build">Desarrollo</a> •
  <a href="#-arquitectura-y-detalles-técnicos">Arquitectura</a> •
  <a href="#-aviso-legal">Aviso Legal</a>
</p>

---

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/f0852927-e18b-4628-a0a1-d17cdefa097d" width="400" alt="Gameplay Screenshot 2"/>
  <img src="https://github.com/user-attachments/assets/ae2db516-b573-451d-a88a-03d01227aaa2" width="400" alt="Gameplay Screenshot 3"/>
</p>

---

## 🎯 Acerca del Proyecto

**J-Sonic** es un juego multijugador (local y online) inspirado en la saga "Sonic the Hedgehog", reinventado con una misión ecológica. El proyecto nació como un desafío educativo para construir una experiencia de juego completa, abordando desde la sincronización de estados en red hasta la arquitectura del motor de juego.

El objetivo es ofrecer una jugabilidad de plataformas clásica y veloz, al mismo tiempo que se transmite un mensaje sobre la conciencia ambiental.

---

## ✨ Características Principales

-   **Motor Multijugador**:
    -   **Local**: Modo de red local para 3 jugadores.
    -   **Online**: Conexión cliente-servidor para jugar a través de internet, utilizando **KryoNet** para una comunicación de red eficiente.
-   **Jugabilidad Clásica**: Plataformas de alta velocidad, loops y físicas inspiradas en los juegos originales.
-   **Misión Ecológica**: Los jugadores suman puntos recogiendo basura y plantando árboles, restaurando el entorno de cada nivel.
-   **Multiplataforma**: Construido sobre el framework **libGDX**, garantizando su funcionamiento en escritorios (Windows, macOS, Linux).

---

## 🛠️ Stack Tecnológico

Este proyecto integra diversas tecnologías para lograr una base sólida y extensible.

-   **Lenguaje Principal**: **Java 21+**
-   **Framework de Juego**: **libGDX**, un motor de juegos multiplataforma de bajo nivel.
-   **Red**: **KryoNet** para la comunicación TCP/UDP de alto rendimiento en el modo online.
-   **Gestión de Dependencias y Build**: **Gradle**, utilizando el wrapper (`gradlew`) para garantizar builds reproducibles en cualquier entorno.
-   **Gráficos**: **LWJGL3** como backend para el renderizado con **OpenGL 2.0+** en escritorio.

---

## 🚀 Primeros Pasos

Sigue estos pasos para ejecutar el proyecto en tu máquina local.

### Requisitos Previos

-   **SO**: Windows 10/11 (principalmente probado en este SO).
-   **JDK**: Versión 21 o superior, con la variable de entorno `JAVA_HOME` configurada.
-   **GPU**: Soporte para OpenGL 2.0 o superior.
-   **IDE (Opcional)**: IntelliJ IDEA 2024.2.1 o compatible.

### Instalación y Ejecución

1.  **Clona el repositorio:**
    ```sh
    git clone https://github.com/josueazocar/Proyecto-JSonic.git
    cd Proyecto-JSonic
    ```

2.  **Ejecuta el juego:**
    -   **Desde la terminal (recomendado):**
        El wrapper de Gradle (`gradlew`) se encarga de todo.
        ```sh
        # En Windows
        .\gradlew.bat lwjgl3:run
        
        # En macOS/Linux
        ./gradlew lwjgl3:run
        ```
    -   **Desde IntelliJ IDEA:**
        1.  Abre la carpeta del proyecto en IntelliJ.
        2.  Asegúrate de que el SDK del proyecto esté configurado en un JDK 11+.
        3.  Ve a la pestaña de Gradle en el panel derecho y ejecuta la tarea: `Proyecto-JSonic > lwjgl3 > Tasks > application > run`.

---

## 🔧 Desarrollo y Build

### Construir el Proyecto

-   **Compilar todos los módulos:**
    ```sh
    .\gradlew.bat build
    ```
-   **Generar un JAR ejecutable:**
    El archivo `.jar` se creará en `lwjgl3/build/libs/`.
    ```sh
    .\gradlew.bat lwjgl3:jar
    ```
    Para ejecutarlo: `java -jar lwjgl3/build/libs/<nombre-del-archivo>.jar`

### Pruebas (Testing)

-   **Ejecutar todos los tests unitarios:**
    ```sh
    .\gradlew.bat test
    ```

### Tareas de Gradle Útiles

-   `clean`: Limpia todos los directorios `build`.
-   `--refresh-dependencies`: Fuerza la re-descarga y validación de todas las dependencias.
-   `--offline`: Usa las dependencias cacheadas localmente para acelerar el build.

### Depuración (Debugging)

-   **En IntelliJ IDEA**: Crea una configuración de tipo "Application", selecciona el módulo `lwjgl3`, y establece `Lwjgl3Launcher` como la clase principal. Luego, puedes usar el modo *Debug* de IntelliJ.
-   **Logs**: Utiliza `Gdx.app.log("TAG", "Mensaje")` para añadir trazas en el código.

---

## 🏗️ Arquitectura y Detalles Técnicos

-   **Estructura Modular**:
    -   `core`: Contiene toda la lógica del juego (entidades, pantallas, sistemas, assets) y es independiente de la plataforma.
    -   `lwjgl3`: Es el punto de entrada para la versión de escritorio. Se encarga de crear la ventana y configurar el backend de LWJGL3.
-   **Ciclo de Vida del Juego**: Se gestiona a través de la interfaz `ApplicationListener` de libGDX, con una clase principal que delega el renderizado a la pantalla activa.
-   **Gestión de Recursos**: Los assets (imágenes, sonidos, etc.) se gestionan a través del `AssetManager` de libGDX para permitir carga y descarga asíncrona.
-   **Solución de Problemas Comunes**:
    -   **Errores de JDK**: Verifica que `JAVA_HOME` apunte a la versión correcta con `java -version`.
    -   **Dependencias corruptas**: Ejecuta `.\gradlew.bat clean --refresh-dependencies`.
    -   **Fallos gráficos**: Asegúrate de que los drivers de tu GPU estén actualizados y que soporta OpenGL 2.0+.

---

## 🎨 Arquitectura Visual (Diagramas UML)

A continuación se presentan algunos diagramas que ilustran el diseño de los componentes clave del proyecto.

### Diagrama de Clases Principal
*Este diagrama muestra las relaciones entre las clases principales del juego*

![UML JSONIC](https://github.com/user-attachments/assets/ee457fde-871c-4c3d-9d22-9bd0ba3dd172)

<br>

### Diagrama de Secuencia y Colaboración

<p align="center">
<img width="850" height="500" alt="image" src="https://github.com/user-attachments/assets/45d7a486-ba7f-48ba-8c9c-c26274e4e7d7" />
<img width="850" height="500" alt="image" src="https://github.com/user-attachments/assets/d63b5141-fb5a-4211-ad55-f0f92d9a26ff" />
<img width="850" height="500" alt="image" src="https://github.com/user-attachments/assets/56a5830c-aa5c-46b3-b23e-e95564824751" />
</p>


## 🤝 Contribuir

Las contribuciones son bienvenidas. Si deseas colaborar:

1.  Abre un **Issue** para discutir el cambio que propones.
2.  Crea una nueva rama para tu feature (`git checkout -b feature/AmazingFeature`).
3.  Haz commit de tus cambios (`git commit -m 'Add some AmazingFeature'`).
4.  Asegúrate de que el proyecto compila (`build`) y los tests pasan (`test`).
5.  Abre un **Pull Request**.

---

## ⚖️ Aviso Legal / Legal Notice

Este proyecto es un juego de fans desarrollado con fines educativos como parte de un curso universitario. No está afiliado, respaldado, ni es oficial de SEGA o Sonic Team. Todos los derechos de los personajes, nombres, sonidos y música originales de Sonic son propiedad de SEGA y/o Sonic Team. Este proyecto no tiene fines de lucro ni comerciales.

*This project is a fan game developed for educational purposes as part of a college course. It is not affiliated with, endorsed by, or officially licensed by SEGA or Sonic Team. All rights to Sonic characters, names, sounds, and original music are owned by SEGA and/or Sonic Team. This project is non-profit and non-commercial.*
