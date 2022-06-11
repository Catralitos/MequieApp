package catalogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import domain.Mensagem;

public class CatalogoMensagens {

	private static CatalogoMensagens singleInstance = null;
	ArrayList<Mensagem> listaMensagens;

	private CatalogoMensagens() {
		this.listaMensagens = new ArrayList<>();
	}

	public static CatalogoMensagens getInstance() {
		if (singleInstance == null) 
			singleInstance = new CatalogoMensagens(); 

		return singleInstance;
	}
	
	public Mensagem novaMensagem(int keyID, byte[] conteudo, String remetente, String tipo) {
		Mensagem m = new Mensagem(keyID, UUID.randomUUID().toString(), conteudo, tipo);
		m.verMensagem(remetente);
		listaMensagens.add(m);
		return m;
	}

	public Mensagem recuperarMensagem(int keyID, String messageID, byte[] conteudo, List<String> utilizadores, String tipo) {
		Mensagem m = new Mensagem(messageID, conteudo, tipo);
		m.setJaViram(utilizadores);
		m.setKeyID(keyID);
		listaMensagens.add(m);
		return m;
	}
}
