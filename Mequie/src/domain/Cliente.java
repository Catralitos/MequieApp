package domain;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.omg.CORBA.DataOutputStream;

import javafx.util.Pair;
import response.CollectResponse;
import response.GInfoOwnerResponse;
import response.GInfoResponse;
import response.HistoryResponse;
import response.UInfoResponse;

public class Cliente{

	private String utilizador;
	private static Socket clientSocket;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	private OutputStream output;
	private InputStream input;

	private static SocketFactory sf;
	private static SSLSocket s;
	private Certificate certificado;
	
	private KeyStore ks;
	private String ksPass;

	public Cliente(String localUserID, String ip, int port) {
		this.utilizador = localUserID;
		try {
			sf = SSLSocketFactory.getDefault();
			s = (SSLSocket) sf.createSocket(ip, port);
			out = new ObjectOutputStream(s.getOutputStream());
			in = new ObjectInputStream(s.getInputStream());
			output = s.getOutputStream();
			input = s.getInputStream();
			System.out.println("Connected");
		} catch (IOException e) {
			System.out.println("ERRO: Não foi possivel conectar ao servidor.");
		}
	}


	/*
	 * Metodo que autentica ou nao o cliente
	 * @param password = senha
	 * @return -1 sse existir o utilizador, mas a password for errada
	 * 			 0 sse nao existir o utilizador e o mesmo foi registrado no sistema
	 * 			 1 sse foi autenticado com sucesso
	 *          -1 sse nao foi autenticado ou registado
	 */
	public int autenticacao(KeyStore kstore, String keystorePassword) {
		try {
			out.writeObject("autenticacao");
			out.writeObject(utilizador);
			ks = kstore;
			ksPass = keystorePassword;
			int registado = in.readInt();
			long nonce = in.readLong();
			

			byte[] buf = Long.toString(nonce).getBytes();
			this.certificado = kstore.getCertificate("fc"+utilizador);
			Signature sig = getSignature(kstore, keystorePassword, buf); 
			if (sig == null) {
				System.out.println("ERRO: Não foi possivel gerar a assinatura");
				return -1;
			}
			
			try {
				byte[] assinatura = sig.sign();
				out.writeInt(assinatura.length);
				out.flush();
				out.write(assinatura);
				out.flush();
			} catch (SignatureException e) {
				System.out.println("ERRO: Falha ao enviar a assinatura");
				return -1;
			}

			if (registado == 0) {
				out.writeLong(nonce);
				out.flush();
				byte[] cert = getCert(kstore, keystorePassword);
				out.writeInt(cert.length);	
				out.flush();
				out.write(cert);
				out.flush();
			} else if (registado == 1) {
				//do nothing 
				//so porque mandas sempre a assinatura de qualquer forma mais vale fazer primeiro
				//mas se calhar depois mudamos
			} else {
				System.out.println("ERRO: autenticação falhou.");
				return -1;
			}
			return in.readInt();

		} catch (IOException | KeyStoreException e) {
			System.out.println("ERRO: Não foi possivel autenticar.");
		}
		return -1;
	}

	private byte[] getCert(KeyStore kstore, String keystorePassword) {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
		    Certificate cert = cf.generateCertificate(new FileInputStream("PubKeys/cert" + utilizador + ".cer"));
			return cert.getEncoded();
		} catch (CertificateException | FileNotFoundException e) {
			System.out.println("ERRO: Não foi possivel obter o certificado.");
		}
		return new byte[0];
	}


	private Signature getSignature(KeyStore kstore, String keystorePassword, byte[] buf) {
		try {
			Signature sig = Signature.getInstance("MD5withRSA");
			PrivateKey pk = (PrivateKey) kstore.getKey("fc" + utilizador, keystorePassword.toCharArray());
			sig.initSign(pk);
			sig.update(buf);
			return sig;
		} catch (UnrecoverableKeyException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException
				| SignatureException e) {
			System.out.println("ERRO: Não foi possivel obter a assinatura.");
		}
		return null;
	}


	/*
	 * Metodo para criar um novo grupo
	 * @param name = nome do grupo
	 * @return true sse o grupo foi criado com sucesso
	 */
	public boolean create(String name) {
		boolean criado = false;
		try {
			out.writeObject("create");
			out.writeObject(name);
			String resposta = (String) in.readObject();
			int x = Integer.parseInt(resposta);
			criado = x == 1;
			if(criado) {
			    KeyGenerator kg = KeyGenerator.getInstance("AES");
			    kg.init(256);
			    SecretKey key = kg.generateKey();			    
			    PublicKey publicKey = this.certificado.getPublicKey();
			    Cipher cKey = Cipher.getInstance("RSA");
			    cKey.init(Cipher.WRAP_MODE, publicKey);
			    byte[] wrappedKey = cKey.wrap(key);
			    out.writeObject(wrappedKey.length+"");
			    out.flush();
			    out.write(wrappedKey);
			    out.flush();
			}
		} catch (InvalidKeyException | IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException e) {
			System.out.println("ERRO: Não foi possivel criar um grupo.");
		}
		return criado;	
	}


	/*
	 * Metodo para adicionar um utilizador a um grupo
	 * @param user = nome do usuario a adicionar
	 * 		  group = nome do grupo
	 * @return true sse o user foi adicionado ao grupo com sucesso
	 */
	public boolean addu(String user, String group) {
		boolean adicionou = false;
		try {
			out.writeObject("addu");
			out.writeObject(this.utilizador);
			out.writeObject(user);
			out.writeObject(group);
			
			String resposta = (String) in.readObject();
			int x = Integer.parseInt(resposta);
			adicionou = x == 1;
			
			if(adicionou) {
				criaKeysGrupo();
			}
		} catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | KeyStoreException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("ERRO: Não foi possivel adicionar o utilizador ao grupo.");
		}
		return adicionou;
	}

	private byte[] criarEncryptedKey(SecretKey key) {
		byte[] encryptedKey = null;
		try {
		    PublicKey publicKey = this.certificado.getPublicKey();
		    Cipher c = Cipher.getInstance("RSA");
		    //c.init(Cipher.ENCRYPT_MODE, publicKey);
		    //c.update(key.getEncoded());
		    //encryptedKey = c.doFinal();
		    c.init(Cipher.WRAP_MODE, publicKey);
		    encryptedKey = c.wrap(key);
			return encryptedKey;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return encryptedKey;
	}
	
	public KeyStore getUserKeystore(String user) {
		try {
			File keystore = new File("keystoresUsers/keystore"+user);
			FileInputStream kfile = new FileInputStream(keystore);
			KeyStore kstore = KeyStore.getInstance("JCEKS");
			kstore.load(kfile, "123456".toCharArray()); 
			return kstore;
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			System.out.println("ERRO: Não foi possivel obter a keystore do servidor");
		}
		return null;
	}
	
	private void criaKeysGrupo() throws NoSuchAlgorithmException, IOException, ClassNotFoundException, NoSuchPaddingException, KeyStoreException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(256);
		SecretKey key = kg.generateKey();
		byte[] encryptedKey = criarEncryptedKey(key);
	    out.writeObject(Integer.toString(encryptedKey.length));
	    out.flush();
	    out.write(encryptedKey);
	    out.flush();
	    
	    int size = Integer.parseInt((String)in.readObject());
	    List<String> membros = new ArrayList<>();
	    for(int i = 0; i < size; i++) {
	    	membros.add((String)in.readObject());
	    }
		KeyStore kstore;
	    for(int i = 0; i < size; i++) {
	    	Cipher c = Cipher.getInstance("RSA");
	    	kstore = getUserKeystore(membros.get(i));
	    	Certificate cert = kstore.getCertificate("fc"+membros.get(i));
	    	PublicKey pk = cert.getPublicKey();
	    	//c.init(Cipher.ENCRYPT_MODE, pk);
	    	//c.update(key.getEncoded());
	    	//byte[] encrypted = c.doFinal();
	    	c.init(Cipher.WRAP_MODE, pk);
	    	byte[] encrypted = c.wrap(key);
	    	out.writeObject(Integer.toString(encrypted.length));
		    out.flush();
		    out.write(encrypted);
		    out.flush();
	    }
	}


	/*
	 * Metodo para remover um utilizador de um grupo
	 * @param userID = nome do usuario a adicionar
	 * 		  groupID = nome do grupo
	 * @return true sse o user foi removido do grupo com sucesso
	 */
	public boolean removeu(String userID, String groupID) {
		boolean removeu = false;
		try {
			out.writeObject("removeu");
			out.writeObject(userID);
			out.writeObject(groupID);
			String resposta = (String) in.readObject();
			int x = Integer.parseInt(resposta);
			removeu = x == 1;
			if(removeu) {
				criaKeysGrupo();
			}
		} catch (IOException | ClassNotFoundException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | KeyStoreException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("ERRO: Não foi possivel remover o utilizador do grupo.");
		}
		return removeu;
	}

	/*
	 * Metodo que devolve a informacao de um dado grupo
	 * @param groupID = nome do grupo
	 * @return informacao do grupo, GInfoResponse com boolean de erro caso nao tenha informacao
	 */
	public GInfoResponse ginfo(String groupID) {
		GInfoResponse ginfo = null;
		try {
			out.writeObject("ginfo");
			out.writeObject(groupID);
			String e = (String) in.readObject();
			if(e.equals("-1")) {
				ginfo = new GInfoResponse(true);
			} else {
				String owner = (String) in.readObject();
				int n = Integer.parseInt((String) in.readObject());
				//ler chave de grupo
				if(owner.equals(utilizador)) {
					ginfo = new GInfoOwnerResponse(groupID, owner, n);
					for (int i = 0; i < n - 1; i++) {
						String user = (String) in.readObject();
						((GInfoOwnerResponse) ginfo).addUser(user);
					}
				} else {
					ginfo = new GInfoResponse(groupID, owner, n);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("ERRO: Não foi possivel buscar a informação do grupo.");
		}
		return ginfo;
	}

	/*
	 * Metodo que devolve a informacao do cliente atual
	 */
	public UInfoResponse uinfo() {
		UInfoResponse uinfo = new UInfoResponse(utilizador);
		try {
			out.writeObject("uinfo");
			out.writeObject(utilizador);
			int next = Integer.parseInt((String) in.readObject());
			if(next == -1) uinfo.setError();
			String membroDe = (String) in.readObject();
			String donoDe = (String) in.readObject();
            uinfo.setGroups(membroDe);
            uinfo.setOwned(donoDe);
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("ERRO: Não foi possivel buscar a informação do utilizador.");
		}
		return uinfo;
	}

	/*
	 * Metodo que envia uma mensagem de texto
	 * @param groupID = grupo a que a mensagem eh enviada
	 * 		  substring = mensagem a ser enviada
	 * @return true sse a mensagem foi enviada com sucesso
	 */
	public boolean msg(String groupID, String substring) {
		boolean enviado = false;
		try {
			out.writeObject("msg");
			out.writeObject(groupID);
			out.writeObject(substring);
			out.writeObject(this.utilizador);
		    
		    Key unwrappedKey = getCurrentGroupKey();
		    
			Cipher c2 = Cipher.getInstance("AES");
	    	c2.init(Cipher.ENCRYPT_MODE, unwrappedKey);
	    	
	    	c2.update(substring.getBytes());
	    	byte[] encryptedMessage = c2.doFinal();
	    	out.writeObject(Integer.toString(encryptedMessage.length));
		    out.flush();
		    out.write(encryptedMessage);
		    out.flush();
			
			enviado = ((String) in.readObject()).equals("1");
		} catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException  | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
			System.out.println("ERRO: Não foi possivel enviar a mensagem");
		}
		return enviado;
	}


	private Key getCurrentGroupKey() {
		Key unwrappedKey = null;
		try {
			int keysize = Integer.parseInt((String)in.readObject());
			byte[] currentKey = new byte[keysize];
			in.read(currentKey);
			
			KeyStore kstore = getUserKeystore(this.utilizador);
		    PrivateKey kr = (PrivateKey) kstore.getKey("fc"+this.utilizador, "123456".toCharArray());
			Cipher c1 = Cipher.getInstance("RSA");
		    c1.init(Cipher.UNWRAP_MODE, kr);
		    unwrappedKey = c1.unwrap(currentKey, "AES", Cipher.SECRET_KEY);	
			return unwrappedKey;	
		} catch(IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | UnrecoverableKeyException | KeyStoreException | NumberFormatException | ClassNotFoundException e) {
			System.out.println("ERRO: Não foi possivel enviar buscar a chave currente do grupo");
		}
		return unwrappedKey;
		
	}

	public boolean photo(String groupID, String ficheiro) {
		boolean foiEnviado = false;
		File file = new File(ficheiro);
		if (!file.exists()) {
			return false;
		}
		try {
			out.writeObject("photo");
			out.writeObject(groupID);
			out.writeObject(ficheiro);
			out.writeObject(this.utilizador);

			Key unwrappedKey = getCurrentGroupKey();
			
			Cipher c = Cipher.getInstance("AES");
	    	c.init(Cipher.ENCRYPT_MODE, unwrappedKey);
	    	
	    	System.out.println("FILE LENGTH: "+file.length());
	    	BufferedImage image = ImageIO.read(file);
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", byteArrayOutputStream);
			
			byte[] imagem = byteArrayOutputStream.toByteArray();
			System.out.println("BYTE IMAGE: "+imagem.length);
			
	    	byte[] encryptedImage = c.doFinal(imagem);
	    	System.out.println("ENCRYPTED IMAGE LENGTH: "+encryptedImage.length);
	    	out.writeObject(Integer.toString(encryptedImage.length));
		    output.flush();
		    output.write(encryptedImage);
		    output.flush();
			
			String resposta = (String)in.readObject();
			int response = Integer.parseInt(resposta);
			if(response == 1) foiEnviado = true;
		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException e) {
			System.out.println("ERRO: Não foi possivel enviar a fotografia");
			return false;
		}
		return true;
	}


	public CollectResponse collect(String groupID) {
		CollectResponse response = new CollectResponse();
		try {
			out.writeObject("collect");
			out.writeObject(groupID);
			if (((String) in.readObject()).equals(":inicio:")) {
				boolean naoParar = true;
				while (naoParar) {
					String msg = (String) in.readObject();
					if (msg.equals(":fim:")) {
						naoParar = false;
						break;
					}
					Mensagem newM;
					if (msg.equals("Texto")) {
						String messageId = (String) in.readObject();
						int sizeConteudo = Integer.parseInt((String) in.readObject());
						byte[] conteudo = new byte[sizeConteudo];
						in.read(conteudo);
						int keySize = Integer.parseInt((String) in.readObject());
						byte[] key = new byte[keySize];
						in.read(key);
						
						PrivateKey myPrivateKey = (PrivateKey) ks.getKey("fc"+this.utilizador, ksPass.toCharArray());
					    Cipher desKey = Cipher.getInstance("RSA");
					    desKey.init(Cipher.UNWRAP_MODE, myPrivateKey);
					    Key unwrappedKey = desKey.unwrap(key, "AES", Cipher.SECRET_KEY);
					    
					    Cipher d = Cipher.getInstance("AES");
					    d.init(Cipher.DECRYPT_MODE, unwrappedKey);
					
					    byte[] descifrado = new byte[sizeConteudo];
					    descifrado = d.doFinal(conteudo);
					    newM = new Mensagem(messageId, descifrado, "Texto");
					    response.addMensagem(newM);
					}
					if (msg.equals("Foto")){
						String path = (String) in.readObject();	
						byte[] sizeAr = new byte[4];
						input.read(sizeAr);
						int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();
						byte[] imageAr = new byte[size];
						input.read(imageAr);
						BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
						File imagem = new File(path);
						ImageIO.write(image, path.substring(path.indexOf('.') + 1), imagem);
					//	Fotografia f = new Fotografia(imagem, remetente);
					//	response.addMensagem(f);
					}
				}
			} else {
				response.setError(true);
			}
		} catch (IOException | ClassNotFoundException | InvalidKeyException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("ERRO: Não foi possivel colectar as respostas.");
		}
		return response;
	}

	public HistoryResponse history(String groupID) {
		HistoryResponse response = new HistoryResponse();
		try {
			out.writeObject("history");
			out.writeObject(groupID);
			if (((String) in.readObject()).equals(":inicio:")) {
				boolean naoParar = true;
				while (naoParar) {
					String msg = (String) in.readObject();
					if (msg.equals(":fim:")) {
						naoParar = false;
						break;
					}
					Mensagem newM;
					if (msg.equals("Texto")) {
						String messageId = (String) in.readObject();
						int sizeConteudo = Integer.parseInt((String) in.readObject());
						byte[] conteudo = new byte[sizeConteudo];
						in.read(conteudo);
						int keySize = Integer.parseInt((String) in.readObject());
						byte[] key = new byte[keySize];
						in.read(key);
						
						Key myPrivateKey = ks.getKey("keyrsa", ksPass.toCharArray());
					    Cipher desKey = Cipher.getInstance("RSA");
					    desKey.init(Cipher.UNWRAP_MODE, myPrivateKey);
					    Key unwrappedKey = desKey.unwrap(key, "AES", Cipher.SECRET_KEY);
					    
					    Cipher d = Cipher.getInstance("AES");
					    d.init(Cipher.DECRYPT_MODE, unwrappedKey);
					
					    byte[] descifrado = new byte[sizeConteudo];
					    descifrado = d.doFinal(conteudo);
					    newM = new Mensagem(messageId, descifrado, "Texto");
					    response.addMensagem(newM);
					} else {
						response.setError(true);
					}
				}
			}
		} catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnrecoverableKeyException | KeyStoreException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("ERRO: Não foi possivel buscar o histórico de mensagens.");
		}
		return response;
	}

	/*
	 * Metodo para fechar o cliente e avisar o servidor que o cliente vai encerrar
	 */
	public void quit() {
		try {
			out.writeObject("exit");
			out.close();
			in.close();
			output.close();
			input.close();
			clientSocket.close();
		} catch (IOException e) {
			System.out.println("ERRO: Não foi possivel fechar o cliente.");
		}

	}

}


