package catalogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CatalogoChaves {

	private SecureRandom srandom = new SecureRandom();

	private static CatalogoChaves singleInstance = null; 


	public static CatalogoChaves getInstance() {
		if (singleInstance == null) 
			singleInstance = new CatalogoChaves(); 

		return singleInstance;
	}

	public KeyStore getServerKeystore() {
		try {
			File keystoreServer = new File("keystoreServer");
			FileInputStream kfile = new FileInputStream(keystoreServer);
			KeyStore kstore = KeyStore.getInstance("JCEKS");
			kstore.load(kfile, "123456".toCharArray()); 
			return kstore;
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			System.out.println("ERRO: Não foi possivel obter a keystore do servidor");
		}
		return null;
	}

	public Key getServerPublicKey() {
		KeyStore kstore = getServerKeystore();
		try {
			return kstore.getCertificate("myserver").getPublicKey();
		} catch (KeyStoreException e) {
			System.out.println("ERRO: Não foi possivel obter a chave publica do servidor");
		}
		return null;
	}

	public PrivateKey getServerPrivateKey() {
		KeyStore kstore = getServerKeystore();
		try {
			return (PrivateKey) kstore.getKey("myserver", "123456".toCharArray());
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			System.out.println("ERRO: Não foi possivel obter a chave privada do servidor");
		}
		return null;
	}

	public Cipher publicKeyEncryptCipher() {
		try {
			Cipher cypher = Cipher.getInstance("RSA");
			cypher.init(Cipher.ENCRYPT_MODE, getServerPublicKey());
			return cypher;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			System.out.println("ERRO: Não foi possivel gerar a cifra");
		}
		return null;
	}

	public Cipher privateKeyDecryptCipher() {
		try {
			Cipher cypher = Cipher.getInstance("RSA");
			cypher.init(Cipher.DECRYPT_MODE, getServerPrivateKey());
			return cypher;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			System.out.println("ERRO: Não foi possivel gerar a cifra");
		}
		return null;
	}

	public Cipher getFileEncryptCipher() {
		try {
			//RSA/ECB/PKCS1Padding
			Cipher cypher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cypher.init(Cipher.ENCRYPT_MODE, getServerPublicKey());
			return cypher;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			System.out.println("ERRO: Não foi possivel gerar a cifra");
		}
		return null;
	}

	public Cipher getFileDecryptCipher() {
		try {

			Cipher cypher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cypher.init(Cipher.DECRYPT_MODE, getServerPrivateKey());
			return cypher;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			System.out.println("ERRO: Não foi possivel gerar a cifra");
		}
		return null;
	}

	public static void processFile(Cipher ci,InputStream in,OutputStream out)
			throws javax.crypto.IllegalBlockSizeException,
			javax.crypto.BadPaddingException,
			java.io.IOException {
		byte[] ibuf = new byte[1024];
		int len;
		while ((len = in.read(ibuf)) != -1) {
			byte[] obuf = ci.update(ibuf, 0, len);
			if ( obuf != null ) out.write(obuf);
		}
		byte[] obuf = ci.doFinal();
		if ( obuf != null ) out.write(obuf);
	}

	public byte[] getCertificate(String user) {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(new FileInputStream("PubKeys/cert" + user + ".cer"));
			return cert.getEncoded();
		} catch (CertificateException | FileNotFoundException e) {
			System.out.println("ERRO: Não foi possivel obter o certificado.");
		}
		return new byte[0];
	}

	public List<byte[]> getCertificates() {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			ArrayList<byte[]> certs = new ArrayList<>();
			List<String> users = CatalogoUtilizadores.getInstance().getNomes();
			for (String user: users) {
				Certificate cert = cf.generateCertificate(new FileInputStream("PubKeys/cert" + user + ".cer"));
				certs.add(cert.getEncoded());
			}
		} catch (CertificateException | FileNotFoundException e) {
			System.out.println("ERRO: Não foi possivel obter os certificados.");
		}
		return null;
	}

	public void encriptarFicheiro(String inputFile, String outputFile) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128);
			SecretKey skey = kgen.generateKey();

			byte[] iv = new byte[128/8];
			srandom.nextBytes(iv);
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			FileOutputStream out = new FileOutputStream(outputFile);
			Cipher cipher = this.getFileEncryptCipher();
			byte[] b = cipher.doFinal(skey.getEncoded());
			out.write(b);

			out.write(iv);

			Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
			ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);
			try (FileInputStream in = new FileInputStream(inputFile)) {
				processFile(ci, in, out);
				in.close();
			}
			out.close();
		} catch (IOException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			System.out.println("ERRO: Não foi possivel encriptar o ficheiro.");
		}
	}

	public void decriptarFicheiros(String inputFile, String outputFile) {
		try {
			FileInputStream in = new FileInputStream(inputFile);
			Cipher cipher = this.getFileDecryptCipher();
			byte[] b = new byte[256];
			in.read(b);
			byte[] keyb = cipher.doFinal(b);
			SecretKeySpec skey = new SecretKeySpec(keyb, "AES");

			byte[] iv = new byte[128/8];
			in.read(iv);
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
			ci.init(Cipher.DECRYPT_MODE, skey, ivspec);
			try (FileOutputStream out = new FileOutputStream(outputFile)){
				processFile(ci, in, out);
				out.close();
			}
			in.close();
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException | IOException e) {
			System.out.println("ERRO: Não foi possivel decriptar o ficheiro");
		}
	}
}
