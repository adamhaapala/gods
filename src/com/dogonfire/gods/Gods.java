package com.dogonfire.gods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Gods extends JavaPlugin 
{
	private ProphecyManager prophecyManager = null;
	private BossManager bossManager = null;
	private HolyArtifactManager holyArtifactManager = null;
	private ChatManager chatManager = null;
	private PermissionsManager permissionsManager = null;
	private HolyLandManager landManager = null;
	private HolyBookManager bibleManager = null;
	private WhitelistManager whitelistManager = null;
	private GodManager godManager = null;
	private QuestManager questManager = null;
	private BelieverManager believerManager = null;
	private AltarManager churchManager = null;
	private LanguageManager languageManager = null;
	private FileConfiguration config = null;
	private Commands commands = null;

	public boolean debug = false;
	public String languageIdentifier = "english";
	public boolean useWhitelist = false;
	public boolean useBlacklist = false;
	public boolean questsEnabled = true;
	public boolean itemBlessingEnabled = true;
	public boolean blessingEnabled = true;
	public boolean enableDetoration = false;
	public boolean commandmentsEnabled = true;
	public boolean sacrificesEnabled = true;
	public boolean holyLandEnabled = false;
	public boolean biblesEnabled = false;
	public boolean holyArtifactsEnabled = false;
	public boolean propheciesEnabled = false;
	public boolean chatFormattingEnabled = false;
	public boolean useGodTitles = true;

	public boolean cursingEnabled = true;
	public boolean lightningCurseEnabled = true;
	public boolean mobCurseEnabled = true;
	public int minCursingTime = 10;
	public int maxCursingTime = 10;
	
	public int minBlessingTime = 10*60;
	public int maxBlessingTime = 3*60;  // How long can a priest maximum keep a player blessed 
	public boolean fastDiggingBlessingEnabled = false;
	public boolean healBlessingEnabled = false;
	public boolean regenerationBlessingEnabled = false;
	public boolean speedBlessingEnabled = false;
	public boolean increaseDamageBlessingEnabled = false;
	
	public int questFrequency = 1;
	public boolean globalQuestsEnabled = false;
	public int globalQuestsPercentChance = 1;
	public boolean slayQuestsEnabled = true;
	public boolean sacrificeQuestsEnabled = true;
	public boolean pilgrimageQuestsEnabled = false;
	public boolean holyFeastQuestsEnabled = false;
	public boolean giveBiblesQuestsEnabled = true;
	public boolean burnBiblesQuestsEnabled = false;
	public boolean crusadeQuestsEnabled = false;
	public boolean convertQuestsEnabled = false;
	public boolean buildTowerQuestsEnabled = false;

	public String serverName = "Your Server";
	public boolean broadcastNewGods = true;
	public boolean broadcastProphecyFullfillment = true;
	public int maxPriestsPrGod = 3;
	public int numberOfBelieversPrPriest = 3;
	public int maxInvitationTimeSeconds = 60;
	public int minItemBlessingTime = 10;
	public int maxPriestPrayerTime = 8;
	public int maxBelieverPrayerTime = 154;
	public int minBelieverPrayerTime = 30;
	public int minGodPowerForItemBlessings = 3;
	public int godPowerForLevel3Items = 100;
	public int godPowerForLevel2Items = 50;
	public int godPowerForLevel1Items = 10;
	public int minBelieversForPriest = 3;
	public int requiredBelieversForQuests = 1;
	public int godVerbosity = 20;
	public boolean leaveReligionOnDeath = false;
	public boolean onlyPriestCanSetHome = false;
	public String priestAssignCommand = "";
	public String priestRemoveCommand = "";
	public Material altarBlockType = Material.SMOOTH_BRICK;
	//public String languageFilename = "english.yml";
	public boolean commandmentsBroadcastFoodEaten = true;
	public boolean commandmentsBroadcastMobSlain = true;
	public Set<Material> holylandBreakableBlockTypes = new HashSet<Material>();
	public double holyLandRadiusPrPower = 1.25D;
	public boolean holyLandDefaultPvP = false;
	public boolean holyLandDefaultMobDamage = true;
	public boolean holyLandLightning = true;
	//public boolean useBibleForPrayer = false;
	public boolean allowMultipleGodsPrDivinePower = false;

	public int minHolyLandRadius = 10;
	public int maxHolyLandRadius = 1000;

	public BossManager getBossManager() 
	{
		return this.bossManager;
	}

	public ProphecyManager getProphecyManager() 
	{
		return this.prophecyManager;
	}

	public HolyArtifactManager getHolyArtifactManager() 
	{
		return this.holyArtifactManager;
	}

	public HolyBookManager getBibleManager() 
	{
		return this.bibleManager;
	}

	public ChatManager getChatManager() 
	{
		return this.chatManager;
	}

	public PermissionsManager getPermissionsManager() 
	{
		return this.permissionsManager;
	}

	public HolyLandManager getLandManager() 
	{
		return this.landManager;
	}

	public AltarManager getAltarManager() 
	{
		return this.churchManager;
	}

	public QuestManager getQuestManager() 
	{
		return this.questManager;
	}

	public GodManager getGodManager() 
	{
		return this.godManager;
	}

	public BelieverManager getBelieverManager() 
	{
		return this.believerManager;
	}

	public LanguageManager getLanguageManager() 
	{
		return this.languageManager;
	}

	public WhitelistManager getWhitelistManager() 
	{
		return this.whitelistManager;
	}

	public boolean isWhitelistedGod(String godName) 
	{
		if (useWhitelist) 
		{
			return whitelistManager.isWhitelistedGod(godName);
		}
		
		return true;
	}

	public boolean isBlacklistedGod(String godName) 
	{
		if (this.useBlacklist) 
		{
			return this.whitelistManager.isBlacklistedGod(godName);
		}
		
		return false;
	}

	public void log(String message) 
	{
		Logger.getLogger("minecraft").info("[" + getDescription().getFullName() + "] " + message);
	}

	public void logDebug(String message) 
	{
		if (this.debug) 
		{
			Logger.getLogger("minecraft").info("[" + getDescription().getFullName() + "] " + message);
		}
	}

	public void sendInfo(Player player, String message) 
	{
		player.sendMessage(ChatColor.AQUA + message);
	}

	public void reloadSettings() 
	{
		reloadConfig();

		loadSettings();

		whitelistManager.load();
	}

	public void loadSettings() 
	{
		// Always clear bosses: They will re-generate
		if(getBossManager()!=null)
		{
			getBossManager().removeDragons();
		}		
		
		this.config = getConfig();

		this.debug = config.getBoolean("Settings.Debug", false);
		this.languageIdentifier = config.getString("Settings.Language", "english");

		this.biblesEnabled = this.config.getBoolean("Bibles.Enabled", true);
		//this.useBibleForPrayer = false;//this.config.getBoolean("Bibles.UseBibleForPrayer", false); // CANNOT do this because it will conflict with GIVEBIBLES quests

		if (this.biblesEnabled) 
		{
			this.bibleManager = new HolyBookManager(this);
			this.bibleManager.load();
		}

		//this.holyArtifactsEnabled = this.config.getBoolean("HolyArtifacts.Enabled", true);

		if (this.holyArtifactsEnabled) 
		{
			this.holyArtifactManager = new HolyArtifactManager(this);
			this.holyArtifactManager.load();
		}

		//this.propheciesEnabled = this.config.getBoolean("Prophecies.Enabled", false);

		if (this.propheciesEnabled) 
		{
			this.prophecyManager = new ProphecyManager(this);
			this.prophecyManager.load();

			this.bossManager = new BossManager(this);
			//this.bossManager.load();
		}

		this.chatFormattingEnabled = this.config.getBoolean("ChatFormatting.Enabled", false);

		if (this.chatFormattingEnabled) 
		{
			this.chatManager = new ChatManager(this);
			this.chatManager.load();
		}

		this.holyLandEnabled = this.config.getBoolean("HolyLand.Enabled", false);

		if (this.holyLandEnabled) 
		{
			this.landManager = new HolyLandManager(this);
			this.landManager.load();
		}

		this.minHolyLandRadius = this.config.getInt("HolyLand.MinRadius", 10);
		this.maxHolyLandRadius = this.config.getInt("HolyLand.MaxRadius", 1000);
		this.holyLandRadiusPrPower = this.config.getDouble("HolyLand.RadiusPrPower", 1.25D);
		this.holyLandDefaultPvP = this.config.getBoolean("HolyLand.DefaultPvP",false);
		this.holyLandDefaultMobDamage = this.config.getBoolean("HolyLand.DefaultMobDamage", true);
		this.holyLandLightning = this.config.getBoolean("HolyLand.Lightning", false);

		List<String> blockList = config.getStringList("HolyLand.BreakableBlockTypes");

		if ((blockList != null) && (blockList.size() > 0)) 
		{
			for (String blockType : blockList) 
			{
				try 
				{
					logDebug("adding breakable block type " + blockType);
					holylandBreakableBlockTypes.add(Material.getMaterial(blockType));
				} 
				catch (Exception ex) 
				{
					log("ERROR parsing HolyLand.BreakableBlockTypes blocktype '" + blockType + "' in config");
				}
			}
		} 
		else 
		{
			log("No HolyLand.BreakableBlockTypes section found in config.");
			log("Adding '" + this.altarBlockType.name() + "' to BreakableBlockTypes");
			this.holylandBreakableBlockTypes.add(this.altarBlockType);
		}

		this.sacrificesEnabled = this.config.getBoolean("Sacrifices.Enabled", true);

		this.commandmentsEnabled = this.config.getBoolean("Commandments.Enabled", true);
		this.commandmentsBroadcastFoodEaten = this.config.getBoolean("Commandments.BroadcastFoodEaten", true);
		this.commandmentsBroadcastMobSlain = this.config.getBoolean("Commandments.BroadcastMobSlain", true);

		this.questsEnabled = this.config.getBoolean("Quests.Enabled", true);
		this.questFrequency = this.config.getInt("Quests.Frequency", 1);
		this.globalQuestsPercentChance = this.config.getInt("Quests.GlobalQuestsPercentChance", 1);

		this.slayQuestsEnabled = this.config.getBoolean("Quests.SlayQuests", true);
		this.sacrificeQuestsEnabled = this.config.getBoolean("Quests.SacrificeQuests", true);
		this.convertQuestsEnabled = this.config.getBoolean("Quests.ConvertQuests", true);
		this.giveBiblesQuestsEnabled = this.config.getBoolean("Quests.GivebiblesQuests", true);

		ConfigurationSection configSection = config.getConfigurationSection("Quests.RewardValues");

		if (configSection != null) 
		{
			for (String rewardItem : configSection.getKeys(false)) 
			{
				try 
				{
					logDebug("Setting value for reward item " + rewardItem + " to " + this.config.getInt(new StringBuilder().append("Quests.RewardValues.").append(rewardItem).toString()));

					getQuestManager().setItemRewardValue(Material.getMaterial(rewardItem), this.config.getInt("Quests.RewardValues." + rewardItem));
				} 
				catch (Exception ex) 
				{
					log("ERROR parsing Quests.RewardValues value '" + rewardItem + "' in config");
				}
			}
		} 
		else 
		{
			getQuestManager().resetItemRewardValues();
		}

		blessingEnabled = config.getBoolean("Blessing.Enabled", true);
		speedBlessingEnabled = config.getBoolean("Blessing.Speed", true);
		regenerationBlessingEnabled = config.getBoolean("Blessing.Regeneration", true);
		healBlessingEnabled = config.getBoolean("Blessing.Heal", true);
		fastDiggingBlessingEnabled = config.getBoolean("Blessing.FastDigging", true);
		increaseDamageBlessingEnabled = config.getBoolean("Blessing.IncreaseDamage", true);
		minBlessingTime = config.getInt("Blessing.MinBlessingTime", 10*60);
		maxBlessingTime = config.getInt("Blessing.MaxBlessingTime",  3*60);
		
		cursingEnabled = config.getBoolean("Cursing.Enabled", true);
		lightningCurseEnabled = config.getBoolean("Cursing.LightningCurse", true);
		mobCurseEnabled = config.getBoolean("Cursing.MobCurse", true);
		maxCursingTime = config.getInt("Cursing.MaxCursingTime", 10);
		minCursingTime = config.getInt("Cursing.MinCursingTime", 5);
		
		this.itemBlessingEnabled = this.config.getBoolean("ItemBlessing.Enabled", true);
		this.minItemBlessingTime = this.config.getInt("ItemBlessing.MinItemBlessingTime", 10);
		this.minGodPowerForItemBlessings = this.config.getInt("ItemBlessing.MinGodPowerForItemBlessings", 3);
		this.godPowerForLevel1Items = this.config.getInt("ItemBlessing.GodPowerForLevel1Items", 10);
		this.godPowerForLevel2Items = this.config.getInt("ItemBlessing.GodPowerForLevel2Items", 50);
		this.godPowerForLevel3Items = this.config.getInt("ItemBlessing.GodPowerForLevel3Items", 100);
		this.onlyPriestCanSetHome = this.config.getBoolean("Settings.OnlyPriestCanSetHome", false);
		this.leaveReligionOnDeath = this.config.getBoolean("Settings.LeaveReligionOnDeath", false);
		this.maxPriestPrayerTime = this.config.getInt("Settings.MaxPriestPrayerTime", 72);
		this.maxBelieverPrayerTime = this.config.getInt("Settings.MaxBelieverPrayerTime", 154);
		this.minBelieverPrayerTime = this.config.getInt("Settings.MinBelieverPrayerTime", 30);
		this.minBelieversForPriest = this.config.getInt("Settings.MinBelieversForPriest", 3);
		this.maxPriestsPrGod = this.config.getInt("Settings.MaxPriestsPrGod", 1);
		//this.numberOfBelieversPrPriest = this.config.getInt("Settings.NumberOfBelieversPrPriest", 3);
		this.broadcastNewGods = this.config.getBoolean("Settings.BroadcastNewGods", true);
		this.useWhitelist = this.config.getBoolean("Settings.UseWhitelist",false);
		this.useBlacklist = this.config.getBoolean("Settings.UseBlacklist",false);
		this.godVerbosity = this.config.getInt("Settings.GodVerbosity", 20);
		this.serverName = this.config.getString("Settings.ServerName","Your Server");
		//this.languageFilename = this.config.getString("Settings.LanguageFile","english.yml");
		this.priestAssignCommand = this.config.getString("Settings.PriestAssignCommand", "");
		this.priestRemoveCommand = this.config.getString("Settings.PriestRemoveCommand", "");
		this.allowMultipleGodsPrDivinePower = this.config.getBoolean("Settings.AllowMultipleGodsPrDivinePower", false);
		
		try 
		{
			this.altarBlockType = Material.getMaterial(this.config.getString("Settings.AltarBlockType", Material.SMOOTH_BRICK.name()));
		} 
		catch (Exception ex) 
		{
			this.altarBlockType = Material.SMOOTH_BRICK;
		}
	}

	public void saveSettings() 
	{
		this.config.set("Settings.Debug", Boolean.valueOf(this.debug));
		this.config.set("Settings.Language", languageIdentifier);
		this.config.set("Settings.UseWhitelist", Boolean.valueOf(this.useWhitelist));
		this.config.set("Settings.UseBlacklist", Boolean.valueOf(this.useBlacklist));
		this.config.set("Settings.BroadcastNewGods", Boolean.valueOf(this.broadcastNewGods));
		this.config.set("Settings.MaxPriestPrayerTime", Integer.valueOf(this.maxPriestPrayerTime));
		this.config.set("Settings.MaxBelieverPrayerTime", Integer.valueOf(this.maxBelieverPrayerTime));
		this.config.set("Settings.MinBelieverPrayerTime", Integer.valueOf(this.minBelieverPrayerTime));
		this.config.set("Settings.MinBelieversForPriest", Integer.valueOf(this.minBelieversForPriest));
		this.config.set("Settings.MaxPriestsPrGod", Integer.valueOf(this.maxPriestsPrGod));
		this.config.set("Settings.NumberOfBelieversPrPriest", Integer.valueOf(this.numberOfBelieversPrPriest));
		this.config.set("Settings.GodVerbosity", Integer.valueOf(this.godVerbosity));
		this.config.set("Settings.ServerName", this.serverName);
		this.config.set("Settings.PriestAssignCommand", priestAssignCommand);
		this.config.set("Settings.PriestRemoveCommand", priestRemoveCommand);
		this.config.set("Settings.OnlyPriestCanSetHome",Boolean.valueOf(this.onlyPriestCanSetHome));
		this.config.set("Settings.LeaveReligionOnDeath",Boolean.valueOf(this.leaveReligionOnDeath));
		this.config.set("Settings.AllowMultipleGodsPrDivinePower",Boolean.valueOf(this.allowMultipleGodsPrDivinePower));
		
		try 
		{
			config.set("Settings.AltarBlockType", altarBlockType.name());
		} 
		catch (Exception ex) 
		{
			config.set("Settings.AltarBlockType", Material.SMOOTH_BRICK.name());
		}

		//this.config.set("Settings.LanguageFile", this.languageFilename);
		this.config.set("ItemBlessing.Enabled", Boolean.valueOf(this.itemBlessingEnabled));
		this.config.set("ItemBlessing.MinGodPowerItemBlessings", Integer.valueOf(this.minGodPowerForItemBlessings));
		this.config.set("ItemBlessing.GodPowerForLevel1Items", Integer.valueOf(this.godPowerForLevel1Items));
		this.config.set("ItemBlessing.MinItemBlessingTime", Integer.valueOf(this.minItemBlessingTime));
		this.config.set("ItemBlessing.GodPowerForLevel2Items", Integer.valueOf(this.godPowerForLevel2Items));
		this.config.set("ItemBlessing.GodPowerForLevel3Items", Integer.valueOf(this.godPowerForLevel3Items));

		config.set("Blessing.Enabled", Boolean.valueOf(blessingEnabled));
		config.set("Blessing.Speed", Boolean.valueOf(speedBlessingEnabled));
		config.set("Blessing.Heal", Boolean.valueOf(healBlessingEnabled));
		config.set("Blessing.Regeneration", Boolean.valueOf(regenerationBlessingEnabled));
		config.set("Blessing.IncreaseDamage", Boolean.valueOf(increaseDamageBlessingEnabled));
		config.set("Blessing.FastDigging", Boolean.valueOf(fastDiggingBlessingEnabled));
		config.set("Blessing.MaxBlessingTime", Integer.valueOf(maxBlessingTime));
		config.set("Blessing.MinBlessingTime", Integer.valueOf(minBlessingTime));
		
		this.config.set("Cursing.Enabled", Boolean.valueOf(this.cursingEnabled));
		this.config.set("Cursing.LightningCurse", Boolean.valueOf(this.lightningCurseEnabled));
		this.config.set("Cursing.MobCurse", Boolean.valueOf(this.mobCurseEnabled));
		this.config.set("Cursing.MaxCursingTime", Integer.valueOf(this.maxCursingTime));
		this.config.set("Cursing.MinCursingTime", Integer.valueOf(this.minCursingTime));

		this.config.set("Quests.Enabled", Boolean.valueOf(this.questsEnabled));
		this.config.set("Quests.Frequency",Integer.valueOf(this.questFrequency));
		this.config.set("Quests.GlobalQuestsPercentChance", Integer.valueOf(this.globalQuestsPercentChance));
		this.config.set("Quests.SlayQuests", Boolean.valueOf(this.slayQuestsEnabled));
		this.config.set("Quests.SacrificeQuests", Boolean.valueOf(this.sacrificeQuestsEnabled));
		this.config.set("Quests.ConvertQuests", Boolean.valueOf(this.convertQuestsEnabled));
		this.config.set("Quests.GivebiblesQuests", Boolean.valueOf(this.giveBiblesQuestsEnabled));

		this.config.set("Sacrifices.Enabled", Boolean.valueOf(this.sacrificesEnabled));

		this.config.set("Commandments.Enabled", Boolean.valueOf(this.commandmentsEnabled));
		this.config.set("Commandments.BroadcastFoodEaten", Boolean.valueOf(this.commandmentsBroadcastFoodEaten));
		this.config.set("Commandments.BroadcastMobSlain", Boolean.valueOf(this.commandmentsBroadcastMobSlain));
		this.config.set("HolyLand.Enabled", Boolean.valueOf(this.holyLandEnabled));
		this.config.set("HolyLand.MinRadius", Integer.valueOf(this.minHolyLandRadius));
		this.config.set("HolyLand.MaxRadius", Integer.valueOf(this.maxHolyLandRadius));
		this.config.set("HolyLand.RadiusPrPower", Double.valueOf(this.holyLandRadiusPrPower));
		this.config.set("HolyLand.DefaultPvP", Boolean.valueOf(this.holyLandDefaultPvP));
		this.config.set("HolyLand.DefaultMobDamage",Boolean.valueOf(this.holyLandDefaultMobDamage));
		this.config.set("HolyLand.Lightning", Boolean.valueOf(this.holyLandLightning));

		List<String> blockTypes = new ArrayList<String>();
		for (Material blockType : this.holylandBreakableBlockTypes) 
		{
			blockTypes.add(blockType.name());
		}

		this.config.set("HolyLand.BreakableBlockTypes", blockTypes);

		for (Material rewardItem : getQuestManager().getRewardItems()) 
		{
			this.config.set("Quests.RewardValues." + rewardItem.name(), Integer.valueOf(getQuestManager().getRewardValue(rewardItem)));
		}

		this.config.set("ChatFormatting.Enabled", Boolean.valueOf(this.chatFormattingEnabled));

		this.config.set("Bibles.Enabled", Boolean.valueOf(this.biblesEnabled));
		//this.config.set("Bibles.UseBibleForPrayer", Boolean.valueOf(this.useBibleForPrayer));
			
		this.config.set("Prophecies.Enabled", Boolean.valueOf(this.propheciesEnabled));

		saveConfig();
	}

	public void onEnable() 
	{
		this.permissionsManager = new PermissionsManager(this);
		this.questManager = new QuestManager(this);
		this.godManager = new GodManager(this);
		this.believerManager = new BelieverManager(this);
		this.languageManager = new LanguageManager(this);
		this.churchManager = new AltarManager(this);
		this.whitelistManager = new WhitelistManager(this);
		this.commands = new Commands(this);

		// Just to make sure there is a config filled out
		loadSettings();
		saveSettings();

		this.permissionsManager.load();
		this.languageManager.load();
		this.godManager.load();
		this.questManager.load();
		this.believerManager.load();
		this.whitelistManager.load();

		getServer().getPluginManager().registerEvents(new BlockListener(this), this);
		getServer().getPluginManager().registerEvents(new ChatListener(this), this);

		Runnable updateTask = new Runnable() 
		{
			public void run() 
			{
				Gods.this.godManager.update();
			}
		};
		
		// Was 700
		//getServer().getScheduler().scheduleSyncRepeatingTask(this, updateTask, 20L, 200L);
		getServer().getScheduler().runTaskTimerAsynchronously(this, updateTask, 20L, 200L);

		try 
		{
			Metrics metrics = new Metrics(this);

			metrics.addCustomData(new Metrics.Plotter("Servers") 
			{
				public int getValue() 
				{
					return 1;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using quests") {
				public int getValue() {
					if (Gods.this.questsEnabled) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using item blessings") {
				public int getValue() {
					if (Gods.this.itemBlessingEnabled) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using blessings") {
				public int getValue() {
					if (Gods.this.blessingEnabled) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using curses") {
				public int getValue() {
					if (Gods.this.cursingEnabled) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using whitelist") {
				public int getValue() {
					if (Gods.this.useWhitelist) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using blacklist") {
				public int getValue() {
					if (Gods.this.useBlacklist) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using commandments") {
				public int getValue() {
					if (Gods.this.commandmentsEnabled) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using holy land") {
				public int getValue() {
					if (Gods.this.holyLandEnabled) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using PermissionsBukkit") {
				public int getValue() {
					if (Gods.this.getPermissionsManager()
							.getPermissionPluginName()
							.equals("PermissionsBukkit")) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using PermissionsEx") {
				public int getValue() {
					if (Gods.this.getPermissionsManager()
							.getPermissionPluginName().equals("PermissionsEx")) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using GroupManager") {
				public int getValue() {
					if (Gods.this.getPermissionsManager()
							.getPermissionPluginName().equals("GroupManager")) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using bPermissions") {
				public int getValue() {
					if (Gods.this.getPermissionsManager()
							.getPermissionPluginName().equals("bPermissions")) {
						return 1;
					}
					return 0;
				}
			});
			metrics.addCustomData(new Metrics.Plotter("Using Bibles") {
				public int getValue() {
					if (Gods.this.biblesEnabled) {
						return 1;
					}
					return 0;
				}
			});
			metrics.start();
		} 
		catch (Exception ex) 
		{
			log("Failed to submit metrics :-(");
		}
		
	}

	public void onDisable() 
	{
		reloadSettings();

		//saveSettings();

		this.godManager.save();
		this.questManager.save();
		this.believerManager.save();

		if ((this.useBlacklist) || (this.useWhitelist)) 
		{
			this.whitelistManager.save();
		}

		if (this.holyLandEnabled) 
		{
			this.landManager.save();
		}

		if (this.biblesEnabled)
		{
			this.bibleManager.save();
		}
		
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		return this.commands.onCommand(sender, cmd, label, args);
	}
}