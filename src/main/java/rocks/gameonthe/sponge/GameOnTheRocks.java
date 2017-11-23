package rocks.gameonthe.sponge;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.api.GriefPreventionApi;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.claim.ClaimFlag;
import me.ryanhamshire.griefprevention.api.claim.ClaimManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import rocks.gameonthe.sponge.command.CommandHelp;
import rocks.gameonthe.sponge.command.CommandRegister;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(
    id = PluginInfo.ID,
    name = PluginInfo.NAME,
    version = PluginInfo.VERSION,
    authors = PluginInfo.AUTHORS,
    description = PluginInfo.DESCRIPTION,
    dependencies = {
        @Dependency(id = "griefprevention", version = PluginInfo.GRIEFPREVENTION),
        @Dependency(id = "nucleus", version = PluginInfo.NUCLEUS),
        @Dependency(id = "luckperms", version = PluginInfo.LUCKPERMS)
    })
public class GameOnTheRocks {

    @Inject private Logger logger;

    public Logger getLogger() {
        return this.logger;
    }


    @Inject PluginContainer pluginContainer;

    private GriefPreventionApi griefPrevention;
    private LuckPermsApi luckPerms;

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
    public void onWorldLoad(LoadWorldEvent event, @Getter(value = "getTargetWorld") World world) {
        WorldProperties properties = world.getProperties();

        // Set Game Rules
        logger.info("Set default game rules in {}.", world.getName());
        properties.setGameRule("mobGriefing", "false");
        properties.setGameRule("doFireTick", "false");
        properties.setGameRule("spawnRadius", "0");
        // Turn keep-inventory on in the default world, unless SkyClaims is installed
        if (properties.getWorldName().equals(Sponge.getServer().getDefaultWorldName())
            && !Sponge.getPluginManager().isLoaded("skyclaims")) {
            properties.setGameRule("keepInventory", "true");
        }

        // Set GP Flags
        logger.info("Set default GP flags in {}.", world.getName());
        ClaimManager cm = griefPrevention.getClaimManager(properties);
        Claim wilderness = cm.getWildernessClaim();
        Subject subject = Sponge.getServiceManager().provideUnchecked(PermissionService.class).getDefaults();
        wilderness.setPermission(subject, ClaimFlag.BLOCK_PLACE, "gravestone:any", Tristate.TRUE, wilderness.getOverrideContext(), getCause());
        wilderness.setPermission(subject, ClaimFlag.BLOCK_PLACE, "tombmanygraves:any", Tristate.TRUE, wilderness.getOverrideContext(), getCause());
        wilderness.setPermission(subject, ClaimFlag.BLOCK_PLACE, "graves:any", Tristate.TRUE, wilderness.getOverrideContext(), getCause());
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

    public Cause getCause() {
        return Cause.source(pluginContainer).build();
    }
}
