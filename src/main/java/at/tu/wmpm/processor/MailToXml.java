package at.tu.wmpm.processor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Service
public class MailToXml implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MailToXml.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        String inMessageBody = in.getBody(String.class);
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("mail");
        doc.appendChild(rootElement);

        Element sender = doc.createElement("sender");
        sender.appendChild(doc.createTextNode(in.getHeaders().get("Return-Path").toString()));
        rootElement.appendChild(sender);


        Element subj = doc.createElement("subject");
        subj.appendChild(doc.createTextNode(in.getHeaders().get("Subject").toString()));
        rootElement.appendChild(subj);

        Element body = doc.createElement("body");
        body.appendChild(doc.createTextNode(inMessageBody));
        rootElement.appendChild(body);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(in.getHeaders().get("Subject").toString()+""+new SimpleDateFormat("ddMMyyyy_HH-mm").format(new Date())+".xml"));


        transformer.transform(source, result);

        log.info("XML for mail created");
    }
}
