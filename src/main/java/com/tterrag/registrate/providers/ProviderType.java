package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;
import com.tterrag.registrate.util.nullness.FieldsAreNonnullByDefault;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a type of data that can be generated, and specifies a factory for the provider.
 * <p>
 * Used as a key for data generator callbacks.
 * <p>
 * This file also defines the built-in provider types, but third-party types can be created with {@link #register(String, ProviderType)}.
 *
 * @param <T>
 *            The type of the provider
 */
@FunctionalInterface
@SuppressWarnings("deprecation")
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public interface ProviderType<T extends RegistrateProvider> {

    // SERVER DATA
    public static final ProviderType<RegistrateRecipeProvider> RECIPE = register("recipe", (p, c) -> new RegistrateRecipeProvider(p, c.output(), c.registries()));
    public static final ProviderType<RegistrateAdvancementProvider> ADVANCEMENT = register("advancement", (p, c) -> new RegistrateAdvancementProvider(p, c.output(), c.registries()));
    public static final ProviderType<RegistrateLootTableProvider> LOOT = register("loot", (p, c) -> new RegistrateLootTableProvider(p, c.output(), c.registries()));
    public static final ProviderType<RegistrateTagsProvider.IntrinsicImpl<Block>> BLOCK_TAGS = register("tags/block", type -> (p, c) -> new RegistrateTagsProvider.IntrinsicImpl<Block>(p, type, "blocks", c.output(), Registries.BLOCK, c.registries(), block -> block.builtInRegistryHolder().key(), c.existingFileHelper()));
    public static final ProviderType<RegistrateItemTagsProvider> ITEM_TAGS = registerDelegate("tags/item", type -> (p, c) -> new RegistrateItemTagsProvider(p, type, "items", c.output(), c.registries(), ((TagsProvider<Block>) c.existing().get(BLOCK_TAGS)).contentsGetter(), c.existingFileHelper()));
    public static final ProviderType<RegistrateTagsProvider.IntrinsicImpl<Fluid>> FLUID_TAGS = register("tags/fluid", type -> (p, c) -> new RegistrateTagsProvider.IntrinsicImpl<Fluid>(p, type, "fluids", c.output(), Registries.FLUID, c.registries(), fluid -> fluid.builtInRegistryHolder().key(), c.existingFileHelper()));
    public static final ProviderType<RegistrateTagsProvider.IntrinsicImpl<EntityType<?>>> ENTITY_TAGS = register("tags/entity", type -> (p, c) -> new RegistrateTagsProvider.IntrinsicImpl<EntityType<?>>(p, type, "entity_types", c.output(), Registries.ENTITY_TYPE, c.registries(), entityType -> entityType.builtInRegistryHolder().key(), c.existingFileHelper()));
    public static final ProviderType<RegistrateGenericProvider> GENERIC_SERVER = ProviderType.register("registrate_generic_server_provider", type -> (p, c) -> new RegistrateGenericProvider(p, c, LogicalSide.SERVER, type));

    // CLIENT DATA
    public static final ProviderType<RegistrateBlockstateProvider> BLOCKSTATE = register("blockstate", (p, c) -> new RegistrateBlockstateProvider(p, c.output(), c.existingFileHelper()));
    public static final ProviderType<RegistrateItemModelProvider> ITEM_MODEL = register("item_model", (p, c) -> new RegistrateItemModelProvider(p, c.output(), ((RegistrateBlockstateProvider) c.existing().get(BLOCKSTATE)).getExistingFileHelper()));
    public static final ProviderType<RegistrateLangProvider> LANG = register("lang", (p, e) -> new RegistrateLangProvider(p, e.output()));
    public static final ProviderType<RegistrateGenericProvider> GENERIC_CLIENT = ProviderType.register("registrate_generic_client_provider", type -> (p, c) -> new RegistrateGenericProvider(p, c, LogicalSide.CLIENT, type));

    T create(AbstractRegistrate<?> parent, ProviderContext context);

    // TODO this is clunky af
    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> registerDelegate(String name, NonNullUnaryOperator<ProviderType<T>> type) {
        ProviderType<T> ret = new ProviderType<T>() {

            @Override
            public T create(@Nonnull AbstractRegistrate<?> parent, ProviderContext context) {
                return type.apply(this).create(parent, context);
            }
        };
        return register(name, ret);
    }

    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> register(String name, NonNullFunction<ProviderType<T>, NonNullBiFunction<AbstractRegistrate<?>, ProviderContext, T>> type) {
        ProviderType<T> ret = new ProviderType<T>() {

            @Override
            public T create(@Nonnull AbstractRegistrate<?> parent, ProviderContext context) {
                return type.apply(this).apply(parent, context);
            }
        };
        return register(name, ret);
    }

    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> register(String name, ProviderType<T> type) {
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }
}
