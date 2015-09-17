package me.isach.ultracosmetics;

import me.isach.ultracosmetics.config.MessageManager;
import me.isach.ultracosmetics.config.SettingsManager;
import me.isach.ultracosmetics.cosmetics.gadgets.Gadget;
import me.isach.ultracosmetics.cosmetics.morphs.Morph;
import me.isach.ultracosmetics.cosmetics.mounts.Mount;
import me.isach.ultracosmetics.cosmetics.particleeffects.ParticleEffect;
import me.isach.ultracosmetics.cosmetics.pets.Pet;
import me.isach.ultracosmetics.cosmetics.treasurechests.TreasureChest;
import me.isach.ultracosmetics.util.ItemFactory;
import me.libraryaddict.disguise.DisguiseAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Created by sacha on 03/08/15.
 */
public class CustomPlayer {

    public UUID uuid;
    public Gadget currentGadget = null;
    public Mount currentMount;
    public ParticleEffect currentParticleEffect;
    public Pet currentPet;
    public TreasureChest currentTreasureChest;
    public Morph currentMorph;

    public CustomPlayer(UUID uuid) {
        try {
            this.uuid = uuid;
            Core.countdownMap.put(getPlayer(), null);
            SettingsManager.getData(getPlayer());
            if (Core.usingFileStorage())
                SettingsManager.getData(getPlayer()).addDefault("Keys", 0);

            if (Core.isAmmoEnabled()) {
                if (!Core.usingFileStorage()) {
                    Core.sqlUtils.initStats(getPlayer());
                } else {
                    for (Gadget g : Core.getGadgets()) {
                        if (g.getType().isEnabled()) {
                            SettingsManager.getData(getPlayer()).addDefault("Ammo." + g.getType().toString().toLowerCase(), 0);
                        }
                    }
                }
            }
            if (Core.usingFileStorage()) {
                SettingsManager.getData(getPlayer()).addDefault("Gadgets-Enabled", true);
                SettingsManager.getData(getPlayer()).addDefault("Third-Person-Morph-View", true);
            }
        } catch (Exception exc) {
            System.out.println("-------------------");
            System.out.println("An error happened with the custom players. (UltraCosmetics");
            System.out.println("");
            System.out.println("Please check first your MySQL settings or the data folder.");
            System.out.println("Report this error by PMing 'iSach' on http://spigotmc.org");
            System.out.println("");
            System.out.println("ERROR:");
            exc.getCause();
            System.out.println("");
            System.out.println("-------------------");
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }


    public void removeGadget() {
        if (currentGadget != null) {
            if (getPlayer() != null)
                getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Unequip").replace("%gadgetname%", currentGadget.getName()));
            currentGadget.removeItem();
            currentGadget.clear();
            currentGadget.unregister();
            currentGadget = null;
        }
    }

    public void removeMount() {
        if (currentMount != null) {
            currentMount.ent.remove();
            currentMount.clear();
            currentMount = null;
            getPlayer().removePotionEffect(PotionEffectType.CONFUSION);
        }
    }

    public void removePet() {
        if (currentPet != null) {
            if (currentPet.armorStand != null)
                currentPet.armorStand.remove();
            currentPet.ent.remove();
            currentPet.clear();
            currentPet = null;
        }
    }

    public void addKey() {
        if (Core.usingFileStorage())
            SettingsManager.getData(getPlayer()).set("Keys", getKeys() + 1);
        else
            Core.sqlUtils.addKey(getPlayer());
    }

    public void removeKey() {
        if (Core.usingFileStorage())
            SettingsManager.getData(getPlayer()).set("Keys", getKeys() - 1);
        else
            Core.sqlUtils.removeKey(getPlayer());
    }

    public int getKeys() {
        if (Core.usingFileStorage()) {
            return (int) SettingsManager.getData(getPlayer()).get("Keys");
        } else {
            return Core.sqlUtils.getKeys(getPlayer());
        }
    }

    public void clear() {
        if (Core.Category.MORPHS.isEnabled() && Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            removeMorph();
            DisguiseAPI.undisguiseToAll(getPlayer());
        }
        removeGadget();
        removeParticleEffect();
        removePet();
        removeMount();
        removeTreasureChest();
    }

    public void openBuyKeyInventory() {

        try {
            final Inventory inventory = Bukkit.createInventory(null, 54, MessageManager.getMessage("Buy-Treasure-Key"));

            for (int i = 27; i < 30; i++) {
                inventory.setItem(i, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
                inventory.setItem(i + 9, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
                inventory.setItem(i + 18, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
                inventory.setItem(i + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
                inventory.setItem(i + 9 + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
                inventory.setItem(i + 18 + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
            }
            ItemStack itemStack = ItemFactory.create(Material.TRIPWIRE_HOOK, (byte) 0, ChatColor.translateAlternateColorCodes('&', ((String) SettingsManager.getMessages().get("Buy-Treasure-Key-ItemName")).replace("%price%", "" + (int) SettingsManager.getConfig().get("TreasureChests.Key-Price"))));
            inventory.setItem(13, itemStack);


            Bukkit.getScheduler().runTaskLater(Core.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    getPlayer().openInventory(inventory);
                }
            }, 3);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void removeParticleEffect() {
        if (currentParticleEffect != null) {
            getPlayer().sendMessage(MessageManager.getMessage("Particle-Effects.Unsummon").replace("%effectname%", currentParticleEffect.getName()));
            currentParticleEffect = null;
        }
    }

    public void removeMorph() {
        if (currentMorph != null) {
            DisguiseAPI.undisguiseToAll(getPlayer());
            currentMorph.clear();
            //getPlayer().sendMessage(MessageManager.getMessage("Morphs.Unmorph").replace("%morphname%", currentMorph.getName()));
            currentMorph = null;
        }
    }

    public int getMoney() {
        try {
            return (int) Core.economy.getBalance(getPlayer());
        } catch (Exception exc) {
            return 0;
        }
    }

    public void setPetName(String petName, String name) {
        if (Core.usingFileStorage()) {
            SettingsManager.getData(getPlayer()).set("Pet-Names." + petName, name);
        } else {
            Core.sqlUtils.setName(getPlayer(), petName, name);
        }
    }

    public String getPetName(String petName) {
        try {
            if (Core.usingFileStorage()) {
                return SettingsManager.getData(getPlayer()).get("Pet-Names." + petName);
            } else {
                if (Core.sqlUtils.getPetName(getPlayer(), petName).equalsIgnoreCase("Unknown")) {
                    return null;
                }
                return Core.sqlUtils.getPetName(getPlayer(), petName);
            }
        } catch (NullPointerException e) {
            return "Error";
        }
    }

    public void addAmmo(String name, int i) {
        if (Core.isAmmoEnabled()) {
            if (Core.usingFileStorage()) {
                SettingsManager.getData(getPlayer()).set("Ammo." + name, getAmmo(name) + i);
            } else {
                Core.sqlUtils.addAmmo(getPlayer(), name, i);
            }
        }
        if (currentGadget != null)
            getPlayer().getInventory().setItem((int) SettingsManager.getConfig().get("Gadget-Slot"), ItemFactory.create(currentGadget.getMaterial(), currentGadget.getData(), "§f§l" + Core.getCustomPlayer(getPlayer()).getAmmo(currentGadget.getType().toString().toLowerCase()) + " " + currentGadget.getName(), "§9Gadget"));


    }

    public void setGadgetsEnabled(Boolean enabled) {
        try {
            if (Core.usingFileStorage()) {
                SettingsManager.getData(getPlayer()).set("Gadgets-Enabled", enabled);
            } else {
                Core.sqlUtils.setGadgetsEnabled(getPlayer(), enabled);
            }

        } catch (NullPointerException e) {
        }
    }

    public boolean hasGadgetsEnabled() {
        try {
            if (Core.usingFileStorage()) {
                return SettingsManager.getData(getPlayer()).get("Gadgets-Enabled");
            } else {
                return Core.sqlUtils.hasGadgetsEnabled(getPlayer());
            }
        } catch (NullPointerException e) {
            return true;
        }
    }

    public void setSeeSelfMorph(Boolean enabled) {
        if (Core.usingFileStorage()) {
            SettingsManager.getData(getPlayer()).set("Third-Person-Morph-View", enabled);
        } else {
            Core.sqlUtils.setSeeSelfMorph(getPlayer(), enabled);
        }
    }

    public boolean canSeeSelfMorph() {
        try {
            if (Core.usingFileStorage()) {
                return SettingsManager.getData(getPlayer()).get("Third-Person-Morph-View");
            } else {
                return Core.sqlUtils.canSeeSelfMorph(getPlayer());
            }
        } catch (NullPointerException e) {
            return false;
        }
    }


    public int getAmmo(String name) {
        if (Core.isAmmoEnabled()) {
            if (Core.usingFileStorage()) {
                return (int) SettingsManager.getData(getPlayer()).get("Ammo." + name);
            } else {
                return Core.sqlUtils.getAmmo(getPlayer(), name);
            }
        }
        return 0;
    }

    public void removeTreasureChest() {
        if (currentTreasureChest == null) return;
        this.currentTreasureChest.clear();
        this.currentTreasureChest = null;
    }

    public void removeAmmo(String name) {
        if (Core.isAmmoEnabled()) {
            if (Core.usingFileStorage()) {
                SettingsManager.getData(getPlayer()).set("Ammo." + name, getAmmo(name) - 1);
            } else {
                Core.sqlUtils.removeAmmo(getPlayer(), name);
            }
        }
    }

    public UUID getUuid() {
        return uuid;
    }

}
