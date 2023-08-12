/*
 *   Copyright (C) 2022 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarryCommand;
import at.pcgamingfreaks.MarriageMaster.Bukkit.CommonMessages;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Permissions;

import me.nahu.scheduler.wrapper.FoliaWrappedJavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class PvPCommand extends MarryCommand implements Listener
{
	private final Message messagePvPOn, messagePvPOff, messagePvPIsOff;
	private final String helpMulti, helpOn, helpOff;
	private MarryCommand onCommand, offCommand;

	public PvPCommand(MarriageMaster plugin)
	{
		super(plugin, "pvp", plugin.getLanguage().getTranslated("Commands.Description.PvP"), Permissions.PVP, true, true, plugin.getLanguage().getCommandAliases("PvP"));

		messagePvPOn    = plugin.getLanguage().getMessage("Ingame.PvP.On");
		messagePvPOff   = plugin.getLanguage().getMessage("Ingame.PvP.Off");
		messagePvPIsOff = plugin.getLanguage().getMessage("Ingame.PvP.IsDisabled");

		helpMulti = "<" + CommonMessages.getHelpPartnerNameVariable() + "> <" + plugin.getCommandManager().getOnSwitchTranslation() + " / " +
				plugin.getCommandManager().getOffSwitchTranslation() + " / " + plugin.getCommandManager().getToggleSwitchTranslation() + ">";
		helpOn    = plugin.getCommandManager().getOnSwitchTranslation();
		helpOff   = plugin.getCommandManager().getOffSwitchTranslation();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void afterRegister()
	{
		MarriageMaster plugin = (MarriageMaster) getMarriagePlugin();
		onCommand = new PvPOnCommand(plugin);
		plugin.getCommandManager().registerSubCommand(onCommand);
		offCommand = new PvPOffCommand(plugin);
		plugin.getCommandManager().registerSubCommand(offCommand);
	}

	@Override
	public void beforeUnregister()
	{
		MarriageMaster plugin = (MarriageMaster) getMarriagePlugin();
		plugin.getCommandManager().unRegisterSubCommand(onCommand);
		onCommand.close();
		plugin.getCommandManager().unRegisterSubCommand(offCommand);
		offCommand.close();
	}

	@Override
	public void close()
	{
		if(onCommand != null)
		{
			onCommand.close();
			onCommand = null;
		}
		if(offCommand != null)
		{
			offCommand.close();
			offCommand = null;
		}
		HandlerList.unregisterAll(this);
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length < 1)
		{
			showHelp(sender, mainCommandAlias);
			return;
		}
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) sender);
		Marriage marriage;
		if(getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 2)
		{
			marriage = player.getMarriageData(getMarriagePlugin().getPlayerData(args[0]));
			if(marriage == null)
			{
				CommonMessages.getMessageTargetPartnerNotFound().send(sender);
				return;
			}
		}
		else
		{
			marriage = player.getNearestPartnerMarriageData();
		}
		if(getMarriagePlugin().getCommandManager().isOnSwitch(args[args.length - 1]))
		{
			//noinspection ConstantConditions
			marriage.setPVPEnabled(true);
			messagePvPOn.send(sender);
		}
		else if(getMarriagePlugin().getCommandManager().isOffSwitch(args[args.length - 1]))
		{
			//noinspection ConstantConditions
			marriage.setPVPEnabled(false);
			messagePvPOff.send(sender);
		}
		else
		{
			if(getMarriagePlugin().getCommandManager().isToggleSwitch(args[args.length - 1]))
			{
				//noinspection ConstantConditions
				if(marriage.isPVPEnabled())
				{
					marriage.setPVPEnabled(false);
					messagePvPOff.send(sender);
				}
				else
				{
					marriage.setPVPEnabled(true);
					messagePvPOn.send(sender);
				}
			}
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		MarriagePlayer player = getMarriagePlugin().getPlayerData((Player) requester);
		if(player.isMarried())
		{
			List<HelpData> help = new LinkedList<>();
			if(player.getPartners().size() > 1)
			{
				help.add(new HelpData(getTranslatedName(), helpMulti, getDescription()));
			}
			else
			{
				//noinspection ConstantConditions
				boolean isPVPEnabled = player.getMarriageData().isPVPEnabled();
				help.add(new HelpData(getTranslatedName() + (isPVPEnabled ? helpOff : helpOn) , "", (isPVPEnabled ? offCommand : onCommand).getDescription()));
			}
			return help;
		}
		return EMPTY_HELP_LIST;
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player dmgSource;
			if(event.getDamager() instanceof Player) dmgSource = (Player) event.getDamager();
			else if(event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) dmgSource = (Player) ((Projectile) event.getDamager()).getShooter();
			else return;
			MarriagePlayer player = getMarriagePlugin().getPlayerData(dmgSource);
			Marriage marriage = player.getMarriageData(getMarriagePlugin().getPlayerData((Player) event.getEntity()));
			if(marriage != null && !marriage.isPVPEnabled())
			{
				messagePvPIsOff.send(dmgSource);
				event.setCancelled(true);
			}
		}
	}

	private abstract static class PvPSubCommand extends MarryCommand
	{
		public PvPSubCommand(FoliaWrappedJavaPlugin plugin, String name, String description, String permission, boolean mustBeMarried, boolean partnerSelectorInHelpForMoreThanOnePartner, String... aliases)
		{
			super(plugin, name, description, permission, mustBeMarried, partnerSelectorInHelpForMoreThanOnePartner, aliases);
		}

		@Override
		public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			return getMarriagePlugin().getCommandManager().getSimpleTabComplete(sender, args);
		}

		@Override
		public List<HelpData> getHelp(@NotNull CommandSender requester)
		{
			return EMPTY_HELP_LIST;
		}

		protected Marriage getMarriage(Player sender, String[] args)
		{
			MarriagePlayer player = getMarriagePlugin().getPlayerData(sender);
			Marriage marriage;
			if(getMarriagePlugin().areMultiplePartnersAllowed() && args.length == 1)
			{
				marriage = player.getMarriageData(getMarriagePlugin().getPlayerData(args[0]));
				if(marriage == null)
				{
					CommonMessages.getMessageTargetPartnerNotFound().send(sender);
					return null;
				}
			}
			else
			{
				marriage = player.getNearestPartnerMarriageData();
			}
			return marriage;
		}
	}

	private class PvPOnCommand extends PvPSubCommand
	{
		public PvPOnCommand(MarriageMaster plugin)
		{
			super(plugin, "pvpon", plugin.getLanguage().getTranslated("Commands.Description.PvPOn"), Permissions.PVP, true, true, plugin.getLanguage().getCommandAliases("PvPOn"));
		}

		@Override
		public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			Marriage marriage = getMarriage((Player) sender, args);
			if(marriage != null)
			{
				marriage.setPVPEnabled(true);
				messagePvPOn.send(sender);
			}
		}
	}

	private class PvPOffCommand extends PvPSubCommand
	{
		public PvPOffCommand(MarriageMaster plugin)
		{
			super(plugin, "pvpoff", plugin.getLanguage().getTranslated("Commands.Description.PvPOff"), Permissions.PVP, true, true, plugin.getLanguage().getCommandAliases("PvPOff"));
		}

		@Override
		public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
		{
			Marriage marriage = getMarriage((Player) sender, args);
			if(marriage != null)
			{
				marriage.setPVPEnabled(false);
				messagePvPOff.send(sender);
			}
		}
	}
}
