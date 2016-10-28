package net.rebelcraft.utils.command;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Random;


public class CommandRegister implements CommandExecutor {
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (src instanceof Player) {
			Player player = (Player) src;
			Random r = new Random();
			int code = r.nextInt(10000);

			player.sendMessage(Text.of("Please use https://discord.gg/0meBvxkpFe3bXh2f to join the Rebelcraft Discord."));
			player.sendMessage(Text.of("Your registration code is: " + code));
			player.sendMessage(Text.of("Please follow the instructions in the message from the Rebelcraft Bot."));

			// TODO: Save code in MySQL database
		} else if (src instanceof ConsoleSource || src instanceof CommandBlockSource) {
			src.sendMessage(Text.of("This command is only to be used by a player!"));
		}
		return CommandResult.success();
	}
}