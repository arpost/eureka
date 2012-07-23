package edu.emory.cci.aiw.cvrg.eureka.common.test;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.persist.PersistService;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * Base class for all the Jersey resource related test classes.
 *
 * @author hrathod
 */
public abstract class AbstractResourceTest extends JerseyTest {

	/**
	 * Class level logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(
			AbstractResourceTest.class);
	/**
	 * The context path for the test.
	 */
	private static final String CONTEXT_PATH = "/";
	/**
	 * The servlet path for the test.
	 */
	private static final String SERVLET_PATH = "/";
	/**
	 * An instance of Guice Injector, used to create other necessary objects for
	 * the test.
	 */
	private final Injector injector;
	/**
	 * An instance of the data provider class.
	 */
	private TestDataProvider dataProvider;
	/**
	 * The persist service set up to set up and tear down test data.
	 */
	private PersistService persistService;

	/**
	 * Create the context, filters, etc. for the embedded server to test
	 * against.
	 */
	protected AbstractResourceTest() {
		super();
		this.injector = Guice.createInjector(this.getModules());
	}

	@Override
	protected AppDescriptor configure() {
		ClientConfig clientConfig = new DefaultClientConfig();
		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
				Boolean.TRUE);
		return (new WebAppDescriptor.Builder()).contextListenerClass(
				getListener()).filterClass(getFilter()).contextPath(CONTEXT_PATH).
				servletPath(SERVLET_PATH).clientConfig(clientConfig).build();
	}

	/**
	 * Runs any test set up code before the actual test is run. The data for the
	 * test is created from this method.
	 *
	 * @throws Exception Thrown if there are errors while creating test
	 * data, or propagating exception from super.setUp().
	 */
	@Override
	public final void setUp() throws Exception {
		super.setUp();
		this.persistService = this.injector.getInstance(PersistService.class);
		this.persistService.start();

		this.dataProvider = this.injector.getInstance(this.getDataProvider());
		this.dataProvider.setUp();
	}

	/**
	 * Tears down any scaffolding for the test after the actual test is
	 * finished. The data for the test is destroyed from this method.
	 *
	 * @throws Exception Thrown if there are errors while cleaning up test data,
	 * or propagating exception from super.tearDown().
	 */
	@Override
	public final void tearDown() throws Exception {
		this.dataProvider.tearDown();
		this.dataProvider = null;

		this.persistService.stop();
		this.persistService = null;
		super.tearDown();
	}

	/**
	 * Returns an instance of the given class, using the Guice Injector.
	 *
	 * @param <T> The generic class from which to create the instance.
	 * @param className The name of the class.
	 * @return An instance of the named class.
	 */
	protected final <T> T getInstance(Class<T> className) {
		return this.injector.getInstance(className);
	}

	/**
	 * Returns the context listener to use when configuring an AppDescriptor for
	 * the test.
	 *
	 * @return A servlet context listener for the test.
	 */
	protected abstract Class<? extends ServletContextListener> getListener();

	/**
	 * Returns the filter to use when configuring an AppDescriptor for the test.
	 *
	 * @return A filter for the test.
	 */
	protected abstract Class<? extends Filter> getFilter();

	/**
	 * Returns the class used to set up the data for the test.
	 *
	 * @return A data provider for the test.
	 */
	protected abstract Class<? extends TestDataProvider> getDataProvider();

	/**
	 * Returns a set of Google Guice modules used to configure the injector for
	 * the test.
	 *
	 * @return An array of Modules.
	 */
	protected abstract Module[] getModules();
}