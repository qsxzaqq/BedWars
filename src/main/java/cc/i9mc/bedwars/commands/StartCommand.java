package cc.i9mc.bedwars.commands;

import cc.i9mc.bedwars.Bedwars;
import cc.i9mc.bedwars.game.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player) || commandSender.getName().equals("SuperPi")) {
            Game game = Bedwars.getInstance().getGame();

            game.setForceStart(true);
            game.start();
        }
        return false;
    }
}
