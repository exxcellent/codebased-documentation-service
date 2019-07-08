package data.file;

import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Generic JAX-B unmarshaller that consumes Strings and produces JAX-B
 * objects. You have to create this StringUnmarshaller using am appropriate
 * marshaller of the needed XML Schema.
 */
public class JaxbStringUnmarshaller {

	private final Unmarshaller unmarshaller;

	public JaxbStringUnmarshaller(final Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	public JaxbStringUnmarshaller(final Object jaxbObject) throws JAXBException {
		this(jaxbObject.getClass());
	}

	public <T> JaxbStringUnmarshaller(final Class<T> clazz) throws JAXBException {
		final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		this.unmarshaller = jaxbContext.createUnmarshaller();
	}

	public <T> T unmarshall(final String objectAsXml) throws JAXBException {
		return (T) unmarshaller.unmarshal(new StringReader(objectAsXml));
	}
}
