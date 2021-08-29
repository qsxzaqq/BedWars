package cc.i9mc.bedwars.specials;

import cc.i9mc.bedwars.Bedwars;
import cc.i9mc.bedwars.game.Game;
import cc.i9mc.bedwars.game.GamePlayer;
import cc.i9mc.bedwars.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProtectionWallListener implements Listener {
    private static final Game game = Bedwars.getInstance().getGame();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent interact) {
        if (interact.getAction().equals(Action.LEFT_CLICK_AIR) || interact.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        if (interact.getMaterial() == null) {
            return;
        }

        ProtectionWall wall = new ProtectionWall();
        if (interact.getMaterial() != wall.getItemMaterial()) {
            return;
        }


        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (GamePlayer.get(interact.getPlayer().getUniqueId()).isSpectator()) {
            return;
        }

        wall.create(interact.getPlayer(), game);
        interact.setCancelled(true);
    }
}
