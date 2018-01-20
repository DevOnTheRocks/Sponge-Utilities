package rocks.gameonthe.sponge.command;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import rocks.gameonthe.sponge.GameOnTheRocks;
import rocks.gameonthe.sponge.Permissions;
import rocks.gameonthe.sponge.config.ServerRetirementConfig;

@NonnullByDefault
public class CommandRetirement implements CommandExecutor {

  private final GameOnTheRocks plugin;
  private final Text ENABLE = Text.of("enable");
  private final Text PHASE = Text.of("phase");
  private final Text DATE = Text.of("date");

  private enum Phase {
    ONE, TWO
  }

  private CommandSpec commandSpec;

  public CommandRetirement(GameOnTheRocks plugin) {
    this.plugin = plugin;
    this.commandSpec = CommandSpec.builder()
        .description(Text.of("Server Retirement"))
        .permission(Permissions.COMMAND_RETIREMENT)
        .arguments(
            GenericArguments.optionalWeak(GenericArguments.bool(ENABLE)),
            GenericArguments.optional(GenericArguments.seq(
                GenericArguments.enumValue(PHASE, Phase.class),
                GenericArguments.dateTime(DATE)
            ))
        )
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec, "retire");
    plugin.getLogger().info("Registered Retirement Command.");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    ServerRetirementConfig config = plugin.getConfig().getServerRetirement();

    if (args.hasAny(ENABLE)) {
      config.setEnabled(args.<Boolean>getOne(ENABLE).get());
      src.sendMessage(Text.of("Server retirement ", config.isEnabled() ? "ENABLED" : "DISABLED", "."));
      plugin.getConfigManager().save();
    } else if (args.hasAny(PHASE) && args.hasAny(DATE)) {
      Phase phase = args.<Phase>getOne(PHASE).get();
      Instant instant = args.<LocalDateTime>getOne(DATE).get().toInstant(ZoneOffset.UTC);
      switch (phase) {
        case ONE:
          config.setPhaseOne(instant);
          break;
        case TWO:
          config.setPhaseTwo(instant);
          break;
      }
      src.sendMessage(Text.of(
          "Server retirement phase ", phase, " set to ", instant, "."
      ));
      plugin.getConfigManager().save();

      src.sendMessage(Text.of(
          "Do you want to reload the now?", Text.NEW_LINE,
          Text.of(TextColors.WHITE, "[", TextColors.GREEN, "Yes", TextColors.WHITE, "]")
              .toBuilder()
              .onHover(TextActions.showText(Text.of("Click to reload")))
              .onClick(TextActions.executeCallback(plugin::reload)),
          " ",
          Text.of(TextColors.WHITE, "[", TextColors.RED, "No", TextColors.WHITE, "]")
              .toBuilder()
              .onHover(TextActions.showText(Text.of("Click to cancel")))
      ));
    } else {
      PaginationList.builder()
          .title(Text.of("Server Retirement"))
          .contents(
              Text.of("Enabled ", " : ", config.isEnabled()),
              Text.of("Phase 1 ", " : ", config.getPhaseOne() != null
                  ? config.getPhaseOne()
                  : "Disabled"
              ),
              Text.of("Phase 2 ", " : ", config.getPhaseTwo() != null
                  ? config.getPhaseTwo()
                  : "Disabled"
              )
          )
          .sendTo(src);
    }
    return CommandResult.success();
  }
}
