package com.ryderbelserion.crazyenchantments.enums;

import com.ryderbelserion.fusion.core.FusionCore;
import com.ryderbelserion.fusion.core.files.FileManager;
import com.ryderbelserion.fusion.core.files.types.YamlCustomFile;
import com.ryderbelserion.fusion.kyori.FusionKyori;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import java.nio.file.Path;
import java.util.Objects;

public enum FileKeys {

    config("config.yml");

    private final FusionKyori kyori = (FusionKyori) FusionCore.Provider.get();
    private final FileManager fileManager = this.kyori.getFileManager();
    private final Path path = this.kyori.getPath();

    private final Path relativePath;

    FileKeys(@NotNull final String fileName) {
        this.relativePath = this.path.resolve(fileName);
    }

    public @NotNull final CommentedConfigurationNode getConfig() {
        return getCustomFile().getConfiguration();
    }

    public @NotNull final YamlCustomFile getCustomFile() {
        return Objects.requireNonNull(this.fileManager.getYamlFile(this.relativePath));
    }

    public @NotNull final Path getPath() {
        return this.relativePath;
    }
}