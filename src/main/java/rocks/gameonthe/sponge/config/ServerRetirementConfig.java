package rocks.gameonthe.sponge.config;

import java.time.Instant;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.apache.commons.lang3.StringUtils;

@ConfigSerializable
public class ServerRetirementConfig {

  @Setting
  private boolean enabled = false;
  @Setting(value = "send-notifications")
  private boolean sendNotifications = true;
  @Setting(value = "phase-one-date", comment = "The date when only pre-existing players can join.")
  private String phaseOne = null;
  @Setting(value = "phase-two-date", comment = "The date when the server should be automatically whitelisted.")
  private String phaseTwo = null;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean sendNotifications() {
    return sendNotifications;
  }

  public Instant getPhaseOne() {
    return StringUtils.isNotBlank(phaseOne)  ? Instant.parse(phaseOne) : null;
  }

  public void setPhaseOne(Instant phaseOne) {
    this.phaseOne = phaseOne.toString();
  }

  public Instant getPhaseTwo() {
    return StringUtils.isNotBlank(phaseTwo) ? Instant.parse(phaseTwo) : null;
  }

  public void setPhaseTwo(Instant phaseTwo) {
    this.phaseTwo = phaseTwo.toString();
  }
}
