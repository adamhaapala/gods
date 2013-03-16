package com.dogonfire.gods;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

import java.io.*;
import java.net.*;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import com.dogonfire.gods.GodManager.GodMood;
import com.dogonfire.gods.GodManager.GodType;
import com.dogonfire.gods.GodManager.GodGender;

public class LanguageManager
{
	private Gods										plugin;
	private String										generalLanguageFileName		= null;
	//private HashMap<String, HashMap<GodGender, File>>	languageConfigFiles	= new HashMap<String, HashMap<GodGender, File>>();
	private HashMap<String, FileConfiguration>			languageConfigs		= new HashMap<String, FileConfiguration>();
	private Random										random				= new Random();
	private int											amount;
	private String										playerName;
	private String										type;

	
	private void downloadLanguageFile(String fileName) throws IOException
	{
		java.io.BufferedInputStream in = new java.io.BufferedInputStream(new java.net.URL("http://www.doggycraft.dk/plugins/gods/lang/" + fileName).openStream());
	
		java.io.FileOutputStream fos = new java.io.FileOutputStream(plugin.getDataFolder() + "/lang/" + fileName);
	
		java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
	
		byte[] data = new byte[1024];
		
		int x=0;
		
		while( (x = in.read(data,0,1024)) >= 0)
		{
			bout.write(data, 0, x);
		}
	
		bout.close();
	
		in.close();	
	}
	
/*	private boolean loadGeneralLanguageFile() 
	{			
	    plugin.logDebug("Loading default language file '" + generalLanguageFileName + "'");
	            
		File languageConfigFile = new File(plugin.getDataFolder() + "/lang/" + generalLanguageFileName);
		generalLanguageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);
						
		return generalLanguageConfig!=null;
	}
*/
	private boolean loadLanguageFile(String fileName)
	{
		File languageConfigFile = new File(plugin.getDataFolder() + "/lang/" + fileName);
		
		if(!languageConfigFile.exists())
		{
			return false;
		}

		FileConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);
				
		languageConfigs.put(fileName, languageConfig);	
		
		plugin.logDebug("Loaded " + languageConfig.getString("Version.Name") + " by " + languageConfig.getString("Version.Author") + " version " + languageConfig.getString("Version.Version"));
		
		return true;
	}
/*	
	private int loadLanguageFiles() 
	{	
		File directory = new File(plugin.getDataFolder() + "/lang");
		int numberOfFilesLoaded = 0;
		
		for (final File fileEntry : directory.listFiles()) 
	    {
	        if (fileEntry.isDirectory()) 
	        {

	        } 
	        else 
	        {	            
	            if(!fileEntry.getName().endsWith(".yml"))
	            {
	            	continue;	            	
	            }

	            plugin.logDebug("Loading language file '" + fileEntry.getName() + "'");
	            
				File languageConfigFile = new File(plugin.getDataFolder() + "/lang", plugin.languageFilename);
				FileConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);
				
				languageConfigs.put(fileEntry.getName(), languageConfig);
				
				//languageConfigFiles.get(fileEntry.getName()).put(YamlConfiguration.loadConfiguration(languageConfigFile));									          
				
				numberOfFilesLoaded++;
	        }
	    }
		
		return numberOfFilesLoaded;
	}
*/		
	public void load()
	{
		generalLanguageFileName = plugin.languageIdentifier + "_general.yml";
		
		plugin.logDebug("generalFileName is " + generalLanguageFileName);
		plugin.logDebug("plugin.language is " + plugin.languageIdentifier);
		
		File directory = new File(plugin.getDataFolder() + "/lang");

		// if the directory does not exist, create it
		if (!directory.exists())
		{
			System.out.println("Creating language file directory '/lang'...");
		
			boolean result = directory.mkdir();  
		    
		    if(result)
		    {    
		    	plugin.logDebug("Directory created");  
		    }
		    else
		    {
		    	plugin.logDebug("Directory FAILED!");
		    	return;
		    }
		}

		if(!loadLanguageFile(generalLanguageFileName))
		{
			plugin.log("Could not load " + generalLanguageFileName + " from the /lang folder!");
			plugin.log("Downloading " + generalLanguageFileName + " from DogOnFire...");
			
			try
			{
				downloadLanguageFile(generalLanguageFileName);
			}
			catch(Exception ex)
			{
				plugin.log("Could not download " + generalLanguageFileName + " language file from DogOnFire: " + ex.getMessage());
				return;
			}			

			if(loadLanguageFile(generalLanguageFileName))
			{		
				plugin.log(generalLanguageFileName + " loaded.");
			}
		}
		
		//int numberOfFiles = loadLanguageFiles();
				
		for(GodType godType : GodType.values())
		{
			for(GodGender godGender : GodGender.values())
			{
				if(godGender==GodGender.None)
				{
					continue;
				}
				
				String fileName = plugin.languageIdentifier + "_" + godType.name().toLowerCase() + "_" + godGender.name().toLowerCase() + ".yml";
				
				if(!loadLanguageFile(fileName))
				{
					plugin.log("Could not load language file " + fileName + " from the /lang folder!");
					plugin.log("Downloading english files from bukkit...");
			
					try
					{
						downloadLanguageFile(fileName);
					}
					catch(Exception ex)
					{
						plugin.log("Could not download language file " + fileName + " from bukkit: " + ex.getMessage());
						continue;
					}

					loadLanguageFile(fileName);
				}
				
				plugin.log("Loaded " + fileName + ".");
			}
		}

		//plugin.log("Loaded " + numberOfFiles + " language files.");

		/*
		for (LANGUAGESTRING languageString : LANGUAGESTRING.values())
		{
			if ((languageConfig.getList(languageString.name()) == null) || (languageConfig.getList(languageString.name()).size() == 0))
			{
				plugin.log("WARNING: No language strings for " + languageString.name() + "!");
			}
		}
		*/
	}

	// LANGUAGE MANAGER SHOULD NEVER SAVE
	private void save()
	{
		/*
		if (languageConfig == null || languageConfigFile == null)
		{
			return;
		}

		try
		{
			languageConfig.save(this.languageConfigFile);
		} 
		catch (Exception ex)
		{
			this.plugin.log("Could not save config to " + this.languageConfigFile + ": " + ex.getMessage());
		}
		*/
	}

	private String getLanguageFileForGod(String godName)
	{
		return plugin.getGodManager().getLanguageFileForGod(godName);
	}

	public String getLanguageString(String godName, LANGUAGESTRING type)
	{
		List<String> strings = languageConfigs.get(getLanguageFileForGod(godName)).getStringList(type.name());

		if (strings.size() == 0)
		{
			plugin.log("No language strings found for " + godName + "," + type.name() + "!");
			return type.name() + " MISSING";
		}

		String text = (String) strings.toArray()[random.nextInt(strings.size())];

		return parseString(text);
	}

	public String getLanguageStringForBook(String godName, LANGUAGESTRING type)
	{
		List<String> strings = languageConfigs.get(getLanguageFileForGod(godName)).getStringList(type.name());

		if (strings.size() == 0)
		{
			this.plugin.log("No language strings found for " + type.name() + "!");
			return type.name() + " MISSING";
		}

		String text = (String) strings.toArray()[random.nextInt(strings.size())];

		return parseStringForBook(text);
	}


	public boolean setDefault()
	{
		/*
		List<String> list = new ArrayList<String>();

		list.add("I am proud of you my Children! You have showed true faith in me!");
		list.add("You make me proud, my children! You have followed my words and proven your faith!");
		list.add("Our faith is growing! You will all be rewarded!");
		list.add("$PlayerName, I am proud of you! Your faith will be rewarded!");
		list.add("$PlayerName, you make me proud! You shall be rewarded!");
		list.add("Yes! I can feel your faith in me! Our faith is a shining beacon for everyone to follow!");
		list.add("Very good! Your faith is strong! I am proud of you all!");
		list.add("Together, we will show the people of $ServerName the shining light of our faith!");

		languageConfig.set(LANGUAGESTRING.GodToBelieverRandomExaltedSpeech.name(), list);

		list = new ArrayList<String>();
		list.add("I am pleased with you all, my children!");
		list.add("Very good, my Children! We are doing well!");
		list.add("Good my Children. Pray harder, and I shall reward you!");
		list.add("Good. You are showing faith in me. Listen to my words and we will all be stronger!");
		list.add("I can feel your faith, $PlayerName!");
		list.add("Good $PlayerName, your faith is strong!");
		list.add("You are doing well, my children! Keep praying to me!");
		list.add("Listen to my words and all will be well.");

		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverRandomPleasedSpeech.name(), list);

		list = new ArrayList<String>();
		list.add("I must have more believers. Get me more believers.");
		list.add("Help me to get more believers and i shall reward you!");
		list.add("More believers is more power!");
		list.add("Remember, the more believers we have the powerful we are!");
		list.add("Spread our religion, and i shall reward you!");
		list.add("We must make our religion known, go and tell others!");
		list.add("Pray at my altars!");
		list.add("Children, go forth and build altars in my name!");
		list.add("My children, do not listen to the words of the false gods!");
		list.add("I am the only true god for $ServerName");
		list.add("$ServerName needs my powers");
		list.add("Only I have the power to handle $ServerName!");
		list.add("Pray at my altars and i will show you the light!");
		list.add("Belief is power!");
		list.add("Believe in me and i will make you stronger!");
		list.add("Help me spreading my words to others in $ServerName!");
		list.add("My Children, only we hold the future of $ServerName!");
		list.add("Pray to me, and get stronger!");
		list.add("Do you believe in $ServerName ? Then pray to me!");
		list.add("My Children, i need your belief to get stronger!");
		list.add("My Children, only you can help me get stronger!");
		list.add("My Children, I will take care of you. Always!");
		list.add("Remember that I am always here, watching over each of you.");
		list.add("You are all my Children and I will always protect you!");
		list.add("My Children, make me proud!");
		list.add("My Children, we must get people to believe!");
		list.add("I know what is best for $ServerName!");
		list.add("Believe in me, my Children!");
		list.add("Pray to me, my Children!");
		list.add("Build altars and spread our religion!");
		list.add("Believe in me and i will take care of you, my Children!");
		list.add("Pray to me, and I will listen!");
		list.add("Make me proud, my Children!");
		list.add("Together, we are stronger!");
		list.add("Pray to me, and we will all become stronger!");
		list.add("Pray to me for a better world!");
		list.add("Do not listen to the words of false Gods! Pray to me!");
		list.add("Listen to the words of $PlayerName, he will help you to understand!");
		list.add("If you feel lost, talk to $PlayerName! He will show you the light!");
		list.add("Remember, $PlayerName is our priest, he will guide you towards the light!");

		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverRandomNeutralSpeech.name(), list);

		list = new ArrayList<String>();
		list.add("We are losing faith, my children... Pray harder!");
		list.add("I can sense your lack of faith... Pray to me and listen to my words!");
		list.add("$PlayerName, we are losing faith! Help me to re-gain faith from our believers!");
		list.add("I am worried about $PlayerName. My followers are losing faith! Please spread my words!");
		list.add("I am disturbed by your lack of faith! Pray harder!");

		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieverRandomDispleasedSpeech.name(),
						list);

		list = new ArrayList<String>();
		list.add("PRAY TO ME!!");
		list.add("NO! You must pray to me!!");
		list.add("I am VERY dissapointed with you!!");
		list.add("$PlayerName have shown no faith in our cause!! You WILL ALL feel my wrath!");
		list.add("You have lost faith in me and lost your way!! I cannot allow this!");

		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverRandomAngrySpeech.name(), list);

		list = new ArrayList<String>();
		list.add("I have not asked you for anything!");
		this.languageConfig.set(LANGUAGESTRING.GodToBelieverNoQuestion.name(),
				list);

		list = new ArrayList<String>();
		list.add("My Children, $PlayerName has accepted to be my priest! A bright future awaits us!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversPriestAccepted.name(), list);

		list = new ArrayList<String>();
		list.add("Tell our believers of our most sacred food: The $Type!");
		list.add("Tell our believers to eat our holy food: The $Type!");
		list.add("Tell our believers about the joy of $Type! You must eat it!");
		this.languageConfig.set(LANGUAGESTRING.GodToPriestEatFoodType.name(),
				list);

		list = new ArrayList<String>();
		list.add("$PlayerName, never eat the $Type! It is the unclean food!");
		list.add("$PlayerName, you are forbidden to eat $Type! It is the unholy food!");
		list.add("$PlayerName, you must never eat $Type! It is unholy food!");
		list.add("$PlayerName, tell our believers to never eat $Type! It is unclean!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToPriestNotEatFoodType.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName, no harm must ever come to our beloved $Type!");
		list.add("Tell our believers of our most sacred creature: The $Type!");
		list.add("Tell our believers to respect our holy creature: The $Type!");
		list.add("$PlayerName, you must tell our believers about our holy creature: The $Type!");
		list.add("$PlayerName, nothing is as holy as the $Type!");
		list.add("$PlayerName, protect our holy of creature: The $Type!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToPriestNotSlayMobType.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName, we must get rid of the $Type! It is a plague to this world!");
		list.add("The $Type is a unholy creature. Tell our believers to slay it!");
		list.add("The $Type is a nasty creature. We must get rid of it!");
		list.add("$PlayerName, tell our believers to slay the $Type. It is a unholy creature!");
		this.languageConfig.set(LANGUAGESTRING.GodToPriestSlayMobType.name(),
				list);

		list = new ArrayList<String>();
		list.add("Good my child, you are now my priest. Let us work for a better world together!");
		list.add("Very well $PlayerName, my priest. Let us work for a better world!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToPriestPriestAccepted.name(), list);

		list = new ArrayList<String>();
		list.add("Very well, I will bring down my curses upon $PlayerName!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToPriestCursedPlayerSet.name(), list);

		list = new ArrayList<String>();
		list.add("Very well, $PlayerName will no longer be cursed by me!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToPriestCursedPlayerUnset.name(), list);

		list = new ArrayList<String>();
		list.add("Very well, I will give my blessings to $PlayerName!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToPriestBlessedPlayerSet.name(), list);

		list = new ArrayList<String>();
		list.add("Very well, $PlayerName will no longer be blessed by me.");
		this.languageConfig.set(
				LANGUAGESTRING.GodToPriestBlessedPlayerUnset.name(), list);

		list = new ArrayList<String>();
		list.add("Very well my child. I will look for one more suited as my priest.");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverPriestRejected.name(), list);

		list = new ArrayList<String>();
		list.add("My Children, I will now give my blessings to $PlayerName!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBlessedPlayerSet.name(), list);

		list = new ArrayList<String>();
		list.add("My Children, $PlayerName will no longer feel my blessings.");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBlessedPlayerUnset.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName, you are an insult to our glory. Feel my wrath!");
		list.add("$PlayerName, you have angered me! Take this!");
		list.add("$PlayerName, feel my wrath! Take this!");
		list.add("$PlayerName, you have angered me and you must suffer!");
		this.languageConfig.set(LANGUAGESTRING.GodToPlayerCursed.name(), list);

		list = new ArrayList<String>();
		list.add("Bless you, $PlayerName!");
		list.add("Here $PlayerName, receive my blessings!");
		list.add("$PlayerName, go with my blessings!");
		this.languageConfig.set(LANGUAGESTRING.GodToPlayerBlessed.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName, please join our faith!");
		list.add("$PlayerName, will you join our religion?");
		languageConfig.set(LANGUAGESTRING.GodToPlayerInvite.name(), list);

		list = new ArrayList<String>();
		list.add("Welcome to our faith, $PlayerName!");
		languageConfig.set(LANGUAGESTRING.GodToPlayerAcceptedInvitation.name(), list);

		list = new ArrayList<String>();
		list.add("My Children, $PlayerName will no longer feel my curses.");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversCursedPlayerUnset.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName has just been blessed by me!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversPlayerBlessed.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName has just been cursed by me!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversPlayerCursed.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName, I have selected you as my priest in $ServerName. Do you accept this honor?");
		this.languageConfig.set(LANGUAGESTRING.GodToBelieverOfferPriest.name(),
				list);

		list = new ArrayList<String>();
		list.add("Thank you my child, for praying to me!");
		list.add("Thank you for praying to me, my child!");
		list.add("I hear your prayer, my child!");
		this.languageConfig.set(LANGUAGESTRING.GodToBelieverPraying.name(),
				list);

		list = new ArrayList<String>();
		list.add("Thank you my child, for building this church in my name!");
		this.languageConfig.set(LANGUAGESTRING.GodToBelieverAltarBuilt.name(),
				list);

		list = new ArrayList<String>();
		list.add("$PlayerName has left our religion!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversPlayerLeftReligion.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName has joined our religion!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversPlayerJoinedReligion.name(), list);

		list = new ArrayList<String>();
		list.add("You look hurt my child. Let me bless you.");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverHealthBlessing.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName has eaten the divine $Type. Recieve my blessings!");
		list.add("$PlayerName has eaten the holy $Type. Be praised!");
		list.add("Well done, $PlayerName! You have eaten the divine $Type!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversEatFoodBlessing.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName ate $Type! Feel my wrath!");
		list.add("$PlayerName, never eat $Type! It is the cursed food!  It is feel my wrath!");
		list.add("$Type is the cursed food. $PlayerName, feel my wrath!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversNotEatFoodCursing.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName has righteously slain the unholy $Type. Receive my blessings!");
		list.add("$PlayerName has slain the unholy $Type with honor. Well done!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSlayMobBlessing.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName, the $Type is our holy creature! It must never be harmed!");
		list.add("$PlayerName! Never do harm to the holy $Type! Feel my wrath!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversNotSlayMobCursing.name(), list);

		list = new ArrayList<String>();
		list.add("Here my child. Take this $Type with my blessings.");
		list.add("$PlayerName, i see that you have no $Type. Take this with my blessings!");
		list.add("$PlayerName, you seem to need a $Type. Take this.");
		list.add("$PlayerName, you need some $Type. Take this with my blessings!");
		list.add("$PlayerName, you should have a $Type. Take this with my blessings!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverItemBlessing.name(), list);

		list = new ArrayList<String>();
		list.add("My Children, I am displeased in you. You have failed to perform this simple task for me...");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSlayQuestFailed.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName has lost his faith in me. I do no longer feel him as part of our religion...");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversLostBeliever.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName has abandoned our religion!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversPlayerLeftReligion.name(), list);

		list = new ArrayList<String>();
		list.add("Oh no! I find myself having no priest! I must find a priest to do my bidding!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversRemovedPriest.name(), list);

		list = new ArrayList<String>();
		list.add("Well done my children! You have shown your faith in my commands. Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSlayQuestCompleted.name(), list);

		list = new ArrayList<String>();
		list.add("My children! I need you to slay $Amount $Type in my honor!");
		list.add("My children, show your faith in me! I need you to slay $Amount $Type in my honor!");
		list.add("The time has come to show your faith in me! I need you to slay $Amount $Type in my honor!");
		list.add("Now hear this, my Children! You must go forth and slay $Amount $Type in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSlayQuestStarted.name(), list);

		list = new ArrayList<String>();
		list.add("Well done my children! Just $Amount $Type left to slay!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSlayQuestProgress.name(), list);

		list = new ArrayList<String>();
		list.add("My children, you still need to slay $Amount $Type in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSlayQuestStatus.name(), list);

		list = new ArrayList<String>();
		list.add("My children! I need you to build $Amount new altars in my honor!");
		list.add("My children, you must show your faith in me! I need you to build $Amount more altars in my honor!");
		list.add("The time has come to show your faith in me! I need you to build $Amount new altars in my honor!");
		list.add("Now hear this, my Children! You must go forth and build $Amount new altars in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBuildAltarsQuestStarted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My children, you still need to build $Amount altars in my honor!");
		list.add("Remember, you must still build $Amount altars in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBuildAltarsQuestStatus.name(),
				list);

		list = new ArrayList<String>();
		list.add("Well done my children! Only $Amount altars left to build in my honor!");
		list.add("Very good! You only need to build $Amount more altars in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBuildAltarsQuestProgress.name(),
				list);

		list = new ArrayList<String>();
		list.add("Well done my children! You have shown your faith in my commands. Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBuildAltarsQuestCompleted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My Children, I am displeased in you. You have failed to perform this simple task for me...");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBuildAltarsQuestFailed.name(),
				list);

		list = new ArrayList<String>();
		list.add("My children! I need you to build a tower $Amount blocks tall in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBuildTowerQuestStarted.name(),
				list);

		list = new ArrayList<String>();
		list.add("Remember, you must build my $Type tower of height $Amount in my honor!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversBuildTowerQuestStatus.name(),
						list);

		list = new ArrayList<String>();
		list.add("Well done my children! Only $Amount blocks left to build on the tower in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBuildTowerQuestProgress.name(),
				list);

		list = new ArrayList<String>();
		list.add("Well done my children! You have shown your faith in my commands. Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBuildTowerQuestCompleted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My Children, I am displeased in you. You have failed to perform this simple task for me...");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversBuildTowerQuestFailed.name(),
						list);

		list = new ArrayList<String>();
		list.add("My children! I need you to burn $Amount $Type as a sacrifice to me!");
		list.add("My children! I ask you now to burn $Amount $Type as a sacrifice to me!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversSacrificeQuestStarted.name(),
						list);

		list = new ArrayList<String>();
		list.add("My children, you must sacrifice $Amount $Type to me! Burn them by fire!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSacrificeQuestStatus.name(), list);

		list = new ArrayList<String>();
		list.add("Well done my children! Only $Amount $Type left to sacrifice in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSacrificeQuestProgress.name(),
				list);

		list = new ArrayList<String>();
		list.add("Well done my children! You have shown your faith in my commands. Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSacrificeQuestCompleted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My Children, I am displeased in you. You have failed to perform this simple task for me...");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSacrificeQuestFailed.name(), list);

		list = new ArrayList<String>();
		list.add("My children! I need you to turn $Amount away from the darkness and into our religion!");
		list.add("My children! I need you to convert $Amount from their false beliefs and into our religion!");
		list.add("My children, you must show $Amount the true ways of our faith!");
		list.add("My children, you must guide $Amount into the light of our religion!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversConvertQuestStarted.name(), list);

		list = new ArrayList<String>();
		list.add("My children, you must still turn $Amount heathens from the dark side into our religion!");
		list.add("Remember, you must still convince $Amount non-believers to join our religion!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversConvertQuestStatus.name(), list);

		list = new ArrayList<String>();
		list.add("Well done, my children! $Amount heathens left to be converted to our side!");
		list.add("Glorius, another believer sees our light! Only $Amount left to convert!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversConvertQuestProgress.name(), list);

		list = new ArrayList<String>();
		list.add("Well done my children! You have shown your faith and gathered believers into our religion. Here is your reward!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversConvertQuestCompleted.name(),
						list);

		list = new ArrayList<String>();
		list.add("My Children, I am displeased in you. You have failed to perform this simple task for me...");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversConvertQuestFailed.name(), list);

		list = new ArrayList<String>();
		list.add("My children, hear this! You must now go and let $Amount heathens read our Holy Book!");
		list.add("My children, you must show $Amount non-believers the true path by letting them read our Holy Book!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversGiveBiblesQuestStarted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My children, you must still let $Amount more non-believers read our Holy Book!");
		list.add("Remember, you must still let $Amount more non-believers read our Holy Book!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversGiveBiblesQuestStatus.name(),
						list);

		list = new ArrayList<String>();
		list.add("Well done, my children! $Amount heathens left to be converted to our side!");
		list.add("Very good, another believer sees our light! Only $Amount more non-believers must read our Holy Book!");
		list.add("Very good, $PlayerName has read $Type! Only $Amount more non-beleivers must read our Holy Book!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversGiveBiblesQuestProgress.name(),
				list);

		list = new ArrayList<String>();
		list.add("Well done my children! You have shown your faith shown non-believers the light of our religion. Here is your reward!");
		list.add("I am proud of you, my children! You have shown our words of truth to the non-believers! Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversGiveBiblesQuestCompleted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My Children, I am displeased in you. You have failed to perform this simple task for me...");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversGiveBiblesQuestFailed.name(),
						list);

		list = new ArrayList<String>();
		list.add("My children, you must BURN $Amount $Type in my honor!");
		list.add("The nasty of religion of $PlayerName is a insult to us! Let us BURN their pityful holy book!");
		list.add("My Children, the time has come to BURN those nasty holy books of $PlayerName!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBurnBiblesQuestStarted.name(),
				list);

		list = new ArrayList<String>();
		list.add("Remember, you must BURN $Amount more $Type in my honor!");
		list.add("My children, BURN their unholy books of $PlayerName! Do not let them pollute this world!");
		list.add("You BURN the $Type! Do not let them pollute this world!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversBurnBiblesQuestStatus.name(),
						list);

		list = new ArrayList<String>();
		list.add("Well done, my children! Only $Amount $Type left to BURN in my honor!");
		list.add("Glorious, my children! Let us BURN $Amount more in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBurnBiblesQuestProgress.name(),
				list);

		list = new ArrayList<String>();
		list.add("Well done my children! You have ridden the world of $Type! Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversBurnBiblesQuestCompleted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My Children, I am displeased in you. You have not shown respect for our holy food...");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversBurnBiblesQuestFailed.name(),
						list);

		list = new ArrayList<String>();
		list.add("My children, you must hold a feast! Eat $Amount $Type in my honor!");
		list.add("My children, celebrate our faith and eat $Amount $Type in my honor!");
		list.add("Let us celebrate the wonder of $Type! You must eat $Amount of them in my honor!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversHolyFeastQuestStarted.name(),
						list);

		list = new ArrayList<String>();
		list.add("My children, you must feast and eat $Amount more $Type in my honor!");
		list.add("My children, let us celebrate the glory of $Type! Eat $Amount $Type more in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversHolyFeastQuestStatus.name(), list);

		list = new ArrayList<String>();
		list.add("Well done, my children! Only $Amount $Type left to eat in my honor!");
		list.add("Glorious, my children! Only $Amount $Type left to eat in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversHolyFeastQuestProgress.name(),
				list);

		list = new ArrayList<String>();
		list.add("Well done my children! You have feasted well. Here is your reward!");
		list.add("That was a wonderful eating part and you have eaten well. Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversHolyFeastQuestCompleted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My Children, I am displeased in you. You have shown no respect for our holy food...");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversHolyFeastQuestFailed.name(), list);

		list = new ArrayList<String>();
		list.add("My children, it is time to show those non-believers who hold the truth in $ServerName!");
		list.add("Grab your swords, my children! We must rid $ServerName from the heathens!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversCrusadeQuestStarted.name(), list);

		list = new ArrayList<String>();
		list.add("My children, you must slay $Amount more non-believers in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversCrusadeQuestStatus.name(), list);

		list = new ArrayList<String>();
		list.add("Well done, my children! Only $Amount non-believers left to kill in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversCrusadeQuestProgress.name(), list);

		list = new ArrayList<String>();
		list.add("Well done my children! You have uphold the justice in &6$ServerName&f. Here is your reward!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversCrusadeQuestCompleted.name(),
						list);

		list = new ArrayList<String>();
		list.add("My Children, you have FAILED to get my &6$Type&f first. I am very dissapointed...");
		list.add("Oh no! You did not fetch me &6$Type&f in time...");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversCrusadeQuestFailed.name(), list);

		list = new ArrayList<String>();
		list.add("My children, it is time to show those non-believers who hold the truth in $ServerName!");
		list.add("Grab your swords, my children! We must rid $ServerName from the heathens!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversHolyBattleQuestStarted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My children, you must slay $Amount more non-believers in my honor!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversHolyBattleQuestStatus.name(),
						list);

		list = new ArrayList<String>();
		list.add("Well done, my children! Kill those infidels!");
		list.add("Well done, my children! Slay them all!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversHolyBattleQuestProgress.name(),
				list);

		list = new ArrayList<String>();
		list.add("Well done my children! You have uphold the justice in $ServerName. Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversHolyBattleQuestCompleted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My Children, you have FAILED to reach the temple of $PlayerName first. I am dissapointed...");
		list.add("Oh no! We did not reach the temple of $PlayerName in time...");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversHolyBattleQuestFailed.name(),
						list);

		list = new ArrayList<String>();
		list.add("My children, I have learned of Holy Artifact in a the lost temple of $PlayerName! We MUST get it before the other Gods!");
		list.add("My children, I have seen a Holy Artifact in the lost temple of $PlayerName! We MUST get to it before the other Gods!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversPilgrimageQuestStarted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My children, you must hurry! Get to the lost temple of $PlayerName before the false Gods!");
		list.add("My children, please hurry! Find the lost temple of $PlayerName before it is too late!");
		list.add("The Holy $Type will guide you to the lost temple of $PlayerName! Go now!");
		list.add("My children, you must follow our Holy $Type to the lost temple of $PlayerName before it is too late!");
		list.add("Hurry! Go to the temple of $Type before it is too late!");
		list.add("We must get to the temple of $Type before it is too late!");
		list.add("Use our Holy Book and let our $Type show you the way to the temple of $PlayerName!");
		list.add("Our Holy Book will show guide you to the temple of $PlayerName!");
		list.add("Do not let the temple of $PlayerName get into the hand of the false Gods!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversPilgrimageQuestStatus.name(),
						list);

		list = new ArrayList<String>();
		list.add("Well done, my children! Only $Amount non-believers left to kill in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversPilgrimageQuestProgress.name(),
				list);

		list = new ArrayList<String>();
		list.add("I am proud of you my children! You travelled far and completed your goal. Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversPilgrimageQuestCompleted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My Children, I am displeased in you. You have not shown respect for our faith...");
		list.add("My Children, I am displeased in you. You have failed to complete this simple task for me...");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversPilgrimageQuestFailed.name(),
						list);

		list = new ArrayList<String>();
		list.add("My children, I have just remembered that I left a $Type while visiting my believers 500 years ago! Retrieve it before it fall into the wrong hands!");
		list.add("OH NO!, I have just remembered that I left a $Type while visiting my believers 500 years ago! Retrieve it before it fall into the wrong hands!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactThisQuestStarted
						.name(), list);

		list = new ArrayList<String>();
		list.add("My children, $PlayerName has carelessly left a $Type in an ancient temple! We MUST go and get it for our own purposes!");
		list.add("I have just learned that $PlayerName has carelessly left a $Type in an ancient temple! We MUST get it for ourselves!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactOtherQuestStarted
						.name(), list);

		list = new ArrayList<String>();
		list.add("My children, please hurry! Find my lost $Type before it is too late!");
		list.add("Our holy book will guide you to my lost $Type! Go now!");
		list.add("My children, you must follow our Holy Creature to my $Type before it is too late!");
		list.add("Hurry! Go to the lost temple and get my $Type before it is too late!");
		list.add("We must get to the temple and get the lost $Type before it is too late!");
		list.add("I cannot believe I was so careless to leave my $Type in that temple...");
		list.add("Use our Holy Book and let our Holy Creature show you the way to the lost temple!");
		list.add("Our Holy Book will show guide you to my $Type!");
		list.add("Please, do not let my $Type get into the hand of the false Gods!");
		languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactThisQuestStatus
						.name(), list);

		list = new ArrayList<String>();
		list.add("Use /g hunt to join the hunt for the artifact");
		languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactQuestHelp.name(),
				list);

		list = new ArrayList<String>();
		list.add("You are $Amount blocks away from the holy artifact!");
		languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactQuestRange.name(),
				list);

		list = new ArrayList<String>();
		list.add("My children, please hurry! Find the lost $Type of $PlayerName before they get to it!");
		list.add("The Holy Creature will guide you to the lost temple of $PlayerName! Go now!");
		list.add("My children, follow our Holy Creature to the lost $Type of $PlayerName before it is too late!");
		list.add("Hurry! We must get that $Type before anyone else!");
		list.add("$PlayerName is such a FOOL to leave that $Type lying around in a temple...");
		list.add("Show those foolish followers of $PlayerName who has the true power in this world!");
		list.add("The foolish $PlayerName will see our power when we take their precious $Type right before their eyes!");
		list.add("Remember, we must still get to the $Type before anyone else!");
		list.add("Use our Holy Book and let our Holy Creature show you the way to the lost $Type!");
		list.add("Do not let that $Type get into the hand of the foolish followers of $PlayerName!");
		languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactOtherQuestStatus
						.name(), list);

		list = new ArrayList<String>();
		list.add("Well done, my children! Only $Amount non-believers left to kill in my honor!");
		languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactQuestProgress
						.name(), list);

		list = new ArrayList<String>();
		list.add("WELL DONE! You have saved my $Type from falling into the wrong hands of other Gods. Here is your reward!");
		list.add("I am proud of you! You have prevented my $Type from falling into the wrong hands of other Gods. Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactThisQuestCompleted
						.name(), list);

		list = new ArrayList<String>();
		list.add("Oh no... My $Type have fallen into the hand of another faith... You have failed me...");
		list.add("Oh no... My $Type is lost to the false Gods... We have failed...");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactThisQuestFailed
						.name(), list);

		list = new ArrayList<String>();
		list.add("WELL DONE! We succesfully claimed the $Type of $PlayerName for ourselves! I can almost hear $PlayerName crying now! Here is your reward!");
		list.add("HAHA! We succesfully took $PlayerName's $Type away right infront of their eyes! $PlayerName is such a fool! Here is your reward!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactOtherQuestCompleted
						.name(), list);

		list = new ArrayList<String>();
		list.add("NOO... The $Type of was retrieved by the followers of $PlayerName . Such a shame... ");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversGetHolyArtifactOtherQuestFailed
						.name(), list);

		list = new ArrayList<String>();
		list.add("My children, we must claim the Holy Land of $type!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversClaimHolyLandQuestStarted.name(),
				list);

		list = new ArrayList<String>();
		list.add("My children, you must hurry! Claim the Holy land of $Type before the false Gods!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversClaimHolyLandQuestStatus.name(),
				list);

		list = new ArrayList<String>();
		list.add("Well done, my children! Only $Amount more second to hold non-believers left to kill in my honor!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversClaimHolyLandQuestProgress.name(),
				list);

		list = new ArrayList<String>();
		list.add("I am proud of you my children! You travelled far and completed your goal. Here is your reward!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToBelieversClaimHolyLandQuestCompleted
						.name(), list);

		list = new ArrayList<String>();
		list.add("My Children, I am displeased in you. You have not shown respect for our faith...");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversClaimHolyLandQuestFailed.name(),
				list);

		list = new ArrayList<String>();
		list.add("$PlayerName, you are a TRUE believer!");
		list.add("Thank you, $PlayerName!");
		list.add("Well done, $PlayerName!");
		list.add("Yes, $PlayerName! Bring me more $Type");
		list.add("Excellent sacrifice, $PlayerName!");
		list.add("Thank you $PlayerName, for your sacrifice of $Type!");
		list.add("A most glorious sacrifice $PlayerName!");
		list.add("Yes $PlayerName! $Type is indeed a proper sacrifice!");
		list.add("This is perfect $PlayerName! $Type is just what I require for our cause!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverGoodSacrifice.name(), list);

		list = new ArrayList<String>();
		list.add("I require no $Type at this time, $PlayerName.");
		list.add("$PlayerName, please do not sacrifice more $Type for now.");
		list.add("Very generous $PlayerName, but i do not require this $Type.");
		list.add("That is a nice gesture $PlayerName, but i do not require more $Type.");
		list.add("That is appreciated $PlayerName, but do not give me more $Type for now.");
		list.add("A nice thought, $PlayerName. But i do not require $Type at this time.");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverMehSacrifice.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName, stop giving me $Type!");
		list.add("$PlayerName, stop sending me those dreaded $Type!");
		list.add("$PlayerName, I had enough of your $Type!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverBadSacrifice.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName, $Type is our sacred food! You must NEVER burn that!");
		list.add("$Type is our most holy food! How dare you throw it into the flames!");
		list.add("$PlayerName, how dare you insult our holy $Type!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverHolyFoodSacrifice.name(), list);

		list = new ArrayList<String>();
		list.add("I am dismayed at your lack of attention $PlayerName! Feel my wrath!");
		list.add("You have NOT shown respect have angered me!");
		list.add("$PlayerName, you have angered me! You must pray harder and listen to my words!");
		list.add("$PlayerName, you have angered me and you must suffer!");
		list.add("You have not paid attention to my words! You have all angered me and you must suffer! ");
		list.add("I am very disturbed at your lack of faith! You will suffer!");
		this.languageConfig.set(LANGUAGESTRING.GodToBelieverCursedAngry.name(),
				list);

		list = new ArrayList<String>();
		list.add("Do NOT attack $PlayerName! Take this!");
		list.add("No! Do not attack $PlayerName!");
		list.add("Get away from $PlayerName!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieverSmiteBlessing.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName just destroyed our altar!");
		list.add("Our altar was destroyed by $PlayerName!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversAltarDestroyedByPlayer.name(),
				list);

		list = new ArrayList<String>();
		list.add("Our altar was destroyed!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversAltarDestroyed.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName just specified his location as our home!");
		this.languageConfig.set(LANGUAGESTRING.GodToBelieversSetHome.name(),
				list);

		list = new ArrayList<String>();
		list.add("We are now at WAR with ungodly believers of $PlayerName! Kill them and make me proud!");
		list.add("My children, we are now at WAR with $PlayerName! Kill them all on sight!");
		this.languageConfig.set(LANGUAGESTRING.GodToBelieversWar.name(), list);

		list = new ArrayList<String>();
		list.add("We have joined forces with $PlayerName! Welcome our brothers!");
		list.add("The believers of $PlayerName are now our allies! Let us work together!");
		list.add("The followers of $PlayerName are now brothers and sisters in faith!");
		this.languageConfig.set(LANGUAGESTRING.GodToBelieversAlliance.name(),
				list);

		list = new ArrayList<String>();
		list.add("We are no longer at WAR with $PlayerName!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversWarCancelled.name(), list);

		list = new ArrayList<String>();
		list.add("We are no longer allied with $PlayerName!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversAllianceCancelled.name(), list);

		list = new ArrayList<String>();
		list.add("Show your faith in me! Sacrifice some $Type in my name!");
		list.add("My children, i require some $Type from you! Sacrifice it to me!");
		list.add("My children, i must have some $Type! Sacrifice it to me!");
		list.add("My children, go forth and sacrifice some $Type in my name!");
		list.add("I ask of you all: Gather some $Type and Sacrifice it to me by fire!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversSacrificeItemType.name(), list);

		list = new ArrayList<String>();
		list.add("Before time, there was a cold room. Then, through a door, $PlayerName entered the room, booted up the server and installed minecraft. $PlayerName nodded and thus spake: 'Let's rock...");
		list.add("In the beginning there was only darknesss. Then out of nothing, $PlayerName created the BLOCKS. $PlayerName saw it was good and thus spake: 'Cool'");
		list.add("From the darknesss, $PlayerName made light and from the light he made $ServerName. $PlayerName saw it was good and thus spake: 'I rock!");
		list.add("In only 3 days $PlayerName made the world from a single block of dirt. $PlayerName saw that it was good and thus spake: 'I shall call it ... $ServerName!'");
		this.languageConfig.set(LANGUAGESTRING.DefaultBibleText1.name(), list);

		list = new ArrayList<String>();
		list.add("Next, $PlayerName decided to populate the fields with life. First among living things was the original mob. It was named '$Type - The firstborn'.");
		list.add("$PlayerName gave life to the world and through infinite wisdom, decided to spawn some mobs. And first among them was the $Type.");
		list.add("$PlayerName mumbled to himself \"Hmm, this map sux... Oh, I know: Let's have some mobs!\". And $PlayerName started to spawn some $Type!");
		this.languageConfig.set(LANGUAGESTRING.DefaultBibleText2.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName saw that his poor creation was starving and thus spake: \"Lo and behold: A $Type! You must eat it!\"");
		list.add("$PlayerName could see that his poor creation hungry and therefore said to it: \"Here, have some $Type! That is serious good eating, trust me!\"");
		this.languageConfig.set(LANGUAGESTRING.DefaultBibleText3.name(), list);

		list = new ArrayList<String>();
		list.add("And thus $PlayerName spoke upon his creation and said: \"You are to eat all things in $ServerName except the $Type! That, you must never eat!\"");
		list.add("$PlayerName turned to his creation and spoke: \"You may eat all things in $ServerName except the $Type! You must never eat the $Type! $Type is not for eating! Is that clear enough?\"");
		list.add("$PlayerName said: \"Hey yo! You can eat whatever you want here in $ServerName but NOT any $Type! Keep your hands off $Type and all is cool, OK?\"");
		list.add("$PlayerName now said: \"You can eat anything you want here in $ServerName but i pity the fool that eats any $Type! Anyone who eats $Type will feel my wrath!\"");
		this.languageConfig.set(LANGUAGESTRING.DefaultBibleText4.name(), list);

		list = new ArrayList<String>();
		list.add("But alas, on $PlayerName's first and only fishing day, the foul $Type crept upon his innocent creation and tempted the firstborn to eat the very forbidden food which $PlayerName had warned him about!");
		list.add("And so, $PlayerName's took a day off. But when he returned he saw a $Type sitting on his innocent creation laughing while shoving the forbidden food down the throat of the firstborn!");
		this.languageConfig.set(LANGUAGESTRING.DefaultBibleText5.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName was furious when learning about this event and cursed the $Type saying: \"Forever will you and your kind be cursed! You shall be hunted down and slain whereever you are found!\"");
		list.add("$PlayerName went angered and took the $Type by the throat, saying: \"You just messed with the wrong God, dude! You better watch your back when walking down the street in the future, PUNK!\"");
		this.languageConfig.set(LANGUAGESTRING.DefaultBibleText6.name(), list);

		list = new ArrayList<String>();
		list.add("\"Never again shall anyone eat the nasty $Type!\" said $PlayerName. \"Forever shall it be the cursed food!\"");
		list.add("$PlayerName thus spake: \"Henceforth let it be known that $Type is the cursed food!\". And behold: The very skies soon filled with burning $Type as a symbol of $PlayerName's will.");
		this.languageConfig.set(LANGUAGESTRING.DefaultBibleText7.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName, use our Holy Book to let others see learn the ways of our religion!");
		list.add("$PlayerName, spread the word using our Holy Book! Give it to non-believers to they may join us!");
		list.add("$PlayerName, give our Holy Book to others so they may see the truth of our religion!");
		list.add("Remember $PlayerName, you must spread the truth by giving out our Holy Book to others!");
		list.add("$PlayerName, you must spread our words by giving the $Type to others!");
		list.add("$PlayerName, read from $Type so that others may see the light!");
		this.languageConfig
				.set(LANGUAGESTRING.GodToPriestUseBible.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName, I have seen what will happen! My prophecies are written in our Holy Book!");
		list.add("$PlayerName, let our believers know about my prophecies! Read from our Holy Book!");
		list.add("$PlayerName, read my prophecies to the non-believers so that they will see the truth of our religion!");
		list.add("Remember $PlayerName, my prophecies are written in our Holy Book!");
		this.languageConfig.set(LANGUAGESTRING.GodToPriestUseProphecies.name(),
				list);

		list = new ArrayList<String>();
		list.add(ChatColor.GOLD
				+ "The Prophecies of $PlayerName "
				+ ChatColor.BLACK
				+ "                                                       These are things I foreseen will come to happen in "
				+ plugin.serverName + ".");
		this.languageConfig.set(LANGUAGESTRING.ProphecyHeaderBibleText.name(),
				list);

		list = new ArrayList<String>();
		list.add("A champion will slay the nasty $Type in the glory of my name.");
		this.languageConfig.set(
				LANGUAGESTRING.UnholyMobWillBeSlainFutureProphecyBibleText
						.name(), list);

		list = new ArrayList<String>();
		list.add("The unholy $Type was slain by the mighty $PlayerName");
		this.languageConfig
				.set(LANGUAGESTRING.UnholyMobWillBeSlainPastProphecyBibleText
						.name(), list);

		list = new ArrayList<String>();
		list.add(" and the skies darkened and storms raged across the lands!");
		this.languageConfig.set(
				LANGUAGESTRING.StormProphecyEffectPastBibleText.name(), list);

		list = new ArrayList<String>();
		list.add(" and storms will sweep the lands!");
		this.languageConfig.set(
				LANGUAGESTRING.StormProphecyEffectFutureBibleText.name(), list);

		list = new ArrayList<String>();
		list.add("The Dragon Lord $PlayerName will be slain by a mighty champion!");
		this.languageConfig.set(
				LANGUAGESTRING.DragonBossWillBeSlainFutureProphecyBibleText
						.name(), list);

		list = new ArrayList<String>();
		list.add("The Dragon Lord $PlayerName was slain");
		this.languageConfig.set(
				LANGUAGESTRING.DragonBossWillBeSlainPastProphecyBibleText
						.name(), list);

		list = new ArrayList<String>();
		list.add(" and the Dragon Lord $PlayerName appeared and ruled the land of $ServerName with terror");
		this.languageConfig.set(
				LANGUAGESTRING.DragonBossProphecyEffectPastBibleText.name(),
				list);

		list = new ArrayList<String>();
		list.add(" and the Dragon Lord $PlayerName will appear and rule the land of $ServerName with terror");
		this.languageConfig.set(
				LANGUAGESTRING.DragonBossProphecyEffectFutureBibleText.name(),
				list);

		list = new ArrayList<String>();
		list.add("Rejoice! All the prophecies in $PlayerName has been fulfilled!");
		this.languageConfig
				.set(LANGUAGESTRING.HolyFoodRainProphecyEffectBibleText.name(),
						list);

		list = new ArrayList<String>();
		list.add("Rejoice! All the prophecies in $PlayerName has been fulfilled!");
		languageConfig.set(
				LANGUAGESTRING.DarknessProphecyEffectBibleText.name(), list);

		list = new ArrayList<String>();
		list.add("Rejoice! All the prophecies in $PlayerName has been fulfilled!");
		languageConfig
				.set(LANGUAGESTRING.HeathenWillBeSlainProphecyEffectBibleText
						.name(), list);

		list = new ArrayList<String>();
		list.add("The cowardly $PlayerName left our faith, turning to madness");
		languageConfig.set(
				LANGUAGESTRING.BelieverWillLeaveReligionProphecyBibleText
						.name(), list);

		list = new ArrayList<String>();
		list.add("$PlayerName fullfilled a prophecy of $Type!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversProphecyFulfilled.name(), list);

		list = new ArrayList<String>();
		list.add("Rejoice! All the prophecies in $PlayerName has been fulfilled!");
		this.languageConfig.set(
				LANGUAGESTRING.GodToBelieversAllPropheciesFulfilled.name(),
				list);

		for (Material material : Material.values())
		{
			this.languageConfig
					.set("Items." + material.name(), material.name());
		}

		this.languageConfig.set("Items." + Material.APPLE.name(), "apple");
		this.languageConfig.set("Items." + Material.CAKE.name(), "cake");
		this.languageConfig.set("Items." + Material.BREAD.name(), "bread");
		this.languageConfig.set("Items." + Material.COOKIE.name(), "cookie");
		this.languageConfig.set("Items." + Material.MELON.name(), "melon");
		this.languageConfig.set("Items." + Material.GRILLED_PORK.name(),
				"bacon");
		this.languageConfig
				.set("Items." + Material.COOKED_BEEF.name(), "steak");
		this.languageConfig.set("Items." + Material.COOKED_FISH.name(), "fish");
		this.languageConfig.set("Items." + Material.COBBLESTONE.name(),
				"cobblestone");
		this.languageConfig.set("Items." + Material.STONE.name(), "stone");
		this.languageConfig.set("Items." + Material.BOOK.name(), "book");
		this.languageConfig.set("Items." + Material.DIAMOND.name(), "diamond");
		this.languageConfig.set("Items." + Material.DIRT.name(), "dirt");
		this.languageConfig.set("Items." + Material.GRASS.name(), "grass");
		this.languageConfig
				.set("Items." + Material.OBSIDIAN.name(), "obsidian");
		this.languageConfig.set("Items." + Material.GOLD_BLOCK.name(),
				"goldblock");
		this.languageConfig.set("Items." + Material.IRON_BLOCK.name(),
				"ironblock");
		this.languageConfig.set("Items." + Material.CARROT.name(), "carrot");

		this.languageConfig.set("Mobs." + EntityType.CHICKEN.name(), "chicken");
		this.languageConfig.set("Mobs." + EntityType.CREEPER.name(), "creeper");
		this.languageConfig.set("Mobs." + EntityType.SPIDER.name(), "spider");
		this.languageConfig.set("Mobs." + EntityType.ENDER_DRAGON.name(),
				"dragon");
		this.languageConfig.set("Mobs." + EntityType.ENDERMAN.name(),
				"enderman");
		this.languageConfig.set("Mobs." + EntityType.COW.name(), "cow");
		this.languageConfig.set("Mobs." + EntityType.BAT.name(), "bat");
		this.languageConfig.set("Mobs." + EntityType.SHEEP.name(), "sheep");
		this.languageConfig.set("Mobs." + EntityType.GHAST.name(), "ghast");
		this.languageConfig.set("Mobs." + EntityType.SILVERFISH.name(),
				"silverfish");
		this.languageConfig.set("Mobs." + EntityType.PIG.name(), "pig");
		this.languageConfig.set("Mobs." + EntityType.PIG_ZOMBIE.name(),
				"pig zombie");
		this.languageConfig.set("Mobs." + EntityType.SQUID.name(), "squid");
		this.languageConfig.set("Mobs." + EntityType.WOLF.name(), "wolf");
		this.languageConfig.set("Mobs." + EntityType.OCELOT.name(), "ocelot");
		this.languageConfig.set("Mobs." + EntityType.IRON_GOLEM.name(),
				"iron golem");
		this.languageConfig.set("Mobs." + EntityType.ZOMBIE.name(), "zombie");
		this.languageConfig.set("Mobs." + EntityType.MUSHROOM_COW.name(),
				"mooshroom");
		this.languageConfig.set("Mobs." + EntityType.BLAZE.name(), "blaze");
		this.languageConfig.set("Mobs." + EntityType.CAVE_SPIDER.name(),
				"cave spider");
		this.languageConfig.set("Mobs." + EntityType.SLIME.name(), "slime");
		this.languageConfig.set("Mobs." + EntityType.SKELETON.name(),
				"skeleton");
		this.languageConfig.set("Mobs." + EntityType.VILLAGER.name(),
				"villager");
		this.languageConfig.set("Mobs." + EntityType.UNKNOWN.name(), "unknown");

		this.languageConfig.set("GodTypes." + GodType.FROST.name(),
				"$Gender of Frost");
		this.languageConfig.set("GodTypes." + GodType.SUN.name(),
				"$Gender of the Sun");
		this.languageConfig.set("GodTypes." + GodType.MOON.name(),
				"$Gender of the Moon");
		this.languageConfig.set("GodTypes." + GodType.LOVE.name(),
				"$Gender of Love");
		this.languageConfig.set("GodTypes." + GodType.EVIL.name(),
				"$Gender of Evil");
		this.languageConfig.set("GodTypes." + GodType.THUNDER.name(),
				"$Gender of Thunder");
		this.languageConfig.set("GodTypes." + GodType.PARTY.name(),
				"$Gender of Party");
		this.languageConfig.set("GodTypes." + GodType.WAR.name(),
				"$Gender of War");
		this.languageConfig.set("GodTypes." + GodType.HARVEST.name(),
				"$Gender of Harvest");
		this.languageConfig.set("GodTypes." + GodType.JUSTICE.name(),
				"$Gender of Justice");
		this.languageConfig.set("GodTypes." + GodType.SEA.name(),
				"$Gender of the Sea");
		this.languageConfig.set("GodTypes." + GodType.WISDOM.name(),
				"$Gender of Wisdom");
		this.languageConfig.set("GodTypes." + GodType.WEREWOLVES.name(),
				"$Gender of Werewolves");
		this.languageConfig.set("GodTypes." + GodType.CREATURES.name(),
				"$Gender of Mobs");

		this.languageConfig.set("GodGender." + "None", "God");
		this.languageConfig.set("GodGender." + "Male", "God");
		this.languageConfig.set("GodGender." + "Female", "Goddess");

		this.languageConfig.set("GodMood." + GodMood.EXALTED.name(), "Exalted");
		this.languageConfig.set("GodMood." + GodMood.PLEASED.name(), "Pleased");
		this.languageConfig.set("GodMood." + GodMood.NEUTRAL.name(), "Neutral");
		this.languageConfig.set("GodMood." + GodMood.DISPLEASED.name(),
				"Displeased");
		this.languageConfig.set("GodMood." + GodMood.ANGRY.name(), "Angry");
*/
		return true;
	}

	LanguageManager(Gods p)
	{
		this.plugin = p;
	}

	public String getPriestAssignCommand(String playerName)
	{
		return "";
	}

	public String getPriestRemoveCommand(String playerName)
	{
		return "";
	}

	public String parseString(String id)
	{
		String string = id;

		if (string.contains("$ServerName"))
		{
			string = string
					.replace("$ServerName", ChatColor.GOLD
							+ this.plugin.serverName + ChatColor.WHITE
							+ ChatColor.BOLD);
		}

		if (string.contains("$PlayerName"))
		{
			string = string.replace("$PlayerName", ChatColor.GOLD
					+ this.playerName + ChatColor.WHITE + ChatColor.BOLD);
		}

		if (string.contains("$Amount"))
		{
			string = string.replace("$Amount",
					ChatColor.GOLD + String.valueOf(this.amount)
							+ ChatColor.WHITE + ChatColor.BOLD);
		}

		if (string.contains("$Type"))
		{
			string = string.replace("$Type", ChatColor.GOLD + this.type
					+ ChatColor.WHITE + ChatColor.BOLD);
		}

		return string;
	}

	public String parseStringForBook(String id)
	{
		String string = id;

		if (string.contains("$ServerName"))
		{
			string = string.replace("$ServerName", plugin.serverName);
		}

		if (string.contains("$PlayerName"))
		{
			string = string.replace("$PlayerName", playerName);
		}

		if (string.contains("$Amount"))
		{
			string = string.replace("$Amount", String.valueOf(amount));
		}

		if (string.contains("$Type"))
		{
			string = string.replace("$Type", type);
		}

		return string;
	}

	public String getPlayerName()
	{
		return this.playerName;
	}

	public void setPlayerName(String name)
	{
		if (name == null)
		{
			this.plugin.logDebug("WARNING: Setting null playername");
		}

		this.playerName = name;
	}

	public int getAmount()
	{
		return this.amount;
	}

	public void setAmount(int a)
	{
		this.amount = a;
	}

	public String getType()
	{
		return this.type;
	}

	public void setType(String t)
	{
		if (t == null)
		{
			this.plugin.logDebug("WARNING: Setting null type");
		}

		this.type = t;
	}

	public String getItemTypeName(Material material)
	{
		String itemTypeName = languageConfigs.get(generalLanguageFileName).getString("Items." + material.name());

		if (itemTypeName == null)
		{
			plugin.logDebug("WARNING: No language string in " + generalLanguageFileName + " for the item '" + material.name() + "'");
			return material.name();
		}

		return itemTypeName;
	}

	public String getMobTypeName(EntityType type)
	{
		String mobTypeName = languageConfigs.get(generalLanguageFileName).getString("Mobs." + type.name());
		
		if (mobTypeName == null)
		{
			plugin.logDebug("WARNING: No language string in " + generalLanguageFileName + " for the mob type '" + type.name() + "'");
			return type.name();
		}

		return mobTypeName;
	}

	public String getGodTypeName(GodType type, String gender)
	{
		String typeName = languageConfigs.get(generalLanguageFileName).getString("GodTypes." + type.name());

		if (typeName == null)
		{
			typeName = "$Gender of Nothing";
		}

		return typeName.replace("$Gender", gender);
	}

	public String getGodGenderName(GodGender gender)
	{
		return languageConfigs.get(generalLanguageFileName).getString("GodGender." + gender.name());
	}

	public String getGodMoodName(GodMood mood)
	{
		return languageConfigs.get(generalLanguageFileName).getString("GodMood." + mood.name());
	}

	public static enum LANGUAGESTRING
	{
		GodToBelieverNoQuestion, 
		GodToPriestPriestAccepted, 
		GodToPriestBlessedPlayerSet, 
		GodToPriestBlessedPlayerUnset, 
		GodToPriestCursedPlayerSet, 
		GodToPriestCursedPlayerUnset, 
		GodToPriestEatFoodType, 
		GodToPriestNotEatFoodType, 
		GodToPriestSlayMobType, 
		GodToPriestNotSlayMobType, 
		GodToPriestUseBible, 
		GodToPriestUseProphecies, 
		GodToBelieversBlessedPlayerSet, 
		GodToBelieversBlessedPlayerUnset, 
		GodToBelieversCursedPlayerSet, 
		GodToBelieversCursedPlayerUnset, 
		GodToBelieverOfferPriest, 
		GodToBelieverPraying, 
		GodToBelieversSlayQuestStarted, 
		GodToBelieversSlayQuestProgress, 
		GodToBelieversSlayQuestStatus, 
		GodToBelieversSlayQuestCompleted, 
		GodToBelieversSlayQuestFailed, 
		GodToBelieversConvertQuestStarted, 
		GodToBelieversConvertQuestProgress, 
		GodToBelieversConvertQuestStatus, 
		GodToBelieversConvertQuestCompleted, 
		GodToBelieversConvertQuestFailed, 
		GodToBelieversBuildAltarsQuestStarted, 
		GodToBelieversBuildAltarsQuestProgress, 
		GodToBelieversBuildAltarsQuestStatus, 
		GodToBelieversBuildAltarsQuestCompleted, 
		GodToBelieversBuildAltarsQuestFailed, 
		GodToBelieversBuildTowerQuestStarted, 
		GodToBelieversBuildTowerQuestProgress, 
		GodToBelieversBuildTowerQuestStatus, 
		GodToBelieversBuildTowerQuestCompleted, 
		GodToBelieversBuildTowerQuestFailed, 
		GodToBelieversSacrificeQuestStarted, 
		GodToBelieversSacrificeQuestProgress, 
		GodToBelieversSacrificeQuestStatus, 
		GodToBelieversSacrificeQuestCompleted, 
		GodToBelieversSacrificeQuestFailed, 
		GodToBelieversHolyFeastQuestStarted,
		GodToBelieversHolyFeastQuestProgress, 
		GodToBelieversHolyFeastQuestStatus, 
		GodToBelieversHolyFeastQuestCompleted, 
		GodToBelieversHolyFeastQuestFailed, 
		GodToBelieversHolyBattleQuestStarted, 
		GodToBelieversHolyBattleQuestProgress, 
		GodToBelieversHolyBattleQuestStatus, 
		GodToBelieversHolyBattleQuestCompleted, 
		GodToBelieversHolyBattleQuestFailed, 
		GodToBelieversGiveBiblesQuestStarted, 
		GodToBelieversGiveBiblesQuestProgress, 
		GodToBelieversGiveBiblesQuestStatus, 
		GodToBelieversGiveBiblesQuestCompleted, 
		GodToBelieversGiveBiblesQuestFailed, 
		GodToBelieversBurnBiblesQuestStarted, 
		GodToBelieversBurnBiblesQuestProgress, 
		GodToBelieversBurnBiblesQuestStatus, 
		GodToBelieversBurnBiblesQuestCompleted, 
		GodToBelieversBurnBiblesQuestFailed, 
		GodToBelieversCrusadeQuestStarted, 
		GodToBelieversCrusadeQuestProgress, 
		GodToBelieversCrusadeQuestStatus, 
		GodToBelieversCrusadeQuestCompleted, 
		GodToBelieversCrusadeQuestFailed, 
		GodToBelieversPilgrimageQuestStarted, 
		GodToBelieversPilgrimageQuestProgress, 
		GodToBelieversPilgrimageQuestStatus, 
		GodToBelieversPilgrimageQuestCompleted, 
		GodToBelieversPilgrimageQuestFailed, 
		GodToBelieversClaimHolyLandQuestStarted, 
		GodToBelieversClaimHolyLandQuestProgress, 
		GodToBelieversClaimHolyLandQuestStatus, 
		GodToBelieversClaimHolyLandQuestCompleted, 
		GodToBelieversClaimHolyLandQuestFailed, 
		GodToBelieversGetHolyArtifactThisQuestStarted, 
		GodToBelieversGetHolyArtifactOtherQuestStarted, 
		GodToBelieversGetHolyArtifactQuestProgress, 
		GodToBelieversGetHolyArtifactQuestHelp, 
		GodToBelieversGetHolyArtifactQuestRange, 
		GodToBelieversGetHolyArtifactThisQuestStatus, 
		GodToBelieversGetHolyArtifactOtherQuestStatus, 
		GodToBelieversGetHolyArtifactThisQuestCompleted, 
		GodToBelieversGetHolyArtifactOtherQuestCompleted, 
		GodToBelieversGetHolyArtifactThisQuestFailed, 
		GodToBelieversGetHolyArtifactOtherQuestFailed, 
		GodToBelieversRemovedPriest, 
		GodToBelieverPriestRejected, 
		GodToBelieverAltarBuilt, 
		GodToBelieversPlayerJoinedReligion, 
		GodToBelieversPlayerLeftReligion, 
		GodToBelieverRandomExaltedSpeech, 
		GodToBelieverRandomPleasedSpeech, 
		GodToBelieverRandomNeutralSpeech, 
		GodToBelieverRandomDispleasedSpeech, 
		GodToBelieverRandomAngrySpeech, 
		GodToBelieversLostBeliever, 
		GodToPlayerBlessed, 
		GodToPlayerCursed, 
		GodToPlayerInvite, 
		GodToPlayerAcceptedInvitation,
		GodToBelieverCursedAngry, 
		GodToBelieverGoodSacrifice, 
		GodToBelieverMehSacrifice, 
		GodToBelieverBadSacrifice, 
		GodToBelieverHolyFoodSacrifice, 
		GodToBelieversPlayerCursed, 
		GodToBelieversPlayerBlessed, 
		GodToBelieverItemBlessing, 
		GodToBelieverHealthBlessing, 
		GodToBelieverSmiteBlessing, 
		GodToBelieversEatFoodBlessing, 
		GodToBelieversNotEatFoodCursing, 
		GodToBelieversSlayMobBlessing, 
		GodToBelieversNotSlayMobCursing, 
		GodToBelieversPriestAccepted, 
		GodToBelieversAltarDestroyed, 
		GodToBelieversAltarDestroyedByPlayer, 
		GodToBelieversWar, 
		GodToBelieversAlliance, 
		GodToBelieversWarCancelled, 
		GodToBelieversAllianceCancelled, 
		GodToBelieversSetHome, 
		GodToBelieversSacrificeItemType, 
		GodToBelieversAllPropheciesFulfilled, 
		GodToBelieversProphecyFulfilled, 
		ProphecyHeaderBibleText, 
		UnholyMobWillBeSlainFutureProphecyBibleText, 
		UnholyMobWillBeSlainPastProphecyBibleText, 
		StormProphecyEffectPastBibleText, 
		StormProphecyEffectFutureBibleText, 
		HolyFoodRainProphecyEffectBibleText, 
		DarknessProphecyEffectBibleText, 
		HeathenWillBeSlainProphecyEffectBibleText, 
		BelieverWillLeaveReligionProphecyBibleText, 
		DragonBossWillBeSlainPastProphecyBibleText, 
		DragonBossWillBeSlainFutureProphecyBibleText, 
		DragonBossProphecyEffectPastBibleText, 
		DragonBossProphecyEffectFutureBibleText, 
		DefaultBibleText1, 
		DefaultBibleText2, 
		DefaultBibleText3, 
		DefaultBibleText4, 
		DefaultBibleText5, 
		DefaultBibleText6, 
		DefaultBibleText7;
	}
}