package cc.i9mc.bedwars.game;

import lombok.Data;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Bed;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameTeam {
    private final TeamColor teamColor;
    private final Location spawn;
    private final Block bedFeet;
    private final Block bedHead;
    private final BlockFace bedFace;
    private int maxPlayers;
    private boolean unbed;
    private boolean bedDestroy;
    private GamePlayer destroyPlayer;

    private List<Block> teamChests;
    private Inventory inventory;

    public GameTeam(TeamColor teamColor, Location location, int maxPlayers) {
        this.unbed = false;

        this.spawn = location;
        this.teamColor = teamColor;
        this.maxPlayers = maxPlayers;

        this.teamChests = new ArrayList<>();

        List<Block> blocks = new ArrayList<>();
        for (int x = -20; x < 20; x++) {
            for (int y = -20; y < 20; y++) {
                for (int z = -20; z < 20; z++) {
                    Block block = spawn.clone().add(x, y, z).getBlock();
                    if (block != null && block.getType() == Material.BED_BLOCK) {
                        blocks.add(block);
                    }
                }
            }
        }

        Bed bedBlock = (Bed) blocks.get(0).getState().getData();
        if (!bedBlock.isHeadOfBed()) {
            bedFeet = blocks.get(0);
            bedHead = blocks.get(1);
        } else {
            bedHead = blocks.get(0);
            bedFeet = blocks.get(1);
        }
        bedFace = ((Bed) bedHead.getState().getData()).getFacing();
    }

    public ChatColor getChatColor() {
        return teamColor.getChatColor();
    }

    public DyeColor getDyeColor() {
        return teamColor.getDyeColor();
    }

    public Color getColor() {
        return teamColor.getColor();
    }

    public List<GamePlayer> getGamePlayers() {
        List<GamePlayer> gamePlayers = new ArrayList<>();
        for (GamePlayer gamePlayer : GamePlayer.getGamePlayers()) {
            if (gamePlayer.getGameTeam() == this) {
                gamePlayers.add(gamePlayer);
            }
        }

        return gamePlayers;
    }

    public List<GamePlayer> getAlivePlayers() {
        List<GamePlayer> alivePlayers = new ArrayList<>();
        for (GamePlayer gamePlayer : getGamePlayers()) {
            if (gamePlayer.isOnline() && !gamePlayer.isSpectator()) {
                alivePlayers.add(gamePlayer);
            }
        }
        return alivePlayers;
    }

    public boolean isInTeam(GamePlayer gamePlayer) {
        for (GamePlayer player : getGamePlayers()) {
            if (player.equals(gamePlayer)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInTeam(GamePlayer removePlayer, GamePlayer gamePlayer) {
        for (GamePlayer player : getGamePlayers()) {
            if (player.equals(gamePlayer) && !player.equals(removePlayer)) {
                return true;
            }
        }
        return false;
    }

    public boolean addPlayer(GamePlayer gamePlayer) {
        if (isFull() || isInTeam(gamePlayer)) {
            return false;
        }
        gamePlayer.setGameTeam(this);
        return true;
    }

    public boolean isFull() {
        return getGamePlayers().size() >= maxPlayers;
    }

    public boolean isDead() {
        for (GamePlayer gamePlayer : getGamePlayers()) {
            if ((gamePlayer.isOnline()) && (!gamePlayer.isSpectator())) {
                return false;
            }
        }
        return true;
    }

    public void equipPlayerWithLeather(Player player) {
        // helmet
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setColor(this.getColor());
        helmet.setItemMeta(meta);

        // chestplate
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        meta = (LeatherArmorMeta) chestplate.getItemMeta();
        meta.setColor(this.getColor());
        chestplate.setItemMeta(meta);

        // leggings
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        meta = (LeatherArmorMeta) leggings.getItemMeta();
        meta.setColor(this.getColor());
        leggings.setItemMeta(meta);

        // boots
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
        meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(this.getColor());
        boots.setItemMeta(meta);

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
        player.updateInventory();
    }

    public String getName() {
        return teamColor.getName();
    }

    public void removeChest(Block chest) {
        teamChests.remove(chest);
        if (teamChests.size() == 0) {
            this.setInventory(null);
        }
    }

    public void createTeamInventory() {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.ENDER_CHEST, "队伍箱子");
        this.setInventory(inventory);
    }
}