package me.isach.ultracosmetics.cosmetics.gadgets;

import me.isach.ultracosmetics.Core;
import me.isach.ultracosmetics.config.MessageManager;
import me.isach.ultracosmetics.config.SettingsManager;
import me.isach.ultracosmetics.listeners.MenuListener;
import me.isach.ultracosmetics.util.Cuboid;
import me.isach.ultracosmetics.util.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Created by sacha on 03/08/15.
 */
public abstract class Gadget implements Listener {

    public boolean useTwoInteractMethods;
    private Material material;
    private Byte data;
    private String configName;
    private Inventory inv;
    public boolean openGadgetsInvAfterAmmo;
    private double countdown;

    private boolean requireAmmo;

    private Listener listener;

    private GadgetType type;

    public boolean displayCountdownMessage = true;

    private String permission;

    private UUID owner;

    public Gadget(Material material, Byte data, String configName, String permission, double countdown, final UUID owner, final GadgetType type) {
        this.material = material;
        this.data = data;
        this.configName = configName;
        this.permission = permission;
        if (SettingsManager.getConfig().get("Gadgets." + configName + ".Cooldown") == null) {
            this.countdown = countdown;
            SettingsManager.getConfig().set("Gadgets." + configName + ".Cooldown", countdown);
        } else {
            this.countdown = Double.valueOf(String.valueOf(SettingsManager.getConfig().get("Gadgets." + configName + ".Cooldown")));
        }
        this.type = type;
        this.useTwoInteractMethods = false;
        if (owner != null) {
            this.owner = owner;
            if (Core.getCustomPlayer(getPlayer()).currentGadget != null)
                Core.getCustomPlayer(getPlayer()).removeGadget();
            if (!getPlayer().hasPermission(permission)) {
                getPlayer().sendMessage(MessageManager.getMessage("No-Permission"));
                return;
            }
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (Bukkit.getPlayer(owner) != null
                                && Core.getCustomPlayer(Bukkit.getPlayer(owner)).currentGadget != null
                                && Core.getCustomPlayer(Bukkit.getPlayer(owner)).currentGadget.getType() == type) {
                            onUpdate();
                        } else {
                            cancel();
                            unregister();
                        }
                    } catch (NullPointerException exc) {
                        removeItem();
                        clear();
                        getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Unequip").replace("%gadgetname%", getName()));
                        cancel();
                    }
                }
            };
            runnable.runTaskTimer(Core.getPlugin(), 0, 1);
            listener = new GadgetListener(this);
            Core.registerListener(listener);
            if (getPlayer().getInventory().getItem((int) SettingsManager.getConfig().get("Gadget-Slot")) != null) {
                getPlayer().getWorld().dropItem(getPlayer().getLocation(), getPlayer().getInventory().getItem((int) SettingsManager.getConfig().get("Gadget-Slot")));
                getPlayer().getInventory().remove((int) SettingsManager.getConfig().get("Gadget-Slot"));
            }
            if (Core.isAmmoEnabled() && getType().requiresAmmo()) {
                getPlayer().getInventory().setItem((int) SettingsManager.getConfig().get("Gadget-Slot"), ItemFactory.create(material, data, "§f§l" + Core.getCustomPlayer(getPlayer()).getAmmo(type.toString().toLowerCase()) + " " + getName(), "§9Gadget"));
            } else {
                getPlayer().getInventory().setItem((int) SettingsManager.getConfig().get("Gadget-Slot"), ItemFactory.create(material, data, getName(), "§9Gadget"));
            }
            getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Equip").replace("%gadgetname%", getName()));
            Core.getCustomPlayer(getPlayer()).currentGadget = this;
        }
        this.requireAmmo = Boolean.valueOf(String.valueOf(SettingsManager.getConfig().get("Gadgets." + configName + ".Ammo.Enabled")));
    }

    public String getName() {
        return MessageManager.getMessage("Gadgets." + configName + ".name");
    }

    public Material getMaterial() {
        return this.material;
    }

    public GadgetType getType() {
        return this.type;
    }

    public Byte getData() {
        return this.data;
    }

    abstract void onInteractRightClick();

    abstract void onInteractLeftClick();

    abstract void onUpdate();


    public abstract void clear();

    public void unregister() {
        try {
            HandlerList.unregisterAll(this);
            HandlerList.unregisterAll(listener);
        } catch (Exception exc) {
        }
    }

    protected UUID getOwner() {
        return owner;
    }

    protected Player getPlayer() {
        return Bukkit.getPlayer(owner);
    }

    public void removeItem() {
        getPlayer().getInventory().setItem((int) SettingsManager.getConfig().get("Gadget-Slot"), null);
    }

    public int getPrice() {
        return (int) SettingsManager.getConfig().get("Gadgets." + configName + ".Ammo.Price");
    }

    public int getResultAmmoAmount() {
        return (int) SettingsManager.getConfig().get("Gadgets." + configName + ".Ammo.Result-Amount");
    }

    public void buyAmmo() {

        Inventory inventory = Bukkit.createInventory(null, 54, MessageManager.getMessage("Menus.Buy-Ammo"));

        inventory.setItem(13, ItemFactory.create(material, data, MessageManager.getMessage("Buy-Ammo-Description").replace("%amount%", "" + getResultAmmoAmount()).replace("%price%", "" + getPrice()).replaceAll("%gadgetname%", getName())));

        for (int i = 27; i < 30; i++) {
            inventory.setItem(i, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
            inventory.setItem(i + 9, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
            inventory.setItem(i + 18, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
            inventory.setItem(i + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
            inventory.setItem(i + 9 + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
            inventory.setItem(i + 18 + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
        }


        getPlayer().openInventory(inventory);
        getPlayer().getOpenInventory().setCursor(getPlayer().getOpenInventory().getItem(49));

        this.inv = inventory;
    }

    public class GadgetListener implements Listener {
        private Gadget gadget;

        public GadgetListener(Gadget gadget) {
            this.gadget = gadget;
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if (event.getPlayer() == getPlayer() && inv != null && isSameInventory(event.getInventory(), inv)) {
                inv = null;
                openGadgetsInvAfterAmmo = false;
                return;
            }
        }

        @EventHandler
        public void onInventoryClickAmmo(final InventoryClickEvent event) {
            if (event.getWhoClicked() == getPlayer() && inv != null && isSameInventory(event.getWhoClicked().getOpenInventory().getTopInventory(), inv)) {
                event.setCancelled(true);
                if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {
                    String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
                    String purchase = MessageManager.getMessage("Purchase");
                    String cancel = MessageManager.getMessage("Cancel");
                    if (displayName.equals(purchase)) {
                        if (Core.economy.getBalance((Player) event.getWhoClicked()) >= getPrice()) {
                            Core.economy.withdrawPlayer((Player) event.getWhoClicked(), getPrice());
                            Core.getCustomPlayer((Player) event.getWhoClicked()).addAmmo(type.toString().toLowerCase(), getResultAmmoAmount());
                            event.getWhoClicked().sendMessage(MessageManager.getMessage("Successful-Purchase"));
                            if (openGadgetsInvAfterAmmo)
                                Bukkit.getScheduler().runTaskLater(Core.getPlugin(), new Runnable() {
                                    @Override
                                    public void run() {
                                        MenuListener.openGadgetsMenu((Player) event.getWhoClicked());
                                        openGadgetsInvAfterAmmo = false;
                                    }
                                }, 1);
                        } else {
                            getPlayer().sendMessage(MessageManager.getMessage("Not-Enough-Money"));
                        }
                        event.getWhoClicked().closeInventory();
                    } else if (displayName.equals(cancel)) {
                        event.getWhoClicked().closeInventory();
                    }
                }
            }
        }

        public boolean isSameInventory(Inventory first, Inventory second) {
            return ((CraftInventory) first).getInventory().equals(((CraftInventory) second).getInventory());
        }

        @EventHandler
        protected void onPlayerInteract(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            if (!uuid.equals(gadget.owner)) return;
            ItemStack itemStack = player.getItemInHand();
            if (itemStack.getType() != gadget.material) return;
            if (itemStack.getData().getData() != gadget.data) return;
            if (player.getInventory().getHeldItemSlot() != (int) SettingsManager.getConfig().get("Gadget-Slot")) return;
            if (Core.getCustomPlayer(getPlayer()).currentGadget != gadget) return;
            if (event.getAction() == Action.PHYSICAL) return;
            event.setCancelled(true);
            player.updateInventory();
            if (!Core.getCustomPlayer(getPlayer()).hasGadgetsEnabled()) {
                getPlayer().sendMessage(MessageManager.getMessage("Gadgets-Enabled-Needed"));
                return;
            }
            if (Core.getCustomPlayer(getPlayer()).currentTreasureChest != null) {
                return;
            }
            if (Core.isAmmoEnabled() && getType().requiresAmmo()) {
                if (Core.getCustomPlayer(getPlayer()).getAmmo(getType().toString().toLowerCase()) < 1) {
                    buyAmmo();
                    return;
                }
            }
            if (type == GadgetType.PORTALGUN) {
                if (getPlayer().getTargetBlock((Set<Material>) null, 20).getType() == Material.AIR) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.PortalGun.No-Block-Range"));
                    return;
                }
            }
            if (type == GadgetType.ROCKET) {
                boolean pathClear = true;
                Cuboid c = new Cuboid(getPlayer().getLocation().add(-1, 0, -1), getPlayer().getLocation().add(1, 75, 1));
                if (!c.isEmpty()) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Rocket.Not-Enough-Space"));
                    return;
                }
                if (!getPlayer().isOnGround()) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Rocket.Not-On-Ground"));
                    return;
                }
            }
            if (type == GadgetType.DISCOBALL) {
                if (Core.discoBalls.size() > 0) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.DiscoBall.Already-Active"));
                    return;
                }
                if (getPlayer().getLocation().add(0, 4, 0).getBlock() != null && getPlayer().getLocation().add(0, 4, 0).getBlock().getType() != Material.AIR) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.DiscoBall.Not-Space-Above"));
                    return;
                }
            }
            if (type == GadgetType.EXPLOSIVESHEEP) {
                if (Core.explosiveSheep.size() > 0) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.ExplosiveSheep.Already-Active"));
                    return;
                }
            }
            if (Core.countdownMap.get(getPlayer()) != null) {
                if (Core.countdownMap.get(getPlayer()).containsKey(getType())) {
                    String timeLeft = new DecimalFormat("0.0").format(Core.countdownMap.get(getPlayer()).get(getType()));
                    if (displayCountdownMessage)
                        getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Countdown-Message").replace("%gadgetname%", getName()).replace("%time%", timeLeft));
                    return;
                } else {
                    Core.countdownMap.get(getPlayer()).put(getType(), countdown);
                }
            } else {
                Core.countdownMap.remove(getPlayer());
                HashMap<GadgetType, Double> countdownMap = new HashMap<>();
                countdownMap.put(getType(), countdown);
                Core.countdownMap.put(getPlayer(), countdownMap);
            }
            if (Core.isAmmoEnabled() && getType().requiresAmmo()) {
                Core.getCustomPlayer(getPlayer()).removeAmmo(getType().toString().toLowerCase());
                getPlayer().getInventory().setItem((int) SettingsManager.getConfig().get("Gadget-Slot"), ItemFactory.create(material, data, "§f§l" + Core.getCustomPlayer(getPlayer()).getAmmo(type.toString().toLowerCase()) + " " + getName(), "§9Gadget"));
            }
            if (useTwoInteractMethods) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR
                        || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                    onInteractRightClick();
                else if (event.getAction() == Action.LEFT_CLICK_BLOCK
                        || event.getAction() == Action.LEFT_CLICK_AIR)
                    onInteractLeftClick();
            } else {
                onInteractRightClick();
            }

        }

        @EventHandler
        protected void onItemDrop(PlayerDropItemEvent event) {
            if (event.getItemDrop().getItemStack().getType() == material) {
                if (event.getItemDrop().getItemStack().getData().getData() == data) {
                    if (event.getItemDrop().getItemStack().getItemMeta().hasDisplayName()) {
                        if (event.getItemDrop().getItemStack().getItemMeta().getDisplayName().endsWith(getName())) {
                            if (SettingsManager.getConfig().get("Remove-Gadget-With-Drop")) {
                                Core.getCustomPlayer(getPlayer()).removeGadget();
                                event.getItemDrop().remove();
                                return;
                            }
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }

        @EventHandler
        protected void onInventoryClick(InventoryClickEvent event) {
            if (event.getCurrentItem() != null
                    && event.getCurrentItem().getType() == material
                    && event.getCurrentItem().getData().getData() == data
                    && event.getCurrentItem().getItemMeta().hasDisplayName()
                    && event.getCurrentItem().getItemMeta().getDisplayName().endsWith(getName())) {
                event.setCancelled(true);
            }
        }
    }

    public enum GadgetType {
        BATBLASTER("ultracosmetics.gadgets.batblaster", "BatBlaster"),
        CHICKENATOR("ultracosmetics.gadgets.chickenator", "Chickenator"),
        COLORBOMB("ultracosmetics.gadgets.colorbomb", "ColorBomb"),
        DISCOBALL("ultracosmetics.gadgets.discoball", "DiscoBall"),
        ETHEREALPEARL("ultracosmetics.gadgets.etherealpearl", "EtherealPearl"),
        FLESHHOOK("ultracosmetics.gadgets.fleshhook", "FleshHook"),
        MELONTHROWER("ultracosmetics.gadgets.melonthrower", "MelonThrower"),
        BLIZZARDBLASTER("ultracosmetics.gadgets.blizzardblaster", "BlizzardBlaster"),
        PORTALGUN("ultracosmetics.gadgets.portalgun", "PortalGun"),
        EXPLOSIVESHEEP("ultracosmetics.gadgets.explosivesheep", "ExplosiveSheep"),
        PAINTBALLGUN("ultracosmetics.gadgets.paintballgun", "PaintballGun"),
        THORHAMMER("ultracosmetics.gadgets.thorhammer", "ThorHammer"),
        ANTIGRAVITY("ultracosmetics.gadgets.antigravity", "AntiGravity"),
        SMASHDOWN("ultracosmetics.gadgets.smashdown", "SmashDown"),
        ROCKET("ultracosmetics.gadgets.rocket", "Rocket"),
        BLACKHOLE("ultracosmetics.gadgets.blackhole", "BlackHole"),
        TSUNAMI("ultracosmetics.gadgets.tsunami", "Tsunami"),
        TNT("ultracosmetics.gadgets.tnt", "TNT");

        String permission;
        public String configName;

        GadgetType(String permission, String configName) {
            this.permission = permission;
            this.configName = configName;
        }

        public boolean requiresAmmo() {
            return SettingsManager.getConfig().get("Gadgets." + configName + ".Ammo.Enabled");
        }

        public String getConfigName() {
            return configName;
        }

        public String getPermission() {
            return permission;
        }

        public boolean isEnabled() {
            return SettingsManager.getConfig().get("Gadgets." + configName + ".Enabled");
        }
    }

}
