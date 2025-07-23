// Archivo: src/com/JSonic/uneg/PantallaSeleccionPersonaje.java
package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.JSonic.uneg.State.PlayerState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import network.Network;

public class PantallaSeleccionPersonaje extends PantallaBase {

    private final JSonicJuego juegoApp;
    private TextureAtlas characterAtlas;
    private final Boolean esAnfitrion;
    private int miID = -1;

    private Button sonicButton, tailsButton, knucklesButton;
    private Button seguirBoton;
    private ButtonGroup<Button> characterGroup;
    private PlayerState.CharacterType personajeSeleccionado;

    private Sound sonicSonido;
    private Sound tailsSonido;
    private Sound knucklesSonido;

    public PantallaSeleccionPersonaje(JSonicJuego juegoApp) {
        super("");
        this.juegoApp = juegoApp;
        this.esAnfitrion = null; // No aplica en modo un jugador
        inicializar();
    }

    public PantallaSeleccionPersonaje(JSonicJuego juegoApp, boolean esAnfitrion) {
        super("");
        this.juegoApp = juegoApp;
        this.esAnfitrion = esAnfitrion;
        juegoApp.conectarAlServidor();
        inicializar();
    }

    @Override
    public void inicializar() {
        mainStage.addActor(new Image(new Texture("Fondos/Portada_desenfoque.png")));
        characterAtlas = new TextureAtlas(Gdx.files.internal("Atlas/seleccionPersonajes.atlas"));

        sonicSonido = Gdx.audio.newSound(Gdx.files.internal("SoundsBackground/SONIC_VOZ.wav"));
        tailsSonido = Gdx.audio.newSound(Gdx.files.internal("SoundsBackground/TAILS_VOZ.wav"));
        knucklesSonido = Gdx.audio.newSound(Gdx.files.internal("SoundsBackground/KNUCKLES_VOZ.wav"));

        Table tabla = new Table();
        tabla.setFillParent(true);
        mainStage.addActor(tabla);

        tabla.add(new Image(new Texture(Gdx.files.internal("Fondos/Titulo_seleccion_personaje.png")))).size(396,110).colspan(3).padBottom(10).row();

        sonicButton = crearBotonPersonaje("sonic_seleccion", "sonic_seleccion_oscuro",  "sonic_seleccionado", "sonic_disabled");
        tailsButton = crearBotonPersonaje("tails_seleccion", "tails_seleccion_oscuro",  "tails_seleccionado", "tails_disabled");
        knucklesButton = crearBotonPersonaje("knuckles_seleccion", "knuckles_seleccion_oscuro", "knuckles_seleccionado", "knuckles_disabled");

        characterGroup = new ButtonGroup<>(sonicButton, tailsButton, knucklesButton);
        characterGroup.setMaxCheckCount(1);
        characterGroup.setMinCheckCount(1);

        sonicButton.addListener(new ClickListener() {@Override public void clicked(InputEvent e, float x, float y) {
            if (!sonicButton.isDisabled()){
                personajeSeleccionado = PlayerState.CharacterType.SONIC;
                sonicSonido.play(0.8f);
            }
        }});
        tailsButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) {
            if (!tailsButton.isDisabled()) {
                personajeSeleccionado = PlayerState.CharacterType.TAILS;
                tailsSonido.play(0.8f);
            }
        }});
        knucklesButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) {
            if (!knucklesButton.isDisabled()) {
                personajeSeleccionado = PlayerState.CharacterType.KNUCKLES;
                knucklesSonido.play(0.8f);
            }
        }});


        Button.ButtonStyle seguirBtonEstilo = new Button.ButtonStyle();
        seguirBtonEstilo.up = new TextureRegionDrawable(characterAtlas.findRegion("boton_seguir"));
        seguirBtonEstilo.down = new TextureRegionDrawable(characterAtlas.findRegion("boton_seguir_down"));
        seguirBtonEstilo.over = new TextureRegionDrawable(characterAtlas.findRegion("boton_seguir_hover"));
        seguirBoton = new Button(seguirBtonEstilo);

        seguirBoton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (personajeSeleccionado != null && !seguirBoton.isDisabled()) {
                    PantallaDeJuego.miPersonaje = personajeSeleccionado; // Guardamos la elección

                    if (esAnfitrion == null) {
                        // --- FLUJO UN JUGADOR ---
                        // No es multijugador, va a la selección de nivel normal.
                        System.out.println("Flujo Un Jugador: -> PantallaSeleccionNivel");
                        juegoApp.setPantallaActiva(new PantallaSeleccionNivel(juegoApp));

                    } else {
                        // --- FLUJO MULTIJUGADOR ---
                        if (esAnfitrion) {
                            // Si es el ANFITRIÓN, el siguiente paso es ELEGIR EL NIVEL.
                            Network.SolicitudAccesoPaquete solicitud = new Network.SolicitudAccesoPaquete();
                            solicitud.nombreJugador = "jugador"; // Asumimos que guardaste el nombre aquí
                            solicitud.characterType = personajeSeleccionado;
                            juegoApp.getGameClient().send(solicitud);

                            System.out.println("Flujo Anfitrión: -> PantallaSeleccionNivel");
                            juegoApp.setPantallaActiva(new PantallaSeleccionNivel(juegoApp, true)); // true para modo multijugador

                        } else {
                            Network.SolicitudAccesoPaquete solicitud = new Network.SolicitudAccesoPaquete();
                            solicitud.nombreJugador = "jugador"; // Asumimos que guardaste el nombre aquí
                            solicitud.characterType = personajeSeleccionado;
                            juegoApp.getGameClient().send(solicitud);
                            // Si es un CLIENTE, el siguiente paso es ir al LOBBY a esperar.
                            System.out.println("Flujo Cliente: -> PantallaLobby");
                            juegoApp.setPantallaActiva(new PantallaLobby(juegoApp, false)); // false porque no es anfitrión
                        }
                    }
                }
            }
        });

        tabla.add(sonicButton).size(200, 355).pad(20);
        tabla.add(tailsButton).size(200, 355).pad(20);
        tabla.add(knucklesButton).size(200, 355).pad(20).row();
        tabla.add(seguirBoton).colspan(3).padTop(40).width(250).height(80);


        if (juegoApp.getGameClient() != null)  {
            sonicButton.setDisabled(true);
            tailsButton.setDisabled(true);
            knucklesButton.setDisabled(true);
            seguirBoton.setDisabled(true);
            characterGroup.uncheckAll();
            personajeSeleccionado = null;
        }

        if (juegoApp.getGameClient() == null) {
        actualizarEstadoBotones();
        }
    }

    private void actualizarEstadoBotones() {
        System.out.println("Actualizando estado de botones. Personajes ocupados: " + JSonicJuego.personajesYaSeleccionados);

        sonicButton.setDisabled(JSonicJuego.personajesYaSeleccionados.contains(PlayerState.CharacterType.SONIC));
        tailsButton.setDisabled(JSonicJuego.personajesYaSeleccionados.contains(PlayerState.CharacterType.TAILS));
        knucklesButton.setDisabled(JSonicJuego.personajesYaSeleccionados.contains(PlayerState.CharacterType.KNUCKLES));

        characterGroup.uncheckAll();
        personajeSeleccionado = null;

        if (!sonicButton.isDisabled()) {
            sonicButton.setChecked(true);
            personajeSeleccionado = PlayerState.CharacterType.SONIC;
        } else if (!tailsButton.isDisabled()) {
            tailsButton.setChecked(true);
            personajeSeleccionado = PlayerState.CharacterType.TAILS;
        } else if (!knucklesButton.isDisabled()) {
            knucklesButton.setChecked(true);
            personajeSeleccionado = PlayerState.CharacterType.KNUCKLES;
        }

        seguirBoton.setDisabled(personajeSeleccionado == null);
    }

    @Override
    public void actualizar(float delta) {
        mainStage.act(delta);

        // Si no estamos en modo multijugador o no tenemos cliente de red, no hacemos nada.
        if (esAnfitrion == null || juegoApp.getGameClient() == null) {
            return;
        }

        // Procesamos la cola de paquetes del servidor.
        java.util.Queue<Object> paquetes = juegoApp.getGameClient().getPaquetesRecibidos();
        while (!paquetes.isEmpty()) {
            Object paquete = paquetes.poll();

            // Si el servidor nos envía nuestro ID...
            if (paquete instanceof Network.PaqueteTuID) {
                Network.PaqueteTuID p = (Network.PaqueteTuID) paquete;
                System.out.println("DEBUG: Recibido mi ID del servidor: " + p.id);
                this.miID = p.id;
                // Llamamos al método que configura la UI según nuestro ID.
                configurarBotonesPorID();
            }
        }
    }

    private void configurarBotonesPorID() {
        // Si todavía no sabemos nuestro ID, no hacemos nada.
        if (miID == -1) return;

        // Deshabilitamos todos los botones primero.
        sonicButton.setDisabled(true);
        tailsButton.setDisabled(true);
        knucklesButton.setDisabled(true);

        // Limpiamos la selección visual y lógica.
        characterGroup.uncheckAll();
        personajeSeleccionado = null;

        // Habilitamos y seleccionamos el que nos corresponde según nuestro ID.
        if (miID == 1) {
            sonicButton.setDisabled(false);
            sonicButton.setChecked(true); // Lo marcamos como seleccionado visualmente.
            personajeSeleccionado = PlayerState.CharacterType.SONIC;
        } else if (miID == 2) {
            tailsButton.setDisabled(false);
            tailsButton.setChecked(true);
            personajeSeleccionado = PlayerState.CharacterType.TAILS;
        } else if (miID == 3) {
            knucklesButton.setDisabled(false);
            knucklesButton.setChecked(true);
            personajeSeleccionado = PlayerState.CharacterType.KNUCKLES;
        } else {
            // Para más de 3 jugadores, se podría asignar un personaje por defecto o un ciclo.
            // Por ahora, si el ID es otro, no se podrá seleccionar nada.
        }

        System.out.println("DEBUG: Personaje asignado por servidor: " + personajeSeleccionado);
        // El botón "Seguir" solo se activa si se nos ha asignado un personaje válido.
        seguirBoton.setDisabled(personajeSeleccionado == null);
    }
    private Button crearBotonPersonaje(String up, String down, String checked, String disabled) {
        Button.ButtonStyle estilo = new Button.ButtonStyle();
        estilo.up = new TextureRegionDrawable(characterAtlas.findRegion(up));
        estilo.down = new TextureRegionDrawable(characterAtlas.findRegion(down));
        estilo.checked = new TextureRegionDrawable(characterAtlas.findRegion(checked));
        estilo.disabled = new TextureRegionDrawable(characterAtlas.findRegion(disabled));
        return new Button(estilo);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        actualizar(delta);

        mainStage.draw();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(mainStage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        mainStage.dispose();
        characterAtlas.dispose();
        if (sonicSonido != null) sonicSonido.dispose();
        if (tailsSonido != null) tailsSonido.dispose();
        if (knucklesSonido != null) knucklesSonido.dispose();
    }
}
