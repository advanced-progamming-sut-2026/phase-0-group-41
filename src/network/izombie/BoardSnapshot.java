package network.izombie;

import model.game.Board;
import model.game.Tile;
import model.minigame.IZombieSession;
import model.plant.Plant;
import model.zombie.Zombie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * عکس فوری (Snapshot) از وضعیت یک مسابقه‌ی «من، زامبی» که بین سرور و هر دو
 * کلاینت رد و بدل می‌شود.
 *
 * چرا این کلاس لازم است؟ چون کلاس‌های مدل اصلی بازی (GameSession، Board،
 * Plant، Zombie و ...) پیاده‌ساز Serializable نیستند و به همین دلیل نمی‌توان
 * خود شیء IZombieSession را مستقیماً با ObjectOutputStream روی شبکه فرستاد
 * (این کار باعث NotSerializableException می‌شود). بنابراین سرور در هر تیک،
 * یک نسخه‌ی سبک و فقط‌خواندنی (DTO) از وضعیت لازم برای رسم صفحه در کلاینت را
 * می‌سازد و همین DTO سریالایز و ارسال می‌شود؛ منطق واقعی بازی («سرور مرجع»
 * است) فقط روی خود سرور اجرا می‌شود.
 */
public class BoardSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    public int currentSun;
    // === رفع باگ بودجه‌ی مشترک ===
    // قبلاً فقط یک عدد خورشید مشترک ارسال می‌شد (که واقعاً مال طرف گیاه بود)
    // و طرف زامبی هیچ‌وقت بودجه‌ی واقعی خودش را نمی‌دید. حالا هر دو مقدار
    // جداگانه ارسال می‌شود تا هر کلاینت طبق نقش خودش عدد درست را نشان دهد.
    public int plantSun;
    public int zombieSun;
    public boolean gameOver;
    public boolean plantSideWon;
    public long tickCount;
    public int timeRemainingTicks; // شمارش معکوس تایمر ۲ دقیقه‌ای طرف گیاه
    public List<String> events = new ArrayList<>();

    public List<PlantDto> plants = new ArrayList<>();
    public List<ZombieDto> zombies = new ArrayList<>();

    public static class PlantDto implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public int row;
        public int col;
        public int health;
        public int maxHealth;
    }

    public static class ZombieDto implements Serializable {
        private static final long serialVersionUID = 1L;
        public String typeName;
        public int row;
        public double xPosition;
        public int health;
        public int maxHealth;
    }

    /** ساخت عکس فوری از یک IZombieSession زنده روی سرور. */
    public static BoardSnapshot capture(IZombieSession session, int timeRemainingTicks, List<String> newEvents) {
        BoardSnapshot snap = new BoardSnapshot();
        snap.currentSun = session.getSunManager().getCurrentSun();
        snap.plantSun = session.getSunManager().getCurrentSun();
        snap.zombieSun = session.getZombieSunManager().getCurrentSun();
        snap.gameOver = session.isGameOver();
        snap.plantSideWon = session.isGameOver() && session.isWon();
        snap.tickCount = session.getTickCount();
        snap.timeRemainingTicks = timeRemainingTicks;
        if (newEvents != null) {
            snap.events.addAll(newEvents);
        }

        Board board = session.getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                Plant p = tile.getPlant();
                if (p != null) {
                    PlantDto dto = new PlantDto();
                    dto.name = p.getName();
                    dto.row = r;
                    dto.col = c;
                    dto.health = p.getHealth();
                    dto.maxHealth = p.getMaxHealth();
                    snap.plants.add(dto);
                }
            }
        }

        for (Zombie z : session.getAliveZombies()) {
            ZombieDto dto = new ZombieDto();
            dto.typeName = z.getTypeName();
            dto.row = z.getRow();
            dto.xPosition = z.getXPosition();
            dto.health = z.getHealth();
            dto.maxHealth = z.getMaxHealth();
            snap.zombies.add(dto);
        }

        return snap;
    }
}
