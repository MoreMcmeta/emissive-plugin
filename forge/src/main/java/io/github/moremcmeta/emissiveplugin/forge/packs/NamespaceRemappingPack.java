/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moremcmeta.emissiveplugin.forge.packs;

import com.google.common.collect.ImmutableSet;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Remaps mod-specific resources to a general namespace while avoiding infinite recursion.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NamespaceRemappingPack implements PackResources {
    private final String SOURCE_NAMESPACE;
    private final String TARGET_NAMESPACE;
    private final ResourceManager RESOURCE_MANAGER;
    private final Supplier<List<PackResources>> SOURCE_PACKS;

    /**
     * Creates a new pack.
     * @param sourceNamespace       namespace to read resources from
     * @param targetNamespace       namespace that callers will use when reading from this pack
     * @param resourceManager       resource manager for Minecraft
     */
    public NamespaceRemappingPack(String sourceNamespace, String targetNamespace, ResourceManager resourceManager) {
        SOURCE_NAMESPACE = requireNonNull(sourceNamespace, "Source namespace cannot be null");
        TARGET_NAMESPACE = requireNonNull(targetNamespace, "Target namespace cannot be null");
        RESOURCE_MANAGER = requireNonNull(resourceManager, "Resource manager cannot be null");
        SOURCE_PACKS = () -> RESOURCE_MANAGER.listPacks()
                .filter((pack) -> pack.getNamespaces(PackType.CLIENT_RESOURCES).contains(sourceNamespace))
                .toList();
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... pathComponents) {
        return getResource(
                PackType.CLIENT_RESOURCES,
                new ResourceLocation(TARGET_NAMESPACE, String.join("/", pathComponents))
        );
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        if (definitelyDoesNotContain(packType, location.getNamespace())) {
            return null;
        }

        ResourceLocation fullPath = new ResourceLocation(SOURCE_NAMESPACE, location.getPath());
        return RESOURCE_MANAGER.getResource(fullPath).<IoSupplier<InputStream>>map((value) -> value::open).orElse(null);
    }

    @Override
    public void listResources(PackType packType, String namespace, String pathStart, ResourceOutput output) {
        if (definitelyDoesNotContain(packType, namespace)) {
            return;
        }

        SOURCE_PACKS.get().forEach(
                (pack) -> pack.listResources(
                        packType,
                        SOURCE_NAMESPACE,
                        pathStart,
                        (location, resource) -> output.accept(
                                new ResourceLocation(TARGET_NAMESPACE, location.getPath()),
                                resource
                        )
                )
        );
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return packType == PackType.CLIENT_RESOURCES ? ImmutableSet.of(TARGET_NAMESPACE) : ImmutableSet.of();
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        return null;
    }

    @Override
    public String packId() {
        return SOURCE_NAMESPACE;
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public void close() {}

    /**
     * Checks whether this pack definitely does not contain a resource. May contain the resource and
     * return false, but never returns true when the resource is present.
     * @param packType      pack type of the resource
     * @param namespace     namespace of the resource
     * @return whether this pack definitely does not contain a resource
     */
    private boolean definitelyDoesNotContain(PackType packType, String namespace) {
        return packType != PackType.CLIENT_RESOURCES || !TARGET_NAMESPACE.equals(namespace);
    }
}