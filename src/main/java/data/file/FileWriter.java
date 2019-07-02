package data.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import data.interfaces.DataOutputToFile;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

public class FileWriter implements DataOutputToFile {

	@Override
	public List<File> writeToFile(String content, String fileName, String fileType, File targetFolder) {
		List<File> createdFiles = new ArrayList<>();
		String subfolderName = fileType;
		File target = Paths.get(targetFolder.getAbsolutePath(), subfolderName).toFile();
		String name = fileName + "." + fileType;

		try {
			File out = createFile(target, name);

			if (fileType.equalsIgnoreCase("png") || fileType.equalsIgnoreCase("svg")) {
				SourceStringReader reader = new SourceStringReader(content);
				FileFormatOption option;
				if (fileType.equals("svg")) {
					option = new FileFormatOption(FileFormat.SVG);
				} else {
					option = new FileFormatOption(FileFormat.PNG);
				}
				final FileOutputStream outStream = new FileOutputStream(out);
				reader.generateImage(outStream, option);
				createdFiles.add(out);
				outStream.close();
			} else {
				Files.write(out.toPath(), content.getBytes());
				createdFiles.add(out);
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return createdFiles;
	}

	private File createFile(File targetFolder, String diagramName) throws IOException {

		Path path = Paths.get(targetFolder.getAbsolutePath(), diagramName);
		File out = path.toFile();

		if (out.exists()) {
			out.delete();
		}
		Files.createDirectories(targetFolder.toPath());
		Files.createFile(path);

		return out;
	}

}
