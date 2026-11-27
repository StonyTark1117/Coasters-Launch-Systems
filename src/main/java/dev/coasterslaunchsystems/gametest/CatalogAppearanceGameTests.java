package dev.coasterslaunchsystems.gametest;

import java.util.List;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** Separate selection lets the native exclusion proof run before positive certificate expansion. */
@GameTestHolder("coasterslaunchsystems_catalog_appearance")
public final class CatalogAppearanceGameTests {
  @GameTestGenerator public static List<TestFunction> appearances() {
    if (!net.neoforged.fml.ModList.get().isLoaded("fourthdimension") || "boundary".equals(System.getenv("LS_CATALOG_PHASE"))) return List.of();
    return List.of(false,true).stream().map(inverted -> new TestFunction("ls_catalog_body",
        "coasterslaunchsystems_catalog_appearance."+(inverted?"fixed_inverted":"wing"),
        "coasterslaunchsystems_catalog:empty",260,0,true,
        h->CatalogBoundaryGameTests.appearance(h,inverted))).toList();
  }
}
