/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/0/.
 */

package xyz.xreatlabs.nexauth.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces platform boundary rules documented in AGENTS.md: - Paper adapter must not import
 * Velocity classes. - Velocity adapter must not import Paper classes. - Common code must not import
 * platform-specific classes.
 */
@AnalyzeClasses(packages = "xyz.xreatlabs.nexauth")
class ArchitectureBoundaryTest {

  @ArchTest
  static final ArchRule paperMustNotDependOnVelocity =
      noClasses()
          .that()
          .resideInAPackage("xyz.xreatlabs.nexauth.paper..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("xyz.xreatlabs.nexauth.velocity..")
          .because("Paper and Velocity adapters must remain separate per AGENTS.md");

  @ArchTest
  static final ArchRule velocityMustNotDependOnPaper =
      noClasses()
          .that()
          .resideInAPackage("xyz.xreatlabs.nexauth.velocity..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("xyz.xreatlabs.nexauth.paper..")
          .because("Paper and Velocity adapters must remain separate per AGENTS.md");

  @ArchTest
  static final ArchRule commonMustNotDependOnPaper =
      noClasses()
          .that()
          .resideInAPackage("xyz.xreatlabs.nexauth.common..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("xyz.xreatlabs.nexauth.paper..")
          .because("Shared common code must be platform-neutral per AGENTS.md");

  @ArchTest
  static final ArchRule commonMustNotDependOnVelocity =
      noClasses()
          .that()
          .resideInAPackage("xyz.xreatlabs.nexauth.common..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("xyz.xreatlabs.nexauth.velocity..")
          .because("Shared common code must be platform-neutral per AGENTS.md");
}
