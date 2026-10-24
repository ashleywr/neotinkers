package slimeknights.tconstruct.smeltery.block.entity.multiblock;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.smeltery.block.entity.controller.SmelteryBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Ensures smeltery structures survive saving and client sync, see issues #7, #28, and #29 */
class StructureDataTagTest {
  private static final BlockPos CONTROLLER = new BlockPos(10, 64, 10);
  private static final BlockPos MIN = new BlockPos(8, 63, 11);
  private static final BlockPos MAX = new BlockPos(12, 70, 15);
  private static final List<BlockPos> TANKS = List.of(new BlockPos(8, 65, 13), new BlockPos(12, 66, 14));

  private static SmelteryMultiblock multiblock() {
    return new SmelteryMultiblock(mock(SmelteryBlockEntity.class));
  }

  private static void assertMatches(StructureData data) {
    assertThat(data).isNotNull();
    assertThat(data.getMinPos()).isEqualTo(MIN);
    assertThat(data.getMaxPos()).isEqualTo(MAX);
    assertThat(data.getTanks()).containsExactlyInAnyOrderElementsOf(TANKS);
  }

  @Test
  void clientTag_roundTrip() {
    SmelteryMultiblock multiblock = multiblock();
    CompoundTag tag = multiblock.createClient(MIN, MAX, TANKS).writeClientTag(CONTROLLER);
    assertMatches(multiblock().readFromTag(tag, CONTROLLER));
  }

  @Test
  void saveTag_roundTrip() {
    SmelteryMultiblock multiblock = multiblock();
    multiblock.tanks.addAll(TANKS);
    StructureData data = multiblock.create(MIN, MAX, ImmutableSet.of(MIN.above()));
    CompoundTag tag = data.writeToTag(CONTROLLER);

    StructureData read = multiblock().readFromTag(tag, CONTROLLER);
    assertMatches(read);
    // the inside volume sizes the melting inventory, which is what the client menu used (#29)
    assertThat(read.getInnerX() * read.getInnerY() * read.getInnerZ()).isEqualTo(data.getInnerX() * data.getInnerY() * data.getInnerZ());
  }

  @Test
  void readsLegacyTag() {
    // 1.20 worlds stored each position as a compound with X, Y, and Z
    CompoundTag tag = new CompoundTag();
    tag.put(MultiblockStructureData.TAG_MIN, legacyPos(MIN.subtract(CONTROLLER)));
    tag.put(MultiblockStructureData.TAG_MAX, legacyPos(MAX.subtract(CONTROLLER)));
    ListTag tanks = new ListTag();
    for (BlockPos tank : TANKS) {
      tanks.add(legacyPos(tank.subtract(CONTROLLER)));
    }
    tag.put("tanks", tanks);
    assertMatches(multiblock().readFromTag(tag, CONTROLLER));
  }

  private static CompoundTag legacyPos(BlockPos pos) {
    CompoundTag tag = new CompoundTag();
    tag.putInt("X", pos.getX());
    tag.putInt("Y", pos.getY());
    tag.putInt("Z", pos.getZ());
    return tag;
  }
}
