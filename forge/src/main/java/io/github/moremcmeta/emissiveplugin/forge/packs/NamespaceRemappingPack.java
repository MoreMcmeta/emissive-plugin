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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Remaps mod-specific resources to a general namespace while avoiding infinite recursion.
 * @author soir20
 */
@ParametersAreNonnullByDefault
public class NamespaceRemappingPack implements PackResources {
    private final String NAME;
    private final String SOURCE_NAMESPACE;
    private final String TARGET_NAMESPACE;
    private final ResourceManager RESOURCE_MANAGER;
    private final Supplier<List<PackResources>> SOURCE_PACKS;

    /**
     * Creates a new pack.
     * @param displayName           pack's display name
     * @param sourceNamespace       namespace to read resources from
     * @param targetNamespace       namespace that callers will use when reading from this pack
     * @param resourceManager       resource manager for Minecraft
     */
    public NamespaceRemappingPack(String displayName, String sourceNamespace, String targetNamespace,
                                  ResourceManager resourceManager) {
        NAME = requireNonNull(displayName, "Display name cannot be null");
        SOURCE_NAMESPACE = requireNonNull(sourceNamespace, "Source namespace cannot be null");
        TARGET_NAMESPACE = requireNonNull(targetNamespace, "Target namespace cannot be null");
        RESOURCE_MANAGER = requireNonNull(resourceManager, "Resource manager cannot be null");
        SOURCE_PACKS = () -> RESOURCE_MANAGER.listPacks()
                .filter((pack) -> pack.getNamespaces(PackType.CLIENT_RESOURCES).contains(sourceNamespace))
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public InputStream getRootResource(String pathComponents) throws IOException {
        return getResource(
                PackType.CLIENT_RESOURCES,
                new ResourceLocation(TARGET_NAMESPACE, String.join("/", pathComponents))
        );
    }

    @Override
    public InputStream getResource(PackType packType, ResourceLocation location) throws IOException {
        if (definitelyDoesNotContain(packType, location.getNamespace())) {
            throw new IOException("Resource not found in " + SOURCE_NAMESPACE + ": " + location);
        }

        ResourceLocation fullPath = new ResourceLocation(SOURCE_NAMESPACE, location.getPath());
        Optional<Resource> resource = Optional.ofNullable(RESOURCE_MANAGER.getResource(fullPath));
        if (resource.isPresent()) {
            return resource.get().getInputStream();
        }

        throw new IOException("Resource not found in " + SOURCE_NAMESPACE + ": " + location);
    }

    @Override
    public boolean hasResource(PackType packType, ResourceLocation location) {
        return SOURCE_PACKS.get().stream()
                .anyMatch((pack) -> pack.hasResource(packType, new ResourceLocation(SOURCE_NAMESPACE, location.getPath())));
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String namespace, String pathStart, int depth,
                                                     Predicate<String> predicate) {
        if (definitelyDoesNotContain(packType, namespace)) {
            return ImmutableList.of();
        }

        return SOURCE_PACKS.get().stream().flatMap(
                (pack) -> pack.getResources(
                        packType,
                        SOURCE_NAMESPACE,
                        pathStart,
                        depth,
                        predicate
                ).stream()
        ).map((location) -> new ResourceLocation(TARGET_NAMESPACE, location.getPath())).collect(Collectors.toSet());
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
    public String getName() {
        return NAME;
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
