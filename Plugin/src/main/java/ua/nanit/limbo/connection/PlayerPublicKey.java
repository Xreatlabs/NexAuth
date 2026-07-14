/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ua.nanit.limbo.connection;

public class PlayerPublicKey {

  private final long expiry;
  private final byte[] key;
  private final byte[] signature;

  public PlayerPublicKey(long expiry, byte[] key, byte[] signature) {
    this.expiry = expiry;
    this.key = key;
    this.signature = signature;
  }

  public long getExpiry() {
    return expiry;
  }

  public byte[] getKey() {
    return key;
  }

  public byte[] getSignature() {
    return signature;
  }
}
