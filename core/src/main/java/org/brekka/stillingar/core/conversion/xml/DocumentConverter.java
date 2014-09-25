package org.brekka.stillingar.core.conversion.xml;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.brekka.stillingar.core.conversion.AbstractTypeConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DocumentConverter extends AbstractTypeConverter<Document> {

    private final TransformerFactory factory;
    
    private final ElementConverter elementConverter;

    public DocumentConverter() {
        this(new ElementConverter());
    }
    
    public DocumentConverter(ElementConverter elementConverter) {
        this(elementConverter, TransformerFactory.newInstance());
    }
    
    public DocumentConverter(ElementConverter elementConverter, TransformerFactory factory) {
        this.elementConverter = elementConverter;
        this.factory = factory;
    }
    
    @Override
    public final Class<Document> targetType() {
        return Document.class;
    }
    
    @Override
    public Document convert(Object obj) {
        Element element = elementConverter.convert(obj);
        Document value = elementToDocument(element, factory);
        return value;
    }

    protected Document elementToDocument(Element element, TransformerFactory transformerFactory) {
        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(element);
            DOMResult result = new DOMResult();
            transformer.transform(source, result);
            Node node = result.getNode();
            return (Document) node;
        } catch (TransformerConfigurationException e) {
            throw new IllegalArgumentException("Element to document transform configuration problem", e);
        } catch (TransformerException e) {
            throw new IllegalArgumentException("Failed to transform element to document", e);
        }
    }
}
