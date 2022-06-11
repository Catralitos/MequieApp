package domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import files.ServerArquiver;

public class Grupo {

	private String nome;
	private String dono;
	private byte[] chaveCurrente;
	private ArrayList<String> utilizadores;
	private ArrayList<Mensagem> caixaMensagens;
	private ArrayList<Mensagem> historicoMensagens;
	private int keyID = 0;


	public Grupo(String nome, String dono) {
		this.nome = nome;
		this.dono = dono;
		this.utilizadores = new ArrayList<>();
		this.caixaMensagens = new ArrayList<>();
		this.historicoMensagens = new ArrayList<>();
		this.chaveCurrente = new byte[0];
	}

	public byte[] getChaveCurrente() {
		return chaveCurrente;
	}

	public void setChaveCurrente(byte[] chaveCurrente) {
		this.chaveCurrente = chaveCurrente;
	}

	public String getNome() {
		return nome;
	}

	public String getDono() {
		return dono;
	}

	public boolean ehDono(String utilizador) {
		return dono.equals(utilizador);
	}

	public List<String> getUtilizadores(){		
		return new ArrayList<>(utilizadores);
	}

	public boolean adicionarUtilizador(String utilizador) {
		boolean add = utilizadores.add(utilizador);
		if(add) {
			keyID++;
		}
		return add;
	}

	public boolean removerUtilizador(String utilizador) {
		utilizadores.remove(utilizador);
		if(!utilizadores.contains(utilizador)){
			keyID++;
		}
		return !utilizadores.contains(utilizador);
	}

	public boolean contemUtilizador(String utilizador) {
		return utilizadores.contains(utilizador) || utilizador.equals(dono);
	}

	public List<String> informacaoDoGrupo(String u) {
		ArrayList<String> rt = new ArrayList<>();
		rt.add(dono);
		rt.add(Integer.toString(1 + utilizadores.size()));
		if (u.equals(dono)) {
			//mete chave de grupo
			for(String user: utilizadores) {
				rt.add(user);
			}
		}
		return rt;
	}

	public int getKeyID() {
		return keyID;
	}

	public void setKeyID(int currentKey) {
		this.keyID = currentKey;
	}

	public boolean adicionarMensagem(Mensagem m) {
		return caixaMensagens.add(m);
	}

	public List<Mensagem> getCaixa() {
		return new ArrayList<>(caixaMensagens);
	}

	public List<Mensagem> getHistorico() {
		return new ArrayList<>(historicoMensagens);
	}

	public void refrescarHistorico() {
		ArrayList<Mensagem> aMudar = new ArrayList<>();
		for (Mensagem mensagem : caixaMensagens) {
			boolean todosViram = true;
			if (!mensagem.jaViu(dono)) {
				todosViram = false;
			}
			if (todosViram) {
				for (String u : utilizadores) {
					if ((!mensagem.jaViu(u) && mensagem.temAcesso(this.nome, u))|| !mensagem.jaViu(dono)) {
						todosViram = false;
					}
				}
			}
			if (todosViram) {
				aMudar.add(mensagem);
			}
		}
		for (Mensagem mensagem : aMudar) {
			if (mensagem.getTipo().equals("Texto")) {
				historicoMensagens.add(mensagem);
			} 
			caixaMensagens.remove(mensagem);
		}
		try {
			ServerArquiver.updateMensagens(this);
		} catch (IOException e) {
			System.out.println("ERRO: Não foi possivel atualizar os ficheiros de mensagens.");
		}
	}

	public void setCaixa(List<Mensagem> caixaGrupo) {
		this.caixaMensagens = new ArrayList<>(caixaGrupo);		
	}

	public void setHistorico(List<Mensagem> historicoGrupo) {
		this.historicoMensagens = new ArrayList<>(historicoGrupo);		
	}

	public void removerMensagemCaixa(Mensagem m) {
		this.caixaMensagens.remove(m);
	}
}
