package slimeknights.tconstruct.library.modifiers.fluid.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffects;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/** Ensures enchanted block breaking from datagen loads against the real enchantment registry */
class BreakBlockFluidEffectTest {
  private static final HolderLookup.Provider REGISTRIES = VanillaRegistries.createLookup();
  private static final TypedMap CONTEXT = TypedMapBuilder.builder().put(ContextKey.REGISTRIES, REGISTRIES).build();

  @Test
  void enchantments_roundTripFromDatagen() {
    // datagen has no enchantment registry, so it creates holders that only know their key
    Holder<Enchantment> silkTouch = Holder.Reference.createStandAlone(new HolderOwner<>() {}, Enchantments.SILK_TOUCH);
    JsonObject json = BreakBlockFluidEffect.LOADER.serialize(new BreakBlockFluidEffect(10, silkTouch, 1)).getAsJsonObject();

    BreakBlockFluidEffect read = BreakBlockFluidEffect.LOADER.deserialize(json, CONTEXT);
    assertThat(read.hardness()).isEqualTo(10);
    Map<Holder<Enchantment>,Integer> enchantments = read.enchantments();
    assertThat(enchantments).hasSize(1);
    Map.Entry<Holder<Enchantment>,Integer> entry = enchantments.entrySet().iterator().next();
    assertThat(entry.getKey().is(Enchantments.SILK_TOUCH)).isTrue();
    // resolved against the registry, so the effect can enchant the fake tool
    assertThat(entry.getKey().isBound()).isTrue();
    assertThat(entry.getValue()).isEqualTo(1);
  }

  @Test
  void generatedGemEffects_load() {
    for (String fluid : new String[] { "molten_emerald", "molten_diamond" }) {
      try (InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream("/data/tconstruct/tinkering/fluid_effects/" + fluid + ".json"), fluid)) {
        JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        FluidEffects effects = FluidEffects.LOADABLE.deserialize(json, CONTEXT);
        assertThat(effects.hasBlockEffects()).as(fluid).isTrue();
      } catch (java.io.IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
