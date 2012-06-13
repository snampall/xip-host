/**
 * Copyright (c) 2012 Washington University in St. Louis. All Rights Reserved.
 */
package edu.wustl.xipHost.hostLogin;

import gov.nih.nci.cagrid.ncia.util.SecureClientUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.globus.gsi.GlobusCredential;
import org.w3c.dom.Element;

/**
 * @author Jarek Krych
 *
 */
public class GridLogin implements Login {
	final static Logger logger = Logger.getLogger(GridLogin.class);
	GlobusCredential globusCred;
	boolean isConnectionSecured = false;
	String username;
	
	@Override
	public boolean login(String username, String password) {
		this.username = username;
		acquireGlobusCredential(username, password);
		if(isConnectionSecured) {
			logger.debug("User: " + username + " successfuly authenticated to NBIA authentication service");
			return true;
		} else {
			logger.debug("User: " + username + " denied access to secured NBIA service");
			return false;
		}
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isConnectionSecured() {
		return isConnectionSecured;
	}

	@Override
	public GlobusCredential getGlobusCredential() {
		return globusCred;
	}

	Element samlAssertionElement;
	@Override
	public Element getSamlAssertion() {
		samlAssertionElement = null;
		return samlAssertionElement;
	}
	
	@Override
	public void invalidateSecuredConnection() {
		globusCred = null;
		samlAssertionElement = null;
		isConnectionSecured = false;
	}
	
	public GlobusCredential acquireGlobusCredential(String userName, String password){
		try{			
			if(userName == null) {
				logger.warn("UserName is null");
				return null;
			}
			if(password == null){
				logger.warn("Password is null");
				return null;
			}
			File f = new File("./resources/service_urls.properties");
	
			Properties prop = new Properties();
			prop.load(new FileInputStream(f));
			String dorianURL = prop.getProperty("cagrid.master.dorian.service.url");
			String authUrl = prop.getProperty("cagrid.master.authentication.service.url");
			
			globusCred = SecureClientUtil.generateGlobusCredential(userName,
					password,
                    dorianURL,
                    authUrl);
			isConnectionSecured = true;
			return globusCred;
		}catch(Exception e){
			globusCred = null;
			isConnectionSecured = false;
			logger.error(e,  e);
			return globusCred;
		}
	}

}
