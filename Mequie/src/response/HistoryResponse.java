package response;

import java.util.ArrayList;
import java.util.List;

import domain.Mensagem;

public class HistoryResponse {
	private boolean error = false;
	private List<Mensagem> history = new ArrayList<>();
	
	public HistoryResponse() {
		
	}

	public HistoryResponse(boolean error) {
		super();
		this.error = error;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public List<Mensagem> getHistory() {
		return history;
	}

	public void setHistory(List<Mensagem> history) {
		this.history = history;
	}
	
	public void addMensagem(Mensagem t) {
		this.history.add(t);
	}
	
	
}
