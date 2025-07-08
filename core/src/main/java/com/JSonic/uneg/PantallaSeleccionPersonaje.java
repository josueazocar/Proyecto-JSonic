// Archivo: src/com/JSonic/uneg/PantallaSeleccionPersonaje.java
package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class PantallaSeleccionPersonaje extends PantallaBase {

    private final JSonicJuego juegoApp;
    private TextureAtlas characterAtlas;
    private final Boolean esAnfitrion;

    private Button sonicButton, tailsButton, knucklesButton;
    private Button playButton;
    private ButtonGroup<Button> characterGroup;
    private PlayerState.CharacterType personajeSeleccionado;

    public PantallaSeleccionPersonaje(JSonicJuego juegoApp) {
        super();
        this.juegoApp = juegoApp;
        this.esAnfitrion = null; // No aplica en modo un jugador
    }

    public PantallaSeleccionPersonaje(JSonicJuego juegoApp, boolean esAnfitrion) {
        super();
        this.juegoApp = juegoApp;
        this.esAnfitrion = esAnfitrion;
    }

    @Override
    public void inicializar() {
        mainStage.addActor(new Image(new Texture("Fondos/Portada_desenfoque.png")));
        characterAtlas = new TextureAtlas(Gdx.files.internal("Atlas/seleccionPersonajes.atlas"));

        Table tabla = new Table();
        tabla.setFillParent(true);
        mainStage.addActor(tabla);

        tabla.add(new Image(new Texture(Gdx.files.internal("Fondos/Titulo_seleccion_personaje.png")))).size(396,110).colspan(3).padBottom(10).row();


        sonicButton = crearBotonPersonaje("sonic_seleccion", "sonic_seleccion_oscuro",  "sonic_seleccionado", "sonic_disabled");
        tailsButton = crearBotonPersonaje("tails_seleccion", "tails_seleccion_oscuro",  "tails_seleccionado", "tails_disabled");
        knucklesButton = crearBotonPersonaje("knuckles_seleccion", "knuckles_seleccion_oscuro", "knuckles_seleccionado", "knuckles_disabled");

        // ... (código para configurar botones y listeners es el mismo de antes)
        characterGroup = new ButtonGroup<>(sonicButton, tailsButton, knucklesButton);
        characterGroup.setMaxCheckCount(1);
        characterGroup.setMinCheckCount(1);

        sonicButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (!sonicButton.isDisabled()) personajeSeleccionado = PlayerState.CharacterType.SONIC; }});
        tailsButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (!tailsButton.isDisabled()) personajeSeleccionado = PlayerState.CharacterType.TAILS; }});
        knucklesButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (!knucklesButton.isDisabled()) personajeSeleccionado = PlayerState.CharacterType.KNUCKLES; }});

        
        Button.ButtonStyle playStyle = new Button.ButtonStyle();
        playStyle.up = new TextureRegionDrawable(characterAtlas.findRegion("boton_jugar"));
        playStyle.down = new TextureRegionDrawable(characterAtlas.findRegion("boton_jugar_down"));
        playStyle.over = new TextureRegionDrawable(characterAtlas.findRegion("boton_jugar_hover"));
        playButton = new Button(playStyle);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (personajeSeleccionado != null && !playButton.isDisabled()) {
                    PantallaDeJuego.miPersonaje = personajeSeleccionado; // Guardamos la elección

                    if (esAnfitrion != null) { // Si esAnfitrion no es nulo, es el flujo multijugador
                        // Pasamos el valor 'esAnfitrion' que guardamos.
                        juegoApp.setPantallaActiva(new PantallaLobby(juegoApp, esAnfitrion));
                    } else {
                        // Flujo de un jugador
                        juegoApp.iniciarJuegoLocal();
                    }


                }
            }
        });

        tabla.add(sonicButton).size(200, 355).pad(20);
        tabla.add(tailsButton).size(200, 355).pad(20);
        tabla.add(knucklesButton).size(200, 355).pad(20).row();
        tabla.add(playButton).colspan(3).padTop(40).width(250).height(80);

        // La lógica de habilitar/deshabilitar botones aplica a ambos modos,
        // pero la lista de `personajesYaSeleccionados` solo se llenará en online.
        actualizarEstadoBotones();
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

        playButton.setDisabled(personajeSeleccionado == null);
    }

    @Override
    public void actualizar(float delta) {
        mainStage.act(delta);
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
    }
}
