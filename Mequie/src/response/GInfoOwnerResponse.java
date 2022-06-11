package response;

import java.util.ArrayList;
import java.util.List;

public class GInfoOwnerResponse extends GInfoResponse {
	private List<String> allUsers = new ArrayList<>();

	public GInfoOwnerResponse(String groupId, String dono, int n) {
		super(groupId, dono, n);
	}

	public GInfoOwnerResponse(String groupId, String dono, int n, List<String> users) {
		super(groupId, dono, n);
		allUsers = users;
	}
	
	public GInfoOwnerResponse() {
		super();
	}

	public void addUser(String u) {
		allUsers.add(u);
	}

	public List<String> getAllUsers() {
		return allUsers;
	}

	public void setAllUsers(List<String> allUsers) {
		this.allUsers = allUsers;
	}

	@Override
	public String toString() {
		String s = super.toString();
		StringBuilder sb = new StringBuilder(s);
		sb.append("\nusers:\n");
		for(String u: allUsers) {
			sb.append(u + "\n");
		}
		return sb.toString();
	}
}
