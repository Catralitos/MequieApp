
package files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;

import catalogs.CatalogoChaves;
import catalogs.CatalogoGrupos;
import domain.Grupo;
import domain.Mensagem;

public class ServerArquiver {

	private ServerArquiver() {
		//do nothing
	}

	public static void escreverPar(String utilizador, String certificado) throws IOException {
		File encriptado = new File ("users.enc");
		if (encriptado.exists() && encriptado.length() > 0) {
			CatalogoChaves.getInstance().decriptarFicheiros("users.enc", "users.txt");
		}
		File file = new File("users.txt");
		FileWriter fileWriter = new FileWriter(file, true);
		BufferedWriter writer = new BufferedWriter(fileWriter);
		String aEscrever = utilizador + "<>" + certificado + "\n";
		writer.write(aEscrever);
		writer.close();
		fileWriter.close();
		CatalogoChaves.getInstance().encriptarFicheiro("users.txt", "users.enc");
		if (!file.delete()) {
			System.out.println("ERRO: Não foi possivel apagar users.txt no arquiver.");
		}
	}

	public static void escreverGrupo(Grupo grupo) throws IOException {
		File encriptado = new File ("groups.enc");
		if (encriptado.exists() && encriptado.length() > 0) {
			CatalogoChaves.getInstance().decriptarFicheiros("groups.enc", "groups.txt");
		}
		File file = new File("groups.txt");
		FileWriter fileWriter = new FileWriter(file, true);
		BufferedWriter writer = new BufferedWriter(fileWriter);
		StringBuilder sb = new StringBuilder();
		sb.append("<gcg>");
		sb.append(grupo.getNome() + "<>");
		sb.append(grupo.getKeyID() + "<>");
		sb.append(grupo.getChaveCurrente() + "<>");
		sb.append(grupo.getDono() + "<>");
		for (String utilizador : grupo.getUtilizadores()) {
			sb.append(utilizador + "<>");
		}
		if (sb.length() > 1) {
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("<gcg>");
		sb.append("\n");
		writer.write(sb.toString());
		writer.close();
		fileWriter.close();
		CatalogoChaves.getInstance().encriptarFicheiro("groups.txt", "groups.enc");
		if (!file.delete()) {
			System.out.println("ERRO: Não foi possivel apagar groups.txt no arquiver.");
		}
	}

	public static void updateGrupos() throws IOException {
		File file = new File("groups.enc");
		boolean apagado = file.delete();
		if (!apagado) {
			System.out.println("ERRO: O ficheiro dos grupos não foi apagado");
		} else {
			//quero so limpar o ficheiro nao se se é boa ideia apagá-lo
			file.createNewFile();
		}
		List<Grupo> grupos = CatalogoGrupos.getInstance().getGrupos();
		for (Grupo g : grupos) {
			ServerArquiver.escreverGrupo(g);
		}

	}

	public static void escreverMensagem(String filePath, Mensagem mensagem) throws IOException {
		File encriptado = new File ("ArquivoGrupos/"+ filePath + ".enc");
		if (encriptado.exists() && encriptado.length() > 0) {
			CatalogoChaves.getInstance().decriptarFicheiros("ArquivoGrupos/"+ filePath + ".enc", "ArquivoGrupos/"+ filePath + ".txt");
		}
		File file = new File("ArquivoGrupos/" + filePath + ".txt");
		FileWriter fileWriter = new FileWriter(file, true);
		BufferedWriter writer = new BufferedWriter(fileWriter);
		StringBuilder sb = new StringBuilder();
		sb.append("<gcg>");
		sb.append(mensagem.getKeyID() + "<>");
		sb.append(mensagem.getMessageID() + "<>");
		sb.append(mensagem.getTipo() + "<>");
		sb.append(new String(mensagem.getConteudo()) + "<>");
		for (String user: mensagem.getJaViram()) {
			sb.append(user + "<>");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("<gcg>");
		sb.append("\n");
		writer.write(sb.toString());
		writer.close();
		fileWriter.close();
		CatalogoChaves.getInstance().encriptarFicheiro("ArquivoGrupos/"+ filePath + ".txt", "ArquivoGrupos/"+ filePath + ".enc");
		if (!file.delete()) {
			System.out.println("ERRO: Não foi possivel apagar ArquivoGrupos/" + filePath + ".txt no arquiver.");
		}
	}

	public static void updateMensagens(Grupo grupo) throws IOException {
		String pathCaixa = grupo.getNome() + "/caixa";
		String pathHistorico = grupo.getNome() + "/historico";
		File fileCaixa = new File(pathCaixa);
		if (fileCaixa.exists()) {
			boolean apaga = fileCaixa.delete();
			if (!apaga) {
				System.out.println("ERRO: Não foi possivel apagar ficheiro de caixa de mensagens.");
			}
		}			
		File fileHistorico = new File(pathHistorico);
		if (fileHistorico.exists()) {
			boolean apaga = fileHistorico.delete();
			if (!apaga) {
				System.out.println("ERRO: Não foi possivel apagar ficheiro de histórico de mensagens.");
			}
		}
		for (Mensagem m : grupo.getCaixa()) {
			ServerArquiver.escreverMensagem(pathCaixa, m);
		}
		for (Mensagem m : grupo.getHistorico()) {
			ServerArquiver.escreverMensagem(pathHistorico, m);
		}
	}

	public static void addKeyToGroup(byte[] wrappedKey, String groupName, String userName, int keyNum) throws IOException {
		//TODO se houver tempo, encriptar estes ficheiros
		
		//CRIAR OS DIRETORIOS
		new File("ArquivoGrupos/" + groupName).mkdir();
		new File("ArquivoGrupos/" + groupName + "/Keys").mkdir();

		//IR BUSCAR OS PATHS
		String pathListPairs = "ArquivoGrupos/" + groupName + "/listaParesGrupo.cif";
		String pathKeysUser = "ArquivoGrupos/" + groupName + "/Keys/"+ userName + ".cif";
		File filePairs = new File(pathListPairs);

		//ESCREVER A LISTA DE PARES CIFRADA
		Cipher c = CatalogoChaves.getInstance().getFileEncryptCipher();
		CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(filePairs, true), c);
		String aEscrever = userName + "<>" + "Keys/" + userName + ".cif\n";
		cos.write(aEscrever.getBytes());
		cos.close();

		//ESCREVER O IDCHAVE:CHAVE (A CHAVE JA VEM CIFRADA, LOGO NAO E PRECISO CIFRAR ISTO)
		File fileChaves = new File(pathKeysUser);
		FileWriter fileWriterChaves = new FileWriter(fileChaves, true);
		BufferedWriter writerChaves = new BufferedWriter(fileWriterChaves);
		writerChaves.write(keyNum + "<>" + new String(wrappedKey) + "\n");
		writerChaves.close();
		fileWriterChaves.close();
	}

	public static boolean deleteKeyFile(String grupo, String utilizador) {
		File keyFile = new File("ArquivoGrupos/" + grupo + "/keys/" + utilizador + ".cif");
		return keyFile.delete();
	}

}
