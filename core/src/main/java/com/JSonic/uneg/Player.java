package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Player extends Entity{
//Attributes


//Methods

    Player(){
        super();//Llamo al constructor de la clase super
        setDefaultValues();//Valores lógicos por defecto
    }

//Setters

//Getters

//Methods
    protected void setDefaultValues(){//Metodo para dar valores por defecto
        setPositionX(100);//Inicialización de posición inicial con respecto a X
        setPositionY(100);//Inicialización de posición inicial con respecto a Y
        setSpeed(4);//Establecimiento de la velocidad
    }


    @Override
    public void KeyHandler() {
        boolean keyPressed = false; // Bandera para saber si alguna tecla de movimiento fue presionada

        // Manejo de movimiento
        if (Gdx.input.isKeyPressed(Keys.W)) { // Arriba
            setPositionY(getSpeed());
            setEstadoActual(EstadoPlayer.UP);
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Keys.S)) { // Abajo
            setPositionY(-getSpeed());
            setEstadoActual(EstadoPlayer.DOWN);
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Keys.A)) { // Izquierda
            setPositionX(-getSpeed());
            setEstadoActual(EstadoPlayer.LEFT);
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) { // Derecha
            setPositionX(getSpeed());
            setEstadoActual(EstadoPlayer.RIGHT);
            keyPressed = true;
        }

        // Manejo del golpe (puñetazo)
        // Usamos isKeyJustPressed para detectar la pulsación de la tecla solo una vez.
        if (Gdx.input.isKeyJustPressed(Keys.J)) { // Tecla para golpe (ej. 'J')
            setEstadoActual(EstadoPlayer.HIT); // Asignación de estado actual a HIT
            tiempoXFrame = 0; // Reiniciamos el tiempo para que la animación de golpe comience desde el principio
            keyPressed = true; // Establecemos la bandera
        }

        // Manejo del golpe (puñetazo)
        // Usamos isKeyJustPressed para detectar la pulsación de la tecla solo una vez.
        if (Gdx.input.isKeyJustPressed(Keys.K)) { // Tecla para golpe (ej. 'J')
            setEstadoActual(EstadoPlayer.HIT); // Asignación de estado actual a HIT
            tiempoXFrame = 0; // Reiniciamos el tiempo para que la animación de golpe comience desde el principio
            keyPressed = true; // Establecemos la bandera
        }


        // Si ninguna tecla de movimiento o golpe fue presionada, volvemos a IDLE
        // Solo si el estado actual no es HIT (para no interrumpir la animación de golpe)
        if (!keyPressed && getEstadoActual() != EstadoPlayer.HIT) {
            setEstadoActual(EstadoPlayer.IDLE);
        }
    }

    protected abstract void CargarSprites();//Segun el personaje seleccionado se define este método

    public abstract void update(float deltaTime);

    public abstract void draw(SpriteBatch batch);

}
