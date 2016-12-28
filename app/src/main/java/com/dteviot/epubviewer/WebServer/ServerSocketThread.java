package com.dteviot.epubviewer.WebServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * A worker thread that services a server socket.
 */
public class ServerSocketThread extends Thread {
    private static final String THREAD_NAME = "ServerSocket";
    private WebServer mWebServer;
    private ServerSocket mServerSocket;
    private volatile boolean mIsRunning = false;

    /*
     * @param webServer to process the requests from the client
     * @port  the socket will listen on
     */
    public ServerSocketThread(WebServer webServer, int port){
        super(THREAD_NAME);
        mWebServer = webServer;
        try {
            mServerSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        
        try {
            mServerSocket.setReuseAddress(true);
            while(mIsRunning) {
                // wait until a client makes a request.
                // will return with a clientSocket that can be used
                // to communicate with the client
                Socket clientSocket = mServerSocket.accept();
                
                // pass socket on to "something else" that will
                // use it to communicate with client
                mWebServer.processClientRequest(clientSocket);
            }
        } 
        catch (IOException e) {
            // Exception can be thrown when stopping,
            // because we're closing socket in stopThread().
            // In which case, just ignore it
            if (mIsRunning) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void startThread() {
        mIsRunning = true;
        super.start();
    }

    public synchronized void stopThread(){
        mIsRunning = false;
        try {
            // force thread out of accept().
            mServerSocket.close();
        } catch (IOException e) {
            // Ignore any error, nothing to do
        }
    }
}
