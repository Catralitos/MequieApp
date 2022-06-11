package domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import files.ServerRestorer;

public class Mensagem {
	
	private String messageID;
	private String tipo;
	private byte[] conteudo;
	private int keyID;
	protected ArrayList<String> jaViram = new ArrayList<>();
	
	public Mensagem(String messageID, byte[] conteudo, String tipo) {
		this.messageID = messageID;
		this.conteudo = conteudo;
		this.tipo = tipo;
	}
	
	public Mensagem(int keyID, String messageID, byte[] conteudo, String tipo) {
		this.messageID = messageID;
		this.conteudo = conteudo;
		this.keyID = keyID;
		this.tipo = tipo;
	}
	
	public boolean verMensagem(String utilizador) {
		if (!this.jaViu(utilizador)) {
			return jaViram.add(utilizador);
		}
		return true;
	}
	
	public boolean jaViu(String utilizador) {
		return jaViram.contains(utilizador);
	}

	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	public byte[] getConteudo() {
		return conteudo;
	}

	public void setConteudo(byte[] conteudo) {
		this.conteudo = conteudo;
	}

	public int getKeyID() {
		return keyID;
	}

	public void setKeyID(int keyID) {
		this.keyID = keyID;
	}

	public List<String> getJaViram() {
		return jaViram;
	}

	public void setJaViram(List<String> utilizadores) {
		this.jaViram = new ArrayList<>(utilizadores);
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public boolean temAcesso(String g, String u) {
		try {
			return ServerRestorer.utilizadorTemChave(g, u, this.getKeyID());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
