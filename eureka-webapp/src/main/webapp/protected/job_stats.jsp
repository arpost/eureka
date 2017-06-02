<%--
  #%L
  Eureka WebApp
  %%
  Copyright (C) 2012 - 2015 Emory University
  %%
  This program is dual licensed under the Apache 2 and GPLv3 licenses.
  
  Apache License, Version 2.0:
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  
  GNU General Public License version 3:
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/gpl-3.0.html>.
  #L%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/template.tld" prefix="template" %>

<template:insert template="/templates/eureka_main.jsp">
	<template:content name="content">
		<h1>Browse Population</h1>
		<div id="msg" class="alert alert-danger" role="alert">
			<p><strong>No data!</strong> This job did not output any data.</p>
		</div>
		<div id="loading"><p>Please wait while treemap is loading...</p></div>
		<div id="infovis" style="width: 900px; height: 500px;"></div>
		<!--[if IE]><script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jit/2.0.1/Extras/excanvas.min.js"></script><![endif]-->
		<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jit/2.0.1/jit.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/eureka.stats${initParam['eureka-build-timestamp']}.js"></script>
		<script type="text/javascript">init(${param['jobId']});</script>
	</template:content>
</template:insert>
