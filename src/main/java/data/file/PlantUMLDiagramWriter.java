package data.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

public class PlantUMLDiagramWriter {

	public static final String CHARSET = "UTF-8";

	public Map<String, String> generateSVGString(Map<String, String> fileNameToContent) {
		Map<String, String> generatedOutput = new HashMap<>();

		for (Entry<String, String> entry : fileNameToContent.entrySet()) {
			String[] splitFileName = entry.getKey().split("\\.");
			String fileNameBase = splitFileName[0];

			SourceStringReader reader = new SourceStringReader(entry.getValue());
			FileFormatOption option = new FileFormatOption(FileFormat.SVG);

			try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
				reader.generateImage(outStream, option);
				generatedOutput.put(fileNameBase + ".svg", outStream.toString(CHARSET));
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return generatedOutput;
	}

	public List<File> generateSVGFile(Map<String, String> fileNameToContent, File targetFolder) {
		List<File> generatedOutput = new ArrayList<>();

		for (Entry<String, String> entry : fileNameToContent.entrySet()) {
			String[] splitFileName = entry.getKey().split("\\.");
			String fileNameBase = splitFileName[0];
			String fileType = "svg";

			String subfolderName = fileType;
			File target = Paths.get(targetFolder.getAbsolutePath(), subfolderName).toFile();

			SourceStringReader reader = new SourceStringReader(entry.getValue());
			FileFormatOption option = new FileFormatOption(FileFormat.SVG);
			try {
				File out = createFile(target, fileNameBase + "." + fileType);
				FileOutputStream outStream = new FileOutputStream(out);
				reader.generateImage(outStream, option);
				generatedOutput.add(out);
				outStream.close();

			} catch (IOException e) {
				System.out.println(e.getMessage());
			}

		}

		return generatedOutput;
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
