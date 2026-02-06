package me.axeno.hommr.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DatabaseTable(tableName = "homes")
public class Home {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, index = true)
    private UUID owner;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private String world;

    @DatabaseField(canBeNull = false)
    private double x;

    @DatabaseField(canBeNull = false)
    private double y;

    @DatabaseField(canBeNull = false)
    private double z;

    @DatabaseField(canBeNull = false)
    private float yaw;

    @DatabaseField(canBeNull = false)
    private float pitch;

    @DatabaseField(canBeNull = false)
    private long createdAt;

    /**
     * Create a Home instance from a Bukkit Location for the given owner and name.
     *
     * @param owner    the UUID of the player who owns the home
     * @param name     the display name for the home
     * @param location the source Bukkit Location whose world, coordinates, yaw, and pitch are used
     * @return a Home populated with the location data, owner and name; `id` is set to 0 and `createdAt` is set to the current system time in milliseconds
     */
    public static Home fromLocation(UUID owner, String name, Location location) {
        return new Home(
                0,
                owner,
                name,
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                System.currentTimeMillis()
        );
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(this.world);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }
}