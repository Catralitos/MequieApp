package catalogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import domain.Grupo;
import domain.Mensagem;
import domain.Utilizador;
import files.ServerArquiver;
import files.ServerRestorer;

public class CatalogoUtilizadores {

	private static CatalogoUtilizadores singleInstance = null; 
	private String filePath = "users.enc";
	private Hashtable<String, Utilizador> tabelaUtilizadores = new Hashtable<>();

	private CatalogoUtilizadores() {
		try {
			File file = new File(filePath);
			if (file.createNewFile()) {
				System.out.println("users.enc criado.");
			} else {
				System.out.println("users.enc já existe.");
			}
			if (file.length() != 0) {
				Hashtable<String, String> tabela = new Hashtable<>(ServerRestorer.usersNoFicheiro());
				for (String utilizador : tabela.keySet()) {
					tabelaUtilizadores.put(utilizador, new Utilizador(utilizador, tabela.get(utilizador)));
				} 
			}
		} catch (IOException e) {
			System.out.println("ERRO: Não foi possivel restaurar o estado do catálogo de utilizadores.");
		}

	}

	public static CatalogoUtilizadores getInstance() {
		if (singleInstance == null) 
			singleInstance = new CatalogoUtilizadores();
		return singleInstance;
	}

	public boolean existeUser(String u) {
		return tabelaUtilizadores.containsKey(u);
	}

	public boolean autenticacao(String utilizador, String certificado) {
		try {
			ServerArquiver.escreverPar(utilizador, certificado);
		} catch (IOException e) {
			System.out.println("ERRO: Não foi possivel arquivar o utilizador.");
			return false;
		}
		Utilizador u = new Utilizador(utilizador, certificado);
		tabelaUtilizadores.put(utilizador, u);
		CatalogoGrupos.getInstance().adicionarAoGrupo("Geral", "MeQuie", utilizador);
		return true;
	}

	public List<String> getNomes() {
		return new ArrayList<>(tabelaUtilizadores.keySet());
	}

	public List<Utilizador> getUtilizadores() {
		return new ArrayList<>(tabelaUtilizadores.values());
	}

	public Utilizador getUtilizador(String utilizador) {
		return tabelaUtilizadores.get(utilizador);
	}

	public List<String> informacaoDoUtilizador(String utilizador) {
		return this.getUtilizador(utilizador).informacaoDoUtilizador();
	}

	/**
	 * Devolve as mensagens enviadas para grupo, que o utilizador ainda não viu.
	 * @param grupo
	 * @param utilizador
	 * @return
	 */
	public List<Mensagem> verMensagens(String grupo, String utilizador) {
		Grupo g = CatalogoGrupos.getInstance().getGrupo(grupo);
		ArrayList<Mensagem> mensagensAVer = new ArrayList<>();
		for (Mensagem mensagem : g.getCaixa()) {
			if (mensagem.jaViu(utilizador)) {
				mensagensAVer.add(mensagem);
				mensagem.verMensagem(utilizador);
			}
		}
		g.refrescarHistorico();
		return mensagensAVer;
	}
}
