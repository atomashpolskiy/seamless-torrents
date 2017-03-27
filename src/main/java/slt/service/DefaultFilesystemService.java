package slt.service;

import slt.Application;

import java.io.File;

public class DefaultFilesystemService implements FilesystemService {
    private static final String HOME_DIR = ".com.github.atomashpolskiy.slt";

    @Override
    public File homeDir() {
        String homeDir = userDir().getAbsolutePath() + "/" + HOME_DIR;
        return _getOrCreateDirectory(homeDir);
    }

    private static File userDir() {
        String userDir = System.getenv(Application.USER_DIR_ENVVAR);
        if (userDir == null || userDir.isEmpty()) {
            throw new IllegalStateException("User directory env variable is not specified: " + Application.USER_DIR_ENVVAR);
        }
        File f = new File(userDir);
        if (!f.exists()) {
            throw new IllegalStateException("User directory does not exist: " + userDir);
        } else if (!f.isDirectory()) {
            throw new IllegalStateException("Not a directory: " + userDir);
        }
        return f;
    }

    @Override
    public File getOrCreateDirectory(String name) {
        if (name.equals("~")) {
            name = userDir().getAbsolutePath();
        } else if (name.startsWith("~/")) {
            name = userDir().getAbsolutePath() + File.separator + name.substring(2);
        }
        return _getOrCreateDirectory(name);
    }

    private File _getOrCreateDirectory(String name) {
        File f = new File(name);
        if (f.exists()) {
            if (!f.isDirectory()) {
                throw new IllegalStateException("File is not a directory: " + name);
            }
        } else {
            if (!f.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + name);
            }
        }
        return f;
    }
}
