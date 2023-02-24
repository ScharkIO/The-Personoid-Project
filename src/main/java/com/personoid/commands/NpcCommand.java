package com.personoid.commands;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.npc.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Player_Schark
 */
public class NpcCommand extends Command {

	protected NpcCommand() {
		super("npc");
	}

	@Override public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		switch (arg(args, 0)) {
			case "spawn" -> {
				NPCRegistry npcRegistry = PersonoidAPI.getRegistry();
				NPC npc = npcRegistry.createNPCInstance("Ursula", Skin.get("MHF_Alex"));
				npcRegistry.spawnNPC(npc, ((Player) sender).getLocation());
			}
			default -> sender.sendMessage(ChatColor.RED + "Invalid argument");
		};
		return true;
	}

	private static String arg(String[] args, int i) {
		return i >= args.length ? "" : args[i];
	}

	@Override
	public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1) {
			return Collections.singletonList("spawn");
		}
		return new ArrayList<>();
	}
}
