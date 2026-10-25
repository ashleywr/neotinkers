package slimeknights.tconstruct.plugin.jei.entity;

import java.util.List;
import java.util.Optional;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntityIngredientSlotsTest {
  private record Typed<T>(IIngredientType<T> getType, T getIngredient) implements ITypedIngredient<T> {}

  @Test
  void preservesMissingEggsAtTheBeginningMiddleAndEnd() {
    var manager = mock(IIngredientManager.class);
    var cow = new ItemStack(Items.COW_SPAWN_EGG);
    var pig = new ItemStack(Items.PIG_SPAWN_EGG);
    when(manager.createTypedIngredient(VanillaTypes.ITEM_STACK, cow)).thenReturn(Optional.of(new Typed<>(VanillaTypes.ITEM_STACK, cow)));
    when(manager.createTypedIngredient(VanillaTypes.ITEM_STACK, pig)).thenReturn(Optional.of(new Typed<>(VanillaTypes.ITEM_STACK, pig)));

    var eggs = EntityIngredientSlots.spawnEggs(List.of(ItemStack.EMPTY, cow, ItemStack.EMPTY, pig, ItemStack.EMPTY), manager);
    assertEquals(5, eggs.size());
    assertTrue(eggs.get(0).isEmpty());
    assertSame(cow, eggs.get(1).orElseThrow().getIngredient());
    assertTrue(eggs.get(2).isEmpty());
    assertSame(pig, eggs.get(3).orElseThrow().getIngredient());
    assertTrue(eggs.get(4).isEmpty());
    verify(manager, never()).createTypedIngredient(VanillaTypes.ITEM_STACK, ItemStack.EMPTY);
  }

  @Test
  void playerOnlyIngredientRemainsLinkableUsingJeisActualLayoutBuilder() throws Exception {
    var manager = mock(IIngredientManager.class);
    // Reflection keeps this regression test independent of JEI's internal package at compile time.
    var type = Class.forName("mezz.jei.library.gui.recipes.supplier.builder.IngredientSupplierBuilder");
    var builder = (IRecipeLayoutBuilder) type.getConstructor(IIngredientManager.class).newInstance(manager);
    var entityType = new IIngredientType<String>() {
      @Override public Class<? extends String> getIngredientClass() { return String.class; }
    };
    @SuppressWarnings("unchecked")
    IIngredientHelper<String> helper = mock(IIngredientHelper.class);
    when(manager.getIngredientHelper(entityType)).thenReturn(helper);
    when(helper.isValidIngredient("player")).thenReturn(true);
    var entity = builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addIngredients(entityType, List.of("player"));
    var eggs = builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                      .addOptionalTypedIngredients(EntityIngredientSlots.spawnEggs(List.of(ItemStack.EMPTY), manager));
    assertEquals(1, ((List<?>) entity.getClass().getMethod("getAllIngredients").invoke(entity)).size());
    assertEquals(1, ((List<?>) eggs.getClass().getMethod("getAllIngredients").invoke(eggs)).size());
    assertDoesNotThrow(() -> builder.createFocusLink(entity, eggs));
  }
}
