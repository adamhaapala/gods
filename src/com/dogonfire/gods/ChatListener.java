package com.dogonfire.gods;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener 
{
	private Gods plugin;
	private String chatTagReplaceString = "[GOD]";

	ChatListener(Gods p) 
	{
		this.plugin = p;
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) 
	{
		Player player = event.getPlayer();

		String godName = plugin.getBelieverManager().getGodForBeliever(player.getName());

		if (plugin.chatFormattingEnabled) 
		{			
			event.setFormat(plugin.getChatManager().formatChat(event.getPlayer(), godName, event.getMessage()));
			/*
			Player talkingPlayer = event.getPlayer();
		    String msg = event.getMessage();
		    String eventFormat = event.getFormat();
		   		    
		    int tagIndex = 0;

		    if ((!chatTagReplaceString.isEmpty()) && (eventFormat.contains(chatTagReplaceString)))
		    {
		      tagIndex = eventFormat.indexOf(chatTagReplaceString);
		      eventFormat = eventFormat.replace(chatTagReplaceString, "");
		    }
			
		    String msgFormat = "";
		    
		    if(godName==null)
		    {
		    	// Check for whitespace infront, that can be eaten
		    	
		    	if(eventFormat.charAt(tagIndex) == ' ')
		    	{
				    msgFormat = eventFormat.substring(0, tagIndex) + eventFormat.substring(tagIndex + 1 + chatTagReplaceString.length());		    		
		    	}		    	
		    }
		    else
		    {
			    msgFormat = eventFormat.substring(0, tagIndex) + ChatColor.GOLD + godName + eventFormat.substring(tagIndex+chatTagReplaceString.length());		    	
		    }
		    
		    event.setFormat(msgFormat);		
		    */
		}

		if (godName == null) 
		{
			return;
		}

		if (this.plugin.getBelieverManager().getReligionChat(player.getName())) 
		{
			event.setCancelled(true);

			for (Player otherPlayer : this.plugin.getServer().getOnlinePlayers()) 
			{
				String otherGod = this.plugin.getBelieverManager().getGodForBeliever(otherPlayer.getName());

				if ((otherGod != null) && (otherGod.equals(godName))) 
				{
					if (this.plugin.getGodManager().isPriest(player.getName())) 
					{
						otherPlayer.sendMessage(ChatColor.YELLOW + "[" + godName + "Chat] " + player.getName() + ": " + ChatColor.WHITE + event.getMessage());
					} 
					else 
					{
						otherPlayer.sendMessage(ChatColor.YELLOW + "[" + godName + "Chat] " + ChatColor.RED + player.getName() + ChatColor.YELLOW + ": " + ChatColor.WHITE + event.getMessage());
					}
				}
			}
		}
	}
}