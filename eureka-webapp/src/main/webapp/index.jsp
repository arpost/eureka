<%--
  #%L
  Eureka WebApp
  %%
  Copyright (C) 2012 - 2013 Emory University
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/template.tld" prefix="template" %>

<template:insert template="/templates/eureka_main.jsp">
	<template:content name="content">
		<div class="jumbotron">
			<div class="row">
				<div class="col-sm-8">
					<h1>Welcome to Eureka! Clinical Analytics</h1>
				</div>
				<div class="col-sm-1 vert-offset-2x">
					<img src="${pageContext.request.contextPath}/assets/images/logo.jpg"/>
				</div>
			</div>
			<p class="vert-offset">
				Upload your spreadsheets, connect to your databases, define
				patient features of interest, and compute them in millions of
				patients. Then, explore!
			</p>
			<p class="vert-offset text-center">
				<a href="${pageContext.request.contextPath}/register.jsp"
				   class="btn btn-default btn-lg">
					Register
				</a>
			</p>
		</div>
		<c:if test="${applicationScope.webappProperties.demoMode}">
			<h3>Want to try it out?</h3>

			<p>This is our online demonstration site. Go to the
				<a href="register.jsp">registration page</a> to get an
				account. If you
				already have an account, go ahead and <a
						href="protected/login">login</a>.
			</p>

			<p>

			<p><strong>NOTE: This demonstration website is NOT suitable for
				use
				with sensitive data including patient data that contains
				identifiers.</strong>
			</p>
			</p>
			<h3>Want to deploy Eureka! at your institution or lab?</h3>

			<p>Go to our
				<a href="http://aiw.sourceforge.net" target="_blank">Sourceforge
					website</a>
				to get your own copy.
			</p>
		</c:if>
		<c:if test="${not applicationScope.webappProperties.demoMode and applicationScope.webappProperties.ephiProhibited}">
			<p>
				<strong>
					NOTE: Loading real patient data into the system is
					strictly prohibited.
				</strong>
			</p>
		</c:if>

		<p class="small">
			The software powering this site has been supported in part by <a
				href="http://www.emoryhealthcare.org" target="_blank"
				rel="nofollow">Emory Healthcare</a>;
			<a href="http://winshipcancer.emory.edu" target="_blank"
			   rel="nofollow">Emory Winship Cancer Institute</a>;
			NHLBI grant R24 HL085343; PHS Grant UL1 RR025008, KL2 RR025009 and
			TL1 RR025010 from the CTSA program, NIH, NCRR; and NCMHD grant
			RC4MD005964.
		</p>

		<h3>Release Notes</h3>
		<div class="row">
			<div class="col-sm-1">
				2014.01.16
			</div>
			<div class="col-sm-11">
				Version 1.8.2 is out! This version includes some 
				minor bug fixes.
			</div>
		</div>
		<div class="row vert-offset">
			<div class="col-sm-1">
				2013.12.13
			</div>
			<div class="col-sm-11">
				Version 1.8.1 is out! This version includes some
				minor bug fixes.
			</div>
		</div>
		<div class="row vert-offset">
			<div class="col-sm-1">
				2013.11.11
			</div>
			<div class="col-sm-11">
				Version 1.8 is out! This version provides the
				ability to directly connect to a database as a data source, in
				addition to uploading a spreadsheet. The phenotype editor UI
				has also been improved.
			</div>
		</div>
		<div class="row vert-offset">
			<div class="col-sm-1">
				2013.07.01
			</div>
			<div class="col-sm-11">
				Version 1.7 is out! It provides
				enhancements to the data element editor and integration with
				jasig CAS (Central
				Authentication Service).
			</div>
		</div>
		<div class="row vert-offset">
			<div class="col-sm-1">
				2013.02.15
			</div>
			<div class="col-sm-11">
				Version 1.6.1 is out! It fixes bugs in registration
				verification and editing data elements with property
				constraints.
			</div>
		</div>
		<div class="row vert-offset">
			<div class="col-sm-1">
				2013.01.28
			</div>
			<div class="col-sm-11">
				Version 1.6 is out! The user-defined data element
				editor now supports four kinds of derived data elements for
				specifying various temporal patterns.
			</div>
		</div>
	</template:content>
</template:insert>
