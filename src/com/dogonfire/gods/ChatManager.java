package com.dogonfire.gods;

import java.io.File;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ChatManager 
{
	private Gods plugin;
	private String chatFormat = "$Prefix<$GodName$Group$PlayerName>$Suffix: $Message";
	private FileConfiguration chatConfig = null;
	private File chatConfigFile = null;

	ChatManager(Gods p) 
	{
		this.plugin = p;
	}

	public void load() 
	{
		chatConfigFile = new File(this.plugin.getDataFolder(), "chatformat.yml");

		chatConfig = YamlConfiguration.loadConfiguration(this.chatConfigFile);

		this.plugin.log("Loaded " + this.chatConfig.getKeys(false).size() + " chat settings.");

		if (this.chatConfig.getKeys(false).size() == 0) 
		{
			this.chatConfig.set("format", this.chatFormat);

			this.chatConfig.set("groups.default.displayname", "");
			this.chatConfig.set("groups.default.prefix", "&1");
			this.chatConfig.set("groups.default.suffix", "&1");

			this.chatConfig.set("groups.admin.displayname", "ADMIN");
			this.chatConfig.set("groups.admin.prefix", "&4");
			this.chatConfig.set("groups.admin.suffix", "&1");

			save();
		}

		if (!this.chatFormat.contains("$Message")) 
		{
			this.plugin.log("Chat format does not contain $Message!!");
		}

		this.chatFormat = this.chatConfig.getString("format");
	}

	public void save() 
	{
		try 
		{
			this.chatConfig.save(this.chatConfigFile);
		} 
		catch (Exception ex) 
		{
			this.plugin.log("Could not save chat config to " + this.chatConfigFile + ": " + ex.getMessage());
		}
	}

	public String addColor(String string) 
	{
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public String formatChat(Player player, String godName, String message) 
	{
		String playerChatFormat = chatFormat;

		int index = playerChatFormat.indexOf("$Prefix");
		
		if (index > -1) 
		{
			String prefix = this.chatConfig.getString("groups." + this.plugin.getPermissionsManager().getGroup(player.getName()).toLowerCase() + ".prefix");

			if (prefix == null) 
			{
				prefix = "NOPREFIX";
			}

			playerChatFormat = playerChatFormat.replace("$Prefix", prefix);
		}

		index = playerChatFormat.indexOf("$World");
		if (index > -1) 
		{
			playerChatFormat = playerChatFormat.replace("$World", player.getWorld().getName());
		}

		index = playerChatFormat.indexOf("$PlayerName");
		if (index > -1) 
		{
			playerChatFormat = playerChatFormat.replace("$PlayerName", player.getDisplayName());
		}

		index = playerChatFormat.indexOf("$Group");
		if (index > -1) 
		{
			String displayName = chatConfig.getString("groups." + plugin.getPermissionsManager().getGroup(player.getName()).toLowerCase() + ".displayname");

			playerChatFormat = playerChatFormat.replace("$Group", displayName);
		}

		index = playerChatFormat.indexOf("$GodName");
		if (index > -1) 
		{
			String colorGodName = "";

			if (godName != null) 
			{
				colorGodName = "&6" + godName;
			}

			playerChatFormat = playerChatFormat.replace("$GodName", colorGodName);
		}

		index = playerChatFormat.indexOf("$Suffix");
		if (index > -1) 
		{
			String suffix = this.chatConfig.getString("groups." + this.plugin.getPermissionsManager().getGroup(player.getName()).toLowerCase() + ".suffix");

			if (suffix == null) 
			{
				suffix = "NOSUFFIX";
			}

			playerChatFormat = playerChatFormat.replace("$Suffix", suffix);
		}

		playerChatFormat = playerChatFormat.replace("$Message", message);

		playerChatFormat = addColor(playerChatFormat);

		playerChatFormat = playerChatFormat.replace("  ", " ");

		return playerChatFormat;
	}
}