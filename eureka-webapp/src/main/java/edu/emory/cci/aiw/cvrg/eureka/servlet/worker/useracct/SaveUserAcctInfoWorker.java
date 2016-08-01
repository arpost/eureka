/*
 * #%L
 * Eureka WebApp
 * %%
 * Copyright (C) 2012 - 2013 Emory University
 * %%
 * This program is dual licensed under the Apache 2 and GPLv3 licenses.
 * 
 * Apache License, Version 2.0:
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * GNU General Public License version 3:
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package edu.emory.cci.aiw.cvrg.eureka.servlet.worker.useracct;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eurekaclinical.eureka.client.comm.User;

import org.eurekaclinical.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ServicesClient;
import edu.emory.cci.aiw.cvrg.eureka.servlet.worker.ServletWorker;
import edu.emory.cci.aiw.cvrg.eureka.webapp.config.WebappProperties;

public class SaveUserAcctInfoWorker implements ServletWorker {

	private static Logger LOGGER = LoggerFactory.getLogger(SaveUserAcctInfoWorker.class);
	
	private final ResourceBundle messages;
	private final ServicesClient servicesClient;
	private final WebappProperties properties;

	public SaveUserAcctInfoWorker(ServletContext ctx, ServicesClient inClient) {
		String localizationContextName = ctx.getInitParameter("javax.servlet.jsp.jstl.fmt.localizationContext");
		this.messages = ResourceBundle.getBundle(localizationContextName);
		this.servicesClient = inClient;
		this.properties = new WebappProperties();
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {   
		String id = req.getParameter("id");
		if(id!=null && !id.isEmpty()){
                        String firstName = req.getParameter("firstName");
                        String lastName = req.getParameter("lastName");
                        String email = req.getParameter("email");                
                        String organization = req.getParameter("organization");
                        String title = req.getParameter("title");
                        String department = req.getParameter("department");
                        String fullName = firstName+' '+lastName;               

                        User user = null;
                        try {
                                user = this.servicesClient.getUserById(Long.valueOf(id));
                        } catch (ClientException ex) {
                                LOGGER.error("Error getting user by id at {}", SaveUserAcctInfoWorker.class.getName(), ex);
                                resp.setContentType("text/plain");
                                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                resp.getWriter().write(ex.getMessage());                        
                        }
                                user.setFirstName(firstName);
                                user.setLastName(lastName);
                                user.setEmail(email);
                                user.setOrganization(organization);
                                user.setTitle(title);
                                user.setDepartment(department);
                                user.setFullName(fullName);
                        try {     
                                this.servicesClient.updateUser(user,Long.valueOf(id));
                        } catch (ClientException ex) {
                                LOGGER.error("Error updating user at {}", SaveUserAcctInfoWorker.class.getName(), ex);
                                resp.setContentType("text/plain");
                                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                resp.getWriter().write(ex.getMessage());                          
                        }
                        resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                                LOGGER.error("Error getting user by id at {}, id is invalid", SaveUserAcctInfoWorker.class.getName());               
                                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);                               
                }
                
	}
}
