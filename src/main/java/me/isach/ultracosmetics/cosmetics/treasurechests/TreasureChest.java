package me.isach.ultracosmetics.cosmetics.treasurechests;

import me.isach.ultracosmetics.Core;
import me.isach.ultracosmetics.config.MessageManager;
import me.isach.ultracosmetics.util.ItemFactory;
import me.isach.ultracosmetics.util.MathUtils;
import me.isach.ultracosmetics.util.UtilParticles;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Chest;
import org.bukkit.material.EnderChest;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by sacha on 18/08/15.
 */
public abstract class TreasureChest implements Listener {

    Map<Location, Material> oldMaterials = new HashMap<>();
    Map<Location, Byte> oldDatas = new HashMap<>();
    ArrayList<Block> blocksToRestore = new ArrayList<>();
    ArrayList<Block> chests = new ArrayList<>();
    ArrayList<Block> chestsToRemove = new ArrayList<>();

    public UUID owner;
    public Material barriersMaterial;
    public Material b1Mat;
    public Material b2Mat;
    public Material b3Mat;
    public Material lampMaterial;

    public boolean enderChests = false;

    RandomGenerator randomGenerator;

    public Byte b1data = (byte) 0x0;
    public Byte b2data = (byte) 0x0;
    public Byte b3data = (byte) 0x0;
    public Byte barrierData = (byte) 0x0;

    Location center;
    Effect particleEffect;

    int chestsLeft = 4;

    private Player player;
    private List<Entity> items = new ArrayList<>();
    private List<Entity> holograms = new ArrayList<>();

    public TreasureChest(UUID owner, Material barriers, Material b1, Material b2, Material b3, final Material lamp, Effect effect) {

        if (owner == null) return;

        this.particleEffect = effect;
        this.owner = owner;
        this.barriersMaterial = barriers;
        this.b1Mat = b1;
        this.b2Mat = b2;
        this.b3Mat = b3;
        this.lampMaterial = lamp;

        Core.registerListener(this);

        this.player = getPlayer();

        BukkitRunnable runnable = new BukkitRunnable() {
            int i = 5;

            @Override
            public void run() {
                if (i == 0) {

                    BukkitRunnable runnable = new BukkitRunnable() {
                        int i = 4;

                        @Override
                        public void run() {
                            try {
                                randomGenerator = new RandomGenerator(getPlayer(), getPlayer().getLocation());
                                UtilParticles.playHelix(getChestLocation(i, center.clone()), 0, particleEffect);
                                UtilParticles.playHelix(getChestLocation(i, center.clone()), 3.5f, particleEffect);
                                Bukkit.getScheduler().runTaskLater(Core.getPlugin(), new Runnable() {
                                    @Override
                                    public void run() {
                                        Block b = getChestLocation(i, center.clone()).getBlock();
                                        b.setType(Material.CHEST);
                                        if (enderChests)
                                            b.setType(Material.ENDER_CHEST);
                                        getPlayer().playSound(getPlayer().getLocation(), Sound.ANVIL_LAND, 2, 1);
                                        for (int i = 0; i < 5; i++) {
                                            UtilParticles.play(b.getLocation(), Effect.LARGE_SMOKE);
                                            UtilParticles.play(b.getLocation(), Effect.LAVA_POP);
                                        }
                                        BlockFace blockFace = BlockFace.SOUTH;
                                        switch (i) {
                                            case 4:
                                                blockFace = BlockFace.SOUTH;
                                                break;
                                            case 3:
                                                blockFace = BlockFace.NORTH;
                                                break;
                                            case 2:
                                                blockFace = BlockFace.EAST;
                                                break;
                                            case 1:
                                                blockFace = BlockFace.WEST;
                                                break;
                                        }

                                        BlockState blockState = b.getState();
                                        if (enderChests) {
                                            EnderChest enderChest = (EnderChest) b.getState().getData();
                                            enderChest.setFacingDirection(blockFace);
                                            blockState.setData(enderChest);
                                        } else {
                                            Chest chest = (Chest) b.getState().getData();
                                            chest.setFacingDirection(blockFace);
                                            blockState.setData(chest);
                                        }
                                        blockState.update();

                                        chests.add(b);
                                        UtilParticles.play(getChestLocation(i, getPlayer().getLocation()), Effect.STEP_SOUND, b.getTypeId(), b.getData(), 0, 0, 0, 1, 50);
                                        i--;
                                        if (i == 0)
                                            cancel();
                                    }
                                }, 30);
                            } catch (Exception exc) {
                                clear();
                                cancel();
                            }
                        }
                    };
                    runnable.runTaskTimer(Core.getPlugin(), 0, 50);
                }
                if (i == 5) {
                    Block lampBlock = getPlayer().getLocation().add(0, -1, 0).getBlock();
                    center = lampBlock.getLocation().add(0.5, 1, 0.5);
                    oldMaterials.put(lampBlock.getLocation(), lampBlock.getType());
                    oldDatas.put(lampBlock.getLocation(), lampBlock.getData());
                    blocksToRestore.add(lampBlock);
                    lampBlock.setType(lampMaterial);
                    UtilParticles.play(lampBlock.getLocation(), Effect.STEP_SOUND, lampBlock.getTypeId(), lampBlock.getData(), 0, 0, 0, 1, 50);
                } else if (i == 4) {
                    for (Block b : getSurroundingBlocks(center.clone().add(0, -1, 0).getBlock())) {
                        oldMaterials.put(b.getLocation(), b.getType());
                        oldDatas.put(b.getLocation(), b.getData());
                        blocksToRestore.add(b);
                        b.setType(b1Mat);
                        b.setData(b1data);
                        UtilParticles.play(b.getLocation(), Effect.STEP_SOUND, b.getTypeId(), b.getData(), 0, 0, 0, 1, 50);
                    }
                } else if (i == 3) {
                    for (Block b : getSurroundingSurrounding(center.clone().add(0, -1, 0).getBlock())) {
                        oldMaterials.put(b.getLocation(), b.getType());
                        oldDatas.put(b.getLocation(), b.getData());
                        blocksToRestore.add(b);
                        b.setType(b2Mat);
                        b.setData(b2data);
                        UtilParticles.play(b.getLocation(), Effect.STEP_SOUND, b.getTypeId(), b.getData(), 0, 0, 0, 1, 50);
                    }
                } else if (i == 2) {
                    for (Block b : getBlock3(center.clone().add(0, -1, 0).getBlock())) {
                        oldMaterials.put(b.getLocation(), b.getType());
                        oldDatas.put(b.getLocation(), b.getData());
                        blocksToRestore.add(b);
                        b.setType(b3Mat);
                        b.setData(b3data);
                        UtilParticles.play(b.getLocation(), Effect.STEP_SOUND, b.getTypeId(), b.getData(), 0, 0, 0, 1, 50);
                    }
                } else if (i == 1) {
                    for (Block b : getSurroundingSurrounding(center.getBlock())) {
                        oldMaterials.put(b.getLocation(), b.getType());
                        oldDatas.put(b.getLocation(), b.getData());
                        blocksToRestore.add(b);
                        b.setType(barriersMaterial);
                        b.setData(barrierData);
                        UtilParticles.play(b.getLocation(), Effect.STEP_SOUND, b.getTypeId(), b.getData(), 0, 0, 0, 1, 50);
                    }
                }
                i--;
            }
        };
        runnable.runTaskTimer(Core.getPlugin(), 0, 12);

        final TreasureChest treasureChest = this;

        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (Core.getCustomPlayer(player).currentTreasureChest == treasureChest)
                    forceOpen(45);
            }
        }, 60 * 20);

        Core.getCustomPlayer(getPlayer()).currentTreasureChest = this;

        new BukkitRunnable() {

            @Override
            public void run() {
                if (getPlayer() == null
                        || Core.getCustomPlayer(getPlayer()) == null
                        || Core.getCustomPlayer(getPlayer()).currentTreasureChest != treasureChest) {
                    cancel();
                    return;
                }
                if (getPlayer().getLocation().distance(center) > 1.5)
                    getPlayer().teleport(center);
                for (Entity ent : player.getNearbyEntities(2, 2, 2)) {
                    if (Core.getCustomPlayer(player).currentPet != null) {
                        if (ent == Core.getCustomPlayer(player).currentPet
                                || Core.getCustomPlayer(player).currentPet.items.contains(ent)) {
                            continue;
                        }
                    }
                    if (!items.contains(ent)
                            && ent != getPlayer()
                            && !holograms.contains(ent)) {
                        Vector v = ent.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).multiply(0.5).add(new Vector(0, 1.5, 0));
                        v.setY(0);
                        v.add(new Vector(0, 1, 0));
                        MathUtils.applyVelocity(ent, v.add(MathUtils.getRandomCircleVector().multiply(0.2)));
                    }
                }
            }
        }.runTaskTimer(Core.getPlugin(), 0, 1);
    }

    public Player getPlayer() {
        if (owner != null)
            return Bukkit.getPlayer(owner);
        return null;
    }

    public void clear() {
        for (Block b : blocksToRestore) {
            UtilParticles.play(b.getLocation(), Effect.STEP_SOUND, b.getTypeId(), b.getData(), 0, 0, 0, 1, 50);
            b.setType(oldMaterials.get(b.getLocation()));
            b.setData(oldDatas.get(b.getLocation()));
        }
        if (!stopping) {
            Bukkit.getScheduler().runTaskLater(Core.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    for (Block b : chestsToRemove) {
                        UtilParticles.play(b.getLocation(), Effect.STEP_SOUND, b.getTypeId(), b.getData(), 0, 0, 0, 1, 50);
                        b.setType(Material.AIR);
                    }
                    for (Block b : chests) {
                        UtilParticles.play(b.getLocation(), Effect.STEP_SOUND, b.getTypeId(), b.getData(), 0, 0, 0, 1, 50);
                        b.setType(Material.AIR);
                    }
                    for (Entity ent : items)
                        ent.remove();
                    for (Entity hologram : holograms)
                        hologram.remove();
                    items.clear();
                    chests.clear();
                    holograms.clear();
                    chestsToRemove.clear();
                    blocksToRestore.clear();
                    if (Core.getCustomPlayer(getPlayer()) != null)
                        Core.getCustomPlayer(getPlayer()).currentTreasureChest = null;
                    owner = null;
                    randomGenerator.clear();
                }
            }, 30);
        } else {
            for (Block b : chestsToRemove) {
                UtilParticles.play(b.getLocation(), Effect.STEP_SOUND, b.getTypeId(), b.getData(), 0, 0, 0, 1, 50);
                b.setType(Material.AIR);
            }
            for (Block b : chests) {
                UtilParticles.play(b.getLocation(), Effect.STEP_SOUND, b.getTypeId(), b.getData(), 0, 0, 0, 1, 50);
                b.setType(Material.AIR);
            }
            for (Entity ent : items)
                ent.remove();
            for (Entity hologram : holograms)
                hologram.remove();
            items.clear();
            chests.clear();
            holograms.clear();
            chestsToRemove.clear();
            blocksToRestore.clear();
            Core.getCustomPlayer(getPlayer()).currentTreasureChest = null;
            owner = null;
            randomGenerator.clear();
        }
    }

    public List<Block> getSurroundingBlocks(Block b) {
        List<Block> blocks = new ArrayList<>();
        blocks.add(b.getRelative(BlockFace.EAST));
        blocks.add(b.getRelative(BlockFace.WEST));
        blocks.add(b.getRelative(BlockFace.NORTH));
        blocks.add(b.getRelative(BlockFace.SOUTH));
        blocks.add(b.getRelative(1, 0, 1));
        blocks.add(b.getRelative(-1, 0, -1));
        blocks.add(b.getRelative(1, 0, -1));
        blocks.add(b.getRelative(-1, 0, 1));
        return blocks;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer() == getPlayer()) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                    || event.getFrom().getBlockY() != event.getTo().getBlockY()
                    || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.setCancelled(true);
                event.getPlayer().teleport(event.getFrom());
            }
        }
    }

    boolean stopping;

    public void forceOpen(int delay) {
        if (delay == 0) {
            stopping = true;
            for (int i = 0; i < 4; i++) {
                randomGenerator.giveRandomThing();
                getPlayer().sendMessage(MessageManager.getMessage("You-Won-Treasure-Chests").replace("%name%", randomGenerator.getName()));
            }
        } else {
            for (final Block b : chests) {
                playChestAction(b, true);
                randomGenerator.loc = b.getLocation().clone().add(0, 1, 0);
                randomGenerator.giveRandomThing();
                ItemStack is = ItemFactory.create(randomGenerator.getMaterial(), randomGenerator.getData(), UUID.randomUUID().toString());

                EntityItem ei = new EntityItem(
                        ((CraftWorld) b.getLocation().clone().add(0.5, 1.2, 0.5).getWorld()).getHandle(),
                        b.getLocation().clone().add(0.5, 1.2, 0.5).getX(),
                        b.getLocation().clone().add(0.5, 1.2, 0.5).getY(),
                        b.getLocation().clone().add(0.5, 1.2, 0.5).getZ(),
                        CraftItemStack.asNMSCopy(is)) {

                    public boolean a(EntityItem entityitem) {
                        return false;
                    }
                };
                ei.getBukkitEntity().setVelocity(new Vector(0, 0.25, 0));
                ei.pickupDelay = Integer.MAX_VALUE;
                ei.getBukkitEntity().setCustomName(UUID.randomUUID().toString());

                ((CraftWorld) b.getLocation().add(0.5, 1.2, 0.5).getWorld()).getHandle().addEntity(ei);

                items.add((Item) ei.getBukkitEntity());
                final String nameas = randomGenerator.getName();
                Bukkit.getScheduler().runTaskLater(Core.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        spawnHologram(b.getLocation().clone().add(0.5, 0.3, 0.5), nameas);
                    }
                }, 15);
                chestsLeft--;
                chestsToRemove.add(b);
            }
            chests.clear();

            Bukkit.getScheduler().runTaskLater(Core.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    clear();
                }
            }, delay);
        }
    }

    public List<Block> getSurroundingSurrounding(Block b) {
        List<Block> blocks = new ArrayList<>();
        blocks.add(b.getRelative(2, 0, 1));
        blocks.add(b.getRelative(2, 0, -1));
        blocks.add(b.getRelative(2, 0, 2));
        blocks.add(b.getRelative(2, 0, -2));
        blocks.add(b.getRelative(1, 0, -2));
        blocks.add(b.getRelative(1, 0, 2));
        blocks.add(b.getRelative(-1, 0, -2));
        blocks.add(b.getRelative(-1, 0, 2));
        blocks.add(b.getRelative(-2, 0, 1));
        blocks.add(b.getRelative(-2, 0, -1));
        blocks.add(b.getRelative(-2, 0, 2));
        blocks.add(b.getRelative(-2, 0, -2));
        return blocks;
    }

    public List<Block> getBlock3(Block b) {
        List<Block> blocks = new ArrayList<>();
        blocks.add(b.getRelative(-2, 0, 0));
        blocks.add(b.getRelative(2, 0, 0));
        blocks.add(b.getRelative(0, 0, 2));
        blocks.add(b.getRelative(0, 0, -2));
        return blocks;
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        if (blocksToRestore.contains(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
    }

    public void playChestAction(Block b, boolean open) {
        Location location = b.getLocation();
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        if (enderChests) {
            TileEntityEnderChest tileChest = (TileEntityEnderChest) world.getTileEntity(position);
            world.playBlockAction(position, tileChest.w(), 1, open ? 1 : 0);
        } else {
            TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
            world.playBlockAction(position, tileChest.w(), 1, open ? 1 : 0);
        }
    }

    private void spawnHologram(Location location, String s) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setSmall(true);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setBasePlate(false);
        armorStand.setCustomName(s);
        armorStand.setCustomNameVisible(true);
        holograms.add(armorStand);
    }

    @EventHandler
    public void onInter(final PlayerInteractEvent event) {
        if (event.getClickedBlock() != null
                && (event.getClickedBlock().getType() == Material.CHEST ||
                event.getClickedBlock().getType() == Material.ENDER_CHEST)
                && chests.contains(event.getClickedBlock())
                && event.getPlayer() == getPlayer()) {
            playChestAction(event.getClickedBlock(), true);
            randomGenerator.loc = event.getClickedBlock().getLocation().add(0, 1, 0);
            randomGenerator.giveRandomThing();

            ItemStack is = ItemFactory.create(randomGenerator.getMaterial(), randomGenerator.getData(), UUID.randomUUID().toString());

            EntityItem ei = new EntityItem(
                    ((CraftWorld) event.getClickedBlock().getLocation().add(0.5, 1.2, 0.5).getWorld()).getHandle(),
                    event.getClickedBlock().getLocation().add(0.5, 1.2, 0.5).getX(),
                    event.getClickedBlock().getLocation().add(0.5, 1.2, 0.5).getY(),
                    event.getClickedBlock().getLocation().add(0.5, 1.2, 0.5).getZ(),
                    CraftItemStack.asNMSCopy(is)) {

                public boolean a(EntityItem entityitem) {
                    return false;
                }
            };
            ei.getBukkitEntity().setVelocity(new Vector(0, 0.25, 0));
            ei.pickupDelay = Integer.MAX_VALUE;
            ei.getBukkitEntity().setCustomName(UUID.randomUUID().toString());
            ei.pickupDelay = 20;

            ((CraftWorld) event.getClickedBlock().getLocation().add(0.5, 1.2, 0.5).getWorld()).getHandle().addEntity(ei);

            items.add((Item) ei.getBukkitEntity());
            final String nameas = randomGenerator.getName();
            Bukkit.getScheduler().runTaskLater(Core.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    spawnHologram(event.getClickedBlock().getLocation().add(0.5, 0.3, 0.5), nameas);
                }
            }, 15);
            chestsLeft--;
            chests.remove(event.getClickedBlock());
            chestsToRemove.add(event.getClickedBlock());
            if (chestsLeft == 0) {
                Bukkit.getScheduler().runTaskLater(Core.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        clear();
                    }
                }, 50);
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (event.getPlayer() == getPlayer()
                && event.getReason().contains("Fly")) {
            event.setCancelled(true);
            event.getPlayer().teleport(center);
        }
    }

    public Location getChestLocation(int i, Location loc) {
        Location chestLocation = center.clone();
        chestLocation.setX(loc.getBlockX() + 0.5);
        chestLocation.setY(loc.getBlockY());
        chestLocation.setZ(loc.getBlockZ() + 0.5);
        switch (i) {
            case 1:
                chestLocation.add(2, 0, 0);
                break;
            case 2:
                chestLocation.add(-2, 0, 0);
                break;
            case 3:
                chestLocation.add(0, 0, 2);
                break;
            case 4:
                chestLocation.add(0, 0, -2);
                break;
        }
        return chestLocation;
    }

}