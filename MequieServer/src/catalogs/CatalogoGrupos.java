package catalogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import domain.Grupo;
import domain.Mensagem;
import domain.TuploMensagem;
import files.ServerArquiver;
import files.ServerRestorer;

public class CatalogoGrupos {

	private static CatalogoGrupos singleInstance = null; 

	private Hashtable<String, Grupo> tabelaGrupos = new Hashtable<>();
	private String filePath = "groups.enc";

	private CatalogoGrupos() {	
		try {
			//Garantir que o ficheiro existe
			File file = new File(filePath);
			if (file.createNewFile()) {
				System.out.println("groups.enc criado.");
			} else {
				System.out.println("groups.enc já existe.");
			}

			if (file.length() != 0) {
				HashMap<String, List<String>> tabelaFicheiro = new HashMap<>(ServerRestorer.gruposNoFicheiro());
				for (String grupo : tabelaFicheiro.keySet()) {
					//PASSO 1 criar um grupo, com o dono (dono é String, nao Utilizador)
					System.out.println("A restaurar grupo: " + grupo);
					Grupo g = new Grupo(grupo, tabelaFicheiro.get(grupo).get(2));
					
					//PASSO 1.5 tratar das chaves
					String keyId = tabelaFicheiro.get(grupo).get(0);
					if (keyId.length() > 0) {
						g.setKeyID(Integer.parseInt(tabelaFicheiro.get(grupo).get(0)));
					}
					g.setChaveCurrente(tabelaFicheiro.get(grupo).get(1).getBytes());
					
					//PASSO 2 meter os utilizadores
					for (int i = 3; i < tabelaFicheiro.get(grupo).size(); i++) {
						g.adicionarUtilizador(tabelaFicheiro.get(grupo).get(i));
					}
					tabelaGrupos.put(grupo, g);
					
					//PASSO 3 Ir a pasta onde estao os ficheiros com as mensagens
					String pathMensagens = "ArquivoGrupos/" + grupo + "/";

					File pasta = new File("ArquivoGrupos/" + grupo);
					if (pasta.exists() && pasta.isDirectory()) {
						//PASSO 4 repopular a caixa
						File fileCaixa = new File(pathMensagens + "inbox.enc");
						if (fileCaixa.exists()) {
							List<TuploMensagem> caixaMensagens = ServerRestorer.mensagensNoFicheiro(pathMensagens + "inbox");
							ArrayList<Mensagem> caixaGrupo = new ArrayList<>();
							for (TuploMensagem q : caixaMensagens) {
								Mensagem m = CatalogoMensagens.getInstance().recuperarMensagem(q.getKeyID(), q.getMessageID(), q.getConteudo(), q.getUtilizadores(), q.getTipo());
							    caixaGrupo.add(m);
							}
							tabelaGrupos.get(grupo).setCaixa(caixaGrupo);
						}
						//PASSO 5 repopular o historico
						File fileHistorico = new File(pathMensagens + "history.enc");
						if (fileHistorico.exists()) {
							List<TuploMensagem> historicoMensagens = ServerRestorer.mensagensNoFicheiro(pathMensagens + "history");
							ArrayList<Mensagem> historicoGrupo = new ArrayList<>();
							for (TuploMensagem q : historicoMensagens) {
								Mensagem m = CatalogoMensagens.getInstance().recuperarMensagem(q.getKeyID(), q.getMessageID(), q.getConteudo(), q.getUtilizadores(), q.getTipo());
							    historicoGrupo.add(m);
							}
							tabelaGrupos.get(grupo).setHistorico(historicoGrupo);
						}
					}
				} 
			}
			if (!this.tabelaGrupos.containsKey("Geral")) {
				Grupo g = new Grupo ("Geral", "MeQuie");
				this.tabelaGrupos.put("Geral", g);
				ServerArquiver.escreverGrupo(g);
			}
		} catch (IOException e) {
			System.out.println("ERRO: Não foi possivel restaurar o estado do catálogo de grupos.");
		}

	}

	public static CatalogoGrupos getInstance() {
		if (singleInstance == null) 
			singleInstance = new CatalogoGrupos(); 

		return singleInstance;
	}

	public Grupo getGrupo(String grupo) {
		return tabelaGrupos.get(grupo);
	}

	public List<Grupo> getGrupos(){
		return new ArrayList<>(this.tabelaGrupos.values());
	}

	public boolean adicionarGrupo(String grupo, String dono) {
		if (tabelaGrupos.get(grupo) != null && tabelaGrupos.get(grupo).getDono().equals(dono)) {
			System.out.println("ERRO: O grupo já existe.");
			return false;
		}
		tabelaGrupos.put(grupo, new Grupo(grupo, dono));
		if (tabelaGrupos.containsKey(grupo)) {
			try {
				ServerArquiver.escreverGrupo(this.tabelaGrupos.get(grupo));
			} catch (IOException e) {
				System.out.println("ERRO: Não foi possivel arquivar o grupo.");
			}
			return true;
		}
		System.out.println("ERRO: Não foi possivel criar o grupo.");
		return false;
	}

	public boolean adicionarAoGrupo(String grupo, String dono, String username) {
		if(tabelaGrupos.get(grupo) == null) {
			System.out.println("ERRO: Grupo não existe");
			return false;
		}
		Grupo g = tabelaGrupos.get(grupo);
		if (!g.ehDono(dono)) {
			System.out.println("ERRO: Utilizador não é dono, logo não tem permissão para adicionar ao grupo.");
			return false;
		} else if (!CatalogoUtilizadores.getInstance().existeUser(username)) {
			System.out.println("ERRO: Utilizador a ser adicionado não existe.");
			return false;
		} else if (g.contemUtilizador(username) || (g.ehDono(dono) && dono.equals(username))) {
			System.out.println("ERRO: Utilizador já está no grupo");
			return false;
		}
		g.adicionarUtilizador(username);
		try {
			ServerArquiver.updateGrupos();
		} catch (IOException e) {
			System.out.println("ERRO: Não foi possivel adicionar utilizador ao grupo.");
			g.removerUtilizador(username);
		}
		return true;
	}

	public boolean removerDoGrupo(String grupo, String dono, String username) {
		Grupo g = tabelaGrupos.get(grupo);
		if (g == null || !g.ehDono(dono)) {
			return false;
		}
		boolean feito = false;
		if(g.ehDono(dono) && dono.equals(username)) {
			if(g.getUtilizadores().isEmpty()) {
				tabelaGrupos.remove(grupo);
				feito = true;
			} else {
				return false;
			}
		} else {
			feito = g.removerUtilizador(username);
		}
		try {
			ServerArquiver.updateGrupos();
			ServerArquiver.deleteKeyFile(grupo, username);
		} catch (IOException e) {
			System.out.println("ERRO: Não foi possivel remover o utilizador do grupo.");
			g.adicionarUtilizador(username);
			return false;
		}
		return feito;
	}


	public List<String> informacaoDoGrupo(String grupo, String username) {
		Grupo g = tabelaGrupos.get(grupo);
		if (g == null) {
			return new ArrayList<>();
		}
		return tabelaGrupos.get(grupo).informacaoDoGrupo(username);
	}

	public boolean enviarMensagem(String grupo, String utilizador, byte[] mensagem, String tipo) {
		Grupo g = tabelaGrupos.get(grupo);
		if (g == null || !g.contemUtilizador(utilizador)) {
			return false;
		}	
		Mensagem m = CatalogoMensagens.getInstance().novaMensagem(g.getKeyID(), mensagem, utilizador, tipo);
		boolean adicionou = g.adicionarMensagem(m);
		if (adicionou) {
			try {
				ServerArquiver.escreverMensagem(grupo + "/inbox", m);
			} catch (IOException e) {
				System.out.println("ERRO: Não foi possivel guardar a mensagem.");
				g.removerMensagemCaixa(m);
			}
			return true;
		}
		return false;
	}

	public List<Mensagem> collect(String grupo, String utilizador){
		//TODO se calhar meter mais ifs, passar algum codigo apra fora, para o caso de algo rebentar
		//poder-se reverter mais facilmente para um estado mais estável

		int antigoKeyID = -1;
		try {
			antigoKeyID = ServerRestorer.getOldestKey(grupo, utilizador);
		} catch (IOException e) {
			System.out.println("ERRO: O utilizador não tem nenhuma chave deste grupo.");
			return new ArrayList<>();
		}
		Grupo g = tabelaGrupos.get(grupo);
		List<Mensagem> caixa = g.getCaixa();
		List<Mensagem> resultado = new ArrayList<>();
		if (!g.contemUtilizador(utilizador) || antigoKeyID < 0) {
			System.out.println("ERRO: O utilizador não pertence a este grupo.");
			return resultado;
		}
		for (Mensagem m : caixa) {
			if (m.getKeyID() >= antigoKeyID) {
				resultado.add(m);
				m.jaViu(utilizador);
			}
		}
		g.refrescarHistorico();
		return resultado;
	}

	public List<Mensagem> history(String grupo, String utilizador){
		//TODO se calhar meter mais ifs, passar algum codigo apra fora, para o caso de algo rebentar
		//poder-se reverter mais facilmente para um estado mais estável

		int antigoKeyID = -1;
		try {
			antigoKeyID = ServerRestorer.getOldestKey(grupo, utilizador);
		} catch (IOException e) {
			System.out.println("ERRO: O utilizador não tem nenhuma chave deste grupo.");
			return new ArrayList<>();
		}
		Grupo g = tabelaGrupos.get(grupo);
		List<Mensagem> historico = g.getHistorico();
		List<Mensagem> resultado = new ArrayList<>();
		if (!g.contemUtilizador(utilizador) || antigoKeyID < 0) {
			System.out.println("ERRO: O utilizador não pertence a este grupo.");
			return resultado;
		}
		for (Mensagem m : historico) {
			if (m.getKeyID() >= antigoKeyID) {
				resultado.add(m);
			}
		}
		g.refrescarHistorico();
		return resultado;
	}

}
