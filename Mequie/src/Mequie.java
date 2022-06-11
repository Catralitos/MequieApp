import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Scanner;

import domain.Cliente;
import domain.Mensagem;
import response.CollectResponse;
import response.GInfoResponse;
import response.HistoryResponse;
import response.UInfoResponse;

public class Mequie {
	private static String serverAddress;
	private static String ip;
	private static String porto;
	private static int portoInt;

	public static void main(String[] args) throws Exception{
		Scanner meuScanner = new Scanner(System.in);

		if (args.length < 5) {
			System.out.println("Faltam um ou mais argumentos! É preciso um endereço do servidor, uma trustore, "
					+ "uma keystore, a password da keystore e um localUserId!");
			System.out.println("O localUserId é uma espécie de username!");
			System.out.println("Exemplo: Mequie 127.0.0.1:8080 truststoreNome keystoreNome keystorePass user1234!");
		}

		serverAddress = args[0];
		String[] dividido = serverAddress.split(":");
		ip = dividido[0];
		porto = dividido[1];
		String truststoreNome = args[1];
		String keystoreNome = args[2];
		String keystorePassword = args[3];
		System.out.println(keystorePassword);
		String localUserID = args[4];
		System.setProperty("javax.net.ssl.trustStore", truststoreNome);

		File keystoreFile = new File("keystoresUsers/"+keystoreNome);
		System.out.println(keystoreFile);

		KeyStore kstore = null;
		try {
			FileInputStream tfile = new FileInputStream(truststoreNome);
			FileInputStream kfile = new FileInputStream(keystoreFile);
			kstore = KeyStore.getInstance("JKS");
			kstore.load(kfile, keystorePassword.toCharArray());
			tfile.close();
		} catch (FileNotFoundException e) {
			System.out.println("Nome da trustore/keystore inválido!");
		} catch(KeyStoreException k) {
			System.out.println("Instância da keystore inválida!");
		} catch (CertificateException c) {
			System.out.println("Certificado não existe!");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Número de argumento errado!");
		} catch (IOException e) {
			System.out.println("Password incorreta!");
		}

		while (dividido.length < 2 || 3 != ip.chars().filter(ch -> ch == '.').count() || porto.length() < 4) {
			System.out.println("O endereco:porto nao estah no formato correcto. Insira um de formato 127.0.0.8080");
			if (meuScanner.hasNext()) {
				serverAddress = meuScanner.next();
				dividido = serverAddress.split(":");
				ip = dividido[0];
				porto = dividido[1];
			}
		}
		portoInt = Integer.parseInt(porto);

		Cliente cliente = new Cliente(localUserID, ip, portoInt);
		//AUTENTICAR
		int autenticado = cliente.autenticacao(kstore, keystorePassword);
		if(autenticado == -1) {
			System.out.println("Autenticação falhada!");
			meuScanner.close();
			return;
		} else if(autenticado == 0) {
			System.out.println("Utilizador nao existente, mas agora registrado!");
		} else {
			System.out.println("Utilizador autenticado com sucesso!");
		}
		boolean flag = true;
		String linha = "";
		System.out.println("Insira um dos seguintes comandos:");
		System.out.println("create <groupID> - criar um chat de grupo");
		System.out.println("addu <userID> <groupID> - adicionar um utilizador a um grupo");
		System.out.println("removeu <userID> <groupID> - remover um utilizador de um grupo");
		System.out.println("ginfo <groupID> - mostrar informacao sobre um grupo");
		System.out.println("uinfo - mostrar informacao sobre o utilizador");
		System.out.println("msg <groupID> <msg> - enviar uma mensagem para o grupo");
		System.out.println("photo <groupID> <photo> - enviar uma foto para o grupo");
		System.out.println("collect <groupID> - ver as mensagens de grupo nao vistas");
		System.out.println("history <groupID> - ver historico de mensagens do grupo");
		while (flag) {

			//LENDO O COMANDO
			while(linha.equals("")){
				linha = meuScanner.nextLine();
			} 

			String[] comando = linha.split(" ");

			//EXECUTANDO O COMANDO
			switch (comando[0]) {
			//CRIANDO GRUPO
			case "create":
				if (comando.length != 2) {
					System.out.println("Insira o comando no formato: create <groupID>");
				} else {
					String groupName = comando[1];
					boolean groupCommand = cliente.create(groupName);
					if(groupCommand) {
						System.out.println("Grupo criado com sucesso!");
					} else {
						System.out.println("Erro na criacao do grupo!");
					}
				}
				break;

				//ADDUSER AO GRUPO
			case "addu":
				if(comando.length != 3) {
					System.out.println("Insira o comando no formato: addu <userID> <groupID>");
				} else {
					String userID = comando[1];
					String groupID = comando[2];
					if (groupID.equals("Geral")) {
						System.out.println("User nao pode ser adicionado ao grupo!");
						break;
					}
					boolean addUser = cliente.addu(userID, groupID);
					if(addUser) {
						System.out.println("User adicionado ao grupo com sucesso!");
					} else {
						System.out.println("User nao pode ser adicionado ao grupo!");
					}
				}
				break;

				//REMOVER USER
			case "removeu":
				if(comando.length != 3) {
					System.out.println("Insira o comando no formato: removeu <userID> <groupID>");
				} else {
					String userID = comando[1];
					String groupID = comando[2];
					if (groupID.equals("Geral")) {
						System.out.println("User nao pode ser removido do grupo!");
						break;
					}
					boolean removeUser = cliente.removeu(userID, groupID);
					if(removeUser) {
						System.out.println("User removido do grupo com sucesso!");
					} else {
						System.out.println("User nao pode ser removido do grupo!");
					}
				}
				break;

				//GROUP INFO
			case "ginfo":
				if(comando.length != 2) {
					System.out.println("Insira o comando no formato: ginfo <groupID>");
				} else {
					String groupID = comando[1];
					GInfoResponse ginfo = cliente.ginfo(groupID);
					if(ginfo.isError()) {
						System.out.println("ERRO: GInfo deu erro.");
					} else {
						System.out.println(ginfo.toString());
					}
				}
				break;

				//USER INFO
			case "uinfo":
				if(comando.length != 1) {
					System.out.println("Insira o comando no formato: uinfo");
				} else {
					UInfoResponse uinfo = cliente.uinfo();
					if(uinfo.getError()) {
						System.out.println("Utilizador não pertence e nem é dono de nenhum grupo");
					} else {
						System.out.println(uinfo.toString());
					}
				}
				break;

				//MESSAGE
			case "msg":
				if(comando.length < 3) {
					System.out.println("Insira o comando no formato: msg <groupID> <msg>");
				} else {
					String groupID = comando[1];
					if (groupID.equals("Geral")) {
						System.out.println("User nao pode enviar mensagem para esse grupo!");
						break;
					}

					boolean enviado = cliente.msg(groupID, linha.trim().substring(5 + groupID.length(), linha.length()));
					if(enviado) {
						System.out.println("Mensagem enviada com sucesso!");
					} else {
						System.out.println("Falha ao enviar a mensagem!");
					}
				}
				break;

				//PHOTO
			case "photo":
				if(comando.length != 3) {
					System.out.println("Insira o comando no formato: photo <groupID> <msg>");
				} else {
					String groupID = comando[1];
					if (groupID.equals("Geral")) {
						System.out.println("User nao pode enviar foto para esse grupo!");
						break;
					}
					boolean enviado = cliente.photo(groupID, comando[2]);
					if(enviado) {
						System.out.println("Foto enviada com sucesso!");
					} else {
						System.out.println("Falha ao enviar a foto!");
					}
				}
				break;

				//COLLECT
			case "collect":
				if(comando.length != 2) {
					System.out.println("Insira o comando no formato: collect <groupID>");
				} else {
					String groupID = comando[1];
					CollectResponse collect = cliente.collect(groupID);
					ArrayList<Mensagem> msgs = new ArrayList<>(collect.getMensagens());
					for (Mensagem m : msgs) {
						if (m.getTipo().equals("Texto")) {
							System.out.println(":" + m.getConteudo());
						}// else if (m instanceof Fotografia) {
							//System.out.println(m.getRemetente() + ":" + ((Fotografia) m).getConteudo().getPath());
						//}
					}
					if(collect.isError()) {
						System.out.println("Erro ao buscar as mensagens!");
					} else {
						System.out.println("Sucesso!");
					}
				}
				break;


				//HISTORY
			case "history":
				if(comando.length != 2) {
					System.out.println("Insira o comando no formato: history <groupID>");
				} else {
					String groupID = comando[1];
					HistoryResponse history = cliente.history(groupID);
					ArrayList<Mensagem> msgs = new ArrayList<>(history.getHistory());
					for (Mensagem t : msgs) {
						//System.out.println(t.getRemetente() + ":" + t.getConteudo());
					}
					if(history.isError()) {
						System.out.println("Erro ao buscar as mensagens!");
					} else {
						System.out.println("Sucesso!");
					}
				}
				break;

				//EXIT
			case "exit":
				System.out.println("Encerrando o programa!");
				cliente.quit();
				flag = false;

				//DEFAULT
			default:

			}
			linha = "";
		}
		meuScanner.close();
	}
}
