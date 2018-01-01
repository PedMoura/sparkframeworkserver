package server;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.BasicConfigurator;


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
    	secure("/home/pedro/Desktop/sparkframeworkserver/deploy/keystore.jks", "password", null, null);//ssl
    	
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
        	Class<?> newClass = DynamicRouteLoader.loader("ResponseClass");
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
        	Class<?> newClass = DynamicRouteLoader.loader("loadfromjar.Class1");
        	objarray.add(newClass);
        	get("/obj/:n", (request1,response1) ->{
            	int index = Integer.parseInt(request1.params(":n"));
            	if(index < objarray.size()) {
            		return objarray.get(0).getMethod("exec").invoke(objarray.get(0).newInstance());
            	}else {
            		return "no such object";
            	}
        	});
        	//return objarray.get(0).getMethod("exec").invoke(objarray.get(0).newInstance());
        	return "class loaded";
        });
    }


	private static Object Date(long creationTime) {
		Date date = new Date(creationTime);
		return date;
	}
}
