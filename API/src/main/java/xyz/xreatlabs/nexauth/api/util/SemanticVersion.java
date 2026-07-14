/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.api.util;

/**
 * A basic record to hold semantic version information.
 *
 * @param major The major version.
 * @param minor The minor version.
 * @param patch The patch version.
 * @param dev Whether this is a development version.
 * @author kyngs
 */
public record SemanticVersion(int major, int minor, int patch, boolean dev) {

  /**
   * Parses a semantic version from a string with format major.minor.patch(-SNAPSHOT|-beta)
   *
   * @param version The string to parse.
   * @return The parsed semantic version.
   */
  public static SemanticVersion parse(String version) {
    if (version == null || version.isBlank()) {
      return null;
    }

    try {
      boolean isDev =
          version.endsWith("-SNAPSHOT")
              || version.contains("-beta")
              || version.contains("-alpha")
              || version.contains("-rc");
      String cleanVersion = version.replaceAll("^v", "").replaceAll("-.*$", "");
      String[] split = cleanVersion.split("\\.");

      if (split.length < 3) {
        return null;
      }

      return new SemanticVersion(
          Integer.parseInt(split[0]),
          Integer.parseInt(split[1]),
          Integer.parseInt(split[2]),
          isDev);
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  /**
   * Compares this version with the specified version.
   *
   * @param other the version to compare to
   * @return -1 if this version is less than the specified version, 0 if they are equal, and 1 if
   *     this version is greater
   */
  public int compare(SemanticVersion other) {
    if (other == null) {
      return 1;
    }
    if (major != other.major) {
      return major < other.major ? -1 : 1;
    }
    if (minor != other.minor) {
      return minor < other.minor ? -1 : 1;
    }
    if (patch != other.patch) {
      return patch < other.patch ? -1 : 1;
    }
    return dev ? (other.dev ? 0 : -1) : (other.dev ? 1 : 0);
  }

  @Override
  public String toString() {
    return major + "." + minor + "." + patch + (dev ? "-dev" : "");
  }
}
