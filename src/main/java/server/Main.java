package server;

import static spark.Spark.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.log4j.BasicConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;

import spark.utils.IOUtils;


public class Main {
	
	static List<String> userlist = new ArrayList<String>();
	static int routeIndex = 0;
	static List<Class<?>> objarray = new ArrayList<Class<?>>();
	
	public static void configurator() {
		BasicConfigurator.configure();
	}

	
	public static void main(String[] args){
    	configurator();
    	ResponseClass res_options = new ResponseClass();
    	//port(4567);
    	secure("/home/pedro/eclipse-workspace/sparkframeworkserver/deploy/keystore.jks", "asint2017", null, null);//ssl
    	
        File uploadDir = new File("Customjar");
        uploadDir.mkdir(); // create the upload directory if it doesn't exist
    	/*
    	 * ROUTES
    	 */
    	get("/", (request,response) -> {
    		response.redirect("/hello");
    		return "";
    	});
    	get("/hello", (request, response) -> "Hello World "); //hello world
    	
    	get("/hello/:username", (request,response) -> { //redirect to hello world and store the user
    		ResponseClass.adduser(request.params("username"));
    		response.redirect("/hello");
    		return null;
    	});
    	
    	get("/xmltry/:section", (request, response) -> { // returns section as xml
    	    response.type("text/xml");
    	    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml>" + request.params("section") + "</xml>";
    	});
    	
        get("/echo/:id", (request,response) -> { // checks if id is numeric or not and returns accordingly
            return res_options.Echo(request.params(":id"));
        }); 
        
        
        get("/Dynamic", (request,response) -> { //Dynamic Route - the method is loaded during runtime
        	Class<?> newClass = DynamicRouteLoader.Loader("ResponseClass");
        	return newClass.getMethod("Dynamic").invoke(newClass.newInstance());       	
        });        	
        
        get("/session", (request,response) ->{ // returns sessions id and session's inception 
			return "session " + request.session().id() + " created at " + Date(request.session().creationTime());
        });
        
        get("users/:user", (request,response) -> { // if the user already accessed the /hello with the username returns the users' list
        	String returnvalue = ResponseClass.getuserlist(request.params("user"));
        	return returnvalue; 
        });
        
        get("/ip", (request,response) ->{ // get the client ip
        	return request.ip() + " request " + request.url();
        });/*
        get("/stop", (request,response) ->{ // stops the server
        	stop();
        	return "";
        });*/
        get("/teste", (request,response) -> {
        	get("/"+routeIndex, (request1,response1) ->{
        		routeIndex++;
        		return "hello";
        	});
        	return "okapa";
        });
        
        get("/createobject", (request,response) ->{//ler from jar a class e nao deste package
        	//Class<?> newClass = DynamicRouteLoader.Loader("loadfromjar.Class1");
        	Class<?> newClass = DynamicRouteLoader.CustomLoader("Serverjar.jar");
        	objarray.add(newClass);
        	
        	get("/obj/:n", (request1,response1) ->{
            	int index = Integer.parseInt(request1.params(":n"));
            	if(index < objarray.size()) {
            		return objarray.get(index).getMethod("exec").invoke(objarray.get(index).newInstance());
            	}else {
            		return "no such object";
            	}
        	});
        	//return objarray.get(0).getMethod("exec").invoke(objarray.get(0).newInstance());
        	return "class loaded";
        });

        get("/uploadclass", (req, res) ->
			  "<form method='post' enctype='multipart/form-data'>" // note the enctype
			+ "    <input type='file' name='uploaded_file' accept=''>" // make sure to call getPart using the same "name" in the post
			+ "    <button>Upload file</button>"
			+ "</form>"
		);
		
		post("/uploadclass", (req, res) -> {
		
			Path tempFile = Files.createTempFile(uploadDir.toPath(), "", ".jar");
					
			req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
					
			try (InputStream input = req.raw().getPart("uploaded_file").getInputStream()) { // getPart needs to use same "name" as input field in form
                System.out.println(getFileName(req.raw().getPart("uploaded_file")));
            	Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);       
                
                //Directoria para guardar os ficheiros upload
        		Path destination = Paths.get("Customjar/" + getFileName(req.raw().getPart("uploaded_file")));
                Files.copy(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
                tempFile.toFile().delete();
           
            }
			return "You uploaded this file: " + getFileName(req.raw().getPart("uploaded_file")) ;
					
		});
		
        get("createobject/:classname", (request,response) -> {//TODO not implemented
        	Class<?> newClass = DynamicRouteLoader.CustomLoader(request.params(":classname"));
        	objarray.add(newClass);
        	get("/obj/:n", (request1,response1) ->{
            	int index = Integer.parseInt(request1.params(":n"));
            	if(index < objarray.size()) {
            		return objarray.get(index).getMethod("exec").invoke(objarray.get(index).newInstance());
            	}else {
            		return "no such object";
            	}
        	});
        	return "class loaded";
        });

    }


	private static Object Date(long creationTime) {
		Date date = new Date(creationTime);
		return date;
	}
	private static String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
