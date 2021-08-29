package cc.i9mc.bedwars.listeners;

import cc.i9mc.gameutils.utils.ActionBarUtil;
import cc.i9mc.bedwars.Bedwars;
import cc.i9mc.bedwars.database.map.MapData;
import cc.i9mc.bedwars.events.BedwarsDestroyBedEvent;
import cc.i9mc.bedwars.game.Game;
import cc.i9mc.bedwars.game.GamePlayer;
import cc.i9mc.bedwars.game.GameState;
import cc.i9mc.bedwars.game.GameTeam;
import cc.i9mc.bedwars.utils.SoundUtil;
import cc.i9mc.bedwars.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class BlockListener implements Listener {
    private final Game game = Bedwars.getInstance().getGame();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        Block block = event.getBlock();

        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        if (block.getType().toString().startsWith("BED")) {
            event.setCancelled(true);
            return;
        }

        if (game.getMapData().hasRegion(block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        for (GameTeam gameTeam : game.getGameTeams()) {
            if (gameTeam.getSpawn().distance(block.getLocation()) <= 5) {
                event.setCancelled(true);
                return;
            }
        }

        if (block.getType() == Material.ENDER_CHEST) {
            GameTeam gameTeam = gamePlayer.getGameTeam();
            if (gameTeam.getTeamChests() == null) {
                gameTeam.createTeamInventory();
            }

            gameTeam.getTeamChests().add(block);
            return;
        }

        if (block.getType() == Material.TNT) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);

            TNTPrimed tnt = event.getBlock().getWorld().spawn(block.getLocation().add(0.5D, 0.0D, 0.5D), TNTPrimed.class);
            tnt.setVelocity(new Vector(0, 0, 0));

            if (player.getItemInHand().getType() == Material.TNT) {
                if (player.getItemInHand().getAmount() == 1) {
                    player.getInventory().setItemInHand(null);
                } else {
                    player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount() - 1);
                }
            }
            return;
        }

        if (event.getItemInHand().getType() == Material.WOOL && !event.getItemInHand().getEnchantments().isEmpty()) {
            if (Math.abs(System.currentTimeMillis() - (player.hasMetadata("Game BLOCK TIMER") ? player.getMetadata("Game BLOCK TIMER").get(0).asLong() : 0L)) < 1000) {
                event.setCancelled(true);
                return;
            }
            player.setMetadata("Game BLOCK TIMER", new FixedMetadataValue(Bedwars.getInstance(), System.currentTimeMillis()));

            if (block.getY() != event.getBlockAgainst().getY()) {
                if (Math.max(Math.abs(player.getLocation().getX() - (block.getX() + 0.5D)), Math.abs(player.getLocation().getZ() - (block.getZ() + 0.5D))) < 0.5) {
                    return;
                }
            }
            BlockFace blockFace = event.getBlockAgainst().getFace(block);

            new BukkitRunnable() {
                int i = 1;

                @Override
                public void run() {
                    if (i > 6) {
                        cancel();
                    }

                    for (GameTeam gameTeam : game.getGameTeams()) {
                        if (gameTeam.getSpawn().distance(block.getRelative(blockFace, i).getLocation()) <= 5) {
                            event.setCancelled(true);
                            return;
                        }
                    }

                    if (Bedwars.getInstance().getGame().getMapData().hasRegion(block.getRelative(blockFace, i).getLocation())) {
                        return;
                    }

                    if (block.getRelative(blockFace, i).getType() == Material.AIR) {
                        block.getRelative(blockFace, i).setType(event.getItemInHand().getType());
                        block.getRelative(blockFace, i).setData(event.getItemInHand().getData().getData());
                        block.getWorld().playSound(block.getLocation(), SoundUtil.get("STEP_WOOL", "BLOCK_CLOTH_STEP"), 1f, 1f);
                    }

                    i++;
                }
            }.runTaskTimer(Bedwars.getInstance(), 0, 4L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (game.getGameState() == GameState.RUNNING) {
            Player player = event.getPlayer();
            Block block = event.getBlock();
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            if (gamePlayer == null) {
                return;
            }

            GameTeam gameTeam = gamePlayer.getGameTeam();

            if (gamePlayer.isSpectator()) {
                event.setCancelled(true);
                return;
            }

            if (block.getType().toString().startsWith("BED")) {
                event.setCancelled(true);

                if (gameTeam.getSpawn().distance(block.getLocation()) <= 18.0D) {
                    player.sendMessage("§c你不能破坏你家的床");
                    return;
                }

                for (GameTeam gameTeam1 : game.getGameTeams()) {
                    if (gameTeam1.getSpawn().distance(block.getLocation()) <= 18.0D) {
                        if (!gameTeam1.isDead()) {
                            Util.dropTargetBlock(block);

                            new BukkitRunnable() {
                                int i = 0;

                                @Override
                                public void run() {
                                    if (i == 5) {
                                        cancel();
                                        return;
                                    }
                                    ActionBarUtil.sendBar(player, "§6+10个金币");
                                    i++;
                                }
                            }.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, 10);
                            player.sendMessage("§6+10个金币 (破坏床)");
                            Bedwars.getInstance().getEcon().depositPlayer(player, 10);


                            game.broadcastSound(SoundUtil.get("ENDERDRAGON_HIT", "ENTITY_ENDERDRAGON_HURT"), 10, 10);
                            game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
                            game.broadcastMessage(" ");
                            game.broadcastMessage("§c§l" + gameTeam1.getName() + " §a的床被 " + gameTeam.getChatColor() + gamePlayer.getDisplayname() + "§a 挖爆!");
                            game.broadcastMessage(" ");
                            game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");

                            game.broadcastTeamTitle(gameTeam1, 1, 20, 1, "§c§l床被摧毁", "§c死亡将无法复活");

                            Bukkit.getPluginManager().callEvent(new BedwarsDestroyBedEvent(player, gameTeam1));

                            gameTeam1.setDestroyPlayer(gamePlayer);
                            gameTeam1.setBedDestroy(true);

                            gamePlayer.getPlayerData().addDestroyedBeds();
                            return;
                        }
                        player.sendMessage("§c此床没有队伍");
                        return;
                    }
                }
            }

            if (block.getType() == Material.ENDER_CHEST) {
                for (GameTeam gameTeam1 : game.getGameTeams()) {
                    if (gameTeam1.getTeamChests().contains(block)) {
                        gameTeam1.removeChest(block);
                        if (gameTeam1.isInTeam(gamePlayer)) {
                            game.broadcastTeamMessage(gameTeam1, "§a你的队友 §f" + gamePlayer.getDisplayname() + " §c正在破坏一个队伍箱子!");
                        } else {
                            game.broadcastTeamMessage(gameTeam1, "§c一个外来人员正在破坏一个队伍箱子!");
                        }
                        break;
                    }
                }

                ItemStack enderChest = new ItemStack(Material.ENDER_CHEST, 1);
                ItemMeta meta = enderChest.getItemMeta();
                enderChest.setItemMeta(meta);

                event.setCancelled(true);
                block.getDrops().clear();
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), enderChest);
                return;
            }

            if (game.getMapData().hasRegion(block.getLocation())) {
                event.setCancelled(true);
                return;
            }

            if (game.getBlocks().contains(block.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (game.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        for (int i = 0; i < event.blockList().size(); i++) {
            Block b = event.blockList().get(i);
            if (Bedwars.getInstance().getGame().getMapData().hasRegion(b.getLocation())) {
                event.setCancelled(true);
                continue;
            }

            if (b.getType() != Material.STAINED_GLASS && b.getType() != Material.BED_BLOCK) {
                if (!game.getBlocks().contains(b.getLocation())) {
                    event.setCancelled(true);
                    b.setType(Material.AIR);
                    b.getWorld().spigot().playEffect(b.getLocation(), Effect.EXPLOSION_HUGE);
                    b.getWorld().playSound(b.getLocation(), SoundUtil.get("EXPLODE", "ENTITY_GENERIC_EXPLODE"), 1.0F, 1.0F);
                }
            }
        }

        if (entity instanceof Fireball) {
            Fireball fireball = (Fireball) entity;
            if (!fireball.hasMetadata("Game FIREBALL")) {
                return;
            }
            GamePlayer ownerPlayer = GamePlayer.get((UUID) fireball.getMetadata("Game FIREBALL").get(0).value());

            for (Entity entity1 : entity.getNearbyEntities(4, 3, 4)) {
                if (!(entity1 instanceof Player)) {
                    continue;
                }

                Player player = (Player) entity1;
                GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

                if (fireball.hasMetadata("Game FIREBALL")) {
                    GameTeam gameTeam = ownerPlayer.getGameTeam();
                    if (gameTeam != null && gameTeam.isInTeam(ownerPlayer, gamePlayer)) {
                        continue;
                    }
                }

                player.damage(3);
                gamePlayer.getAssistsMap().setLastDamage(ownerPlayer, System.currentTimeMillis());
                player.setMetadata("FIREBALL PLAYER NOFALL", new FixedMetadataValue(Bedwars.getInstance(), ownerPlayer.getUuid()));
                player.setVelocity(Util.getPosition(player.getLocation(), fireball.getLocation(), 1.5D).multiply(0.5));
            }
        }
        event.setCancelled(true);
    }
}
