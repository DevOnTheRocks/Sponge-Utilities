package rocks.gameonthe.sponge.listener;

import me.ryanhamshire.griefprevention.api.GriefPreventionApi;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.claim.ClaimManager;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class NatureCoreHandler {

  private final GriefPreventionApi gp;
  private final String NATURE_CORE = "randomthings:naturecore";

  public NatureCoreHandler(GriefPreventionApi gp) {
    this.gp = gp;
  }

  @Listener
  public void onInteractBlock(InteractBlockEvent.Secondary event, @First Player player) {
    // Check if the target is a Nature Core and if the player's hand is empty
    if (event.getTargetBlock().getState().getType().getName().equalsIgnoreCase(NATURE_CORE)
        && event.getTargetBlock().getLocation().isPresent()
        && !player.getItemInHand(event.getHandType()).isPresent()) {

      Location<World> location = event.getTargetBlock().getLocation().get();
      World world = location.getExtent();
      ClaimManager manager = gp.getClaimManager(world);
      Claim claim = manager.getClaimAt(location);

      if (claim.isWilderness() || claim.isTrusted(player.getUniqueId())) {
        world.setBlock(
            location.getBlockPosition(),
            BlockState.builder().blockType(BlockTypes.AIR).build()
        );
        player.offer(
            Keys.REPRESENTED_BLOCK,
            event.getTargetBlock().getState()
        );
      }
    }
  }
}
