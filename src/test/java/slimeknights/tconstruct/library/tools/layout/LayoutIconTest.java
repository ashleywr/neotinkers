package slimeknights.tconstruct.library.tools.layout;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.library.tools.layout.LayoutIcon.ItemStackIcon;
import slimeknights.tconstruct.library.tools.layout.LayoutIcon.PatternIcon;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.test.TestHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LayoutIconTest extends BaseMcTest {
  /* Empty */

  @Test
  void empty_getValue_isEmpty() {
    assertThat(LayoutIcon.EMPTY.getValue(ItemStack.class)).isNull();
    assertThat(LayoutIcon.EMPTY.getValue(Pattern.class)).isNull();
  }

  @Test
  void empty_bufferReadWrite() {
    RegistryFriendlyByteBuf buffer = TestHelper.buffer();
    LayoutIcon.EMPTY.write(buffer);

    LayoutIcon decoded = LayoutIcon.read(buffer);
    assertThat(decoded).isSameAs(LayoutIcon.EMPTY);
  }

  @Test
  void empty_jsonSerialize() {
    JsonObject json = LayoutIcon.EMPTY.toJson();
    assertThat(json.entrySet()).isEmpty();
  }

  @Test
  void empty_jsonDeserialize() {
    JsonObject json = new JsonObject();
    LayoutIcon parsed = LayoutIcon.SERIALIZER.deserialize(json, LayoutIcon.class, mock(JsonDeserializationContext.class));
    assertThat(parsed).isSameAs(LayoutIcon.EMPTY);
  }


  /* Item */

  @Test
  void item_getValue_hasSameItem() {
    ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
    LayoutIcon itemIcon = LayoutIcon.ofItem(stack);
    ItemStack contained = itemIcon.getValue(ItemStack.class);
    assertThat(contained).isNotNull();
    assertThat(contained).isSameAs(stack);
    assertThat(itemIcon.getValue(Pattern.class)).isNull();
  }

  @Test
  void item_bufferReadWrite() {
    ItemStack original = new ItemStack(Items.DIAMOND_PICKAXE);
    LayoutIcon itemIcon = LayoutIcon.ofItem(original);
    RegistryFriendlyByteBuf buffer = TestHelper.buffer();
    itemIcon.write(buffer);

    LayoutIcon decoded = LayoutIcon.read(buffer);
    assertThat(decoded).isInstanceOf(ItemStackIcon.class);
    ItemStack stack = decoded.getValue(ItemStack.class);
    assertThat(stack).isNotNull();
    assertThat(ItemStack.matches(original, stack)).isTrue();
  }

  @Test
  void item_jsonSerialize() {
    // 1.21 stacks have no NBT by default, so give it custom data to serialize
    ItemStack original = new ItemStack(Items.DIAMOND_PICKAXE);
    CompoundTag data = new CompoundTag();
    data.putInt("test", 1);
    original.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    LayoutIcon itemIcon = LayoutIcon.ofItem(original);
    JsonObject json = itemIcon.toJson();
    assertThat(json.entrySet()).hasSize(2);
    assertThat(GsonHelper.getAsString(json, "item")).isEqualTo(BuiltInRegistries.ITEM.getKey(Items.DIAMOND_PICKAXE).toString());
    // NBT is written as a JSON object, while strings are still accepted on read
    assertThat(GsonHelper.getAsJsonObject(json, "nbt").get("test").getAsInt()).isEqualTo(1);
  }

  @Test
  void item_jsonDeserialize() {
    JsonObject json = new JsonObject();
    json.addProperty("item", BuiltInRegistries.ITEM.getKey(Items.DIAMOND).toString());
    json.addProperty("nbt", "{test:1}");
    LayoutIcon icon = LayoutIcon.SERIALIZER.deserialize(json, LayoutIcon.class, mock(JsonDeserializationContext.class));
    assertThat(icon).isInstanceOf(ItemStackIcon.class);
    ItemStack stack = icon.getValue(ItemStack.class);
    assertThat(stack).isNotNull();
    assertThat(stack.getItem()).isEqualTo(Items.DIAMOND);
    CompoundTag nbt = TestHelper.customData(stack);
    assertThat(nbt).isNotNull();
    assertThat(nbt.getInt("test")).isEqualTo(1);
  }


  /* Pattern */

  @Test
  void pattern_getValue_hasSamePattern() {
    Pattern pattern = new Pattern("test:the_pattern");
    LayoutIcon itemIcon = LayoutIcon.ofPattern(pattern);
    Pattern contained = itemIcon.getValue(Pattern.class);
    assertThat(contained).isNotNull();
    assertThat(contained).isEqualTo(pattern);
    assertThat(itemIcon.getValue(ItemStack.class)).isNull();
  }

  @Test
  void pattern_bufferReadWrite() {
    Pattern pattern = new Pattern("test:the_pattern");
    LayoutIcon icon = LayoutIcon.ofPattern(pattern);
    RegistryFriendlyByteBuf buffer = TestHelper.buffer();
    icon.write(buffer);

    LayoutIcon decoded = LayoutIcon.read(buffer);
    assertThat(decoded).isInstanceOf(PatternIcon.class);
    Pattern contained = decoded.getValue(Pattern.class);
    assertThat(contained).isNotNull();
    assertThat(contained).isEqualTo(pattern);
  }

  @Test
  void pattern_jsonSerialize() {
    Pattern pattern = new Pattern("test:the_pattern");
    LayoutIcon icon = LayoutIcon.ofPattern(pattern);
    JsonObject json = icon.toJson();
    assertThat(json.entrySet()).hasSize(1);
    assertThat(GsonHelper.getAsString(json, "pattern")).isEqualTo(pattern.toString());
  }

  @Test
  void pattern_jsonDeserialize() {
    JsonObject json = new JsonObject();
    json.addProperty("pattern", "test:json_pattern");
    LayoutIcon parsed = LayoutIcon.SERIALIZER.deserialize(json, LayoutIcon.class, mock(JsonDeserializationContext.class));
    Pattern contained = parsed.getValue(Pattern.class);
    assertThat(contained).isNotNull();
    assertThat(contained).isEqualTo(new Pattern("test:json_pattern"));
  }
}
