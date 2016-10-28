package net.rebelcraft.utils;

import com.google.inject.Inject;
import net.rebelcraft.utils.command.CommandHelp;
import net.rebelcraft.utils.command.CommandRegister;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = "rebelutils", name = "Rebel Utilities", version = "1.0")
public class RebelUtilities {
	@Inject
	private Logger logger;

	public Logger getLogger() {
		return this.logger;
	}

	@Listener
	public void onServerStarted(GameStartedServerEvent event) {
		getLogger().info("Plugin has begun initialization.");
		registerCommands();
		// TODO
		getLogger().info("Initialization complete.");
	}

	@Listener
	public void onServerStopped(GameStoppedServerEvent event) {
		getLogger().info("Plugin has begun shutdown preparation.");
		// TODO
		getLogger().info("Shutdown actions complete.");
	}

	public void registerCommands() {
		Sponge.getCommandManager().register(this, CommandSpec.builder()
				.description(Text.of("Help Command"))
				.permission(Permissions.COMMAND_HELP)
				.executor(new CommandHelp())
				.build(), "ruhelp");

		Sponge.getCommandManager().register(this, CommandSpec.builder()
				.description(Text.of("Allows you to register"))
				.permission(Permissions.COMMAND_REGISTER)
				.executor(new CommandRegister())
				.build(), "register");
	}
}