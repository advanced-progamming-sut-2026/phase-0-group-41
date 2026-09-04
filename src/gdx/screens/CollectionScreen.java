package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.HudBar;
import gdx.util.ImageUtils;
import model.plant.Plant;
import model.plant.PlantFactory;
import model.plant.PlantTag;
import model.user.User;
import model.zombie.Zombie;
import model.zombie.ZombieFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * منوی کلکسیون: دو زبانه‌ی مجزا برای گیاهان و زامبی‌ها. طبق سند، در هر زبانه
 * فهرستی از موجودات با کلیک روی هرکدام، اطلاعات جزئی آن نمایش داده می‌شود.
 */
public class CollectionScreen extends BaseMenuScreen {

    private enum Tab { PLANTS, ZOMBIES }
    private enum PlantFilter { ALL, LOCKED, UNLOCKED, UPGRADEABLE }

    private Tab currentTab = Tab.PLANTS;
    private PlantFilter plantFilter = PlantFilter.ALL;
    private String tagFilter = "All";

    private final HudBar hudBar;
    private final Table listTable = new Table();
    private final Table detailsTable = new Table();
    private final SelectBox<String> tagFilterBox;

    public CollectionScreen(PvZGame game) {
        super(game);
        User user = game.getLoggedInUser();

        Table top = new Table();
        top.setFillParent(true);
        top.top();
        hudBar = new HudBar(skin, user);
        top.add(hudBar).expandX().fillX().top();
        stage.addActor(top);

        rootTable.add(title("Collection")).padBottom(10f).row();

        Table tabRow = new Table();
        addButton(tabRow, "Plants", () -> { currentTab = Tab.PLANTS; clearDetails(); refreshList(); });
        addButton(tabRow, "Zombies", () -> { currentTab = Tab.ZOMBIES; clearDetails(); refreshList(); });
        rootTable.add(tabRow).padBottom(10f).row();

        Table filterRow = new Table();
        filterRow.add(new Label("Filter:", skin)).padRight(6f);
        SelectBox<String> statusFilterBox = new SelectBox<>(skin);
        statusFilterBox.setItems("All", "Locked", "Unlocked", "Upgradeable");
        statusFilterBox.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                plantFilter = PlantFilter.valueOf(statusFilterBox.getSelected().toUpperCase());
                refreshList();
            }
        });
        filterRow.add(statusFilterBox).width(160f).padRight(16f);

        filterRow.add(new Label("Tag:", skin)).padRight(6f);
        List<String> tagItems = new ArrayList<>();
        tagItems.add("All");
        for (PlantTag tag : PlantTag.values()) {
            tagItems.add(tag.name());
        }
        tagFilterBox = new SelectBox<>(skin);
        tagFilterBox.setItems(tagItems.toArray(new String[0]));
        tagFilterBox.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                tagFilter = tagFilterBox.getSelected();
                refreshList();
            }
        });
        filterRow.add(tagFilterBox).width(160f);
        rootTable.add(filterRow).padBottom(10f).row();

        listTable.top().left();
        ScrollPane listScroll = new ScrollPane(listTable, skin);
        listScroll.setFadeScrollBars(false);
        rootTable.add(listScroll).width(900f).height(280f).padBottom(14f).row();

        detailsTable.top().left();
        rootTable.add(detailsTable).width(900f).height(140f).padBottom(10f).row();

        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);

        refreshList();
    }

    private void refreshList() {
        listTable.clear();
        tagFilterBox.setVisible(currentTab == Tab.PLANTS);
        User user = game.getLoggedInUser();
        if (user == null) {
            return;
        }
        int col = 0;
        if (currentTab == Tab.PLANTS) {
            for (String plantName : PlantFactory.allPlantNames()) {
                boolean unlocked = user.getUnlockedPlants().contains(plantName);
                int level = user.getPlantLevel(plantName);
                int packets = user.getSeedPackets(plantName);
                boolean upgradeable = unlocked && packets >= 1; // مطابق هزینه‌ی ثابت ارتقا در CollectionController

                if (plantFilter == PlantFilter.LOCKED && unlocked) continue;
                if (plantFilter == PlantFilter.UNLOCKED && !unlocked) continue;
                if (plantFilter == PlantFilter.UPGRADEABLE && !upgradeable) continue;
                if (!"All".equals(tagFilter) && unlocked) {
                    Plant probe = PlantFactory.create(plantName);
                    if (!probe.hasTag(PlantTag.valueOf(tagFilter))) continue;
                } else if (!"All".equals(tagFilter) && !unlocked) {
                    continue; // گیاه قفل را نمی‌توان بر اساس تگ فیلتر کرد (تگ‌هایش مشخص نیست تا کشف نشده)
                }

                listTable.add(buildPlantCard(plantName, unlocked, level, packets)).size(90f, 110f).pad(4f);
                if (++col % 8 == 0) listTable.row();
            }
        } else {
            for (String zombieName : ZombieFactory.allZombieNames()) {
                boolean seen = user.getSeenZombies().contains(zombieName);
                listTable.add(buildZombieCard(zombieName, seen)).size(90f, 90f).pad(4f);
                if (++col % 8 == 0) listTable.row();
            }
        }
    }

    private Stack buildPlantCard(String plantName, boolean unlocked, int level, int packets) {
        Stack stack = new Stack();
        stack.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        if (unlocked) {
            // === رفع باگ: کش‌شدن گرافیک گیاه در گالری/کالکشن ===
            // قبلاً Image پیش‌فرض (Scaling.stretch) بود، پس هر گیاه بدون
            // توجه به نسبت ابعاد واقعی‌اش داخل کارت ۹۰×۱۱۰ کش می‌شد. با
            // Scaling.fit تصویر با حفظ نسبت ابعاد و وسط‌چین نمایش داده می‌شود.
            Image plantImage = new Image(ImageUtils.loadRegion(AssetPaths.plantIcon(plantName)));
            plantImage.setScaling(Scaling.fit);
            plantImage.setAlign(Align.center);
            stack.add(plantImage);
        } else if (!AssetPaths.ICON_LOCK.isEmpty()) {
            stack.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_LOCK)));
        }
        Table overlay = new Table();
        overlay.bottom();
        String caption = unlocked ? (plantName + " Lv" + level) : plantName;
        Label label = new Label(caption, skin);
        label.setFontScale(0.5f);
        overlay.add(label);
        stack.add(overlay);

        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showPlantDetails(plantName, unlocked, level, packets);
            }
        });
        return stack;
    }

    private Stack buildZombieCard(String zombieName, boolean seen) {
        Stack stack = new Stack();
        stack.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        if (seen) {
            stack.add(new Image(ImageUtils.loadRegion(AssetPaths.zombieIcon(zombieName))));
            Table overlay = new Table();
            overlay.bottom();
            Label label = new Label(zombieName, skin);
            label.setFontScale(0.5f);
            overlay.add(label);
            stack.add(overlay);
            stack.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showZombieDetails(zombieName);
                }
            });
        }
        // در غیر این صورت، کارت خالی می‌ماند (زامبی هنوز کشف نشده)
        return stack;
    }

    private void showPlantDetails(String plantName, boolean unlocked, int level, int packets) {
        detailsTable.clear();
        if (!unlocked) {
            detailsTable.add(new Label(plantName + " - Locked", skin)).padBottom(6f).row();
            addButton(detailsTable, "Buy for 2000 coins", () -> {
                User user = game.getLoggedInUser();
                if (user.getUnlockedPlants().contains(plantName)) {
                    return;
                }
                if (!user.spendCoins(2000)) {
                    showError("Not enough coins.");
                    return;
                }
                user.getUnlockedPlants().add(plantName);
                user.addSeedPackets(plantName, 1);
                user.addNews("New plant unlocked: " + plantName);
                game.getUserManager().save();
                hudBar.refresh(user);
                refreshList();
                clearDetails();
            });
            return;
        }

        Plant plant = PlantFactory.create(plantName);
        plant.applyUpgradeLevel(level);
        StringBuilder tags = new StringBuilder();
        for (PlantTag tag : PlantTag.values()) {
            if (plant.hasTag(tag)) {
                if (tags.length() > 0) tags.append(", ");
                tags.append(tag.name());
            }
        }
        String info = plantName + " | Level " + level
                + " | Health: " + plant.getMaxHealth()
                + " | Cost: " + plant.getSunCost()
                + " | Seed packets: " + packets + "/1"
                + (tags.length() > 0 ? " | Tags: " + tags : "");
        Label infoLabel = new Label(info, skin);
        infoLabel.setWrap(true);
        detailsTable.add(infoLabel).width(880f).padBottom(8f).row();

        addButton(detailsTable, "Upgrade (1000 coins, 1 seed packet)", () -> {
            User user = game.getLoggedInUser();
            if (user.upgradePlant(plantName, 1000, 1)) {
                game.getUserManager().save();
                hudBar.refresh(user);
                refreshList();
                showPlantDetails(plantName, true, user.getPlantLevel(plantName), user.getSeedPackets(plantName));
            } else {
                showError("Not enough coins or seed packets to upgrade.");
            }
        });
    }

    private void showZombieDetails(String zombieName) {
        detailsTable.clear();
        Zombie zombie = ZombieFactory.create(zombieName, 3);
        String info = zombieName + " | Health: " + zombie.getMaxHealth() + " | Speed: " + zombie.getSpeed();
        Label infoLabel = new Label(info, skin);
        infoLabel.setWrap(true);
        detailsTable.add(infoLabel).width(880f);
    }

    private void clearDetails() {
        detailsTable.clear();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_COLLECTION;
    }
}
