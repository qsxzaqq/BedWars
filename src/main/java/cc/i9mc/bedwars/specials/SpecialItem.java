package cc.i9mc.bedwars.specials;

import cc.i9mc.bedwars.Bedwars;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public abstract class SpecialItem {
    @Getter
    private static final List<Class<? extends SpecialItem>> availableSpecials = new ArrayList<>();

    public static void loadSpecials() {
        SpecialItem.availableSpecials.add(MagnetShoe.class);
        SpecialItem.availableSpecials.add(ProtectionWall.class);
        SpecialItem.availableSpecials.add(TNTSheep.class);
        SpecialItem.availableSpecials.add(RescuePlatform.class);
        SpecialItem.availableSpecials.add(WarpPowder.class);
        Bedwars.getInstance().getServer().getPluginManager().registerEvents(new MagnetShoeListener(), Bedwars.getInstance());
        Bedwars.getInstance().getServer().getPluginManager().registerEvents(new ProtectionWallListener(), Bedwars.getInstance());
        Bedwars.getInstance().getServer().getPluginManager().registerEvents(new TNTSheepListener(), Bedwars.getInstance());
        Bedwars.getInstance().getServer().getPluginManager().registerEvents(new RescuePlatformListener(), Bedwars.getInstance());
        Bedwars.getInstance().getServer().getPluginManager().registerEvents(new WarpPowderListener(), Bedwars.getInstance());
    }

    public abstract Material getActivatedMaterial();

    public abstract Material getItemMaterial();

}
