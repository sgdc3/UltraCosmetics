package me.isach.ultracosmetics.cosmetics.morphs;

import me.isach.ultracosmetics.Core;
import me.isach.ultracosmetics.config.MessageManager;
import me.isach.ultracosmetics.util.MathUtils;
import me.isach.ultracosmetics.util.UtilParticles;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Created by sacha on 27/08/15.
 */
public class MorphCreeper extends Morph {

    private double charge = 0;

    public MorphCreeper(UUID owner) {
        super(DisguiseType.CREEPER, Material.SULPHUR, (byte) 0, "Creeper", "ultracosmetics.morphs.creeper", owner, MorphType.CREEPER);

        if (owner != null) {
            CreeperWatcher creeperWatcher = (CreeperWatcher) disguise.getWatcher();

            final MorphCreeper creeper = this;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (getPlayer() == null
                            || Core.getCustomPlayer(getPlayer()).currentMorph != creeper) {
                        cancel();
                        return;
                    }
                    final CreeperWatcher creeperWatcher = (CreeperWatcher) disguise.getWatcher();
                    if (getPlayer().isSneaking()) {
                        creeperWatcher.setIgnited(true);
                        if (charge + 4 <= 100)
                            charge += 4;
                        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.CREEPER_HISS, 0.2f, 1);
                    } else {
                        if (creeperWatcher.isIgnited()) {
                            disguise = new MobDisguise(disguiseType);
                            DisguiseAPI.disguiseToAll(getPlayer(), disguise);
                            disguise.setShowName(true);
                            if (!Core.getCustomPlayer(getPlayer()).canSeeSelfMorph())
                                disguise.setViewSelfDisguise(false);
                        }
                        if (charge == 100) {
                            UtilParticles.play(getPlayer().getLocation(), Effect.EXPLOSION_HUGE);
                            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.EXPLODE, 1, 1);

                            for (Entity ent : getPlayer().getNearbyEntities(3, 3, 3)) {
                                if (ent instanceof Creature || ent instanceof Player) {
                                    double dX = getPlayer().getLocation().getX() - ent.getLocation().getX();
                                    double dY = getPlayer().getLocation().getY() - ent.getLocation().getY();
                                    double dZ = getPlayer().getLocation().getZ() - ent.getLocation().getZ();
                                    double yaw = Math.atan2(dZ, dX);
                                    double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
                                    double X = Math.sin(pitch) * Math.cos(yaw);
                                    double Y = Math.sin(pitch) * Math.sin(yaw);
                                    double Z = Math.cos(pitch);

                                    Vector vector = new Vector(X, Z, Y);
                                    MathUtils.applyVelocity(ent, vector.multiply(1.3D).add(new Vector(0, 1.4D, 0)));
                                }
                            }
                            sendActionBar(getPlayer(), "");
                            charge = 0;
                            return;
                        }
                        if (charge > 0)
                            charge -= 4;
                    }
                    if (charge > 0 && charge < 100) {
                        if (charge < 5) {
                            sendActionBar(getPlayer(), "");
                        } else
                            sendActionBar(getPlayer(), MessageManager.getMessage("Morphs.Creeper.charging").replace("%chargelevel%", charge + ""));
                    } else if (charge == 100)
                        sendActionBar(getPlayer(), MessageManager.getMessage("Morphs.Creeper.release-to-explode"));


                }
            }.runTaskTimer(Core.getPlugin(), 0, 1);
        }
    }

    public static void sendActionBar(Player player, String message) {
        CraftPlayer p = (CraftPlayer) player;
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
    }
}
