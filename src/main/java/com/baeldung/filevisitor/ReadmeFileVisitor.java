package com.baeldung.filevisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.common.GlobalConstants;

public class ReadmeFileVisitor extends SimpleFileVisitor<Path> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private List<String> reameList = new ArrayList<>();

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.toString().equalsIgnoreCase(GlobalConstants.tutorialsRepoLocalPath + "/.git/") || dir.toString().equalsIgnoreCase(GlobalConstants.tutorialsRepoLocalPath + "/.git")) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {

        File file = path.toFile();
        if (file.isFile() && file.getName().toLowerCase().endsWith(GlobalConstants.README_FILE_NAME_LOWERCASE)) {
            logger.info("redme found {}", path);
            reameList.add(path.toString());
        }
        return FileVisitResult.CONTINUE;
    }

    public List<String> getReameList() {
        return reameList;
    }

}
