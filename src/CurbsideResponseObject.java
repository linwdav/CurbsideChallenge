import java.util.ArrayList;
import java.util.List;

public class CurbsideResponseObject {

	int depth;
	String id;
	String message;
	List<String> next = new ArrayList<>();
	String secret;

	public CurbsideResponseObject(int depth, String id, String message, List<String> next, String secret) {
		this.depth = depth;
		this.id = id;
		this.message = message;
		this.next = next;
		this.secret = secret;
	}

	@Override
	public String toString() {
		return String.format("Depth: %d\nId: %s\nMessage: %s\nSecret: %s", depth, id, message, secret);
	}

}