package edu.emory.cci.aiw.cvrg.eureka.services.authentication;

/*
 * #%L
 * Eureka Common
 * %%
 * Copyright (C) 2012 - 2014 Emory University
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

import edu.emory.cci.aiw.cvrg.eureka.services.entity.UserEntity;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.eurekaclinical.common.comm.User;
import org.jasig.cas.client.authentication.AttributePrincipal;

/**
 *
 * @author Andrew Post
 */
public abstract class AbstractUserSupport implements UserSupport {
	
	protected AbstractUserSupport() {
	}
	
	@Override
	public AttributePrincipal getUserPrincipal(HttpServletRequest request) {
		return (AttributePrincipal) request.getUserPrincipal();
	}
	
	@Override
	public Map<String, Object> getUserPrincipalAttributes(HttpServletRequest request) {
		AttributePrincipal principal = getUserPrincipal(request);
		return principal.getAttributes();
	}
		
	@Override
	public boolean isSameUser(HttpServletRequest servletRequest, User user) {
		return isSameUser(servletRequest, user.getUsername());
	}
	
	@Override
	public boolean isSameUser(HttpServletRequest servletRequest, UserEntity user) {
		return isSameUser(servletRequest, user.getUsername());
	}
	
	@Override
	public boolean isSameUser(HttpServletRequest servletRequest, String username) {
		AttributePrincipal principal = getUserPrincipal(servletRequest);
		if (!principal.getName().equals(username)) {
			return false;
		}
		return true;
	}
	
}
