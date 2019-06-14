package data.interfaces;

import java.io.File;
import java.util.List;

public interface DataOutputToFile extends DataOutput {

	public List<File> writeToFile(String content, String fileName, String fileType, File targetFolder);
	
}
