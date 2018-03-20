package template;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.script.Script;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class GoogleCredentialHelper {

    private static GoogleCredentialHelper instance = null;

    private static final String APPLICATION_NAME = "Template";
    private static final String CREDENTIAL_FILEPATH = "./client_secret.json";
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/template");

    private FileDataStoreFactory dataStoreFactory;
    private final JsonFactory jsonFactory;
    private HttpTransport httpTransport;
    private List<String> scopes;

    private GoogleCredentialHelper(List<String> scopes) {
        this.jsonFactory = JacksonFactory.getDefaultInstance();
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            this.dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            this.scopes = scopes;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Script getScriptService() throws IOException {
        Credential credential = authorize();
        return new Script.Builder(
                httpTransport, jsonFactory, setHttpTimeout(credential))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential authorize() throws IOException {

        InputStream in = new FileInputStream(CREDENTIAL_FILEPATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                jsonFactory, new InputStreamReader(in)
        );

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, jsonFactory, clientSecrets, scopes)
                        .setDataStoreFactory(dataStoreFactory)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;

    }

    private static HttpRequestInitializer setHttpTimeout(
            final HttpRequestInitializer requestInitializer) {
        return httpRequest -> {
            requestInitializer.initialize(httpRequest);
            httpRequest.setReadTimeout(380000);
        };
    }

    public static GoogleCredentialHelper getInstance(List<String> scopes) {
        if (instance == null) {
            instance = new GoogleCredentialHelper(scopes);
        }
        return instance;
    }

}
