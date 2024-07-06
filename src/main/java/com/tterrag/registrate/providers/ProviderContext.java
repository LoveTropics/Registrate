package com.tterrag.registrate.providers;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public record ProviderContext(
        PackOutput output,
        CompletableFuture<HolderLookup.Provider> registries,
        ExistingFileHelper existingFileHelper,
        Map<ProviderType<?>, RegistrateProvider> existing
) {
}
