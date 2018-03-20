package template;

import com.google.api.services.script.Script;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ScriptAdapter {

    private String scriptId;
    private Script service;

    ScriptAdapter(String scriptId) {
        this.scriptId = scriptId;
        try {
            List<String> scopes = Arrays.asList(
                    "https://www.googleapis.com/auth/script.projects",
                    "https://www.googleapis.com/auth/drive",
                    "https://www.googleapis.com/auth/script.scriptapp",
                    "https://www.googleapis.com/auth/script.external_request"
            );
            this.service = GoogleCredentialHelper.getInstance(scopes)
                    .getScriptService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int add(int v1, int v2) throws Exception {

        ExecutionRequest request = new ExecutionRequest().setFunction("add");

        List<Object> params = new ArrayList<>();
        params.add(v1);
        params.add(v2);
        request.setParameters(params);

        try {

            Operation op =
                    service.scripts().run(scriptId, request).execute();
            if (op.getError() == null) {
                return ((BigDecimal) op.getResponse().get("result"))
                        .intValueExact();
            }

            // Error
            System.out.println(getScriptError(op));

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        throw new Exception("Unexpected error");

    }

    @SuppressWarnings("unchecked")
    private String getScriptError(Operation op) {

        if (op.getError() == null) {
            return null;
        }

        Map<String, Object> detail = op.getError().getDetails().get(0);
        List<Map<String, Object>> stacktrace = (List<Map<String, Object>>)
                        detail.get("scriptStackTraceElements");

        java.lang.StringBuilder sb =
                new StringBuilder("\nScript error message: ");
        sb.append(detail.get("errorMessage"));
        sb.append("\nScript error type: ");
        sb.append(detail.get("errorType"));

        if (stacktrace != null) {
            sb.append("\nScript error stacktrace:");
            for (Map<String, Object> elem : stacktrace) {
                sb.append("\n  ");
                sb.append(elem.get("function"));
                sb.append(":");
                sb.append(elem.get("lineNumber"));
            }
        }
        sb.append("\n");
        return sb.toString();

    }

}
