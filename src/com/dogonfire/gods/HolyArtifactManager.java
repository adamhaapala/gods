package com.dogonfire.gods;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class HolyArtifactManager 
{
	private Gods plugin;
	private FileConfiguration holyArtifactsConfig = null;
	private File holyArtifactsConfigFile = null;

	HolyArtifactManager(Gods plugin) 
	{
		this.plugin = plugin;
	}

	public void load() 
	{
		if (holyArtifactsConfigFile == null) 
		{
			holyArtifactsConfigFile = new File(this.plugin.getDataFolder(), "holyartifacts.yml");
		}

		holyArtifactsConfig = YamlConfiguration.loadConfiguration(this.holyArtifactsConfigFile);

		plugin.log("Loaded " + holyArtifactsConfig.getKeys(false).size() + " holy artifacts.");
	}

	public void save() 
	{
		if (holyArtifactsConfig == null || (holyArtifactsConfigFile == null)) 
		{
			return;
		}

		try 
		{
			holyArtifactsConfig.save(holyArtifactsConfigFile);
		} 
		catch (Exception ex) 
		{
			plugin.log("Could not save config to " + holyArtifactsConfigFile + ": " + ex.getMessage());
		}
	}

	public float handleDamage(String playerName, Entity targetEntity, ItemStack itemInHand, String godName) 
	{
		// BOSS: Can be damaged, but only slain by a holy artifact
		plugin.logDebug("Handle Holy Artifact damage"); 

		
		if (itemInHand.getAmount() == 0) 
		{
			return 1;
		}
					
		HolyArtifact item = new HolyArtifact(itemInHand, itemInHand.getType(), godName);

		// Make sure no-one slays a BOSS
		if(targetEntity.getType()==EntityType.GIANT || targetEntity.getType()==EntityType.ENDER_DRAGON)
		{
			if (!item.isHolyArtifact()) 
			{
				//if(targetEntity.)
				{
					return 0;
				}
			}
			else
			{
				return 1;
			}
		}

		// Dont do anything
		if(!item.isHolyArtifact())
		{
			return 1;
		}
		
		plugin.logDebug("Holy Artifact doing " + (1 + item.getKills() / 100.0f) + " damage"); 
		
		return (1 + item.getKills() / 100.0f);
	}

	public void handleDeath(String killerName, String godName, ItemStack itemInHand) 
	{
		if (itemInHand.getAmount() > 0) 
		{
			HolyArtifact item = new HolyArtifact(itemInHand, itemInHand.getType(), godName);

			if (item.isHolyArtifact()) 
			{
				int oldKills = item.getKills();

				int kills = item.getKills();
				kills++;

				item.setKills(kills);

				if (item.isNewItemRank(oldKills)) 
				{
					String name = item.generateName(itemInHand.getType(), godName);
					item.setName(item.getItemRankName(kills) + " " + name);
					plugin.getServer().broadcastMessage(ChatColor.AQUA + killerName + "'s " + ChatColor.WHITE + name + ChatColor.AQUA + " is now " + item.getItemRankName(kills));
				}
			}
		}
	}
}
