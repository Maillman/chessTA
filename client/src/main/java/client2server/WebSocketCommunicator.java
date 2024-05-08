package client2server;

import com.google.gson.Gson;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketCommunicator extends Endpoint {

    Session session;
    ServerMessageObserver serverMessageObserver;
    public WebSocketCommunicator(String url, ServerMessageObserver serverMessageObserver) throws ResponseException {
        try{
            url = url.replace("http","ws");
            URI socketURI = new URI(url + "/connect");
            this.serverMessageObserver = serverMessageObserver;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>(){
                @Override
                public void onMessage(String message) {
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                    serverMessageObserver.notify(notification);
                }
            });
        }catch(Exception e){
            throw new ResponseException(500, e.getMessage());
        }
    }
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
