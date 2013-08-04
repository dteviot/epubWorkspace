package com.dteviot.epubviewer.WebServer;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.net.Uri;

import com.dteviot.epubviewer.IResourceSource;
import com.dteviot.epubviewer.ResourceResponse;

public class FileRequestHandler implements HttpRequestHandler {

    private IResourceSource mResourceSource = null;

    public FileRequestHandler(IResourceSource resourceSource){
        this.mResourceSource = resourceSource;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        String uriString = request.getRequestLine().getUri();
        ResourceResponse resource = mResourceSource.fetch(Uri.parse(uriString));
        if ((resource != null) && (resource.getData() != null)) {
            InputStreamEntity entity = new InputStreamEntity(resource.getData(), resource.getSize());
            entity.setContentType(resource.getMimeType());
            response.setEntity(entity);
        } else {
            response.setStatusLine(request.getProtocolVersion(), HttpStatus.SC_NOT_FOUND, "File Not Found"); 
        }
    }
}
