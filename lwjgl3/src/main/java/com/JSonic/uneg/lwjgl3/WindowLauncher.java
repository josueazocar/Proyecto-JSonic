package com.JSonic.uneg.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.JSonic.uneg.Main;

/** Launches the desktop (LWJGL3) application. */
public class WindowLauncher {
    private static final int tile = 48;
    private static final int maxScreenCol = 16;//Numero de tiles horizontales en pantalla
    private static final int maxScreenRow = 12; //Numero de tiles verticales en la pantalla
    private static final int screenWidth = tile * maxScreenCol;//768 pixels
    private static final int screenHeight = tile * maxScreenRow;//576 pixels

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication(screenWidth, screenHeight);
    }

    private static Lwjgl3Application createApplication(int screenWidth, int screenHeight) {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration(screenWidth, screenHeight));
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration(int screenWidth,int screenHeight) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("J-SONIC");
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.


        configuration.setWindowedMode(1280, 720);

        //configuration.setWindowedMode(screenWidth, screenHeight);

        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("SonicIcon.png");
        return configuration;
    }
}
