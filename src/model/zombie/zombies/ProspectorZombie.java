package model.zombie.zombies;
import model.zombie.Zombie;
import model.game.GameSession;
import model.game.Board;
import model.game.Tile;
import model.plant.Plant;
public class ProspectorZombie extends Zombie {
    private boolean isDynamiteActive=true;
    private int dynamiteTicks=0;
    private static final int EXPLOSION_TIME = 100;

    private boolean isMovingRight=false;
 public ProspectorZombie(){
     super("prospector",300,0.015,200,10);
 }
@Override
    public void onTick(GameSession session){
     if(isDead())return;

     if(isDynamiteActive){
         dynamiteTicks++;
         if(dynamiteTicks>=EXPLOSION_TIME){
             isDynamiteActive=false;
             isMovingRight=true;
             setXPosition(0.0);
         }
     }
     if(isMovingRight){
         moveRightOrAttack(session);
     }
     else{
         moveLeftOrAttack(session);
     }
}
    public void defuseDynamite() {
        if (isDynamiteActive) {
            isDynamiteActive = false;
        }
    }

    private void moveLeftOrAttack(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        Tile tile = session.getBoard().getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        // زامبی از راست وارد می‌شود، باید به وسط خانه برسد (<= 0.5)
        if (target != null && !target.isDead() && (getXPosition() - col) <= 0.5) {
            setEating(true);
            target.takeDamage(getDamagePerTick());
            if (target.isDead()) {
                tile.setPlant(null);
                setEating(false);
            }
        } else {
            setEating(false);
            setXPosition(getXPosition() - getSpeed()); // حرکت به چپ (منها)
        }
    }

    // متد ۴: حرکت برعکس بعد از انفجار (چپ به راست) با تارگت‌گیری دقیق
    private void moveRightOrAttack(GameSession session) {
        int col = (int) Math.floor(getXPosition());

        // در حرکت به راست، باید مطمئن شویم از نقشه خارج نمی‌شود
        if (col >= Board.COLS) {
            takeDamage(9999); // خروج از نقشه مساوی با حذف زامبی است
            return;
        }

        Tile tile = session.getBoard().getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        // زامبی از چپ وارد می‌شود، کمی جلو می‌آید تا به گیاه برسد (>= 0.2)
        if (target != null && !target.isDead() && (getXPosition() - col) >= 0.2) {
            setEating(true);
            target.takeDamage(getDamagePerTick());
            if (target.isDead()) {
                tile.setPlant(null);
                setEating(false);
            }
        } else {
            setEating(false);
            setXPosition(getXPosition() + getSpeed()); // حرکت به راست (جمع)
        }
    }

}
