

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class run
 */
@WebServlet("/run")
public class run extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public run() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try{
	 		//response.sendRedirect("wait.jsp");
		 	Runtime runtime = Runtime.getRuntime();
		 	//Process script = runtime.exec("F:\\putty.exe");
		 	Process script = runtime.exec("bash /home/gathors/gathors.bash " + request.getSession().getAttribute("uid"));
		 	script.waitFor();			 	
	 	} catch (Exception e){
	 		System.out.println("cannot run");
	 	}
	 	//get result and preview
	 	//response.sendRedirect("results.jsp");
	 	//double randomNum = Math.random();
	    //request.setAttribute("randomNum", randomNum);
	    
	 	RequestDispatcher view=request.getRequestDispatcher("results.jsp");
	    view.forward(request, response);
	}

}
