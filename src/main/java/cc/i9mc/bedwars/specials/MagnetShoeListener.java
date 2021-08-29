package cc.i9mc.bedwars.specials;

import cc.i9mc.bedwars.Bedwars;
import cc.i9mc.bedwars.game.Game;
import cc.i9mc.bedwars.game.GameState;
import cc.i9mc.bedwars.utils.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class MagnetShoeListener implements Listener {
    private static final Game game = Bedwars.getInstance().getGame();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent ev) {
        if (ev.isCancelled()) {
            return;
        }

        if (!(ev.getEntity() instanceof Player)) {
            return;
        }

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        Player player = (Player) ev.getEntity();
        ItemStack boots = player.getInventory().getBoots();

        if (boots == null) {
            return;
        }

        MagnetShoe shoe = new MagnetShoe();
        if (boots.getType() != shoe.getItemMaterial()) {
            return;
        }

        if (this.rollKnockbackDice()) {
            ev.setCancelled(true);
            player.damage(ev.getDamage());
        }
    }

    private boolean rollKnockbackDice() {
        int roll = Util.randInt(0, 100);
        return (roll <= 70);
    }

}
