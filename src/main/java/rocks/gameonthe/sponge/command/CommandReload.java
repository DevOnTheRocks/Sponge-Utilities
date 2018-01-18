package rocks.gameonthe.sponge.command;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import rocks.gameonthe.sponge.GameOnTheRocks;
import rocks.gameonthe.sponge.Permissions;

@NonnullByDefault
public class CommandReload implements CommandExecutor {

  private final GameOnTheRocks plugin;

  private CommandSpec commandSpec;

  public CommandReload(GameOnTheRocks plugin) {
    this.plugin = plugin;
    this.commandSpec = CommandSpec.builder()
        .description(Text.of("Reload"))
        .permission(Permissions.COMMAND_RELOAD)
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec, "gotrreload");
    plugin.getLogger().info("Registered Reload Command.");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

    plugin.reload(src);
    return CommandResult.success();
  }
}
