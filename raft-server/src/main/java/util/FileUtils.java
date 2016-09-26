package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Comparator.reverseOrder;

public abstract class FileUtils {

	// using commons-io would be easier, but let's avoid the dependency for now
	/**
	 * Recursively deletes a directory and its contents.
	 */
	public static void deleteDirectory(Path rootPath) throws IOException {
		Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS) // return all files/directories below rootPath including linked files
		    .sorted(reverseOrder()) // sort in reverse order, so the directory itself comes after the subdirectories and files
		    .map(Path::toFile)
		    .forEach(File::delete);
	}
}
