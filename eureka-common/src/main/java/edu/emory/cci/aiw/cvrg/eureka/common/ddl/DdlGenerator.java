package edu.emory.cci.aiw.cvrg.eureka.common.ddl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates the SQL statements needed to create the tables used to persist the
 * model for the application.
 * 
 * @author hrathod
 * 
 */
public class DdlGenerator {

	/**
	 * The class level logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DdlGenerator.class);
	/**
	 * The dialect to produce the SQL for.
	 */
	private static final String DIALECT = "org.hibernate.dialect.Oracle10gDialect";

	/**
	 * Given a list of classes annotated with the {@link Entity} annotation, a
	 * Hibernate database dialect, and an output file name, generates the SQL
	 * needed to persist the given entities to a database and writes the SQL out
	 * to the given file.
	 * 
	 * @param classes A list of classes to generate SQL for.
	 * @param outFile The location of the output file.
	 */
	private static void generate(List<Class<?>> classes, String outFile) {
		final Configuration configuration = new Configuration();
		configuration.setProperty("hibernate.dialect", DIALECT);
		configuration.setProperty("hibernate.hbm2ddl.auto", "create");

		for (Class<?> c : classes) {
			Entity annotation = c.getAnnotation(Entity.class);
			if (annotation != null) {
				configuration.addAnnotatedClass(c);
			} else {
				LOGGER.warn("{} is not annotated with @Entity", c.getName());
			}
		}

		SchemaExport export = new SchemaExport(configuration);
		export.setDelimiter(";");
		export.setOutputFile(outFile);
		export.execute(true, false, false, false);
	}

	/**
	 * Generate the SQL for entities used in ETL layer classes.
	 * 
	 * @param outputFile The location of file where the SQL should be written.
	 */
	private static void generateBackendDdl(final String outputFile) {
		final List<Class<?>> backendClasses = new ArrayList<Class<?>>();
		backendClasses
				.add(edu.emory.cci.aiw.cvrg.eureka.common.entity.Configuration.class);
		backendClasses
				.add(edu.emory.cci.aiw.cvrg.eureka.common.entity.Job.class);
		backendClasses
				.add(edu.emory.cci.aiw.cvrg.eureka.common.entity.JobEvent.class);
		generate(backendClasses, outputFile);
	}

	/**
	 * Generate the SQL for entities used in service layer classes.
	 * 
	 * @param outputFile The location of file where the SQL should be written.
	 */
	private static void generateServiceDdl(final String outputFile) {
		final List<Class<?>> serviceClasses = new ArrayList<Class<?>>();
		serviceClasses
				.add(edu.emory.cci.aiw.cvrg.eureka.common.entity.User.class);
		serviceClasses
				.add(edu.emory.cci.aiw.cvrg.eureka.common.entity.FileUpload.class);
		serviceClasses
				.add(edu.emory.cci.aiw.cvrg.eureka.common.entity.FileError.class);
		serviceClasses
				.add(edu.emory.cci.aiw.cvrg.eureka.common.entity.FileWarning.class);
		serviceClasses
				.add(edu.emory.cci.aiw.cvrg.eureka.common.entity.Role.class);
		serviceClasses
				.add(edu.emory.cci.aiw.cvrg.eureka.common.entity.AbstractFileInfo.class);

		generate(serviceClasses, outputFile);
	}

	/**
	 * @param args Command line arguments to the application. <b>Note:</b> these
	 *            arguments are currently not utilized. The first param is the
	 *            file where the service layer DDL should go, and the second
	 *            parameter is where the ETL layer DDL should go.
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Please provide two parameters, first for "
					+ "the service layer file and the second for "
					+ "the ETL layer file.");
		} else {
			generateServiceDdl(args[0]);
			generateBackendDdl(args[1]);
		}
	}
}