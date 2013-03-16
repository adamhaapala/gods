package com.dogonfire.gods;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener 
{
	private Gods plugin;
	private Random random = new Random();
	private HashMap<String, Long> lastEatTimes = new HashMap<String, Long>();

	BlockListener(Gods p) 
	{
		this.plugin = p;
	}
	
	@EventHandler
	public void OnPlayerInteract(PlayerInteractEvent event) 
	{
		Player player = event.getPlayer();
		String godName = null;
	
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) 
		{
			godName = plugin.getBelieverManager().getGodForBeliever(event.getPlayer().getName());
			Material type = player.getItemInHand().getType();

			if (godName != null && type != null && type != Material.AIR)
			{
				Long lastEatTime = (Long) lastEatTimes.get(player.getName());
				Long currentTime = System.currentTimeMillis();

				if ((lastEatTime == null) || (currentTime.longValue() - lastEatTime.longValue() > 10000L)) 
				{
					if (plugin.commandmentsEnabled && player.getHealth()!=player.getMaxHealth()) 
					{
						if ((player.isOp()) || (plugin.getPermissionsManager().hasPermission(player, "gods.commandments"))) 
						{
							plugin.getGodManager().handleEat(player.getName(), godName, type.name());
						}
					}

					if (plugin.questsEnabled) 
					{
						plugin.getQuestManager().handleEat(player.getName(), godName, type.name());
					}

					lastEatTimes.put(player.getName(), currentTime);
				}
			}
		}

		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) 
		{
			if (!plugin.getAltarManager().isAltarSign(event.getClickedBlock())) 
			{
				return;
			}
						
			BlockState state = event.getClickedBlock().getState();

			Sign sign = (Sign) state;

			if (plugin.cursingEnabled) 
			{
				String cursedPlayer = plugin.getAltarManager().getCursedPlayerFromAltar(event.getClickedBlock(), sign.getLines());

				if (cursedPlayer != null) 
				{
					if (plugin.getGodManager().isPriest(player.getName())) 
					{
						String oldCursedPlayer = plugin.getGodManager().getCursedPlayerForGod(godName);

						if (oldCursedPlayer != null && oldCursedPlayer.equals(cursedPlayer)) 
						{
							plugin.getGodManager().setCursedPlayerForGod(godName, null);

							plugin.getLanguageManager().setPlayerName(cursedPlayer);

							plugin.getGodManager().GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToPriestCursedPlayerUnset, 2);
							plugin.getGodManager().GodSayToBelieversExcept(godName, LanguageManager.LANGUAGESTRING.GodToBelieversCursedPlayerUnset, cursedPlayer);
						} 
						else 
						{
							plugin.getGodManager().setCursedPlayerForGod(godName, cursedPlayer);

							plugin.getLanguageManager().setPlayerName(cursedPlayer);

							plugin.getGodManager().GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToPriestCursedPlayerSet, 2);
							plugin.getGodManager().GodSayToBelieversExcept(godName, LanguageManager.LANGUAGESTRING.GodToBelieversCursedPlayerSet, player.getName());

							plugin.log(player.getName() + " asked " + godName + " for curses on " + cursedPlayer);
						}
					} 
					else 
					{
						plugin.sendInfo(player, ChatColor.RED + "You cannot curse players");
					}

					return;
				}

			}

			if (plugin.blessingEnabled) 
			{
				String blessedPlayer = plugin.getAltarManager().getBlessedPlayerFromAltar(event.getClickedBlock(), sign.getLines());

				if (blessedPlayer != null) 
				{
					if (plugin.getGodManager().isPriest(player.getName())) 
					{
						String oldBlessedPlayer = plugin.getGodManager().getBlessedPlayerForGod(godName);

						if ((oldBlessedPlayer != null) && (oldBlessedPlayer.equals(blessedPlayer))) 
						{
							plugin.getGodManager().setBlessedPlayerForGod(godName, null);

							plugin.getLanguageManager().setPlayerName(blessedPlayer);

							plugin.getGodManager().GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToPriestBlessedPlayerUnset, 2);

							plugin.getGodManager().GodSayToBelieversExcept(godName, LanguageManager.LANGUAGESTRING.GodToBelieversBlessedPlayerUnset, player.getName());
						} 
						else 
						{
							plugin.getGodManager().setBlessedPlayerForGod(godName, blessedPlayer);

							plugin.getLanguageManager().setPlayerName(blessedPlayer);

							plugin.getGodManager().GodSay(godName, player, LanguageManager.LANGUAGESTRING.GodToPriestBlessedPlayerSet, 2);
							
							plugin.getGodManager().GodSayToBelieversExcept(godName, LanguageManager.LANGUAGESTRING.GodToBelieversBlessedPlayerSet, player.getName());

							plugin.log(player.getName() + " asked " + godName + " for blessings on " + blessedPlayer);
						}
					} 
					else 
					{
						plugin.sendInfo(player, ChatColor.RED + "You cannot bless players");
					}

					return;
				}

			}

			if ((!event.getPlayer().isOp()) && (!plugin.getPermissionsManager().hasPermission(event.getPlayer(), "gods.altar.pray"))) 
			{
				plugin.sendInfo(player, ChatColor.RED + "You do not have permission to pray at altars");
				return;
			}

			Block block = event.getClickedBlock();

			if (!plugin.getAltarManager().isPrayingAltar(block)) 
			{
				return;
			}

			godName = sign.getLine(2);

			if (godName == null) 
			{
				return;
			}

			godName = godName.trim();

			if (godName.length() <= 1) 
			{
				plugin.sendInfo(event.getPlayer(), ChatColor.RED + "That is not a proper name for a God!");
				return;
			}

			godName = plugin.getGodManager().formatGodName(godName);

			if ((plugin.isBlacklistedGod(godName)) || (!plugin.isWhitelistedGod(godName))) 
			{
				plugin.sendInfo(player, ChatColor.RED + "You cannot pray to such a God");
				return;
			}

			if (!plugin.getGodManager().hasGodAccess(player.getName(), godName)) 
			{
				plugin.sendInfo(event.getPlayer(), ChatColor.RED + "That is a private sect. You cannot pray to it.");
				return;
			}

			if (plugin.getGodManager().handlePray(block.getLocation(), event.getPlayer(), godName))
			{
				plugin.log(event.getPlayer().getName() + " prayed to " + godName);
			}
		}
	}
	

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) 
	{
		if (plugin.questsEnabled) 
		{
			if (event.getAction() == Action.PHYSICAL) 
			{
				if (plugin.getQuestManager().handlePressurePlate(event.getPlayer().getName(), event.getClickedBlock())) 
				{
					event.setCancelled(true);
				}
			}

			if ((event.getClickedBlock() != null) && (this.plugin.getQuestManager().handleOpenChest(event.getPlayer().getName(), event.getClickedBlock().getLocation()))) 
			{
				event.setCancelled(true);
			}
		}

		if (plugin.biblesEnabled) 
		{
			if ((event.getAction().equals(Action.RIGHT_CLICK_AIR)) || (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) 
			{
				if ((event.getItem() != null) && (event.getPlayer().getItemInHand() != null)) 
				{
					Player player = event.getPlayer();

					ItemStack book = player.getItemInHand();

					if (book.getType() == Material.WRITTEN_BOOK) 
					{
						String godName = plugin.getBibleManager().getGodForBible(book);

						if (godName != null) 
						{
							Long lastEatTime = (Long) lastEatTimes.get(player.getName());
							Long currentTime = Long.valueOf(System.currentTimeMillis());

							if ((lastEatTime == null)|| (currentTime.longValue() - lastEatTime.longValue() > 10000L)) 
							{
								plugin.getGodManager().handleReadBible(godName, player);
								plugin.getQuestManager().handleReadBible(godName, player.getName());
								lastEatTimes.put(player.getName(), currentTime);
							}
						}
					}
				}
			}
			else if ((event.getAction().equals(Action.LEFT_CLICK_AIR)) || (event.getAction().equals(Action.LEFT_CLICK_BLOCK))) 
			{
				if ((event.getItem() != null) && (event.getPlayer().getItemInHand() != null)) 
				{
					Player player = event.getPlayer();

					ItemStack book = player.getItemInHand();

					if (book.getType() == Material.WRITTEN_BOOK) 
					{
						String godName = plugin.getBibleManager().getGodForBible(book);
						
						if (godName != null) 
						{
							plugin.getGodManager().handleBibleMelee(godName, player);
							plugin.getQuestManager().handleBibleMelee(godName, player.getName());
						}
					}
				}
			}
		}

		if (!plugin.holyLandEnabled) 
		{
			return;
		}

		if (!plugin.getPermissionsManager().hasPermission(event.getPlayer(), "gods.holyland")) 
		{
			plugin.logDebug(event.getPlayer().getName() + " does not have holyland permission");
			return;
		}

		if (!event.getPlayer().isOp() && plugin.getLandManager().isNeutralLandLocation(event.getPlayer().getLocation())) 
		{
			event.setCancelled(true);
			return;
		}

		if (event.getClickedBlock() == null) 
		{
			return;
		}

		String blockGodName = plugin.getLandManager().getGodAtHolyLandLocation(event.getClickedBlock().getLocation());

		if (blockGodName == null) 
		{
			return;
		}

		if (plugin.holylandBreakableBlockTypes.contains(event.getClickedBlock().getType()) || plugin.getAltarManager().isAltarBlock(event.getClickedBlock())) 
		{	
			return;
		}

		String playerGodName = plugin.getBelieverManager().getGodForBeliever(event.getPlayer().getName());

		if (playerGodName == null) 
		{
			event.getPlayer().sendMessage(ChatColor.RED + "You do not have access to the holy land of " + ChatColor.GOLD + blockGodName);
			event.setCancelled(true);
			return;
		}

		if (!playerGodName.equals(blockGodName)) 
		{
			if (plugin.getGodManager().hasAllianceRelation(blockGodName, playerGodName)) 
			{
				return;
			}

			event.getPlayer().sendMessage(ChatColor.RED + "You do not have access to the holy land of " + ChatColor.GOLD + blockGodName);
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) 
	{
		Player player = event.getPlayer();

		lastEatTimes.remove(player.getName());

		if (!plugin.holyLandEnabled) 
		{
			return;
		}

		plugin.getLandManager().handleQuit(player.getName());
	}

	@EventHandler
	public void OnCreatureSpawn(CreatureSpawnEvent event) 
	{
		if (!plugin.holyLandEnabled) 
		{
			return;
		}

		if (plugin.getLandManager().isNeutralLandLocation(event.getLocation())) 
		{
			return;
		}

		if (plugin.getLandManager().getGodAtHolyLandLocation(event.getLocation()) != null) 
		{
			if(!plugin.getLandManager().isMobTypeAllowedToSpawn(event.getEntityType()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void OnEntityDamageByEntity(EntityDamageByEntityEvent event) 
	{
		if (event.getDamager() == null) 
		{
			return;
		}

				
		if (plugin.holyArtifactsEnabled) 
		{
			if(event.getDamager() instanceof Player)
			{							
				Player player = (Player)event.getDamager();
				String godName = plugin.getBelieverManager().getGodForBeliever(player.getName());
				
				float damage = event.getDamage();
				ItemStack itemInHand = player.getItemInHand();
			
				damage *= (plugin.getHolyArtifactManager().handleDamage(player.getName(), event.getEntity(), itemInHand, godName));

				event.setDamage((int)damage);		
			}
		}

		if (!plugin.holyLandEnabled) 
		{
			return;
		}

		if (plugin.getLandManager().isNeutralLandLocation(event.getEntity().getLocation())) 
		{
			event.setCancelled(true);
		}

		String godName = this.plugin.getLandManager().getGodAtHolyLandLocation(event.getEntity().getLocation());

		if (godName != null) 
		{
			if ((event.getDamager() instanceof Player)) 
			{
				Player player = (Player) event.getDamager();

				String attackerGodName = this.plugin.getBelieverManager().getGodForBeliever(player.getName());

				if (attackerGodName == null) 
				{
					if (plugin.holyLandLightning) 
					{
						plugin.getGodManager().strikePlayerWithLightning(player.getName(), 3);
					}

					event.setCancelled(true);

					return;
				}

				if (!godName.equals(attackerGodName)) 
				{
					if (plugin.holyLandLightning) 
					{
						plugin.getGodManager().strikePlayerWithLightning(player.getName(), 3);
					}

					if (!plugin.getGodManager().hasWarRelation(godName, attackerGodName)) 
					{
						event.setCancelled(true);
						return;
					}

				}

				if (!plugin.getGodManager().getGodPvP(godName)) 
				{
					event.setCancelled(true);
				}

			} 
			else if (((event.getDamager() instanceof LivingEntity)) && (!this.plugin.getGodManager().getGodMobDamage(godName))) 
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void OnPlayerDropItem(PlayerDropItemEvent event) 
	{
		Player player = event.getPlayer();

		if (!player.isOp() && !plugin.getPermissionsManager().hasPermission(player, "gods.altar.sacrifice")) 						
		{
			plugin.logDebug("OnPlayerDropItem(): Does not have gods.altar.sacrifice");
			return;
		}

		if (player.getGameMode() == GameMode.CREATIVE) 
		{
			return;
		}

		plugin.getAltarManager().addDroppedItem(event.getItemDrop().getEntityId(), player.getName());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnBlockPlace(BlockPlaceEvent event) 
	{
		if (!this.plugin.holyLandEnabled) 
		{
			return;
		}

		if (event.getBlock() == null) 
		{
			return;
		}

		Player player = event.getPlayer();

		if (player.isOp()) 
		{
			return;
		}

		if (player != null && !plugin.getPermissionsManager().hasPermission(event.getPlayer(), "gods.holyland")) 
		{
			plugin.logDebug(event.getPlayer().getName() + " does not have holyland permission");
			return;
		}

		if (this.plugin.getLandManager().isNeutralLandLocation(event.getBlock().getLocation())) 
		{
			if (player != null) 
			{
				player.sendMessage(ChatColor.RED + "You cannot build in neutral land");
			}

			event.setCancelled(true);
			return;
		}

		String godName = this.plugin.getLandManager().getGodAtHolyLandLocation(event.getBlock().getLocation());
		String playerGod = null;

		if (godName == null) 
		{
			return;
		}

		if (player != null) 
		{
			playerGod = this.plugin.getBelieverManager().getGodForBeliever(player.getName());
		}

		if ((playerGod == null) || (!playerGod.equals(godName))) 
		{
			if (player != null) 
			{
				player.sendMessage(ChatColor.RED + "You do not have access to the holy land of " + ChatColor.YELLOW + godName);
			}

			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnBlockBreak(BlockBreakEvent event) 
	{
		if (!plugin.holyLandEnabled) 
		{
			return;
		}

		Player player = event.getPlayer();

		if (player.isOp()) 
		{
			return;
		}

		if (player != null && !plugin.getPermissionsManager().hasPermission(player, "gods.holyland")) 
		{
			this.plugin.logDebug(event.getPlayer().getName() + " does not have holyland permission");
			return;
		}

		if (event.getBlock() == null) 
		{
			return;
		}

		if (this.plugin.getLandManager().isNeutralLandLocation(event.getBlock().getLocation())) 
		{
			if (player != null) 
			{
				player.sendMessage(ChatColor.RED + "You cannot break blocks in neutral land");
			}

			event.setCancelled(true);
			return;
		}

		String godName = this.plugin.getLandManager().getGodAtHolyLandLocation(event.getBlock().getLocation());
		Player attacker = event.getPlayer();
		String attackerGod = null;

		if (godName == null) {
			return;
		}

		if (this.plugin.holylandBreakableBlockTypes.contains(event.getBlock()
				.getType())) {
			return;
		}

		if (this.plugin.getAltarManager().isAltarBlock(event.getBlock())) 
		{
			if (attacker != null) 
			{
				this.plugin.getLanguageManager().setPlayerName(
						attacker.getName());
				this.plugin
						.getGodManager()
						.godSayToBelievers(
								godName,
								LanguageManager.LANGUAGESTRING.GodToBelieversAltarDestroyedByPlayer,
								2 + this.random.nextInt(10));
				attacker.sendMessage(ChatColor.AQUA
						+ "You destroyed the altar of " + ChatColor.YELLOW
						+ godName + ChatColor.AQUA + "!");
			} else {
				this.plugin
						.getGodManager()
						.godSayToBelievers(
								godName,
								LanguageManager.LANGUAGESTRING.GodToBelieversAltarDestroyed,
								2 + this.random.nextInt(10));
			}

			this.plugin.getLandManager().deleteGodAtHolyLandLocation(
					event.getBlock().getLocation());
		} else {
			if (attacker != null) {
				attackerGod = this.plugin.getBelieverManager()
						.getGodForBeliever(attacker.getName());
			}

			if ((attackerGod == null) || (!attackerGod.equals(godName))) 
			{
				if (attacker != null) 
				{
					attacker.sendMessage(ChatColor.RED + "You do not have access to the holy land of " + ChatColor.YELLOW + godName);
				}

				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void OnSignChange(SignChangeEvent event) 
	{
		if ((plugin.cursingEnabled) && (plugin.getAltarManager().isCursingAltar(event.getBlock(), event.getLines()))) 
		{
			if (!this.plugin.getAltarManager().handleNewCursingAltar(event)) 
			{
				event.setCancelled(true);
				event.getBlock().setType(Material.AIR);
				event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
			}

			return;
		}

		if ((plugin.blessingEnabled) && (plugin.getAltarManager().isBlessingAltar(event.getBlock(), event.getLines()))) 
		{
			if (!plugin.getAltarManager().handleNewBlessingAltar(event)) 
			{
				event.setCancelled(true);
				event.getBlock().setType(Material.AIR);
				event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
			}

			return;
		}
		
		if (plugin.getAltarManager().isPrayingAltar(event.getBlock())) 
		{
			if (!plugin.getAltarManager().handleNewPrayingAltar(event)) 
			{
				event.setCancelled(true);
				event.getBlock().setType(Material.AIR);
				event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
			}

			return;
		}
	}
	

	@EventHandler
	public void OnEntityDeath(EntityDeathEvent event) 
	{
		if (!(event.getEntity().getKiller() instanceof Player)) 
		{
			return;
		}

		Player player = event.getEntity().getKiller();

		if(player == null)
		{
			return;
		}
		
		String godName = plugin.getBelieverManager().getGodForBeliever(player.getName());

		if (godName == null) 
		{
			return;
		}

		if (plugin.propheciesEnabled) 
		{
			plugin.getProphecyManager().handleMobKill(player.getName(), godName, event.getEntityType().name());
		}

		if (plugin.questsEnabled) 
		{
			plugin.getQuestManager().handleKilledMob(godName, event.getEntityType().name());
		}

		if (plugin.holyArtifactsEnabled) 
		{
			plugin.getHolyArtifactManager().handleDeath(event.getEntity().getKiller().getName(), godName, event.getEntity().getKiller().getItemInHand());
		}

		if (!player.isOp() && !plugin.getPermissionsManager().hasPermission(player, "gods.commandments")) 
		{
			return;
		}

		plugin.getGodManager().handleKilled(player.getName(), godName, event.getEntityType().name());
	}

	@EventHandler
	public void OnEntityCombust(EntityCombustEvent event) 
	{
		if (!plugin.sacrificesEnabled) 
		{
			return;
		}

		if (event.getEntity() == null) 
		{
			return;
		}

		if (!(event.getEntity() instanceof Item)) 
		{
			return;
		}

		Item item = (Item) event.getEntity();

		if (event.getEntity().getType() != EntityType.DROPPED_ITEM) 
		{
			return;
		}

		String believerName = this.plugin.getAltarManager().getDroppedItemPlayer(event.getEntity().getEntityId());

		if (believerName == null) 
		{
			return;
		}

		Player player = this.plugin.getServer().getPlayer(believerName);

		if (player == null) 
		{
			return;
		}

		if (!player.isOp() && (!this.plugin.getPermissionsManager().hasPermission(player, "gods.altar.sacrifice"))) 
		{
			plugin.logDebug("Does not have gods.altar.sacrifice");
			return;
		}

		String godName = this.plugin.getBelieverManager().getGodForBeliever(believerName);

		if (godName == null) 
		{
			return;
		}

		if (plugin.getQuestManager().handleSacrifice(godName, item.getItemStack().getType().name())) 
		{
			return;
		}

		plugin.getGodManager().handleSacrifice(godName, believerName, item.getItemStack().getType());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) 
	{
		Player player = event.getEntity();

		String godName = plugin.getBelieverManager().getGodForBeliever(player.getName());

		plugin.getGodManager().handleKilledPlayer(player.getName(), godName);
		plugin.getQuestManager().handleKilledPlayer(player.getName(), godName);
		
		if (!plugin.holyLandEnabled) 
		{
			return;
		}

		if (godName == null) 
		{
			return;
		}

		double powerBefore = plugin.getBelieverManager().getBelieverPower(player.getName());
		plugin.getBelieverManager().reducePrayer(player.getName(), 2);
		double powerAfter = plugin.getBelieverManager().getBelieverPower(player.getName());

		int powerLoss = (int) (powerBefore - powerAfter);

		if ((event.getEntity().getKiller() != null) && ((event.getEntity().getKiller() instanceof Player))) 
		{
			Player killer = event.getEntity().getKiller();

			plugin.sendInfo(killer, ChatColor.GOLD + godName + ChatColor.RED + " lost " + ChatColor.YELLOW + powerLoss + ChatColor.RED + " power because of your kill!");
		}
	
		plugin.sendInfo(player, ChatColor.GOLD + godName + ChatColor.RED + " lost " + ChatColor.YELLOW + powerLoss + ChatColor.RED + " power because of your death!");
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) 
	{
		if (!plugin.holyLandEnabled) 
		{
			return;
		}

		Player player = event.getPlayer();

		if (!plugin.getPermissionsManager().hasPermission(player, "gods.holyland")) 
		{
			return;
		}

		Location to = event.getTo();

		String godFrom = null;
		String godTo = null;

		godFrom = this.plugin.getLandManager().getGodAtHolyLandLocationFrom(player.getName());

		if (this.plugin.getLandManager().isNeutralLandLocation(to)) 
		{
			godTo = "NeutralLand";
			plugin.getLandManager().setNeutralLandLocationFrom(player.getName());
		} 
		else 
		{
			godTo = this.plugin.getLandManager().getGodAtHolyLandLocationTo(player.getName(), to);
		}

		if ((godFrom == null) && (godTo == null)) 
		{
			return;
		}

		if ((godTo != null) && ((godFrom == null) || (!godFrom.equals(godTo)))) 
		{
			if (godTo.equals("NeutralLand")) 
			{
				this.plugin.sendInfo(player, ChatColor.YELLOW + "Neutral Land - " + ChatColor.AQUA + "You are safe against mobs and PvP");
			} 
			else 
			{
				String playerGod = this.plugin.getBelieverManager()
						.getGodForBeliever(player.getName());

				if ((playerGod != null) && (playerGod.equals(godTo)))
					this.plugin.sendInfo(player,
							ChatColor.GREEN
									+ "Holy Land of "
									+ godTo
									+ " - "
									+ ChatColor.AQUA
									+ this.plugin.getGodManager()
											.getGodDescription(godTo));
				else
					this.plugin.sendInfo(player,
							ChatColor.GOLD
									+ "Holy Land of "
									+ godTo
									+ " - "
									+ ChatColor.AQUA
									+ this.plugin.getGodManager()
											.getGodDescription(godTo));
			}
		} 
		else if ((godFrom != null) && (godTo == null))
		{			
			this.plugin.sendInfo(player, ChatColor.DARK_GREEN + "Wilderness");
		}
	}
}
