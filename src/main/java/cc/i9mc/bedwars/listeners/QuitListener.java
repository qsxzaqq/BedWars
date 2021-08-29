package cc.i9mc.bedwars.listeners;

import cc.i9mc.bedwars.Bedwars;
import cc.i9mc.bedwars.game.Game;
import cc.i9mc.bedwars.game.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    private final Game game = Bedwars.getInstance().getGame();

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        game.removePlayers(GamePlayer.get(event.getPlayer().getUniqueId()));
    }
}
