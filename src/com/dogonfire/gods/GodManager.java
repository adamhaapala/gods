package com.dogonfire.gods;

import com.dogonfire.gods.tasks.GenerateGlobalQuestTask;
import com.dogonfire.gods.tasks.GiveItemTask;
import com.dogonfire.gods.tasks.GodSpeakTask;
import com.dogonfire.gods.tasks.HealPlayerTask;
import com.dogonfire.gods.tasks.SpawnGuideMobTask;
import com.dogonfire.gods.tasks.SpawnHostileMobsTask;
import com.mysql.jdbc.log.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;



public class GodManager 
{
	private Gods plugin;
	private FileConfiguration godsConfig = null;
	private File godsConfigFile = null;
	private Random random = new Random();
	private boolean isUpdating = false;
	//private long lastGodPowerCheckTime = -99999;
	//private HashMap<String, Float> godPowers = new HashMap<String, Float>();
	
	enum GodType {
		FROST,
		LOVE,
		EVIL,
		JUSTICE,
		HARVEST,
		SEA,
		MOON,
		SUN,
		THUNDER,
		PARTY,
		WAR,
		WEREWOLVES,
		CREATURES,
		WISDOM
	};

	enum GodGender {
		None,
		Male,
		Female,
	};
	
	enum GodMood {
		EXALTED,
		PLEASED,
		NEUTRAL,
		DISPLEASED,
		ANGRY
	};
	
	GodManager(Gods p) 
	{
		this.plugin = p;
	}

	public void load() 
	{
		this.godsConfigFile = new File(this.plugin.getDataFolder(), "gods.yml");

		this.godsConfig = YamlConfiguration.loadConfiguration(this.godsConfigFile);

		this.plugin.log("Loaded " + this.godsConfig.getKeys(false).size() + " gods.");

		for (String godName : this.godsConfig.getKeys(false)) 
		{
			String priestName = this.godsConfig.getString(godName + ".PriestName");

			if (priestName != null) 
			{
				List<String> list = new ArrayList<String>();
				list.add(priestName);

				this.godsConfig.set("PriestName", null);
				this.godsConfig.set(godName + ".Priests", list);

				save();
			}
		}
	}

	public void save() 
	{
		if ((godsConfig == null) || (godsConfigFile == null)) 
		{
			return;
		}
		try 
		{
			godsConfig.save(godsConfigFile);
		} 
		catch (Exception ex) 
		{
			plugin.log("Could not save config to " + godsConfigFile + ": " + ex.getMessage());
		}
	}
	
	public GodGender getGenderForGod(String godName)
	{
		String genderString = godsConfig.getString(godName + ".Gender");
		GodGender godGender = GodGender.None;
		
		if(genderString!=null)
		{
			try
			{
				godGender = GodGender.valueOf(genderString);
			}
			catch(Exception ex)
			{
				godGender = GodGender.None;
			}
		}
		
		return godGender;
	}
	
	public void setGenderForGod(String godName, GodGender godGender)
	{
		godsConfig.set(godName + ".Gender", godGender.name());
		
		save();
	}
	
	public String getLanguageFileForGod(String godName)
	{
		String languageFileName = godsConfig.getString(godName + ".LanguageFileName");
		
		if(languageFileName==null)
		{
			languageFileName = plugin.languageIdentifier + "_" + plugin.getGodManager().getDivineForceForGod(godName).name().toLowerCase() + "_" + plugin.getGodManager().getGenderForGod(godName).name().toLowerCase() + ".yml";
			
			godsConfig.set(godName + ".LanguageFileName", languageFileName);
			
			save();
		}
		
		return languageFileName;
	}
	
	public float getExactMoodForGod(String godName)
	{
		return (float)godsConfig.getDouble(godName + ".Mood");
	}

	public GodMood getMoodForGod(String godName)
	{
		float godMood = (float)godsConfig.getDouble(godName + ".Mood");
		
		if(godMood<-20)
		{
			return GodMood.ANGRY;						
		}
		else if(godMood<-10)
		{
			return GodMood.DISPLEASED;			
		}
		else if(godMood<10)
		{			
			return GodMood.NEUTRAL;
		}
		else if(godMood<30)
		{			
			return GodMood.PLEASED;
		}

		return GodMood.EXALTED;
	}

	public void addMoodForGod(String godName, float mood)
	{
		float godMood = (float)godsConfig.getDouble(godName + ".Mood");

		godMood += mood;
		
		if(godMood>40)
		{
			godMood = 40;
		}
		else if(godMood<-40)
		{
			godMood = -40;
		}
		
		godsConfig.set(godName + ".Mood", godMood);
		
		save();
	}

	public ChatColor getColorForGod(String godName) 
	{
		ChatColor color = ChatColor.WHITE;
		
		GodType godType = getDivineForceForGod(godName);
		
		switch(godType)
		{
			case WEREWOLVES : color = ChatColor.DARK_GRAY; break;
			case LOVE : color = ChatColor.RED; break;
			case JUSTICE : color = ChatColor.DARK_AQUA; break;
			case HARVEST : color = ChatColor.DARK_GREEN; break;
			case EVIL : color = ChatColor.BLACK; break;
			case WAR : color = ChatColor.DARK_RED; break;
			case PARTY : color = ChatColor.BOLD; break;
			case SEA : color = ChatColor.BLUE; break;
			case MOON : color = ChatColor.GRAY; break;		
			case CREATURES : color = ChatColor.GREEN; break;		
			case SUN : color = ChatColor.YELLOW; break;		
			case FROST : color = ChatColor.DARK_BLUE; break;		
			case WISDOM : color = ChatColor.GRAY; break;		
		}
		
		return color;
	}

	public void setColorForGod(String godName, ChatColor color) 
	{
		this.godsConfig.set(godName + ".Color", color.name());

		save();
	}

	public String getTitleForGod(String godName) 
	{
		if(!plugin.useGodTitles)
		{
			return "";
		}
		
		GodType godType = plugin.getGodManager().getDivineForceForGod(godName);		

		return plugin.getLanguageManager().getGodTypeName(godType, plugin.getLanguageManager().getGodGenderName(plugin.getGodManager().getGenderForGod(godName)));
	}
	
	public void setHomeForGod(String godName, Location location) 
	{
		this.godsConfig.set(godName + ".Home.X", Double.valueOf(location.getX()));
		this.godsConfig.set(godName + ".Home.Y", Double.valueOf(location.getY()));
		this.godsConfig.set(godName + ".Home.Z", Double.valueOf(location.getZ()));
		this.godsConfig.set(godName + ".Home.World", location.getWorld().getName());

		save();
	}

	public Location getHomeForGod(String godName) 
	{
		Location location = new Location(null, 0.0D, 0.0D, 0.0D);

		String worldName = this.godsConfig.getString(godName + ".Home.World");

		if (worldName == null) 
		{
			return null;
		}

		location.setWorld(this.plugin.getServer().getWorld(worldName));

		location.setX(this.godsConfig.getDouble(godName + ".Home.X"));
		location.setY(this.godsConfig.getDouble(godName + ".Home.Y"));
		location.setZ(this.godsConfig.getDouble(godName + ".Home.Z"));

		return location;
	}

	public long getSeedForGod(String godName)
	{
		long seed = godsConfig.getLong(godName + ".Seed");

		if(seed==0)
		{
			seed = random.nextLong();
			godsConfig.set(godName + ".Seed", seed);
			
			save();
		}
		
		return seed;
	}
	
	public boolean setPendingPriest(String godName, String playerName) 
	{
		String lastPriestTime = this.godsConfig.getString(godName + ".PendingPriestTime");

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastDate = null;
		Date thisDate = new Date();
		
		try 
		{
			lastDate = formatter.parse(lastPriestTime);
		} 
		catch (Exception ex) 
		{
			lastDate = new Date();
			lastDate.setTime(0L);
		}

		long diff = thisDate.getTime() - lastDate.getTime();
		long diffMinutes = diff / 60000L % 60L;

		if (diffMinutes < 3L) 
		{
			return false;
		}

		if (playerName == null) 
		{
			return false;
		}

		this.godsConfig.set(godName + ".PendingPriestName", playerName);

		save();

		this.plugin.getBelieverManager().setPendingPriest(playerName);

		return true;
	}

	public List<String> getInvitedPlayerForGod(String godName) 
	{
		return godsConfig.getStringList(godName + ".InvitedPlayers");		
	}

	public void setCursedPlayerForGod(String godName, String believerName) 
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		this.godsConfig.set(godName + ".CursedPlayer", believerName);
		this.godsConfig.set(godName + ".CursedTime", formatter.format(thisDate));

		save();
	}

	public void setBlessedPlayerForGod(String godName, String believerName) 
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		godsConfig.set(godName + ".BlessedPlayer", believerName);
		godsConfig.set(godName + ".BlessedTime", formatter.format(thisDate));

		save();
	}

	public boolean toggleWarRelationForGod(String godName, String enemyGodName) 
	{
		List<String> gods = this.godsConfig.getStringList(godName + ".Enemies");

		if (!gods.contains(enemyGodName)) 
		{
			gods.add(enemyGodName);
			this.godsConfig.set(godName + ".Enemies", gods);

			gods = this.godsConfig.getStringList(enemyGodName + ".Enemies");
			if (!gods.contains(godName)) 
			{
				gods.add(godName);
				this.godsConfig.set(enemyGodName + ".Enemies", gods);
			}

			if (this.godsConfig.getStringList(godName + ".Allies").contains(enemyGodName)) 
			{
				this.godsConfig.set(godName + ".Allies." + enemyGodName, null);
			}

			if (this.godsConfig.getStringList(enemyGodName + ".Allies").contains(godName)) 
			{
				this.godsConfig.set(enemyGodName + ".Allies." + godName, null);
			}

			save();

			return true;
		}

		gods.remove(enemyGodName);
		this.godsConfig.set(godName + ".Enemies", gods);

		gods = this.godsConfig.getStringList(enemyGodName + ".Enemies");
		if (gods.contains(godName)) 
		{
			gods.remove(godName);
			this.godsConfig.set(enemyGodName + ".Enemies", gods);
		}

		if (this.godsConfig.getStringList(godName + ".Allies").contains(enemyGodName)) 
		{
			this.godsConfig.set(godName + ".Allies." + enemyGodName, null);
		}

		if (this.godsConfig.getStringList(enemyGodName + ".Allies").contains(godName)) 
		{
			this.godsConfig.set(enemyGodName + ".Allies." + godName, null);
		}

		save();

		return false;
	}

	public boolean toggleAllianceRelationForGod(String godName, String allyGodName) 
	{
		List<String> gods = this.godsConfig.getStringList(godName + ".Allies");

		if (!gods.contains(allyGodName)) 
		{
			gods.add(allyGodName);

			this.godsConfig.set(godName + ".Allies", gods);

			gods = this.godsConfig.getStringList(allyGodName + ".Allies");

			if (!gods.contains(godName)) 
			{
				gods.add(godName);
				this.godsConfig.set(allyGodName + ".Allies", gods);
			}

			if (this.godsConfig.getStringList(godName + ".Enemies").contains(allyGodName)) 
			{
				this.godsConfig.set(godName + ".Enemies." + allyGodName, null);
			}

			if (this.godsConfig.getStringList(allyGodName + ".Enemies").contains(godName)) 
			{
				this.godsConfig.set(allyGodName + ".Enemies." + godName, null);
			}

			save();

			return true;
		}

		gods.remove(allyGodName);
		this.godsConfig.set(godName + ".Allies", gods);

		gods = this.godsConfig.getStringList(allyGodName + ".Allies");
		if (gods.contains(godName)) 
		{
			gods.remove(godName);
			this.godsConfig.set(allyGodName + ".Allies", gods);
		}

		if (this.godsConfig.getStringList(godName + ".Enemies").contains(
				allyGodName)) {
			this.godsConfig.set(godName + ".Enemies." + allyGodName, null);
		}

		if (this.godsConfig.getStringList(allyGodName + ".Enemies").contains(
				godName)) {
			this.godsConfig.set(allyGodName + ".Enemies." + godName, null);
		}

		save();

		return false;
	}

	List<String> getAllianceRelations(String godName) 
	{
		return this.godsConfig.getStringList(godName + ".Allies");
	}

	List<String> getWarRelations(String godName) 
	{
		return this.godsConfig.getStringList(godName + ".Enemies");
	}

	public boolean hasAllianceRelation(String godName, String otherGodName) 
	{
		return this.godsConfig.contains(godName + ".Allies" + otherGodName);
	}

	public boolean hasWarRelation(String godName, String otherGodName) 
	{
		return this.godsConfig.contains(godName + ".Enemies" + otherGodName);
	}

	public void setPrivateAccess(String godName, boolean open) 
	{
		this.godsConfig.set(godName + ".PrivateAccess", Boolean.valueOf(open));

		save();
	}

	public boolean isPrivateAccess(String godName) 
	{
		Boolean access = Boolean.valueOf(godsConfig.getBoolean(godName + ".PrivateAccess"));

		if (access != null) 
		{
			return access.booleanValue();
		}

		return false;
	}

	public List<String> getEnemyGodsForGod(String godName) 
	{
		return godsConfig.getStringList(godName + ".War");
	}

	private int getVerbosityForGod(String godName) 
	{
		int verbosity = godsConfig.getInt(godName + ".Verbosity");

		if (verbosity == 0) 
		{
			verbosity = 1 + random.nextInt(25);

			godsConfig.set(godName + ".Verbosity", Integer.valueOf(verbosity));

			save();
		}

		return plugin.godVerbosity + (int)(getGodPower(godName)/100) + verbosity;
	}

	private String generateHolyMobTypeForGod(String godName) 
	{
		EntityType mobType = EntityType.UNKNOWN;
		int r1 = random.nextInt(7);

		switch (r1) 
		{
			case 0: mobType = EntityType.CHICKEN; break;
			case 1: mobType = EntityType.COW; break;
			case 2: mobType = EntityType.PIG; break;
			case 3: mobType = EntityType.SHEEP; break;
			case 4: mobType = EntityType.OCELOT; break;
			case 5: mobType = EntityType.WOLF; break;
			case 6: mobType = EntityType.MUSHROOM_COW; break;
		}

		return mobType.name();
	}

	private String generateUnholyMobTypeForGod(String godName) 
	{
		EntityType mobType = EntityType.UNKNOWN;
		int r1 = random.nextInt(11);

		switch (r1) 
		{
			case 0: mobType = EntityType.CHICKEN; break;
			case 1: mobType = EntityType.COW; break;
			case 2: mobType = EntityType.ENDERMAN; break;
			case 3: mobType = EntityType.PIG; break;
			case 4: mobType = EntityType.SHEEP; break;
			case 5: mobType = EntityType.OCELOT; break;
			case 6: mobType = EntityType.WOLF; break;
			case 7: mobType = EntityType.SQUID; break;
			case 8: mobType = EntityType.SPIDER; break;
			case 9: mobType = EntityType.SKELETON; break;
			case 10: mobType = EntityType.ZOMBIE;
		}

		return mobType.name();
	}

	public EntityType getUnholyMobTypeForGod(String godName) 
	{
		String mobTypeString = godsConfig.getString(godName + ".SlayMobType");
		EntityType mobType = EntityType.UNKNOWN;

		if (mobTypeString == null) 
		{
			mobTypeString = generateUnholyMobTypeForGod(godName);

			godsConfig.set(godName + ".SlayMobType", mobTypeString);

			save();
		}

		mobType = EntityType.fromName(mobTypeString);

		if (mobType == null) 
		{
			mobTypeString = generateUnholyMobTypeForGod(godName);

			godsConfig.set(godName + ".SlayMobType", mobTypeString);

			save();

			mobType = EntityType.fromName(mobTypeString);
		}

		return mobType;
	}

	public EntityType getHolyMobTypeForGod(String godName) 
	{
		String mobTypeString = godsConfig.getString(godName + ".NotSlayMobType");
		EntityType mobType = EntityType.UNKNOWN;

		if (mobTypeString == null) 
		{
			do 
			{
				mobTypeString = generateHolyMobTypeForGod(godName);
			} while (mobTypeString.equals(getUnholyMobTypeForGod(godName).name()));

			godsConfig.set(godName + ".NotSlayMobType", mobTypeString);

			save();
		}

		mobType = EntityType.fromName(mobTypeString);

		if (mobType == null) 
		{
			do 
			{
				mobTypeString = generateHolyMobTypeForGod(godName);
			} while (mobTypeString.equals(getUnholyMobTypeForGod(godName).name()));

			godsConfig.set(godName + ".NotSlayMobType", mobTypeString);

			save();

			mobType = EntityType.fromName(mobTypeString);
		}

		return mobType;
	}

	public Material getEatFoodTypeForGod(String godName) 
	{
		String foodTypeString = this.godsConfig.getString(godName + ".EatFoodType");
		Material foodType = Material.AIR;

		if (foodTypeString == null) 
		{
			int r1 = this.random.nextInt(7);

			switch (r1) 
			{
				case 0: foodType = Material.APPLE; break;
				case 1: foodType = Material.BREAD; break;
				case 2: foodType = Material.COOKED_FISH; break;
				case 3: foodType = Material.MELON; break; 
				case 4: foodType = Material.COOKED_BEEF; break;
				case 5: foodType = Material.GRILLED_PORK; break;
				case 6: foodType = Material.CARROT_ITEM; break;
			}

			foodTypeString = foodType.name();

			godsConfig.set(godName + ".EatFoodType", foodTypeString);

			save();
		} 
		else 
		{
			foodType = Material.getMaterial(foodTypeString);
		}

		return foodType;
	}

	public Material getNotEatFoodTypeForGod(String godName) 
	{
		String foodTypeString = this.godsConfig.getString(godName + ".NotEatFoodType");
		Material foodType = Material.AIR;

		if (foodTypeString == null) 
		{
			do 
			{
				int r1 = this.random.nextInt(7);

				switch (r1) 
				{
					case 0: foodType = Material.APPLE; break;
					case 1: foodType = Material.BREAD; break;
					case 2: foodType = Material.COOKED_FISH; break;
					case 3: foodType = Material.MELON; break;
					case 4: foodType = Material.COOKED_BEEF; break;
					case 5: foodType = Material.GRILLED_PORK; break;
					case 6: foodType = Material.CARROT;
				}

				foodTypeString = foodType.name();
			} while (foodTypeString.equals(getEatFoodTypeForGod(godName).name()));

			godsConfig.set(godName + ".NotEatFoodType", foodTypeString);

			save();
		} 
		else 
		{
			foodType = Material.getMaterial(foodTypeString);
		}

		return foodType;
	}

	Material getSacrificeItemTypeForGod(String godName) 
	{
		String itemName = "";
		Integer value = Integer.valueOf(0);
		String sacrificeItemName = null;

		ConfigurationSection configSection = godsConfig.getConfigurationSection(godName + ".SacrificeValues");

		if ((configSection == null) || (configSection.getKeys(false).size() == 0)) 
		{
			return null;
		}

		for (int i = 0; i < configSection.getKeys(false).size(); i++) 
		{
			itemName = (String) configSection.getKeys(false).toArray()[random.nextInt(configSection.getKeys(false).size())];

			value = Integer.valueOf(godsConfig.getInt(godName + ".SacrificeValues." + itemName));

			if (value.intValue() > 10) 
			{
				sacrificeItemName = itemName;
			}

			plugin.logDebug("Value of wanted item " + itemName + " is " + value);
		}

		if (sacrificeItemName != null) 
		{
			return Material.getMaterial(sacrificeItemName);
		}
		
		return null;
	}
	
	public float getFalloffModifierForGod(String godName)
	{
		Random moodRandom = new Random(getSeedForGod(godName));		
/*
		switch(goDtype)
		{
			case EVIL : 
		}
		//return 0.10 + (moodRandom.nextInt(10) * plugin.getBelieverManager().getOnlineBelieversForGod(godName).size()) * sin(time/100.0f);
*/		
		double falloffValue = -0.10f * (moodRandom.nextInt(10) * plugin.getBelieverManager().getOnlineBelieversForGod(godName).size()) * Math.sin(System.currentTimeMillis()/60000.0f);
	
		plugin.logDebug(godName + " mood falloff is " + falloffValue);
		
		return (float)falloffValue;
		
		//0.20 is max
		//return -0.08f*plugin.getBelieverManager().getOnlineBelieversForGod(godName).size();
	}

	public float getPleasedModifierForGod(String godName)
	{
		return 1.0f;
	}
	
	public float getAngryModifierForGod(String godName)
	{
		return -1.0f;
	}

	public void handleEat(String playerName, String godName, String foodType) 
	{
		Material eatFoodType = getEatFoodTypeForGod(godName);
		Material notEatFoodType = getNotEatFoodTypeForGod(godName);

		if (foodType.equals(eatFoodType.name())) 
		{
			addMoodForGod(godName, getPleasedModifierForGod(godName));

			if (blessPlayer(godName, playerName, getGodPower(godName))) 
			{
				this.plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(eatFoodType));
				this.plugin.getLanguageManager().setPlayerName(playerName);

				if (this.plugin.commandmentsBroadcastFoodEaten) 
				{
					godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversEatFoodBlessing, 2 + random.nextInt(20));
				} 
				else 
				{
					godSayToBeliever(godName, playerName, LanguageManager.LANGUAGESTRING.GodToBelieversEatFoodBlessing);
				}
			}
		}

		if (foodType.equals(notEatFoodType.name())) 
		{
			addMoodForGod(godName, getAngryModifierForGod(godName));

			if (cursePlayer(godName, playerName, getGodPower(godName))) 
			{
				plugin.getLanguageManager().setType(this.plugin.getLanguageManager().getItemTypeName(notEatFoodType));
				
				plugin.getLanguageManager().setPlayerName(playerName.toUpperCase());

				if (this.plugin.commandmentsBroadcastFoodEaten) 
				{
					godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversNotEatFoodCursing, 2 + this.random.nextInt(10));
				} 
				else 
				{
					godSayToBeliever(godName, playerName, LanguageManager.LANGUAGESTRING.GodToBelieversNotEatFoodCursing);
				}
			}
		}
	}

	public void handleKilledPlayer(String playerName, String godName) 
	{
		if (plugin.leaveReligionOnDeath) 
		{
			plugin.getBelieverManager().believerLeave(godName, playerName);
		}
	}

	public void handleKilled(String playerName, String godName, String mobType) 
	{
		if ((!plugin.commandmentsEnabled) || (mobType == null)) 
		{
			return;
		}

		EntityType holyMobType = getHolyMobTypeForGod(godName);
		EntityType unholyMobType = getUnholyMobTypeForGod(godName);

		if ((unholyMobType != null) && (mobType.equals(unholyMobType.name()))) 
		{
			if (blessPlayer(godName, playerName, getGodPower(godName))) 
			{
				addMoodForGod(godName, getPleasedModifierForGod(godName));

				plugin.getLanguageManager().setPlayerName(playerName);
				plugin.getLanguageManager().setType(plugin.getLanguageManager().getMobTypeName(unholyMobType));

				if (this.plugin.commandmentsBroadcastMobSlain) 
				{
					godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSlayMobBlessing, 2 + random.nextInt(20));
				} 
				else
				{
					godSayToBeliever(godName, playerName, LanguageManager.LANGUAGESTRING.GodToBelieversSlayMobBlessing);
				}
			}

		}

		if ((holyMobType != null) && (mobType.equals(holyMobType.name()))) 
		{
			if (cursePlayer(godName, playerName, getGodPower(godName))) 
			{
				addMoodForGod(godName, getAngryModifierForGod(godName));

				plugin.getLanguageManager().setPlayerName(playerName.toUpperCase());
				plugin.getLanguageManager().setType(this.plugin.getLanguageManager().getMobTypeName(holyMobType));

				if (plugin.commandmentsBroadcastMobSlain) 
				{
					godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversNotSlayMobCursing, 2 + random.nextInt(10));
				} 
				else
				{
					godSayToBeliever(godName, playerName, LanguageManager.LANGUAGESTRING.GodToBelieversNotSlayMobCursing);
				}
			}
		}
	}

	public void handleSacrifice(String godName, String believerName, Material type) 
	{
		Player player = this.plugin.getServer().getPlayer(believerName);

		if (player == null) 
		{
			return;
		}

		if (godName == null) 
		{
			return;
		}

		int godPower = (int) this.plugin.getGodManager().getGodPower(godName);

		plugin.log(believerName + " sacrificed " + type.name() + " to " + godName);

		Material eatFoodType = getEatFoodTypeForGod(godName);

		if (type == eatFoodType) 
		{
			addMoodForGod(godName, getAngryModifierForGod(godName));
			cursePlayer(godName, believerName, getGodPower(godName));

			plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(eatFoodType));
			plugin.getLanguageManager().setPlayerName(believerName);

			if (plugin.commandmentsBroadcastFoodEaten)
			{
				godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieverHolyFoodSacrifice, 2 + random.nextInt(10));
			}
			else 
			{
				godSayToBeliever(godName, believerName, LanguageManager.LANGUAGESTRING.GodToBelieverHolyFoodSacrifice);
			}

			strikePlayerWithLightning(believerName, 1 + random.nextInt(3));

			return;
		}

		float value = getSacrificeValueForGod(godName, type);

		plugin.getLanguageManager().setPlayerName(believerName);
		plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(type));

		if (value > 10.0F) 
		{
			addMoodForGod(godName, getPleasedModifierForGod(godName));
			plugin.getBelieverManager().addPrayer(player.getName(), godName); 
			//addBelief(player, godName, true);
			blessPlayer(godName, believerName, godPower);
			godSayToBeliever(godName, believerName,
			LanguageManager.LANGUAGESTRING.GodToBelieverGoodSacrifice);
		} 
		else if (value >= -5.0F) 
		{
			godSayToBeliever(godName, believerName,
			LanguageManager.LANGUAGESTRING.GodToBelieverMehSacrifice);
		} 
		else 
		{
			addMoodForGod(godName, getAngryModifierForGod(godName));
			strikePlayerWithLightning(believerName, 1 + random.nextInt(3));
			godSayToBeliever(godName, believerName, LanguageManager.LANGUAGESTRING.GodToBelieverBadSacrifice);
		}

		value -= 1.0F;

		godsConfig.set(godName + ".SacrificeValues." + type.name(), Float.valueOf(value));

		save();
	}

	private float getSacrificeValueForGod(String godName, Material type) 
	{
		return (float) godsConfig.getDouble(godName + ".SacrificeValues." + type.name());
	}

	private Material getSacrificeUnwantedForGod(String godName) 
	{
		List<Material> unwantedItems = new ArrayList<Material>();
		ConfigurationSection configSection = godsConfig.getConfigurationSection(godName + ".SacrificeValues.");

		if (configSection != null) 
		{
			for (String itemType : configSection.getKeys(false)) 
			{
				Material item = null;
				
				try
				{
					item = Material.valueOf(itemType);
				}
				catch(Exception ex)
				{
					continue;
				}
				
				if(godsConfig.getDouble(godName + ".SacrificeValues." + itemType)>0)
				{
					continue;
				}
				
				unwantedItems.add(item);
			}
		} 
		else 
		{
			return null;
		}
		
		if(unwantedItems.size()==0)
		{
			return null;
		}
		
		return unwantedItems.get(random.nextInt(unwantedItems.size()));		
	}

	public String getEnemyPlayerForGod(String godName) 
	{
		List<String> enemyGods = getEnemyGodsForGod(godName);

		if(enemyGods.size()==0)
		{
			return null;
		}
		
		int g = 0;

		do
		{
			String enemyGod = enemyGods.get(enemyGods.size());
		
			if (enemyGod!=null) 
			{
				Set<String> believers = this.plugin.getBelieverManager().getBelieversForGod(enemyGod);

				int b = 0;

				while (b < 10) 
				{
					int r = random.nextInt(believers.size());

					String believerName = (String) believers.toArray()[r];

					if (plugin.getServer().getPlayer(believerName) != null) 
					{
						return believerName;
					}

					b++;
				}
			}

			g++;
		} while(g < 50);

		return null;
	}

	public String getCursedPlayerForGod(String godName) 
	{
		Date lastCursedDate = getLastCursingTimeForGod(godName);

		if (lastCursedDate == null) 
		{
			return null;
		}

		Date thisDate = new Date();

		long diff = thisDate.getTime() - lastCursedDate.getTime();
		long diffMinutes = diff / 60000L;

		if (diffMinutes > this.plugin.maxCursingTime) 
		{
			this.godsConfig.set(godName + ".CursedPlayer", null);
			this.godsConfig.set(godName + ".CursedTime", null);
			save();

			return null;
		}

		return this.godsConfig.getString(godName + ".CursedPlayer");
	}

	public String getBlessedPlayerForGod(String godName) 
	{
		Date lastBlessedDate = getLastBlessedTimeForGod(godName);

		if (lastBlessedDate == null) 
		{
			return null;
		}

		Date thisDate = new Date();

		long diff = thisDate.getTime() - lastBlessedDate.getTime();
		long diffSeconds = diff / 1000L;

		if (diffSeconds > plugin.maxBlessingTime) 
		{
			godsConfig.set(godName + ".BlessedPlayer", null);
			godsConfig.set(godName + ".BlessedTime", null);
			save();

			return null;
		}

		return this.godsConfig.getString(godName + ".BlessedPlayer");
	}

	public boolean godExist(String godName) 
	{
		String name = godsConfig.getString(formatGodName(godName) + ".Created");
		
		if (name == null) 
		{
			return false;
		}

		return true;
	}

	public String formatGodName(String godName) 
	{
		return godName.substring(0, 1).toUpperCase() + godName.substring(1).toLowerCase();
	}

	public void createGod(String godName, Location location) 
	{
		Date thisDate = new Date();

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);

		setHomeForGod(godName, location);

		this.godsConfig.set(godName + ".Created", formatter.format(thisDate));

		save();
	}

	public String getCreatedDate(String godName) 
	{
		return this.godsConfig.getString(godName + ".Created");
	}

	public Date getLastCursingTimeForGod(String godName) 
	{
		String lastCursedString = this.godsConfig.getString(godName + ".CursedTime");

		if (lastCursedString == null) 
		{
			return null;
		}

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastCursedDate = null;
		
		try 
		{
			lastCursedDate = formatter.parse(lastCursedString);
		} 
		catch (Exception ex) 
		{
			lastCursedDate = new Date();
			lastCursedDate.setTime(0L);
		}

		return lastCursedDate;
	}

	public Date getLastBlessedTimeForGod(String godName) 
	{
		String lastBlessedString = this.godsConfig.getString(godName + ".BlessedTime");

		if (lastBlessedString == null) 
		{
			return null;
		}

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastBlessedDate = null;
		try 
		{
			lastBlessedDate = formatter.parse(lastBlessedString);
		} 
		catch (Exception ex) 
		{
			lastBlessedDate = new Date();
			lastBlessedDate.setTime(0L);
		}

		return lastBlessedDate;
	}

	public float getGodPower(String godName) 
	{
		/*
		if(!godPowers.containsKey(godName))
		{
			godPowers.put(godName, 0.0F);
		}*/

		//if(System.currentTimeMillis() - lastGodPowerCheckTime - random.nextInt(2000) > 60000)
		//{
			float godPower = 0.0F;
			int minGodPower = 0;
			String name = godsConfig.getString(godName);

			if (name == null) 
			{
				return 0.0F;
			}
									
			Set<String> believers = plugin.getBelieverManager().getBelieversForGod(godName);

			if (plugin.useWhitelist) 
			{
				minGodPower = (int) plugin.getWhitelistManager().getMinGodPower(godName);
			}

			for (String believer : believers) 
			{
				float believerPower = plugin.getBelieverManager().getBelieverPower(believer);

				godPower += believerPower;
			}

			if (godPower < minGodPower) 
			{
				godPower = minGodPower;
			}
			
			//godPowers.put(godName, godPower);
			
			//lastGodPowerCheckTime = System.currentTimeMillis();
		//}

		return godPower;
	}

	public int getGodLevel(String godName) 
	{
		float power = getGodPower(godName);

		if (power < 3.0F)
		{
			return 0;
		}
		else if (power < 10.0F) 
		{
			return 1;
		}
		
		return 2;
	}

	private String getNextBelieverForPriest(String godName) 
	{
		Set<String> allBelievers = this.plugin.getBelieverManager().getBelieversForGod(godName);
		List<PriestCandidate> candidates = new ArrayList<PriestCandidate>();

		if (allBelievers.size() == 0) 
		{
			this.plugin.logDebug("Did not find any priest candidates");
			return null;
		}

		String pendingPriestName = getPendingPriestName(godName);

		for (String candidate : allBelievers) 
		{
			Player player = this.plugin.getServer().getPlayer(candidate);

			if (player != null) 
			{
				if (!isPriest(candidate)) 
				{
					if ((pendingPriestName == null) || (!pendingPriestName.equals(candidate))) 
					{
						if (!this.plugin.getBelieverManager().hasRecentPriestOffer(candidate)) 
						{
							if (this.plugin.getPermissionsManager().hasPermission(player, "gods.priest")) 
							{
								candidates.add(new PriestCandidate(candidate));
							}
						}
					}
				}
			}
		}
		
		if (candidates.size() == 0) 
		{
			return null;
		}

		Collections.sort(candidates, new NewPriestComparator());

		PriestCandidate finalCandidate = null;

		if (candidates.size() > 2) 
		{
			finalCandidate = (PriestCandidate) candidates.toArray()[this.random.nextInt(3)];
		} 
		else 
		{
			finalCandidate = (PriestCandidate) candidates.toArray()[0];
		}

		return finalCandidate.name;
	}

	public List<String> getPriestsForGod(String godName) 
	{
		List<String> names = this.godsConfig.getStringList(godName + ".Priests");
		List<String> list = new ArrayList<String>();

		if (names == null) 
		{
			return null;
		}

		for (String name : names) 
		{
			if ((name != null) && (!name.equals("none"))) 
			{
				Date thisDate = new Date();
				Date lastPrayerDate = this.plugin.getBelieverManager().getLastPrayerTime(name);

				long diff = thisDate.getTime() - lastPrayerDate.getTime();

				long diffHours = diff / 3600000L;

				if (diffHours > this.plugin.maxPriestPrayerTime) 
				{
					this.plugin.getLanguageManager().setPlayerName(name);

					godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversRemovedPriest, 2 + this.random.nextInt(40));

					removePriest(godName, name);
				} 
				else 
				{
					list.add(name);
				}
			}
		}
		return list;
	}

	public boolean isPriest(String believerName) 
	{
		if (believerName == null) 
		{
			return false;
		}

		Set<String> gods = getGods();

		for (String godName : gods) 
		{
			List<String> list = getPriestsForGod(godName);

			if (list.contains(believerName)) 
			{
				return true;
			}
		}

		return false;
	}

	public boolean isPriestForGod(String believerName, String godName) 
	{
		if (believerName == null) 
		{
			return false;
		}

		List<String> priestNames = getPriestsForGod(godName);

		if ((priestNames != null) && (priestNames.contains(believerName))) 
		{
			return true;
		}

		return false;
	}

	public String getPendingPriestName(String godName) 
	{
		String name = this.godsConfig.getString(godName + ".PendingPriestName");

		if ((name == null) || (name.equals("none"))) 
		{
			return null;
		}
		return name;
	}

	public String getQuestType(String godName) 
	{
		String name = this.godsConfig.getString(godName + ".QuestType");

		if ((name == null) || (name.equals("none"))) 
		{
			return null;
		}
		return name;
	}

	public String getGodDescription(String godName) 
	{
		String description = this.godsConfig.getString(godName + ".Description");

		if (description == null) 
		{
			description = new String("No description :/");
		}

		return description;
	}

	public boolean hasGodAccess(String believerName, String godName) 
	{
		if (!isPrivateAccess(godName)) 
		{
			return true;
		}

		String currentGodName = this.plugin.getBelieverManager().getGodForBeliever(believerName);

		if ((currentGodName == null) || (!currentGodName.equals(godName))) 
		{
			return false;
		}

		return true;
	}

	public void setGodPvP(String godName, boolean pvp) 
	{
		this.godsConfig.set(godName + ".PvP", Boolean.valueOf(pvp));

		save();
	}

	public boolean getGodPvP(String godName) 
	{
		return plugin.holyLandDefaultPvP || godsConfig.getBoolean(godName + ".PvP");
	}

	public boolean getGodMobDamage(String godName) 
	{
		return (plugin.holyLandDefaultMobDamage) || (godsConfig.getBoolean(godName + ".MobDamage"));
	}

	public void setGodDescription(String godName, String description) 
	{
		godsConfig.set(godName + ".Description", description);

		save();
	}

	public void setDivineForceForGod(String godName, GodType divineForce)
	{
		godsConfig.set(godName + ".DivineForce", divineForce.name().toUpperCase());	

		save();
	}

	public GodType getDivineForceForGod(String godName)
	{
		GodType type;
		
		try
		{
			type = GodType.valueOf(godsConfig.getString(godName + ".DivineForce"));
		}
		catch(Exception ex)
		{
			// Do not force werewolves
			do
			{
				type = GodType.values()[random.nextInt(GodType.values().length)];
			} while(type==GodType.WEREWOLVES);
			
			setDivineForceForGod(godName, type);			
		}
		
		return type;
	}
	
	public Set<String> getGods() 
	{
		Set<String> gods = godsConfig.getKeys(false);

		return gods;
	}

	public Set<String> getTopGods() 
	{
		Set<String> topGods = godsConfig.getKeys(false);

		return topGods;
	}

	private Material getRewardBlessing(String godName) 
	{
		if (getGodPower(godName) > this.plugin.godPowerForLevel3Items) 
		{
			return Material.DIAMOND;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel2Items) 
		{
			return Material.GOLD_INGOT;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel1Items) 
		{
			return Material.CAKE;
		}

		return Material.COAL;
	}

	private Material getPickAxeBlessing(String godName) 
	{
		if (getGodPower(godName) > this.plugin.godPowerForLevel3Items) 
		{
			return Material.DIAMOND_PICKAXE;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel2Items) 
		{
			return Material.IRON_PICKAXE;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel1Items) 
		{
			return Material.STONE_PICKAXE;
		}

		return Material.WOOD_PICKAXE;
	}

	private Material getSpadeBlessing(String godName) 
	{
		if (getGodPower(godName) > this.plugin.godPowerForLevel3Items) 
		{
			return Material.DIAMOND_SPADE;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel2Items) 
		{
			return Material.IRON_SPADE;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel1Items) 
		{
			return Material.STONE_SPADE;
		}

		return Material.WOOD_SPADE;
	}

	private Material getHoeBlessing(String godName) 
	{
		if (getGodPower(godName) > this.plugin.godPowerForLevel3Items) 
		{
			return Material.DIAMOND_HOE;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel2Items) 
		{
			return Material.IRON_HOE;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel1Items) 
		{
			return Material.STONE_HOE;
		}

		return Material.WOOD_HOE;
	}

	private Material getAxeBlessing(String godName) 
	{
		if (getGodPower(godName) > this.plugin.godPowerForLevel3Items) 
		{
			return Material.DIAMOND_AXE;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel2Items) 
		{
			return Material.IRON_AXE;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel1Items) 
		{
			return Material.STONE_AXE;
		}

		return Material.WOOD_AXE;
	}

	private Material getSwordBlessing(String godName) 
	{
		if (getGodPower(godName) > this.plugin.godPowerForLevel3Items) 
		{
			return Material.DIAMOND_SWORD;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel2Items) 
		{
			return Material.IRON_SWORD;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel1Items) 
		{
			return Material.STONE_SWORD;
		}

		return Material.WOOD_SWORD;
	}

	private Material getFoodBlessing(String godName) 
	{
		return getEatFoodTypeForGod(godName);
	}

	private int getHealthBlessing(String godName) 
	{
		if (getGodPower(godName) > this.plugin.godPowerForLevel3Items) 
		{
			return 3;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel2Items) 
		{
			return 2;
		}
		if (getGodPower(godName) > this.plugin.godPowerForLevel1Items) 
		{
			return 1;
		}

		return 0;
	}

	private boolean hasPickAxe(Player player) 
	{
		PlayerInventory inventory = player.getInventory();

		if (inventory.contains(Material.WOOD_PICKAXE)) 
		{
			return true;
		}

		if (inventory.contains(Material.STONE_PICKAXE)) 
		{
			return true;
		}

		if (inventory.contains(Material.IRON_PICKAXE)) 
		{
			return true;
		}

		if (inventory.contains(Material.DIAMOND_PICKAXE)) 
		{
			return true;
		}

		return false;
	}

	private boolean hasSpade(Player player) 
	{
		PlayerInventory inventory = player.getInventory();

		if (inventory.contains(Material.WOOD_SPADE)) 
		{
			return true;
		}

		if (inventory.contains(Material.STONE_SPADE)) 
		{
			return true;
		}

		if (inventory.contains(Material.IRON_SPADE)) 
		{
			return true;
		}

		if (inventory.contains(Material.DIAMOND_SPADE)) 
		{
			return true;
		}

		return false;
	}

	private boolean hasHoe(Player player) 
	{
		PlayerInventory inventory = player.getInventory();

		if (inventory.contains(Material.WOOD_HOE)) 
		{
			return true;
		}

		if (inventory.contains(Material.STONE_HOE)) 
		{
			return true;
		}

		if (inventory.contains(Material.IRON_HOE)) 
		{
			return true;
		}

		if (inventory.contains(Material.DIAMOND_HOE)) 
		{
			return true;
		}

		return false;
	}

	private boolean hasAxe(Player player) 
	{
		PlayerInventory inventory = player.getInventory();

		if (inventory.contains(Material.WOOD_AXE)) 
		{
			return true;
		}

		if (inventory.contains(Material.STONE_AXE)) 
		{
			return true;
		}

		if (inventory.contains(Material.IRON_AXE)) 
		{
			return true;
		}

		if (inventory.contains(Material.DIAMOND_AXE)) 
		{
			return true;
		}

		return false;
	}

	private boolean hasSword(Player player) 
	{
		PlayerInventory inventory = player.getInventory();

		for (int i = 0; i < inventory.getSize(); i++) 
		{
			ItemStack stack = inventory.getItem(i);

			if ((stack != null)
					&& ((stack.getType().equals(Material.WOOD_SWORD))
							|| (stack.getType().equals(Material.STONE_SWORD))
							|| (stack.getType().equals(Material.IRON_SWORD)) || (stack
								.getType().equals(Material.DIAMOND_SWORD)))
					&& (stack.getAmount() != 0)) 
			{
				return true;
			}
		}

		return false;
	}

	private boolean hasFood(Player player, String godName) 
	{
		PlayerInventory inventory = player.getInventory();

		if (inventory.contains(this.plugin.getGodManager().getEatFoodTypeForGod(godName))) 
		{
			return true;
		}

		return false;
	}

	public int getHealthNeed(String godName, Player player) 
	{
		return player.getMaxHealth() - player.getHealth();
	}

	private ItemStack getItemNeed(String godName, Player player) 
	{
		if (!hasFood(player, godName)) 
		{
			return new ItemStack(getFoodBlessing(godName));
		}
		if (!hasPickAxe(player)) 
		{
			return new ItemStack(getPickAxeBlessing(godName));
		}
		if (!hasSword(player)) 
		{
			return new ItemStack(getSwordBlessing(godName));
		}
		if (!hasSpade(player)) 
		{
			return new ItemStack(getSpadeBlessing(godName));
		}
		if (!hasAxe(player)) 
		{
			return new ItemStack(getAxeBlessing(godName));
		}
		if (!hasHoe(player)) 
		{
			return new ItemStack(getHoeBlessing(godName));
		}

		return null;
	}

	public boolean cursePlayer(String godName, String playerName, float godPower) 
	{
		Player player = this.plugin.getServer().getPlayer(playerName);

		if (player == null) 
		{
			return false;
		}

		if (plugin.getBelieverManager().hasRecentCursing(player.getName())) 
		{
			return false;
		}

		int curseType = 0;
		int t=0;
		
		do
		{
			curseType = random.nextInt(7);
			t++;
		} while(t<50 && ((curseType==5 && !plugin.lightningCurseEnabled) || (curseType==6 && !plugin.mobCurseEnabled)));
		
		float cursePower = 1 + godPower / 100; 
		
		switch (curseType) 
		{
			case 0: player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int)(10 * 20 * cursePower), 1)); break;
			case 1: player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int)(10 *  20 * cursePower), 1)); break;
			case 2: player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int)(10 * 20 * cursePower), 1)); break;
			case 3: player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)(10 * 20 * cursePower), 1)); break;
			case 4: player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, (int)(10 * 20 * cursePower), 1)); break;
			case 5: strikePlayerWithLightning(playerName, 1); break;
			case 6: strikePlayerWithMobs(godName, playerName, godPower); break;
		}
		
		plugin.getBelieverManager().setCursingTime(player.getName());

		return true;
	}

	public boolean blessPlayer(String godName, String playerName, float godPower) 
	{
		Player player = plugin.getServer().getPlayer(playerName);

		if (player == null) 
		{
			return false;
		}

		if (plugin.getBelieverManager().hasRecentBlessing(player.getName())) 
		{
			return false;
		}

		int blessingType = 0;
		int t=0;

		float blessingPower = 1 + godPower / 100; 
		
		do
		{
			blessingType = random.nextInt(5);		
			t++;
		} while(t<50 && ((blessingType==0 && !plugin.fastDiggingBlessingEnabled) || (blessingType==1 && !plugin.healBlessingEnabled) || (blessingType==2 && !plugin.regenerationBlessingEnabled) || (blessingType==3 && !plugin.speedBlessingEnabled) || (blessingType==4 && !plugin.increaseDamageBlessingEnabled)));
		
		switch (blessingType) 
		{
			case 0: player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int)(15 * 20 * blessingPower), 1)); break;
			case 1: player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, (int)(15 * 20 * blessingPower), 1)); break;
			case 2: player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int)(15 * 20 * blessingPower), 1)); break;
			case 3: player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int)(15 * 20 * blessingPower), 1)); break;
			case 4: player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int)(15 *20 * blessingPower), 1)); break;
		}

		plugin.getBelieverManager().setBlessingTime(player.getName());

		return true;
	}

	public void spawnGuidingMobs(String godName, String playerName, Location targetLocation) 
	{
		EntityType mobType = getHolyMobTypeForGod(godName);
		
		Player player = this.plugin.getServer().getPlayer(playerName);

		if (player == null) 
		{
			return;
		}

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SpawnGuideMobTask(plugin, player, targetLocation, mobType), 2L);
	}

	public void spawnHostileMobs(String godName, Player player, EntityType mobType, int numberOfMobs) 
	{
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SpawnHostileMobsTask(this.plugin, godName, player, mobType, numberOfMobs), 2L);
	}

	public void giveItem(String godName, Player player, Material material, boolean speak) 
	{
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new GiveItemTask(this.plugin, godName, player, material, speak), 2L);
	}

	public ItemStack blessPlayerWithItem(String godName, Player player) 
	{
		ItemStack item = getItemNeed(godName, player);

		if (item != null) 
		{			
			giveItem(godName, player, item.getType(), true);
		}

		return item;
	}

	public boolean setPlayerOnFire(String playerName, int seconds) 
	{
		for (Player matchPlayer : this.plugin.getServer().matchPlayer(playerName)) 
		{
			matchPlayer.setFireTicks(seconds);
		}

		return true;
	}

	public boolean strikePlayerWithMobs(String godName, String playerName, float godPower) 
	{
		Player player = this.plugin.getServer().getPlayer(playerName);

		if (player == null) 
		{
			this.plugin.logDebug("player is null");
		}

		EntityType mobType = EntityType.UNKNOWN;

		switch (this.random.nextInt(5)) 
		{
			case 0: mobType = EntityType.SKELETON; break;
			case 1: mobType = EntityType.ZOMBIE; break;
			case 2: mobType = EntityType.PIG_ZOMBIE; break;
			case 3: mobType = EntityType.SPIDER; break;
			case 4: mobType = EntityType.WOLF; break;
			case 5: mobType = EntityType.GIANT; break;
		}

		int numberOfMobs = 1 + (int)(godPower / 10);

		spawnHostileMobs(godName, player, mobType, numberOfMobs);

		return true;
	}

	public boolean strikePlayerWithLightning(String playerName, int damage) 
	{
		for (Player matchPlayer : this.plugin.getServer().matchPlayer(playerName)) 
		{
			if (damage <= 0) 
			{
				matchPlayer.getWorld().strikeLightningEffect(matchPlayer.getLocation());
			} 
			else 
			{
				LightningStrike strike = matchPlayer.getWorld().strikeLightning(matchPlayer.getLocation());
				matchPlayer.damage(damage - 1, strike);
			}
		}

		return true;
	}

	public boolean strikeCreatureWithLightning(Creature creature, int damage) 
	{
		if (damage <= 0) 
		{
			creature.getWorld().strikeLightningEffect(creature.getLocation());
		} 
		else 
		{
			LightningStrike strike = creature.getWorld().strikeLightning(creature.getLocation());
			creature.damage(damage - 1, strike);
		}

		return true;
	}

	public boolean rewardBeliever(String godName, Player believer) 
	{
		ItemStack items = new ItemStack(getRewardBlessing(godName));

		giveItem(godName, believer, items.getType(), false);

		return true;
	}

	public void healPlayer(String godName, Player player, int healing) 
	{
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new HealPlayerTask(plugin, godName, player, LanguageManager.LANGUAGESTRING.GodToBelieverHealthBlessing), 2L);
	}

	public void believerAccept(String believerName) 
	{
		String godName = plugin.getBelieverManager().getGodForBeliever(believerName);

		Player player = plugin.getServer().getPlayer(believerName);

		if (player == null) 
		{
			plugin.logDebug("believerAccept(): player is null for " + believerName);
			return;
		}
		
		plugin.getLanguageManager().setPlayerName(player.getName());

		// Accepting invite?
		String pendingGodInvitation = plugin.getBelieverManager().getInvitation(believerName);				
		
		plugin.logDebug("pendingGodInvitation is " + pendingGodInvitation);

		if (pendingGodInvitation != null) 
		{
			if (addBelief(player, pendingGodInvitation, true)) 
			{
				plugin.getBelieverManager().clearInvitation(believerName);
				
				plugin.log(player.getName() + " accepted the invitation to join " + godName);
			
				GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToPlayerAcceptedInvitation, 2 + random.nextInt(40));
			}
			else
			{
				plugin.log(player.getName() + " could NOT accept the invitation to join " + godName);				
			}
			
			return;
		}
		
		// Accepting priest?
		String pendingPriest = getPendingPriestName(godName);
		if (pendingPriest != null) 
		{
			if (getPendingPriestName(godName).equals(believerName)) 
			{
				assignPriest(godName, believerName);
				save();

				plugin.log(player.getName() + " accepted the offer from " + godName + " to be priest");
				plugin.sendInfo(player, ChatColor.AQUA + " Use " + ChatColor.WHITE + "/g followers" + ChatColor.AQUA + " to keep track of your followers!");
			
				// NULL HERE?
				GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToPriestPriestAccepted, 2 + random.nextInt(40));
				GodSayToBelieversExcept(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPriestAccepted, player.getName());

				return;
			}
		}

		// NULLLEXAPCT HERE?
		plugin.logDebug(player.getName() + " did not have anything to accepted from " + godName);
		GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToBelieverNoQuestion, 2 + random.nextInt(20));

	}

	public void believerReject(String believerName) 
	{
		String godName = this.plugin.getBelieverManager().getGodForBeliever(believerName);
		Player player = this.plugin.getServer().getPlayer(believerName);

		plugin.getLanguageManager().setPlayerName(player.getName());

		// Rejecting invite?
		String pendingGodInvitation = plugin.getBelieverManager().getInvitation(believerName);				
		
		if (pendingGodInvitation != null) 
		{
			plugin.getBelieverManager().clearInvitation(believerName);
				
			plugin.log(player.getName() + " rejected the invitation to join " + godName);
			
			plugin.sendInfo(player, ChatColor.AQUA + "You " + ChatColor.RED + " rejected " + ChatColor.AQUA + " the offer to join " + ChatColor.GOLD + godName + ChatColor.AQUA + "!");
			
			return;
		}
		
		// Rejecting preist offer?
		String pendingPriest = getPendingPriestName(godName);
		if (pendingPriest == null) 
		{
			if (player != null) 
			{
				GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToBelieverNoQuestion, 2 + random.nextInt(20));
			}

			return;
		}

		if (getPendingPriestName(godName).equals(believerName)) 
		{
			godsConfig.set(godName + ".PendingPriestName", null);

			plugin.getBelieverManager().clearPendingPriest(believerName);

			if (player != null) 
			{
				plugin.log(player.getName() + " rejected the offer from " + godName + " to be priest");

				GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToBelieverPriestRejected, 2 + random.nextInt(20));
			}

			save();
		}
	}

	public void handleReadBible(String godName, Player player) 
	{
		//if (plugin.useBibleForPrayer) 
		//{
		//	handlePray(player.getLocation(), player, godName);
		//}
	}

	public void handleBibleMelee(String godName, Player player) 
	{
	}

	public boolean handlePray(Player player, String godName) 
	{
		if (addBelief(player, godName, plugin.getBelieverManager().getChangingGod(player.getName()))) 
		{
			addMoodForGod(godName, getPleasedModifierForGod(godName));

			plugin.getLanguageManager().setPlayerName(player.getName());

			GodSay(godName, player,LanguageManager.LANGUAGESTRING.GodToBelieverPraying, 2 + this.random.nextInt(10));
			
			player.getLocation().getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 25);

			return true;
		}

		return false;
	}

	public boolean handlePray(Location location, Player player, String godName) 
	{
		if (addBelief(player, godName, plugin.getBelieverManager().getChangingGod(player.getName()))) 
		{			
			Block altarBlock = plugin.getAltarManager().getAltarBlockFromSign(player.getWorld().getBlockAt(location));

			// Ensure gender for dead Gods which suddenly gets an resurrection
			if(plugin.getGodManager().getGenderForGod(godName) == GodGender.None)
			{
				GodGender godGender = plugin.getAltarManager().getGodGenderFromAltarBlock(altarBlock);

				plugin.getGodManager().setGenderForGod(godName, godGender);

				plugin.logDebug("God did not have a gender, setting gender to " + godGender);
			}		
			
			addMoodForGod(godName, getPleasedModifierForGod(godName));

			if ((plugin.holyLandEnabled) && (plugin.getPermissionsManager().hasPermission(player, "gods.holyland"))) 
			{
				plugin.getLandManager().setPrayingHotspot(player.getName(), godName, altarBlock.getLocation());
			}
			
			plugin.getQuestManager().handlePrayer(godName, player.getName());

			plugin.getLanguageManager().setPlayerName(player.getName());

			GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToBelieverPraying, 2 + random.nextInt(10));
			location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 25);

			return true;
		}

		return false;
	}

	private boolean addBelief(Player player, String godName, boolean allowChangeGod) 
	{
		String oldGodName = plugin.getBelieverManager().getGodForBeliever(player.getName());

		if (godName == null) 
		{
			plugin.sendInfo(player, ChatColor.RED + "That is not a valid name for a God");
			return false;
		}

		if ((oldGodName != null) && (!oldGodName.equals(godName))) 
		{
			if (!allowChangeGod) 
			{
				plugin.getBelieverManager().setChangingGod(player.getName());

				plugin.sendInfo(
								player,
								ChatColor.RED
										+ "By praying to this God you will LEAVE the religion of "
										+ ChatColor.GOLD
										+ oldGodName
										+ ChatColor.RED
										+ "! Click again to confirm this action.");
				return false;
			}

			plugin.getBelieverManager().clearChangingGod(player.getName());
		}

		if (!plugin.getBelieverManager().addPrayer(player.getName(), godName)) 
		{
			this.plugin.sendInfo(player,ChatColor.RED + "You cannot pray at an altar so soon! Gather your strength and try again later.");
			return false;
		}

		if (!godExist(godName)) 
		{
			if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "gods.god.create"))) 
			{
				plugin.sendInfo(player, ChatColor.RED + "You cannot create a new God");
				return false;
			}			
			
			plugin.log(player.getName() + " created new god " + godName);

			if (plugin.broadcastNewGods) 
			{
				plugin.getServer().broadcastMessage(ChatColor.WHITE + player.getName() + ChatColor.AQUA + " started to believe in the " + plugin.getLanguageManager().getGodGenderName(getGenderForGod(godName)) + " " + ChatColor.GOLD + godName);
			}

						
			createGod(godName, player.getLocation());
		}

		if ((oldGodName != null) && (!oldGodName.equals(godName))) 
		{
			if (isPriestForGod(player.getName(), oldGodName)) 
			{
				removePriest(oldGodName, player.getName());
			}

			plugin.getLanguageManager().setPlayerName(player.getName());

			godSayToBelievers(oldGodName, LanguageManager.LANGUAGESTRING.GodToBelieversPlayerLeftReligion, 2 + random.nextInt(20));
			
			plugin.sendInfo(player, "You left the religion of " + ChatColor.GOLD + oldGodName);

			GodSayToBelieversExcept(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPlayerJoinedReligion, player.getName());
		} 
		else 
		{
			Material foodType = getEatFoodTypeForGod(godName);

			this.plugin.getLanguageManager().setType(this.plugin.getLanguageManager().getItemTypeName(foodType));

			giveItem(godName, player, foodType, false);
		}

		if ((oldGodName == null) || (!oldGodName.equals(godName))) 
		{
			this.plugin.getQuestManager().handleJoinReligion(player.getName(), godName);
		}

		return true;
	}

	public boolean addAltar(Player player, String godName, Location location) 
	{
		addBelief(player, godName, true);

		plugin.getLanguageManager().setPlayerName(player.getName());
		
		GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToBelieverAltarBuilt, 2 + this.random.nextInt(30));

		return true;
	}

	public static String parseBelief(String message) 
	{
		return null;
	}

	public void assignPriest(String godName, String playerName) 
	{
		godsConfig.set(godName + ".PendingPriestName", null);
		plugin.getBelieverManager().clearPendingPriest(playerName);

		plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), plugin.getLanguageManager().getPriestAssignCommand(playerName));

		List<String> priestNames = this.godsConfig.getStringList(godName + ".Priests");

		priestNames.add(playerName);

		godsConfig.set(godName + ".Priests", priestNames);

		godsConfig.set(godName + ".PendingPriestName", null);
		godsConfig.set(godName + ".PendingPriestTime", null);
		
		// Make sure he doesnt time out
		plugin.getBelieverManager().setLastPrayerDate(playerName);
		
		save();
		
		
	}

	public void removePriest(String godName, String playerName) 
	{
		this.plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), this.plugin.getLanguageManager().getPriestRemoveCommand(playerName));

		List<String> priestNames = this.godsConfig.getStringList(godName + ".Priests");

		priestNames.remove(playerName);

		this.godsConfig.set(godName + ".Priests", priestNames);

		save();

		this.plugin.log(godName + " removed " + playerName + " as priest");
	}

	public boolean removeBeliever(String believerName) 
	{
		String godName = this.plugin.getBelieverManager().getGodForBeliever(believerName);

		if (godName == null) 
		{
			return false;
		}

		if (isPriestForGod(believerName, godName)) 
		{
			removePriest(godName, believerName);
		}

		this.plugin.getBelieverManager().removeBeliever(godName, believerName);

		this.plugin.getLanguageManager().setPlayerName(believerName);
		godSayToBelievers(godName,
				LanguageManager.LANGUAGESTRING.GodToBelieversLostBeliever,
				2 + this.random.nextInt(100));

		return true;
	}

	public boolean believerLeaveGod(String believerName) 
	{
		String godName = this.plugin.getBelieverManager().getGodForBeliever(believerName);

		if (godName == null) 
		{
			return false;
		}

		if (isPriestForGod(believerName, godName)) 
		{
			removePriest(godName, believerName);
		}

		plugin.getBelieverManager().believerLeave(godName, believerName);

		plugin.getLanguageManager().setPlayerName(believerName);

		godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPlayerLeftReligion, 2 + this.random.nextInt(20));

		return true;
	}

	public void removeGod(String godName) 
	{
		// Remove any alliance and war relation to this god
		for (String otherGodName : getGods()) 
		{
			if(hasAllianceRelation(otherGodName, godName))
			{
				toggleAllianceRelationForGod(otherGodName, godName);
			}
			
			if(hasWarRelation(otherGodName, godName))
			{
				toggleWarRelationForGod(otherGodName, godName);
			}
		}		
		
		godsConfig.set(godName, null);
		plugin.getBibleManager().clearBible(godName);

		save();
	}

	public void addBeliefAndRewardBelievers(String godName) 
	{
		for (String playerName : this.plugin.getBelieverManager().getBelieversForGod(godName)) 
		{
			Player player = this.plugin.getServer().getPlayer(playerName);

			if (player != null) 
			{
				plugin.getBelieverManager().incPrayer(player.getName(), godName);

				List<ItemStack> rewards = plugin.getQuestManager().getRewardsForQuestCompletion(godName);

				for (ItemStack items : rewards) 
				{
					giveItem(godName, player, items.getType(), false);
				}
			}
		}
	}

	public void GodSayToPriest(String godName, LanguageManager.LANGUAGESTRING message) 
	{
		List<String> priestNames = getPriestsForGod(godName);

		if (priestNames == null) 
		{
			return;
		}

		for (String priestName : priestNames) 
		{
			Player player = this.plugin.getServer().getPlayer(priestName);

			if (player != null) 
			{
				//this.plugin.logDebug(godName + ": " + this.plugin.getLanguageManager().getLanguageString(message));

				GodSay(godName, player, message, 2 + this.random.nextInt(30));
			}
		}
	}

	public void GodsSayToBelievers(LanguageManager.LANGUAGESTRING message, int delay) 
	{
		for (String godName : getGods()) 
		{
			godSayToBelievers(godName, message, delay);
		}
	}

	public void godSayToBelievers(String godName, LanguageManager.LANGUAGESTRING message, int delay) 
	{
		//plugin.logDebug(godName + ": " + plugin.getLanguageManager().getLanguageString(message));

		for (String playerName : plugin.getBelieverManager().getBelieversForGod(godName)) 
		{
			Player player = plugin.getServer().getPlayer(playerName);

			if (player != null) 
			{
				GodSay(godName, player, message, delay);
			}
		}
	}

	public void OtherGodSayToBelievers(String godName, LanguageManager.LANGUAGESTRING message, int delay) 
	{
		//this.plugin.logDebug(godName + ": " + this.plugin.getLanguageManager().getLanguageString(message));

		for (Player player : this.plugin.getServer().getOnlinePlayers()) 
		{
			String playerGod = this.plugin.getBelieverManager().getGodForBeliever(player.getName());

			if ((playerGod != null) && (!playerGod.equals(godName))) 
			{
				GodSay(godName, player, message, delay);
			}
		}
	}

	public void GodSayToBelieversExcept(String godName, LanguageManager.LANGUAGESTRING message, String exceptPlayer) 
	{
		//this.plugin.logDebug(godName + ": " + this.plugin.getLanguageManager().getLanguageString(message));

		for (String playerName : plugin.getBelieverManager().getBelieversForGod(godName)) 
		{
			Player player = plugin.getServer().getPlayer(playerName);

			if ((player != null) && (!player.getName().equals(exceptPlayer))) 
			{
				GodSay(godName, player, message, 2 + random.nextInt(20));
			}
		}
	}

	public void godSayToBeliever(String godName, String playerName, LanguageManager.LANGUAGESTRING message) 
	{
		godSayToBeliever(godName, playerName, message, 2 + random.nextInt(10)); 
	}

	public void godSayToBeliever(String godName, String playerName, LanguageManager.LANGUAGESTRING message, int delay) 
	{
		Player player = plugin.getServer().getPlayer(playerName);

		if (player == null) 
		{
			plugin.logDebug("GodSayToBeliever player for " + playerName + " is null");
			return;
		}

		GodSay(godName, player, message, delay);
	}
		
	public void GodSay(String godName, Player player, LanguageManager.LANGUAGESTRING message, int delay) 
	{
		String playerNameString = plugin.getLanguageManager().getPlayerName();
		String typeNameString = plugin.getLanguageManager().getType();
		int amount = plugin.getLanguageManager().getAmount();

		if(player==null)
		{
			plugin.logDebug("GodSay(): Player is null!");
			return;
		}
		
		plugin.logDebug(godName + " to " + player.getName() + ": " + plugin.getLanguageManager().getLanguageString(godName, message));

		if(!plugin.getPermissionsManager().hasPermission(player, "gods.listen"))
		{
			return;
		}
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new GodSpeakTask(plugin, godName, player.getName(), playerNameString, typeNameString, amount, message), delay);
	}

	public boolean manageGod(String godName) 
	{
		if ((plugin.getBelieverManager().getBelieversForGod(godName).size() == 0) && (plugin.getGodManager().getGodPower(godName) < 1.0F)) 
		{
			removeGod(godName);

			return false;
		}

		return true;
	}

	public boolean managePriests(String godName) 
	{
		int numberOfBelievers = plugin.getBelieverManager().getBelieversForGod(godName).size();
		List<String> priestNames = getPriestsForGod(godName);

		if (priestNames == null) 
		{
			priestNames = new ArrayList<String>();
		}
		
		Player player;
		
		// 0	1
		// 6	2
		// 12	3
		// 24	4 
		// 36	5		
		if (numberOfBelievers < plugin.minBelieversForPriest + 6 * priestNames.size()) 
		{
			// NOT talking to priest
			//if (random.nextInt(getVerbosityForGod(godName)) == 0) 
			//{
				//GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversRandomSpeech, 2 + this.random.nextInt(100));
				return false;
			//}
		} 
		else if (priestNames.size() < plugin.maxPriestsPrGod) 
		{
			if (random.nextInt(3) == 0) 
			{
				plugin.logDebug(godName + " has too few priests. Finding one...");

				String believerName = getNextBelieverForPriest(godName);

				if (believerName == null) 
				{
					plugin.logDebug(godName + " could not find a candidate for a priest");
					return false;
				}

				player = plugin.getServer().getPlayer(believerName);

				if (player == null) 
				{
					return false;
				}

				if (setPendingPriest(godName, believerName)) 
				{
					plugin.log(godName + " offered " + player.getName() + " to be priest");
					plugin.getLanguageManager().setPlayerName(player.getName());

					GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToBelieverOfferPriest, 2);

					player.sendMessage(ChatColor.AQUA + "Type " + ChatColor.WHITE + "/godaccept" + ChatColor.AQUA + " or " + ChatColor.WHITE + "/godreject" + ChatColor.AQUA + " now.");

					return true;
				}
			}
		}

		for (String priestName : priestNames) 
		{		
			if (random.nextInt(1 + 1000/getVerbosityForGod(godName)) == 0) 
			{
				player = plugin.getServer().getPlayer(priestName);

				if (player == null) 
				{
					continue;
				}

				plugin.getLanguageManager().setPlayerName(priestName);
				int r = 0;
				int t = 0;
				
				do
				{				
					r = random.nextInt(3);
					t++;
				} while(t<50 && ((r==1 && !plugin.biblesEnabled) || (r==2 && !plugin.propheciesEnabled)));

				switch (r) 
				{					
					// General commandments speak
					case 0:
						switch (random.nextInt(4)) 
						{
								case 0:
									plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(getEatFoodTypeForGod(godName)));
									GodSayToPriest(godName, LanguageManager.LANGUAGESTRING.GodToPriestEatFoodType);
									break;
								case 1:
									plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(getNotEatFoodTypeForGod(godName)));
									GodSayToPriest(godName, LanguageManager.LANGUAGESTRING.GodToPriestNotEatFoodType);
									break;
								case 2:
									plugin.getLanguageManager().setType(plugin.getLanguageManager().getMobTypeName(getUnholyMobTypeForGod(godName)));									
									GodSayToPriest(godName, LanguageManager.LANGUAGESTRING.GodToPriestSlayMobType);
									break;
								case 3:
									plugin.getLanguageManager().setType(plugin.getLanguageManager().getMobTypeName(getHolyMobTypeForGod(godName)));						
									GodSayToPriest(godName, LanguageManager.LANGUAGESTRING.GodToPriestNotSlayMobType);
									break;
						}; return true; 
						
					case 1:
							if (plugin.biblesEnabled) 
							{
								String bibleTitle = plugin.getBibleManager().getBibleTitle(godName);
								this.plugin.getLanguageManager().setType(bibleTitle);
								GodSayToPriest(godName, LanguageManager.LANGUAGESTRING.GodToPriestUseBible);
								return true;
							} break;
						
					case 2:
							if (plugin.propheciesEnabled) 
							{
								String bibleTitle = plugin.getBibleManager().getBibleTitle(godName);
								plugin.getLanguageManager().setType(bibleTitle);
								GodSayToPriest(godName, LanguageManager.LANGUAGESTRING.GodToPriestUseProphecies);
								return true;
							}
							break;
							/*
					case 3:
							if (plugin.sacrificesEnabled) 
							{
								Material itemType = getSacrificeItemTypeForGod(godName);

								if (itemType != null) 
								{
									String itemName = plugin.getLanguageManager().getItemTypeName(itemType);
									plugin.getLanguageManager().setType(itemName);
									GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversSacrificeItemType, 2 + random.nextInt(100));
									
									return true;
								}
							} break;
				*/
					//case 4: GodSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversRandomSpeech, 2 + random.nextInt(100));
				}
			}
		}
		
		return false;
	}

	private void manageMood(String godName)
	{
		// Do not decrease when no believer is online
		if(plugin.getBelieverManager().getOnlineBelieversForGod(godName).size()==0)
		{
			return;
		}
		
		plugin.getGodManager().addMoodForGod(godName, plugin.getGodManager().getFalloffModifierForGod(godName));		
	}

	private boolean manageBelieverForExaltedGod(String godName, String believerName)
	{
		Player player = plugin.getServer().getPlayer(believerName);

		if (player == null) 
		{
			return false;
		}
		
		if ((player.getGameMode() != GameMode.CREATIVE) && (plugin.getPermissionsManager().hasPermission(player, "gods.itemblessings"))) 
		{
			if (!plugin.getBelieverManager().hasRecentItemBlessing(player.getName())) 
			{
				if (plugin.itemBlessingEnabled) 
				{
					float power = getGodPower(godName);

					if ((power >= plugin.minGodPowerForItemBlessings) && (random.nextInt((int) (1.0F + 50.0F / power)) == 0)) 
					{
						int healing = getHealthNeed(godName, player);

						if ((healing > 1) && (this.random.nextInt(2) == 0)) 
						{
							healPlayer(godName, player, getHealthBlessing(godName));

							plugin.getBelieverManager().setItemBlessingTime(believerName);

							return true;
						}

						ItemStack blessedItem = blessPlayerWithItem(godName, player);

						if (blessedItem != null) 
						{
							plugin.getLanguageManager().setPlayerName(believerName);
						
							plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(blessedItem.getType()));
						
							plugin.getBelieverManager().setItemBlessingTime(believerName);

							return true;
						}
					}
				}
			}
		}
		
		if (!plugin.getBelieverManager().hasRecentItemBlessing(player.getName())) 
		{
			if (blessPlayer(godName, believerName, getGodPower(godName))) 
			{
				plugin.getLanguageManager().setPlayerName(believerName);
				
				GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToPlayerBlessed, 2 + random.nextInt(10));
					
				GodSayToBelieversExcept(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPlayerBlessed, player.getName());
				
				return true;
			}
		}

		if (random.nextInt(1 + 1000/getVerbosityForGod(godName)) == 0) 
		{
			godSayToBeliever(godName, believerName, LanguageManager.LANGUAGESTRING.GodToBelieverRandomExaltedSpeech);
			return true;
		}

		if (random.nextInt(1 + 2000/getVerbosityForGod(godName)) == 0) 
		{
			if(godSayNeededSacrificeToBeliever(godName, believerName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private boolean manageBelieverForPleasedGod(String godName, String believerName)
	{
		Player player = plugin.getServer().getPlayer(believerName);

		if (player == null) 
		{
			return false;
		}
		
		if ((player.getGameMode() != GameMode.CREATIVE) && (plugin.getPermissionsManager().hasPermission(player, "gods.itemblessings"))) 
		{
			if (!plugin.getBelieverManager().hasRecentItemBlessing(player.getName())) 
			{
				if (plugin.itemBlessingEnabled) 
				{
					float power = getGodPower(godName);

					if ((power >= plugin.minGodPowerForItemBlessings) && (random.nextInt((int) (1.0F + 100.0F / power)) == 0)) 
					{
						int healing = getHealthNeed(godName, player);

						if ((healing > 1) && (this.random.nextInt(2) == 0)) 
						{
							healPlayer(godName, player,getHealthBlessing(godName));

							plugin.getBelieverManager().setItemBlessingTime(believerName);

							return true;
						}

						ItemStack blessedItem = blessPlayerWithItem(godName, player);

						if (blessedItem != null) 
						{
							plugin.getLanguageManager().setPlayerName(believerName);
						
							plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(blessedItem.getType()));
						
							plugin.getBelieverManager().setItemBlessingTime(believerName);

							return true;
						}
					}
				}
			}
		}

		if (random.nextInt(1 + 1000/getVerbosityForGod(godName)) == 0) 
		{
			godSayToBeliever(godName, believerName, LanguageManager.LANGUAGESTRING.GodToBelieverRandomPleasedSpeech);
			return true;
		}

		if (random.nextInt(1 + 2000/getVerbosityForGod(godName)) == 0) 
		{
			if(godSayNeededSacrificeToBeliever(godName, believerName))
			{
				return true;
			}
		}
		
		return false;
	}
	

	private boolean manageBelieverForNeutralGod(String godName, String believerName)
	{
		if (random.nextInt(1 + 1000/getVerbosityForGod(godName)) == 0) 
		{
			godSayToBeliever(godName, believerName, LanguageManager.LANGUAGESTRING.GodToBelieverRandomNeutralSpeech);
			return true;
		}
		
		if (random.nextInt(1 + 2000/getVerbosityForGod(godName)) == 0) 
		{
			if(godSayNeededSacrificeToBeliever(godName, believerName))
			{
				return true;
			}
		}

		return false;
	}

	private boolean manageBelieverForDispleasedGod(String godName, String believerName)
	{				
		if (random.nextInt(1 + 1000/getVerbosityForGod(godName)) == 0) 
		{
			godSayToBeliever(godName, believerName, LanguageManager.LANGUAGESTRING.GodToBelieverRandomDispleasedSpeech);
			return true;
		}

		if (random.nextInt(1 + 2000/getVerbosityForGod(godName)) == 0) 
		{
			if(godSayNeededSacrificeToBeliever(godName, believerName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private boolean manageBelieverForAngryGod(String godName, String believerName) 
	{
		int godPower = 1 + (int) plugin.getGodManager().getGodPower(godName);

		if (random.nextInt(1 + 1000 / godPower) == 0) 
		{
			Player player = plugin.getServer().getPlayer(believerName);

			if (player == null) 
			{
				return false;
			}

			if (cursePlayer(godName, believerName, godPower)) 
			{
				plugin.getLanguageManager().setPlayerName(believerName);
				
				GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToBelieverCursedAngry, 2 + random.nextInt(10));
				
				return true;
			}
		}
						
		if (random.nextInt(1 + 1000/getVerbosityForGod(godName)) == 0) 
		{
			godSayToBeliever(godName, believerName, LanguageManager.LANGUAGESTRING.GodToBelieverRandomAngrySpeech);
			return true;
		}

		if (random.nextInt(1 + 2000/getVerbosityForGod(godName)) == 0) 
		{
			if(godSayNeededSacrificeToBeliever(godName, believerName))
			{
				return true;
			}
		}
		
		return false;
	}

	private boolean godSayNeededSacrificeToBeliever(String godName, String believerName)
	{
		if (plugin.sacrificesEnabled) 
		{
			Material itemType = getSacrificeItemTypeForGod(godName);

			if (itemType != null) 
			{
				String itemName = plugin.getLanguageManager().getItemTypeName(itemType);
				plugin.getLanguageManager().setType(itemName);
				godSayToBeliever(godName, believerName, LanguageManager.LANGUAGESTRING.GodToBelieversSacrificeItemType);
					
				return true;
			}
		}

		return false;
	}
	
	private void manageLostBelievers(String godName)
	{
		if (random.nextInt(100) > 0) 
		{
			return;
		}
		
		Set<String> believers = plugin.getBelieverManager().getBelieversForGod(godName);
		Set<String> managedBelievers = new HashSet<String>();
		
		if (believers.size() == 0) 
		{
			return;
		}
		
		plugin.logDebug("Managing lost believers for " + godName);
		
		for (int n = 0; n < 5; n++) 
		{
			String believerName = (String) believers.toArray()[random.nextInt(believers.size())];
	
			if (!managedBelievers.contains(believerName)) 
			{	
				// Check for lost believer
				// Dont remove them all in one pass?
				//if (random.nextInt(1 + removedBelievers) == 0) 
				{
					Date thisDate = new Date();
					long timeDiff = thisDate.getTime() - plugin.getBelieverManager().getLastPrayerTime(believerName).getTime();

					if (timeDiff > 3600000 * plugin.maxBelieverPrayerTime) 
					{
						plugin.getLanguageManager().setPlayerName(believerName);

						godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversLostBeliever, 2 + random.nextInt(100));

						plugin.getBelieverManager().removeBeliever(godName, believerName);
					}
				}
			}

			managedBelievers.add(believerName);
		}
	}

	private void manageBelievers(String godName) 
	{
		Set<String> believers = plugin.getBelieverManager().getOnlineBelieversForGod(godName);
		Set<String> managedBelievers = new HashSet<String>();

		if (believers.size() == 0) 
		{
			return;
		}

		GodMood godMood = getMoodForGod(godName);
		
		// For talking about the priest
		List<String> priestNames = getPriestsForGod(godName);		
		
		for (int n = 0; n < 10; n++) 
		{
			String believerName = (String) believers.toArray()[random.nextInt(believers.size())];

			if (!managedBelievers.contains(believerName)) 
			{	
				if(priestNames.size()==0)
				{
					plugin.getLanguageManager().setPlayerName("our priest");
				}
				else
				{
					plugin.getLanguageManager().setPlayerName(priestNames.get(random.nextInt(priestNames.size())));
				}
					
				// Happy god supports with everything
				switch(godMood)
				{		
					case EXALTED	: manageBelieverForExaltedGod(godName, believerName); break;
					case PLEASED	: manageBelieverForPleasedGod(godName, believerName); break;
					case NEUTRAL	: manageBelieverForNeutralGod(godName, believerName); break;
					case DISPLEASED	: manageBelieverForDispleasedGod(godName, believerName); break;
					case ANGRY		: manageBelieverForAngryGod(godName, believerName); break;
				}

				managedBelievers.add(believerName);
			}
		}
	}

	private void manageCurses(String godName) 
	{
		if (!plugin.cursingEnabled) 
		{
			return;
		}

		String cursedPlayer = getCursedPlayerForGod(godName);

		if (cursedPlayer == null) 
		{
			return;
		}

		int godPower = 1 + (int) plugin.getGodManager().getGodPower(godName);

		if (random.nextInt(1 + 100 / godPower) == 0) 
		{
			Player player = plugin.getServer().getPlayer(cursedPlayer);

			if ((player == null) || (!plugin.getPermissionsManager().hasPermission(player, "gods.curses"))) 
			{
				return;
			}

			if (cursePlayer(godName, cursedPlayer, godPower)) 
			{
				plugin.getLanguageManager().setPlayerName(cursedPlayer);
				
				GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToPlayerCursed, 2 + random.nextInt(10));
				
				GodSayToBelieversExcept(godName, LanguageManager.LANGUAGESTRING.GodToBelieversPlayerCursed, player.getName());
			}

			//cursedPlayer = getEnemyPlayerForGod(godName);
		}
	}

	private void manageBlessings(String godName) 
	{
		if (!plugin.blessingEnabled) 
		{
			return;
		}

		String blessedPlayer = getBlessedPlayerForGod(godName);

		if (blessedPlayer == null) 
		{
			return;
		}

		int godPower = 1 + (int) getGodPower(godName);

		if (random.nextInt(1 + 100 / godPower) == 0) 
		{
			Player player = plugin.getServer().getPlayer(blessedPlayer);

			if ((player == null) || (!plugin.getPermissionsManager().hasPermission(player, "gods.blessings"))) 
			{
				return;
			}

			if (blessPlayer(godName, blessedPlayer, getGodPower(godName))) 
			{
				this.plugin.getLanguageManager().setPlayerName(blessedPlayer);
			
				GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToPlayerBlessed, 2 + random.nextInt(10));
				
				GodSayToBelieversExcept( godName, LanguageManager.LANGUAGESTRING.GodToBelieversPlayerBlessed, player.getName());
			}
		}
	}

	private boolean manageGlobalQuests()
	{
		if (!plugin.globalQuestsEnabled) 
		{
			return false;
		}

		if (!plugin.getQuestManager().hasGlobalQuest()) 
		{
			int globalQuestChance = plugin.globalQuestsPercentChance;

			if (random.nextInt(101) < globalQuestChance) 
			{
				plugin.logDebug("Starting generate global quest task");
				plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new GenerateGlobalQuestTask(plugin), 2L);

				return true;
			}

			return false;
		}

		long time = plugin.getQuestManager().getGlobalQuestLockedTime();

		if (time != -1L) 
		{
			if (time > 120000L) 
			{
				plugin.getQuestManager().removeGlobalQuest();
				plugin.logDebug("Removed locked global quest");
			}

		} 
		else if (plugin.getQuestManager().hasExpiredGlobalQuest()) 
		{
			GodsSayToBelievers(LanguageManager.LANGUAGESTRING.GodToBelieversPilgrimageQuestFailed, 2 + random.nextInt(20));
			//plugin.getQuestManager().
			
			plugin.getQuestManager().removeGlobalQuest();
		} 
		else if (random.nextInt(10) == 0) 
		{
			plugin.getQuestManager().godsSayStatus();
		}

		return false;
	}
	
	private void manageQuests(String godName) 
	{
		if (!plugin.questsEnabled) 
		{
			return;
		}

		int numberOfBelievers = plugin.getBelieverManager().getOnlineBelieversForGod(godName).size();

		if (!plugin.getQuestManager().hasQuest(godName)) 
		{
			int godPower = 1 + (int) plugin.getGodManager().getGodPower(godName);
			int demand = godPower * plugin.questFrequency;

			if ((numberOfBelievers < plugin.requiredBelieversForQuests) || (random.nextInt(1 + 100000 / demand) > 0)) 
			{
				return;
			}

			plugin.getQuestManager().generateQuest(godName);
		} 
		else if (plugin.getQuestManager().hasExpiredQuest(godName)) 
		{
			addMoodForGod(godName, getAngryModifierForGod(godName));

			plugin.getQuestManager().godSayFailed(godName);
						
			plugin.getQuestManager().removeQuestForGod(godName);
		} 
		else if (random.nextInt(20) == 0) 
		{
			plugin.getQuestManager().godSayStatus(godName);
		}
	}

	private Material getSacrificeNeedForGod(String godName)
	{
		Random materialRandom = new Random(getSeedForGod(godName));
		List<Integer> materials = new ArrayList<Integer>();
		
		for(int n=0; n<3; n++)
		{
			materials.add(materialRandom.nextInt(25));
		}		
		
		int typeIndex = 0;
		Material type = Material.AIR;
		
		do 
		{
			typeIndex = materials.get(random.nextInt(materials.size()));

			//NOTE: This should be items and NOT blocks (people can use lighter on blocks)
			switch (typeIndex) 
			{
				case 0: type = Material.RED_ROSE; break;
				case 1: type = Material.LEAVES; break;
				case 2: type = getNotEatFoodTypeForGod(godName); break;
				case 3: type = Material.IRON_HOE; break;
				case 4: type = Material.IRON_SWORD; break;
				case 5: type = Material.IRON_CHESTPLATE; break;
				case 6: type = Material.IRON_HELMET; break;
				case 7: type = Material.IRON_AXE; break;
				case 8: type = Material.IRON_INGOT; break;
				case 9: type = Material.GOLD_INGOT; break;
				case 10: type = Material.APPLE; break;
				case 11: type = Material.DIAMOND; break;
				case 12: type = Material.DIAMOND_SWORD; break;
				case 13: type = Material.BOOK; break;
				case 14: type = Material.CAKE; break;
				case 15: type = Material.MELON; break;
				case 16: type = Material.COOKIE; break;
				case 17: type = Material.PUMPKIN; break;
				case 18: type = Material.SUGAR_CANE; break;
				case 19: type = Material.EGG; break;
				case 20: type = Material.WHEAT; break;
				case 21: type = Material.ENDER_PEARL; break;
				case 22: type = Material.SPIDER_EYE; break;
				case 23: type = Material.POTATO; break;
				case 24: type = Material.BONE; break;
			}
		} while (type == getEatFoodTypeForGod(godName));
		
		return type;
	}
	
	private void manageSacrifices(String godName) 
	{
		if (!plugin.sacrificesEnabled) 
		{
			return;
		}

		int godPower = 1 + (int) plugin.getGodManager().getGodPower(godName);

		if (random.nextInt(1 + 300 / godPower) > 0) 
		{
			return;
		}
		
		// Handle positive sacrifice values		
		Material type = getSacrificeNeedForGod(godName);

		float value = getSacrificeValueForGod(godName, type);
		
		value += random.nextInt(2);

		// Cap it to a stack
		if(value>64)
		{
			value = 64;
		}
		else if(value<-64)
		{
			value = -64;
		}
						
		save();
		
		// Handle negative sacrifice values		
		type = getSacrificeUnwantedForGod(godName);
		
		if(type!=null)
		{
			value = 0.90f*getSacrificeValueForGod(godName, type);
			
			if(value>-0.01)
			{
				value = 0;
			}
			
			plugin.logDebug("Reducing unwanted " + type.name() + " sacrifice need for " + godName + " + to + " + value);
			
			if(value == 0)
			{
				godsConfig.set(godName + ".SacrificeValues." + type.name(), null);
			}
			else
			{
				godsConfig.set(godName + ".SacrificeValues." + type.name(), value);
			}

			save();						
		}

	}

	private void manageSacrifices() 
	{
		if (!plugin.sacrificesEnabled) 
		{
			return;
		}

		if (random.nextInt(10) > 0) 
		{
			return;
		}

		plugin.getAltarManager().clearDroppedItems();
	}

	private void manageHolyLands() 
	{
		if (!plugin.holyLandEnabled) 
		{
			return;
		}

		if (random.nextInt(1000) > 0) 
		{
			return;
		}

		plugin.getLandManager().removeAbandonedLands();
	}
	
	public void update() 
	{
		Set<String> godNames = getGods();

		long timeBefore = System.currentTimeMillis();

		manageGlobalQuests();

		if(godNames.size()==0)
		{
			return;
		}
		
		String godName = (String)godNames.toArray()[random.nextInt(godNames.size())];
		
		plugin.logDebug("Processing God '" + godName + "'");

		if (manageGod(godName)) 
		{
			boolean godTalk = false;
				
			manageMood(godName);
				
			if(!godTalk)
			{
				godTalk = managePriests(godName);
			}

			manageLostBelievers(godName);

			// If already talked to priest, dont talk again
			if(!godTalk)
			{
				manageBelievers(godName);
			}

			manageQuests(godName);

			manageBlessings(godName);

			manageCurses(godName);

			manageSacrifices(godName);
		}

		manageSacrifices();

		manageHolyLands();

		long timeAfter = System.currentTimeMillis();

		plugin.logDebug("Processed " + 1 + " God in " + (timeAfter - timeBefore) + " ms");
	}

	public class NewPriestComparator implements Comparator 
	{
		public NewPriestComparator() 
		{
		}

		public int compare(Object object1, Object object2) 
		{
			GodManager.PriestCandidate c1 = (GodManager.PriestCandidate) object1;
			GodManager.PriestCandidate c2 = (GodManager.PriestCandidate) object2;

			float power1 = GodManager.this.plugin.getBelieverManager().getBelieverPower(c1.name);
			float power2 = GodManager.this.plugin.getBelieverManager().getBelieverPower(c2.name);

			return (int) (power2 - power1);
		}
	}

	public class PriestCandidate 
	{
		public String name;

		PriestCandidate(String godName) 
		{
			this.name = godName;
		}
	}
}