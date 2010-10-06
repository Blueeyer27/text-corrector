package org.sc.annotator.adaptive.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sc.annotator.adaptive.AdaptiveMatcher;
import org.sc.annotator.adaptive.Context;
import org.sc.annotator.adaptive.Match;
import org.sc.annotator.adaptive.exceptions.MatcherCloseException;
import org.sc.annotator.adaptive.exceptions.MatcherException;
import org.slf4j.Logger;

public class AdaptiveMatchingServlet extends HttpServlet {
	
	private AdaptiveMatcher matcher;
	
	private Logger logger;
	
	public AdaptiveMatchingServlet(AdaptiveMatcher matcher, Logger logging) {
		this.matcher = matcher;
		logger = logging;
	}
	
	public void init() throws ServletException { 
		super.init();
		
		logger.info("AdaptiveMatchingServlet initialized.");
	}
	
	public void destroy() { 
		super.destroy();
		try {
			matcher.close();
		} catch (MatcherCloseException e) {
			logger.warn("AdaptiveMatchingServlet.destroy", e);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String text = request.getParameter("text");
		if(text == null) { 
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No 'text' parameter supplied.");
			return;
		}
		String context = request.getParameter("context");
		if(context == null) { 
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No 'context' parameter supplied.");
			return;
		}
		
		text = URLDecoder.decode(text, "UTF-8");
		context = URLDecoder.decode(context, "UTF-8");
		
		Context c = new Context(context);
		Collection<Match> matches;
		try {
			matches = matcher.findMatches(c, text);

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text");
			PrintWriter pw = response.getWriter();
			for(Match m : matches) { 
				pw.println(m.value());
			}
		
		} catch (MatcherException e) {
			
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String text = request.getParameter("text");
		if(text == null) { 
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No 'text' parameter supplied.");
			return;
		}
		String context = request.getParameter("context");
		if(context == null) { 
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No 'context' parameter supplied.");
			return;
		}
		String value = request.getParameter("value");
		if(value == null) { 
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No 'value' parameter supplied.");
			return;
		}

		text = URLDecoder.decode(text, "UTF-8");
		context = URLDecoder.decode(context, "UTF-8");
		value = URLDecoder.decode(value, "UTF-8");

		Match m = new Match(new Context(context), text, value);

		try { 
			Context c = matcher.registerMatch(m);

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text");
			response.getWriter().println(c.toString());

		} catch(MatcherException e) { 

			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}
