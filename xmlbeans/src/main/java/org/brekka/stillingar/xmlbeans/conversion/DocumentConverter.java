package org.brekka.stillingar.xmlbeans.conversion;

import javax.xml.transform.TransformerFactory;

public class DocumentConverter extends org.brekka.stillingar.core.conversion.xml.DocumentConverter {

    public DocumentConverter() {
        this(new ElementConverter(), TransformerFactory.newInstance());
    }
    
    public DocumentConverter(org.brekka.stillingar.core.conversion.xml.ElementConverter elementConverter) {
        this(elementConverter, TransformerFactory.newInstance());
    }
    
    public DocumentConverter(org.brekka.stillingar.core.conversion.xml.ElementConverter elementConverter, TransformerFactory factory) {
        super(elementConverter, factory);
    }
}
