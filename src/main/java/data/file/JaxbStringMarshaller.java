package data.file;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

/**
 * Generic JAX-B marshaller that consumes JAX-B objects and produces XML strings.
 * You have to create this StringMarshaller using am appropriate marshaller of the
 * needed XML Schema.
 */
public class JaxbStringMarshaller {

    private final Marshaller marshaller;

    public JaxbStringMarshaller(final Object jaxbObject) throws JAXBException {
        this(jaxbObject.getClass());
    }

    public JaxbStringMarshaller(final Class clazz) throws JAXBException {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            this.marshaller = jaxbContext.createMarshaller();
        } catch (final JAXBException e) {
            throw new JAXBException("XML string marshaller for " + clazz.toString()
                    + " could not initialized", e);
        }
    }
    
    public <T> JaxbStringMarshaller(final Class<T> clazz, final String packageName) throws JAXBException {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(packageName, clazz.getClassLoader());
            this.marshaller = jaxbContext.createMarshaller();
        } catch (final JAXBException e) {
            throw new JAXBException("XML string marshaller for " + clazz.toString()
                    + " could not initialized", e);
        }
    }

    public JaxbStringMarshaller(final Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public String marshall(final Object jaxbObject) throws JAXBException {
        try {
            final StringWriter stringWriter = new StringWriter();
            marshaller.marshal(jaxbObject, stringWriter);
            return stringWriter.toString();
        } catch (final JAXBException e) {
        	System.out.println("Could not marshall jaxbObject");
            throw e;
        }
    }

    public void setMarshallerProperty(final String propertyName, final Object propertyValue) throws PropertyException {
        this.marshaller.setProperty(propertyName, propertyValue);
    }
}

