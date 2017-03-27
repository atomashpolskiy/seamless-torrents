package slt;

import java.net.URL;

public interface DownloadItem {

    URL torrentUrl();

    void completeAndCleanup();
}
