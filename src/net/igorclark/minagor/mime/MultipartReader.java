package net.igorclark.minagor.mime;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.mortbay.util.MultiMap;
import org.mortbay.util.StringUtil;
import org.mortbay.util.TypeUtil;

public class MultipartReader {
	
	public HashMap<String, String> parseMultipartData(InputStream fcgiInputStream, String contentType, String boundary) throws IOException {
		
        BufferedInputStream in = new BufferedInputStream(fcgiInputStream);
        HashMap<String, String> h = new HashMap<String, String>();
        byte[] byteBoundary = null;
        
        // TODO - handle encodings
        
        try {
        	byteBoundary = (boundary+"--").getBytes(StringUtil.__ISO_8859_1);
        }
        catch(UnsupportedEncodingException u) {
        	System.out.println("Caught UnsupportedEncodingException: " + u.getMessage());
        }
        MultiMap params = new MultiMap();
        
        try
        {
            // Get first boundary
            byte[] bytes=TypeUtil.readLine(in);
            String line=bytes==null?null:new String(bytes,"UTF-8");

            if(line==null || !line.equals(boundary))
            {
                throw new IOException("Missing initial multi part boundary");
            }
            
            // Read each part
            boolean lastPart=false;
            String content_disposition=null;
            String content_type=null;
            String content_length=null;
            while(!lastPart)
            {
                while(true)
                {
                    bytes=TypeUtil.readLine(in);
                    // If blank line, end of part headers
                    if(bytes==null || bytes.length==0)
                        break;
                    line=new String(bytes,"UTF-8");
                    
                    // place part header key and value in map
                    int c=line.indexOf(':',0);
                    if(c>0)
                    {
                        String key=line.substring(0,c).trim().toLowerCase();
                        String value=line.substring(c+1,line.length()).trim();
                        if(key.equals("content-disposition"))
                            content_disposition=value;
                        if(key.equals("content-length"))
                            content_length=value;
                        if(key.equals("content-type"))
                            content_type=value;
                    }
                }
                
                // Extract content-disposition
                boolean form_data=false;
                if(content_disposition==null)
                {
                    throw new IOException("Missing content-disposition");
                }
                
                StringTokenizer tok=new StringTokenizer(content_disposition,";");
                String name=null;
                String filename=null;
                while(tok.hasMoreTokens())
                {
                    String t=tok.nextToken().trim();
                    String tl=t.toLowerCase();
                    if(t.startsWith("form-data"))
                        form_data=true;
                    else if(tl.startsWith("name="))
                        name=value(t);
                    else if(tl.startsWith("filename="))
                        filename=value(t);
                }
                
                // Check disposition
                if(!form_data)
                {
                    continue;
                }
                if(name==null||name.length()==0)
                {
                    continue;
                }
                
                OutputStream out=null;
                File file=null;
                try
                {
                    if (filename!=null && filename.length()>0)
                    {
                    	String tempFileName = "MultiPart-" + filename;
                        file = File.createTempFile(tempFileName, "");
                        out = new FileOutputStream(file);
                        //request.setAttribute(name,file);
                        /* FIXME
                        Hashtable<String, String> fileInfoHash = new Hashtable<String, String>();
                        fileInfoHash.put("form_fieldname", name);
                        fileInfoHash.put("original_filename", filename);
                        fileInfoHash.put("stored_filename", tempFileName);
                        h.put(name, fileInfoHash);
                        */
                        StringBuffer buf = new StringBuffer("form_fieldname:" + name + "|");
                        buf.append("original_filename:" + filename + "|");
                        buf.append("stored_filename:" + tempFileName + "|");
                        // FIXME from written data
                        //buf.append("original_filesize:" + content_length + "|");
                        buf.append("original_filetype:" + content_type);
                        h.put(name, buf.toString());
                        
                        /*
                        if (_deleteFiles)
                        {
                            file.deleteOnExit();
                            ArrayList files = (ArrayList)request.getAttribute(FILES);
                            if (files==null)
                            {
                                files=new ArrayList();
                                request.setAttribute(FILES,files);
                            }
                            files.add(file);
                        }
                        */
                    }
                    else
                        out=new ByteArrayOutputStream();
                    
                    int state=-2;
                    int c;
                    boolean cr=false;
                    boolean lf=false;
                    
                    // loop for all lines`
                    while(true)
                    {
                        int b=0;
                        while((c=(state!=-2)?state:in.read())!=-1)
                        {
                            state=-2;
                            // look for CR and/or LF
                            if(c==13||c==10)
                            {
                                if(c==13)
                                    state=in.read();
                                break;
                            }
                            // look for boundary
                            if(b>=0&&b<byteBoundary.length&&c==byteBoundary[b])
                                b++;
                            else
                            {
                                // this is not a boundary
                                if(cr)
                                    out.write(13);
                                if(lf)
                                    out.write(10);
                                cr=lf=false;
                                if(b>0)
                                    out.write(byteBoundary,0,b);
                                b=-1;
                                out.write(c);
                            }
                        }
                        // check partial boundary
                        if((b>0&&b<byteBoundary.length-2)||(b==byteBoundary.length-1))
                        {
                            if(cr)
                                out.write(13);
                            if(lf)
                                out.write(10);
                            cr=lf=false;
                            out.write(byteBoundary,0,b);
                            b=-1;
                        }
                        // boundary match
                        if(b>0||c==-1)
                        {
                            if(b==byteBoundary.length)
                                lastPart=true;
                            if(state==10)
                                state=-2;
                            break;
                        }
                        // handle CR LF
                        if(cr)
                            out.write(13);
                        if(lf)
                            out.write(10);
                        cr=(c==13);
                        lf=(c==10||state==10);
                        if(state==10)
                            state=-2;
                    }
                }
                finally
                {
                    out.close();
                }
                
                if (file==null)
                {
                    bytes = ((ByteArrayOutputStream)out).toByteArray();
                    h.put(name, new String(bytes));
                }
            }
        
       }
        catch(Exception e) {
        	System.out.println(this.getClass().getName() + " caught " + e.getClass().getName() + ": " + e.getMessage());
        }
        finally
        {
            //deleteFiles(request);
        }
    	return h;

	}
	
    private String value(String nameEqualsValue)
    {
        String value=nameEqualsValue.substring(nameEqualsValue.indexOf('=')+1).trim();
        int i=value.indexOf(';');
        if(i>0)
            value=value.substring(0,i);
        if(value.startsWith("\""))
        {
            value=value.substring(1,value.indexOf('"',1));
        }
        else
        {
            i=value.indexOf(' ');
            if(i>0)
                value=value.substring(0,i);
        }
        return value;
    }

}