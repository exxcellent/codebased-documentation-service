package data.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XMLFileWriter {

	public static File writeToFile(Document doc, String fileName, File targetFolder) {

		XMLOutputter outter = new XMLOutputter();
		outter.setFormat(Format.getPrettyFormat());
		Paths.get(targetFolder.getAbsolutePath(), "xml").toFile().mkdirs();
		File outputFile = Paths.get(targetFolder.getAbsolutePath(), "xml", fileName + ".xml").toFile();
		try {
			outter.output(doc, new FileWriter(outputFile));
			return outputFile;
		} catch (IOException e) {
			System.err.println("Error writing File: " + outputFile.getAbsolutePath());
			System.err.println(e.getMessage());
			return null;
		}
	}
}
