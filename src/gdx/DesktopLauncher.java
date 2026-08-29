package gdx;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/** نقطه‌ی شروع نسخه‌ی گرافیکی بازی (فاز دو). Main.java کنسولی فاز یک دست‌نخورده باقی مانده است. */
public final class DesktopLauncher {

    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Plants vs Zombies 2 - Phase 2");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        new Lwjgl3Application(new PvZGame(), config);
    }
}
