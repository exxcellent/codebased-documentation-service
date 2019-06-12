package business.generator.interfaces;

import java.io.File;
import java.util.List;

public interface DocumentGenerator {

	public List<File> generateDocuments(File targetFolder, File... srcFolders);
	
}
