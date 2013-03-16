package com.dogonfire.gods;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class HolyLandManager
{
	private Gods					plugin			= null;
	private FileConfiguration		landConfig		= null;
	private File					landConfigFile	= null;
	private HashMap<String, String>	fromLocations	= new HashMap<String,String>();

	HolyLandManager(Gods p)
	{
		this.plugin = p;
	}

	public void load()
	{
		if (this.landConfigFile == null)
		{
			this.landConfigFile = new File(this.plugin.getDataFolder(),
					"holyland.yml");
		}

		this.landConfig = YamlConfiguration
				.loadConfiguration(this.landConfigFile);

		this.plugin.log("Loaded " + this.landConfig.getKeys(false).size()
				+ " holy land entries.");
	}

	public void save()
	{
		if ((this.landConfig == null) || (this.landConfigFile == null))
		{
			return;
		}

		try
		{
			landConfig.save(landConfigFile);
		} 
		catch (Exception ex)
		{
			plugin.log("Could not save config to " + landConfigFile + ": " + ex.getMessage());
		}
	}

	public String getGodForBeliever(String believerName)
	{
		return this.landConfig.getString(believerName + ".God");
	}

	public Set<String> getBelievers()
	{
		Set<String> allBelievers = landConfig.getKeys(false);

		return allBelievers;
	}

	public String getNearestBeliever(Location location)
	{
		Set<String> allBelievers = this.landConfig.getKeys(false);
		double minLength = 999999.0D;
		Player minPlayer = null;

		for (String believerName : allBelievers)
		{
			Player player = this.plugin.getServer().getPlayer(believerName);

			if ((player != null) && (player.getWorld() == location.getWorld()))
			{
				double length = player.getLocation().subtract(location)
						.length();

				if (length < minLength)
				{
					minLength = length;
					minPlayer = player;
				}
			}
		}
		if (minPlayer == null)
		{
			return null;
		}

		return minPlayer.getName();
	}

	private int hashVector(Location location)
	{
		return location.getBlockX() * 73856093 ^ location.getBlockY()
				* 19349663 ^ location.getBlockZ() * 83492791;
	}

	private Date getFirstPrayerTime(String holylandHash)
	{
		String firstPrayerString = this.landConfig.getString(holylandHash
				+ ".FirstPrayerTime");

		if (firstPrayerString == null)
		{
			return null;
		}

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date firstPrayerDate = null;
		try
		{
			firstPrayerDate = formatter.parse(firstPrayerString);
		} catch (Exception ex)
		{
			firstPrayerDate = new Date();
			firstPrayerDate.setTime(0L);
		}

		return firstPrayerDate;
	}

	public void setNeutralLandHotspot(Location location, int radius)
	{
		Location clampedLocation = new Location(location.getWorld(),
				location.getBlockX(), 0.0D, location.getBlockZ());

		String neutralLandName = "NeutralLand_" + location.getWorld().getName();

		landConfig.set(neutralLandName + ".Radius", Integer.valueOf(radius));
		landConfig.set(neutralLandName + ".X", Integer.valueOf(clampedLocation.getBlockX()));
		landConfig.set(neutralLandName + ".Z", Integer.valueOf(clampedLocation.getBlockZ()));
		landConfig.set(neutralLandName + ".World", clampedLocation.getWorld().getName());

		save();
	}

	public boolean isNeutralLandLocation(Location location)
	{
		Location clampedLocation = new Location(location.getWorld(), location.getBlockX(), 0.0D, location.getBlockZ());

		String neutralLandName = "NeutralLand_" + location.getWorld().getName();

		int radius = this.landConfig.getInt(neutralLandName + ".Radius");
		double x = this.landConfig.getDouble(neutralLandName + ".X");
		double z = this.landConfig.getDouble(neutralLandName + ".Z");

		Location neutralLocation = new Location(location.getWorld(), x, 0.0D, z);

		if ((radius == 0) || (neutralLocation.subtract(clampedLocation).length() > radius))
		{
			return false;
		}

		return true;
	}

	public boolean isMobTypeAllowedToSpawn(EntityType mobType)
	{
		if (mobType == EntityType.BAT 
				|| mobType == EntityType.SQUID
				|| mobType == EntityType.CHICKEN
				|| mobType == EntityType.PIG
				|| mobType == EntityType.COW 
				|| mobType == EntityType.OCELOT
				|| mobType == EntityType.SHEEP
				|| mobType == EntityType.VILLAGER
				|| mobType == EntityType.MUSHROOM_COW
				|| mobType == EntityType.IRON_GOLEM)
			return true;
		else
			return false;

	}

	public void setPrayingHotspot(String believerName, String godName,
			Location location)
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Location clampedLocation = new Location(location.getWorld(),
				location.getBlockX(), 0.0D, location.getBlockZ());

		int hash = hashVector(clampedLocation);

		Date thisDate = new Date();

		if (this.landConfig.getString(hash + ".FirstPrayerTime") == null)
		{
			this.landConfig.set(hash + ".FirstPrayerTime",
					formatter.format(thisDate));
		}

		/*
		 * Random random = new Random();
		 * 
		 * double radius = getHolylandRadius(godName);
		 * 
		 * Location changeLocation; for (int n = 0; n < 1000; n++) { double r =
		 * 2 + random.nextInt((int) radius);
		 * 
		 * double a = random.nextInt(628) / 100.0D;
		 * 
		 * changeLocation = new Location(location.getWorld(),
		 * location.getBlockX() + r * Math.cos(a), location.getBlockY(),
		 * location.getBlockZ() + r* Math.sin(a)); }
		 */

		this.landConfig.set(hash + ".GodName", godName);
		this.landConfig.set(hash + ".LastPrayerTime",
				formatter.format(thisDate));
		this.landConfig.set(hash + ".X",
				Integer.valueOf(clampedLocation.getBlockX()));
		this.landConfig.set(hash + ".Z",
				Integer.valueOf(clampedLocation.getBlockZ()));
		this.landConfig.set(hash + ".World", clampedLocation.getWorld()
				.getName());

		save();
	}

	private double getHolylandRadius(String godName)
	{
		float godPower = this.plugin.getGodManager().getGodPower(godName);

		if (godPower == 0.0F)
		{
			return 0.0D;
		}

		double radius = this.plugin.minHolyLandRadius
				+ this.plugin.holyLandRadiusPrPower * godPower;

		if (radius > this.plugin.maxHolyLandRadius)
		{
			return this.plugin.maxHolyLandRadius;
		}

		return radius;
	}

	public String getGodAtHolyLandLocationFrom(String believerName)
	{
		return (String) this.fromLocations.get(believerName);
	}

	public String getGodAtHolyLandLocationTo(String believerName,
			Location location)
	{
		String godName = getGodAtHolyLandLocation(location);

		this.fromLocations.put(believerName, godName);

		return godName;
	}

	public void setNeutralLandLocationFrom(String believerName)
	{
		this.fromLocations.put(believerName, "NeutralLand");
	}

	private void setBiomeAt(World world, int x, int z, Biome biome)
	{
		Chunk chunk = world.getChunkAt(x >> 4, z >> 4);

		if (!chunk.isLoaded())
		{
			chunk.load();
		}

		world.setBiome(x, z, biome);
	}

	public boolean deleteGodAtHolyLandLocation(Location location)
	{
		Location clampedLocation = new Location(null, location.getBlockX(),
				0.0D, location.getBlockZ());

		int holylandHash = hashVector(clampedLocation);

		String godName = this.landConfig.getString(holylandHash + ".GodName");

		if (godName != null)
		{
			this.landConfig.set(holylandHash + ".GodName", null);

			save();

			return true;
		}

		return false;
	}

	public String getGodAtHolyLandLocation(Location location)
	{
		Location clampedLocation = new Location(null, location.getBlockX(),
				0.0D, location.getBlockZ());
		Date oldestFirstPrayerTime = new Date();
		String oldestGodname = null;

		for (String holylandHash : this.landConfig.getKeys(false))
		{
			String godName = this.landConfig.getString(holylandHash
					+ ".GodName");

			if (godName != null)
			{
				String worldName = this.landConfig.getString(holylandHash
						+ ".World");

				if ((worldName != null)
						&& (worldName.equals(location.getWorld().getName())))
				{
					int x = this.landConfig.getInt(holylandHash + ".X");
					int z = this.landConfig.getInt(holylandHash + ".Z");

					Location holylandLocation = new Location(null, x, 0.0D, z);

					if (holylandLocation.subtract(clampedLocation).length() < getHolylandRadius(godName))
					{
						Date firstPrayerTime = getFirstPrayerTime(holylandHash);

						if ((firstPrayerTime != null)
								&& (firstPrayerTime
										.before(oldestFirstPrayerTime)))
						{
							oldestFirstPrayerTime = firstPrayerTime;
							oldestGodname = godName;
						}
					}

				}

			}

		}

		return oldestGodname;
	}

	public void handleQuit(String playerName)
	{
		this.fromLocations.remove(playerName);
	}

	public void removeAbandonedLands()
	{
		long timeBefore = System.currentTimeMillis();

		Date thisDate = new Date();

		for (String holylandHash : this.landConfig.getKeys(false))
		{
			if (!holylandHash.contains("NeutralLand"))
			{
				String lastPrayerString = this.landConfig
						.getString(holylandHash + ".LastPrayerTime");

				String pattern = "HH:mm dd-MM-yyyy";

				DateFormat formatter = new SimpleDateFormat(pattern);

				Date lastPrayerDate = null;
				try
				{
					lastPrayerDate = formatter.parse(lastPrayerString);
				} catch (Exception ex)
				{
					this.landConfig.set(holylandHash, null);
				}

				long diff = thisDate.getTime() - lastPrayerDate.getTime();
				long diffMinutes = diff / 60000L;

				if (diffMinutes > 1000L)
				{
					this.landConfig.set(holylandHash, null);
				}
			}
		}
		long timeAfter = System.currentTimeMillis();

		this.plugin.logDebug("Traversed "
				+ this.landConfig.getKeys(false).size() + " Holy lands in "
				+ (timeAfter - timeBefore) + " ms");
	}
}