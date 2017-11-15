package me.dausech.xmlsig;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
@Component
public class Assinatura {

	@Autowired
	PrivateKey privateKey;

	@Autowired
	KeyInfo keyInfo;

	@Autowired
	X509Certificate certificado;

	public String assinar(String xml, String tagSignature) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

			NodeList list = doc.getElementsByTagName(tagSignature);			
			Element el = (Element) list.item(0);
			String referenceURI = el.getAttribute("id");		
			el.setIdAttribute("id", true);

			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", new XMLDSigRI());

			List<Transform> transformList = new ArrayList<Transform>();
			transformList.add(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
			transformList.add(
					fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null));

			Reference ref = fac.newReference("#" + referenceURI, fac.newDigestMethod(DigestMethod.SHA256, null),
					transformList, null, null);

			SignedInfo si = fac.newSignedInfo(
					fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
					fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
					Collections.singletonList(ref));

			KeyInfoFactory kif = fac.getKeyInfoFactory();
			X509Data x509Data = kif.newX509Data(Collections.singletonList(certificado));
			KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509Data));

			DOMSignContext dsc = new DOMSignContext(privateKey, el.getParentNode());

			XMLSignature signature = fac.newXMLSignature(si, ki);
			signature.sign(dsc);

			StreamResult streamResult = new StreamResult(new StringWriter());
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "no");
			trans.transform(new DOMSource(doc), streamResult);

			return streamResult.getWriter().toString();

		} catch (Exception e) {
			return e.getMessage();
		}

	}

}
