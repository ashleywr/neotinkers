package slimeknights.tconstruct.library.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

/** Helpers related to Tag */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagUtil {
  /* Helper functions */

  /**
   * Reads a block position from Tag
   * @param parent  Parent tag
   * @param key     Position key
   * @param offset  Amount to offset position by
   * @return  Block position, or null if invalid or missing
   */
  @Nullable
  public static BlockPos readOptionalPos(CompoundTag parent, String key, BlockPos offset) {
    BlockPos pos = readPos(parent.get(key));
    return pos == null ? null : pos.offset(offset);
  }

  /**
   * Reads a block position from a tag written by {@link NbtUtils#writeBlockPos(BlockPos)}.
   * 1.21 writes positions as an int array, while 1.20 wrote a compound with X, Y, and Z, so both are accepted to keep worlds migrated from 1.20 intact.
   * @param tag  Tag to read
   * @return  Block position, or null if invalid or missing
   */
  @Nullable
  public static BlockPos readPos(@Nullable Tag tag) {
    if (tag instanceof IntArrayTag array) {
      int[] coords = array.getAsIntArray();
      if (coords.length == 3) {
        return new BlockPos(coords[0], coords[1], coords[2]);
      }
    } else if (tag instanceof CompoundTag compound && compound.contains("X", Tag.TAG_ANY_NUMERIC) && compound.contains("Y", Tag.TAG_ANY_NUMERIC) && compound.contains("Z", Tag.TAG_ANY_NUMERIC)) {
      return new BlockPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z"));
    }
    return null;
  }

  /**
   * Checks if the given tag is a numeric type
   * @param tag  Tag to check
   * @return  True if the type matches
   */
  public static boolean isNumeric(Tag tag) {
    byte type = tag.getId();
    return type == Tag.TAG_BYTE || type == Tag.TAG_SHORT || type == Tag.TAG_INT || type == Tag.TAG_LONG || type == Tag.TAG_FLOAT || type == Tag.TAG_DOUBLE;
  }
}
