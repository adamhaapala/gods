package com.dogonfire.gods;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class QuestManager
{
	private Gods						plugin				= null;
	private FileConfiguration			questsConfig		= null;
	private File						questsConfigFile	= null;
	private Random						random				= new Random();
	private HashMap<Material, Integer>	rewardValues		= new HashMap();

	QuestManager(Gods p)
	{
		this.plugin = p;
	}

	public void load()
	{
		if (questsConfigFile == null)
		{
			questsConfigFile = new File(this.plugin.getDataFolder(), "quests.yml");
		}

		questsConfig = YamlConfiguration.loadConfiguration(this.questsConfigFile);

		plugin.log("Loaded " + this.questsConfig.getKeys(false).size() + " quests.");
	}

	public void save()
	{
		if ((this.questsConfig == null) || (this.questsConfigFile == null))
		{
			return;
		}
		try
		{
			questsConfig.save(this.questsConfigFile);
		} 
		catch (Exception ex)
		{
			plugin.log("Could not save config to " + questsConfigFile + ": " + ex.getMessage());
		}
	}

	public void resetItemRewardValues()
	{
		this.rewardValues.put(Material.COAL, Integer.valueOf(5));
		this.rewardValues.put(Material.IRON_INGOT, Integer.valueOf(10));
		this.rewardValues.put(Material.GOLD_INGOT, Integer.valueOf(50));
		this.rewardValues.put(Material.DIAMOND, Integer.valueOf(100));
		this.rewardValues.put(Material.BOOK, Integer.valueOf(30));
		this.rewardValues.put(Material.COMPASS, Integer.valueOf(50));
		this.rewardValues.put(Material.PAPER, Integer.valueOf(20));
		this.rewardValues.put(Material.SUGAR_CANE, Integer.valueOf(25));
		this.rewardValues.put(Material.MELON_SEEDS, Integer.valueOf(5));
		this.rewardValues.put(Material.PUMPKIN_SEEDS, Integer.valueOf(5));
		this.rewardValues.put(Material.SEEDS, Integer.valueOf(1));
		this.rewardValues.put(Material.RED_ROSE, Integer.valueOf(1));
		this.rewardValues.put(Material.YELLOW_FLOWER, Integer.valueOf(1));
	}

	public int getRewardValue(Material rewardItem)
	{
		return ((Integer) rewardValues.get(rewardItem)).intValue();
	}

	public void setQuestTypeForGod(String godName, int questType)
	{
		this.questsConfig.set(godName + ".Type", Integer.valueOf(questType));

		save();
	}

	public void setQuestTargetTypeForGod(String godName, String targetType)
	{
		this.questsConfig.set(godName + ".TargetType", targetType);

		save();
	}

	public QUESTTYPE getGlobalQuestType()
	{
		QUESTTYPE type = null;
		try
		{
			type = QUESTTYPE.valueOf(this.questsConfig.getString("Global.Type").toUpperCase());
		} 
		catch (Exception ex)
		{
			plugin.logDebug("Could not recognize quest type '" + this.questsConfig.getString("Global.Type") + "': " + ex.getMessage());
			type = QUESTTYPE.NONE;
		}

		return type;
	}

	public Material getGlobalQuestTargetItemType()
	{
		Material type = null;
		try
		{
			type = Material.getMaterial(questsConfig.getString("Global.ItemType").toUpperCase());
		} 
		catch (Exception ex)
		{
			this.plugin.logDebug("Could not recognize quest type '" + questsConfig.getString("Global.ItemType") + "': " + ex.getMessage());
			type = Material.AIR;
		}

		return type;
	}

	public QUESTTYPE getQuestTypeForGod(String godName)
	{
		QUESTTYPE type = null;
		
		try
		{
			type = QUESTTYPE.valueOf(this.questsConfig.getString(
					godName + ".Type").toUpperCase());
		} 
		catch (Exception ex)
		{
			plugin.logDebug("Could not recognize quest type '" + questsConfig.getString(new StringBuilder().append(godName).append(".Type").toString()) + "' for " + godName + ": " + ex.getMessage());
			type = QUESTTYPE.NONE;
		}

		return type;
	}

	public Date getGlobalQuestCreatedTime()
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		String startTime = this.questsConfig.getString("Global.CreatedTime");
		Date startTimeDate = null;
		try
		{
			startTimeDate = formatter.parse(startTime);
		} catch (Exception ex)
		{
			this.plugin.log("Invalid global quest created date. Reset.");
			startTimeDate = new Date();
			startTimeDate.setTime(0L);
		}

		return startTimeDate;
	}

	public Date getQuestCreatedTimeForGod(String godName)
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		String startTime = questsConfig.getString(godName + ".CreatedTime");
		Date startTimeDate = null;
		
		try
		{
			startTimeDate = formatter.parse(startTime);
		} 
		catch (Exception ex)
		{
			plugin.log("Invalid quest created date for " + godName + ". Reset.");
			startTimeDate = new Date();
			startTimeDate.setTime(0L);
		}

		return startTimeDate;
	}

	public long getGlobalQuestLockedTime()
	{
		String pattern = "HH:mm:ss dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lockedTimeDate = null;
		long time = new Date().getTime();
		try
		{
			String lockedTime = this.questsConfig.getString("Global.LockedTime");
			lockedTimeDate = formatter.parse(lockedTime);
		} catch (Exception ex)
		{
			return -1L;
		}

		return time - lockedTimeDate.getTime();
	}

	public int getGlobalQuestMaxDuration()
	{
		int value = this.questsConfig.getInt("Global.MaxDuration");

		return value;
	}

	public int getQuestMaxDurationForGod(String godName)
	{
		int value = this.questsConfig.getInt(godName + ".MaxDuration");

		return value;
	}

	public int getQuestProgressForGod(String godName)
	{
		int value = this.questsConfig.getInt(godName + ".Progress");

		return value;
	}

	public int getQuestAmountForGod(String godName)
	{
		int value = this.questsConfig.getInt(godName + ".Amount");

		return value;
	}

	public String getQuestTargetTypeForGod(String godName)
	{
		String value = this.questsConfig.getString(godName + ".TargetType");

		return value;
	}

	public String getGlobalQuestTargetType()
	{
		String value = this.questsConfig.getString("Global.TargetType");

		return value;
	}

	public Location getGlobalQuestLocation()
	{
		int x = 0;
		int z;
		int y;
		String worldName;

		try
		{
			x = this.questsConfig.getInt("Global.Location.X");
			y = this.questsConfig.getInt("Global.Location.Y");
			z = this.questsConfig.getInt("Global.Location.Z");
			worldName = this.questsConfig.getString("Global.Location.World");
		} 
		catch (Exception ex)
		{
			return null;
		}

		return new Location(this.plugin.getServer().getWorld(worldName), x, y, z);
	}

	public void removeGlobalQuest()
	{
		questsConfig.set("Global", null);

		save();
	}

	public void removeQuestForGod(String godName)
	{
		questsConfig.set(godName, null);

		save();
	}

	boolean hasQuest(String godName)
	{
		String quest = questsConfig.getString(godName + ".Type");

		return quest != null;
	}

	boolean hasGlobalQuest()
	{
		String quest = questsConfig.getString("Global.Type");

		return quest != null;
	}

	boolean hasGlobalQuestType(QUESTTYPE type)
	{
		QUESTTYPE currentType;
		
		try
		{
			String currentTypeString = questsConfig.getString("Global.Type");

			if(currentTypeString==null)
			{
				return false;
			}
			
			currentType = QUESTTYPE.valueOf(currentTypeString.toUpperCase());			
		} 
		catch (Exception ex)
		{
			plugin.logDebug("Could not recognize global quest type '" + questsConfig.getString("Global.Type") + "': "+ ex.getMessage());
			currentType = QUESTTYPE.NONE;
		}

		return currentType == type;
	}

	boolean hasExpiredGlobalQuest()
	{
		Date thisDate = new Date();
		Date questStartTime = getGlobalQuestCreatedTime();
		int questMaxDuration = getGlobalQuestMaxDuration();

		long diff = thisDate.getTime() - questStartTime.getTime();
		long diffMinutes = diff / 60000L;

		return diffMinutes > questMaxDuration;
	}

	boolean hasExpiredQuest(String godName)
	{
		Date thisDate = new Date();
		Date questStartTime = getQuestCreatedTimeForGod(godName);
		int questMaxDuration = getQuestMaxDurationForGod(godName);

		long diff = thisDate.getTime() - questStartTime.getTime();
		long diffMinutes = diff / 60000L;

		return diffMinutes > questMaxDuration;
	}

	void setGlobalQuestCompletedBy(String godName)
	{
		String pattern = "HH:mm:ss dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		this.questsConfig.set("Global.LockedBy", godName);
		this.questsConfig
				.set("Global.LockedTime", formatter.format(new Date()));

		save();
	}

	String getGlobalQuestTargetLockedGodName()
	{
		String playerName = null;

		try
		{
			playerName = this.questsConfig.getString("Global.LockedBy");
		} catch (Exception ex)
		{
			return null;
		}

		return playerName;
	}

	boolean isGlobalQuestTarget(Location location)
	{
		int z;
		int y;
		int x;
		String world;

		try
		{
			world = this.questsConfig.getString("Global.Location.World");
			x = this.questsConfig.getInt("Global.Location.X");
			y = this.questsConfig.getInt("Global.Location.Y");
			z = this.questsConfig.getInt("Global.Location.Z");
		} 
		catch (Exception ex)
		{
			return false;
		}

		return (x == location.getBlockX()) && (y == location.getBlockY()) && (z == location.getBlockZ()) && (location.getWorld().getName().equals(world));
	}

	Location generateAncientCaveTemple(ItemStack item, String godName, String worldName, int minDist, int maxDist, Location center)
	{
		int run = 0;

		Block target = null;
		Inventory contents = null;

		List defaultspawnblocks = new ArrayList();
		defaultspawnblocks.add(Material.STONE);
		defaultspawnblocks.add(Material.SMOOTH_BRICK);
		defaultspawnblocks.add(Material.MOSSY_COBBLESTONE);
		defaultspawnblocks.add(Material.OBSIDIAN);

		World world = this.plugin.getServer().getWorld(worldName);
		int x;
		int z;
		int y;

		do
		{
			run++;

			int minLevel = 4;
			int maxLevel = 50;
			int maxLight = 4;
			int minLight = 0;
			do
			{
				x = this.random.nextInt(maxDist * 2) - maxDist + center.getBlockX();
				z = this.random.nextInt(maxDist * 2) - maxDist + center.getBlockZ();
			} while ((Math.abs(x - center.getBlockX()) < minDist) || (Math.abs(z - center.getBlockZ()) < minDist));

			do
			{
				y = this.random.nextInt(maxLevel);
			} while (y < minLevel);

			target = world.getBlockAt(x, y, z);

			if ((target.getType() == Material.AIR)
					&& (world.getBlockAt(x, y + 1, z).getType() == Material.AIR)
					&& (target.getLightLevel() <= maxLight)
					&& (target.getLightLevel() >= minLight))
			{
				target = world.getBlockAt(x, y - 1, z);

				if ((defaultspawnblocks.contains(target.getType()))
						&& (world.getHighestBlockAt(target.getLocation())
								.getType() != Material.WATER))
				{
					target.setType(Material.GLOWSTONE);

					target = world.getBlockAt(x, y, z);
					target.setType(Material.CHEST);

					Chest tb = (Chest) target.getState();
					contents = tb.getInventory();
				}
			}
		} while ((contents == null) && (run < 100));

		if (run >= 100)
		{
			plugin.log("Ancient cave holy artifact chest generation FAILED in " + worldName);
			return null;
		}

		HolyArtifact artifact = new HolyArtifact(item, item.getType(), godName);
		artifact.enchant(item);
		artifact.makeHolyArtifact();
		artifact.setHolyArtifactName(item.getType(), godName);

		contents.addItem(new ItemStack[] { item });

		for (ItemStack rewardItem : getRewardsForQuestCompletion(20 + random.nextInt(150)))
		{
			contents.addItem(new ItemStack[] { rewardItem });
		}

		for (int oy = y - 7; oy < y + 7; oy++)
		{
			for (int ox = x - 20; ox < x + 20; ox++)
			{
				for (int oz = z - 20; oz < z + 20; oz++)
				{
					Block stoneTarget = world.getBlockAt(ox, oy, oz);

					if ((random.nextInt(4) == 0) && (stoneTarget.getType() == Material.AIR) && (world.getBlockAt(ox, oy - 1, oz).getType() == Material.STONE))
					{
						stoneTarget.setType(Material.CHEST);
						world.getBlockAt(ox, oy - 1, oz).setType(Material.GLOWSTONE);
					} 
					else if ((stoneTarget.getType() == Material.STONE) && (random.nextInt(4) == 0))
					{
						stoneTarget.setType(Material.SMOOTH_BRICK);
					}
				}
			}
		}

		plugin.logDebug("Ancient cave holy artifact chest generated in " + run + " runs");

		return target.getLocation();
	}

	private String generateLostTempleName()
	{
		int length = 2 + this.random.nextInt(6);
		String templeName = "";

		int l = 0;
		boolean wasVocal = false;

		while (l < length)
		{
			if (wasVocal)
			{
				switch (this.random.nextInt(20))
				{
					case 0:
						templeName = templeName + "b";
						wasVocal = false;
						break;
					case 1:
						templeName = templeName + "f";
						wasVocal = false;
						break;
					case 2:
						templeName = templeName + "g";
						wasVocal = false;
						break;
					case 3:
						templeName = templeName + "h";
						wasVocal = false;
						break;
					case 4:
						templeName = templeName + "j";
						wasVocal = false;
						break;
					case 5:
						templeName = templeName + "k";
						wasVocal = false;
						break;
					case 6:
						templeName = templeName + "l";
						wasVocal = false;
						break;
					case 7:
						templeName = templeName + "ll";
						wasVocal = false;
						break;
					case 8:
						templeName = templeName + "v";
						wasVocal = false;
						break;
					case 9:
						templeName = templeName + "r";
						wasVocal = false;
						break;
					case 10:
						templeName = templeName + "rr";
						wasVocal = false;
						break;
					case 11:
						templeName = templeName + "kk";
						wasVocal = false;
						break;
					case 12:
						templeName = templeName + "p";
						wasVocal = false;
						break;
					case 13:
						templeName = templeName + "t";
						wasVocal = false;
						break;
					case 14:
						templeName = templeName + "s";
						wasVocal = false;
						break;
					case 15:
						templeName = templeName + "x";
						wasVocal = false;
						break;
					case 16:
						templeName = templeName + "d";
						wasVocal = false;
						break;
					case 17:
						templeName = templeName + "n";
						wasVocal = false;
						break;
					case 18:
						templeName = templeName + "m";
						wasVocal = false;
						break;
					case 19:
						templeName = templeName + "nn";
						wasVocal = false;
					default:
						break;
				}
			} else
			{
				switch (this.random.nextInt(6))
				{
					case 0:
						templeName = templeName + "a";
						wasVocal = true;
						break;
					case 1:
						templeName = templeName + "o";
						wasVocal = true;
						break;
					case 2:
						templeName = templeName + "i";
						wasVocal = true;
						break;
					case 3:
						templeName = templeName + "u";
						wasVocal = true;
						break;
					case 4:
						templeName = templeName + "y";
						wasVocal = true;
						break;
					case 5:
						templeName = templeName + "e";
						wasVocal = true;
				}
			}

			l++;
		}

		return templeName.substring(0, 1).toUpperCase() + templeName.substring(1).toLowerCase();
	}

	private Location getPositionForLostCity(String godName, String worldName, int minDist, int maxDist, Location center)
	{
		int run = 0;
		Block target = null;
		Inventory contents = null;

		List defaultspawnblocks = new ArrayList();
		defaultspawnblocks.add(Material.COBBLESTONE);

		minDist = 1;
		maxDist = 2000;

		do
		{
			run++;

			int minLevel = 60;
			int maxLevel = 80;
			int maxLight = 4;
			int minLight = 2;
			int x;
			int z;
			do
			{
				x = random.nextInt(maxDist * 2) - maxDist + center.getBlockX();
				z = random.nextInt(maxDist * 2) - maxDist + center.getBlockZ();
			} while ((Math.abs(x - center.getBlockX()) < minDist) || (Math.abs(z - center.getBlockZ()) < minDist));

			int y;

			do
			{
				y = random.nextInt(maxLevel);
			} while (y < minLevel);

			World world = plugin.getServer().getWorld(worldName);
			target = world.getBlockAt(x, y, z);

			if (target.getType() == Material.AIR)
			{
				target = world.getBlockAt(x, y - 1, z);

				if (defaultspawnblocks.contains(target.getType()))
				{
					target.setType(Material.GLOWSTONE);

					target = world.getBlockAt(x, y, z);
					target.setType(Material.CHEST);

					Chest tb = (Chest) target.getState();
					contents = tb.getInventory();
				}
			}
		} while ((contents == null) && (run < 1000));

		if (run >= 1000)
		{
			this.plugin.log("Lost city chest generation FAILED");
			return null;
		}

		return target.getLocation();
	}

	Location getPositionForAncientTemple(String worldName, int minDist, int maxDist, Location center)
	{
		int run = 0;
		Block target = null;
		Inventory contents = null;

		List defaultspawnblocks = new ArrayList();
		defaultspawnblocks.add(Material.ENDER_PORTAL_FRAME);
		
		do
		{
			run++;

			int minLevel = 4;
			int maxLevel = 50;
			int maxLight = 4;
			int minLight = 0;
			int x;
			int z;
			do
			{
				x = random.nextInt(maxDist * 2) - maxDist + center.getBlockX();
				z = random.nextInt(maxDist * 2) - maxDist + center.getBlockZ();
			} while ((Math.abs(x - center.getBlockX()) < minDist) || (Math.abs(z - center.getBlockZ()) < minDist));
			
			int y;
			
			do
			{
				y = random.nextInt(maxLevel);
			} while (y < minLevel);

			World world = plugin.getServer().getWorld(worldName);
			target = world.getBlockAt(x, y, z);

			if (target.getType() == Material.AIR)
			{
				target = world.getBlockAt(x, y - 1, z);

				if (defaultspawnblocks.contains(target.getType()))
				{
					target = world.getBlockAt(x, y, z);
				}
			}
		}

		while ((target != null) && (run < 1000));

		if (run >= 1000)
		{
			plugin.log("Ancient temple chest generation FAILED");
			return null;
		}

		plugin.logDebug("Ancient temple chest generated in " + run + " runs");

		return target.getLocation();
	}

	Location getPositionForLostHolyLand(String worldName, int minDist, int maxDist, Location center)
	{
		int run = 0;
		boolean placed = false;
		Block target = null;

		List defaultspawnblocks = new ArrayList();
		defaultspawnblocks.add(Material.GRASS);

		minDist = 1;
		maxDist = 2000;
		do
		{
			run++;

			int minLevel = 60;
			int maxLevel = 80;
			int maxLight = 4;
			int minLight = 2;
			int x;
			int z;
			do
			{
				x = this.random.nextInt(maxDist * 2) - maxDist + center.getBlockX();
				z = this.random.nextInt(maxDist * 2) - maxDist + center.getBlockZ();
			} while ((Math.abs(x - center.getBlockX()) < minDist) || (Math.abs(z - center.getBlockZ()) < minDist));
			
			int y;
			
			do
			{
				y = random.nextInt(maxLevel);
			}
			while (y < minLevel);

			World world = this.plugin.getServer().getWorld(worldName);
			target = world.getBlockAt(x, y, z);

			if ((target.getType() == Material.AIR)
					&& (target.getRelative(BlockFace.NORTH).getType() == Material.AIR)
					&& (target.getRelative(BlockFace.SOUTH).getType() == Material.AIR)
					&& (target.getRelative(BlockFace.WEST).getType() == Material.AIR)
					&& (target.getRelative(BlockFace.EAST).getType() == Material.AIR))
			{
				target = world.getBlockAt(x, y - 1, z);

				if (defaultspawnblocks.contains(target.getType()))
				{
					setDominationColor(target.getRelative(BlockFace.DOWN), ChatColor.WHITE);

					target = world.getBlockAt(x, y, z);

					placed = true;
				}
			}
		} while ((!placed) && (run < 100));

		if (run >= 100)
		{
			this.plugin.log("Lost Holy Land claim generation FAILED");
			return null;
		}

		return target.getLocation();
	}

	Location getPositionForHolyBattle(String worldName, int minDist, int maxDist, Location center)
	{
		int run = 0;
		boolean placed = false;
		Block target = null;

		List defaultspawnblocks = new ArrayList();
		defaultspawnblocks.add(Material.GRASS);

		minDist = 1;
		maxDist = 2000;
		do
		{
			run++;

			int minLevel = 60;
			int maxLevel = 80;
			int maxLight = 4;
			int minLight = 2;
			int x;
			int z;
			do
			{
				x = random.nextInt(maxDist * 2) - maxDist + center.getBlockX();
				z = random.nextInt(maxDist * 2) - maxDist + center.getBlockZ();
			} while ((Math.abs(x - center.getBlockX()) < minDist) || (Math.abs(z - center.getBlockZ()) < minDist));
			
			int y;
			do
			{
				y = random.nextInt(maxLevel);
			}
			while (y < minLevel);

			World world = plugin.getServer().getWorld(worldName);
			target = world.getBlockAt(x, y, z);

			if ((target.getType() == Material.AIR)
					&& (target.getRelative(BlockFace.NORTH).getType() == Material.AIR)
					&& (target.getRelative(BlockFace.SOUTH).getType() == Material.AIR)
					&& (target.getRelative(BlockFace.WEST).getType() == Material.AIR)
					&& (target.getRelative(BlockFace.EAST).getType() == Material.AIR))
			{
				target = world.getBlockAt(x, y - 1, z);

				if (defaultspawnblocks.contains(target.getType()))
				{
					target.setType(Material.CACTUS);

					target = world.getBlockAt(x, y, z);

					placed = true;
				}
			}
		} while ((!placed) && (run < 100));

		if (run >= 100)
		{
			plugin.log("Holy battle ground generation FAILED");
			return null;
		}

		return target.getLocation();
	}

	private List<String> getGodsForGetHolyArtifactQuest()
	{
		Set<String> gods = plugin.getGodManager().getGods();
		List<String> questGods = new ArrayList();

		for (String godName : gods)
		{
			if (hasQuest(godName))
			{
				return questGods;
			}

			if (plugin.getBelieverManager().getOnlineBelieversForGod(godName).size() > 0)
			{
				questGods.add(godName);
			}
		}

		if (questGods.size() < 2)
		{
			questGods.clear();
		}

		return questGods;
	}

	private List<String> getGodsForHolyBattleQuest()
	{
		Set<String> gods = plugin.getGodManager().getGods();
		List<String> questGods = new ArrayList();

		for (String godName : gods)
		{
			List enemyGodNames = plugin.getGodManager().getEnemyGodsForGod(godName);

			if ((enemyGodNames.size() != 0)
					&& (this.plugin.getBelieverManager()
							.getOnlineBelieversForGod(godName).size() != 0))
			{
				questGods.add(godName);
				questGods.add((String) enemyGodNames.get(this.random
						.nextInt(questGods.size())));
				return questGods;
			}
		}
		return questGods;
	}

	private boolean generateGlobalPilgrimageQuest(List<String> godNames)
	{
		return false;
	}

	private boolean generateGlobalGetHolyArtifactQuest(List<String> godNames)
	{
		if (godNames.size() == 0)
		{
			return false;
		}

		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.GETHOLYARTIFACT;
		questMaxDuration = 90;

		Location holyArtifactTarget = null;

		String godName = (String) godNames.get(this.random.nextInt(godNames.size()));

		Set<String> playerNames = plugin.getBelieverManager().getOnlineBelieversForGod(godName);

		if (playerNames.size() == 0)
		{
			return false;
		}

		Player player = plugin.getServer().getPlayer((String) playerNames.toArray()[0]);
		String worldName = player.getWorld().getName();

		Location center = player.getLocation();

		ItemStack item = null;
		switch (random.nextInt(9))
		{
			case 0:
				item = new ItemStack(Material.SHEARS);
				break;
			case 1:
				item = new ItemStack(Material.FISHING_ROD);
				break;
			case 2:
				item = new ItemStack(Material.STICK);
				break;
			case 3:
				item = new ItemStack(Material.GOLD_SPADE);
				break;
			case 4:
				item = new ItemStack(Material.TORCH);
				break;
			case 5:
				item = new ItemStack(Material.WATCH);
				break;
			case 6:
				item = new ItemStack(Material.GOLD_SWORD);
				break;
			case 7:
				item = new ItemStack(Material.GOLD_BOOTS);
				break;
			case 8:
				item = new ItemStack(Material.GOLD_RECORD);
				break;
		}

		switch (random.nextInt(1))
		{
			case 0:
				holyArtifactTarget = generateAncientCaveTemple(item, godName, worldName, 2500, 10000, center);
				break;
			case 1:
				holyArtifactTarget = getPositionForLostCity(godName, worldName, 2500, 10000, center);
				break;
		}

		if (holyArtifactTarget == null)
		{
			return false;
		}

		questsConfig.set("Global.Type", questType.toString());
		questsConfig.set("Global.MaxDuration", Integer.valueOf(questMaxDuration));
		questsConfig.set("Global.TargetType", godName);
		questsConfig.set("Global.ItemType", item.getType().name().toUpperCase());
		questsConfig.set("Global.CreatedTime", formatter.format(thisDate));
		questsConfig.set("Global.Location.World", holyArtifactTarget.getWorld().getName());
		questsConfig.set("Global.Location.X", Integer.valueOf(holyArtifactTarget.getBlockX()));
		questsConfig.set("Global.Location.Y", Integer.valueOf(holyArtifactTarget.getBlockY()));
		questsConfig.set("Global.Location.Z", Integer.valueOf(holyArtifactTarget.getBlockZ()));

		save();

		plugin.log("Global quest started: Get Holy Artifact at "
				+ holyArtifactTarget.getBlockX() + ","
				+ holyArtifactTarget.getBlockY() + ","
				+ holyArtifactTarget.getBlockZ() + " in "
				+ holyArtifactTarget.getWorld());

		return true;
	}

	private boolean generateGlobalHolyBattleQuest(List<String> godNames)
	{
		if (godNames.size() == 0)
		{
			return false;
		}

		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.HOLYBATTLE;
		questMaxDuration = 60;

		Location pilgrimageTarget = null;

		Set playerNames = this.plugin.getBelieverManager()
				.getOnlineBelieversForGod((String) godNames.get(0));

		if (playerNames.size() == 0)
		{
			return false;
		}

		Player player = this.plugin.getServer().getPlayer(
				(String) playerNames.toArray()[0]);

		String worldName = player.getWorld().getName();

		Location center = player.getLocation();

		int minDist = 500;
		int maxDist = 3000;

		switch (this.random.nextInt(1))
		{
			case 0:
				pilgrimageTarget = getPositionForHolyBattle(worldName, 500,
						1500, center);
		}

		if (pilgrimageTarget == null)
		{
			return false;
		}

		String templeName = "Arnor";

		this.questsConfig.set("Global.Type", questType.toString());
		this.questsConfig.set("Global.MaxDuration",
				Integer.valueOf(questMaxDuration));
		this.questsConfig.set("Global.TargetType", templeName);
		this.questsConfig.set("Global.CreatedTime", formatter.format(thisDate));
		this.questsConfig.set("Global.Location.World", pilgrimageTarget
				.getWorld().getName());
		this.questsConfig.set("Global.Location.X",
				Integer.valueOf(pilgrimageTarget.getBlockX()));
		this.questsConfig.set("Global.Location.Y",
				Integer.valueOf(pilgrimageTarget.getBlockY()));
		this.questsConfig.set("Global.Location.Z",
				Integer.valueOf(pilgrimageTarget.getBlockZ()));

		save();

		this.plugin.log("Global quest started: Holy battle at "
				+ pilgrimageTarget.getBlockX() + ","
				+ pilgrimageTarget.getBlockY() + ","
				+ pilgrimageTarget.getBlockZ() + " in "
				+ pilgrimageTarget.getWorld());

		return true;
	}

	private boolean generateGlobalClaimHolyLandQuest(List<String> godNames)
	{
		if (godNames.size() == 0)
		{
			return false;
		}

		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.CLAIMHOLYLAND;
		questMaxDuration = 60;

		Location pilgrimageTarget = null;

		Set playerNames = plugin.getBelieverManager().getOnlineBelieversForGod((String) godNames.get(0));

		if (playerNames.size() == 0)
		{
			return false;
		}

		Player player = this.plugin.getServer().getPlayer(
				(String) playerNames.toArray()[0]);

		String worldName = player.getWorld().getName();

		Location center = player.getLocation();

		int minDist = 500;
		int maxDist = 3000;

		switch (this.random.nextInt(1))
		{
			case 0:
				pilgrimageTarget = getPositionForLostHolyLand(worldName, 500,
						1500, center);
		}

		if (pilgrimageTarget == null)
		{
			return false;
		}

		String templeName = "Arnor";

		this.questsConfig.set("Global.Type", questType.toString());
		this.questsConfig.set("Global.MaxDuration",
				Integer.valueOf(questMaxDuration));
		this.questsConfig.set("Global.TargetType", templeName);
		this.questsConfig.set("Global.CreatedTime", formatter.format(thisDate));
		this.questsConfig.set("Global.Location.World", pilgrimageTarget
				.getWorld().getName());
		this.questsConfig.set("Global.Location.X",
				Integer.valueOf(pilgrimageTarget.getBlockX()));
		this.questsConfig.set("Global.Location.Y",
				Integer.valueOf(pilgrimageTarget.getBlockY()));
		this.questsConfig.set("Global.Location.Z",
				Integer.valueOf(pilgrimageTarget.getBlockZ()));

		save();

		this.plugin.log("Global quest started: Claim holy land at "
				+ pilgrimageTarget.getBlockX() + ","
				+ pilgrimageTarget.getBlockY() + ","
				+ pilgrimageTarget.getBlockZ() + " in "
				+ pilgrimageTarget.getWorld());

		return true;
	}

	private boolean generateSlayQuest(String godName)
	{
		int questAmount = 0;
		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.SLAY;
		questAmount = 1
				+ this.random.nextInt(3)
				+ this.plugin.getBelieverManager()
						.getOnlineBelieversForGod(godName).size();
		questMaxDuration = (5 + this.random.nextInt(5)) * questAmount;

		EntityType holyCreature = plugin.getGodManager().getUnholyMobTypeForGod(godName);
		EntityType targetType = EntityType.UNKNOWN;

		do
		{
			switch (random.nextInt(10))
			{
				case 0: targetType = EntityType.SHEEP; break;
				case 1: targetType = EntityType.SPIDER; break;
				case 2: targetType = EntityType.CHICKEN; break;
				case 3: targetType = EntityType.COW; break;
				case 4: targetType = EntityType.PIG; break;
				case 5:
				case 6:
				case 7:
				case 8:
				case 9: targetType = plugin.getGodManager().getUnholyMobTypeForGod(godName);
			}
		} while (targetType == holyCreature);

		this.questsConfig.set(godName + ".Type", questType.toString());
		this.questsConfig.set(godName + ".Amount", Integer.valueOf(questAmount));
		this.questsConfig.set(godName + ".TargetType", targetType.name());
		this.questsConfig.set(godName + ".MaxDuration", Integer.valueOf(questMaxDuration));
		this.questsConfig.set(godName + ".Progress", null);
		this.questsConfig.set(godName + ".CreatedTime", formatter.format(thisDate));

		save();

		plugin.log(godName + " issued a quest: Kill " + questAmount + " "
				+ targetType.name());

		return true;
	}

	private boolean generateBuildTowerQuest(String godName)
	{
		String questTargetType = "NONE";
		int questAmount = 0;
		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.BUILDTOWER;
		questAmount = 6
				+ (int) this.plugin.getGodManager().getGodPower(godName)
				/ 2
				+ 2
				* this.plugin.getBelieverManager()
						.getOnlineBelieversForGod(godName).size();
		questMaxDuration = (1 + this.random.nextInt(5)) * questAmount;

		switch (this.random.nextInt(4))
		{
			case 0:
				questTargetType = Material.SMOOTH_BRICK.name();
				break;
			case 1:
				questTargetType = Material.COBBLESTONE.name();
				break;
			case 2:
				questTargetType = Material.STONE.name();
				break;
			case 3:
				questTargetType = Material.DIRT.name();
		}

		this.questsConfig.set(godName + ".Type", questType.toString());
		this.questsConfig.set(godName + ".Amount", Integer.valueOf(questAmount));
		this.questsConfig.set(godName + ".TargetType", questTargetType);
		this.questsConfig.set(godName + ".MaxDuration", Integer.valueOf(questMaxDuration));
		this.questsConfig.set(godName + ".Progress", null);
		this.questsConfig.set(godName + ".CreatedTime", formatter.format(thisDate));

		save();

		plugin.log(godName + " issued a quest: Build a " + questAmount + " high tower of " + questTargetType);

		return true;
	}

	private boolean generateConvertQuest(String godName)
	{
		int questAmount = 0;
		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.CONVERT;
		questAmount = 1 + (plugin.getServer().getOnlinePlayers().length - plugin.getBelieverManager().getOnlineBelieversForGod(godName).size()) / 5;
		questMaxDuration = (5 + random.nextInt(5)) * questAmount;

		questsConfig.set(godName + ".Type", questType.toString());
		questsConfig.set(godName + ".Amount", Integer.valueOf(questAmount));
		questsConfig.set(godName + ".TargetType", null);
		questsConfig.set(godName + ".MaxDuration",Integer.valueOf(questMaxDuration));
		questsConfig.set(godName + ".Progress", null);
		questsConfig.set(godName + ".CreatedTime", formatter.format(thisDate));

		save();

		plugin.log(godName + " issued a quest: Convert " + questAmount + " players");

		return true;
	}

	private boolean generateHolyFeastQuest(String godName)
	{
		String questTargetType = plugin.getGodManager().getEatFoodTypeForGod(godName).name();
		int questAmount = 0;
		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.HOLYFEAST;
		questAmount = 1 + (int) this.plugin.getGodManager().getGodPower(godName) / 50 + 1 * this.plugin.getBelieverManager().getOnlineBelieversForGod(godName).size();

		questMaxDuration = (1 + this.random.nextInt(5)) * questAmount;

		this.questsConfig.set(godName + ".Type", questType.toString());
		this.questsConfig.set(godName + ".Amount", Integer.valueOf(questAmount));
		this.questsConfig.set(godName + ".TargetType", questTargetType);
		this.questsConfig.set(godName + ".MaxDuration", Integer.valueOf(questMaxDuration));
		this.questsConfig.set(godName + ".Progress", null);
		this.questsConfig.set(godName + ".CreatedTime", formatter.format(thisDate));

		save();

		plugin.log(godName + " issued a quest: Feast " + questAmount + " of " + questTargetType);

		return true;
	}

	private boolean generateSacrificeQuest(String godName)
	{
		String questTargetType = "NONE";
		int questAmount = 0;
		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.ITEMSACRIFICE;
		questAmount = 1 + random.nextInt(1 + plugin.getBelieverManager().getOnlineBelieversForGod(godName).size());
		questMaxDuration = (1 + random.nextInt(5)) * questAmount;

		switch (random.nextInt(9))
		{
			case 0:
				questTargetType = Material.GOLD_SWORD.name();
				break;
			case 1:
				questTargetType = Material.GOLD_PICKAXE.name();
				break;
			case 2:
				questTargetType = Material.GOLD_SPADE.name();
				break;
			case 3:
				questTargetType = Material.GOLD_AXE.name();
				break;
			case 4:
				questTargetType = Material.FISHING_ROD.name();
				break;
			case 5:
				questTargetType = Material.ANVIL.name();
				break;
			case 6:
				questTargetType = Material.BOAT.name();
				break;
			case 7:
				questTargetType = Material.BOOK.name();
				break;
			case 8:
				questTargetType = Material.BOW.name();
		}

		questsConfig.set(godName + ".Type", questType.toString());
		questsConfig.set(godName + ".Amount", Integer.valueOf(questAmount));
		questsConfig.set(godName + ".TargetType", questTargetType);
		questsConfig.set(godName + ".MaxDuration", Integer.valueOf(questMaxDuration));
		questsConfig.set(godName + ".Progress", null);
		questsConfig.set(godName + ".CreatedTime", formatter.format(thisDate));

		save();
		
		// Make sure the good wants this

		plugin.log(godName + " issued a quest: Sacrifice " + questAmount + " " + questTargetType);

		return true;
	}

	private boolean generateGiveBiblesQuest(String godName)
	{
		String questTargetType = "NONE";
		int questAmount = 0;
		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.GIVEBIBLES;
		questAmount = 1 + (this.plugin.getServer().getOnlinePlayers().length - this.plugin.getBelieverManager().getOnlineBelieversForGod(godName).size()) / (2 + this.random.nextInt(3));
		questMaxDuration = (1 + this.random.nextInt(5)) * questAmount;

		this.questsConfig.set(godName + ".Type", questType.toString());
		this.questsConfig.set(godName + ".Amount", Integer.valueOf(questAmount));
		this.questsConfig.set(godName + ".TargetType", questTargetType);
		this.questsConfig.set(godName + ".MaxDuration", Integer.valueOf(questMaxDuration));
		this.questsConfig.set(godName + ".Progress", null);
		this.questsConfig.set(godName + ".CreatedTime", formatter.format(thisDate));

		save();

		this.plugin.log(godName + " issued a quest: Make " + questAmount + " non-believers read the Holy Book");

		return true;
	}

	private boolean generateBurnBiblesQuest(String godName)
	{
		String questTargetType = "NONE";
		int questAmount = 0;
		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.BURNBIBLES;
		questAmount = 1 + 1 * this.plugin.getBelieverManager()
				.getOnlineBelieversForGod(godName).size();
		questMaxDuration = (1 + this.random.nextInt(5)) * questAmount;

		List enemyGods = this.plugin.getGodManager()
				.getEnemyGodsForGod(godName);

		if (enemyGods.size() == 0)
		{
			return false;
		}

		questTargetType = (String) enemyGods.get(this.random.nextInt(enemyGods
				.size()));

		this.questsConfig.set(godName + ".Type", questType.toString());
		this.questsConfig
				.set(godName + ".Amount", Integer.valueOf(questAmount));
		this.questsConfig.set(godName + ".TargetType", questTargetType);
		this.questsConfig.set(godName + ".MaxDuration",
				Integer.valueOf(questMaxDuration));
		this.questsConfig.set(godName + ".Progress", null);
		this.questsConfig.set(godName + ".CreatedTime",
				formatter.format(thisDate));

		save();

		this.plugin.log(godName + " issued a quest: Burn " + questAmount + " "
				+ questTargetType);

		return true;
	}

	private boolean generateCrusadeQuest(String godName)
	{
		String questTargetType = "NONE";
		int questAmount = 0;
		int questMaxDuration = 0;
		Date thisDate = new Date();
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		QUESTTYPE questType = QUESTTYPE.CRUSADE;
		questAmount = 1 + this.plugin.getBelieverManager().getOnlineBelieversForGod(godName).size();
		questMaxDuration = (5 + this.random.nextInt(5)) * questAmount;

		List<String> enemyGods = this.plugin.getGodManager().getEnemyGodsForGod(godName);

		if (enemyGods.size() == 0)
		{
			return false;
		}

		questTargetType = (String) enemyGods.get(this.random.nextInt(enemyGods.size()));

		this.questsConfig.set(godName + ".Type", questType.toString());
		this.questsConfig.set(godName + ".Amount", Integer.valueOf(questAmount));
		this.questsConfig.set(godName + ".TargetType", questTargetType);
		this.questsConfig.set(godName + ".MaxDuration", Integer.valueOf(questMaxDuration));
		this.questsConfig.set(godName + ".Progress", null);
		this.questsConfig.set(godName + ".CreatedTime", formatter.format(thisDate));

		save();

		this.plugin.log(godName + " issued a quest: Kill " + questAmount + " non-believers!");

		return true;
	}

	public boolean generateGlobalQuest()
	{
		if (!plugin.globalQuestsEnabled)
		{
			return false;
		}

		if (plugin.getQuestManager().hasGlobalQuest())
		{
			return false;
		}

		List<String> godNames = getGodsForGetHolyArtifactQuest();

		if (generateGlobalGetHolyArtifactQuest(godNames))
		{
			GodSayNewGlobalQuest();

			return true;
		}

		godNames = getGodsForHolyBattleQuest();

		if (generateGlobalHolyBattleQuest(godNames))
		{
			GodSayNewGlobalQuest();

			return true;
		}

		return false;
	}

	public boolean generateQuest(String godName)
	{
		boolean newQuest = false;

		if (hasQuest(godName))
		{
			return false;
		}

		int t = 0;
		do
		{
			switch (random.nextInt(8))
			{
				case 0:
					if (plugin.slayQuestsEnabled)
						newQuest = generateSlayQuest(godName);
					break;
				case 1:
					if ((plugin.sacrificeQuestsEnabled) && (this.plugin.sacrificesEnabled))
						newQuest = generateSacrificeQuest(godName);
					break;
				case 2:
					if (plugin.convertQuestsEnabled)
						newQuest = generateConvertQuest(godName);
					break;
				case 3:
					if (plugin.holyFeastQuestsEnabled)
						newQuest = generateHolyFeastQuest(godName);
					break;
				case 4:
					if ((plugin.biblesEnabled) && (plugin.giveBiblesQuestsEnabled))
						newQuest = generateGiveBiblesQuest(godName);
					break;
				case 5:
					if ((plugin.biblesEnabled) && (plugin.burnBiblesQuestsEnabled))
						newQuest = generateBurnBiblesQuest(godName);
					break;
				case 6:
					if (plugin.crusadeQuestsEnabled)
						newQuest = generateCrusadeQuest(godName);
					break;
				case 7:
					if (plugin.buildTowerQuestsEnabled)
						newQuest = generateBuildTowerQuest(godName);
					break;
			}

			t++;
		} while ((!newQuest) && (t < 10));

		if (newQuest)
		{
			godSayNewQuest(godName);
		}
		else
		{
			plugin.logDebug("Could not generate any quest");
		}

		return newQuest;
	}

	private void godSayNewQuest(String godName)
	{
		int amount = getQuestAmountForGod(godName);
		String questTargetType = getQuestTargetTypeForGod(godName);

		QUESTTYPE questType = getQuestTypeForGod(godName);

		plugin.getLanguageManager().setAmount(amount);

		switch (questType)
		{
			case CRUSADE:
				plugin.getLanguageManager().setType(plugin.getLanguageManager().getMobTypeName(EntityType.fromName(questTargetType)));
				plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversCrusadeQuestStarted, 2 + this.random.nextInt(100));
				break;
			case DELIVERITEM:
				this.plugin.getLanguageManager().setType(questTargetType);
				this.plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversBuildAltarsQuestStarted, 2 + this.random.nextInt(100));
				break;
			case GETHOLYARTIFACT:
				this.plugin.getLanguageManager().setType(questTargetType);
				this.plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactThisQuestStarted, 2 + this.random.nextInt(100));
				break;
			case HARVEST:
				//this.plugin.getLanguageManager().setType(questTargetType);
				//this.plugin.getGodManager().GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHarvestQuestStarted, 2 + this.random.nextInt(100));
				//break;
			case CLAIMHOLYLAND:
				this.plugin.getLanguageManager().setType(questTargetType);
				this.plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversClaimHolyLandQuestStarted, 2 + this.random.nextInt(100));
				break;
			case GIVEBIBLES:
				plugin.getLanguageManager().setType(questTargetType);
				plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGiveBiblesQuestStarted, 2 + this.random.nextInt(100));
				break;
			case KILLBOSS:
				plugin.getLanguageManager().setType(questTargetType);
				//this.plugin.getGodManager().GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.g, 2 + this.random.nextInt(100));
				break;
			case HOLYFEAST:
				plugin.getLanguageManager().setType(questTargetType);
				plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHolyFeastQuestStarted, 2 + random.nextInt(100));
				break;
			case SLAY:
				plugin.getLanguageManager().setType(questTargetType);
				plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSlayQuestStarted, 2 + random.nextInt(100)); 
				break;				
			case COLLECTBIBLES:
			case CONVERT:
				plugin.getLanguageManager().setType(questTargetType);
				plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversConvertQuestStarted, 2 + random.nextInt(100));
				break;
			case HOLYBATTLE:
				this.plugin.getLanguageManager().setType(questTargetType);
				this.plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHolyBattleQuestStarted, 2 + random.nextInt(100));
				break;
			case NONE:
			case PILGRIMAGE:
				plugin.getLanguageManager().setType(questTargetType);
				plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPilgrimageQuestStarted, 2 + random.nextInt(100));
				break;
			case SACRIFICE:
				plugin.getLanguageManager().setType(questTargetType);
				plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSacrificeQuestStarted, 2 + random.nextInt(100));
				break;
		}
	}

	private void GodSayNewGlobalQuest()
	{
		String questTargetType = getGlobalQuestTargetType();

		QUESTTYPE questType = getGlobalQuestType();

		for (String godName : this.plugin.getGodManager().getGods())
		{
			switch (questType)
			{
				case GETHOLYARTIFACT: 
				{
					plugin.getLanguageManager().setType("Holy Artifact");

					if (godName.equals(questTargetType))
					{
						plugin.getLanguageManager().setPlayerName(questTargetType);

						plugin.getGodManager()
								.godSayToBelievers(
										godName,
										LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactThisQuestStarted,
										2 + this.random.nextInt(100));
					} 
					else
					{
						plugin.getLanguageManager().setPlayerName(questTargetType);
						plugin.getGodManager()
								.godSayToBelievers(
										godName,
										LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactOtherQuestStarted,
										2 + this.random.nextInt(100));
					}
				} break;

				case CLAIMHOLYLAND: 
				{
					plugin.getLanguageManager().setType(questTargetType);

					plugin.getGodManager()
							.godSayToBelievers(
									godName,
									LanguageManager.LANGUAGESTRING.GodToBelieversClaimHolyLandQuestStarted,
									2 + this.random.nextInt(100));
				}
					break;

				case HOLYBATTLE: {
					plugin.getLanguageManager().setType(questTargetType);
					plugin.getGodManager()
							.godSayToBelievers(
									godName,
									LanguageManager.LANGUAGESTRING.GodToBelieversHolyBattleQuestStarted,
									2 + this.random.nextInt(100));
				}
					break;

			}
		}
	}

	private boolean addQuestProgressForGod(String godName)
	{
		boolean complete = false;

		int questAmount = this.questsConfig.getInt(godName + ".Amount");
		int questProgress = this.questsConfig.getInt(godName + ".Progress");

		questProgress++;

		complete = questProgress >= questAmount;

		this.questsConfig.set(godName + ".Progress", Integer.valueOf(questProgress));

		save();

		return complete;
	}

	private boolean addQuestPlayerProgressForGod(String godName, String playerName)
	{
		boolean complete = false;

		List<String> players = this.questsConfig.getStringList(godName + ".Players");

		if (players.contains(playerName))
		{
			return false;
		}

		players.add(playerName);

		int questAmount = this.questsConfig.getInt(godName + ".Amount");

		complete = players.size() >= questAmount;

		questsConfig.set(godName + ".Players", players);
		questsConfig.set(godName + ".Progress", Integer.valueOf(players.size()));

		save();

		return complete;
	}

	public void setDominationColor(Block block, ChatColor color)
	{
		block.setType(Material.WOOL);
		block.getRelative(BlockFace.UP).setType(Material.STONE_PLATE);
		block.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST)
				.setType(Material.WOOL);
		block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST)
				.setType(Material.WOOL);
		block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST)
				.setType(Material.WOOL);
		block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST)
				.setType(Material.WOOL);
		block.getRelative(BlockFace.NORTH).setType(Material.REDSTONE_LAMP_OFF);
		block.getRelative(BlockFace.EAST).setType(Material.REDSTONE_LAMP_OFF);
		block.getRelative(BlockFace.WEST).setType(Material.REDSTONE_LAMP_OFF);
		block.getRelative(BlockFace.SOUTH).setType(Material.REDSTONE_LAMP_OFF);
	}

	public boolean handleJoinReligion(String playerName, String godName)
	{
		if (!hasQuest(godName))
		{
			return false;
		}

		QUESTTYPE questType = getQuestTypeForGod(godName);
		String questTargetType = getQuestTargetTypeForGod(godName);
		boolean complete = false;

		if ((questType == null) || (questType != QUESTTYPE.CONVERT))
		{
			return false;
		}

		complete = addQuestProgressForGod(godName);

		if (complete)
		{
			if (this.plugin.biblesEnabled)
			{
				plugin.getBibleManager().handleQuestCompleted(godName, questType, playerName);
			}

			plugin.getLanguageManager().setPlayerName(playerName);
			plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversConvertQuestCompleted, 2 + random.nextInt(10));

			plugin.getGodManager().addMoodForGod(godName, 3*plugin.getGodManager().getPleasedModifierForGod(godName));
			plugin.getGodManager().addBeliefAndRewardBelievers(godName);
			removeQuestForGod(godName);
		} 
		else
		{
			godSayProgress(godName);
		}

		return true;
	}

	public boolean handlePressurePlate(String playerName, Block block)
	{
		if (block == null)
		{
			return false;
		}

		if (plugin.getQuestManager().hasGlobalQuestType(QUESTTYPE.CLAIMHOLYLAND))
		{
			if (plugin.getQuestManager().isGlobalQuestTarget(block.getLocation()))
			{
				String godName = plugin.getBelieverManager().getGodForBeliever(playerName);

				if (godName != null)
				{
					plugin.getQuestManager().setDominationColor(block, plugin.getGodManager().getColorForGod(godName));
				}
			}
		}

		return false;
	}

	public boolean handleOpenChest(String playerName, Location blockLocation)
	{
		if (plugin.getQuestManager().isGlobalQuestTarget(blockLocation))
		{
			String lockedGodName = plugin.getQuestManager().getGlobalQuestTargetLockedGodName();
			String questGod = plugin.getQuestManager().getGlobalQuestTargetType();
			String godName = plugin.getBelieverManager().getGodForBeliever(playerName);

			if (lockedGodName != null)
			{
				if ((godName != null) && (godName.equals(lockedGodName)))
				{
					return false;
				}

				plugin
						.getServer()
						.getPlayer(playerName)
						.sendMessage(
								ChatColor.RED
										+ "This chest has already been claimed by a follower of "
										+ ChatColor.GOLD + lockedGodName
										+ ChatColor.RED + "!");

				return true;
			}

			if (godName != null)
			{
				plugin.getLanguageManager().setType("Holy Artifact");
				plugin.getLanguageManager().setPlayerName(getGlobalQuestTargetType());

				if (godName.equals(questGod))
				{
					plugin.getGodManager().godSayToBelievers(questGod, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactThisQuestCompleted, 2);
					plugin.getGodManager().OtherGodSayToBelievers(questGod, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactOtherQuestFailed, 10);
				} 
				else
				{
					plugin.getGodManager().godSayToBelievers(questGod, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactThisQuestFailed, 2);
					plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactOtherQuestCompleted, 10);
				}

				plugin.getGodManager().addMoodForGod(godName, 3*plugin.getGodManager().getPleasedModifierForGod(godName));
				plugin.getGodManager().addBeliefAndRewardBelievers(godName);

				plugin.getServer().broadcastMessage(ChatColor.WHITE + playerName + ChatColor.AQUA + " found the lost artifact of " + ChatColor.GOLD + godName);

				setGlobalQuestCompletedBy(godName);

				return false;
			}

			return true;
		}

		return false;
	}

	public void handlePrayer(String godName, String playerName)
	{
		if (!hasQuest(godName))
		{
			return;
		}

		QUESTTYPE questType = getQuestTypeForGod(godName);
		boolean complete = false;

		if ((questType == null) || (questType != QUESTTYPE.CONVERT))
		{
			return;
		}

		String playerGod = plugin.getBelieverManager().getGodForBeliever(playerName);

		if (playerGod != null && playerGod.equals(godName))
		{
			return;
		}

		complete = addQuestPlayerProgressForGod(godName, playerName);

		if (complete)
		{
			plugin.getLanguageManager().setType(plugin.getBibleManager().getBibleTitle(godName));
			plugin.getLanguageManager().setPlayerName(playerName);
			plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversConvertQuestCompleted, 2 + random.nextInt(10));

			plugin.getGodManager().addMoodForGod(godName, 3*plugin.getGodManager().getPleasedModifierForGod(godName));
			plugin.getGodManager().addBeliefAndRewardBelievers(godName);
			
			removeQuestForGod(godName);
		}
		else
		{
			godSayProgress(godName);
		}
	}

	public void handleReadBible(String godName, String playerName)
	{
		if (!hasQuest(godName))
		{
			return;
		}

		QUESTTYPE questType = getQuestTypeForGod(godName);
		boolean complete = false;

		if ((questType == null) || (questType != QUESTTYPE.GIVEBIBLES))
		{
			return;
		}

		String playerGod = this.plugin.getBelieverManager().getGodForBeliever(playerName);

		if (playerGod != null && playerGod.equals(godName))
		{
			return;
		}

		complete = addQuestPlayerProgressForGod(godName, playerName);

		if (complete)
		{
			plugin.getLanguageManager().setType(plugin.getBibleManager().getBibleTitle(godName));
			plugin.getLanguageManager().setPlayerName(playerName);
			plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGiveBiblesQuestCompleted, 2 + random.nextInt(10));

			plugin.getGodManager().addMoodForGod(godName, 3*plugin.getGodManager().getPleasedModifierForGod(godName));
			plugin.getGodManager().addBeliefAndRewardBelievers(godName);
			
			removeQuestForGod(godName);
		}
		else
		{
			godSayProgress(godName);
		}
	}

	public void handleBibleMelee(String godName, String playerName)
	{
		if (!hasGlobalQuestType(QUESTTYPE.GETHOLYARTIFACT))
		{
			return;
		}

		Location pilgrimageLocation = getGlobalQuestLocation();

		String playerGod = this.plugin.getBelieverManager().getGodForBeliever(playerName);

		if ((playerGod == null) || (pilgrimageLocation == null))
		{
			return;
		}

		plugin.getGodManager().spawnGuidingMobs(godName, playerName, pilgrimageLocation);
	}

	public void handleKilledPlayer(String playerName, String playerGod)
	{
		for (String godName : questsConfig.getKeys(false))
		{
			if (!godName.equals(playerGod))
			{
				if (!hasQuest(godName))
				{
					return;
				}

				QUESTTYPE questType = getQuestTypeForGod(godName);
				String questTargetType = getQuestTargetTypeForGod(godName);
				boolean complete = false;

				if ((questType != null) && (questType == QUESTTYPE.CRUSADE) && (questTargetType != null))
				{
					complete = addQuestProgressForGod(godName);

					if (complete)
					{
						this.plugin
								.getGodManager()
								.godSayToBelievers(
										godName,
										LanguageManager.LANGUAGESTRING.GodToBelieversCrusadeQuestCompleted,
										2 + this.random.nextInt(10));

						plugin.getGodManager().addMoodForGod(godName, 3*plugin.getGodManager().getPleasedModifierForGod(godName));
						plugin.getGodManager().addBeliefAndRewardBelievers(godName);
						removeQuestForGod(godName);
					} else
					{
						godSayProgress(godName);
					}
				}
			}
		}
	}

	public void handleKilledMob(String godName, String mobType)
	{
		if (!hasQuest(godName))
		{
			return;
		}

		QUESTTYPE questType = getQuestTypeForGod(godName);
		String questTargetType = getQuestTargetTypeForGod(godName);
		boolean complete = false;

		if ((questType == null) || (questType != QUESTTYPE.SLAY))
		{
			return;
		}

		if (questTargetType == null)
		{
			return;
		}

		if (!questTargetType.equalsIgnoreCase(mobType))
		{
			return;
		}

		complete = addQuestProgressForGod(godName);

		if (complete)
		{
			this.plugin
					.getGodManager()
					.godSayToBelievers(
							godName,
							LanguageManager.LANGUAGESTRING.GodToBelieversSlayQuestCompleted,
							2 + this.random.nextInt(10));

			plugin.getGodManager().addMoodForGod(godName, 3*plugin.getGodManager().getPleasedModifierForGod(godName));
			plugin.getGodManager().addBeliefAndRewardBelievers(godName);
			removeQuestForGod(godName);
		} 
		else
		{
			godSayProgress(godName);
		}
	}

	public Set<Material> getRewardItems()
	{
		return this.rewardValues.keySet();
	}

	public void setItemRewardValue(Material item, int value)
	{
		this.rewardValues.put(item, Integer.valueOf(value));
	}

	public List<ItemStack> getRewardsForQuestCompletion(String godName)
	{
		List rewards = new ArrayList();

		int power = (int) plugin.getGodManager().getGodPower(godName);

		while (power > 0)
		{
			int r = random.nextInt(rewardValues.size());

			int value = ((Integer) rewardValues.values().toArray()[r]).intValue();

			if ((value > 0) && (value <= power))
			{
				ItemStack items = new ItemStack((Material) this.rewardValues.keySet().toArray()[r], 1);
				rewards.add(items);
				power -= value;
			}
		}

		return rewards;
	}

	public List<ItemStack> getRewardsForQuestCompletion(int power)
	{
		List rewards = new ArrayList();

		while (power > 0)
		{
			int r = this.random.nextInt(rewardValues.size());

			int value = ((Integer) rewardValues.values().toArray()[r]).intValue();

			if ((value > 0) && (value <= power))
			{
				ItemStack items = new ItemStack((Material) rewardValues.keySet().toArray()[r], 1);
				rewards.add(items);
				power -= value;
			}
		}

		return rewards;
	}

	public boolean handleSacrifice(String godName, String entityType)
	{
		if (!hasQuest(godName))
		{
			return false;
		}

		QUESTTYPE questType = getQuestTypeForGod(godName);
		String questTargetType = getQuestTargetTypeForGod(godName);
		boolean complete = false;

		if ((questType == null) || ((questType != QUESTTYPE.SACRIFICE) && (questType != QUESTTYPE.BURNBIBLES)))
		{
			return false;
		}

		if ((questTargetType == null) || (!questTargetType.equalsIgnoreCase(entityType)))
		{
			return false;
		}

		complete = addQuestProgressForGod(godName);

		if (complete)
		{
			switch (questType)
			{
				case SACRIFICE: 
					plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(Material.getMaterial(questTargetType)));
					plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSacrificeQuestCompleted, 2 + this.random.nextInt(10));
					break;
				case BURNBIBLES:
					this.plugin.getLanguageManager().setPlayerName(questTargetType);
					this.plugin.getLanguageManager().setType(this.plugin.getBibleManager().getBibleTitle(questTargetType));
					this.plugin.getGodManager().godSayToBelievers(godName,LanguageManager.LANGUAGESTRING.GodToBelieversBurnBiblesQuestCompleted, 2 + this.random.nextInt(10));
					break;
			}

			plugin.getGodManager().addMoodForGod(godName, 3*plugin.getGodManager().getPleasedModifierForGod(godName));
			plugin.getGodManager().addBeliefAndRewardBelievers(godName);
			removeQuestForGod(godName);
		} 
		else
		{
			godSayProgress(godName);
		}

		return true;
	}

	public void handleEat(String playerName, String godName, String entityType)
	{

		if (!hasQuest(godName))
		{
			return;
		}

		QUESTTYPE questType = getQuestTypeForGod(godName);
		String questTargetType = getQuestTargetTypeForGod(godName);
		boolean complete = false;

		if (questType == null)
		{
			plugin.logDebug("handleEat(): null quest");
			return;
		}

		if ((questTargetType == null) || !questTargetType.equalsIgnoreCase(entityType))
		{
			plugin.logDebug("handleEat(): null questType");
			return;
		}

		if (questType != QUESTTYPE.HOLYFEAST)
		{
			plugin.logDebug("handleEat(): quest is not feast");
			return;
		}

		plugin.logDebug("handling quest eating for " + godName);

		complete = addQuestProgressForGod(godName);

		if (complete)
		{
			plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHolyFeastQuestCompleted, 2 + this.random.nextInt(10));
		
			plugin.getGodManager().addMoodForGod(godName, 3*plugin.getGodManager().getPleasedModifierForGod(godName));
			plugin.getGodManager().addBeliefAndRewardBelievers(godName);
			removeQuestForGod(godName);
		} 
		else
		{
			godSayProgress(godName);
		}
	}

	public void godsSayStatus()
	{
		String targetType = getGlobalQuestTargetType();

		plugin.getLanguageManager().setType(targetType);
		QUESTTYPE questType = getGlobalQuestType();
				
		for (String godName : this.plugin.getGodManager().getGods())
		{
			switch (questType)
			{
				case CRUSADE:
					this.plugin
							.getGodManager()
							.godSayToBelievers(
									godName,
									LanguageManager.LANGUAGESTRING.GodToBelieversCrusadeQuestStatus,
									2 + this.random.nextInt(100));
					break;
				case DELIVERITEM:
					//plugin.getGodManager().GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.god, 2 + this.random.nextInt(100));
					break;
				case HARVEST:
					//plugin.getGodManager().GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.g, 2 + this.random.nextInt(100));
					break;
				case KILLBOSS:
					// this.plugin.getGodManager().GodSayToBelievers(godName,
					// LanguageManager.LANGUAGESTRING.GodToBelieversKillBossQuestStatus,
					// 2 + this.random.nextInt(100));
					break;
				case HOLYFEAST:
					this.plugin
							.getGodManager()
							.godSayToBelievers(
									godName,
									LanguageManager.LANGUAGESTRING.GodToBelieversHolyFeastQuestStatus,
									2 + this.random.nextInt(100));
					break;
				case GIVEBIBLES:
					this.plugin
							.getGodManager()
							.godSayToBelievers(
									godName,
									LanguageManager.LANGUAGESTRING.GodToBelieversGiveBiblesQuestStatus,
									2 + this.random.nextInt(100));
					break;
				case SLAY:
					this.plugin
							.getGodManager()
							.godSayToBelievers(
									godName,
									LanguageManager.LANGUAGESTRING.GodToBelieversSlayQuestStatus,
									2 + this.random.nextInt(100));
					break;
				case CLAIMHOLYLAND:
					this.plugin
							.getGodManager()
							.godSayToBelievers(
									godName,
									LanguageManager.LANGUAGESTRING.GodToBelieversClaimHolyLandQuestStatus,
									2 + this.random.nextInt(100));
					break;
				case PILGRIMAGE:
					plugin.getGodManager()
							.godSayToBelievers(
									godName,
									LanguageManager.LANGUAGESTRING.GodToBelieversPilgrimageQuestStatus,
									2 + this.random.nextInt(100));
					break;
				case SACRIFICE:
					plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSacrificeQuestStatus, 2 + this.random.nextInt(100));
					break;
				case NONE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPilgrimageQuestStatus, 2 + random.nextInt(100)); break;
				case COLLECTBIBLES:
					plugin.getLanguageManager().setType("Holy Artifact");
					plugin.getLanguageManager().setPlayerName(targetType);

					if (godName.equals(targetType))
					{
						plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactThisQuestStatus, 2 + random.nextInt(100));
					} 
					else
					{
						plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactOtherQuestStatus, 2 + random.nextInt(100));
					}
					break;
				case CONVERT			: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversConvertQuestStatus, 2 + random.nextInt(100)); break;
				case HOLYBATTLE			: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHolyBattleQuestStatus, 2 + random.nextInt(100)); break;
				case GETHOLYARTIFACT	: 
					{
						int delay = 2 + random.nextInt(100);

						plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(getGlobalQuestTargetItemType()));

						plugin.getLanguageManager().setPlayerName(targetType);

						if (godName.equals(targetType))
						{
							plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactThisQuestStatus, delay);
						} 
						else
						{
							plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactOtherQuestStatus, delay);
						}
						
						// If not hunting, say some help
						for(String believerName : plugin.getBelieverManager().getBelieversForGod(godName))
						{														
							if(!plugin.getBelieverManager().isHunting(believerName))
							{
								if(random.nextInt(6)==0)
								{
									plugin.getGodManager().godSayToBeliever(godName, believerName, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactQuestHelp, delay + 20 + random.nextInt(100)); 							
								}
							}
							else
							{
								Location artifactLocation = getGlobalQuestLocation();
								Player player = plugin.getServer().getPlayer(believerName);
																
								if(artifactLocation==null)
								{
									plugin.logDebug("GlobalArtifactQuest ArtifactLocation is null");
									return;									
								}

								if(player==null)
								{
									plugin.logDebug("GlobalArtifactQuest player '" + believerName + "' is null");
									return;									
								}
								
								if(artifactLocation.getWorld().getName().equals(player.getWorld().getName()))
								{
									plugin.logDebug("GlobalArtifactQuest for '" + believerName + "' is wrong world");
									return;
								}
								
								Vector vector = artifactLocation.toVector().subtract(player.getLocation().toVector());
								
								plugin.getLanguageManager().setAmount((int)vector.length());
								plugin.getGodManager().godSayToBeliever(godName, believerName, LanguageManager.LANGUAGESTRING.GodToBelieversGetHolyArtifactQuestRange, delay + 20 + random.nextInt(100)); 											
							}
						}
					} break;
			}
		}
	}

	public void godSayStatus(String godName)
	{
		int amount = getQuestAmountForGod(godName);
		int progress = getQuestProgressForGod(godName);
		String targetType = getQuestTargetTypeForGod(godName);

		this.plugin.getLanguageManager().setAmount(amount - progress);
		this.plugin.getLanguageManager().setType(targetType);

		QUESTTYPE questType = getQuestTypeForGod(godName);

		switch (questType)
		{
			case CRUSADE:
				plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversConvertQuestStatus, 2 + this.random.nextInt(100));
				break;
			case DELIVERITEM: break;
			case HARVEST:
				plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSacrificeQuestStatus, 2 + random.nextInt(100)); break;
			case KILLBOSS:
				this.plugin
						.getGodManager()
						.godSayToBelievers(
								godName,
								LanguageManager.LANGUAGESTRING.GodToBelieversGiveBiblesQuestStatus,
								2 + this.random.nextInt(100));
				break;
			case HOLYFEAST:
				this.plugin
						.getGodManager()
						.godSayToBelievers(
								godName,
								LanguageManager.LANGUAGESTRING.GodToBelieversHolyFeastQuestStatus,
								2 + this.random.nextInt(100));
				break;
			case GIVEBIBLES:
				this.plugin
						.getGodManager()
						.godSayToBelievers(
								godName,
								LanguageManager.LANGUAGESTRING.GodToBelieversGiveBiblesQuestStatus,
								2 + this.random.nextInt(100));
				break;
			case SLAY: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSlayQuestStatus, 2 + random.nextInt(100)); break;
			case CLAIMHOLYLAND:
				this.plugin
						.getGodManager()
						.godSayToBelievers(
								godName,
								LanguageManager.LANGUAGESTRING.GodToBelieversClaimHolyLandQuestStatus,
								2 + this.random.nextInt(100));
				break;
			case PILGRIMAGE:
				plugin.getGodManager()
						.godSayToBelievers(
								godName,
								LanguageManager.LANGUAGESTRING.GodToBelieversPilgrimageQuestStatus,
								2 + this.random.nextInt(100));
				break;
			case SACRIFICE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSacrificeQuestStatus, 2 + this.random.nextInt(100)); break;
			case NONE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPilgrimageQuestStatus, 2 + this.random.nextInt(100)); break;
			case COLLECTBIBLES:
			case CONVERT: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversConvertQuestStatus, 2 + this.random.nextInt(100)); break;
			case GETHOLYARTIFACT:
			case HOLYBATTLE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHolyBattleQuestStatus, 2 + this.random.nextInt(100)); break;
		}
	}

	public void godSayProgress(String godName)
	{
		int amount = getQuestAmountForGod(godName);
		int progress = getQuestProgressForGod(godName);
		String targetType = getQuestTargetTypeForGod(godName);

		this.plugin.getLanguageManager().setAmount(amount - progress);
		this.plugin.getLanguageManager().setType(targetType);

		QUESTTYPE questType = getQuestTypeForGod(godName);

		switch (questType)
		{
			case CRUSADE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversCrusadeQuestProgress, 2 + random.nextInt(10)); break;
			case DELIVERITEM: //plugin.getGodManager().GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversBuildAltarsQuestProgress, 2 + random.nextInt(10)); break;
			case HARVEST: //plugin.getGodManager().GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHarvestQuestProgress, 2 + this.random.nextInt(10)); break;
			case KILLBOSS: //plugin.getGodManager().GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGiveBiblesQuestProgress, 2 + random.nextInt(100)); break;
			case HOLYFEAST: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHolyFeastQuestProgress, 2 + random.nextInt(100)); break;
			case GIVEBIBLES: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGiveBiblesQuestProgress, 2 + random.nextInt(100)); break;
			case SLAY: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSlayQuestProgress, 2 + random.nextInt(100)); break;
			case CLAIMHOLYLAND: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversClaimHolyLandQuestProgress, 2 + random.nextInt(100)); break;
			case SACRIFICE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSacrificeQuestProgress, 2 + random.nextInt(100)); break;
			case NONE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPilgrimageQuestProgress, 2 + random.nextInt(100)); break;
			case COLLECTBIBLES:
			case CONVERT: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversConvertQuestProgress, 2 + random.nextInt(100)); break;
			case GETHOLYARTIFACT: break;
			case HOLYBATTLE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHolyBattleQuestProgress, 2 + random.nextInt(100)); break;
			case PILGRIMAGE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPilgrimageQuestProgress, 2 + random.nextInt(100)); break;
		}
	}
	
	public void godSayFailed(String godName)
	{
		int amount = getQuestAmountForGod(godName);
		int progress = getQuestProgressForGod(godName);
		String targetType = getQuestTargetTypeForGod(godName);

		this.plugin.getLanguageManager().setAmount(amount - progress);
		this.plugin.getLanguageManager().setType(targetType);

		QUESTTYPE questType = getQuestTypeForGod(godName);

		switch (questType)
		{
			case CRUSADE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversCrusadeQuestFailed, 2 + random.nextInt(10)); break;
			case DELIVERITEM: //plugin.getGodManager().GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversBuildAltarsQuestProgress, 2 + random.nextInt(10)); break;
			case HARVEST: //plugin.getGodManager().GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHarvestQuestProgress, 2 + this.random.nextInt(10)); break;
			case KILLBOSS: //plugin.getGodManager().GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGiveBiblesQuestProgress, 2 + random.nextInt(100)); break;
			case HOLYFEAST: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHolyFeastQuestFailed, 2 + random.nextInt(100)); break;
			case GIVEBIBLES: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversGiveBiblesQuestFailed, 2 + random.nextInt(100)); break;
			case SLAY: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSlayQuestFailed, 2 + random.nextInt(100)); break;
			case CLAIMHOLYLAND: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversClaimHolyLandQuestFailed, 2 + random.nextInt(100)); break;
			case SACRIFICE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSacrificeQuestFailed, 2 + random.nextInt(100)); break;
			case NONE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPilgrimageQuestFailed, 2 + random.nextInt(100)); break;
			case COLLECTBIBLES:
			case CONVERT: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversConvertQuestFailed, 2 + random.nextInt(100)); break;
			case GETHOLYARTIFACT: break;
			case HOLYBATTLE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversHolyBattleQuestFailed, 2 + random.nextInt(100)); break;
			case PILGRIMAGE: plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPilgrimageQuestFailed, 2 + random.nextInt(100)); break;
		}
	}


	public void handleBuiltPrayingAltar(String godName)
	{
		if (!hasQuest(godName))
		{
			return;
		}

		QUESTTYPE questType = getQuestTypeForGod(godName);

		boolean complete = false;

		if ((questType == null) || (questType != QUESTTYPE.BUILDALTARS))
		{
			return;
		}

		complete = addQuestProgressForGod(godName);

		if (complete)
		{
			plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversBuildAltarsQuestCompleted, 2 + random.nextInt(10));
			plugin.getGodManager().addBeliefAndRewardBelievers(godName);

			removeQuestForGod(godName);
		} 
		else
		{
			int amount = getQuestAmountForGod(godName);
			int progress = getQuestProgressForGod(godName);

			this.plugin.getLanguageManager().setAmount(amount - progress);
			this.plugin
					.getGodManager()
					.godSayToBelievers(
							godName,
							LanguageManager.LANGUAGESTRING.GodToBelieversBuildAltarsQuestProgress,
							2 + this.random.nextInt(10));
		}
	}

	static enum QUESTTYPE
	{
		NONE, 
		FIREWORKPARTY, 
		HUMANSACRIFICE, 
		SACRIFICE, 
		MOBSACRIFICE, 
		ITEMSACRIFICE, 
		GIVEROSE, 
		KILLBOSS, 
		HARVEST, 
		CONVERT, 
		GETHOLYARTIFACT, 
		DELIVERITEM, 
		SLAY, 
		BUILDALTARS, 
		BUILDTOWER, 
		HOLYFEAST, 
		COLLECTBIBLES, 
		BURNBIBLES, 
		GIVEBIBLES, 
		PILGRIMAGE, 
		HOLYBATTLE, 
		CLAIMHOLYLAND, 
		CRUSADE;

		public String toString()
		{
			String output = name().toString();
			output = output.charAt(0) + output.substring(1).toLowerCase();

			return output;
		}
	}
}