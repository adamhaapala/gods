package com.dogonfire.gods.tasks;

import com.dogonfire.gods.Gods;
import com.dogonfire.gods.LanguageManager;
import java.util.Random;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GiveItemTask implements Runnable
{
	private Gods		plugin;
	private Player		player	= null;
	private Material	itemType;
	private String		godName	= null;
	private boolean		speak	= false;

	public GiveItemTask(Gods instance, String god, Player p, Material material, boolean godspeak)
	{
		this.plugin = instance;
		this.player = p;
		this.godName = new String(god);
		this.itemType = material;
		this.speak = godspeak;
	}

	private boolean giveItem()
	{
		Vector dir = this.player.getLocation().getDirection();

		dir.setY(0);

		Location spawnLocation = this.player.getLocation().toVector().add(dir.multiply(4)).toLocation(this.player.getWorld());

		spawnLocation.setY(spawnLocation.getY() + 2.0D);

		if (spawnLocation.getBlock().getType() != Material.AIR)
		{
			//this.plugin.logDebug("Could not giveItem(): Not air infront of " + this.player.getName());
			return false;
		}

		try
		{
			spawnLocation.getWorld().dropItem(spawnLocation,
					new ItemStack(this.itemType, 1));

			spawnLocation.getWorld().playEffect(spawnLocation,
					Effect.MOBSPAWNER_FLAMES, 25);
		} 
		catch (Exception ex)
		{
			//this.plugin.log("Could not giveItem(): " + ex.getMessage());
			return false;
		}

		return true;
	}

	public void run()
	{
		if (giveItem())
		{
			Random random = new Random();

			if (speak)
			{
				plugin.getLanguageManager().setPlayerName(player.getName());

				plugin.getLanguageManager().setPlayerName(player.getName());
				plugin.getLanguageManager().setType(itemType.name());
				plugin.getGodManager().GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToBelieverItemBlessing, 2 + random.nextInt(10));
			}

			//this.plugin.log(this.godName + " gave a " + this.itemType.name() + " to " + this.player.getName());
		}
	}
}