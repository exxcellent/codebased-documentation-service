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

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

public class PlantUMLFileWriter {

	public static List<File> createDiagramPngFile(Entry<String, String> descriptionEntry, File targetFolder,
			String diagramType) {

		List<File> diagramFiles = new ArrayList<>();
		File target = Paths.get(targetFolder.getAbsolutePath(), "png").toFile();
		SourceStringReader reader = new SourceStringReader(descriptionEntry.getValue());
		try {
			String name = descriptionEntry.getKey() + "_" + diagramType + "_Diagram.png";
			File out = createFile(target, name);

			reader.generateImage(out);
			diagramFiles.add(out);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return diagramFiles;
	}

	public static List<File> createDiagramSVGFile(Entry<String, String> descriptionEntry, File targetFolder,
			String diagramType) {

		List<File> diagramFiles = new ArrayList<>();
		File target = Paths.get(targetFolder.getAbsolutePath(), "svg").toFile();
		SourceStringReader reader = new SourceStringReader(descriptionEntry.getValue());
		try {
			String name = descriptionEntry.getKey() + "_" + diagramType + "_Diagram.svg";
			File out = createFile(target, name);

			final FileOutputStream outStream = new FileOutputStream(out);
			
			reader.generateImage(outStream, new FileFormatOption(FileFormat.SVG));
			diagramFiles.add(out);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return diagramFiles;
	}

	public static List<File> createDiagramDescriptionFile(Entry<String, String> descriptionEntry, File targetFolder,
			String diagramType) {
		List<File> diagramFiles = new ArrayList<>();
		File target = Paths.get(targetFolder.getAbsolutePath(), "txt").toFile();
		try {
			String name = descriptionEntry.getKey() + "_" + diagramType + "_Diagram_Description.txt";
			File outUMLCode = createFile(target, name);

			Files.write(outUMLCode.toPath(), descriptionEntry.getValue().getBytes());
			diagramFiles.add(outUMLCode);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return diagramFiles;

	}

	private static File createFile(File targetFolder, String diagramName) throws IOException {

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
