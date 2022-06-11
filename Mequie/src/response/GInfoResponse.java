package response;

public class GInfoResponse {
	protected String groupID;
	protected String owner;
	protected int numberUsers;
	protected boolean error = false;
	
	public GInfoResponse(boolean e) {
		error = e;
	}

	public GInfoResponse(String groupId, String dono, int n) {
		this.groupID = groupId;
		owner = dono;
		numberUsers = n;
	}

	public GInfoResponse() {
	}

	public String getGroupID() {
		return groupID;
	}

	@Override
	public String toString() {
		return "groupID = " + groupID + "\nowner = " + owner + "\nnumberUsers = " + numberUsers;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public int getNumberUsers() {
		return numberUsers;
	}

	public void setNumberUsers(int numberUsers) {
		this.numberUsers = numberUsers;
	}
	
	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}
}
