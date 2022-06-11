import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import connection.ServerThread;

public class MequieServer {
	private static Socket socket;
	private static ServerSocketFactory ssf;
	private static SSLServerSocket ss;

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Faltam um ou mais argumentos! É preciso um porto TCP, uma keystore e a password da keystore!");
			System.out.println("Exemplo: MequieServer 8080 keystoreNome keystorePassword");
		}
		
		int porto = Integer.parseInt(args[0]);
		String keystoreNome = args[1];
		String keystorePassword = args[2];
		System.setProperty("javax.net.ssl.keyStore", keystoreNome);
		System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
		
		try {
			FileInputStream kfile = new FileInputStream(keystoreNome);
			KeyStore kstore = KeyStore.getInstance("JCEKS");
			kstore.load(kfile, keystorePassword.toCharArray());
		} catch (FileNotFoundException e) {
			System.out.println("Nome da keystore inválido!");
		} catch(KeyStoreException k) {
			System.out.println("Instância da keystore inválida!");
		} catch (CertificateException c) {
			System.out.println("Certificado não existe!");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Número de argumento errado!");
		} catch (IOException e) {
			System.out.println("Password incorreta!");
		}
		File pubKeys = new File("PubKeys");
	    if (!pubKeys.exists()){
	        pubKeys.mkdir();
	        // If you require it to make the entire directory path including parents,
	        // use directory.mkdirs(); here instead.
	    }
	    File arquivoGrupos = new File("ArquivoGrupos");
	    if (!arquivoGrupos.exists()){
	    	arquivoGrupos.mkdir();
	        // If you require it to make the entire directory path including parents,
	        // use directory.mkdirs(); here instead.
	    }

		MequieServer servidor = new MequieServer();
		servidor.comecarServidor(porto);
	}
	
	public void comecarServidor (int porto){
		try {
			ssf = SSLServerSocketFactory.getDefault();
			ss = (SSLServerSocket) ssf.createServerSocket(porto);
			System.out.println("MequieServer started");
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		boolean flag = true;
		while(flag) 
			try {
				System.out.println("Waiting for a client ...");
				socket = ss.accept();
				System.out.println("Client accepted");
				Thread thread = new ServerThread(socket);
			    thread.start();
			} catch(IOException i) {
				try {
					System.out.println("ERRO: A fechar o servidor...");
					ss.close();
					flag = false;
				} catch (IOException o) {
					System.out.println("ERRO: Não foi possivel fechar o servidor.");
				}
			}
		}
}
