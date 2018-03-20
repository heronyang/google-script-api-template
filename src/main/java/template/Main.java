package template;

public class Main {

    // TODO: replace this with your script id
    private static final String scriptId = "<YOUR_SCRIPT_ID>";

    public static void main(String[] args) {

        ScriptAdapter scriptAdapter = new ScriptAdapter(scriptId);

        try {
            System.out.println("Result: " + scriptAdapter.add(3, 5));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
