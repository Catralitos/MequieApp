package response;

import java.util.ArrayList;
import java.util.List;

import domain.Mensagem;

public class CollectResponse {
	private boolean error = false;
	private List<Mensagem> mensagens = new ArrayList<>();
	
	public CollectResponse() {
		
	}
	
	public CollectResponse(boolean error) {
		super();
		this.error = error;
	}

	public CollectResponse(boolean error, List<Mensagem> mensagens) {
		super();
		this.error = error;
		this.mensagens = mensagens;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public List<Mensagem> getMensagens() {
		return mensagens;
	}

	public void setMensagens(List<Mensagem> mensagens) {
		this.mensagens = mensagens;
	}
	
	public void addMensagem(Mensagem t) {
		mensagens.add(t);
	}

	public void addMensagem(String messageId, byte[] descifrado, String string) {
		mensagens.add(new Mensagem(messageId, descifrado, string));
	}

}
