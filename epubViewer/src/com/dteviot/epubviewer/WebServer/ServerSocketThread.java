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
    private int mPort;

    /*
     * @param webServer to process the requests from the client
     * @port  the socket will listen on
     */
    public ServerSocketThread(WebServer webServer, int port){
        super(THREAD_NAME);
        mWebServer = webServer;
        mPort = port;
    }

    @Override
    public void run() {
        super.run();
        
        try {
            mServerSocket = new ServerSocket(mPort);
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
            mServerSocket.close();
        } 
        catch (IOException e) {
            // Exception can be thrown when stopping,
            // because we're closing thread in accept().
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
