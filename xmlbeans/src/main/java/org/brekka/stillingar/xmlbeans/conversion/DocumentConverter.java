package org.brekka.stillingar.xmlbeans.conversion;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DocumentConverter extends AbstractTypeConverter<Document> {

    private final TransformerFactory factory;
    
    public DocumentConverter() {
        this(TransformerFactory.newInstance());
    }
    
    public DocumentConverter(TransformerFactory factory) {
        this.factory = factory;
    }

    public Document convert(XmlObject xmlValue) {
        Element element = ElementConverter.xmlObjectToElement(xmlValue);
        return elementToDocument(element, factory);
    }

    public Class<Document> targetType() {
        return Document.class;
    }

    public static Document elementToDocument(Element element, TransformerFactory factory) {
        try {
            Transformer transformer = factory.newTransformer();
            DOMSource source = new DOMSource(element);
            DOMResult result = new DOMResult();
            transformer.transform(source, result);
            Node node = result.getNode();
            return (Document) node;
        } catch (TransformerConfigurationException e) {
            throw new IllegalArgumentException(e);
        } catch (TransformerException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
