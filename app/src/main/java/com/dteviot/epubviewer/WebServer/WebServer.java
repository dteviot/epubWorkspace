package com.dteviot.epubviewer.WebServer;

import java.io.IOException;
import java.net.Socket;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

/*
 * A minimal HTTP request processor
 */
public class WebServer {
    private static final String MATCH_EVERYTING_PATTERN = "*";

    private BasicHttpContext mHttpContext = null;
    private HttpService mHttpService = null;

    /*
     * @handler that processes get requests
     */
    public WebServer(HttpRequestHandler handler){
        mHttpContext = new BasicHttpContext();

        // set up Interceptors.
        //... ResponseContent is required, or it doesn't work.
        //... Others are recommended (in Apache docs) but not
        //... strictly needed in this case.
        BasicHttpProcessor httpproc = new BasicHttpProcessor();
        httpproc.addInterceptor(new ResponseContent());
        httpproc.addInterceptor(new ResponseConnControl());
        httpproc.addInterceptor(new ResponseDate());
        httpproc.addInterceptor(new ResponseServer());

        mHttpService = new HttpService(httpproc, 
            new DefaultConnectionReuseStrategy(),
            new DefaultHttpResponseFactory());
        
        HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
        registry.register(MATCH_EVERYTING_PATTERN, handler);
        mHttpService.setHandlerResolver(registry);
    }

    /*
     * Called when a client connects to server
     * @socket the client is using 
     */
    public void processClientRequest(Socket socket) {
        try {
            DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();
            serverConnection.bind(socket, new BasicHttpParams());
            mHttpService.handleRequest(serverConnection, mHttpContext);
            serverConnection.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        }
    }
}