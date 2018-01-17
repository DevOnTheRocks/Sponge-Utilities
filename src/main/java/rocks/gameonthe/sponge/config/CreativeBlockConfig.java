package rocks.gameonthe.sponge.config;

import com.google.common.collect.Lists;
import java.util.List;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.block.BlockType;

@ConfigSerializable
public class CreativeBlockConfig {

  @Setting
  private boolean enabled = false;
  @Setting(value = "block-list", comment = "A list of block that allow players to break them by right-clicking with an open hand.")
  private List<BlockType> blocks = Lists.newArrayList();

  public List<BlockType> getBlocks() {
    return blocks;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
