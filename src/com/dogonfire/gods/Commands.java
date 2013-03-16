package com.dogonfire.gods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import com.dogonfire.gods.GodManager.GodType;

public class Commands 
{
	private Gods plugin = null;

	Commands(Gods p) 
	{
		this.plugin = p;
	}

	private boolean CommandList(CommandSender sender) 
	{
		if (sender != null && !sender.isOp() && !plugin.getPermissionsManager().hasPermission((Player) sender, "gods.list")) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		List<God> gods = new ArrayList<God>();
		String playerGod = null;

		Set<String> list = plugin.getGodManager().getTopGods();

		for (String godName : list) 
		{
			int power = (int) plugin.getGodManager().getGodPower(godName);

			int believers = plugin.getBelieverManager().getBelieversForGod(godName).size();

			if (believers > 0) 
			{
				gods.add(new God(godName, power, believers));
			}
		}

		if (gods.size() == 0) 
		{
			if (sender != null) 
			{
				sender.sendMessage(ChatColor.GOLD + "There are no Gods in " + plugin.serverName + "!");
			} 
			else 
			{
				plugin.log("There are no Gods in " + plugin.serverName + "!");
			}

			return true;
		}

		if (sender != null) 
		{
			playerGod = this.plugin.getBelieverManager().getGodForBeliever(sender.getName());
			sender.sendMessage(ChatColor.YELLOW + "--------- The Gods of " + plugin.serverName + " ---------");
		} 
		else 
		{
			this.plugin.log("--------- The Gods of " + plugin.serverName + " ---------");
		}

		Collections.sort(gods, new TopGodsComparator());

		int l = gods.size();

		List<God> topGods = gods;
		if (l > 15) 
		{
			topGods = topGods.subList(0, 15);
		}

		int n = 1;
		boolean playerGodShown = false;

		for (God god : topGods) 
		{
			String fullGodName = String.format("%-16s", god.name) + "   " + String.format("%-16s", plugin.getGodManager().getTitleForGod(god.name));
			
			if (sender != null) 
			{
				if ((playerGod != null) && (god.name.equals(playerGod))) 
				{
					playerGodShown = true;
					sender.sendMessage("" + ChatColor.GOLD
							//+ n
							+ String.format("%2d", n)
							+ " - "
//							+ StringUtils.rightPad(fullGodName, 30)
							+ fullGodName
							+ ChatColor.GOLD
							+ StringUtils.rightPad(new StringBuilder().append(" Power ").append(god.power).toString(), 2)
							+ StringUtils.rightPad(new StringBuilder().append(" Believers ").append(god.believers).toString(), 2));
				} 
				else 
				{
					sender.sendMessage("" + ChatColor.YELLOW
							+ String.format("%2d", n)
							+ ChatColor.AQUA
							+ " - "
							+ fullGodName
							+ ChatColor.GOLD
							+ StringUtils.rightPad(new StringBuilder().append(" Power ").append(god.power).toString(), 2)
							+ StringUtils.rightPad(new StringBuilder().append(" Believers ").append(god.believers).toString(), 2));
				}
			} 
			else 
			{
				this.plugin.log(String.format("%2d", n)
						+ " - "
						+ fullGodName
						+ ChatColor.GOLD
						+ StringUtils.rightPad(new StringBuilder().append(" Mood ").append(plugin.getGodManager().getExactMoodForGod(god.name)).toString(), 2)
						+ StringUtils.rightPad(new StringBuilder().append(" Power ").append(god.power).toString(), 2)
						+ StringUtils.rightPad(new StringBuilder().append(" Believers ").append(god.believers).toString(), 2));
			}

			n++;
		}

		n = 1;
		if ((playerGod != null) && (!playerGodShown)) 
		{
			for (God god : gods) 
			{
				String fullGodName = String.format("%-16s", god.name) + "   " + String.format("%-16s", plugin.getGodManager().getTitleForGod(god.name));

				if ((playerGod != null) && (god.name.equals(playerGod))) 
				{
					playerGodShown = true;
					sender.sendMessage("" + ChatColor.GOLD
							+ n
							+ " - "
							+ fullGodName
							+ StringUtils.rightPad(new StringBuilder().append(" Power ").append(god.power).toString(), 2)
							+ StringUtils.rightPad(new StringBuilder().append(" Believers ").append(god.believers).toString(), 2));
				}

				n++;
			}

		}

		return true;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		Player player = null;

		if ((sender instanceof Player)) 
		{
			player = (Player) sender;
		}

		if (player == null) 
		{
			if (cmd.getName().equalsIgnoreCase("gods") || (cmd.getName().equalsIgnoreCase("g"))) 
			{
				if ((args.length == 1) && (args[0].equalsIgnoreCase("reload"))) 
				{
					this.plugin.reloadSettings();
					this.plugin.loadSettings();
					this.plugin.getQuestManager().load();
					this.plugin.getGodManager().load();
					this.plugin.getBelieverManager().load();

					return true;
				}

				CommandList(null);
			}

			return true;
		}

		if (cmd.getName().equalsIgnoreCase("godaccept")) 
		{
			if (!player.isOp() && !plugin.getPermissionsManager().hasPermission(player, "gods.accept")) 
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission for that");
				return false;
			}
			
			plugin.getGodManager().believerAccept(sender.getName());
			
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("godreject")) 
		{
			if ((!player.isOp()) && !plugin.getPermissionsManager().hasPermission(player, "gods.reject")) 
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission for that");
				return false;
			}

			this.plugin.getGodManager().believerReject(sender.getName());
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("gods") || cmd.getName().equalsIgnoreCase("g")) 
		{
			if (args.length == 0) 
			{
				CommandGods(sender);
				plugin.log(sender.getName() + " /gods");
				return true;
			}
			if (args.length == 1) 
			{
				if (args[0].equalsIgnoreCase("reload")) 
				{
					if (CommandReload(sender)) 
					{
						plugin.log(sender.getName() + " /gods reload");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("info")) 
				{
					if (CommandInfo(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods info");
					}

					return true;
				}
				else if ((args[0].equalsIgnoreCase("c")) || (args[0].equalsIgnoreCase("chat"))) 
				{
					if (CommandChat(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods chat");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("list")) 
				{
					if (CommandList(sender)) 
					{
						plugin.log(sender.getName() + " /gods list");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("followers")) 
				{
					if (CommandFollowers(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods followers");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("sethome")) 
				{
					if (CommandSetHome(player, args)) 
					{
						plugin.log(sender.getName() + " /gods sethome");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("home")) 
				{
					if (CommandHome(player, args)) 
					{
						plugin.log(sender.getName() + " /gods home");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("bible")) 
				{
					if (CommandBible(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods bible");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("editbible")) 
				{
					if (CommandEditBible(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods editbible");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("setbible")) 
				{
					if (CommandSetBible(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods setbible");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("pvp")) 
				{
					if (CommandTogglePvP(player, args)) 
					{
						plugin.log(sender.getName() + " /gods pvp");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("hunt")) 
				{
					if (CommandHunt(player, args)) 
					{
						plugin.log(sender.getName() + " /gods hunt");
					}

					return true;
				}
				else if ((args[0].equalsIgnoreCase("open")) || (args[0].equalsIgnoreCase("close"))) 
				{
					CommandAccess(player, args);

					return true;
				}
				else if (args[0].equalsIgnoreCase("leave")) 
				{
					String godName = plugin.getBelieverManager().getGodForBeliever(sender.getName());

					if (plugin.getGodManager().believerLeaveGod(sender.getName())) 
					{
						sender.sendMessage(ChatColor.AQUA + "You left the religion of " + ChatColor.YELLOW + godName);
					} 
					else 
					{
						sender.sendMessage(ChatColor.RED + "You are not part of any religion!");
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("help")) 
				{
					if (CommandHelp(sender)) 
					{
						this.plugin.log(sender.getName() + " /gods help");
					}

					return true;
				}

				sender.sendMessage(ChatColor.RED + "Invalid Gods command");
				return true;
			}

			if (args.length == 2) 
			{
				if (args[0].equalsIgnoreCase("invite")) 
				{
					if (CommandInvite(player, args)) 
					{
						plugin.log(sender.getName() + " /gods invite " + args[1]);
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("war")) 
				{
					if (CommandWar(player, args)) 
					{
						plugin.log(sender.getName() + " /gods war " + args[1]);
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("ally")) 
				{
					if (CommandAlliance(player, args)) 
					{
						plugin.log(sender.getName() + " /gods ally " + args[1]);
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("info")) 
				{
					if (CommandInfo(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods info " + args[1]);
					}

					return true;
				}
				else if (args[0].equalsIgnoreCase("followers")) 
				{
					if (CommandFollowers(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods followers " + args[1]);
					}
					return true;
				}
				else if (args[0].equalsIgnoreCase("check")) 
				{
					if (CommandCheck(sender, args[1])) 
					{
						plugin.log(sender.getName() + " /gods check " + args[1]);
					}
					
					return true;
				}
				else if (args[0].equalsIgnoreCase("kick")) 
				{
					if (CommandKick(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods kick " + args[1]);
					}
					return true;
				}
				else if (args[0].equalsIgnoreCase("desc")) 
				{
					if (CommandSetDescription(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods desc " + args[1]);
					}
					return true;
				}
				else if (args[0].equalsIgnoreCase("setsafe")) 
				{
					if (CommandSetNeutralLand(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods setsafe " + args[1]);
					}
					return true;
				}
				if ((args[0].equalsIgnoreCase("help")) && (args[1].equalsIgnoreCase("altar"))) 
				{
					if (CommandHelpAltar(sender, args)) 
					{
						plugin.log(sender.getName() + " /gods help " + args[1]);
					}
					return true;
				}

				sender.sendMessage(ChatColor.RED + "Invalid Gods command");
				return true;
			}

			if (args.length == 3) 
			{
				if (args[0].equalsIgnoreCase("setpriest")) 
				{
					if (CommandSetPriest(player, args)) 
					{
						this.plugin.log(sender.getName() + " /gods setpriest " + args[1]);
					}

					return true;
				}
			}

			if (args[0].equalsIgnoreCase("desc")) 
			{
				if (CommandSetDescription(sender, args)) 
				{
					plugin.log(sender.getName() + " /gods desc " + args[1]);
				}

				return true;
			}

			sender.sendMessage(ChatColor.RED + "Too many arguments!");
			return true;
		}

		return true;
	}

	private boolean CommandInfo(CommandSender sender, String[] args) 
	{
		if ((sender != null) &&  (!sender.isOp()) &&  !plugin.getPermissionsManager().hasPermission((Player)sender, "gods.info")) 
		{
			sender.sendMessage(ChatColor.RED +  "You do not have permission for that");
			return false;
		}

		String godName = null;

		if (args.length == 2) 
		{
			godName = args[1];
		}

		if (godName == null) 
		{			
			godName = plugin.getBelieverManager().getGodForBeliever(sender.getName());

			if (godName == null) 
			{
				sender.sendMessage(ChatColor.RED + "You do not believe in any God.");
				return true;
			}
		}

		godName = plugin.getGodManager().formatGodName(godName);

		if (!plugin.getGodManager().godExist(godName)) 
		{
			sender.sendMessage(ChatColor.RED + "There is no God with such name.");
			return true;
		}

		/*
		String dateCreated = this.plugin.getGodManager().getCreatedDate(godName);

		if (dateCreated == null)
		{
			dateCreated = "Unknown";
		}
		*/

		List<String> priestNames = this.plugin.getGodManager().getPriestsForGod(godName);

		if ((priestNames == null) || (priestNames.size() == 0))
		{
			priestNames = new ArrayList<String>();
			priestNames.add("None");
		}
		
		sender.sendMessage(ChatColor.YELLOW + "--------- " + godName + " " + plugin.getGodManager().getColorForGod(godName) + plugin.getGodManager().getTitleForGod(godName) + ChatColor.YELLOW + " ---------");
/*
		if (!this.plugin.getGodManager().isPrivateAccess(godName))
		{
			sender.sendMessage(ChatColor.YELLOW + "--------- godName " + plugin.getGodManager().getGenderForGod(godName) + " of " + plugin.getGodManager().getDivineForceForGod(godName) + " ---------");
		}
		else
		{
			sender.sendMessage(ChatColor.YELLOW + "--------- The Sect of " + godName + " ---------");
		}
*/
		sender.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + plugin.getGodManager().getGodDescription(godName));

		//sender.sendMessage(ChatColor.AQUA + "Created: " + ChatColor.YELLOW + dateCreated);
		
		ChatColor moodColor = ChatColor.AQUA;
		GodManager.GodMood godMood = plugin.getGodManager().getMoodForGod(godName);
		
		switch(godMood)
		{
			case EXALTED 	: moodColor = ChatColor.GOLD; break;
			case PLEASED 	: moodColor = ChatColor.DARK_GREEN; break;
			case NEUTRAL 	: moodColor = ChatColor.WHITE; break;
			case DISPLEASED : moodColor = ChatColor.GRAY; break;
			case ANGRY 	 	: moodColor = ChatColor.DARK_RED; break;
		}
		
		sender.sendMessage("" + moodColor + godName + " is " + plugin.getLanguageManager().getGodMoodName(godMood));

		if (priestNames.size() == 1)
		{
			sender.sendMessage(ChatColor.AQUA + "Priest: " + ChatColor.YELLOW + (String)priestNames.get(0));
		}
		else
		{
			sender.sendMessage(ChatColor.AQUA + "Priests: ");

			for (String priestName : priestNames)
			{
				sender.sendMessage(ChatColor.YELLOW + " - " + priestName);
			}
		}

		sender.sendMessage(ChatColor.AQUA + "Believers: " + ChatColor.YELLOW + plugin.getBelieverManager().getBelieversForGod(godName).size());
		sender.sendMessage(ChatColor.AQUA + "Exact power: " + ChatColor.YELLOW + 
		plugin.getGodManager().getGodPower(godName));

		if (this.plugin.commandmentsEnabled) 
		{
			sender.sendMessage(ChatColor.AQUA + "Holy food: " + ChatColor.YELLOW + plugin.getLanguageManager().getItemTypeName(this.plugin.getGodManager().getEatFoodTypeForGod(godName)));      
			sender.sendMessage(ChatColor.AQUA + "Unholy food: " + ChatColor.YELLOW + plugin.getLanguageManager().getItemTypeName(plugin.getGodManager().getNotEatFoodTypeForGod(godName)));
   
			sender.sendMessage(ChatColor.AQUA + "Holy creature: " + ChatColor.YELLOW + plugin.getLanguageManager().getMobTypeName(plugin.getGodManager().getHolyMobTypeForGod(godName)));
			sender.sendMessage(ChatColor.AQUA + "Unholy creature: " + ChatColor.YELLOW + plugin.getLanguageManager().getMobTypeName(plugin.getGodManager().getUnholyMobTypeForGod(godName)));
		}

		List<String> allyRelations = plugin.getGodManager().getAllianceRelations(godName);
		List<String> warRelations = plugin.getGodManager().getWarRelations(godName);
		
		if(warRelations.size()>0 || allyRelations.size()>0)
		{
			sender.sendMessage(ChatColor.AQUA + "Religious relations: ");

			for (String ally : plugin.getGodManager().getAllianceRelations(godName))
			{
				sender.sendMessage(ChatColor.GREEN + " Alliance with " + ChatColor.GOLD + ally);
			}

			List<String> enemies = plugin.getGodManager().getWarRelations(godName);

			for(String enemy : enemies) 
			{
				sender.sendMessage(ChatColor.RED + " War with " + ChatColor.GOLD + enemy);
			}
		}

		return true;
	}

	private boolean CommandCheck(CommandSender sender, String believerName) 
	{
		if ((sender != null) && (!sender.isOp()) && (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.check"))) 
		{
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission for that");
			return false;
		}

		String godName = null;

		godName = this.plugin.getBelieverManager().getGodForBeliever(
				believerName);

		if (godName == null)
			sender.sendMessage(ChatColor.AQUA + believerName
					+ " does not believe in a god");
		else if (this.plugin.getGodManager().isPriest(believerName))
			sender.sendMessage(ChatColor.AQUA + believerName
					+ " is the Priest of " + ChatColor.YELLOW + godName);
		else {
			sender.sendMessage(ChatColor.AQUA + believerName + " believes in "
					+ ChatColor.YELLOW + godName);
		}

		return true;
	}

	private boolean CommandGods(CommandSender sender) 
	{
		sender.sendMessage(ChatColor.YELLOW + "------------------ " + plugin.getDescription().getFullName() + " ------------------");
		sender.sendMessage(ChatColor.AQUA + "By DogOnFire");
		sender.sendMessage("" + ChatColor.AQUA);
		sender.sendMessage(ChatColor.AQUA + "There are currently "+ ChatColor.WHITE + plugin.getGodManager().getGods().size() + ChatColor.AQUA + " Gods and");
		sender.sendMessage("" + ChatColor.WHITE + plugin.getBelieverManager().getBelievers().size() + ChatColor.AQUA + " believers in " + this.plugin.serverName);
		sender.sendMessage("" + ChatColor.AQUA);
		
		if(sender!=null)
		{
			String godName = plugin.getBelieverManager().getGodForBeliever(sender.getName());

			if(godName!=null)
			{
				sender.sendMessage(ChatColor.WHITE + "You believe in " + ChatColor.GOLD + godName);
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "You do not believe in any god");				
			}
			sender.sendMessage("" + ChatColor.AQUA);
		}
		
		sender.sendMessage("" + ChatColor.AQUA + "Use " + ChatColor.WHITE + "/gods help" + ChatColor.AQUA + " for a list of commands");
		sender.sendMessage(ChatColor.AQUA + "Use " + ChatColor.WHITE + "/gods help altar" + ChatColor.AQUA + " for info about how to build an altar");

		return true;
	}

	private boolean CommandHelpAltar(CommandSender sender, String[] args) 
	{
		String altarTypeName = this.plugin.getLanguageManager().getItemTypeName(this.plugin.altarBlockType);

		sender.sendMessage(ChatColor.YELLOW + "--------------- How to build an Altar ---------------");
		sender.sendMessage(ChatColor.AQUA + "Build an altar to your God by following these simple steps:");
		sender.sendMessage("" + ChatColor.AQUA);
		sender.sendMessage(ChatColor.WHITE + "  1  - Place a " + altarTypeName);
		sender.sendMessage(ChatColor.WHITE + "  2a - Place a torch on top for a male god");
		sender.sendMessage(ChatColor.WHITE + "  2b - Place a redstone torch on top for a female god");
		sender.sendMessage(ChatColor.WHITE + "  3  - Place a sign on the side of the " + altarTypeName);
		sender.sendMessage(ChatColor.WHITE + "  4  - Write the name of your God on the sign");
		sender.sendMessage("" + ChatColor.AQUA);
		sender.sendMessage(ChatColor.AQUA + "You can now pray to your God by right-clicking the sign!");

		return true;
	}

	private boolean CommandHelp(CommandSender sender) 
	{
		if ((sender != null) && (!sender.isOp()) && (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.help"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		sender.sendMessage(ChatColor.YELLOW + "------------------ " + this.plugin.getDescription().getFullName() + " ------------------");
		sender.sendMessage(ChatColor.AQUA + "/gods" + ChatColor.WHITE + " - Basic info");

		sender.sendMessage(ChatColor.AQUA + "/gods help altar" + ChatColor.WHITE + " - How to build an altar to a God");

		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.list")))
			sender.sendMessage(ChatColor.AQUA + "/gods list" + ChatColor.WHITE + " - List of all gods");
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.info")))
			sender.sendMessage(ChatColor.AQUA + "/gods info" + ChatColor.WHITE + " - Show info about your God");
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.info")))
			sender.sendMessage(ChatColor.AQUA + "/gods info <godname>" + ChatColor.WHITE + " - Show info about a specific God");
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.followers")))
			sender.sendMessage(ChatColor.AQUA + "/gods followers" + ChatColor.WHITE + " - Show the followers of your God");
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.followers")))
			sender.sendMessage(ChatColor.AQUA + "/gods followers <godname>" + ChatColor.WHITE + " - Show followers of a God");
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.check")))
			sender.sendMessage(ChatColor.AQUA + "/gods check <playername>" + ChatColor.WHITE + " - Show religion for a player");
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.chat")))
			sender.sendMessage(ChatColor.AQUA + "/gods chat" + ChatColor.WHITE + " - Chat only with believers within your religion");
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.home")))
			sender.sendMessage(ChatColor.AQUA + "/gods home" + ChatColor.WHITE + " - Teleports you to your religion home");
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.sethome")))
			sender.sendMessage(ChatColor.AQUA + "/gods sethome" + ChatColor.WHITE + " - Sets the home of your religion");
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.leave"))) 
		{
			sender.sendMessage(ChatColor.AQUA + "/gods leave" + ChatColor.WHITE + " - Leave your religion");
		}
		
		sender.sendMessage(ChatColor.AQUA + "/godaccept" + ChatColor.WHITE + " - Accept a proposal from your god");
		sender.sendMessage(ChatColor.AQUA + "/godreject" + ChatColor.WHITE + " - Reject a proposal from your god");

		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.reload"))) 
		{
			sender.sendMessage(ChatColor.AQUA + "/gods reload" + ChatColor.WHITE + " - Reload config for gods system");
		}
		if (this.plugin.getGodManager().isPriest(sender.getName())) 
		{
			if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.invite")))
			{
				sender.sendMessage(ChatColor.AQUA + "/gods invite <playername>" + ChatColor.WHITE + " - Invite a player to your religion");
			}
			if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.kick")))
			{
				sender.sendMessage(ChatColor.AQUA + "/gods kick <playername>" + ChatColor.WHITE + " - Kick a believer from your religion");
			}
			if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.bible")))
			{
				sender.sendMessage(ChatColor.AQUA + "/gods bible" + ChatColor.WHITE + " - Produces the Holy Book for your religion");
			}
			if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.editbible")))
			{
				sender.sendMessage(ChatColor.AQUA + "/gods editbible" + ChatColor.WHITE + " - Edits the Holy Book for your religion");
			}
			if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.setbible"))) 
			{
				sender.sendMessage(ChatColor.AQUA + "/gods setbible" + ChatColor.WHITE + " - Sets a book to be the Holy Book for your religion");
			}
			if ((sender.isOp()) || (plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.alliance")))
			{
				sender.sendMessage(ChatColor.AQUA + "/gods ally <godname>" + ChatColor.WHITE + " - Toggle alliance with another religion");
			}
			if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.war")))
			{
				sender.sendMessage(ChatColor.AQUA + "/gods war <godname>" + ChatColor.WHITE + " - Toggle war with another religion");
			}
	
			sender.sendMessage(ChatColor.AQUA + "/gods open" + ChatColor.WHITE + " - Set your religion as open to join for everyone");
			sender.sendMessage(ChatColor.AQUA + "/gods close" + ChatColor.WHITE + " - Set your religion as invite only");

			if (this.plugin.holyLandEnabled) 
			{
				sender.sendMessage(ChatColor.AQUA + "/gods desc <text>" + ChatColor.WHITE + " - Set the description for your religion");
				sender.sendMessage(ChatColor.AQUA + "/gods pvp" + ChatColor.WHITE + " - Toggle pvp for your religon");
			}
		}

		return true;
	}

	private boolean CommandReload(CommandSender sender) 
	{
		if ((!sender.isOp()) && (!plugin.getPermissionsManager().hasPermission((Player) sender, "gods.reload"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		this.plugin.loadSettings();

		this.plugin.getGodManager().load();
		this.plugin.getQuestManager().load();
		this.plugin.getBelieverManager().load();
		this.plugin.getWhitelistManager().load();

		sender.sendMessage(ChatColor.YELLOW + this.plugin.getDescription().getFullName() + ": " + ChatColor.WHITE + "Reloaded configuration.");
		plugin.log(sender.getName() + " /gods reload");

		return true;
	}

	private boolean CommandWar(Player player, String[] args) 
	{
		if ((!player.isOp()) && (!plugin.getPermissionsManager().hasPermission(player, "gods.priest.war"))) 
		{
			player.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		if (!this.plugin.getGodManager().isPriest(player.getName())) 
		{
			player.sendMessage(ChatColor.RED + "Only priests can declare religous wars");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(player.getName());
		String enemyGodName = plugin.getGodManager().formatGodName(args[1]);

		if (!this.plugin.getGodManager().godExist(args[1])) 
		{
			player.sendMessage(ChatColor.RED + "There is no God with the name " + ChatColor.GOLD + args[1]);
			return false;
		}

		List<String> alliances = this.plugin.getGodManager().getAllianceRelations(godName);

		if (alliances.contains(enemyGodName)) 
		{
			player.sendMessage(ChatColor.RED + "You are ALLIED with " + ChatColor.GOLD + args[1] + ChatColor.RED + "!");
			return false;
		}

		if (this.plugin.getGodManager().toggleWarRelationForGod(godName, enemyGodName)) 
		{
			this.plugin.getLanguageManager().setPlayerName(godName);
			this.plugin.getGodManager().godSayToBelievers(enemyGodName, LanguageManager.LANGUAGESTRING.GodToBelieversWar, 10);

			this.plugin.getLanguageManager().setPlayerName(enemyGodName);
			this.plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversWar, 10);
		} 
		else 
		{
			this.plugin.getLanguageManager().setPlayerName(godName);
			this.plugin.getGodManager().godSayToBelievers(enemyGodName,LanguageManager.LANGUAGESTRING.GodToBelieversWarCancelled, 10);

			this.plugin.getLanguageManager().setPlayerName(enemyGodName);
			this.plugin.getGodManager().godSayToBelievers(godName, LanguageManager.LANGUAGESTRING.GodToBelieversWarCancelled, 10);
		}

		return true;
	}

	private boolean CommandAlliance(Player player, String[] args) 
	{
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player,"gods.priest.alliance"))) 
		{
			player.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		if (!this.plugin.getGodManager().isPriest(player.getName())) 
		{
			player.sendMessage(ChatColor.RED + "Only priests can declare religous wars");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(player.getName());
		String allyGodName = plugin.getGodManager().formatGodName(args[1]);
		
		if (!this.plugin.getGodManager().godExist(args[1])) 
		{
			player.sendMessage(ChatColor.RED + "There is no God with the name " + ChatColor.GOLD + args[1]);
			return false;
		}

		List wars = this.plugin.getGodManager().getWarRelations(godName);

		if (wars.contains(allyGodName)) 
		{
			player.sendMessage(ChatColor.RED + "You are in WAR with " + ChatColor.GOLD + args[1] + ChatColor.RED + "!");
			return false;
		}

		if (this.plugin.getGodManager().toggleAllianceRelationForGod(godName, allyGodName)) 
		{
			this.plugin.getLanguageManager().setPlayerName(godName);
			this.plugin.getGodManager().godSayToBelievers(allyGodName,
					LanguageManager.LANGUAGESTRING.GodToBelieversAlliance, 10);

			this.plugin.getLanguageManager().setPlayerName(allyGodName);
			this.plugin.getGodManager().godSayToBelievers(godName,
					LanguageManager.LANGUAGESTRING.GodToBelieversAlliance, 10);
		} else {
			this.plugin.getLanguageManager().setPlayerName(godName);
			this.plugin
					.getGodManager()
					.godSayToBelievers(
							allyGodName,
							LanguageManager.LANGUAGESTRING.GodToBelieversAllianceCancelled,
							10);

			this.plugin.getLanguageManager().setPlayerName(allyGodName);
			this.plugin
					.getGodManager()
					.godSayToBelievers(
							godName,
							LanguageManager.LANGUAGESTRING.GodToBelieversAllianceCancelled,
							10);
		}

		return true;
	}

	private boolean CommandAccess(Player player, String[] args) 
	{
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "gods.priest.access"))) 
		{
			player.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		if (!this.plugin.getGodManager().isPriest(player.getName())) 
		{
			player.sendMessage(ChatColor.RED + "Only priests can set religion access");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(player.getName());

		if (!this.plugin.getGodManager().godExist(godName)) 
		{
			player.sendMessage(ChatColor.RED + "That God does not exist");
			return false;
		}

		String access = args[0];

		if (access.equalsIgnoreCase("open")) 
		{
			this.plugin.getGodManager().setPrivateAccess(godName, false);
			this.plugin.log(player.getName() + " /gods open");
			player.sendMessage(ChatColor.AQUA + "You set the religion access to " + ChatColor.YELLOW + "open" + ChatColor.AQUA + ".");
			player.sendMessage(ChatColor.AQUA + "Players can join religion by praying at altars.");
		} 
		else if (access.equalsIgnoreCase("close")) 
		{
			this.plugin.getGodManager().setPrivateAccess(godName, true);
			this.plugin.log(player.getName() + " /gods close");
			player.sendMessage(ChatColor.AQUA + "You set the religion access to " + ChatColor.RED + "closed" + ChatColor.AQUA + ".");
			player.sendMessage(ChatColor.AQUA + "Players can now only pray to this religion by invitation.");
		} 
		else 
		{
			player.sendMessage(ChatColor.RED + "That is not a valid command");
			return false;
		}

		return true;
	}

	private boolean CommandInvite(Player player, String[] args) 
	{
		if ((!player.isOp()) && (!plugin.getPermissionsManager().hasPermission(player, "gods.priest.invite"))) 
		{
			player.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		if (!plugin.getGodManager().isPriest(player.getName())) 
		{
			player.sendMessage(ChatColor.RED + "Only priests can invite players");
			return false;
		}

		String godName = plugin.getBelieverManager().getGodForBeliever(player.getName());

		if (!plugin.getGodManager().godExist(godName)) 
		{
			player.sendMessage(ChatColor.RED + "That God does not exist");
			return false;
		}

		String playerName = args[1];
		Player invitedPlayer = plugin.getServer().getPlayer(playerName);
		
		if(invitedPlayer==null)
		{			
			player.sendMessage(ChatColor.RED + "There is no player with the name '" + ChatColor.YELLOW + playerName + ChatColor.RED + " online.");
			return false;
		}

		
		String invitedPlayerGod = plugin.getBelieverManager().getGodForBeliever(playerName);
		if(invitedPlayerGod!=null && invitedPlayerGod.equals(godName))
		{			
			player.sendMessage(ChatColor.YELLOW + playerName + ChatColor.RED + " already believes in '" + ChatColor.GOLD + godName + ChatColor.RED + "!");
			return false;
		}
				
		plugin.getBelieverManager().setInvitation(invitedPlayer.getName(), godName);
				
		plugin.log(godName + " invited to " + invitedPlayer.getName() + " to join the religion");
		plugin.getLanguageManager().setPlayerName(invitedPlayer.getName());

		plugin.getGodManager().GodSay(godName, invitedPlayer, LanguageManager.LANGUAGESTRING.GodToPlayerInvite, 1);
		invitedPlayer.sendMessage(ChatColor.AQUA + "Type " + ChatColor.WHITE + "/godaccept" + ChatColor.AQUA + " or " + ChatColor.WHITE + "/godreject" + ChatColor.AQUA + " now.");
				
		player.sendMessage(ChatColor.AQUA + "You invited " + ChatColor.YELLOW + playerName + ChatColor.AQUA + " to join " + ChatColor.GOLD + godName + ChatColor.AQUA + "!");

		return true;
	}
	
	private boolean CommandSetDescription(CommandSender sender, String[] args) 
	{
		if ((!sender.isOp()) && (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.description"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		if (!this.plugin.getGodManager().isPriest(sender.getName())) 
		{
			sender.sendMessage(ChatColor.RED + "Only priests can set religion info");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(sender.getName());

		String description = "";

		for (String arg : args) 
		{
			if (!arg.equals(args[0])) 
			{
				description = description + " " + arg;
			}
		}

		this.plugin.getGodManager().setGodDescription(godName, description);

		sender.sendMessage(ChatColor.AQUA + "You set your religion description to " + ChatColor.YELLOW + plugin.getGodManager().getGodDescription(godName));

		return true;
	}

	private boolean CommandSetDivineForce(CommandSender sender, String[] args) 
	{
		if ((!sender.isOp()) && (!plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.setforce"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		if (!this.plugin.getGodManager().isPriest(sender.getName())) 
		{
			sender.sendMessage(ChatColor.RED + "Only priests can set the divine force");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(sender.getName());

		String divineForce = "";

		for (String arg : args) 
		{
			if (!arg.equals(args[0])) 
			{
				arg = arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
				divineForce = divineForce + " " + arg;
			}
		}

		plugin.getGodManager().setDivineForceForGod(godName, GodType.valueOf(divineForce));

		sender.sendMessage(ChatColor.AQUA + "You set your religion's divine force to be " + ChatColor.YELLOW + plugin.getGodManager().getDivineForceForGod(godName));

		return true;
	}

	private boolean CommandKick(CommandSender sender, String[] args) {
		if ((!sender.isOp())
				&& (!this.plugin.getPermissionsManager().hasPermission(
						(Player) sender, "gods.priest.kick"))) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission for that");
			return false;
		}

		if (!this.plugin.getGodManager().isPriest(sender.getName())) {
			sender.sendMessage(ChatColor.RED
					+ "Only priests can kick believers from a religion");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(
				sender.getName());

		String believerName = args[1];
		Player believer = this.plugin.getServer().getPlayer(believerName);

		String believerGodName = this.plugin.getBelieverManager()
				.getGodForBeliever(believerName);

		if ((believerGodName == null) || (!believerGodName.equals(godName))) {
			sender.sendMessage(ChatColor.RED
					+ "There is no such believer called '" + believerName
					+ "' in your religion");
			return false;
		}

		if (believerGodName.equalsIgnoreCase(sender.getName())) {
			sender.sendMessage(ChatColor.RED
					+ "You cannot kick yourself from your own religion, Bozo!");
			return false;
		}

		this.plugin.getBelieverManager().removeBeliever(godName, believerName);

		sender.sendMessage(ChatColor.AQUA + "You kicked " + ChatColor.YELLOW
				+ believerName + ChatColor.AQUA + " from your religion!");

		if (believer != null) {
			believer.sendMessage(ChatColor.RED
					+ "You were kicked from the religion of "
					+ ChatColor.YELLOW + godName + ChatColor.AQUA + "!");
		}

		this.plugin.log(sender.getName() + " /gods kick " + believerName);

		return true;
	}

	private boolean CommandTogglePvP(CommandSender sender, String[] args) {
		if ((!sender.isOp())
				&& (!this.plugin.getPermissionsManager().hasPermission(
						(Player) sender, "gods.priest.pvp"))) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission for that");
			return false;
		}

		if (!this.plugin.getGodManager().isPriest(sender.getName())) {
			sender.sendMessage(ChatColor.RED
					+ "Only priests can toggle pvp for a religion");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(
				sender.getName());

		boolean pvp = this.plugin.getGodManager().getGodPvP(godName);

		if (pvp) 
		{
			sender.sendMessage(ChatColor.AQUA
					+ "You set PvP for your religion to " + ChatColor.YELLOW
					+ " disabled");
			plugin.getGodManager().setGodPvP(godName, false);
		} 
		else 
		{
			sender.sendMessage(ChatColor.AQUA
					+ "You set PvP for your religion to " + ChatColor.YELLOW
					+ " enabled");
			plugin.getGodManager().setGodPvP(godName, true);
		}

		return true;
	}

	private boolean CommandSetHome(CommandSender sender, String[] args) 
	{
		if ((!sender.isOp()) && (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.sethome"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(sender.getName());

		if (godName == null) 
		{
			sender.sendMessage(ChatColor.RED + "You do not believe in a God");
			return false;
		}

		Player player = (Player) sender;

		if ((this.plugin.onlyPriestCanSetHome)
				&& (!this.plugin.getGodManager().isPriest(player.getName()))) {
			sender.sendMessage(ChatColor.RED
					+ "Only your priest can set the home for your religion");
			return false;
		}

		if (this.plugin.holyLandEnabled) {
			if (this.plugin.getLandManager().isNeutralLandLocation(
					player.getLocation())) {
				sender.sendMessage(ChatColor.RED
						+ "You can only set religion home within your Holy Land");
				return false;
			}

			String locationGod = this.plugin.getLandManager()
					.getGodAtHolyLandLocation(player.getLocation());

			if ((locationGod == null) || (!locationGod.equals(godName))) {
				sender.sendMessage(ChatColor.RED
						+ "You can only set religion home within your Holy Land");
				return false;
			}
		}

		this.plugin.getGodManager()
				.setHomeForGod(godName, player.getLocation());

		this.plugin.getLanguageManager().setPlayerName(player.getName());
		this.plugin.getGodManager().godSayToBelievers(godName,
				LanguageManager.LANGUAGESTRING.GodToBelieversSetHome, 2);

		return true;
	}

	private boolean CommandHome(CommandSender sender, String[] args) 
	{
		if ((!sender.isOp())
				&& (!this.plugin.getPermissionsManager().hasPermission(
						(Player) sender, "gods.home"))) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permission for that");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(
				sender.getName());

		if (godName == null) {
			sender.sendMessage(ChatColor.RED + "You do not believe in a God");
			return false;
		}

		Location location = this.plugin.getGodManager().getHomeForGod(godName);

		if (location == null) {
			return false;
		}

		Player player = (Player) sender;
		player.teleport(location);

		return true;
	}

	private boolean CommandChat(CommandSender sender, String[] args) 
	{
		if ((!sender.isOp()) && (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.chat"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(sender.getName());

		if (godName == null) 
		{
			sender.sendMessage(ChatColor.RED + "You do not believe in a God");
			return false;
		}

		if (plugin.getBelieverManager().getReligionChat(sender.getName())) 
		{
			plugin.getBelieverManager().setReligionChat(sender.getName(), false);
			sender.sendMessage(ChatColor.AQUA + "You are now chatting public");
		} 
		else 
		{
			plugin.getBelieverManager().setReligionChat(sender.getName(), true);
			sender.sendMessage(ChatColor.AQUA + "You are now only chatting with the believers of " + ChatColor.YELLOW + godName);
		}

		return true;
	}

	private boolean CommandHunt(CommandSender sender, String[] args) 
	{
		if ((!sender.isOp()) && (!plugin.getPermissionsManager().hasPermission((Player) sender, "gods.hunt"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		String godName = plugin.getBelieverManager().getGodForBeliever(sender.getName());

		if (godName == null) 
		{
			sender.sendMessage(ChatColor.RED + "You do not believe in a God");
			return false;
		}

		if (plugin.getBelieverManager().isHunting(sender.getName())) 
		{
			plugin.getBelieverManager().setHunting(sender.getName(), false);
			sender.sendMessage(ChatColor.AQUA + "You are no longer hunting");
		} 
		else 
		{
			plugin.getBelieverManager().setHunting(sender.getName(), true);
			sender.sendMessage(ChatColor.AQUA + "You are now hunting");
		}

		return true;
	}

	private boolean CommandSetNeutralLand(CommandSender sender, String[] args) 
	{
		if (!this.plugin.holyLandEnabled) 
		{
			sender.sendMessage(ChatColor.RED + "Holy Land is not enabled on this server");
			return false;
		}

		if ((!sender.isOp()) && (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.setsafe"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		int radius = 0;
		try 
		{
			radius = Integer.valueOf(args[1]).intValue();
		} 
		catch (Exception ex) 
		{
			radius = 0;
		}

		if (radius == 0) 
		{
			sender.sendMessage(ChatColor.RED + "Invalid radius value");
			return false;
		}

		Player player = (Player) sender;

		plugin.getLandManager().setNeutralLandHotspot(player.getLocation(), radius);

		sender.sendMessage(ChatColor.AQUA + "You set neutral lands around this location in a radius of " + ChatColor.WHITE + radius);

		return true;
	}

	private boolean CommandSetPriest(CommandSender sender, String[] args) 
	{
		if (!sender.isOp() && (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.setpriest"))) 
		{ 
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		String godName = args[1];

		if (!plugin.getGodManager().godExist(godName)) 
		{
			sender.sendMessage(ChatColor.RED + "There is no god called '" + ChatColor.GOLD + godName + ChatColor.AQUA + "'");
			return false;
		}

		Player player = plugin.getServer().getPlayer(args[2]);

		if (player == null) 
		{
			sender.sendMessage(ChatColor.RED + "There is no such player online");
			return false;
		}

		plugin.getBelieverManager().addPrayer(player.getName(), godName);
		plugin.getGodManager().assignPriest(godName, player.getName());

		sender.sendMessage(ChatColor.AQUA + "You set " + ChatColor.GOLD + player.getName() + ChatColor.AQUA + " as priest of " + ChatColor.GOLD + godName);

		return true;
	}

	private boolean CommandFollowers(CommandSender sender, String[] args) 
	{
		if ((sender != null) && (!sender.isOp()) && (!plugin.getPermissionsManager().hasPermission((Player) sender, "gods.followers"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		List<Believer> believers = new ArrayList();
		String playerGod = null;

		String godName = "";

		if (args.length >= 2) 
		{
			godName = args[1];
			godName = this.plugin.getGodManager().formatGodName(godName);
		} 
		else 
		{
			godName = this.plugin.getBelieverManager().getGodForBeliever(sender.getName());
		}

		if (godName == null) 
		{
			sender.sendMessage(ChatColor.RED + "You do not believe in a God");
			return false;
		}

		Set<String> list = this.plugin.getBelieverManager().getBelieversForGod(godName);

		for (String believerName : list) 
		{
			int power = (int) this.plugin.getGodManager().getGodPower(godName);
			Date lastPrayer = this.plugin.getBelieverManager().getLastPrayerTime(believerName);

			believers.add(new Believer(believerName, lastPrayer));
		}

		if (believers.size() == 0) 
		{
			if (sender != null) 
			{
				sender.sendMessage(ChatColor.GOLD + godName + ChatColor.AQUA + " has no believers!");
			} 
			else 
			{
				plugin.log("There are no Gods in " + this.plugin.serverName + "!");
			}

			return true;
		}

		if (sender != null) 
		{
			playerGod = this.plugin.getBelieverManager().getGodForBeliever(sender.getName());
			sender.sendMessage(ChatColor.YELLOW + "--------- The Followers of " + godName + " ---------");
		} 
		else 
		{
			this.plugin.log("--------- The Followers of " + godName + " ---------");
		}

		Collections.sort(believers, new BelieversComparator());

		int l = believers.size();

		List<Believer> believersList = believers;
		if (l > 15) 
		{
			believersList = believersList.subList(0, 15);
		}

		int n = 1;
		boolean playerShown = false;
		Player player = (Player) sender;

		Date thisDate = new Date();

		for (Believer believer : believersList) 
		{
			long minutes = (thisDate.getTime() - believer.lastPrayer.getTime()) / 60000L;
			long hours = (thisDate.getTime() - believer.lastPrayer.getTime()) / 3600000L;
			long days = (thisDate.getTime() - believer.lastPrayer.getTime()) / 86400000L;

			String date = "";

			if (days > 0L) 
			{
				date = days + " days ago";
			} 
			else if (hours > 0L) 
			{
				date = hours + " hours ago";
			} 
			else 
			{
				date = minutes + " min ago";
			}

			if (sender != null) 
			{
				if ((playerGod != null) && (believer.name.equals(player.getName()))) 
				{
					playerShown = true;
					sender.sendMessage(ChatColor.GOLD
							+ StringUtils.rightPad(believer.name, 20)
							+ ChatColor.AQUA
							+ StringUtils.rightPad(
									new StringBuilder().append(" Prayed ")
											.append(ChatColor.GOLD)
											.append(date).toString(), 18));
				} 
				else 
				{
					sender.sendMessage(ChatColor.YELLOW
							+ StringUtils.rightPad(believer.name, 20)
							+ ChatColor.AQUA
							+ StringUtils.rightPad(
									new StringBuilder().append(" Prayed ")
											.append(ChatColor.GOLD)
											.append(date).toString(), 18));
				}
			} 
			else 
			{
				plugin.log(StringUtils.rightPad(believer.name, 20) + ChatColor.AQUA + StringUtils.rightPad(new StringBuilder().append(" Prayed ").append(ChatColor.GOLD).append(date).toString(), 18));
			}

			n++;
		}

		n = 1;
		if ((playerGod != null) && (!playerShown)) 
		{
			for (Believer believer : believers) 
			{
				if ((playerGod != null) && (believer.name.equals(player.getName()))) 
				{
					sender.sendMessage(ChatColor.GOLD + StringUtils.rightPad(believer.name, 20) + StringUtils.rightPad( new StringBuilder().append(" Prayed ").append(believer.lastPrayer).toString(), 18));
				}

				n++;
			}

		}

		return true;
	}

	private boolean CommandBible(CommandSender sender, String[] args) 
	{
		if (!this.plugin.biblesEnabled) 
		{
			sender.sendMessage(ChatColor.RED + "Bibles are not enabled on this server");
			return false;
		}

		if (!sender.isOp() && (!plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.bible"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(sender.getName());

		if (godName == null) 
		{
			sender.sendMessage(ChatColor.RED + "You do not believe in a God");
			return false;
		}

		Player player = (Player) sender;

		if (!this.plugin.getGodManager().isPriest(player.getName())) 
		{
			sender.sendMessage(ChatColor.RED + "Only your priest can produce the Holy Book");
			return false;
		}

		if (!plugin.getBibleManager().giveBible(godName, player.getName())) 
		{
			sender.sendMessage(ChatColor.RED + "Could not produce a Holy Book for " + godName);
			return false;
		}

		sender.sendMessage(ChatColor.AQUA + "You produced a copy of " + ChatColor.GOLD + plugin.getBibleManager().getBibleTitle(godName) + ChatColor.AQUA + "!");

		return true;
	}

	private boolean CommandEditBible(CommandSender sender, String[] args) 
	{
		if (!this.plugin.biblesEnabled) 
		{
			sender.sendMessage(ChatColor.RED + "Bibles are not enabled on this server");
			return false;
		}

		if ((!sender.isOp()) && (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.editbible"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(sender.getName());

		if (godName == null) 
		{
			sender.sendMessage(ChatColor.RED + "You do not believe in a God");
			return false;
		}

		Player player = (Player) sender;

		if (!this.plugin.getGodManager().isPriest(player.getName())) 
		{
			sender.sendMessage(ChatColor.RED + "Only your priest can edit your Holy Book");
			return false;
		}

		if (!this.plugin.getBibleManager().giveEditBible(godName, player.getName())) 
		{
			sender.sendMessage(ChatColor.RED + "Could not produce a editable bible for " + godName);
			return false;
		}

		sender.sendMessage(ChatColor.AQUA + "You produced a copy of " + ChatColor.GOLD + this.plugin.getBibleManager().getBibleTitle(godName) + ChatColor.AQUA + "!");

		sender.sendMessage(ChatColor.AQUA + "After you have edited it, set this as your bible with " + ChatColor.WHITE + "/g setbible" + ChatColor.AQUA + "!");

		return true;
	}

	private boolean CommandSetBible(CommandSender sender, String[] args) 
	{
		if (!this.plugin.biblesEnabled) 
		{
			sender.sendMessage(ChatColor.RED + "Bibles are not enabled on this server");
			return false;
		}

		if ((!sender.isOp()) && (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "gods.priest.setbible"))) 
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(sender.getName());

		if (godName == null) 
		{
			sender.sendMessage(ChatColor.RED + "You do not believe in a God");
			return false;
		}

		Player player = (Player) sender;

		if (!this.plugin.getGodManager().isPriestForGod(player.getName(),
				godName)) {
			sender.sendMessage(ChatColor.RED
					+ "Only your priest can set the Bible");
			return false;
		}

		if (!this.plugin.getBibleManager().setBible(godName, player.getName())) {
			sender.sendMessage(ChatColor.RED
					+ "You cannot use that as the Bible for " + ChatColor.GOLD
					+ godName);
			return false;
		}

		sender.sendMessage(ChatColor.AQUA + "You set " + ChatColor.GOLD
				+ this.plugin.getBibleManager().getBibleTitle(godName)
				+ ChatColor.AQUA + " as your holy scripture!");

		return true;
	}

	public class Believer 
	{
		public String name;
		public Date lastPrayer;

		Believer(String name, Date lastPrayer) 
		{
			this.name = name;
			this.lastPrayer = lastPrayer;
		}
	}

	public class BelieversComparator implements Comparator 
	{
		public BelieversComparator() 
		{
		}

		public int compare(Object object1, Object object2) 
		{
			Commands.Believer b1 = (Commands.Believer) object1;
			Commands.Believer b2 = (Commands.Believer) object2;

			return (int) (b2.lastPrayer.getTime() - b1.lastPrayer.getTime());
		}
	}

	public class God 
	{
		public int power;
		public String name;
		public int believers;

		God(String godName, int godPower, int godbelievers) 
		{
			power = godPower;
			name = new String(godName);
			believers = godbelievers;
		}
	}

	public class TopGodsComparator implements Comparator 
	{
		public TopGodsComparator() 
		{
		}

		public int compare(Object object1, Object object2) 
		{
			Commands.God g1 = (Commands.God) object1;
			Commands.God g2 = (Commands.God) object2;

			return g2.power - g1.power;
		}
	}
}