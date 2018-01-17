package rocks.gameonthe.sponge.listener;

import com.google.common.collect.Lists;
import java.util.List;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.claim.ClaimManager;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult.Type;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import rocks.gameonthe.sponge.GameOnTheRocks;

public class CreativeBlockHandler {

  private final GameOnTheRocks plugin;
  private final List<String> creativeBlocks = Lists.newArrayList("randomthings:naturecore");

  public CreativeBlockHandler(GameOnTheRocks plugin) {
    this.plugin = plugin;
  }

  @Listener
  public void onInteractBlock(InteractBlockEvent.Secondary event, @First Player player) {
    // Check if the target is a Nature Core and if the player's hand is empty
    if (event.getTargetBlock().getLocation().isPresent()
        && !player.getItemInHand(event.getHandType()).isPresent()
        && plugin.getConfig().getCreativeBlock().getBlocks()
        .contains(event.getTargetBlock().getState().getType())
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
            BlockState.builder().blockType(BlockTypes.AIR).build(),
            plugin.getCause()
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

          world.spawnEntity(item, plugin.getCause());
        }
      }
    }
  }
}
