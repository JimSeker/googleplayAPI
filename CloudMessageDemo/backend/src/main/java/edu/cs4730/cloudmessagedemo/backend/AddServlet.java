package edu.cs4730.cloudmessagedemo.backend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Seker on 3/14/2017.
 */

public class AddServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String myMessage = req.getParameter("message");

        if (myMessage == null || myMessage.equals("")) {
            req.setAttribute("_retStr", "invalid input");
        } else {
            //I think this will send the message to everyone.
            new MessagingEndpoint().sendMessage(myMessage);
            req.setAttribute("_retStr", "Message sent.");
            req.setAttribute("result"," it worked?");
        }
       // this is crashing and I don't know why.
       // getServletContext().getRequestDispatcher("/query_result.jsp")
       //         .forward(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doGet(req, resp);
    }

}
