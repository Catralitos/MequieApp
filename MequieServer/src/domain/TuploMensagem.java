package domain;

import java.util.ArrayList;
import java.util.List;

public class TuploMensagem {

	private int keyID;
	private String messageID;
	private String tipo;
	private byte[] conteudo;
	private ArrayList<String> utilizadores;

    public TuploMensagem(int keyID, String messageID, byte[] conteudo, List<String> utilizadores, String tipo) {
       this.keyID = keyID;
       this.messageID = messageID;
       this.conteudo = conteudo;
       this.utilizadores = new ArrayList<>(utilizadores);
       this.tipo = tipo;
    }

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getKeyID() {
		return keyID;
	}

	public void setKeyID(int keyID) {
		this.keyID = keyID;
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

	public List<String> getUtilizadores() {
		return new ArrayList<>(utilizadores);
	}

	public void setUtilizadores(List<String> utilizadores) {
		this.utilizadores = new ArrayList<>(utilizadores);
	}

}