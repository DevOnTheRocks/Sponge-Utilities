package rocks.gameonthe.sponge.listener;

import java.util.List;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.claim.ClaimManager;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult.Type;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import rocks.gameonthe.sponge.GameOnTheRocks;

public class CreativeBlockHandler {

  private final GameOnTheRocks plugin;
  private final List<BlockType> blocks;

  public CreativeBlockHandler(GameOnTheRocks plugin) {
    this.plugin = plugin;
    this.blocks = plugin.getConfig().getCreativeBlock().getBlocks();
  }

  @Listener
  public void onPlaceBlock(ChangeBlockEvent.Place event, @First Player player) {
    event.getTransactions().stream()
        .filter(t -> t.isValid() &&
            blocks.contains(t.getFinal().getState().getType())
        )
        .forEach(t -> {
          BlockState state = t.getFinal().getState();
          player.sendMessage(Text.of(
              "To remove this ",
              Text.builder(state.getName())
                  .onHover(TextActions.showItem(
                      ItemStack.builder().fromBlockState(state).build().createSnapshot()
                  )),
              " right-click it with an empty hand."
          ));
        });
  }

  @Listener
  public void onInteractBlock(InteractBlockEvent.Secondary event, @First Player player) {
    // Check if the target is configured and if the player's hand is empty
    if (event.getTargetBlock().getLocation().isPresent()
        && !player.getItemInHand(event.getHandType()).isPresent()
        && blocks.contains(event.getTargetBlock().getState().getType())
        ) {

      Location<World> location = event.getTargetBlock().getLocation().get();
      World world = location.getExtent();
      ClaimManager manager = plugin.getGriefPrevention().getClaimManager(world);
      Claim claim = manager.getClaimAt(location);

      if (claim.isWilderness() || claim.isTrusted(player.getUniqueId())) {
        plugin.getLogger().info("{} removed a {} from {} ({},{},{}).",
            player.getName(),
            event.getTargetBlock().getState().getName(),
            world.getName(),
            location.getPosition().getFloorX(),
            location.getPosition().getFloorY(),
            location.getPosition().getFloorZ()
        );
        // Break Block
        world.setBlock(
            location.getBlockPosition(),
            BlockState.builder().blockType(BlockTypes.AIR).build()
        );
        // Create ItemStack
        ItemStack itemStack = ItemStack.builder()
            .fromBlockState(event.getTargetBlock().getState())
            .build();
        // Attempt to place the block in the player's inventory
        if (player.getInventory().offer(itemStack).getType() != Type.SUCCESS) {
          // If the player's inventory can't accept the item, drop it
          Entity item = world.createEntity(EntityTypes.ITEM, player.getLocation().getPosition());
          item.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot());
          world.spawnEntity(item);
        }
      }
    }
  }
}
