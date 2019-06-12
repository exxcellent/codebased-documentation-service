package data.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileReader {

	public static List<File> findFilesWithName(File srcDirectory, String fileName, String fileType) {

		List<File> files = new ArrayList<>();
		List<Path> paths = new ArrayList<>();

		try (Stream<Path> stream = Files.walk(Paths.get(srcDirectory.getAbsolutePath()), 2,
				FileVisitOption.FOLLOW_LINKS)) {
			paths = stream.filter(p -> p.endsWith(fileName + fileType)).collect(Collectors.toList());
		} catch (IOException e) {
			System.out.println("Error while searching for files: " + fileName + fileType);
			System.out.println(e.getMessage());
		}

		for (Path currentPath : paths) {
			files.add(currentPath.toFile());
		}

		if (files.isEmpty()) {
			System.out.println("No files with name: " + fileName + fileType);
		}

		return files;
	}

}
