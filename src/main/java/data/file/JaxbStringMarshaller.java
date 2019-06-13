package data.file;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

/**
 * I'm a generic JAX-B marshaller that consumes JAX-B objects and produces XML strings.
 * You have to create this StringMarshaller using am appropriate marshaller of the
 * needed XML Schema.
 *
 * This class is not thread-safe.
 *
 * @author Lars Gielsok, MaibornWolff GmbH
 */
public class JaxbStringMarshaller {

    private final Marshaller marshaller;


    /**
     * I create a JAX-B-String-unmarshaller of a JAX-B object.
     *
     * @param jaxbObject generated JAX-B target transfer object.
     * @throws TechnicalException if the marshaller could not be initialized.
     */
    public JaxbStringMarshaller(final Object jaxbObject) throws JAXBException {
        this(jaxbObject.getClass());
    }


    /**
     * I create a unmarshaller of a JAX-B class.
     *
     * @param clazz the class of the JAX-B target transfer object.
     * @throws TechnicalException if the unmarshaller could not be initialized.
     */
    public JaxbStringMarshaller(final Class clazz) throws JAXBException {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            this.marshaller = jaxbContext.createMarshaller();
        } catch (final JAXBException e) {
            throw new JAXBException("XML string marshaller for " + clazz.toString()
                    + " could not initialized", e);
        }
    }
    
    /**
     * I create a unmarshaller of a JAX-B class.
     *
     * @param clazz the class of the JAX-B target transfer object.
     * @throws TechnicalException if the unmarshaller could not be initialized.
     */
    public JaxbStringMarshaller(final Class clazz, final String packageName) throws JAXBException {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(packageName, clazz.getClassLoader());
            this.marshaller = jaxbContext.createMarshaller();
        } catch (final JAXBException e) {
            throw new JAXBException("XML string marshaller for " + clazz.toString()
                    + " could not initialized", e);
        }
    }


    /**
     * I create a JAX-B-String-unmarshaller of a given concrete Marshaller.
     *
     * @param marshaller the concrete
     */
    public JaxbStringMarshaller(final Marshaller marshaller) {
        this.marshaller = marshaller;
    }


    /**
     * Marshall given object to String and return that. The JAX-B object is generated from a XSD schema with a
     * maven plugin.
     *
     * @param jaxbObject the JAX-B object to be converted to a String.
     * @return XML String of the JAX-B object.
     * @throws Exception 
     */
    public String marshall(final Object jaxbObject) throws Exception {
        try {
            final StringWriter stringWriter = new StringWriter();
            marshaller.marshal(jaxbObject, stringWriter);
            return stringWriter.toString();
        } catch (final JAXBException e) {
            throw new Exception("Could not serialize JAX-B object into a string", e);
        }
    }


    /**
     * add a custom property to the string marshaller
     * @param propertyName
     * @param propertyValue
     * @throws PropertyException
     */
    public void setMarshallerProperty(final String propertyName, final Object propertyValue) throws PropertyException {
        this.marshaller.setProperty(propertyName, propertyValue);
    }
}

