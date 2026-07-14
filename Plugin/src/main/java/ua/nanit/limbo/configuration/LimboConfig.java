/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.configuration;

import java.net.SocketAddress;
import ua.nanit.limbo.server.data.BossBar;
import ua.nanit.limbo.server.data.InfoForwarding;
import ua.nanit.limbo.server.data.PingData;
import ua.nanit.limbo.server.data.Title;

public interface LimboConfig {
  SocketAddress getAddress();

  int getMaxPlayers();

  PingData getPingData();

  String getDimensionType();

  int getGameMode();

  boolean isSecureProfile();

  InfoForwarding getInfoForwarding();

  long getReadTimeout();

  int getDebugLevel();

  boolean isUseBrandName();

  boolean isUseJoinMessage();

  boolean isUseBossBar();

  boolean isUseTitle();

  boolean isUsePlayerList();

  boolean isUseHeaderAndFooter();

  String getBrandName();

  String getJoinMessage();

  BossBar getBossBar();

  Title getTitle();

  String getPlayerListUsername();

  String getPlayerListHeader();

  String getPlayerListFooter();

  boolean isUseEpoll();

  int getBossGroupSize();

  int getWorkerGroupSize();

  default boolean isUseTrafficLimits() {
    return false;
  }

  default int getMaxPacketSize() {
    return -1;
  }

  default int getMaxPacketsPerSec() {
    return -1;
  }

  default int getMaxBytesPerSec() {
    return -1;
  }

  double getInterval();

  double getMaxPacketRate();
}
