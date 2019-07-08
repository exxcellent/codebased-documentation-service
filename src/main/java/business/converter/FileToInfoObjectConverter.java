package business.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import collectors.models.InfoObject;
import data.file.FileReader;
import filemanagement.FileWriter;

public class FileToInfoObjectConverter {
	
	/**
	 * Creates InfoObjects (if possible) based on the files in the given list.
	 * 
	 * @param files list of files, that are to be turned into InfoObjects.
	 * @return list with the created InfoObjects.
	 */
	public static <T extends InfoObject> List<T> createJSONObjects(List<File> files, Class<T> clazz) {
		List<T> objects = new ArrayList<>();
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

		for (File file : files) {
			try (JsonReader reader = new JsonReader(
					new InputStreamReader(new FileInputStream(file), FileWriter.CHARSET))) {
				objects.add(gson.fromJson(reader, clazz));
			} catch (IOException e) {
				System.out.println("Could not access file: " + file.getAbsolutePath());
				System.out.println(e.getMessage());
			} catch (IllegalStateException | JsonSyntaxException e) {
				System.out.println("Error reading JSON from file: " + file.getAbsolutePath());
				System.out.println(e.getMessage());
			}
		}

		return objects;
	}
	
	public static <T extends InfoObject> List<T> getCollectedInfoObjects(String name, Class<T> clazz, File... srcFolders) {
		List<File> foundFiles = new ArrayList<>();
		for (File file : srcFolders) {
			foundFiles.addAll(FileReader.findFilesWithName(file, name, ".json"));
		}
		return createJSONObjects(foundFiles, clazz);
	}

}
