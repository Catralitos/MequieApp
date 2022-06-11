package domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mensagem {
	
	private String messageID;
	private String tipo;
	private byte[] conteudo;
	
	public Mensagem(String messageID, byte[] conteudo, String tipo) {
		this.messageID = messageID;
		this.conteudo = conteudo;
		this.tipo = tipo;
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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
}
