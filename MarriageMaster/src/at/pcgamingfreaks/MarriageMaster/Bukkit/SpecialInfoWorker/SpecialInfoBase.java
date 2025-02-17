/*
 *   Copyright (C) 2021 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker;

import at.pcgf.libs.me.nahu.scheduler.wrapper.FoliaWrappedJavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class SpecialInfoBase implements Listener
{
	private final FoliaWrappedJavaPlugin plugin;
	private final String permission;

	protected SpecialInfoBase(final FoliaWrappedJavaPlugin plugin, final String permission)
	{
		this.plugin = plugin;
		this.permission = permission;
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent event)
	{
		if(event.getPlayer().hasPermission(permission))
		{
			plugin.getScheduler().runTaskLaterAtEntity(event.getPlayer(), () -> {
				if(event.getPlayer().isOnline())
				{
					sendMessage(event.getPlayer());
				}
			}, 3 * 20L); // Run with a 3 seconds delay
		}
	}

	protected abstract void sendMessage(final Player player);
}