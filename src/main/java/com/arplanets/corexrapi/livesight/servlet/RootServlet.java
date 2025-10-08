package com.arplanets.corexrapi.livesight.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/")
public class RootServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.getWriter().write("OK");

    }
}
