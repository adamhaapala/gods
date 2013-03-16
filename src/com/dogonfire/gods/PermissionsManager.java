package com.dogonfire.gods;

import java.util.List;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.api.util.Calculable;
import de.bananaco.bpermissions.api.util.CalculableType;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsManager 
{
	private String pluginName = "null";
	private PluginManager pluginManager = null;
	private Gods plugin;
	private PermissionsPlugin permissionsBukkit = null;

	private PermissionManager pex = null;

	private GroupManager groupManager = null;

	public PermissionsManager(Gods p) {
		this.plugin = p;
	}

	public void load() {
		this.pluginManager = this.plugin.getServer().getPluginManager();

		if (this.pluginManager.getPlugin("PermissionsBukkit") != null) {
			this.plugin.log("Using PermissionsBukkit.");
			this.pluginName = "PermissionsBukkit";
			this.permissionsBukkit = ((PermissionsPlugin) this.pluginManager
					.getPlugin("PermissionsBukkit"));
		} else if (this.pluginManager.getPlugin("PermissionsEx") != null) {
			this.plugin.log("Using PermissionsEx.");
			this.pluginName = "PermissionsEx";
			this.pex = PermissionsEx.getPermissionManager();
		} else if (this.pluginManager.getPlugin("GroupManager") != null) {
			this.plugin.log("Using GroupManager");
			this.pluginName = "GroupManager";
			this.groupManager = ((GroupManager) this.pluginManager
					.getPlugin("GroupManager"));
		} else if (this.pluginManager.getPlugin("bPermissions") != null) {
			this.plugin.log("Using bPermissions.");
			this.pluginName = "bPermissions";
		} else {
			this.plugin
					.log("No permissions plugin detected! Defaulting to superperm");
			this.pluginName = "SuperPerm";
		}
	}

	public Plugin getPlugin() 
	{
		return this.plugin;
	}

	public String getPermissionPluginName() 
	{
		return this.pluginName;
	}

	public boolean hasPermission(Player player, String node) 
	{
		if (this.pluginName.equals("PermissionsBukkit")) 
		{
			return player.hasPermission(node);
		}
		if (this.pluginName.equals("PermissionsEx")) 
		{
			return this.pex.has(player, node);
		}
		if (this.pluginName.equals("GroupManager")) 
		{
			AnjoPermissionsHandler handler = this.groupManager.getWorldsHolder().getWorldPermissions(player);

			if (handler == null) 
			{
				return false;
			}

			return handler.has(player, node);
		}
		if (this.pluginName.equals("bPermissions")) 
		{
			return ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), node);
		}

		return player.hasPermission(node);
	}

	public boolean isGroup(String groupName) {
		if (this.pluginName.equals("PermissionsBukkit")) {
			if (this.permissionsBukkit.getGroup(groupName) == null) {
				return false;
			}

			return true;
		}

		return false;
	}

	public String getGroup(String playerName) 
	{
		if (this.pluginName.equals("PermissionsBukkit")) 
		{
			if (this.permissionsBukkit.getGroups(playerName) == null) 
			{
				return "";
			}

			if (this.permissionsBukkit.getGroups(playerName).size() == 0) 
			{
				return "";
			}

			return ((Group) this.permissionsBukkit.getGroups(playerName).get(0)).getName();
		}
		if (this.pluginName.equals("PermissionsEx")) 
		{
			if ((pex.getUser(playerName).getGroups() == null) || (pex.getUser(playerName).getGroups().length == 0)) 
			{
				return "";
			}

			return pex.getUser(this.pluginName).getGroups()[0].getName();
		}
		if (this.pluginName.equals("GroupManager")) {
			AnjoPermissionsHandler handler = this.groupManager
					.getWorldsHolder().getWorldPermissionsByPlayerName(
							playerName);

			if (handler == null) {
				return "";
			}

			return handler.getGroup(playerName);
		}
		if (this.pluginName.equals("bPermissions")) {
			de.bananaco.bpermissions.api.World w = WorldManager.getInstance()
					.getWorld(playerName);

			if (w == null) {
				return "";
			}

			return "";
		}

		return "";
	}

	public String getPrefix(String playerName) {
		if (this.pluginName.equals("PermissionsBukkit")) {
			return "";
		}
		if (this.pluginName.equals("PermissionsEx")) {
			return this.pex.getUser(this.pluginName).getOwnSuffix();
		}
		if (this.pluginName.equals("GroupManager")) {
			AnjoPermissionsHandler handler = this.groupManager
					.getWorldsHolder().getWorldPermissionsByPlayerName(
							playerName);

			if (handler == null) {
				return "";
			}

			return handler.getUserPrefix(playerName);
		}
		if (this.pluginName.equals("bPermissions")) {
			de.bananaco.bpermissions.api.World w = WorldManager.getInstance()
					.getWorld(playerName);

			if (w == null) {
				return "";
			}

			Calculable c = w.get(playerName, CalculableType.USER);

			return c.getValue("prefix");
		}

		return "";
	}

	public void setGroup(String playerName, String group) {
		if (this.permissionsBukkit.getServer().getPlayer(playerName) != null) {
			if (this.permissionsBukkit.getServer().getPlayer(playerName)
					.getGameMode() == GameMode.CREATIVE) {
				this.permissionsBukkit.getServer().dispatchCommand(
						Bukkit.getConsoleSender(), "gm " + playerName);
			}
		}

		this.permissionsBukkit.getServer().dispatchCommand(
				Bukkit.getConsoleSender(),
				"perm player setgroup " + playerName + " " + group);
	}
}

/*
 * Location: C:\Projects\minecraft\Gods.jar Qualified Name:
 * com.dogonfire.gods.PermissionsManager JD-Core Version: 0.6.2
 */