package com.dogonfire.gods;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import com.dogonfire.gods.GodManager.GodGender;

public class AltarManager 
{
	private Gods plugin;
	private HashMap<Integer, String> droppedItems = new HashMap();

	AltarManager(Gods p) 
	{
		this.plugin = p;
	}

	public void update() 
	{
	}

	public String getCursedPlayerFromAltar(Block block, String[] lines) 
	{
		if ((block == null) || (block.getType() != Material.WALL_SIGN)) 
		{
			return null;
		}

		String cursesName = lines[0].trim();

		if (!cursesName.equalsIgnoreCase("curses")) 
		{
			return null;
		}

		String playerName = lines[2];

		if ((playerName == null) || (playerName.length() < 1)) 
		{
			return null;
		}

		return playerName;
	}

	public String getBlessedPlayerFromAltar(Block block, String[] lines) 
	{
		if ((block == null) || (block.getType() != Material.WALL_SIGN)) 
		{
			return null;
		}

		String cursesName = lines[0].trim();

		if (!cursesName.equalsIgnoreCase("blessings")) 
		{
			return null;
		}

		String playerName = lines[2];

		if ((playerName == null) || (playerName.length() < 1)) 
		{
			return null;
		}

		return playerName;
	}

	public GodGender getGodGenderFromAltarBlock(Block block) 
	{
		if (block.getRelative(BlockFace.UP).getType().equals(Material.REDSTONE_TORCH_ON)) 
		{
			return GodGender.Female;
		}

		return GodGender.Male;
	}

	public Block getAltarBlockFromSign(Block block) 
	{
		if ((block == null) || (block.getType() != Material.WALL_SIGN)) 
		{
			return null;
		}

		if ((block.getRelative(BlockFace.NORTH).getType() == this.plugin.altarBlockType) && (block.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType().equals(Material.TORCH) || block.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType().equals(Material.REDSTONE_TORCH_ON))) 
		{
			return block.getRelative(BlockFace.NORTH);
		}

		if ((block.getRelative(BlockFace.SOUTH).getType() == this.plugin.altarBlockType) && (block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType().equals(Material.TORCH) || block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType().equals(Material.REDSTONE_TORCH_ON))) 
		{
			return block.getRelative(BlockFace.SOUTH);
		}

		if ((block.getRelative(BlockFace.EAST).getType() == this.plugin.altarBlockType) && (block.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType().equals(Material.TORCH) || block.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType().equals(Material.REDSTONE_TORCH_ON))) 
		{
			return block.getRelative(BlockFace.EAST);
		}

		if ((block.getRelative(BlockFace.WEST).getType() == this.plugin.altarBlockType) && (block.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType().equals(Material.TORCH) || block.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType().equals(Material.REDSTONE_TORCH_ON))) 
		{
			return block.getRelative(BlockFace.WEST);
		}

		return null;
	}

	public boolean isAltarSign(Block block) 
	{
		if ((block == null) || (block.getType() != Material.WALL_SIGN)) 
		{
			return false;
		}
		
		if ((block.getRelative(BlockFace.NORTH).getType() == this.plugin.altarBlockType) && ((block.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType().equals(Material.TORCH) || block.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType().equals(Material.REDSTONE_TORCH_ON)))) 
		{
			return true;
		}

		if ((block.getRelative(BlockFace.SOUTH).getType() == this.plugin.altarBlockType) && ((block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType().equals(Material.TORCH) || block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType().equals(Material.REDSTONE_TORCH_ON)))) 
		{
			return true;
		}

		if ((block.getRelative(BlockFace.EAST).getType() == this.plugin.altarBlockType) && ((block.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType().equals(Material.TORCH) || block.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType().equals(Material.REDSTONE_TORCH_ON)))) 
		{
			return true;
		}

		if ((block.getRelative(BlockFace.WEST).getType() == this.plugin.altarBlockType) && ((block.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType().equals(Material.TORCH) || block.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType().equals(Material.REDSTONE_TORCH_ON)))) 
		{
			return true;
		}

		return false;
	}

	public boolean isAltarBlock(Block block) 
	{
		if ((block == null) || (block.getType() != this.plugin.altarBlockType)) 
		{
			return false;
		}

		if (block.getRelative(BlockFace.UP).getTypeId() != Material.TORCH.getId() && block.getRelative(BlockFace.UP).getTypeId() != Material.REDSTONE_TORCH_ON.getId()) 
		{
			return false;
		}

		if (block.getRelative(BlockFace.NORTH).getTypeId() == Material.WALL_SIGN.getId()) 
		{
			return true;
		}

		if (block.getRelative(BlockFace.SOUTH).getTypeId() == Material.WALL_SIGN.getId()) 
		{
			return true;
		}

		if (block.getRelative(BlockFace.WEST).getTypeId() == Material.WALL_SIGN.getId()) 
		{
			return true;
		}

		if (block.getRelative(BlockFace.EAST).getTypeId() == Material.WALL_SIGN.getId()) 
		{
			return true;
		}

		return false;
	}

	public boolean isPrayingAltar(Block block) 
	{
		return isAltarSign(block);
	}

	public boolean isCursingAltar(Block block, String[] lines) 
	{
		if (!isAltarSign(block)) 
		{
			return false;
		}

		return getCursedPlayerFromAltar(block, lines) != null;
	}

	public boolean isBlessingAltar(Block block, String[] lines) 
	{
		if (!isAltarSign(block)) 
		{
			return false;
		}

		return getBlessedPlayerFromAltar(block, lines) != null;
	}

	public boolean handleNewCursingAltar(SignChangeEvent event) 
	{
		Player player = event.getPlayer();

		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "gods.altar.build"))) 
		{
			this.plugin.sendInfo(player, ChatColor.RED + "You cannot build altars");
			return false;
		}

		event.setLine(0, "Curses");
		event.setLine(1, "On");

		event.setLine(3, "");

		plugin.sendInfo(player, "Right click the sign on the altar to curse that player!");

		return true;
	}

	public boolean handleNewBlessingAltar(SignChangeEvent event) {
		Player player = event.getPlayer();

		if ((!player.isOp())
				&& (!this.plugin.getPermissionsManager().hasPermission(player,
						"gods.altar.build"))) {
			return false;
		}

		event.setLine(0, "Blessings");
		event.setLine(1, "On");

		event.setLine(3, "");

		this.plugin.sendInfo(event.getPlayer(),
				"Right click the sign on the altar to bless that player!");

		return true;
	}

	public boolean handleNewSacrificingAltar(SignChangeEvent event) 
	{
		Player player = event.getPlayer();

		if ((!player.isOp())
				&& (!this.plugin.getPermissionsManager().hasPermission(player,
						"gods.altar.build"))) {
			this.plugin.sendInfo(player, ChatColor.RED
					+ "You cannot build altars");
			return false;
		}

		return false;
	}

	public boolean handleNewPrayingAltar(SignChangeEvent event) 
	{
		Player player = event.getPlayer();

		if (!player.isOp() && !plugin.getPermissionsManager().hasPermission(player, "gods.altar.build")) 
		{
			plugin.sendInfo(player, ChatColor.RED + "You cannot build altars");
			return false;
		}

		String godName = "";
		int line = 0;
		int otherline = 0;

		while ((godName.isEmpty()) && (line < 4)) 
		{
			godName = event.getLine(line++);
		}

		line--;

		while (otherline < 4) 
		{
			if (otherline == line) 
			{
				otherline++;
			} 
			else 
			{
				String text = event.getLine(otherline);

				if ((text != null) && (text.length() > 0)) 
				{
					plugin.sendInfo(event.getPlayer(), ChatColor.RED + "Only write the name of a God on the sign");
					return false;
				}

				otherline++;
			}
		}

		if (godName.length() <= 1) 
		{
			plugin.sendInfo(event.getPlayer(), ChatColor.RED + "That is not a proper name for a God!");
			return false;
		}

		godName = godName.trim();
		godName = plugin.getGodManager().formatGodName(godName);

		if ((godName.length() <= 1) || (godName.contains(" "))) 
		{
			plugin.sendInfo(event.getPlayer(), ChatColor.RED + "That is not a proper name for a God!");
			return false;
		}

		if ((plugin.isBlacklistedGod(godName)) || (!plugin.isWhitelistedGod(godName))) 
		{
			plugin.sendInfo(player, ChatColor.RED + "You cannot build altars to that God");
			return false;
		}

		if (!plugin.getGodManager().hasGodAccess(player.getName(), godName)) 
		{
			plugin.sendInfo(player, ChatColor.RED + "You cannot build altars to that God");
			return false;
		}

		Block altarBlock = plugin.getAltarManager().getAltarBlockFromSign(player.getWorld().getBlockAt(event.getBlock().getLocation()));

		if(altarBlock==null)
		{
			return false;
		}
		
		if(plugin.getGodManager().getGenderForGod(godName) == GodGender.None)
		{
			GodGender godGender = plugin.getAltarManager().getGodGenderFromAltarBlock(altarBlock);

			plugin.getGodManager().setGenderForGod(godName, godGender);

			plugin.logDebug("God did not have a gender, setting gender to " + godGender);
		}
				
		if (plugin.getGodManager().addAltar(event.getPlayer(), godName, event.getBlock().getLocation())) 
		{
			if ((this.plugin.holyLandEnabled) && (this.plugin.getPermissionsManager().hasPermission(player, "gods.holyland"))) 
			{				
				plugin.getLandManager().setPrayingHotspot(player.getName(), godName, altarBlock.getLocation());
			}
			
			plugin.getQuestManager().handleBuiltPrayingAltar(godName);

			event.setLine(0, "Altar");
			event.setLine(1, "of");
			event.setLine(2, godName);
			event.setLine(3, "");

			plugin.sendInfo(event.getPlayer(), "Right click the sign on the altar to pray to your god.");
		} 
		else 
		{
			return false;
		}

		return true;
	}

	public void addDroppedItem(int entityID, String playerName) 
	{
		this.droppedItems.put(Integer.valueOf(entityID), playerName);
	}

	public String getDroppedItemPlayer(int entityID) 
	{
		return (String) this.droppedItems.get(Integer.valueOf(entityID));
	}

	public void clearDroppedItems() 
	{
		this.plugin.logDebug("Cleared " + this.droppedItems.size() + " dropped items");
		this.droppedItems.clear();
	}
}