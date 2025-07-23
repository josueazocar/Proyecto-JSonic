package com.JSonic.uneg;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music; // Importar Music para la música de fondo
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;

public class SoundManager {

    private final AssetManager assetManager;
    private Music backgroundMusic; // Objeto Music para la música de fondo

    private float sfxVolume = 1.0f;
    private final HashMap<String, String> sfxMap = new HashMap<>();
    private String currentMusicPath; // Para recordar qué música está sonando
    private Sound sonidoClick;
    private String clickSoundPath;

    // Constructor que recibe el AssetManager
    public SoundManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Pone en cola de carga cualquier efecto de sonido.
     * @param key La clave para llamarlo (ej: "explosion_bomba").
     * @param path La ruta al archivo.
     */
    public void registerSound(String key, String path) {
        if (!sfxMap.containsKey(key)) {
            sfxMap.put(key, path);
            assetManager.load(path, Sound.class);
            Gdx.app.log("SoundManager", "Sonido registrado [" + key + "] en la ruta: " + path);
        }
    }
    // Método para cargar la música
    // Se recomienda usar el AssetManager para esto
    public void loadMusic(String filePath) {
        if (!assetManager.isLoaded(filePath, Music.class)) {
            assetManager.load(filePath, Music.class);
            Gdx.app.log("SoundManager", "Cargando música: " + filePath);
        }
        this.currentMusicPath = filePath; // Guarda la ruta para luego obtenerla
    }

    /**
     * Reproduce cualquier sonido registrado usando su clave.
     * @param key La clave del sonido a reproducir.
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

    public void setSfxVolume(float volume) { this.sfxVolume = volume; }

    // Método para obtener la música una vez cargada
    private Music getLoadedMusic(String filePath) {
        if (assetManager.isLoaded(filePath, Music.class)) {
            return assetManager.get(filePath, Music.class);
        }
        Gdx.app.log("SoundManager", "Advertencia: La música " + filePath + " no está cargada.");
        return null;
    }

    // Método para reproducir la música de fondo
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

        // --- Controles de Audio ---

        public void stopBackgroundMusic() { if (backgroundMusic != null) backgroundMusic.stop(); }
        public void pauseBackgroundMusic() { if (backgroundMusic != null) backgroundMusic.pause(); }
        public void resumeBackgroundMusic() { if (backgroundMusic != null) backgroundMusic.play(); }
        public void setSfxVolume(float volume) { this.sfxVolume = Math.max(0, Math.min(1, volume)); }
        public void setMusicVolume(float volume) {
            this.musicVolume = Math.max(0, Math.min(1, volume));
            if (backgroundMusic != null) {
                backgroundMusic.setVolume(this.musicVolume);
            }
        }
    }



    // Método para detener la música
    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
            Gdx.app.log("SoundManager", "Música de fondo detenida.");
        }
    }

    // Método para pausar la música
    public void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
            Gdx.app.log("SoundManager", "Música de fondo pausada.");
        }
    }

    // Método para reanudar la música
    public void resumeBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
            Gdx.app.log("SoundManager", "Música de fondo reanudada.");
        }
    }

    // Método para ajustar el volumen de la música
    public void setBackgroundMusicVolume(float volume) {
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(volume);
        }
    }

    // Método para liberar los recursos de la música
    // IMPORTANTE: Esto debe llamarse al final de la aplicación
    public void dispose() {
        if (currentMusicPath != null && assetManager.isLoaded(currentMusicPath, Music.class)) {
            assetManager.unload(currentMusicPath); // Descarga la música del AssetManager
            Gdx.app.log("SoundManager", "Música de fondo liberada del AssetManager.");
        }
        // No llamamos backgroundMusic.dispose() directamente si está gestionado por AssetManager.
        // AssetManager.dispose() en Main se encargará de liberar todos los assets.
    }

    public void loadClickSound(String filePath) {
        registerSound("click", filePath);
    }

    public void playClickSound() {
        play("click");
    }


}
