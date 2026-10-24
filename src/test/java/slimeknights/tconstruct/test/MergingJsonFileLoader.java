package slimeknights.tconstruct.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import slimeknights.mantle.data.listener.MergingJsonDataLoader;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static org.mockito.Mockito.mock;

/**
 * Extension of {@link JsonFileLoader} with extra functionality to mock multiple data packs.
 * Used to live in Mantle's package to reach its protected members, but the 1.21 test runner loads tests as part of the
 * Tinkers module, and two modules cannot share a package, so the members are reached by reflection instead.
 * @param <B>  Builder type
 */
public class MergingJsonFileLoader<B> extends JsonFileLoader {
  private final MergingJsonDataLoader<B> dataLoader;
  private final String folder;
  private final Gson gson;
  private final Function<ResourceLocation,B> builderConstructor;
  private final Method parse;
  private final Method finishLoad;

  public MergingJsonFileLoader(MergingJsonDataLoader<B> dataLoader) {
    this(dataLoader, Internals.of(dataLoader));
  }

  private MergingJsonFileLoader(MergingJsonDataLoader<B> dataLoader, Internals<B> internals) {
    super(internals.gson, internals.folder);
    this.dataLoader = dataLoader;
    this.folder = internals.folder;
    this.gson = internals.gson;
    this.builderConstructor = internals.builderConstructor;
    this.parse = internals.parse;
    this.finishLoad = internals.finishLoad;
  }

  /**
   * Loads and parses the relevant files into the data loader
   * @param mergeFolder  If nonnull, subfolder to load as a "second datapack", for testing merging behavior. If null, skips the merging
   * @param files  List of files
   */
  public void loadAndParseFiles(@Nullable String mergeFolder, ResourceLocation... files) {
    Map<ResourceLocation,B> parsedMap = new HashMap<>();
    for (Entry<ResourceLocation, JsonElement> entry : loadFilesAsSplashlist(files).entrySet()) {
      ResourceLocation id = entry.getKey();
      invoke(parse, parsedMap.computeIfAbsent(id, builderConstructor), id, entry.getValue());
    }
    if (mergeFolder != null) {
      JsonFileLoader fakeSecondDataPack = new JsonFileLoader(gson, folder + "/" + mergeFolder);
      for (Entry<ResourceLocation, JsonElement> entry : fakeSecondDataPack.loadFilesAsSplashlist(files).entrySet()) {
        ResourceLocation id = entry.getKey();
        invoke(parse, parsedMap.computeIfAbsent(id, builderConstructor), id, entry.getValue());
      }
    }
    invoke(finishLoad, parsedMap, mock(ResourceManager.class));
  }

  private void invoke(Method method, Object... args) {
    try {
      method.invoke(dataLoader, args);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to call " + method.getName(), e);
    }
  }

  /** Protected members of the data loader */
  private record Internals<B>(Gson gson, String folder, Function<ResourceLocation,B> builderConstructor, Method parse, Method finishLoad) {
    @SuppressWarnings("unchecked")
    static <B> Internals<B> of(MergingJsonDataLoader<B> loader) {
      try {
        Class<?> base = MergingJsonDataLoader.class;
        Method parse = base.getDeclaredMethod("parse", Object.class, ResourceLocation.class, JsonElement.class);
        Method finishLoad = base.getDeclaredMethod("finishLoad", Map.class, ResourceManager.class);
        parse.setAccessible(true);
        finishLoad.setAccessible(true);
        return new Internals<>((Gson)field(base, loader, "gson"), (String)field(base, loader, "folder"),
                               (Function<ResourceLocation,B>)field(base, loader, "builderConstructor"), parse, finishLoad);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Failed to access merging data loader internals", e);
      }
    }

    private static Object field(Class<?> base, Object loader, String name) throws ReflectiveOperationException {
      Field field = base.getDeclaredField(name);
      field.setAccessible(true);
      return field.get(loader);
    }
  }
}
