package edu.emory.cci.aiw.cvrg.eureka.common.entity;

/*
 * #%L
 * Eureka Common
 * %%
 * Copyright (C) 2012 - 2013 Emory University
 * %%
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
 * #L%
 */

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Andrew Post
 */
@Entity
@Table(name="sourceconfigs_groups", uniqueConstraints=@UniqueConstraint(columnNames={"sourceconfigs_id", "groups_id"}))
public class SourceConfigGroupMembership {
	@Id
	@SequenceGenerator(name = "SC_GRP_SEQ_GENERATOR", sequenceName = "SC_GRP_SEQ",
	allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,
	generator = "SC_GRP_SEQ_GENERATOR")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="groups_id", nullable = false)
	private EtlGroup group;
	
	@ManyToOne
	@JoinColumn(name="sourceconfigs_id", nullable = false)
	private SourceConfigEntity sourceConfig;
	
	private boolean groupRead;
	
	private boolean groupWrite;
	
	private boolean groupExecute;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public EtlGroup getGroup() {
		return group;
	}

	public void setGroup(EtlGroup group) {
		this.group = group;
	}

	public SourceConfigEntity getSourceConfig() {
		return sourceConfig;
	}

	public void setSourceConfig(SourceConfigEntity sourceConfig) {
		this.sourceConfig = sourceConfig;
	}
	
	public boolean isGroupRead() {
		return groupRead;
	}

	public void setGroupRead(boolean groupRead) {
		this.groupRead = groupRead;
	}

	public boolean isGroupWrite() {
		return groupWrite;
	}

	public void setGroupWrite(boolean groupWrite) {
		this.groupWrite = groupWrite;
	}

	public boolean isGroupExecute() {
		return groupExecute;
	}

	public void setGroupExecute(boolean groupExecute) {
		this.groupExecute = groupExecute;
	}

	public final String configName() {
		return this.sourceConfig != null ? this.sourceConfig.getName() : null;
	}

	
}
