package ch.sbb.polarion.extension.example.xxe;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.io.StringReader;

public class XxeVulnerable {

    // ruleid: polarion-xxe-unsafe-parser
    public Document parseDoc(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    // ruleid: polarion-xxe-unsafe-parser
    public void parseSax(String xml) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(new InputSource(new StringReader(xml)), null);
    }

    // ruleid: polarion-xxe-unsafe-parser
    public void parseStax(String xml) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
        while (reader.hasNext()) {
            reader.next();
        }
    }

    // ACCESS_EXTERNAL_DTD set to a non-empty value still resolves file://
    // external entities, so naming the constant is not hardening.
    // ruleid: polarion-xxe-unsafe-parser
    public Document parseDocDtdNotEmptied(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "file");
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    // Restricting schema resolution alone addresses neither DOCTYPE processing
    // nor external general entities.
    // ruleid: polarion-xxe-unsafe-parser
    public Document parseDocSchemaOnly(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    // A comment naming the hardening feature must not suppress the finding. The
    // earlier suppression was a `pattern-not-regex` over the matched region,
    // which matched comment text as readily as code.
    // ruleid: polarion-xxe-unsafe-parser
    public Document parseDocCommentOnly(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // TODO: set disallow-doctype-decl here before shipping
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    // Hardening one factory must not clear another in the same method: the
    // suppression is tied to the receiver the positive pattern binds.
    public void parseTwoFactories(String hardened, String exposed) throws Exception {
        DocumentBuilderFactory safeFactory = DocumentBuilderFactory.newInstance();
        safeFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        safeFactory.newDocumentBuilder().parse(new InputSource(new StringReader(hardened)));

        // ruleid: polarion-xxe-unsafe-parser
        DocumentBuilderFactory exposedFactory = DocumentBuilderFactory.newInstance();
        exposedFactory.newDocumentBuilder().parse(new InputSource(new StringReader(exposed)));
    }

    // The feature is set after the builder exists, so it does not apply to that
    // builder. Hardening has to sit between newInstance() and the creation call.
    // ruleid: polarion-xxe-unsafe-parser
    public Document parseDocHardenedTooLate(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return builder.parse(new InputSource(new StringReader(xml)));
    }
}
