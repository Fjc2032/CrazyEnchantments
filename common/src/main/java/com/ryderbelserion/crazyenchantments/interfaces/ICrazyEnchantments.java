package com.ryderbelserion.crazyenchantments.interfaces;

import com.ryderbelserion.crazyenchantments.objects.ConfigOptions;
import com.ryderbelserion.fusion.core.files.FileManager;
import java.nio.file.Path;

public interface ICrazyEnchantments {

    FileManager getFileManager();

    ConfigOptions getOptions();

    Path getPath();

    void init();

    void reload();

}