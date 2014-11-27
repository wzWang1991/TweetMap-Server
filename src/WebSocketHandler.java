import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;


public class WebSocketHandler extends WebSocketServer  {
	private final static int PORT = 11083;
	private static WebSocketHandler instance = null;
	
	
	private WebSocketHandler() throws UnknownHostException {
		
	}
	
	public synchronized static WebSocketHandler getInstance() throws UnknownHostException {
		if (instance == null) {
			instance = new WebSocketHandler(PORT);
		}
		return instance;
	}
	
    HashSet<WebSocket> clients = new HashSet<>();

    private WebSocketHandler(int port) throws UnknownHostException {
        super(new InetSocketAddress(new InetSocketAddress(0).getAddress(), port));
		System.out.println("Create new websocket instance.");
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println(webSocket.getRemoteSocketAddress());
        clients.add(webSocket);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        clients.remove(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {

    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    public void publish(String s) {
        //System.out.println(clients.size());
        for (WebSocket webSocket : clients)
            webSocket.send(s);
    }
}
