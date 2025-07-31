<img width="1277" height="744" alt="Capture 2" src="https://github.com/user-attachments/assets/ae316b5a-cfb7-46ac-a175-a39ecb0a39b1" />
# J-SONIC

Este proyecto es un juego de fans desarrollado con fines educativos como parte de un curso universitario. No está afiliado, respaldado, ni es oficial de SEGA o Sonic Team. Todos los derechos de los personajes, nombres, sonidos y música originales de Sonic son propiedad de SEGA y/o Sonic Team. Este proyecto no tiene fines de lucro ni comerciales.

This project is a fan game developed for educational purposes as part of a college course. It is not affiliated with, endorsed by, or officially licensed by SEGA or Sonic Team. All rights to Sonic characters, names, sounds, and original music are owned by SEGA and/or Sonic Team. This project is non-profit and non-commercial.

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an empty `ApplicationListener` implementation.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.



