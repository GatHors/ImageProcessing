

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mysql.jdbc.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 * Servlet implementation class login
 */
@WebServlet("/login")
public class login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public login() {
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
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		//String method = null;
		Connection conn = null;
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/gathor_users", "root", "123456");
		} catch (Exception e){
			System.out.println("Cannot connect");
		}
		
		try{
			//Actions of register;
			if (request.getParameter("submit").equals("Register")){

				//insert into mysql;
				String sql = "insert into user(usr,pwd) values('"+username+"','"+password+"')";
				java.sql.Statement st = (Statement)conn.createStatement();
				st.executeUpdate(sql);
				
				//forward message;
				HttpSession session = request.getSession();
				session.setAttribute("redirect_action", "Congruadation!!!You successfully your account, please go back to login.");
				response.sendRedirect("redirect.jsp");
			}
			
			//Actions of login;
			if (request.getParameter("submit").equals("Login")){
				
				//comparing username and password;
				String sql = "select pwd from user where usr='"+username+"'";
				//System.out.println(sql);
				java.sql.Statement st = (Statement)conn.createStatement();
				ResultSet rs = st.executeQuery(sql);
				if (rs.next()){
					String sqlpwd = rs.getString("pwd");
					//Success!
					if(sqlpwd.equals(password)){
						HttpSession session = request.getSession();
						session.setAttribute("uid", username);
						//response.sendRedirect("upload.jsp");
					 	RequestDispatcher view=request.getRequestDispatcher("upload.jsp");
					    view.forward(request, response);
					}else{
						HttpSession session = request.getSession();
						session.setAttribute("redirect_action", "Username or password invaild!!");
						RequestDispatcher view=request.getRequestDispatcher("redirect.jsp");
					    view.forward(request, response);
					}
				}else{
					HttpSession session = request.getSession();
					session.setAttribute("redirect_action", "Username or password invaild!!");
					RequestDispatcher view=request.getRequestDispatcher("redirect.jsp");
				    view.forward(request, response);
				}
				
			}
		}catch (Exception e){
			System.out.println("error type " + e.getMessage());
		}
		
		
	}

}
