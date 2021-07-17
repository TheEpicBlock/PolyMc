package io.github.theepicblock.polymc.datagen;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class Main implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("datagen");
    @Override
    public void onInitialize() {
        try {
            LOGGER.info("Retrieving vanilla ids");
            String output = System.getenv("output-dir");
            File outputDir = new File(output);
            outputDir.mkdirs();

            File outputFile = new File(outputDir, "block-ids");
            LOGGER.info("Output: "+outputFile.toPath().toAbsolutePath());

            Files.writeString(outputFile.toPath(), "test", StandardOpenOption.CREATE);

            System.exit(0); // Shut down the server.
        } catch (Exception e) {
            // Shutdown the server and tell Gradle it went wrongly
            System.exit(1);
        }
    }
}
