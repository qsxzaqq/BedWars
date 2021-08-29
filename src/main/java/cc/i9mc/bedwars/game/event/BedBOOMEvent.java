package cc.i9mc.bedwars.game.event;

import cc.i9mc.bedwars.Bedwars;
import cc.i9mc.bedwars.game.Game;
import cc.i9mc.bedwars.game.GameTeam;
import org.bukkit.Material;
import org.bukkit.Sound;

public class BedBOOMEvent extends GameEvent {
    public BedBOOMEvent() {
        super("床自毁", 600, 2);
    }

    @Override
    public void excute(Game game) {
        Bedwars.getInstance().mainThreadRunnable(() -> {
            for (GameTeam gameTeam : game.getGameTeams()) {
                if (gameTeam.isBedDestroy()) continue;
                gameTeam.getBedHead().setType(Material.AIR);
                gameTeam.getBedFeet().setType(Material.AIR);
                gameTeam.setBedDestroy(true);
            }
        });

        game.broadcastSound(Sound.ENDERDRAGON_GROWL, 1, 1);
        game.broadcastTitle(10, 20, 10, "§c§l床自毁", "§e所有队伍床消失");
    }
}
