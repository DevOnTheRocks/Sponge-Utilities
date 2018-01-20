package rocks.gameonthe.sponge.config;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;

@ConfigSerializable
public class CreativeBlockConfig {

  @Setting
  private boolean enabled = false;
  @Setting(value = "block-list", comment = "A list of block that allow players to break them by right-clicking with an open hand.")
  private List<String> blocks = Lists.newArrayList();

  public List<BlockType> getBlocks() {
    return blocks.stream()
        .filter(b -> Sponge.getRegistry().getType(BlockType.class, b).isPresent())
        .map(b -> Sponge.getRegistry().getType(BlockType.class, b).get())
        .collect(Collectors.toList());
  }

  public void addBlock(BlockType type) {
    blocks.add(type.getName());
  }

  public void removeBlock(BlockType type) {
    blocks.remove(type.getName());
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
