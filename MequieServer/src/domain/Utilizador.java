package domain;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import catalogs.CatalogoGrupos;

public class Utilizador {

	private String nome;
	private String certificado;
	private ArrayList<Grupo> grupos;

	public Utilizador(String utilizador, String certificado) {
		this.nome = utilizador;
		this.certificado = certificado;
		this.grupos = new ArrayList<>();
	}

	public String getNome() {
		return nome;
	}

	public boolean adicionarGrupo(Grupo grupo) {
		return grupos.add(grupo);
	}
	
	public List<Grupo> getGrupos(){
		return grupos;
	}

	public List<String> informacaoDoUtilizador() {
		this.grupos = (ArrayList<Grupo>) CatalogoGrupos.getInstance().getGrupos();
		List<String> lista = new ArrayList<>();
		lista.add("MembroDe:");
		for (Grupo grupo : grupos) {
			if (grupo.getUtilizadores().contains(this.nome)) {
				lista.add(grupo.getNome());
			}
		}
		lista.add("middle");
		lista.add("DonoDe:");
		for (Grupo grupo : grupos) {
			if (grupo.ehDono(nome)) {
				lista.add(grupo.getNome());
			}
		}
		StringBuilder gps = new StringBuilder("MembroDe:");
		gps.append("MembroDe:");
		for (Grupo grupo : grupos) {
			if (grupo.getUtilizadores().contains(this.nome)) {
				gps.append(grupo.getNome() + ", ");
			}
		}
		lista.add(gps.toString());
		StringBuilder donos = new StringBuilder("DonoDe:");
		for (Grupo grupo : grupos) {
			if (grupo.ehDono(nome)) {
				donos.append(grupo.getNome() + ", ");
			}
		}
		lista.add(donos.toString());
		return lista;	
	}
}
