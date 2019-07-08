package application;

import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;

import business.generator.impl.generators.DocumentationGenerator;
import data.file.FileWriter;

public class CommandLineApplication {

	public static void main(String[] args) {
		CommandLineApplication cmdLine = new CommandLineApplication();
		cmdLine.run(args);

	}

	public void run(String... args) {

		File srcFolder = null;
		File targetFolder = null;
		boolean formatXML = false;
		boolean formatplantUML = false;
		boolean visualize = false;

		for (int i = 0; i < args.length; i++) {
			String argument = args[i];
			switch (argument.toLowerCase(Locale.ROOT)) {
			case "-src":
				if (i + 1 < args.length) {
					srcFolder = Paths.get(args[i + 1]).toFile();
				}
				break;

			case "-tgt":
				if (i + 1 < args.length) {
					targetFolder = Paths.get(args[i + 1]).toFile();
				}
				break;

			case "xml":
				formatXML = true;
				break;

			case "pltuml":
				formatplantUML = true;
				break;

			case "vis":
				visualize = true;

			}
		}

		if (targetFolder == null) {
			targetFolder = srcFolder;
		}
		if (srcFolder == null) {
			System.err.println("At least source directory [-src \"[path]\"] is needed.");
			return;
		}
		if (!targetFolder.exists() || !srcFolder.exists()) {
			System.err.println("Given directory doesn't exist.");
			return;
		}
		
//		List<File> generatedFiles = new ArrayList<>();

		if (formatplantUML) {
			System.out.println("Generating PlantUML-Descriptions" + (visualize ? " and -Graphs" : "."));
		}
		
		if (formatXML) {
			System.out.println("Generating xml description files.");
		}
		
		DocumentationGenerator generator = new DocumentationGenerator();
		generator.createDocumentation(new FileWriter(targetFolder), formatplantUML, formatXML, visualize, srcFolder);
		
	}

}
