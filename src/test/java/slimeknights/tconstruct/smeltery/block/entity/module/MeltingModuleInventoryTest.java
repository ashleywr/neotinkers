package slimeknights.tconstruct.smeltery.block.entity.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer.IOreRate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Ensures large smeltery inventories survive saving and sync, see issue #29 */
class MeltingModuleInventoryTest {
  private static final HolderLookup.Provider REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

  private static MeltingModuleInventory inventory(int size) {
    return new MeltingModuleInventory(mock(MantleBlockEntity.class), mock(IFluidHandler.class), mock(IOreRate.class), size);
  }

  @Test
  void largeInventory_roundTrip() {
    // 300 slots overflowed the byte size, syncing 44 slots to the client
    MeltingModuleInventory original = inventory(300);
    original.setStackInSlot(10, new ItemStack(Items.IRON_INGOT));
    original.setStackInSlot(280, new ItemStack(Items.GOLD_INGOT));
    CompoundTag tag = original.writeToTag(REGISTRIES);

    MeltingModuleInventory read = inventory(0);
    read.readFromTag(REGISTRIES, tag);
    assertThat(read.getSlots()).isEqualTo(300);
    assertThat(read.getStackInSlot(10).getItem()).isEqualTo(Items.IRON_INGOT);
    // slot 280 used to be saved as slot 24, overwriting whatever was there
    assertThat(read.getStackInSlot(280).getItem()).isEqualTo(Items.GOLD_INGOT);
    assertThat(read.getStackInSlot(24).isEmpty()).isTrue();
  }

  @Test
  void readsLegacyByteIndexes() {
    // older saves store unsigned bytes, so 200 reads back as -56
    MeltingModuleInventory original = inventory(1);
    original.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
    CompoundTag item = ((ListTag)original.writeToTag(REGISTRIES).get("items")).getCompound(0);
    item.putByte("slot", (byte)150);
    ListTag items = new ListTag();
    items.add(item);
    CompoundTag tag = new CompoundTag();
    tag.put("items", items);
    tag.putByte("size", (byte)200);

    MeltingModuleInventory read = inventory(0);
    read.readFromTag(REGISTRIES, tag);
    assertThat(read.getSlots()).isEqualTo(200);
    assertThat(read.getStackInSlot(150).getItem()).isEqualTo(Items.IRON_INGOT);
  }
}
