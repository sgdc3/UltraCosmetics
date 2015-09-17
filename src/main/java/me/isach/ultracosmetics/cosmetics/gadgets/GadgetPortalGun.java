package me.isach.ultracosmetics.cosmetics.gadgets;

import me.isach.ultracosmetics.Core;
import me.isach.ultracosmetics.util.MathUtils;
import me.isach.ultracosmetics.util.UtilParticles;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by sacha on 07/08/15.
 */
public class GadgetPortalGun extends Gadget {

    boolean teleported = false;

    Location locBlue;
    BlockFace blueBlockFace;

    Location locRed;
    BlockFace redBlockFace;

    public GadgetPortalGun(UUID owner) {
        super(Material.REDSTONE_COMPARATOR, (byte) 0x0, "PortalGun", "ultracosmetics.gadgets.portalgun", 1, owner, GadgetType.PORTALGUN);
        displayCountdownMessage = false;
        useTwoInteractMethods = true;
    }

    @Override
    void onInteractRightClick() {
        getPlayer().playSound(getPlayer().getLocation(), Sound.ENDERMAN_TELEPORT, 0.2f, 1.5f);
        UtilParticles.drawParticleLine(getPlayer().getEyeLocation().add(getPlayer().getEyeLocation().getDirection().multiply(0.6)), getPlayer().getTargetBlock((HashSet<Material>) null, 20).getLocation(), Effect.COLOURED_DUST, 100, -255, -255, 255);
        locBlue = getPlayer().getTargetBlock((Set<Material>) null, 20).getLocation();
        List<Block> b = getPlayer().getLastTwoTargetBlocks((Set<Material>) null, 20);
        blueBlockFace = getBlockFace(b.get(0), b.get(1));
        locBlue = locBlue.getBlock().getRelative(blueBlockFace).getLocation().add(0, -0.5, -1);
        if (blueBlockFace == BlockFace.UP || blueBlockFace == BlockFace.DOWN) {
            locBlue.add(0.5, 0.7, 0.5);
        } else if (blueBlockFace == BlockFace.WEST) {
            locBlue.add(0.6, 0.8, 0.5);
        } else if (blueBlockFace == BlockFace.EAST) {
            locBlue.add(.3, 0.8, 0.5);
        } else if (blueBlockFace == BlockFace.NORTH) {
            locBlue.add(0.4, 1.8, 1.75);
        } else if (blueBlockFace == BlockFace.SOUTH) {
            locBlue.add(0.4, 1.8, 1.2);
        }
    }


    @Override
    void onInteractLeftClick() {
        getPlayer().playSound(getPlayer().getLocation(), Sound.ENDERMAN_TELEPORT, 0.2f, 1.5f);
        UtilParticles.drawParticleLine(getPlayer().getEyeLocation().add(getPlayer().getEyeLocation().getDirection().multiply(0.6)), getPlayer().getTargetBlock((HashSet<Material>) null, 20).getLocation(), Effect.COLOURED_DUST, 100, 255, -255, -255);
        locRed = getPlayer().getTargetBlock((Set<Material>) null, 20).getLocation();
        List<Block> b = getPlayer().getLastTwoTargetBlocks((Set<Material>) null, 20);
        redBlockFace = getBlockFace(b.get(0), b.get(1));
        locRed = locRed.clone().getBlock().getRelative(redBlockFace).getLocation().add(0, -0.5, -1);
        if (redBlockFace == BlockFace.UP || redBlockFace == BlockFace.DOWN) {
            locRed.add(0.5, 0.7, 0.5);
        } else if (redBlockFace == BlockFace.WEST) {
            locRed.add(0.6, 0.8, 0.5);
        } else if (redBlockFace == BlockFace.EAST) {
            locRed.add(.3, 0.8, 0.5);
        } else if (redBlockFace == BlockFace.NORTH) {
            locRed.add(0.4, 1.8, 1.75);
        } else if (redBlockFace == BlockFace.SOUTH) {
            locRed.add(0.4, 1.8, 1.2);
        }
    }

    public Vector getVectorFromBlockFace(BlockFace bf) {
        Vector v = new Vector(0, 0, 0);
        if (bf == BlockFace.UP) {
            v.add(new Vector(0, 0.3, 0));
        } else if (bf == BlockFace.DOWN) {
            v.add(new Vector(0, -0.3, 0));
        } else if (bf == BlockFace.WEST) {
            v.add(new Vector(-0.3, 0, 0));
        } else if (bf == BlockFace.EAST) {
            v.add(new Vector(0.3, 0, 0));
        } else if (bf == BlockFace.NORTH) {
            v.add(new Vector(-0.3, 0, 0));
        } else if (bf == BlockFace.SOUTH) {
            v.add(new Vector(0.3, 0, 0));
        }
        return v;
    }

    public float getPitch(BlockFace bf) {
        float pitch = 0;
        if (bf == BlockFace.UP) {
            pitch = -90;
        } else if (bf == BlockFace.DOWN) {
            pitch = 90;
        }
        return pitch;
    }

    public float getYaw(BlockFace bf) {
        float yaw = 90;
        if (bf == BlockFace.WEST) {
            yaw = 90;
        } else if (bf == BlockFace.EAST) {
            yaw = -90;
        } else if (bf == BlockFace.NORTH) {
            yaw = 180;
        } else if (bf == BlockFace.SOUTH) {
            yaw = 0;
        }
        return yaw;
    }

    public BlockFace getBlockFace(Block a, Block b) {
        for (BlockFace bf : BlockFace.values()) {
            if (a.getRelative(bf).getLocation().equals(b.getLocation())) {
                return bf.getOppositeFace();
            }
        }
        return null;
    }

    @Override
    void onUpdate() {
        if (locBlue != null) {
            Location portalCenter = locBlue.clone();
            if (locRed != null && !teleported) {
                Location toDistance;
                if (blueBlockFace == BlockFace.DOWN) {
                    toDistance = getPlayer().getEyeLocation().clone();
                } else if (blueBlockFace == BlockFace.UP) {
                    toDistance = getPlayer().getLocation().clone();
                } else {
                    toDistance = getPlayer().getLocation().add(0, 1.03, 0);
                }
                if (blueBlockFace == BlockFace.UP || blueBlockFace == BlockFace.DOWN) {
                    portalCenter.add(0, 0, 1);
                } else if (blueBlockFace == BlockFace.NORTH || blueBlockFace == BlockFace.SOUTH) {
                    portalCenter.add(0, -1, 0);
                } else if (blueBlockFace == BlockFace.EAST || blueBlockFace == BlockFace.WEST) {
                    portalCenter.add(0, 0, 1);
                }
                if (toDistance.distance(locBlue) < 1.01) {
                    teleported = true;
                    getPlayer().teleport(locRed);
                    getPlayer().setVelocity(getVectorFromBlockFace(redBlockFace));
                    if (redBlockFace == BlockFace.UP || redBlockFace == BlockFace.DOWN) {
                        Location loc = getPlayer().getLocation().clone();
                        loc.setPitch(getPitch(redBlockFace));
                        getPlayer().teleport(loc);
                    } else {
                        Location loc = getPlayer().getLocation().clone();
                        loc.setYaw(getYaw(redBlockFace));
                        getPlayer().teleport(loc);
                    }
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Core.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            teleported = false;
                        }
                    }, 20);
                }
            }
            Location loc = locBlue.clone();
            for (int i = 0; i < 100; i++) {
                double inc = (2 * Math.PI) / 20;
                double angle = i * inc;
                Vector v = new Vector();
                v.setX(Math.cos(angle) * 0.3f);
                v.setZ(Math.sin(angle) * 0.3f);
                double x = 0;
                double z = 0;
                if (blueBlockFace != BlockFace.UP && blueBlockFace != BlockFace.DOWN) {
                    if (blueBlockFace == BlockFace.EAST || blueBlockFace == BlockFace.WEST) {
                        x = 0;
                        z = 1.5;
                    } else if (blueBlockFace == BlockFace.NORTH || blueBlockFace == BlockFace.SOUTH) {
                        z = 0;
                        x = 1.5;
                    }
                }
                MathUtils.rotateVector(v, x, 0, z);
                float finalR = -255 / 255;
                float finalG = -255 / 255;
                float finalB = 255 / 255;
                UtilParticles.play(loc.add(v), Effect.COLOURED_DUST, 0, 0, finalR, finalG, finalB, 1f, 0);
            }
        }
        if (locRed != null) {
            if (locBlue != null && !teleported) {
                Location toDistance;
                if (redBlockFace == BlockFace.DOWN) {
                    toDistance = getPlayer().getEyeLocation().clone();
                } else if (redBlockFace == BlockFace.UP) {
                    toDistance = getPlayer().getLocation().clone();
                } else {
                    toDistance = getPlayer().getLocation().add(0, 1.1, 0);
                }
                if (toDistance.distance(locRed) < 1.1) {
                    teleported = true;
                    getPlayer().teleport(locBlue);
                    getPlayer().setVelocity(getVectorFromBlockFace(blueBlockFace));
                    if (blueBlockFace == BlockFace.UP || blueBlockFace == BlockFace.DOWN) {
                        Location loc = getPlayer().getLocation().clone();
                        loc.setPitch(getPitch(blueBlockFace));
                        getPlayer().teleport(loc);
                    } else {
                        Location loc = getPlayer().getLocation().clone();
                        loc.setYaw(getYaw(blueBlockFace));
                        getPlayer().teleport(loc);
                    }
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Core.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            teleported = false;
                        }
                    }, 20);
                }
            }
            Location loc = locRed.clone();
            for (int i = 0; i < 100; i++) {
                double inc = (2 * Math.PI) / 20;
                double angle = i * inc;
                Vector v = new Vector();
                v.setX(Math.cos(angle) * 0.3f);
                v.setZ(Math.sin(angle) * 0.3f);
                double x = 0;
                double z = 0;
                if (redBlockFace != BlockFace.UP
                        && redBlockFace != BlockFace.DOWN) {
                    if (redBlockFace == BlockFace.EAST
                            || redBlockFace == BlockFace.WEST) {
                        x = 0;
                        z = 1.5;
                    } else if (redBlockFace == BlockFace.NORTH
                            || redBlockFace == BlockFace.SOUTH) {
                        z = 0;
                        x = 1.5;
                    }
                }
                MathUtils.rotateVector(v, x, 0, z);
                float finalR = 255 / 255;
                float finalG = -255 / 255;
                float finalB = -255 / 255;
                UtilParticles.play(loc.add(v), Effect.COLOURED_DUST, 0, 0, finalR, finalG, finalB, 1f, 0);
            }
        }

    }

    public BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    public BlockFace[] radial = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

    public BlockFace yawToFace(float yaw) {
        return yawToFace(yaw, true);
    }

    public BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections) {
            return radial[Math.round(yaw / 45f) & 0x7];
        } else {
            return axis[Math.round(yaw / 90f) & 0x3];
        }
    }

    @Override
    public void clear() {
        locBlue = null;
        locRed = null;
    }
}
