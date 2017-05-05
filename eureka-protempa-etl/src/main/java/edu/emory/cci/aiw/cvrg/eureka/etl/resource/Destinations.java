package edu.emory.cci.aiw.cvrg.eureka.etl.resource;

/*
 * #%L
 * Eureka Protempa ETL
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
import org.eurekaclinical.eureka.client.comm.Cohort;
import org.eurekaclinical.eureka.client.comm.Node;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.CohortDestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.CohortEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.DestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.DestinationGroupMembership;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.EtlGroup;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.AuthorizedUserEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.I2B2DestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.NodeEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.NodeToNodeEntityVisitor;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.PatientSetExtractorDestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.PatientSetSenderDestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.TabularFileDestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.etl.config.EtlProperties;
import edu.emory.cci.aiw.cvrg.eureka.etl.dao.DestinationDao;
import edu.emory.cci.aiw.cvrg.eureka.etl.dao.EtlGroupDao;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;
import org.eurekaclinical.eureka.client.comm.CohortDestination;
import org.eurekaclinical.eureka.client.comm.Destination;
import org.eurekaclinical.eureka.client.comm.I2B2Destination;
import org.eurekaclinical.eureka.client.comm.PatientSetExtractorDestination;
import org.eurekaclinical.eureka.client.comm.PatientSetSenderDestination;
import org.eurekaclinical.eureka.client.comm.TabularFileDestination;
import org.eurekaclinical.standardapis.exception.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew Post
 */
public final class Destinations {
	
	/**
	 * The class level logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Destinations.class);

	private final EtlGroupDao groupDao;
	private final AuthorizedUserEntity etlUser;
	private final DestinationDao destinationDao;
	private final EtlProperties etlProperties;

	public Destinations(EtlProperties inEtlProperties, AuthorizedUserEntity inEtlUser,
			DestinationDao inDestinationDao, EtlGroupDao inGroupDao) {
		this.groupDao = inGroupDao;
		this.etlUser = inEtlUser;
		this.destinationDao = inDestinationDao;
		this.etlProperties = inEtlProperties;
	}

 	public Long create(Destination etlDestination) {
		if (etlDestination instanceof CohortDestination) {
			CohortDestinationEntity cde = new CohortDestinationEntity();
			cde.setName(etlDestination.getName());
			cde.setDescription(etlDestination.getDescription());
			cde.setOwner(this.etlUser);
			Date now = new Date();
			cde.setCreatedAt(now);
			cde.setEffectiveAt(now);
			Cohort cohort = ((CohortDestination) etlDestination).getCohort();
			CohortEntity cohortEntity = new CohortEntity();
			Node node = cohort.getNode();
			NodeToNodeEntityVisitor v = new NodeToNodeEntityVisitor();
			node.accept(v);
			cohortEntity.setNode(v.getNodeEntity());
			cde.setCohort(cohortEntity);
			DestinationEntity destinationEntity = this.destinationDao.create(cde);
                        return destinationEntity.getId();
		} else {
			throw new HttpStatusException(Response.Status.BAD_REQUEST, "Can't create i2b2 destinations via web services yet");
		}
	}

	public void update(Destination etlDestination) {
		if (etlDestination instanceof CohortDestination) {
			DestinationEntity oldEntity = this.destinationDao.retrieve(etlDestination.getId());
			if (oldEntity == null || !(oldEntity instanceof CohortDestinationEntity)) {
				throw new HttpStatusException(Response.Status.NOT_FOUND);
			}
			if (!this.etlUser.getId().equals(etlDestination.getOwnerUserId())) {
				throw new HttpStatusException(Response.Status.NOT_FOUND);
			}
			Date now = new Date();
			oldEntity.setExpiredAt(now);
			this.destinationDao.update(oldEntity);
			
			CohortDestinationEntity cde = new CohortDestinationEntity();
			cde.setName(etlDestination.getName());
			cde.setDescription(etlDestination.getDescription());
			cde.setOwner(this.etlUser);
			cde.setEffectiveAt(now);
			cde.setCreatedAt(oldEntity.getCreatedAt());
			
			Cohort cohort = ((CohortDestination) etlDestination).getCohort();
			CohortEntity cohortEntity = new CohortEntity();
			cde.setCohort(cohortEntity);
			Node node = cohort.getNode();
			NodeToNodeEntityVisitor v = new NodeToNodeEntityVisitor();
			node.accept(v);
			NodeEntity nodeEntity = v.getNodeEntity();
			cohortEntity.setNode(nodeEntity);
			this.destinationDao.create(cde);
			
		} else {
			throw new HttpStatusException(Response.Status.BAD_REQUEST, "Can't update i2b2 destinations via web services yet");
		}
	}

	List<DestinationEntity> configs(AuthorizedUserEntity user) {
		return user.getDestinations();
	}

	List<DestinationGroupMembership> groupConfigs(EtlGroup group) {
		return group.getDestinations();
	}

	String toConfigId(File file) {
		return FromConfigFile.toDestId(file);
	}

	public List<I2B2Destination> getAllI2B2s() {
		List<I2B2Destination> result = new ArrayList<>();
		I2B2DestinationsDTOExtractor extractor
				= new I2B2DestinationsDTOExtractor(this.etlProperties, this.etlUser, this.groupDao);
		for (I2B2DestinationEntity configEntity
				: this.destinationDao.getAllI2B2Destinations()) {
			I2B2Destination dto = extractor.extractDTO(configEntity);
			if (dto != null) {
				result.add(dto);
			}
		}
		return result;
	}

	public List<CohortDestination> getAllCohorts() {
		List<CohortDestination> result = new ArrayList<>();
		CohortDestinationsDTOExtractor extractor
				= new CohortDestinationsDTOExtractor(this.etlUser, this.groupDao);
		for (CohortDestinationEntity configEntity
				: this.destinationDao.getAllCohortDestinations()) {
			CohortDestination dto = extractor.extractDTO(configEntity);
			if (dto != null) {
				result.add(dto);
			}
		}
		return result;
	}
	
	public List<PatientSetExtractorDestination> getAllPatientSetExtractors() {
		List<PatientSetExtractorDestination> result = new ArrayList<>();
		PatientSetExtractorDestinationsDTOExtractor extractor
				= new PatientSetExtractorDestinationsDTOExtractor(this.etlUser, this.groupDao);
		for (PatientSetExtractorDestinationEntity configEntity
				: this.destinationDao.getAllPatientSetExtractorDestinations()) {
			PatientSetExtractorDestination dto = extractor.extractDTO(configEntity);
			if (dto != null) {
				result.add(dto);
			}
		}
		return result;
	}
	
	public List<PatientSetSenderDestination> getAllPatientSetSenders() {
		List<PatientSetSenderDestination> result = new ArrayList<>();
		PatientSetSenderDestinationsDTOExtractor extractor
				= new PatientSetSenderDestinationsDTOExtractor(this.etlUser, this.groupDao);
		for (PatientSetSenderDestinationEntity configEntity
				: this.destinationDao.getAllPatientSetSenderDestinations()) {
			PatientSetSenderDestination dto = extractor.extractDTO(configEntity);
			if (dto != null) {
				result.add(dto);
			}
		}
		return result;
	}
	
	public List<TabularFileDestination> getAllTabularFiles() {
		List<TabularFileDestination> result = new ArrayList<>();
		TabularFileDestinationsDTOExtractor extractor
				= new TabularFileDestinationsDTOExtractor(this.etlUser, this.groupDao);
		for (TabularFileDestinationEntity configEntity
				: this.destinationDao.getAllTabularFileDestinations()) {
			TabularFileDestination dto = extractor.extractDTO(configEntity);
			if (dto != null) {
				result.add(dto);
			}
		}
		return result;
	}

	/**
	 * Gets the specified source extractDTO. If it does not exist or the current
	 * user lacks read permissions for it, this method returns
	 * <code>null</code>.
	 *
	 * @return a extractDTO.
	 */
	public final Destination getOne(String configId) {
		if (configId == null) {
			throw new IllegalArgumentException("configId cannot be null");
		}
		DestinationDTOExtractorVisitor visitor
				= new DestinationDTOExtractorVisitor(this.etlProperties, this.etlUser, this.groupDao);
		
		DestinationEntity byName = this.destinationDao.getByName(configId);
		if (byName == null) {
			throw new HttpStatusException(Response.Status.NOT_FOUND);
		}
		byName.accept(visitor);
		return visitor.getDestination();
	}

	/**
	 * Gets all configs for which the current user has read permissions.
	 *
	 * @return a {@link List} of configs.
	 */
	public final List<Destination> getAll() {
		List<Destination> result = new ArrayList<>();
		DestinationDTOExtractorVisitor visitor
				= new DestinationDTOExtractorVisitor(this.etlProperties, this.etlUser, this.groupDao);
		for (DestinationEntity configEntity : this.destinationDao.getAll()) {
			configEntity.accept(visitor);
			Destination dto = visitor.getDestination();
			if (dto != null) {
				result.add(dto);
			}
		}
		return result;
	}

	void delete(String destId) {
		DestinationEntity dest = this.destinationDao.getByName(destId);
		if (dest == null || !this.etlUser.equals(dest.getOwner())) {
			throw new HttpStatusException(Response.Status.NOT_FOUND);
		}
		dest.setExpiredAt(new Date());
		this.destinationDao.update(dest);
	}

}
