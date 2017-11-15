package me.dausech.xmlsig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.w3c.dom.Document;

@SpringBootApplication
public class Main  implements CommandLineRunner {

	@Autowired
	Assinatura assinatura;
	
	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder().sources(Main.class).web(false).run(args);
	}
	
	@Override
	public void run(String... args) throws InterruptedException {
		String path = "./xmltestes/evento.xml";
		String xml = txtFromFile(path);
		
		String assinado = assinatura.assinar(xml, "evtInfoContri");
		
		try {
			FileWriter fw = new FileWriter(new File("./xmltestes/evento_assinado.xml"));
			fw.write(assinado);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String txtFromFile(String fileName) {
		String s = null;

		try {
			InputStream is = new FileInputStream(new File(fileName));

			DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);

			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			s = sw.toString();
			
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
		return s;
	}
}
