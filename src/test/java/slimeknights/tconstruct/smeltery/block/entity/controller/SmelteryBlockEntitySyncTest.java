package slimeknights.tconstruct.smeltery.block.entity.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.SmelteryMultiblock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Sends a large smeltery through the save and client sync paths, see issues #7, #28, and #29 */
class SmelteryBlockEntitySyncTest {
  private static final HolderLookup.Provider REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
  private static final BlockPos CONTROLLER = new BlockPos(0, 64, 0);
  // inside is 5x12x5 = 300 blocks above the floor, more than fit in a byte
  private static final BlockPos MIN = new BlockPos(-3, 63, 1);
  private static final BlockPos MAX = new BlockPos(3, 75, 7);
  private static final List<BlockPos> TANKS = List.of(new BlockPos(-3, 65, 4));

  private static SmelteryBlockEntity create() {
    return new SmelteryBlockEntity(CONTROLLER, TinkerSmeltery.smelteryController.get().defaultBlockState());
  }

  /** Creates a formed smeltery holding an item in a high slot */
  private static SmelteryBlockEntity formed() {
    SmelteryBlockEntity smeltery = create();
    StructureData structure = new SmelteryMultiblock(smeltery).createClient(MIN, MAX, TANKS);
    smeltery.setStructure(structure);
    smeltery.getMeltingInventory().setStackInSlot(280, new ItemStack(Items.IRON_INGOT));
    return smeltery;
  }

  private static void assertMatches(SmelteryBlockEntity original, SmelteryBlockEntity read) {
    assertThat(read.getStructure()).isNotNull();
    assertThat(read.getStructure().getTanks()).containsExactlyElementsOf(TANKS);
    // the client menu adds one slot per inventory slot, a smaller client inventory disconnected the player
    assertThat(read.getMeltingInventory().getSlots()).isEqualTo(original.getMeltingInventory().getSlots()).isEqualTo(300);
    assertThat(read.getMeltingInventory().getStackInSlot(280).getItem()).isEqualTo(Items.IRON_INGOT);
    assertThat(read.getTank().getCapacity()).isEqualTo(original.getTank().getCapacity());
  }

  @Test
  void clientSync_keepsStructure() {
    SmelteryBlockEntity server = formed();
    CompoundTag tag = server.getUpdateTag(REGISTRIES);

    SmelteryBlockEntity client = create();
    client.handleUpdateTag(tag, REGISTRIES);
    assertMatches(server, client);
  }

  @Test
  void save_keepsStructure() {
    SmelteryBlockEntity saved = formed();
    CompoundTag tag = saved.saveWithoutMetadata(REGISTRIES);

    SmelteryBlockEntity loaded = create();
    loaded.loadWithComponents(tag, REGISTRIES);
    assertMatches(saved, loaded);
  }
}
