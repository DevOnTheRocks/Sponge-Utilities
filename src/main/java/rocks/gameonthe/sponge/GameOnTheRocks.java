package rocks.gameonthe.sponge;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.api.GriefPreventionApi;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.claim.ClaimFlag;
import me.ryanhamshire.griefprevention.api.claim.ClaimManager;
import me.ryanhamshire.griefprevention.api.claim.ClaimResult;
import me.ryanhamshire.griefprevention.api.claim.ClaimType;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.world.ConstructWorldPropertiesEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import rocks.gameonthe.sponge.command.CommandHelp;
import rocks.gameonthe.sponge.command.CommandRegister;

@Plugin(
    id = PluginInfo.ID,
    name = PluginInfo.NAME,
    version = PluginInfo.VERSION,
    authors = PluginInfo.AUTHORS,
    description = PluginInfo.DESCRIPTION,
    dependencies = {
        @Dependency(id = "griefprevention", version = PluginInfo.GRIEFPREVENTION),
        //@Dependency(id = "nucleus", version = PluginInfo.NUCLEUS),
        @Dependency(id = "luckperms", version = PluginInfo.LUCKPERMS)
    })
public class GameOnTheRocks {

  @Inject
  private Logger logger;

  public Logger getLogger() {
    return this.logger;
  }


  @Inject
  PluginContainer pluginContainer;

  private GriefPreventionApi griefPrevention;
  private LuckPermsApi luckPerms;

  private Set<WorldProperties> newWorlds = Sets.newHashSet();

  @Listener
  public void onPostInitialization(GamePostInitializationEvent event) {
    getLogger().info("Plugin has begun initialization.");
    griefPrevention = GriefPrevention.getApi();
    luckPerms = LuckPerms.getApi();
  }

  @Listener
  public void onServerAboutToStart(GameAboutToStartServerEvent event) {
    // registerCommands();
    getLogger().info("Initialization complete.");
  }

  @Listener
  public void onConstructWorldProperties(ConstructWorldPropertiesEvent event,
      @Getter(value = "getWorldProperties") WorldProperties properties) {
    if (!properties.isInitialized()) {
      newWorlds.add(properties);
    }
  }

  @Listener
  public void onWorldLoad(LoadWorldEvent event, @Getter(value = "getTargetWorld") World world) {
    WorldProperties properties = world.getProperties();
    ClaimManager cm = griefPrevention.getClaimManager(properties);
    boolean defaultWorld = properties.getWorldName()
        .equals(Sponge.getServer().getDefaultWorldName());

    // For new worlds only
    if (newWorlds.contains(properties)) {
      // Set world spawn
      int y = world.getExtentView(
          Vector3i.from(0, 0, 0),
          Vector3i.from(0, 255, 0)
      ).getBlockMax().getY();
      properties.setSpawnPosition(Vector3i.from(0, y, 0));
    }

    // Set Game Rules
    logger.info("Set default game rules in {}.", world.getName());
    properties.setGameRule("mobGriefing", "false");
    properties.setGameRule("doFireTick", "false");
    properties.setGameRule("spawnRadius", "0");
    // Turn keep-inventory on in the default world, unless SkyClaims is installed
    if (defaultWorld && !Sponge.getPluginManager().isLoaded("skyclaims")) {
      properties.setGameRule("keepInventory", "true");
    }

    // Set GP Flags
    logger.info("Set default GP flags in {}.", world.getName());
    Claim wilderness = cm.getWildernessClaim();
    Context context = wilderness.getOverrideContext();
    List<String> gravestones = Lists
        .newArrayList("gravestone:any", "tombmanygraves:any", "graves:any");
    gravestones.forEach(g -> {
      wilderness.setPermission(ClaimFlag.BLOCK_PLACE, g, Tristate.TRUE, context);
      wilderness.setPermission(ClaimFlag.BLOCK_BREAK, g, Tristate.TRUE, context);
    });

    // Spawn claim
    if (defaultWorld && !cm.getClaimAt(world.getSpawnLocation()).isAdminClaim()) {
      ClaimResult result;
      do {
        result = Claim.builder()
            .type(ClaimType.ADMIN)
            .world(world)
            .bounds(
                Vector3i.from(512, 0, 512),
                Vector3i.from(-512, 255, -512)
            )
            .build();
        switch (result.getResultType()) {
          case CLAIM_ALREADY_EXISTS:
          case OVERLAPPING_CLAIM:
            result.getClaims().forEach(cm::deleteClaim);
            break;
          case SUCCESS:
            Claim claim = result.getClaim().get();
            claim.setPermission(ClaimFlag.ENTITY_SPAWN, Tristate.FALSE, claim.getContext());
            claim.setPermission(ClaimFlag.ENTITY_DAMAGE, Tristate.FALSE, claim.getContext());
            claim.setPermission(ClaimFlag.PORTAL_USE, Tristate.TRUE, claim.getContext());
            logger.info("Successfully created spawn claim.");
            break;
          default:
            logger.info("Error creating spawn claim {}.\n{}",
                result.getResultType(),
                result.getMessage().orElse(Text.of("No message provided.")).toPlain()
            );
            return;
        }
      } while (!result.successful());
    }
  }

  @Listener
  public void onServerStopping(GameStoppingServerEvent event) {
    getLogger().info("Plugin has begun shutdown preparation.");
    // TODO
    getLogger().info("Shutdown actions complete.");
  }

  public void registerCommands() {
    Sponge.getCommandManager().register(this, CommandSpec.builder()
        .description(Text.of("Help Command"))
        .permission(Permissions.COMMAND_HELP)
        .executor(new CommandHelp())
        .build(), "gotrhelp");

    Sponge.getCommandManager().register(this, CommandSpec.builder()
        .description(Text.of("Allows you to register"))
        .permission(Permissions.COMMAND_REGISTER)
        .executor(new CommandRegister())
        .build(), "register");
  }
}
