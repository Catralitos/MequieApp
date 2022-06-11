package connection;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import catalogs.CatalogoGrupos;
import catalogs.CatalogoUtilizadores;
import domain.Grupo;
import domain.Mensagem;
import files.ServerArquiver;
import files.ServerRestorer;
import javafx.util.Pair;

//Threads utilizadas para comunicacao com os clientes
public class ServerThread extends Thread {
	private Socket socket = null;
	private String user;
	private ObjectOutputStream outStream;
	private ObjectInputStream inStream;
	private InputStream input;
	private OutputStream output;
	private String erroGenerico = "ERRO: Não foi possivel enviar uma resposta.";
	private Random random = new Random();


	public ServerThread() {
		super();
	}

	public ServerThread(Socket socket) {
		super();
		this.socket = socket;
	}

	public ServerThread(String user) {
		super();
		this.user = user;
	}

	public ServerThread(Socket socket, String user) {
		super();
		this.socket = socket;
		this.user = user;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public void run() {
		String op = null;
		String localUserID = null;
		int registado = 0;
		try {
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());
			input = socket.getInputStream();
			output = socket.getOutputStream();
		} catch (IOException e) {
			System.out.println(erroGenerico);
		}
		try {
			op = (String) inStream.readObject();
			localUserID = (String) inStream.readObject();

			if(CatalogoUtilizadores.getInstance().existeUser(localUserID)) {
				System.out.println(localUserID + " já faz parte do catalogo de utilizadores!");
				registado = 1;
			}else {
				System.out.println(localUserID + " não faz parte do catalogo de utilizadores!");
				registado = 0;
			}

			//Enviar nonce e flag
			long nonce = random.nextLong();
			outStream.writeInt(registado);
			outStream.flush();
			outStream.writeLong(nonce);
			outStream.flush();

			//Receber assinatura
			int sigLength = inStream.readInt();
			byte[] assinatura = new byte[sigLength];
			inStream.readFully(assinatura);

			boolean check = true;

			if (registado == 0) {
				long nonceRecebido = inStream.readLong();
				int certLength = inStream.readInt();
				byte[] cert = new byte[certLength];
				inStream.readFully(cert);

				if (nonceRecebido != nonce) {
					check = false;
				}

				InputStream in = new ByteArrayInputStream(cert);
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				Certificate c = certFactory.generateCertificate(in);
				Signature sig = Signature.getInstance("MD5withRSA");
				sig.initVerify(c.getPublicKey());
				sig.update(Long.toString(nonce).getBytes());

				try {
					check = sig.verify(assinatura);
				} catch (SignatureException e) {
					System.out.println("ERRO: Falha a verificar assinatura");
				}
				//GUARDAR O NOVO CERTIFICADO
				File certificado = new File("PubKeys/cert" + localUserID + ".cer");
				if (certificado.createNewFile()) {
					final FileOutputStream os = new FileOutputStream("PubKeys/cert" + localUserID + ".cer");
					os.write(cert);
					os.close();
				}

				//ESCREVER O PAR EM users.txt
				CatalogoUtilizadores.getInstance().autenticacao(localUserID, certificado.getPath());

			} else if (registado == 1) {			
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				Certificate cert = cf.generateCertificate(new FileInputStream("PubKeys/cert" + localUserID + ".cer"));
				Signature sig = Signature.getInstance("MD5withRSA");
				sig.initVerify(cert);
				sig.update(Long.toString(nonce).getBytes());
				try {
					check = sig.verify(assinatura);
				} catch (SignatureException e) {
					System.out.println("ERRO: Falha a verificar assinatura");
				}
			}
			if (check && registado == 0) {
				outStream.writeInt(0);
				outStream.flush();
				user = localUserID;
			} else if (check && registado == 1){
				outStream.writeInt(1);
				outStream.flush();
				user = localUserID;
			} else {
				outStream.writeInt(-1);
				outStream.flush();
			}
			//TODO catches mais variados para sabermos os erros melhor de futuro.
		} catch (ClassNotFoundException | IOException | NoSuchAlgorithmException | CertificateException | InvalidKeyException /*| IllegalBlockSizeException | BadPaddingException */e) {
			System.out.println(erroGenerico);
		} catch (SignatureException e1) {
			e1.printStackTrace();
		}
		boolean flag = true;
		while(flag) {
			try {
				op = (String) inStream.readObject();
			} catch (ClassNotFoundException | IOException e) {
				flag = false;
				System.out.println(erroGenerico);
			}

			switch(op) {
			//CREATE
			case "create":
				try {
					String name = (String) inStream.readObject();
					int criado = createGroup(name) ? 1 : -1;
					outStream.writeObject(Integer.toString(criado));
					
					if(criado == 1) {
						int tamanhoKey = Integer.parseInt((String)inStream.readObject());
						byte[] wrappedKey = new byte[tamanhoKey];
						int recKey = inStream.read(wrappedKey);
						System.out.println(recKey);
						System.out.println("RECEBEU A KEY");
						ServerArquiver.addKeyToGroup(wrappedKey, name, user, 0);
						CatalogoGrupos.getInstance().getGrupo(name).setChaveCurrente(wrappedKey);
						CatalogoGrupos.getInstance().getGrupo(name).setKeyID(0);
						ServerArquiver.updateGrupos();
					}
				} catch (ClassNotFoundException | IOException e) {
					System.out.println(erroGenerico);
				}
				break;

				//ADDUSER
			case "addu":
				try {
					
					String dono = (String) inStream.readObject();
					String user = (String) inStream.readObject();
					String group = (String) inStream.readObject();
					
					this.user = dono;
					
					int adicionou = adicionarAGrupo(group, user) ? 1 : -1;
					outStream.writeObject(Integer.toString(adicionou));
					if(adicionou == 1) {
						criaKeysGrupo(group);
					}

				} catch (ClassNotFoundException | IOException e) {
					System.out.println(erroGenerico);
				}
				break;

				//REMOVE
			case "removeu":
				try {
					String u = (String) inStream.readObject();
					String g = (String) inStream.readObject();
					int removeu = removerDoGrupo(g, u) ? 1 : -1;
					outStream.writeObject(Integer.toString(removeu));
					if(removeu == 1) {
						criaKeysGrupo(g);
					}
				} catch (ClassNotFoundException | IOException e) {
					System.out.println(erroGenerico);
				}
				break;

			case "ginfo":
				try {
					String g = (String) inStream.readObject();
					List<String> ginfo = this.informacaoGrupo(g);
					if (ginfo.size() > 2) {
						outStream.writeObject("1");
					} else if (ginfo.isEmpty()) {
						outStream.writeObject("-1");
					} else {
						outStream.writeObject("0");
					}
					for(int i = 0; i < ginfo.size(); i++) {
						if(ginfo.isEmpty()) outStream.writeObject("0");
						outStream.writeObject(ginfo.get(i));
					}
				} catch (ClassNotFoundException | IOException e) {
					System.out.println(erroGenerico);
				}
				break;

			case "uinfo":
				try {
					List<String> uinfo = this.informacaoUser();
					outStream.writeObject("1");
					for(int i = 0; i < uinfo.size(); i++) {
						if(uinfo.isEmpty()) outStream.writeObject("0");
						outStream.writeObject(uinfo.get(i));
						outStream.writeObject("1");
					}
					outStream.writeObject("-1");
				} catch (IOException e) {
					System.out.println(erroGenerico);
				} 
				break;

			case "msg":
				try {
					String grupo = (String) inStream.readObject();
					String msg = (String) inStream.readObject();
					String remetente = (String) inStream.readObject();
					
					if(CatalogoGrupos.getInstance().getGrupo(grupo) != null) {
						byte[] currentKey = CatalogoGrupos.getInstance().getGrupo(grupo).getChaveCurrente();
						outStream.writeObject(Integer.toString(currentKey.length));
					    outStream.flush();
					    outStream.write(currentKey);
					    outStream.flush();
					    
					    int messageSize = Integer.parseInt((String)inStream.readObject());
						byte[] encryptedMessage = new byte[messageSize];
						inStream.read(encryptedMessage);
					    
						int enviada = CatalogoGrupos.getInstance().enviarMensagem(grupo, remetente, encryptedMessage, "texto") ? 1 : -1;
						outStream.writeObject(Integer.toString(enviada));
					} else {
						int enviada = -1;
						outStream.writeObject(Integer.toString(enviada));
					}
					
					
				} catch (IOException | ClassNotFoundException e) {
					System.out.println(erroGenerico);
				}
				break;

			case "collect":
				try {
					String g = (String) inStream.readObject();
					List<Mensagem> mensagens = CatalogoGrupos.getInstance().getGrupo(g).getCaixa();
					List<Pair<Mensagem, byte[]>> listParMenKey = new ArrayList<>();
					for(Mensagem m : mensagens) {
						if(!ServerRestorer.utilizadorTemChave(g, this.user, m.getKeyID())) {
							m.verMensagem(this.user);
							mensagens.remove(m);
						} else if(m.jaViu(this.user)) {
							mensagens.remove(m);
						}
						else {
							Pair<Mensagem, byte[]> p = new Pair<>(m, ServerRestorer.getCiferedKey(g, this.user, m.getKeyID()));
							listParMenKey.add(p);
						}
					}
					
					outStream.writeObject(":inicio:");
					for (Pair<Mensagem, byte[]> p : listParMenKey) {
						Mensagem m = (Mensagem) p.getKey();
						if (m.getTipo().equals("texto")) {
							outStream.writeObject("Texto");
							outStream.writeObject(m.getMessageID());
							outStream.writeObject(m.getConteudo().length+"");
							outStream.write(m.getConteudo());
							outStream.writeObject(p.getValue().length+"");
							outStream.write(p.getValue());
						} else {
							outStream.writeObject("Foto");
							outStream.writeObject(m.getMessageID());
							outStream.writeObject(m.getConteudo().length+"");
							outStream.write(m.getConteudo());
							outStream.writeObject(p.getValue().length+"");
							outStream.write(p.getValue());
		
							//String path = ((Fotografia) m).getConteudo().getPath();
							//String[] fullPath = path.split("\\\\");
							//String nomeSemPasta = fullPath[1];
							//String extensao = path.substring(path.indexOf('.') + 1);
						//	outStream.writeObject("Foto");
						//	outStream.writeObject(nomeSemPasta);
						//	File localizacaoVerdadeira = new File(path);
						//	BufferedImage image = ImageIO.read(localizacaoVerdadeira);

						//	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
						//	ImageIO.write(image, extensao, byteArrayOutputStream);
						//	byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
						//	output.write(size);
						//	output.write(byteArrayOutputStream.toByteArray());
						}
					}
					outStream.writeObject(":fim:");
					for (Mensagem m : mensagens) {
						m.verMensagem(user);
					}
					CatalogoGrupos.getInstance().getGrupo(g).refrescarHistorico();
				} catch (IOException | ClassNotFoundException e) {
					System.out.println(erroGenerico);
				}
				break;
					
			case "history":		
				try {
					String g = (String) inStream.readObject();
					List<Mensagem> mensagens = CatalogoGrupos.getInstance().getGrupo(g).getCaixa();
					List<Pair<Mensagem, byte[]>> listParMenKey = new ArrayList<>();
					for(Mensagem m : mensagens) {
						if(!ServerRestorer.utilizadorTemChave(g, this.user, m.getKeyID())) {
							m.verMensagem(this.user);
							mensagens.remove(m);
						} else if(m.jaViu(this.user)) {
							mensagens.remove(m);
						}
						else {
							Pair<Mensagem, byte[]> p = new Pair<>(m, ServerRestorer.getCiferedKey(g, this.user, m.getKeyID()));
							listParMenKey.add(p);
						}
					}
					outStream.writeObject(":inicio:");
					for (Pair<Mensagem, byte[]> p : listParMenKey) {
						Mensagem m = (Mensagem) p.getKey();
						if (m.getTipo().equals("Texto")) {
							outStream.writeObject("Texto");
							outStream.writeObject(m.getMessageID());
							outStream.writeObject(m.getConteudo().length+"");
							outStream.write(m.getConteudo());
							outStream.writeObject(p.getValue().length+"");
							outStream.write(p.getValue());
						}
					}
					outStream.writeObject(":fim:");
				} catch (IOException | ClassNotFoundException e) {
					System.out.println(erroGenerico);
				}
				break;
					
			case "photo":
				try {
					String grupo = (String) inStream.readObject();
					String path = (String) inStream.readObject();					
					String user = (String)inStream.readObject();
					
					if(CatalogoGrupos.getInstance().getGrupo(grupo) != null) {
						byte[] currentKey = CatalogoGrupos.getInstance().getGrupo(grupo).getChaveCurrente();
						outStream.writeObject(Integer.toString(currentKey.length));
					    outStream.flush();
					    outStream.write(currentKey);
					    outStream.flush();
					    
					    int imageSize = Integer.parseInt((String)inStream.readObject());
						byte[] encryptedImage = new byte[imageSize];
						input.read(encryptedImage);
						
						int enviada = CatalogoGrupos.getInstance().enviarMensagem(grupo, user, encryptedImage, "photo") ? 1 : -1;
						outStream.writeObject(Integer.toString(enviada));
					} else {
						int enviada = -1;
						outStream.writeObject(Integer.toString(enviada));
					}
				}catch(IOException | ClassNotFoundException i) {
					System.out.println(erroGenerico);
				}

				break;
				 

			case "exit":
				try {
					outStream.close();
					inStream.close();
					output.close();
					input.close();
					socket.close();
				} catch (IOException e) {
					System.out.println("ERRO: Não foi possivel fechar o servidor.");
				}

			default:

			}
		}
	}

	public void criaKeysGrupo(String group) throws NumberFormatException, ClassNotFoundException, IOException {
		int keysize = Integer.parseInt((String)inStream.readObject());
		byte[] encryptedKey = new byte[keysize];
		inStream.read(encryptedKey);
		System.out.println("Depois de receber a chave do grupo!");
		
		List<String> membros = CatalogoGrupos.getInstance().getGrupo(group).getUtilizadores();
		membros.add(CatalogoGrupos.getInstance().getGrupo(group).getDono());
		System.out.println(membros.toString());
		outStream.writeObject(membros.size()+"");
		for(String membro: membros) {
			outStream.writeObject(membro);
		}
		
		List<Pair<String,byte[]>> novoGrupo = new ArrayList<>();
		for(int i = 0; i < membros.size(); i++) {
			int keysize2 = Integer.parseInt((String)inStream.readObject());
			byte[] encryptedKey2 = new byte[keysize2];
			inStream.read(encryptedKey2);
	    	novoGrupo.add(new Pair<>(membros.get(i),encryptedKey2));
	 
	    }
		
		Grupo g = CatalogoGrupos.getInstance().getGrupo(group);
		for(Pair<String,byte[]> membro: novoGrupo) {
			ServerArquiver.addKeyToGroup(membro.getValue(), group, membro.getKey(), g.getKeyID()+1);
		}
		CatalogoGrupos.getInstance().getGrupo(group).setChaveCurrente(novoGrupo.get(novoGrupo.size()-1).getValue());
		g.setKeyID(g.getKeyID()+1);
	}
	/*
	private String groupInfoKey(String g) {
		return CatalogoGrupos.getInstance().groupInfoKey(g);
	}
	 */
	
	private List<String> informacaoUser() {
		return CatalogoUtilizadores.getInstance().informacaoDoUtilizador(user);
	}

	private List<String> informacaoGrupo(String g) {
		return CatalogoGrupos.getInstance().informacaoDoGrupo(g, user);
	}

	/*
	private int autentica(String u, String p) {
		return CatalogoUtilizadores.getInstance().autenticacao(u, p) ? 1 : -1;
	}*/


	private boolean createGroup(String name) {
		return CatalogoGrupos.getInstance().adicionarGrupo(name, user);
	}


	private boolean adicionarAGrupo(String grupo, String utilizador) {
		return CatalogoGrupos.getInstance().adicionarAoGrupo(grupo, user, utilizador);		
	}


	private boolean removerDoGrupo(String grupo, String utilizador) {
		return CatalogoGrupos.getInstance().removerDoGrupo(grupo, user, utilizador);		
	}
}