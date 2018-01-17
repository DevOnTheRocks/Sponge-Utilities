package rocks.gameonthe.sponge.listener;

import java.time.Instant;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import rocks.gameonthe.sponge.GameOnTheRocks;
import rocks.gameonthe.sponge.Permissions;
import rocks.gameonthe.sponge.config.ServerRetirementConfig;

public class ServerRetirementHandler {

  private final GameOnTheRocks plugin;

  public ServerRetirementHandler(GameOnTheRocks plugin) {
    this.plugin = plugin;
  }

  @Listener(order = Order.LAST)
  public void onPlayerLogin(final ClientConnectionEvent.Login event,
      @Getter("getTargetUser") User user) {
    ServerRetirementConfig config = plugin.getConfig().getServerRetirement();
    final Instant now = Instant.now();

    if (user.hasPermission(Permissions.BYPASS_RETIREMENT)) {
      return;
    }

    if (config.getPhaseOne() != null && config.getPhaseOne().isBefore(now)) {
      if (getFirstPlayed(user).isAfter(config.getPhaseOne())) {
        event.setCancelled(true);
        event.setMessage(Text.of(TextColors.RED, "This sever is no longer accepting new players!"));
        plugin.getLogger().info(
            "{} first attempted to join the server on {} but phase 1 began on {}.",
            user.getName(),
            getFirstPlayed(user),
            config.getPhaseOne()
        );
      }
    }

    if (config.getPhaseTwo() != null && config.getPhaseTwo().isBefore(now)) {
      if (!Sponge.getServer().hasWhitelist()) {
        Sponge.getServer().setHasWhitelist(true);
      }
      event.setMessage(Text.of("This server was retired on ", config.getPhaseTwo(), "."));
      event.setMessageCancelled(false);
    }
  }

  private static Instant getFirstPlayed(User player) {
    return player.get(JoinData.class).get().firstPlayed().get();
  }
}
