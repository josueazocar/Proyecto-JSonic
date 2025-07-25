package com.JSonic.uneg;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music; // Importar Music para la música de fondo
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;

/**
 * Gestor de sonidos y música de fondo.
 * Permite registrar, cargar y reproducir efectos de sonido y música
 * usando un AssetManager de libGDX.
 */
public class SoundManager {

    private final AssetManager assetManager;
    private Music backgroundMusic; // Objeto Music para la música de fondo

    private float sfxVolume = 1.0f;
    private final HashMap<String, String> sfxMap = new HashMap<>();
    private String currentMusicPath; // Para recordar qué música está sonando
    private float musicVolume = 1.0f;

    /**
     * Crea un SoundManager con el AssetManager especificado.
     * @param assetManager gestor de recursos para cargar sonidos y música.
     */
    public SoundManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Registra un efecto de sonido para cargarlo posteriormente.
     * @param key   clave identificadora del sonido.
     * @param path  ruta al archivo de sonido.
     */
    public void registerSound(String key, String path) {
        if (!sfxMap.containsKey(key)) {
            sfxMap.put(key, path);
            assetManager.load(path, Sound.class);
            Gdx.app.log("SoundManager", "Sonido registrado [" + key + "] en la ruta: " + path);
        }
    }
    // Método para cargar la música
    /**
     * Encola la carga de un archivo de música de fondo.
     * @param filePath ruta al archivo de música.
     */
    public void loadMusic(String filePath) {
        if (!assetManager.isLoaded(filePath, Music.class)) {
            assetManager.load(filePath, Music.class);
            Gdx.app.log("SoundManager", "Cargando música: " + filePath);
        }
        this.currentMusicPath = filePath;
    }

    /**
     * Reproduce un efecto de sonido registrado.
     * @param key clave del sonido a reproducir.
     */
    public void play(String key) {
        String path = sfxMap.get(key);
        if (path != null && assetManager.isLoaded(path, Sound.class)) {
            Sound sound = assetManager.get(path, Sound.class);
            sound.play(sfxVolume);
        } else {
            Gdx.app.error("SoundManager", "No se pudo reproducir el sonido: '" + key + "'. ¿Está registrado y cargado?");
        }
    }


    // Método para obtener la música una vez cargada
    /**
     * Obtiene la instancia de Music si ya fue cargada.
     * @param filePath ruta al archivo de música.
     * @return la instancia de Music o null si no está cargada.
     */
    private Music getLoadedMusic(String filePath) {
        if (assetManager.isLoaded(filePath, Music.class)) {
            return assetManager.get(filePath, Music.class);
        }
        Gdx.app.log("SoundManager", "Advertencia: La música " + filePath + " no está cargada.");
        return null;
    }

    /**
     * Reproduce música de fondo, deteniendo la anterior si existe.
     * @param filePath ruta al archivo de música.
     * @param volume   nivel de volumen [0.0,1.0].
     * @param looping  true para reproducción en bucle.
     */
    public void playBackgroundMusic(String filePath, float volume, boolean looping) {
        // Detener la música actual si está sonando
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }

        // Asegurarse de que la música esté cargada
        if (!assetManager.isLoaded(filePath, Music.class)) {
            // Si no está cargada, la cargamos y esperamos (en un juego real, esto se haría en una pantalla de carga)
            loadMusic(filePath);
            assetManager.finishLoadingAsset(filePath); // Espera solo por este asset
        }

        backgroundMusic = getLoadedMusic(filePath);

        if (backgroundMusic != null) {
            backgroundMusic.setVolume(volume);
            backgroundMusic.setLooping(looping);
            backgroundMusic.play();
            Gdx.app.log("SoundManager", "Reproduciendo música: " + filePath);
        } else {
            Gdx.app.log("SoundManager", "Error: No se pudo reproducir la música " + filePath);
        }


    }

    /**
     * Detiene la música de fondo en reproducción.
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
            Gdx.app.log("SoundManager", "Música de fondo detenida.");
        }
    }

    /**
     * Pausa la música de fondo actualmente activa.
     */
    public void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
            Gdx.app.log("SoundManager", "Música de fondo pausada.");
        }
    }

    /**
     * Reanuda la música de fondo pausada.
     */
    public void resumeBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
            Gdx.app.log("SoundManager", "Música de fondo reanudada.");
        }
    }

    /**
     * Ajusta el volumen de la música de fondo.
     * @param volume nuevo volumen [0.0,1.0].
     */
    public void setBackgroundMusicVolume(float volume) {
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(volume);
        }
    }

    /**
     * Libera los recursos de música cargados.
     * Debe llamarse al cerrar la aplicación.
     */
    public void dispose() {
        if (currentMusicPath != null && assetManager.isLoaded(currentMusicPath, Music.class)) {
            assetManager.unload(currentMusicPath); // Descarga la música del AssetManager
            Gdx.app.log("SoundManager", "Música de fondo liberada del AssetManager.");
        }

    }

    /**
     * Registra el efecto de sonido de clic.
     * @param filePath ruta al archivo de sonido de clic.
     */
    public void loadClickSound(String filePath) {
        registerSound("click", filePath);
    }

    /**
     * Reproduce el efecto de sonido de clic.
     */
    public void playClickSound() {
        play("click");
    }


}
