package edu.emory.cci.aiw.cvrg.eureka.webapp.config;

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
import com.google.inject.Singleton;
import edu.emory.cci.aiw.cvrg.eureka.servlet.*;
import edu.emory.cci.aiw.cvrg.eureka.servlet.cohort.CohortHomeServlet;
import edu.emory.cci.aiw.cvrg.eureka.servlet.cohort.DeleteCohortServlet;
import edu.emory.cci.aiw.cvrg.eureka.servlet.cohort.EditCohortServlet;
import edu.emory.cci.aiw.cvrg.eureka.servlet.cohort.SaveCohortServlet;
import edu.emory.cci.aiw.cvrg.eureka.servlet.filter.UserFilter;
import edu.emory.cci.aiw.cvrg.eureka.servlet.filter.MessagesFilter;
import edu.emory.cci.aiw.cvrg.eureka.servlet.filter.PasswordExpiredFilter;
import edu.emory.cci.aiw.cvrg.eureka.servlet.filter.RolesFilter;
import edu.emory.cci.aiw.cvrg.eureka.servlet.oauth.GitHubRegistrationOAuthCallbackServlet;
import edu.emory.cci.aiw.cvrg.eureka.servlet.oauth.GlobusRegistrationOAuthCallbackServlet;
import edu.emory.cci.aiw.cvrg.eureka.servlet.oauth.GoogleRegistrationOAuthCallbackServlet;
import edu.emory.cci.aiw.cvrg.eureka.servlet.oauth.TwitterRegistrationOAuthCallbackServlet;
import edu.emory.cci.aiw.cvrg.eureka.servlet.proposition.*;
import java.util.HashMap;
import java.util.Map;
import org.eurekaclinical.common.config.AbstractServletModule;
import org.eurekaclinical.common.servlet.LogoutServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hrathod
 */
class ServletModule extends AbstractServletModule {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ServletModule.class);
	private static final String CONTAINER_PATH = "/site/*";
	private static final String CONTAINER_PROTECTED_PATH = "/protected/*";
	private static final String FILTER_PATH = "^/(?!(assets|bower_components)).*";
	private static final String PASSWORD_EXPIRED_REDIRECT_URL = "/protected/password_expiration.jsp";
	private static final String PASSWORD_SAVE_PATH = "/protected/user_acct";
	private static final String LOGOUT_PATH = "/logout";

	private final WebappProperties properties;

	public ServletModule(WebappProperties inProperties) {
		super(inProperties, CONTAINER_PATH, CONTAINER_PROTECTED_PATH,
				LOGOUT_PATH);
		this.properties = inProperties;
	}

	private void setupMessageFilter() {
		bind(MessagesFilter.class).in(Singleton.class);
		filterRegex(FILTER_PATH).through(MessagesFilter.class);
	}

	private void setupUserFilter() {
		bind(UserFilter.class).in(Singleton.class);
		filterRegex(FILTER_PATH).through(UserFilter.class);
	}
	
	private void setupRolesFilter() {
		filterRegex(FILTER_PATH).through(RolesFilter.class);
	}

	private void setupPasswordExpiredFilter() {
		bind(PasswordExpiredFilter.class).in(Singleton.class);
		Map<String, String> params = new HashMap<>();
		params.put("redirect-url", PASSWORD_EXPIRED_REDIRECT_URL);
		params.put("save-url", PASSWORD_SAVE_PATH);
		if (LOGGER.isDebugEnabled()) {
			this.printParams(params);
		}
		filter(CONTAINER_PROTECTED_PATH).through(
				PasswordExpiredFilter.class, params);
	}

	@Override
	protected void setupFilters() {
		this.setupMessageFilter();
		this.setupUserFilter();
		this.setupRolesFilter();
		this.setupPasswordExpiredFilter();
	}

	@Override
	protected void setupServlets() {
		serve("/protected/get-session").with(PostMessageLoginServlet.class);
        serve("/destroy-session").with(DestroySessionServlet.class);
		
		bind(RegisterUserServlet.class).in(Singleton.class);
		serve("/register").with(RegisterUserServlet.class);

		bind(ProxyServlet.class).in(Singleton.class);
		serve("/proxy-resource/*").with(ProxyServlet.class);

		bind(ForgotPasswordServlet.class).in(Singleton.class);
		serve("/forgot_password").with(ForgotPasswordServlet.class);

		bind(LogoutServlet.class).in(Singleton.class);
		serve(LOGOUT_PATH).with(LogoutServlet.class);

		bind(VerifyUserServlet.class).in(Singleton.class);
		serve("/verify").with(VerifyUserServlet.class);

		bind(JobSubmitServlet.class).in(Singleton.class);
		Map<String, String> uploadParams = new HashMap<>();
		uploadParams.put("dest_dir", this.properties.getUploadDir());
		serve("/protected/upload").with(JobSubmitServlet.class,
				uploadParams);

		bind(LoginServlet.class).in(Singleton.class);
		serve("/protected/login").with(LoginServlet.class);

		bind(JobPollServlet.class).in(Singleton.class);
		serve("/protected/jobpoll").with(JobPollServlet.class);

		bind(JobListServlet.class).in(Singleton.class);
		serve("/protected/jobs").with(JobListServlet.class);

		bind(JobStatsServlet.class).in(Singleton.class);
		serve("/protected/jobstats").with(JobStatsServlet.class);

		bind(JobPatientCountsServlet.class).in(Singleton.class);
		serve("/protected/jobpatcounts").with(JobPatientCountsServlet.class);

		bind(AdminManagerServlet.class).in(Singleton.class);
		serve("/protected/admin").with(AdminManagerServlet.class);

		bind(UserAcctManagerServlet.class).in(Singleton.class);
		serve("/protected/user_acct").with(UserAcctManagerServlet.class);

		bind(EditorHomeServlet.class).in(Singleton.class);
		serve("/protected/editorhome").with(EditorHomeServlet.class);

		bind(SystemPropositionListServlet.class).in(Singleton.class);
		serve("/protected/systemlist").with(
				SystemPropositionListServlet.class);

		bind(SavePropositionServlet.class).in(Singleton.class);
		serve("/protected/saveprop").with(SavePropositionServlet.class);

		bind(DeletePropositionServlet.class).in(Singleton.class);
		serve("/protected/deleteprop").with(DeletePropositionServlet.class);

		bind(UserPropositionListServlet.class).in(Singleton.class);
		serve("/protected/userproplist").with(
				UserPropositionListServlet.class);

		bind(ListUserDefinedPropositionChildrenServlet.class).in(
				Singleton.class);
		serve("/protected/userpropchildren").with(
				ListUserDefinedPropositionChildrenServlet.class);

		bind(EditPropositionServlet.class).in(Singleton.class);
		serve("/protected/editprop").with(EditPropositionServlet.class);

		bind(DateRangePhenotypeServlet.class).in(Singleton.class);
		serve("/protected/destinationphenotypes").with(
				DateRangePhenotypeServlet.class);

		bind(SearchSystemPropositionJSTreeV3Servlet.class).in(Singleton.class);
		serve("/protected/jstree3_searchsystemlist").with(
				SearchSystemPropositionJSTreeV3Servlet.class);

		bind(SearchSystemPropositionJSTreeV1Servlet.class).in(Singleton.class);
		serve("/protected/searchsystemlist").with(
				SearchSystemPropositionJSTreeV1Servlet.class);

		bind(CohortHomeServlet.class).in(Singleton.class);
		serve("/protected/cohorthome").with(CohortHomeServlet.class);

		bind(EditCohortServlet.class).in(Singleton.class);
		serve("/protected/editcohort").with(EditCohortServlet.class);

		bind(SaveCohortServlet.class).in(Singleton.class);
		serve("/protected/savecohort").with(SaveCohortServlet.class);

		bind(DeleteCohortServlet.class).in(Singleton.class);
		serve("/protected/deletecohort").with(DeleteCohortServlet.class);

		bind(ChooseAccountTypeServlet.class).in(Singleton.class);
		serve("/chooseaccounttype").with(ChooseAccountTypeServlet.class);

		bind(GitHubRegistrationOAuthCallbackServlet.class).in(Singleton.class);
		serve("/registrationoauthgithubcallback").with(
				GitHubRegistrationOAuthCallbackServlet.class);

		bind(GoogleRegistrationOAuthCallbackServlet.class).in(Singleton.class);
		serve("/registrationoauthgooglecallback").with(
				GoogleRegistrationOAuthCallbackServlet.class);

		bind(TwitterRegistrationOAuthCallbackServlet.class).in(Singleton.class);
		serve("/registrationoauthtwittercallback").with(
				TwitterRegistrationOAuthCallbackServlet.class);

		bind(GlobusRegistrationOAuthCallbackServlet.class).in(Singleton.class);
		serve("/registrationoauthglobuscallback").with(
				GlobusRegistrationOAuthCallbackServlet.class);
	}
	
	@Override
	protected Map<String, String> getCasValidationFilterInitParams() {
		Map<String, String> params = new HashMap<>();
        params.put("casServerUrlPrefix", this.properties.getCasUrl());
        params.put("serverName", this.properties.getProxyCallbackServer());
        params.put("proxyCallbackUrl", getCasProxyCallbackUrl());
        params.put("proxyReceptorUrl", getCasProxyCallbackPath());
        return params;
	}

}
