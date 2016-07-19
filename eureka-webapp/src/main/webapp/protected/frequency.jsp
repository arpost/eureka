<%--
  #%L
  Eureka WebApp
  %%
  Copyright (C) 2012 - 2014 Emory University
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
<h3>Frequency Editor</h3>
<p>
	Computes an interval over the temporal extent of the intervals contributing to the specified frequency
	count below.
</p>
<div class="row">
	<div class="col-xs-5">
		<ul class="nav nav-tabs">
			<li class="active">
				<a href="#systemElems" data-toggle="tab">System</a>
			</li>
			<li>
				<a href="#userElems" data-toggle="tab">User</a>
			</li>
		</ul>
		<div id="treeContent" class="tab-content proposition-tree">
			<div id="systemElems" class="tab-pane fade active in">
				<div id="systemTree"></div>
				<div id="searchUpdateDiv" class="searchUpdateMessage"></div>
			</div>
			<div id="userElems" class="tab-pane fade">
				<div id="userTree"></div>
			</div>
		</div>

	</div>
	<div id="definitionContainer" class="col-xs-7">
		<form id="frequencyForm" class="form-horizontal vert-offset" role="form">
			<div class="form-group">
				<label for="propDisplayName">Name</label>
				<input type="text" id="propDisplayName" value="${proposition.displayName}"
                                        class="form-control"/>
			</div>
			<div class="form-group">
				<label for="propDescription">Description</label>
				<textarea id="propDescription" class="form-control"><c:if
                                        test="${not empty proposition}">${proposition.description}</c:if></textarea>
			</div>
			<!--<label>Definition</label>-->
			<fieldset>
			<legend>Threshold</legend>
                        <div class="form-group">
				<div class="col-sm-4">
					<label class="sr-only" for="freqTypes">Type</label>
					<select id="freqTypes" class="form-control" name="freqTypes" id="freqTypes"
							title="Specify whether only the first n intervals will be matched (First) or any n intervals (At least)">
						<c:forEach var="freqType" items="${frequencyTypes}">
							<option value="${freqType.id}"
									<c:if test="${propositionType == 'FREQUENCY' ? freqType.id == proposition.frequencyType : freqType.id == defaultFrequencyType.id}">selected="selected"</c:if>>${freqType.description}</option>
						</c:forEach>
					</select>
				</div>
				<div class="col-sm-4">
					<label class="sr-only" for="freqAtLeastField">Count</label>
					<input type="number" id="frequencyCountField" name="freqAtLeastField" id="freqAtLeastField" min="1"
						   value="<c:choose><c:when test="${propositionType == 'FREQUENCY'}">${proposition.atLeast}</c:when><c:otherwise><c:out value="1"/></c:otherwise></c:choose>"
						   title="Specify the frequency count" class="form-control"/>
				</div>
				<div class="col-sm-4">
						<label id="valueThresholdConsecutiveLabel" class="checkbox-inline<c:if test="${empty proposition or proposition.phenotype.type != 'VALUE_THRESHOLD'}"> hide</c:if>">
						<input type="checkbox" value="true" name="freqIsConsecutive"
								  title="For value threshold phenotypes, specifies whether no intervening values are present that do not match the threshold or any duration or property constraints specified below"
							   <c:if test="${propositionType == 'FREQUENCY' and proposition.isConsecutive}">checked="checked"</c:if> />consecutive
					</label>
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-12">
					<label class="sr-only" for="freqMainPhenotype">Phenotype</label>
					<div id="freqMainPhenotype" class="tree-drop tree-drop-single jstree-drop"
						 title="Drag and drop the system/user-defined phenotype member in here">
						<div class="label-info text-center">
                                                	Drop Here
                                                </div>
						<ul data-type="main" data-drop-type="single" class="sortable">
							<c:if test="${not empty proposition and propositionType == 'FREQUENCY'}">
								<li data-key="${proposition.phenotype.phenotypeKey}"
                                                                    data-desc="${proposition.phenotype.phenotypeDisplayName}"
                                                                    data-space="${proposition.phenotype.inSystem ? 'system' : 'user'}">
                                                                    <span class="glyphicon glyphicon-remove delete-icon">
                                                                    </span>
                                                                    <span class="desc">${empty proposition.phenotype.phenotypeDisplayName ? '' : proposition.phenotype.phenotypeDisplayName} 
                                                                        (${proposition.phenotype.phenotypeKey})
                                                                    </span>
								</li>
							</c:if>
						</ul>
					</div>
				</div>
			</div>
			</fieldset>
			<fieldset>
				<legend><input class="checkbox-inline" type="checkbox" value="true"
					   name="freqPhenotypeSpecifyDuration"
					   <c:if test="${propositionType == 'FREQUENCY' and proposition.phenotype.hasDuration}">checked="checked"</c:if>>
				Duration</legend>
                                <div class="form-group">
                                        <div class="col-md-2">
                                                <label class="sr-only" for="freqPhenotypeMinDurationValue">From</label>
                                                <input type="text" class="form-control"
                                                           name="freqPhenotypeMinDurationValue"
                                                           id="freqPhenotypeMinDurationValue"
                                                           placeholder="min"
                                                           value="<c:if test="${propositionType == 'FREQUENCY'}">${proposition.phenotype.minDuration}</c:if>"/>
                                        </div>
                                        <div class="col-md-3">
                                                <label class="sr-only" for="freqPhenotypeMinDurationUnits">From units</label>
                                                <select name="freqPhenotypeMinDurationUnits" id="freqPhenotypeMinDurationUnits" class="form-control">
                                                        <c:forEach var="unit" items="${timeUnits}">
                                                                <option value="${unit.id}"
                                                                                <c:if test="${propositionType == 'FREQUENCY' ? unit.id == proposition.phenotype.minDurationUnits : unit.id == defaultTimeUnit.id}">selected="selected"</c:if>>${unit.description}</option>
                                                        </c:forEach>
                                                </select>
                                        </div>
                                        <div class="col-md-1 control-label" style="text-align: center">
                                                <label for="freqPhenotypeMaxDurationValue">to</label>
                                        </div>
                                        <div class="col-md-2">
                                                <input type="text" class="form-control"
                                                           name="freqPhenotypeMaxDurationValue"
                                                           placeholder="max"
                                                           value="<c:if test="${propositionType == 'FREQUENCY'}">${proposition.phenotype.maxDuration}</c:if>"/>
                                        </div>
                                        <div class="col-md-3">
                                                <label class="sr-only" for="freqPhenotypeMaxDurationUnits">To units</label>
                                                <select name="freqPhenotypeMaxDurationUnits" id="freqPhenotypeMaxDurationUnits" class="form-control">
                                                        <c:forEach var="unit" items="${timeUnits}">
                                                                <option value="${unit.id}"
                                                                                <c:if test="${propositionType == 'FREQUENCY' ? unit.id == proposition.phenotype.maxDurationUnits : unit.id == defaultTimeUnit.id}">selected="selected"</c:if>>${unit.description}</option>
                                                        </c:forEach>
                                                </select>
                                        </div>
                                </div>
			</fieldset>
			<fieldset>
			<legend>
				<input type="checkbox" class="checkbox-inline propertyValueConstraint"
                                        <c:if test="${propositionType == 'FREQUENCY' and proposition.phenotype.hasPropertyConstraint}">
                                            checked="checked"
                                        </c:if>
                                        name="freqPhenotypeSpecifyProperty"
                                />Property value
			</legend>
			<div class="form-group">
				<div class="col-sm-6">
					<label class="sr-only" for="freqPhenotypePropertyName">Property name</label>
					<select name="freqPhenotypePropertyName" id="freqPhenotypePropertyName" class="form-control"
							data-properties-provider="freqMainPhenotype">
						<c:if test="${propositionType == 'FREQUENCY'}">
							<c:forEach var="property"
                                                                items="${properties[proposition.phenotype.phenotypeKey]}">
								<option value="${property}"
                                                                        <c:if test="${proposition.phenotype.property == property}">
                                                                            selected="selected"
                                                                        </c:if>>${property}
                                                                </option>
							</c:forEach>
						</c:if>
					</select>
				</div>
				<div class="col-sm-6">
					<label class="sr-only" for="freqPhenotypePropertyValue">Property value</label>
					<input type="text" class="form-control propertyValueField"
						   name="freqPhenotypePropertyValue"
						   id="freqPhenotypePropertyValue"
						   value="
                                                   <c:if test="${propositionType == 'FREQUENCY' and not empty proposition.phenotype.propertyValue}">
                                                       ${proposition.phenotype.propertyValue}
                                                   </c:if>"
                                        />
				</div>
			</div>
			</fieldset>
			<fieldset>
                                <legend>
                                        <input type="checkbox" value="true" name="freqIsWithin" class="checkbox-inline"
                                                   <c:if test="${propositionType == 'FREQUENCY' and proposition.isWithin}">checked="checked"</c:if>>
                                        Distance between
                                </legend>
                                <div class="form-group">
                                        <div class="col-md-2">
                                                <label class="sr-only" for="freqWithinAtLeast">From</label>
                                                <input type="text" class="form-control" name="freqWithinAtLeast"
                                                           id="freqWithinAtLeast"
                                                           placeholder="min"
                                                           value="<c:if test="${propositionType == 'FREQUENCY'}">${proposition.withinAtLeast}</c:if>"/>
                                        </div>
                                        <div class="col-md-3">
                                                <label class="sr-only" for="freqWithinAtLeastUnits">Time units</label>
                                                <select name="freqWithinAtLeastUnits" id="freqWithinAtLeastUnits" class="form-control">
                                                        <c:forEach var="unit" items="${timeUnits}">
                                                                <option value="${unit.id}"
                                                                                <c:if test="${propositionType == 'FREQUENCY' ? unit.id == proposition.withinAtLeastUnits : unit.id == defaultTimeUnit.id}">selected="selected"</c:if>>${unit.description}</option>
                                                        </c:forEach>
                                                </select>
                                        </div>
                                        <div class="col-md-1 control-label" style="text-align: center">
                                                <label for="freqWithinAtMost">to</label>
                                        </div>
                                        <div class="col-md-2">
                                                <input type="text" class="form-control" name="freqWithinAtMost"
                                                           placeholder="max" id="freqWithinAtMost"
                                                           value="<c:if test="${propositionType == 'FREQUENCY'}">${proposition.withinAtMost}</c:if>"/>
                                        </div>
                                        <div class="col-md-3">
                                                <label class="sr-only" for="freqWithinAtMostUnits">Time units</label>
                                                <select name="freqWithinAtMostUnits" class="form-control" id="freqWithinAtMostUnits">
                                                        <c:forEach var="unit" items="${timeUnits}">
                                                                <option value="${unit.id}"
                                                                                <c:if test="${propositionType == 'FREQUENCY' ? unit.id == proposition.withinAtMostUnits : unit.id == defaultTimeUnit.id}">selected="selected"</c:if>>${unit.description}</option>
                                                        </c:forEach>
                                                </select>
                                        </div>
                                </div>
			</fieldset>
			<div class="form-group text-center">
				<button id="savePropositionButton" type="button" class="btn btn-primary">
					Save
				</button>
			</div>
		</form>
	</div>
</div>
<div id="deleteModal" class="modal fade" role="dialog" aria-labelledby="deleteModalLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 id="deleteModalLabel" class="modal-title">Delete Element</h4>
			</div>
			<div id="deleteContent" class="modal-body">
			</div>
			<div class="modal-footer">
				<button id="deleteButton" type="button" class="btn btn-primary">Delete</button>
				<button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
			</div>
		</div>
	</div>
</div>
<div id="replaceModal" class="modal fade" role="dialog" aria-labelledby="replaceModalLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 id="replaceModalLabel" class="modal-title">Replace Element</h4>
			</div>
			<div id="replaceContent" class="modal-body">
			</div>
			<div class="modal-footer">
				<button id="replaceButton" type="button" class="btn btn-primary">Replace</button>
				<button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
			</div>
		</div>
	</div>
</div>
<div id="errorModal" class="modal fade" role="dialog" aria-labelledby="errorModalLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 id="errorModalLabel" class="modal-title">Error</h4>
			</div>
			<div id="errorContent" class="modal-body">
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
			</div>
		</div>
	</div>
</div>
<div id="searchModal" class="modal fade" role="dialog" aria-labelledby="searchModalLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 id="searchModalLabel" class="modal-title">
					Broad Search Update
				</h4>
			</div>
			<div id="searchContent" class="modal-body">
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
			</div>
		</div>
	</div>
</div>
<div id="searchValidationModal" class="modal fade" role="dialog" aria-labelledby="searchModalLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 id="searchModalLabel" class="modal-title">
					Search String Validation Failed
				</h4>
			</div>
			<div id="searchContent" class="modal-body">
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
			</div>
		</div>
	</div>
</div>
<div id="searchNoResultsModal" class="modal fade" role="dialog" aria-labelledby="searchModalLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 id="searchModalLabel" class="modal-title">
					No Search Results
				</h4>
			</div>
			<div id="searchContent" class="modal-body">
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
			</div>
		</div>
	</div>
</div>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/jquery.jstree.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/eureka.tree-phenotype${initParam['eureka-build-timestamp']}.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/assets/js/eureka.editor${initParam['eureka-build-timestamp']}.js"></script>
<script type="text/javascript">
	eureka.editor.setup('FREQUENCY', '', ${proposition != null ? proposition.id : 'null'},
			'#systemTree', '#userTree', '#definitionContainer', '#savePropositionButton', 'span.delete-icon',
			'ul.sortable', '${pageContext.request.contextPath}/assets/css/jstree-themes/default/style.css', '#searchModal', '#searchValidationModal',
			'#searchNoResultsModal','#searchUpdateDiv');
</script>
</template:content>
</template:insert>
