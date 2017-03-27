package slt.service;

import java.io.File;
import java.util.Comparator;

class LastModifiedComparator implements Comparator<File> {
    private final boolean oldFirst;

    static LastModifiedComparator oldFirst() {
        return new LastModifiedComparator(true);
    }

    static LastModifiedComparator newFirst() {
        return new LastModifiedComparator(false);
    }

    private LastModifiedComparator(boolean oldFirst) {
        this.oldFirst = oldFirst;
    }

    @Override
    public int compare(File o1, File o2) {
        int ret = (o1.lastModified() >= o2.lastModified()) ? 1 : -1;
        return oldFirst ? ret : -ret;
    }
}
