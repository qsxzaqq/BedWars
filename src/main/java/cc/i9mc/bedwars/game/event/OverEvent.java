package cc.i9mc.bedwars.game.event;

import cc.i9mc.bedwars.events.BedwarsGameOverEvent;
import cc.i9mc.bedwars.game.Game;
import cc.i9mc.bedwars.game.GameOverRunnable;
import org.bukkit.Bukkit;

public class OverEvent extends GameEvent {
    public OverEvent() {
        super("游戏结束", 600, 3);
    }

    public void excute(Game game) {
        game.getEventManager().setCurrentEvent(4);
        Bukkit.getPluginManager().callEvent(new BedwarsGameOverEvent(game.getWinner()));
        new GameOverRunnable(game);
    }
}
