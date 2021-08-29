package cc.i9mc.bedwars.listeners;

import cc.i9mc.gameutils.utils.TitleUtil;
import cc.i9mc.bedwars.Bedwars;
import cc.i9mc.bedwars.database.PlayerData;
import cc.i9mc.bedwars.game.Game;
import cc.i9mc.bedwars.game.GamePlayer;
import cc.i9mc.bedwars.game.GameState;
import cc.i9mc.bedwars.game.GameTeam;
import cc.i9mc.bedwars.types.ToolType;
import cc.i9mc.bedwars.utils.SoundUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReSpawnListener implements Listener {
    private final List<UUID> noDamage = new ArrayList<>();
    private final Game game = Bedwars.getInstance().getGame();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        GameTeam gameTeam = gamePlayer.getGameTeam();
        PlayerData playerData = gamePlayer.getPlayerData();

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        gamePlayer.clean();

        if (gameTeam.isBedDestroy()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    TextComponent textComponent = new TextComponent("§c你凉了!想再来一局嘛? ");
                    textComponent.addExtra("§b§l点击这里!");
                    textComponent.getExtra().get(0).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/queue join qc jdqc"));
                    player.spigot().sendMessage(textComponent);

                    event.setRespawnLocation(gameTeam.getSpawn());
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0.0F);
                    player.teleport(gameTeam.getSpawn());
                    GamePlayer.getOnlinePlayers().forEach((gamePlayer1 -> gamePlayer1.getPlayer().hidePlayer(player)));

                    gamePlayer.toSpectator("§c你凉了！", "§7你没床了");
                }
            }.runTaskLater(Bedwars.getInstance(), 1L);
            playerData.addLoses();

            if (gameTeam.isDead()) {
                game.broadcastSound(SoundUtil.get("ENDERDRAGON_HIT", "ENTITY_ENDERDRAGON_HURT"), 10, 10);
                game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
                game.broadcastMessage(" ");
                game.broadcastMessage(gameTeam.getChatColor() + gameTeam.getName() + " §c凉了! §e挖床者: " + (gameTeam.getDestroyPlayer() != null ? gameTeam.getDestroyPlayer().getDisplayname() : "null"));
                game.broadcastMessage(" ");
                game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
            }

            return;
        }

        event.setRespawnLocation(gameTeam.getSpawn());
        player.setExp(0f);
        player.setLevel(0);
        player.teleport(gameTeam.getSpawn());
        player.setGameMode(GameMode.SURVIVAL);
        noDamage.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> noDamage.remove(player.getUniqueId()), 60);
        TitleUtil.sendTitle(player, 1, 20, 1, "§a已复活！", "§7因为你的床还在所以你复活了");
    }

    @EventHandler
    public void onDamage(EntityDamageEvent evt) {
        if (noDamage.contains(evt.getEntity().getUniqueId())) {
            evt.setCancelled(true);
        }
    }
}
