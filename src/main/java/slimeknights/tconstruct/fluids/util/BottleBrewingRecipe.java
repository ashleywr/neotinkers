package slimeknights.tconstruct.fluids.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

/**
 * Recipe for transforming a bottle, depending on a vanilla brewing recipe to get the ingredient.
 * <p>
 * In NeoForge 1.21.1 the {@code BrewingRecipe} base class is gone and brewing recipes implement {@link IBrewingRecipe};
 * the container mixes ({@code PotionBrewing.CONTAINER_MIXES}) are no longer publicly accessible, so the ingredient must
 * be supplied directly rather than looked up from the vanilla container mix matching {@code from}/{@code to}.
 */
public class BottleBrewingRecipe implements IBrewingRecipe {
  private final Ingredient input;
  private final Ingredient ingredient;
  private final ItemStack output;
  public BottleBrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
    this.input = input;
    this.ingredient = ingredient;
    this.output = output;
  }

  /** @deprecated container-mix lookup is no longer available in 1.21; pass the ingredient explicitly instead */
  @Deprecated
  public BottleBrewingRecipe(Ingredient input, Item from, Item to, ItemStack output) {
    // 1.21 hides PotionBrewing's container mixes, so the ingredient cannot be found from the items
    this(input, Ingredient.EMPTY, output);
  }

  @Override
  public boolean isInput(ItemStack stack) {
    return input.test(stack);
  }

  @Override
  public boolean isIngredient(ItemStack stack) {
    return ingredient.test(stack);
  }

  @Override
  public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
    return isInput(input) && isIngredient(ingredient) ? output.copy() : ItemStack.EMPTY;
  }
}
