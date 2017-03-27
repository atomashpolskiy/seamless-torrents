package slt;

import com.google.inject.Inject;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slt.service.FilesystemService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class InitCommand extends CommandWithMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitCommand.class);
    private static final String DEFAULT_CONFIG_FILE = "settings.yml";

    @Inject
    private FilesystemService filesystemService;

    public InitCommand() {
        super(CommandMetadata.builder(InitCommand.class));
    }

    @Override
    public CommandOutcome run(Cli cli) {
        createConfiguration();
        return CommandOutcome.succeeded();
    }

    private void createConfiguration() {
        File userConfig = getUserConfig();
        if (userConfig.exists()) {
            return;
        }

        try (InputStream in = InitCommand.class.getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            Files.copy(in, userConfig.toPath());
            LOGGER.info("Created default configuration: " + userConfig.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to open stream for default config file: " + DEFAULT_CONFIG_FILE, e);
        }
    }

    public File getUserConfig() {
        return new File(filesystemService.homeDir(), DEFAULT_CONFIG_FILE);
    }
}
