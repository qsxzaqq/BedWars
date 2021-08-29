package cc.i9mc.bedwars.game.event;

import cc.i9mc.k8sgameack.events.ACKGameEndEvent;
import cc.i9mc.bedwars.events.BedwarsGameEndEvent;
import cc.i9mc.bedwars.game.Game;
import org.bukkit.Bukkit;

public class EndEvent extends GameEvent {
    public EndEvent() {
        super("游戏结束！", 30, 4);
    }

    public void excute(Game game) {
        Bukkit.getPluginManager().callEvent(new BedwarsGameEndEvent());
        Bukkit.getPluginManager().callEvent(new ACKGameEndEvent());
        Bukkit.shutdown();
    }
}
