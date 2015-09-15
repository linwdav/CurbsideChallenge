import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Solution to engineering challenge found at https://www.shopcurbside.com/jobs/
 * @author David Lin http://davidlin.org
 */
public class CurbsideChallenge {

	// Pre-defined Curbside url and paths
    private final String BASE_URL = "http://challenge.shopcurbside.com/";
    private final String GET_SESSION_PATH = "get-session";
    private final String START_PATH = "start";

	// Pre-defined Curbside parameters
    private final String KEY_SESSION = "Session";
    private final int HTTP_ERROR_MIN = 400;
	private final int REQUESTS_PER_SESSION_MAX = 10;

	// Misc defines
	private final String KEY_GET = "GET";
	private final String KEY_NEXT = "next";

	// Session info
    private String sessionId = "";
	private int requestCountForSession = 0;

    public static void main(String[] args) {
        CurbsideChallenge client = new CurbsideChallenge();
		List<CurbsideResponseObject> responses = client.getAllCurbsideResponses();

		for (CurbsideResponseObject response : responses) {
			if (response.secret != null) {
				System.out.print(response.secret);
			}
		}
    }

	/**
	 * Get all Curbside responses via breadth first search.
	 * @return	List of CurbsideResponseObjects
	 */
	private List<CurbsideResponseObject> getAllCurbsideResponses() {
		List<CurbsideResponseObject> responseObjects = new ArrayList<>();
		Queue<CurbsideResponseObject> queue = new LinkedList<>();

		CurbsideResponseObject firstResponse = deserializeJson(sendGetRequest(START_PATH));
		queue.add(firstResponse);

		while(!queue.isEmpty()) {
			CurbsideResponseObject response = queue.remove();
			responseObjects.add(response);

			if (response.next != null) {
				for (String next : response.next) {
					CurbsideResponseObject nextResponse = deserializeJson(sendGetRequest(next));
					queue.add(nextResponse);
				}
			}
		}

		return responseObjects;
	}

	/**
	 * Validate current session.
	 * @return	Valid session id
	 */
	private String validateSession() {
		if (requestCountForSession >= REQUESTS_PER_SESSION_MAX || sessionId.isEmpty()) {
			sessionId = sendGetRequest(GET_SESSION_PATH);
			requestCountForSession = 0;
		}
		return sessionId;
	}

	/**
	 * Send Curbside request.
	 * @param path    Curbside url path
	 * @return	Request response
	 */
    private String sendGetRequest(String path) {
        String stringUrl = BASE_URL + path;
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(KEY_GET);

			if (path != GET_SESSION_PATH) {
				// Set session header
				con.setRequestProperty(KEY_SESSION, validateSession());
				requestCountForSession++;
			}

            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    responseCode >= HTTP_ERROR_MIN ? con.getErrorStream() : con.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }
        catch (MalformedURLException ex) {
            System.out.println(ex);
        }
        catch (IOException ex) {
            System.out.println(ex);
        }
        return "";
    }

    /**
     * Deserialize json into CurbsideResponseObject.
     * @param json Valid json string.
     * @return CurbsideResponseObject
     */
    private CurbsideResponseObject deserializeJson(String json) {
		// Change 'next' keys with mixed cases to all lowercase
		int nextPos = json.toLowerCase().indexOf(KEY_NEXT);
		String fixedJson = nextPos > -1 ? json.substring(0, nextPos) + KEY_NEXT + json.substring(nextPos + KEY_NEXT.length()) : json;

		System.out.println(fixedJson);

        Gson gson = new Gson();
		CurbsideResponseObject responseObj = null;
		try {
			responseObj = gson.fromJson(fixedJson, CurbsideResponseObject.class);
		}
		catch (JsonParseException ex) {
			// 'next' key is a string instead of an array of strings
			try {
				CurbsideResponseObjectSpecial special = gson.fromJson(fixedJson, CurbsideResponseObjectSpecial.class);
				List<String> next = new ArrayList<>(Arrays.asList(special.next));
				responseObj = new CurbsideResponseObject(special.depth, special.id, special.message, next, special.secret);
			}
			catch (JsonParseException ex2) {
				System.out.println(ex2);
			}
		}
        return responseObj;
    }

}
