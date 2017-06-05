package edu.emory.cci.aiw.cvrg.eureka.servlet;

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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.inject.Injector;

import org.eurekaclinical.eureka.client.comm.Destination;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ServicesClient;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eurekaclinical.common.comm.clients.ClientException;

/**
 *
 * @author Andrew Post
 */
@Singleton
public class DateRangePhenotypeServlet extends HttpServlet {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final long serialVersionUID = 1L;

	private final Injector injector;

	@Inject
	public DateRangePhenotypeServlet(Injector inInjector) {
		this.injector = inInjector;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		
		String destinationId = req.getParameter("destinationId");
		if (destinationId == null) {
			throw new ServletException("Missing query parameter 'destinationId'");
		}
		Destination destination;
		try {
			destination = this.injector.getInstance(ServicesClient.class).getDestination(destinationId);
		} catch (ClientException ex) {
			throw new ServletException("Error getting destination '" + destinationId + "'");
		}
		String json = MAPPER.writeValueAsString(destination.getPhenotypeFields());
		resp.setContentLength(json.length());
		PrintWriter out = resp.getWriter();
		out.println(json);
	}
}
