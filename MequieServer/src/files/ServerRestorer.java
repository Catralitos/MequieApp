package files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;

import catalogs.CatalogoChaves;
import domain.TuploMensagem;

public class ServerRestorer {

	private ServerRestorer() {

	}

	public static Map<String, String> usersNoFicheiro() throws IOException{
		Map<String,String> rt = new HashMap<>();
		CatalogoChaves.getInstance().decriptarFicheiros("users.enc", "users.txt");
		File file = new File("users.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		while((st = br.readLine()) != null) {
			String[] dados = new String(st).split("<>");
			rt.put(dados[0], dados[1]);
		}
		br.close();
		if (!file.delete()) {
			System.out.println("ERRO: Não foi possivel apagar users.txt no restorer.");
		}
		return rt;
	}

	public static Map<String, List<String>> gruposNoFicheiro() throws IOException {
		Map<String,List<String>> rt = new HashMap<>();
		CatalogoChaves.getInstance().decriptarFicheiros("groups.enc", "groups.txt");
		File file = new File("groups.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		StringBuilder sb = new StringBuilder();
		while((st = br.readLine()) != null) {
			if (st.substring(st.length() - 5).equals("<gcg>")) {
				if (st.substring(0,5).equals("<gcg>")){
                    sb.append(st.substring(5, st.length() - 5));
                } else {
                    sb.append(st);
                }
				String[] groupData = sb.toString().split("<>");
				String nome = groupData[0];
				List<String> ls = new ArrayList<>();
				for (int i = 1; i < groupData.length; i++) {
					ls.add(groupData[i]);
				}
				rt.put(nome, ls);
				sb.delete(0, sb.length());
			} else {
				if (st.substring(0, 5).equals("<gcg>")){ 
					sb.append(st.substring(5)); 
				} else { 
					sb.append(st);
				}
			}
		}
		br.close();
		if (!file.delete()) {
			System.out.println("ERRO: Não foi possivel apagar groups.txt no restorer.");
		}
		return rt;
	}

	public static List<TuploMensagem> mensagensNoFicheiro(String path) throws IOException {
		List<TuploMensagem> rt = new ArrayList<>();
		CatalogoChaves.getInstance().decriptarFicheiros(path + ".enc", path + ".txt");
		File file = new File(path + ".txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		StringBuilder sb = new StringBuilder();
		while((st = br.readLine()) != null) {
			if (st.length() >= 5 && st.substring(st.length() - 5).equals("<gcg>")){
				if (st.substring(0,5).equals("<gcg>")){
                    sb.append(st.substring(5, st.length() - 5));
                } else {
                    sb.append(st);
                }
				String[] messageData = sb.toString().split("<>");
				int keyID = Integer.parseInt(messageData[0]);
				String messageID = messageData[1];
				String tipo = messageData[2];
				byte[] conteudo = messageData[3].getBytes();
				List<String> users = new ArrayList<>();
				for (int i = 4; i < messageData.length; i++) {
					users.add(messageData[0]);
				}
				TuploMensagem tuplo = new TuploMensagem(keyID, messageID, conteudo, users, tipo);
				rt.add(tuplo);
				sb.delete(0, sb.length());
			} else {
				if (st.length() >= 5 && st.substring(0, 5).equals("<gcg>")){
					if(st.length() >= 5) {
						sb.append(st.substring(5)); 
					}	
				} else { 
					sb.append(st);
				}
			}

		}
		br.close();
		if (!file.delete()) {
			System.out.println("ERRO: Não foi possivel apagar" + path + ".txt no restorer.");
		}
		return rt;
	}

	public static int getOldestKey(String grupo, String utilizador) throws IOException {
		File keyFile = new File("ArquivoGrupos/" + grupo + "/keys/" + utilizador + ".cif");
		BufferedReader br = new BufferedReader(new FileReader(keyFile));
		String st = br.readLine();
		if (st == null) {
			br.close();
			return -1;
		}
		String[] messageData = st.split("<>");
		int resultado = Integer.parseInt(messageData[0]);
		br.close();
		return resultado;
	}

	public static byte[] getCiferedKey(String grupo, String utilizador, int keyID) throws IOException {
		File keyFile = new File("ArquivoGrupos/" + grupo + "/keys/" + utilizador + ".cif");
		String st;
		BufferedReader br = new BufferedReader(new FileReader(keyFile));
		while((st = br.readLine()) != null) {
			String[] messageData = st.split("<>");
			int resultado = Integer.parseInt(messageData[0]);
			if (resultado == keyID) {
				br.close();
				return messageData[1].getBytes();
			}
		}	
		br.close();
		return new byte [0];
	}

	public static boolean utilizadorTemChave(String grupo, String utilizador, int keyID) throws IOException {
		File keyFile = new File("ArquivoGrupos/" + grupo + "/keys/" + utilizador + ".cif");
		String st;
		BufferedReader br = new BufferedReader(new FileReader(keyFile));
		while((st = br.readLine()) != null) {
			String[] messageData = st.split("<>");
			int resultado = Integer.parseInt(messageData[0]);
			if (resultado == keyID) {
				br.close();
				return true;
			}
		}	
		br.close();
		return false;
	}

}
