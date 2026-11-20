import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class Main {
    public static void main(String[] args) throws Exception {
        BoundaryTestSuite.runAllTests();

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("PORT must be between 1 and 65535");
        }
        GameServer server = new GameServer(port);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        String url = "http://localhost:" + port;
        System.out.println("\n==============================================================");
        System.out.println("  GRAND LINE ESCAPE — JAVA BACKEND SERVER RUNNING");
        System.out.println("  Open in your browser: " + url);
        System.out.println("  Press Ctrl+C in this terminal to stop the server.");
        System.out.println("==============================================================\n");

        if (Boolean.parseBoolean(System.getenv().getOrDefault("OPEN_BROWSER", "true"))
                && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException e) {
                System.err.println("Could not open a browser: " + e.getMessage());
            }
        }
    }
}
