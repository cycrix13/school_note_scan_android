package com.hien.schoolnotescan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class WebServer extends NanoHTTPD {
	
	private static final String ROW = "<tr%s><td><a href='%s' class='file'> %s </a></td></tr>";
	
	private Resources res;
	private DocumentManager mDocManager;
	private Context ct;
    
	public WebServer(Resources res, DocumentManager docManager, Context ct) {
        super(8080);
        
        this.res = res;
        mDocManager = docManager;
        this.ct = ct;
    }

    @Override
    public Response serve(String uri, Method method, 
    		Map<String, String> header,
    		Map<String, String> parms, 
    		Map<String, String> files) {
    	
    	Response respone = null;
    	
    	if (uri.equals("/bg.png")) {
    		respone = new NanoHTTPD.Response(Status.OK, "image/jpeg", 
    				res.openRawResource(R.raw.bg));
    	} else if (uri.equals("/theadbg.png")) {
    		respone = new NanoHTTPD.Response(Status.OK, "image/jpeg", 
    				res.openRawResource(R.raw.theadbg));
    	} else if (uri.equals("/titlebg.png")) {
    		respone = new NanoHTTPD.Response(Status.OK, "image/jpeg", 
    				res.openRawResource(R.raw.titlebg));
    	} else if (uri.equals("/")) {
    		String str1 = loadFileResource(R.raw.index1);
    		String str2 = loadFileResource(R.raw.index2);
    		respone = new NanoHTTPD.Response(str1 + generateRow() + str2);
    	} else if(uri.startsWith("/files/") && uri.endsWith(".pdf")) {
    		String fileName = uri.substring(7);
    		String docName = fileName.substring(0, fileName.length() - 4);
    		Document doc = mDocManager.getDocByName(docName);
    		
    		if (doc == null) {
    			respone = new NanoHTTPD.Response("404 Not found");
    		} else {
	    		String path = PdfHelper.RenderPdf(doc.mNotePathArr, "doc.pdf", ct);
	    		try {
					respone = new NanoHTTPD.Response(Status.OK, "application/pdf", 
							new FileInputStream(path));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
    		}
    	} else {
    		respone = new NanoHTTPD.Response("404 Not found");
    	}
    	
        return respone;
    }
    
    private String loadFileResource(int rid) {
    	
    	InputStream in = res.openRawResource(rid);
		InputStreamReader reader = new InputStreamReader(in);
		char[] buffer = new char[3000];
		int hasRead = 0;
		try {
			hasRead = reader.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String bodyStr = String.copyValueOf(buffer, 0, hasRead);
		return bodyStr;
    }
    
    private String generateRow() {
    	
    	StringBuilder builder = new StringBuilder();
    	for (int i = 0; i < mDocManager.mDocList.size(); i++) {
    		String trclass = i % 2 == 0 ? " class='shadow'" : "";
    		String name = mDocManager.mDocList.get(i).mName;
    		String link = "/files/" + name + ".pdf";
    		builder.append(String.format(ROW, trclass, link, name));
    	}
    	
    	return builder.toString();
    }
}