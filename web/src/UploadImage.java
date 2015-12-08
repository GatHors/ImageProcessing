
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.StreamTokenizer;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

/**
 * Servlet implementation class UploadImage
 */
@WebServlet("/UploadImage")
public class UploadImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	File tmpDir = null;
    File saveDir = null;
;

    public UploadImage() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		   doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = (String)request.getSession().getAttribute("uid");
	    String savePath = "/home/gathors/proj/v-opencv/user/" + username;
	    System.out.println(savePath);
	    File userdir = new File(savePath);
	    if(!userdir.isDirectory()){
	    	userdir.mkdir();
	    }
	    File input = new File(savePath + "/input");
	    File output = new File("/home/gathors/apache-tomcat-8.0.28/webapps/image/images/" + username);
	    if (!input.isDirectory()){
	    	input.mkdir();
	    }
	    if (!output.isDirectory()){
	    	output.mkdir();
	    }
		//upload file
		 try{
	            String agent = request.getHeader("user-agent");
	            String os = null;
	            if (agent.contains("Win")) os = "Windows";
	            if (agent.contains("Linux")) os = "Linux";
	            	            
		        if(ServletFileUpload.isMultipartContent(request)){
		          DiskFileItemFactory factory = new DiskFileItemFactory();
		          factory.setRepository(tmpDir);
		          factory.setSizeThreshold(1024000);
		          ServletFileUpload file = new ServletFileUpload(factory);
		          file.setFileSizeMax(5000000);
		          file.setSizeMax(10000000);
		          FileItemIterator fileIter = file.getItemIterator(request);
		          while(fileIter.hasNext()){
		            FileItemStream fis = fileIter.next();
		            
		            if(!fis.isFormField() && fis.getName().length()>0){
		            	if (os.equals("Windows")){
		            		//String fileName = fis.getName().substring(fis.getName().lastIndexOf("\\"));
			                BufferedInputStream in = new BufferedInputStream(fis.openStream());
			                //BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(saveDir+"/"+fileName.toLowerCase())));
			                //System.out.println(saveDir+"/"+fileName.toLowerCase());
			                System.out.println(savePath+"/request.jpg");
			                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(savePath+"/input/request.jpg")));
			                
			                Streams.copy(in, out, true);
		            	}
		            	
		            	else if (os.equals("Linux")){
		            		//String fileName = fis.getName().substring(fis.getName().lastIndexOf("/") +1);
			                BufferedInputStream in = new BufferedInputStream(fis.openStream());
			                //BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(saveDir+"/"+fileName.toLowerCase())));
			                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(savePath+"/input/request.jpg")));
			                Streams.copy(in, out, true);
		            	}
		            	else{
		            		
			                BufferedInputStream in = new BufferedInputStream(fis.openStream());
			                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(savePath+"/input/request.jpg")));
			                Streams.copy(in, out, true);
		            	}
		               
		            }
		          }
		          response.getWriter().println("File upload successfully!!!");
		        }
		    }catch(Exception e){
		        e.printStackTrace();
		    }
		 
		 	
		 	//run binary code
		    response.sendRedirect("wait.jsp");

		 }          

	 public void init() throws ServletException {

		   super.init();

		    tmpDir = new File("/home/gathors/image/tmp");
		    saveDir = new File("/home/gathors/proj/v-opencv/user/");
		    if(!tmpDir.isDirectory())
		        tmpDir.mkdir();
		    if(!saveDir.isDirectory())
		        saveDir.mkdir();

		    
	}


}


