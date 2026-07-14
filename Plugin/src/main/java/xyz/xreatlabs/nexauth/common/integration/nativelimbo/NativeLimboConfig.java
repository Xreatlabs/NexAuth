/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common.integration.nativelimbo;

import java.net.SocketAddress;
import ua.nanit.limbo.configuration.LimboConfig;
import ua.nanit.limbo.server.data.BossBar;
import ua.nanit.limbo.server.data.InfoForwarding;
import ua.nanit.limbo.server.data.PingData;
import ua.nanit.limbo.server.data.Title;

public class NativeLimboConfig implements LimboConfig {

  private final PingData pingData;
  private final SocketAddress address;
  private final InfoForwarding forwarding;

  public NativeLimboConfig(SocketAddress address, InfoForwarding forwarding) {
    this.pingData = new PingData();
    this.pingData.setDescription("NexAuth Limbo");
    this.pingData.setVersion("NexAuth");
    this.address = address;
    this.forwarding = forwarding;
  }

  @Override
  public SocketAddress getAddress() {
    return address;
  }

  @Override
  public int getMaxPlayers() {
    return 100;
  }

  @Override
  public PingData getPingData() {
    return pingData;
  }

  @Override
  public String getDimensionType() {
    return "the_end";
  }

  @Override
  public int getGameMode() {
    return 2;
  }

  @Override
  public boolean isSecureProfile() {
    return false;
  }

  @Override
  public InfoForwarding getInfoForwarding() {
    return forwarding;
  }

  @Override
  public long getReadTimeout() {
    return 30000;
  }

  @Override
  public int getDebugLevel() {
    return 0;
  }

  @Override
  public boolean isUseBrandName() {
    return false;
  }

  @Override
  public boolean isUseJoinMessage() {
    return false;
  }

  @Override
  public boolean isUseBossBar() {
    return false;
  }

  @Override
  public boolean isUseTitle() {
    return false;
  }

  @Override
  public boolean isUsePlayerList() {
    return false;
  }

  @Override
  public boolean isUseHeaderAndFooter() {
    return false;
  }

  @Override
  public String getBrandName() {
    return "NexAuth";
  }

  @Override
  public String getJoinMessage() {
    return "";
  }

  @Override
  public BossBar getBossBar() {
    return null;
  }

  @Override
  public Title getTitle() {
    return null;
  }

  @Override
  public String getPlayerListUsername() {
    return "";
  }

  @Override
  public String getPlayerListHeader() {
    return "";
  }

  @Override
  public String getPlayerListFooter() {
    return "";
  }

  @Override
  public boolean isUseEpoll() {
    return true;
  }

  @Override
  public int getBossGroupSize() {
    return 1;
  }

  @Override
  public int getWorkerGroupSize() {
    return 2;
  }

  @Override
  public boolean isUseTrafficLimits() {
    return false;
  }

  @Override
  public int getMaxPacketSize() {
    return -1;
  }

  @Override
  public double getInterval() {
    return -1;
  }

  @Override
  public double getMaxPacketRate() {
    return -1;
  }
}
