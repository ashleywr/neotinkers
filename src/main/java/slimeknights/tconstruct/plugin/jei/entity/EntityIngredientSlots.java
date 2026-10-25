package slimeknights.tconstruct.plugin.jei.entity;

import java.util.List;
import java.util.Optional;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.ItemStack;

/** Keeps spawn-egg entries aligned with entities that have no egg, including players. */
final class EntityIngredientSlots {
  private EntityIngredientSlots() {}

  static List<Optional<ITypedIngredient<?>>> spawnEggs(List<ItemStack> eggs, IIngredientManager ingredientManager) {
    return eggs.stream().<Optional<ITypedIngredient<?>>>map(stack -> {
      if (stack.isEmpty()) {
        return Optional.<ITypedIngredient<?>>empty();
      }
      return ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack)
                              .<ITypedIngredient<?>>map(ingredient -> ingredient);
    }).toList();
  }
}
