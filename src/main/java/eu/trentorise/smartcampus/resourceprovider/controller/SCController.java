package eu.trentorise.smartcampus.resourceprovider.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;


import eu.trentorise.smartcampus.resourceprovider.jdbc.JdbcResourceServices;




@Controller
public class SCController {
	
	@Autowired
	DataSource dataSource;
	
	
	private static final Logger logger = Logger.getLogger(SCController.class);
	protected String retrieveAppName(HttpServletRequest request)
			 {
		try {
			Authentication auth= SecurityContextHolder.getContext().getAuthentication();
	
			Map<String,String> x=new JdbcResourceServices(dataSource).loadAddInfoByToken(auth.getPrincipal().toString());
			
			return x.get("name");
		
		} catch (Exception e) {
			logger.error("Exception checking token");
			
		}
		return null;
	}


}
