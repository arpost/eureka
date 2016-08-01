/*
 * #%L
 * Eureka Services
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
package edu.emory.cci.aiw.cvrg.eureka.services.resource;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import org.eurekaclinical.eureka.client.comm.PasswordChangeRequest;
import org.eurekaclinical.eureka.client.comm.User;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.LocalUserEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.RoleEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.UserEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.UserEntityToUserVisitor;
import edu.emory.cci.aiw.cvrg.eureka.common.util.StringUtil;
import edu.emory.cci.aiw.cvrg.eureka.services.authentication.ServicesAuthenticationSupport;
import edu.emory.cci.aiw.cvrg.eureka.services.clients.I2b2Client;
import edu.emory.cci.aiw.cvrg.eureka.services.config.ServiceProperties;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.*;
import edu.emory.cci.aiw.cvrg.eureka.services.email.EmailException;
import edu.emory.cci.aiw.cvrg.eureka.services.email.EmailSender;
import edu.emory.cci.aiw.cvrg.eureka.services.util.UserToUserEntityVisitor;
import org.eurekaclinical.standardapis.exception.HttpStatusException;

/**
 * RESTful end-point for {@link UserEntity} related methods.
 *
 * @author hrathod
 */
@Transactional
@Path("/protected/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

	/**
	 * The class logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(
			UserResource.class);
	/**
	 * Data access object to work with User objects.
	 */
	private final UserDao userDao;
	
	/**
	 * Data access object to work with LocalUser objects.
	 */
	private final LocalUserDao localUserDao;
	/**
	 * Data access object to work with Role objects.
	 */
	private final RoleDao roleDao;
	/**
	 * Used to send emails to the user when needed.
	 */
	private final EmailSender emailSender;
	/**
	 * An i2b2 client to change the password remotely.
	 */
	private final I2b2Client i2b2Client;
	/**
	 * And validation errors that we may have encountered while validating a new
	 * user request, or a user update.
	 */
	private String validationError;
	
	private UserToUserEntityVisitor visitor;
	private final ServicesAuthenticationSupport authenticationSupport;
	private final ServiceProperties properties;

	/**
	 * Create a UserResource object with a User DAO and a Role DAO.
	 *
	 * @param inUserDao DAO used to access {@link UserEntity} related functionality.
	 * @param inRoleDao DAO used to access {@link RoleEntity} related functionality.
	 * @param inEmailSender Used to send emails to the user when necessary.
	 */
	@Inject
	public UserResource(UserDao inUserDao, LocalUserDao inLocalUserDao,
			RoleDao inRoleDao,
			EmailSender inEmailSender, I2b2Client inClient,
			OAuthProviderDao inOAuthProviderDao,
			LoginTypeDao inLoginTypeDao,
			AuthenticationMethodDao inAuthenticationMethodDao,
			ServiceProperties inProperties) {
		this.userDao = inUserDao;
		this.localUserDao = inLocalUserDao;
		this.roleDao = inRoleDao;
		this.emailSender = inEmailSender;
		this.i2b2Client = inClient;
		this.visitor = new UserToUserEntityVisitor(inOAuthProviderDao,
				inRoleDao, inLoginTypeDao, inAuthenticationMethodDao);
		this.authenticationSupport = new ServicesAuthenticationSupport();
		this.properties = inProperties;
	}

	/**
	 * Get a list of all users in the system.
	 *
	 * @return A list of {@link UserEntity} objects.
	 */
	@RolesAllowed({"admin"})
	@GET
	public List<User> getUsers() {
		List<UserEntity> users = this.userDao.getAll();
		LOGGER.debug("Returning list of users");
		UserEntityToUserVisitor visitor = new UserEntityToUserVisitor();
		visitor.visit(users);
		return visitor.getUsers();
	}

	/**
	 * Get a user by the user's identification number.
	 *
	 * @param inId The identification number for the user to fetch.
	 * @return The user referenced by the identification number.
	 */
	@RolesAllowed({"researcher", "admin"})
	@Path("/{id}")
	@GET
	public User getUserById(@Context HttpServletRequest req,
			@PathParam("id") Long inId) {
		UserEntity userEntity = this.userDao.retrieve(inId);
		if (userEntity == null) {
			throw new HttpStatusException(Response.Status.NOT_FOUND);
		}
		if (!req.isUserInRole("admin") && !this.authenticationSupport.isSameUser(req, userEntity)) {
			throw new HttpStatusException(Response.Status.FORBIDDEN);
		}
		this.userDao.refresh(userEntity);
		LOGGER.debug("Returning user for ID {}", inId);
		UserEntityToUserVisitor visitor = new UserEntityToUserVisitor();
		userEntity.accept(visitor);
		return visitor.getUser();
	}

	/**
	 * Get a user using the username.
	 * @param req The HTTP request containing the user name.
	 *
	 * @return The user corresponding to the given name.
	 */
	@RolesAllowed({"researcher", "admin"})
	@Path("/me")
	@GET
	public User getMe(@Context HttpServletRequest req) {
		AttributePrincipal principal = 
				this.authenticationSupport.getUserPrincipal(req);
		String username = principal.getName();
		UserEntity userEntity = this.userDao.getByUsername(username);
		if (userEntity != null) {
			this.userDao.refresh(userEntity);
		} else {
			throw new HttpStatusException(Response.Status.NOT_FOUND);
		}
		LOGGER.debug("Returning user for name {}", username);
		UserEntityToUserVisitor visitor = new UserEntityToUserVisitor();
		userEntity.accept(visitor);
		return visitor.getUser();
	}

	/**
	 * Add a new user to the system. The user is activated immediately.
	 *
	 * @param user Object containing all the information about the user
	 * to add.
	 */
	@RolesAllowed({"admin"})
	@POST
	public Response addUser(final User user, @Context UriInfo uriInfo) {
		if (this.userDao.getByUsername(user.getUsername()) != null) {
			throw new HttpStatusException(Response.Status.CONFLICT);
		}
		String[] errors = user.validate();
		if (errors.length == 0) {
			user.accept(visitor);
			UserEntity userEntity = visitor.getUserEntity();
			LOGGER.debug("Saving new user {}", userEntity.getEmail());
			this.userDao.create(userEntity);
			try {
				LOGGER.debug("Sending email to {}", userEntity.getEmail());
				this.emailSender.sendActivationMessage(userEntity);
			} catch (EmailException e) {
				LOGGER.error("Error sending email to {}", userEntity.getEmail(), e);
			}
		} else {
			LOGGER.info(
					"Invalid new user request: {}, reason {}", user,
					this.validationError);
			throw new HttpStatusException(
					Response.Status.BAD_REQUEST, StringUtils.join(errors, ", "));
		}
		UserEntity addedUser = this.userDao.getByUsername(user.getUsername());
		URI uri = uriInfo.getAbsolutePathBuilder().path(addedUser.getId().toString()).build();
		return Response.created(uri).entity(user).build();
	}

	/**
	 * Changes a user's password.
	 *
	 * @param request the incoming servlet request
	 * @param passwordChangeRequest the request to use to make the password
	 * change
	 *
	 * @throws HttpStatusException Thrown when a password cannot be properly
	 * hashed, or the passwords are mismatched.
	 */
	@RolesAllowed({"researcher", "admin"})
	@Path("/passwordchange")
	@POST
	public void changePassword(@Context HttpServletRequest request,
			PasswordChangeRequest passwordChangeRequest) {
		String username = request.getUserPrincipal().getName();
		LocalUserEntity user = this.localUserDao.getByName(username);
		if (user == null) {
			LOGGER.error("User " + username + " not found");
			throw new HttpStatusException(Response.Status.NOT_FOUND);
		} else
			this.localUserDao.refresh(user);

		String newPassword = passwordChangeRequest.getNewPassword();
		String oldPasswordHash;
		String newPasswordHash;
		try {
			oldPasswordHash = StringUtil.md5(passwordChangeRequest.getOldPassword());
			newPasswordHash = StringUtil.md5(newPassword);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error(e.getMessage(), e);
			throw new HttpStatusException(
					Response.Status.INTERNAL_SERVER_ERROR, e);
		}
		if (user.getPassword().equals(oldPasswordHash)) {
			user.setPassword(newPasswordHash);
			user.setPasswordExpiration(this.getExpirationDate());
			if (this.properties.getI2b2URL() != null) {
				this.i2b2Client.changePassword(user.getEmail(), newPassword);
			}
			this.localUserDao.update(user);

			try {
				this.emailSender.sendPasswordChangeMessage(user);
			} catch (EmailException ee) {
				LOGGER.error(ee.getMessage(), ee);
			}
		} else {
			throw new HttpStatusException(
					Response.Status.BAD_REQUEST,
					"Error while changing password. Old password is incorrect.");
		}
	}

	/**
	 * Put an updated user to the system. Unless the user has the admin role,
	 * s/he may only update their own user info.
	 *
	 * @param inUser Object containing all the information about the user to
	 * add.
	 * @return A "Created" response with a link to the user page if successful.
	 */
	@RolesAllowed({"researcher", "admin"})
	@Path("/{id}")
	@PUT
	public Response putUser(@Context HttpServletRequest req, User inUser,
							@PathParam("id") Long inId) {
		String username = req.getUserPrincipal().getName();
		if (!req.isUserInRole("admin") && !username.equals(inUser.getUsername())) {
			throw new HttpStatusException(Response.Status.FORBIDDEN);
		}
		LOGGER.debug("Received updated user: {}", inUser);
		Response response;
                
		UserEntity currentUser = this.userDao.retrieve(inId);
		User me = getMe(req);
                
		boolean activation = (!currentUser.isActive()) && (inUser.isActive());
                
		if (this.validateUpdatedUser(currentUser, inUser, me)) {               

			currentUser.setFirstName(inUser.getFirstName());
			currentUser.setLastName(inUser.getLastName());
			currentUser.setEmail(inUser.getEmail());
			currentUser.setOrganization(inUser.getOrganization());
			currentUser.setTitle(inUser.getTitle());
			currentUser.setDepartment(inUser.getDepartment());
			currentUser.setFullName(inUser.getFullName());                

			List<RoleEntity> updatedRoles = this.roleIdsToRoles(inUser.getRoles());                
			currentUser.setRoles(updatedRoles);
			currentUser.setActive(inUser.isActive());
			currentUser.setLastLogin(inUser.getLastLogin());

			LOGGER.debug("Saving updated user: {}", currentUser.getEmail());
			this.userDao.update(currentUser);

			if (activation) {
				try {
					this.emailSender.sendActivationMessage(currentUser);
				} catch (EmailException ee) {
					LOGGER.error(ee.getMessage(), ee);
				}
			}

			response = Response.ok().
					entity(currentUser).
					build();
		} else {
			response = Response.notModified(this.validationError).build();
		}               

		return response; 
	}

	/**
	 * Validate that a user being updated does not violate any rules.
	 *
	 * @param currentUser The user before update.
	 * @param inUser The updated user that to be validate.
	 * @param me The current login user.
	 * @return True if the user is valid, false otherwise.
	 */
	private boolean validateUpdatedUser(UserEntity currentUser, User inUser, User me) {
		boolean result = true;
		boolean updateByMe = true;
		// the roles to check
		RoleEntity adminUserRole = this.roleDao.getRoleByName("admin");

		updateByMe = me.getUsername().equals(currentUser.getUsername());
                
		// a admin user can not be stripped of admin rights, 
		// or be de-activated by him/herself.
		if(currentUser.getRoles().contains(adminUserRole) && updateByMe){
			if ( !inUser.getRoles().contains(Long.valueOf(2))) {
				this.validationError = "admin user can not be stripped of admin rights by him/herself ";
				result = false;
			} else {
				if (currentUser.isActive() && !inUser.isActive()){
					this.validationError = "admin user can not be de-activated by him/herself";				
					result = false;                                    
				}
			}
		}
                
		return result;
	}

	private Date getExpirationDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 90);
		return calendar.getTime();
	}

	private List<RoleEntity> roleIdsToRoles(List<Long> inRoleIds) {
		List<RoleEntity> roles = new ArrayList<>(inRoleIds.size());
		for (Long roleId : inRoleIds) {
			roles.add(this.roleDao.retrieve(roleId));
		}
		return roles;
	}
}
