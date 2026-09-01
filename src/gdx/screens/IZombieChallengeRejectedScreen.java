package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import gdx.PvZGame;
import gdx.assets.AssetPaths;

public class IZombieChallengeRejectedScreen extends BaseMenuScreen {

    public IZombieChallengeRejectedScreen(PvZGame game, String opponentUsername) {
        super(game);
        rootTable.add(title("Challenge Declined")).padBottom(16f).row();
        Label msg = new Label(opponentUsername + " declined your challenge.", skin);
        msg.setWrap(true);
        rootTable.add(msg).width(500f).padBottom(30f).row();
        addButton(rootTable, "Back to Mini Games", game::goToMiniGames);
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_QUEST;
    }
}
