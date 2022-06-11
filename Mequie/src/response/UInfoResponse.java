package response;

import java.util.ArrayList;
import java.util.List;

public class UInfoResponse {
	private String user;
	private String groups;
	private String owned;
	private boolean error = false;

	/*
	public UInfoResponse(String user, List<String> groups, List<String> ownerGroups, List<String> memberGroups) {
		super();
		this.user = user;
		this.groups = groups;
		this.memberGroups = "";
		this.ownerGroups = "";
	}
	 */

	public UInfoResponse(String utilizador) {
		user = utilizador;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getGroups() {
		return groups;
	}

	public void setGroups(String membroDe) {
		this.groups = membroDe;
	}

	public String getOwnedGroups(){
		return owned;
	}
	
	public void setOwned(String string) {
		this.owned = string;
	}

	public void setError() {
		error = true;
	}

	public boolean getError() {
		return error;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Info do " + user + "\n");
		sb.append(groups + "\n");
		sb.append(owned + "\n");
		return sb.toString();
	}


}
