package cc.i9mc.bedwars.game.event;

import cc.i9mc.bedwars.game.Game;

public class WitherBowEvent extends GameEvent {
    public WitherBowEvent() {
        super("开启凋零弓", 2400, 1);
    }

    @Override
    public void excute(Game game) {
        game.broadcastTitle(1, 2, 1, "&b&l凋零战弓", "&l所有弓箭转换为凋零战弓");
    }

    @Override
    public void excuteRunnbale(Game game, int seconds) {

    }
}
