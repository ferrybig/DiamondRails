package me.ferrybig.bukkit.plugins.diamondrails;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class DiamondRails extends JavaPlugin implements Listener {

    private static final int SEARCH_DEPTH = -3;
    private static final Material TRACK_MATERIAL = Material.DIAMOND_BLOCK;
    private static final BlockFace[] SEARCH_LOCATIONS;

    static {
        SEARCH_LOCATIONS = new BlockFace[8 * 3];
        int i = 0;
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                if (x != 0 || z != 0) {
                    for (int y = -1; y < 2; y++) {
                        SEARCH_LOCATIONS[i++] = new BlockFace(x, y, z);
                    }
                }
            }
        }
    }

    private static final BlockFace[] BLOCK_LOCATIONS;

    static {
        BLOCK_LOCATIONS = new BlockFace[9 * 3];
        int i = 0;
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = -1; y < 2; y++) {
                    BLOCK_LOCATIONS[i++] = new BlockFace(x, y, z);
                }
            }
        }
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMinecartMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            Minecart minecart = (Minecart) event.getVehicle();
            Location loc = minecart.getLocation();
            Material type = loc.getBlock().getType();
            if (type == Material.ACTIVATOR_RAIL
                    || type == Material.RAILS
                    || type == Material.DETECTOR_RAIL
                    || type == Material.POWERED_RAIL) {
                return;
            }
            loc.add(0, SEARCH_DEPTH, 0);
            Block search = getActualBlock(loc.getBlock(), loc);
            if (search == null) {
                return;
            }
            loc = loc.add(minecart.getVelocity().multiply(4));
            Block target = getNextLocation(search, loc);
            if (target != null) {
                target = target.getRelative(0, -SEARCH_DEPTH, 0);
                Location newLoc = target.getLocation().add(0.5, 0.7, 0.5);
                loc = minecart.getLocation();
                Vector speed = newLoc.toVector().subtract(loc.toVector());
                Vector oldSpeed = minecart.getVelocity();
                oldSpeed.multiply(0.5);
                oldSpeed.setY(0);
                speed.add(oldSpeed);
                if (speed.lengthSquared() > 1) {
                    speed.multiply(1.0 / (speed.length()));
                }
                minecart.setVelocity(speed);
            }

        }
    }

    private Block getNextLocation(Block scanBlock, Location calculatedHeading) {
        double distance, lastDistance = Double.MAX_VALUE;
        Block scannedBlock, lastBlock = null;
        Location cache = new Location(calculatedHeading.getWorld(), 0, 0, 0);
        for (BlockFace f : SEARCH_LOCATIONS) {
            scannedBlock = f.getRelative(scanBlock);
            if (scannedBlock.getType() == TRACK_MATERIAL) {
                cache.setX(scannedBlock.getX() + 0.5);
                cache.setY(scannedBlock.getY() + 0.5);
                cache.setZ(scannedBlock.getZ() + 0.5);
                distance = calculatedHeading.distanceSquared(cache);
                if (distance < lastDistance) {
                    lastDistance = distance;
                    lastBlock = scannedBlock;
                }
            }
        }
        return lastBlock;
    }

    private Block getActualBlock(Block scanBlock, Location calculatedHeading) {
        double distance, lastDistance = Double.MAX_VALUE;
        Block scannedBlock, lastBlock = null;
        Location cache = new Location(calculatedHeading.getWorld(), 0, 0, 0);
        for (BlockFace f : BLOCK_LOCATIONS) {
            scannedBlock = f.getRelative(scanBlock);
            if (scannedBlock.getType() == TRACK_MATERIAL) {
                cache.setX(scannedBlock.getX() + 0.5);
                cache.setY(scannedBlock.getY() + 0.5);
                cache.setZ(scannedBlock.getZ() + 0.5);
                distance = square(calculatedHeading.getX() - cache.getX())
                        + square(calculatedHeading.getY() - cache.getY()) / 2
                        + square(calculatedHeading.getZ() - cache.getZ());
                if (distance < lastDistance) {
                    lastDistance = distance;
                    lastBlock = scannedBlock;
                }
            }
        }
        return lastBlock;
    }

    private static double square(double x) {
        return x * x;
    }

    private static class BlockFace {

        private final int modX;
        private final int modY;
        private final int modZ;

        public BlockFace(int modX, int modY, int modZ) {
            this.modX = modX;
            this.modY = modY;
            this.modZ = modZ;
        }

        public int getModX() {
            return modX;
        }

        public int getModY() {
            return modY;
        }

        public int getModZ() {
            return modZ;
        }

        public Block getRelative(Block bl) {
            return bl.getRelative(modX, modY, modZ);
        }
    }
}
