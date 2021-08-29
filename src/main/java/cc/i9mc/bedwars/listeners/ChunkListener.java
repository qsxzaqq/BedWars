package cc.i9mc.bedwars.listeners;

import cc.i9mc.bedwars.Bedwars;
import cc.i9mc.bedwars.game.Game;
import cc.i9mc.bedwars.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {
    private final Game game = Bedwars.getInstance().getGame();

    @EventHandler
    public void onUnload(ChunkUnloadEvent unload) {
        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (!game.getMapData().chunkIsInRegion(unload.getChunk().getX(), unload.getChunk().getZ())) {
            return;
        }

        unload.setCancelled(true);
    }

}
