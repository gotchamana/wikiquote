package io.github.service;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import lombok.Cleanup;

public class Windows10NotificationService implements NotificationService {

    private Path script;
    private Path config;

    public Windows10NotificationService(Path config, Path script) {
        this.config = config;
        this.script = script;
    }

    @Override
    public void sendNotification(String title, String content, Path icon) {
        configNotification(title, content, icon);
        runNotificationScript();
    }

    private void configNotification(String title, String content, Path icon) {
        try {
            var configDoc = parseXml();
            modifyNotificationConfig(configDoc, title, content, icon);

            @Cleanup var out = Files.newOutputStream(config);
            saveXml(out, configDoc);
        } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private Document parseXml() throws ParserConfigurationException, IOException, SAXException {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        var builder = factory.newDocumentBuilder();
        return builder.parse(config.toUri().toString());
    }

    private void modifyNotificationConfig(Document config, String title, String content, Path icon) {
        setNotificationIcon(config, icon);
        setNotificationText(config, title, content);
    }

    private void setNotificationIcon(Document config, Path icon) {
        var optionNodes = config.getElementsByTagName("Option");

        nodeStream(optionNodes)
            .map(Node::getAttributes)
            .forEach(attrs -> {
                var attr = attrs.getNamedItem("Name");
                var name = attr.getTextContent();

                if (name.equals("ImagesPath")) {
                    var parent = icon.toAbsolutePath().getParent();
                    attrs.getNamedItem("Value").setTextContent(parent.toUri().toString());
                }

                if (name.equals("LogoImageName"))
                    attrs.getNamedItem("Value").setTextContent(icon.getFileName().toString());
            });
    }

    private void setNotificationText(Document config, String title, String content) {
        var textNodes = config.getElementsByTagName("Text");

        nodeStream(textNodes)
            .filter(textNode -> {
                var attr = textNode.getAttributes().getNamedItem("Name");
                if (attr == null) return false;

                var name = attr.getTextContent();
                return name.equals("HeaderText") || name.equals("TitleText") || name.equals("BodyText1");
            })
            .forEach(textNode -> {
                if (textNode.getAttributes().getNamedItem("Name").getTextContent().equals("BodyText1"))
                    textNode.setTextContent(content);
                else
                    textNode.setTextContent(title);
            });
    }

    private Stream<Node> nodeStream(NodeList nodes) {
        return IntStream.range(0, nodes.getLength()).mapToObj(nodes::item);
    }

    private void saveXml(OutputStream out, Document doc) throws TransformerException {
        var factory = TransformerFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        var transformer = factory.newTransformer();
        var source = new DOMSource(doc);
        transformer.transform(source, new StreamResult(out));
    }

    private void runNotificationScript() {
        try {
            var command = List.of(
                "powershell.exe",
                "-executionpolicy", "bypass",
                "-File", script.toAbsolutePath().toString(),
                "-Config", config.toAbsolutePath().toString()
            );
            var process = new ProcessBuilder(command).inheritIO().start();
            process.onExit().get();
            Thread.sleep(3000);
        } catch (IOException | ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}
