package cc.i9mc.bedwars.listeners;

import cc.i9mc.bedwars.Bedwars;
import cc.i9mc.bedwars.database.PlayerData;
import cc.i9mc.bedwars.game.Game;
import cc.i9mc.bedwars.game.GamePlayer;
import cc.i9mc.bedwars.game.GameState;
import cc.i9mc.bedwars.game.GameTeam;
import cc.i9mc.bedwars.guis.ModeSelectionGUI;
import cc.i9mc.bedwars.guis.TeamSelectionGUI;
import cc.i9mc.bedwars.shop.NewItemShop;
import cc.i9mc.bedwars.spectator.SpectatorCompassGUI;
import cc.i9mc.bedwars.spectator.SpectatorSettingGUI;
import cc.i9mc.bedwars.spectator.SpectatorSettings;
import cc.i9mc.bedwars.types.ModeType;
import cc.i9mc.bedwars.utils.SoundUtil;
import cc.i9mc.bedwars.villager.MerchantCategoryManager;
import cc.i9mc.gameutils.utils.BungeeUtil;
import cc.i9mc.watchnmslreport.BukkitReport;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
    private final Game game = Bedwars.getInstance().getGame();

    @EventHandler
    public void onEntityShoot(EntityShootBowEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player p = (Player)event.getEntity();
            if (game.getEventManager().currentEvent().getPriority() < 1) {
                    WitherSkull skull = p.launchProjectile(WitherSkull.class);
                    skull.setYield(3.0F);
                    skull.setVelocity(event.getProjectile().getVelocity());
                    p.getWorld().playSound(p.getLocation(), Sound.WITHER_HURT, 1.0F, 0.0F);
                    p.getWorld().playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 5);
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        Entity e1 = e.getEntity();
        Entity e2 = e.getDamager();
        if (e1 instanceof Player && e2 instanceof WitherSkull) {
            Player player = (Player)e1;
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
            e2.getWorld().playSound(e2.getLocation(), Sound.WITHER_HURT, 1.0F, 0.0F);
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (GamePlayer.get(event.getEntity().getUniqueId()).isSpectator()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void craftItem(PrepareItemCraftEvent event) {
        for (HumanEntity h : event.getViewers()) {
            if (h instanceof Player) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        GamePlayer gamePlayer =  GamePlayer.get(event.getWhoClicked().getUniqueId());

        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory().getName().equals("§8道具商店")) {
            event.setCancelled(true);
            ItemStack clickedStack = event.getCurrentItem();

            if (clickedStack == null) {
                return;
            }

            gamePlayer.getNewItemShop().handleInventoryClick(event, game, (Player) event.getWhoClicked());
        }
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material interactingMaterial = event.getMaterial();

        if (interactingMaterial == null) {
            event.setCancelled(true);
            return;
        }

        if (game.getGameState() == GameState.WAITING) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                event.setCancelled(true);
                switch (interactingMaterial) {
                    case PAPER:
                        new ModeSelectionGUI(player).open();
                        return;
                    case BED:
                        new TeamSelectionGUI(player, game).open();
                        return;
                    case SLIME_BALL:
                        BungeeUtil.send("BW-Lobby-1", player);
                        return;
                    default:
                        return;
                }
            }
        }

        if (game.getGameState() == GameState.RUNNING) {
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            GameTeam gameTeam = gamePlayer.getGameTeam();

            if (event.getAction() == Action.PHYSICAL) {
                event.setCancelled(true);
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType().toString().startsWith("BED")) {
                if (gamePlayer.isSpectator()) {
                    event.setCancelled(true);
                    return;
                }

                if (player.isSneaking() && player.getItemInHand() != null && player.getItemInHand().getType().isBlock()) {
                    return;
                }

                player.sendMessage("§4睡你妈逼起来嗨!");
                event.setCancelled(true);
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                if (gamePlayer.isSpectator()) {
                    event.setCancelled(true);
                    return;
                }

                if (player.isSneaking() && player.getItemInHand() != null && player.getItemInHand().getType().isBlock()) {
                    return;
                }

                if (gameTeam.getTeamChests().contains(event.getClickedBlock())) {
                    player.openInventory(gameTeam.getInventory());
                } else {
                    gamePlayer.sendMessage("§c这个箱子不是你队伍的箱子!");
                }
                event.setCancelled(true);
                return;
            }

            if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && (gamePlayer.getSpectatorTarget() != null) && interactingMaterial == Material.COMPASS) {
                gamePlayer.getSpectatorTarget().tp();
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                switch (interactingMaterial) {
                    case COMPASS:
                        event.setCancelled(true);
                        if (!gamePlayer.isSpectator()) {
                            return;
                        }

                        new SpectatorCompassGUI(player).open();
                        return;
                    case REDSTONE_COMPARATOR:
                        new SpectatorSettingGUI(player).open();
                        return;
                    case PAPER:
                        event.setCancelled(true);
                        Bukkit.dispatchCommand(player, "queue join qc jdqc");
                        return;
                    case SLIME_BALL:
                        event.setCancelled(true);
                        BungeeUtil.send("BW-Lobby-1", player);
                        return;
                    case BED:
                        event.setCancelled(true);
                        if (gamePlayer.isSpectator()) {
                            return;
                        }

                        int priority = game.getEventManager().currentEvent().getPriority();
                        if (priority > 2 || priority == 2 && game.getEventManager().getLeftTime() <= 120) {
                            player.sendMessage("§c开局已超过10分钟.");
                            return;
                        }

                        if (gameTeam.isUnbed()) {
                            player.sendMessage("§c已使用过回春床了.");
                            return;
                        }

                        if (!gameTeam.isBedDestroy()) {
                            player.sendMessage("§c床还在,回啥春呢?");
                            return;
                        }

                        if (player.getLocation().distance(gameTeam.getSpawn()) > 18) {
                            player.sendMessage("§c请靠近出生点使用!");
                            return;
                        }

                        BlockFace face = gameTeam.getBedFace();

                        if (face == BlockFace.NORTH) {
                            Location l = gameTeam.getBedHead().getLocation();
                            l.getBlock().setType(Material.AIR);
                            l.getBlock().setType(Material.BED_BLOCK);
                            Block block = gameTeam.getBedHead();
                            BlockState bedFoot = block.getState();
                            BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.SOUTH).getState();
                            bedFoot.setType(Material.BED_BLOCK);
                            bedHead.setType(Material.BED_BLOCK);
                            bedFoot.setRawData((byte) 0);
                            bedHead.setRawData((byte) 8);
                            bedFoot.update(true, false);
                            bedHead.update(true, true);
                        } else if (face == BlockFace.EAST) {
                            Location l = gameTeam.getBedHead().getLocation();
                            l.getBlock().setType(Material.AIR);
                            l.getBlock().setType(Material.BED_BLOCK);
                            Block block = gameTeam.getBedHead();
                            BlockState bedFoot = block.getState();
                            BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.WEST).getState();
                            bedFoot.setType(Material.BED_BLOCK);
                            bedHead.setType(Material.BED_BLOCK);
                            bedFoot.setRawData((byte) 1);
                            bedHead.setRawData((byte) 9);
                            bedFoot.update(true, false);
                            bedHead.update(true, true);
                        } else if (face == BlockFace.SOUTH) {
                            Location l = gameTeam.getBedHead().getLocation();
                            l.getBlock().setType(Material.AIR);
                            l.getBlock().setType(Material.BED_BLOCK);
                            Block block = gameTeam.getBedHead();
                            BlockState bedFoot = block.getState();
                            BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.NORTH).getState();
                            bedFoot.setType(Material.BED_BLOCK);
                            bedHead.setType(Material.BED_BLOCK);
                            bedFoot.setRawData((byte) 2);
                            bedHead.setRawData((byte) 10);
                            bedFoot.update(true, false);
                            bedHead.update(true, true);
                        } else if (face == BlockFace.WEST) {
                            Location l = gameTeam.getBedHead().getLocation();
                            l.getBlock().setType(Material.AIR);
                            l.getBlock().setType(Material.BED_BLOCK);
                            Block block = gameTeam.getBedHead();
                            BlockState bedFoot = block.getState();
                            BlockState bedHead = bedFoot.getBlock().getRelative(BlockFace.EAST).getState();
                            bedFoot.setType(Material.BED_BLOCK);
                            bedHead.setType(Material.BED_BLOCK);
                            bedFoot.setRawData((byte) 3);
                            bedHead.setRawData((byte) 11);
                            bedFoot.update(true, false);
                            bedHead.update(true, true);
                        }

                        if (player.getItemInHand().getAmount() == 1) {
                            player.getInventory().setItemInHand(null);
                        } else {
                            player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount() - 1);
                        }

                        gameTeam.setBedDestroy(false);
                        gameTeam.setUnbed(true);

                        player.sendMessage("§a使用回春床成功!");
                        game.broadcastSound(SoundUtil.get("ENDERDRAGON_HIT", "ENTITY_ENDERDRAGON_HURT"), 10, 10);
                        game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
                        game.broadcastMessage(" ");
                        game.broadcastMessage(gameTeam.getChatColor() + gameTeam.getName() + " §c使用了回春床！");
                        game.broadcastMessage(" ");
                        game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
                        return;
                    case FIREBALL:
                        event.setCancelled(true);
                        if (gamePlayer.isSpectator()) {
                            return;
                        }

                        if (Math.abs(System.currentTimeMillis() - (player.hasMetadata("Game FIREBALL TIMER") ? player.getMetadata("Game FIREBALL TIMER").get(0).asLong() : 0L)) < 1000) {
                            return;
                        }

                        if (player.getItemInHand().getAmount() == 1) {
                            player.getInventory().setItemInHand(null);
                        } else {
                            player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount() - 1);
                        }

                        player.setMetadata("Game FIREBALL TIMER", new FixedMetadataValue(Bedwars.getInstance(), System.currentTimeMillis()));

                        Fireball fireball = player.launchProjectile(Fireball.class);
                        fireball.setVelocity(fireball.getVelocity().multiply(2));
                        fireball.setYield(3.0F);
                        fireball.setBounce(false);
                        fireball.setIsIncendiary(false);
                        fireball.setMetadata("Game FIREBALL", new FixedMetadataValue(Bedwars.getInstance(), player.getUniqueId()));
                        return;
                    default:
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem().getItemStack();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        PlayerData playerData = gamePlayer.getPlayerData();

        if (gamePlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        if (itemStack.getType() == Material.BED || itemStack.getType() == Material.BED_BLOCK) {
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().getDisplayName() != null) {
                return;
            }

            event.setCancelled(true);
            event.getItem().remove();
        }

        if (itemStack.getType() == Material.CLAY_BRICK || itemStack.getType() == Material.IRON_INGOT || itemStack.getType() == Material.GOLD_INGOT) {
            double xp = itemStack.getAmount();

            if (itemStack.getType() == Material.IRON_INGOT) {
                xp = xp * 10;
            }else if (itemStack.getType() == Material.GOLD_INGOT) {
                xp = xp * 50;
            }

            if (player.hasPermission("bw.xp.vip1")) {
                if (itemStack.getType() == Material.CLAY_BRICK) {
                    xp += xp * 1.3D;
                } else if (itemStack.getType() == Material.IRON_INGOT) {
                    xp += xp * 1.1D;
                } else if (itemStack.getType() == Material.GOLD_INGOT) {
                    xp += xp * 1.0D;
                }
            } else if (player.hasPermission("bw.xp.vip2")) {
                if (itemStack.getType() == Material.CLAY_BRICK) {
                    xp += xp * 1.4D;
                } else if (itemStack.getType() == Material.IRON_INGOT) {
                    xp += xp * 1.3D;
                } else if (itemStack.getType() == Material.GOLD_INGOT) {
                    xp += xp * 1.2D;
                }
            } else if (player.hasPermission("bw.xp.vip3")) {
                if (itemStack.getType() == Material.CLAY_BRICK) {
                    xp += xp * 1.6D;
                } else if (itemStack.getType() == Material.IRON_INGOT) {
                    xp += xp * 1.5D;
                } else if (itemStack.getType() == Material.GOLD_INGOT) {
                    xp += xp * 1.4D;
                }
            } else if (player.hasPermission("bw.xp.vip4")) {
                if (itemStack.getType() == Material.CLAY_BRICK) {
                    xp += xp * 1.8D;
                } else if (itemStack.getType() == Material.IRON_INGOT) {
                    xp += xp * 1.7D;
                } else if (itemStack.getType() == Material.GOLD_INGOT) {
                    xp += xp * 1.6D;
                }
            }

            if (playerData.getModeType() == ModeType.DEFAULT) {
                event.setCancelled(true);
                event.getItem().remove();

                player.playSound(player.getLocation(), SoundUtil.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);
                player.getInventory().addItem(new ItemStack(itemStack.getType(), itemStack.getAmount()));
            } else if (playerData.getModeType() == ModeType.EXPERIENCE) {
                event.setCancelled(true);
                event.getItem().remove();

                player.playSound(player.getLocation(), SoundUtil.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 10, 15);
                player.setLevel((int) (player.getLevel() + xp));
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.startsWith("/report")) {
            return;
        }

        if (message.startsWith("/queue join qc jdqc")) {
            return;
        }

        if (BukkitReport.getInstance().getStaffs().containsKey(player.getName())) {
            if (event.getMessage().startsWith("/wnm") || event.getMessage().startsWith("/staff")) {
                return;
            }
        }

        if (!player.hasPermission("bw.*")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            event.setCancelled(true);
            if (gamePlayer.isSpectator()) {
                return;
            }

            NewItemShop itemShop = gamePlayer.getNewItemShop();
            if (itemShop == null) {
                itemShop = new NewItemShop(MerchantCategoryManager.getCategories(), game);
            }

            itemShop.setCurrentCategory(null);
            itemShop.openCategoryInventory(player);
            gamePlayer.setNewItemShop(itemShop);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (event.getItem().getType() != Material.POTION) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getInventory().getItemInHand().getType() == Material.GLASS_BOTTLE) {
                    player.getInventory().setItemInHand(new ItemStack(Material.AIR));
                }
            }
        }.runTaskLater(Bedwars.getInstance(), 0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        GamePlayer gamePlayer = GamePlayer.get(event.getPlayer().getUniqueId());
        if (gamePlayer.isSpectator() && game.getGameState() == GameState.RUNNING) {
            if (gamePlayer.isSpectator() && event.getRightClicked() instanceof Player && SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRSTPERSON)) {
                event.setCancelled(true);
                if (GamePlayer.get(event.getRightClicked().getUniqueId()).isSpectator()) {
                    return;
                }

                gamePlayer.sendTitle(0, 20, 0, "§a正在旁观§7" + event.getRightClicked().getName(), "§a点击左键打开菜单  §c按Shift键退出");
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                event.getPlayer().setSpectatorTarget(event.getRightClicked());
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (gamePlayer.isSpectator() && (SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRSTPERSON)) && player.getGameMode() == GameMode.SPECTATOR) {
            gamePlayer.sendTitle(0, 20, 0, "§e退出旁观模式", "");
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);
            return;
        }

        if (player.hasMetadata("等待上一次求救")) {
            return;
        }

        if (player.getLocation().getPitch() > -80) {
            return;
        }

        player.setMetadata("等待上一次求救", new FixedMetadataValue(Bedwars.getInstance(), ""));


        GameTeam gameTeam = gamePlayer.getGameTeam();

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i > 5) {
                    player.removeMetadata("等待上一次求救", Bedwars.getInstance());
                    cancel();
                    return;
                }

                game.broadcastTeamTitle(gameTeam, 0, 8, 0, "", gameTeam.getChatColor() + gamePlayer.getDisplayname() + " 说: §c注意,我们的床有危险！");
                game.broadcastTeamSound(gameTeam, SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);
                i++;
            }
        }.runTaskTimer(Bedwars.getInstance(), 0, 10L);
    }
}
