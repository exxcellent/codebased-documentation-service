package data.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XMLFileWriter {

	public static File writeToFile(Object jaxbObject, String fileName, File targetFolder) throws JAXBException {

		
		final JaxbStringMarshaller jaxbMarshaller = new JaxbStringMarshaller(jaxbObject.getClass());
		
		
		return null;
	}
	
	
}
