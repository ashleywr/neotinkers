package slimeknights.tconstruct.test;

/**
 * Base class for tests that need Minecraft. Tests run through the NeoForge unit test runner, which bootstraps the game
 * and loads the mod before any test runs, so registries and the mod container are already available.
 */
public class BaseMcTest {
  /** Kept for existing callers; vanilla tiers no longer need a sorting registry in 1.21 */
  public static void setupTierSorting() {}
}
