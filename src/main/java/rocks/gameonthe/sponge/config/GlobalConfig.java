package rocks.gameonthe.sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class GlobalConfig {

  @Setting(value = "creative-block")
  private CreativeBlockConfig creativeBlock = new CreativeBlockConfig();
  @Setting(value = "server-retirement")
  private ServerRetirementConfig serverRetirement = new ServerRetirementConfig();

  public CreativeBlockConfig getCreativeBlock() {
    return creativeBlock;
  }

  public ServerRetirementConfig getServerRetirement() {
    return serverRetirement;
  }
}
