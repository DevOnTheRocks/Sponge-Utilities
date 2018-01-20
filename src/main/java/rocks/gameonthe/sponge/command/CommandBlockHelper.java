package rocks.gameonthe.sponge.command;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import rocks.gameonthe.sponge.GameOnTheRocks;
import rocks.gameonthe.sponge.Permissions;
import rocks.gameonthe.sponge.config.CreativeBlockConfig;

@NonnullByDefault
public class CommandBlockHelper implements CommandExecutor {

  private final GameOnTheRocks plugin;
  private final Text ENABLE = Text.of("enable");
  private final Text ACTION = Text.of("action");
  private final Text BLOCK = Text.of("block");

  private CommandSpec commandSpec;

  private enum Action {
    ADD, REMOVE
  }

  public CommandBlockHelper(GameOnTheRocks plugin) {
    this.plugin = plugin;
    this.commandSpec = CommandSpec.builder()
        .description(Text.of("Creative Block Helper"))
        .permission(Permissions.COMMAND_BLOCK_HELPER)
        .arguments(
            GenericArguments.optionalWeak(GenericArguments.bool(ENABLE)),
            GenericArguments.optional(GenericArguments.seq(
                GenericArguments.enumValue(ACTION, Action.class),
                GenericArguments.optional(GenericArguments.catalogedElement(BLOCK, BlockType.class))
            ))
        )
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec, "cbhelper");
    plugin.getLogger().info("Registered Creative Block Helper Command.");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    CreativeBlockConfig config = plugin.getConfig().getCreativeBlock();

    if (args.hasAny(ENABLE)) {
      config.setEnabled(args.<Boolean>getOne(ENABLE).get());
      src.sendMessage(
          Text.of("Server retirement ", config.isEnabled() ? "ENABLED" : "DISABLED", "."));
      plugin.getConfigManager().save();
    } else if (args.hasAny(ACTION)) {
      Action action = args.<Action>getOne(ACTION).get();
      BlockType type = args.<BlockType>getOne(BLOCK).orElse(null);
      if (type == null) {
        if (src instanceof Player && ((Player) src).getItemInHand(HandTypes.MAIN_HAND)
            .isPresent()) {
          ItemStack stack = ((Player) src).getItemInHand(HandTypes.MAIN_HAND).get();
          if (stack.getItem().getBlock().isPresent()) {
            type = stack.getItem().getBlock().get();
          } else {
            throw new CommandException(Text.of(
                TextColors.RED, stack.getItem().getName(), " is not a block."
            ));
          }
        } else {
          throw new CommandException(Text.of(
              TextColors.RED, "A block must be provided or held in your main hand."
          ));
        }

        switch (action) {
          case ADD:
            config.addBlock(type);
            src.sendMessage(Text.of(type.getName(), " was successfully added."));
            break;
          case REMOVE:
            config.removeBlock(type);
            src.sendMessage(Text.of(type.getName(), " was successfully removed."));
            break;
        }
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
      }
    } else {

      List<Text> list = Lists.newArrayList(
          Text.of("Enabled ", " : ", config.isEnabled()),
          Text.of("Blocks ", " : ")
      );
      list.addAll(
          config.getBlocks().stream()
              .map(b -> Text.builder(b.getName())
                  .onHover(TextActions.showItem(ItemStack.builder()
                      .fromBlockState(BlockState.builder().blockType(b).build()).build()
                      .createSnapshot()
                  ))
                  .build())
              .collect(Collectors.toList())
      );

      PaginationList.builder()
          .title(Text.of("Creative Block Helper"))
          .contents(list)
          .sendTo(src);
    }
    return CommandResult.success();
  }
}
