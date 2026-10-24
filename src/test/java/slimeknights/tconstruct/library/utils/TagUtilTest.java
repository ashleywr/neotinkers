package slimeknights.tconstruct.library.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagUtilTest {
  private static final BlockPos POS = new BlockPos(3, -4, 5);
  private static final BlockPos OFFSET = new BlockPos(100, 64, -200);

  @Test
  void readOptionalPos_readsWrittenPos() {
    CompoundTag tag = new CompoundTag();
    tag.put("pos", NbtUtils.writeBlockPos(POS));
    assertThat(TagUtil.readOptionalPos(tag, "pos", BlockPos.ZERO)).isEqualTo(POS);
  }

  @Test
  void readOptionalPos_appliesOffset() {
    CompoundTag tag = new CompoundTag();
    tag.put("pos", NbtUtils.writeBlockPos(POS));
    assertThat(TagUtil.readOptionalPos(tag, "pos", OFFSET)).isEqualTo(POS.offset(OFFSET));
  }

  @Test
  void readOptionalPos_readsLegacyCompound() {
    // 1.20 wrote positions as a compound
    CompoundTag pos = new CompoundTag();
    pos.putInt("X", POS.getX());
    pos.putInt("Y", POS.getY());
    pos.putInt("Z", POS.getZ());
    CompoundTag tag = new CompoundTag();
    tag.put("pos", pos);
    assertThat(TagUtil.readOptionalPos(tag, "pos", OFFSET)).isEqualTo(POS.offset(OFFSET));
  }

  @Test
  void readOptionalPos_missingIsNull() {
    assertThat(TagUtil.readOptionalPos(new CompoundTag(), "pos", OFFSET)).isNull();
  }

  @Test
  void readPos_invalidIsNull() {
    assertThat(TagUtil.readPos(StringTag.valueOf("nope"))).isNull();
    assertThat(TagUtil.readPos(new CompoundTag())).isNull();
    assertThat(TagUtil.readPos(null)).isNull();
  }
}
