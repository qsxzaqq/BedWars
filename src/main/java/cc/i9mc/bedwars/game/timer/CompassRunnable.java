package cc.i9mc.bedwars.game.timer;

import cc.i9mc.bedwars.Bedwars;
import cc.i9mc.bedwars.game.GamePlayer;
import cc.i9mc.bedwars.spectator.SpectatorTarget;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by JinVan on 2021-01-02.
 */
public class CompassRunnable {
    private boolean timer;

    public void start() {
        if (!this.timer) {
            timer = true;

            Bukkit.getScheduler().runTaskTimer(Bedwars.getInstance(), () -> {
                for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
                    if (gamePlayer.isSpectator()) {
                        SpectatorTarget target = gamePlayer.getSpectatorTarget();
                        target.sendTip();
                        target.autoTp();
                        continue;
                    }
                    Player player = gamePlayer.getPlayer();
                    ItemStack itemStack = player.getItemInHand();
                    if (itemStack != null && itemStack.getType() == Material.COMPASS) {
                        gamePlayer.getPlayerCompass().sendClosestPlayer();
                    }
                }
            }, 0L, 1L);
        }
    }
}
