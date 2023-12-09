import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {
    private static final String TASK_ENDPOINT = "/task";
    private static final String STATUS_ENDPOINT = "/status";

    private final int port;

    private HttpServer httpServer;


    public Server(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress(port),0);
        HttpContext taskContext = httpServer.createContext(TASK_ENDPOINT);
        HttpContext statusContext = httpServer.createContext(STATUS_ENDPOINT);

        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);

        httpServer.setExecutor(Executors.newFixedThreadPool(8));
        httpServer.start();
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        Headers headers = exchange.getRequestHeaders();
        if (headers.containsKey("X-Test") && headers.get("X-Test").get(0).equalsIgnoreCase("true")) {
            String msg = "Wazaaaap";
            sendResponse(msg.getBytes(),exchange);
            return;
        }
        boolean isDebug = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebug = true;
        }

        long startTime = System.nanoTime();
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        byte[] responseBytes = calculateResponse(requestBytes);
        long finishTime = System.nanoTime();

        if (isDebug) {
            String debugMessage = String.format("Operation took %d ns\n",finishTime-startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }
        sendResponse(responseBytes,exchange);
    }

    private byte[] calculateResponse(byte[] requestBytes) {
        String bodyString = new String(requestBytes);
        String[] stringNumbers = bodyString.split(",");

        BigInteger result = BigInteger.ONE;
        for(String number: stringNumbers) {
            BigInteger bigInteger = new BigInteger(number);
            result = result.multiply(bigInteger);
        }
        return String.format("Result of multiplication is %s\n",result).getBytes();
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = "Sever is alive";
        sendResponse(responseMessage.getBytes(),exchange);
    }

    private void sendResponse(byte[] bytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200,bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        Server server = new Server(port);
        server.startServer();

        System.out.println("Server is listening on port: " + port);
    }
}
