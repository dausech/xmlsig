package me.dausech.xmlsig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configuracao {

	@Value("${arquivo.cert}")
	public String certificado;

	@Value("${senha.cert}")
	public String senha;

	@Bean
	public KeyStore keyStore() throws Exception {
		InputStream entrada = new FileInputStream(certificado);
		KeyStore ks = KeyStore.getInstance("pkcs12");
		try {
			ks.load(entrada, senha.toCharArray());
		} catch (IOException e) {
			throw new Exception("Senha do Certificado Digital incorreta ou Certificado invalido.");
		}
		return ks;
	}

	@Bean
	public KeyStore.PrivateKeyEntry pkEntry() throws Exception {

		KeyStore ks = keyStore();
		KeyStore.PrivateKeyEntry pkEntry = null;
		Enumeration<String> aliasesEnum = ks.aliases();
		while (aliasesEnum.hasMoreElements()) {
			String alias = (String) aliasesEnum.nextElement();
			if (ks.isKeyEntry(alias)) {
				pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
						new KeyStore.PasswordProtection(senha.toCharArray()));
				break;
			}
		}
		return pkEntry;
	}

	@Bean
	public PrivateKey privateKey() throws Exception {
		KeyStore.PrivateKeyEntry pkEntry = pkEntry();
		PrivateKey privateKey = pkEntry.getPrivateKey();
		return privateKey;
	}
	
	@Bean
	public X509Certificate certificate() throws Exception {
		KeyStore.PrivateKeyEntry pkEntry = pkEntry();		
		X509Certificate cert = (X509Certificate) pkEntry.getCertificate();
		return cert;
	}

	@Bean
	public KeyInfo keyInfo() throws Exception {		
		XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
		X509Certificate cert = certificate();
		KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();
		List<X509Certificate> x509Content = new ArrayList<X509Certificate>();
		x509Content.add(cert);
		X509Data x509Data = keyInfoFactory.newX509Data(x509Content);
		return keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));
	}
}
