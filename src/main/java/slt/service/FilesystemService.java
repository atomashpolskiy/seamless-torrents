package slt.service;

import java.io.File;

public interface FilesystemService {

    File homeDir();

    File getOrCreateDirectory(String name);
}
