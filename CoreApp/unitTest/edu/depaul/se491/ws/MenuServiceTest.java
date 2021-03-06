package edu.depaul.se491.ws;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.depaul.se491.beans.CredentialsBean;
import edu.depaul.se491.beans.MenuItemBean;
import edu.depaul.se491.beans.RequestBean;
import edu.depaul.se491.daos.ConnectionFactory;
import edu.depaul.se491.daos.TestConnectionFactory;
import edu.depaul.se491.daos.TestDAOFactory;
import edu.depaul.se491.enums.MenuItemCategory;
import edu.depaul.se491.test.DBBuilder;
import edu.depaul.se491.test.TestDataGenerator;

/**
 * 
 * @author Malik
 *
 */
public class MenuServiceTest {
	private static ConnectionFactory connFactory;
	private static TestDAOFactory daoFactory;
	private static DBBuilder dbBuilder;
	private static TestDataGenerator testDataGen;
	private MenuService service;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		connFactory = TestConnectionFactory.getInstance();
		daoFactory = new TestDAOFactory(connFactory);
		dbBuilder = new DBBuilder(connFactory);
		testDataGen = new TestDataGenerator(connFactory);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		dbBuilder.rebuildAll();
		testDataGen.generateData();
		
		// release and close resources
		dbBuilder = null;
		testDataGen = null;
		daoFactory = null;
		// close connection data source (pool)
		connFactory.close();
		
	}

	@Before
	public void setUp() throws Exception {
		// create menu service 
		service = new MenuService(daoFactory);
		 
		// rebuild the DB before each method
		dbBuilder.rebuildAll();
			
		// generate test data
		testDataGen.generateData();
	}
	
	@Test
	public void testGet() {
		// invalid request
		Response response = service.get(null);
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.get(new RequestBean<Long>(null, null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.get(new RequestBean<Long>(null, new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.get(new RequestBean<Long>(new CredentialsBean(), new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());


		// invalid request
		response = service.get(new RequestBean<Long>(new CredentialsBean("manager", "password"), null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// valid request, not found
		response = service.get(new RequestBean<Long>(new CredentialsBean("manager", "password"), new Long(100)));
		assertNotNull(response);
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
		
		// valid request, invalid id
		response = service.get(new RequestBean<Long>(new CredentialsBean("manager", "password"), new Long(0)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		
		// valid request
		response = service.get(new RequestBean<Long>(new CredentialsBean("manager", "password"), new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		MenuItemBean menuItem = (MenuItemBean) response.getEntity();
		assertNotNull(menuItem);
	}

	@Test
	public void testPost() {
		// invalid request
		Response response = service.post(null);
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.post(new RequestBean<MenuItemBean>(null, null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.post(new RequestBean<MenuItemBean>(null, new MenuItemBean()));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.post(new RequestBean<MenuItemBean>(new CredentialsBean(), new MenuItemBean()));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());


		// invalid request
		response = service.post(new RequestBean<MenuItemBean>(new CredentialsBean("manager", "password"), null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		
		// valid request, bad menu item data
		response = service.post(new RequestBean<MenuItemBean>(new CredentialsBean("manager", "password"), new MenuItemBean()));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		

		// valid request
		MenuItemBean menuItem = new MenuItemBean(0L, "name", "description", 12.45, MenuItemCategory.SIDE);
		response = service.post(new RequestBean<MenuItemBean>(new CredentialsBean("manager", "password"), menuItem));
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		MenuItemBean createdMenuItem = (MenuItemBean) response.getEntity();
		assertNotNull(createdMenuItem);
	}

	@Test
	public void testUpdate() {
		// invalid request
		Response response = service.post(null);
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.update(new RequestBean<MenuItemBean>(null, null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.update(new RequestBean<MenuItemBean>(null, new MenuItemBean()));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.update(new RequestBean<MenuItemBean>(new CredentialsBean(), new MenuItemBean()));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());


		// invalid request
		response = service.update(new RequestBean<MenuItemBean>(new CredentialsBean("manager", "password"), null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// valid request, bad menu item data
		response = service.update(new RequestBean<MenuItemBean>(new CredentialsBean("manager", "password"), new MenuItemBean()));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		
		// valid request
		MenuItemBean oldMenuItem = new MenuItemBean(1L, "updated name", "updated description", 12.45, MenuItemCategory.SIDE);
		response = service.update(new RequestBean<MenuItemBean>(new CredentialsBean("manager", "password"), oldMenuItem));
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		Boolean updated = (Boolean) response.getEntity();
		assertNotNull(updated);
		assertTrue(updated);
		
		MenuItemBean updatedMenuItem = (MenuItemBean) service.get(new RequestBean<Long>(new CredentialsBean("manager", "password"), new Long(1))).getEntity();
		assertNotNull(updatedMenuItem);
		
		assertEquals(oldMenuItem.getId(), updatedMenuItem.getId());
		assertEquals(oldMenuItem.getName(), updatedMenuItem.getName());
		assertEquals(oldMenuItem.getDescription(), updatedMenuItem.getDescription());
		assertEquals(0, Double.compare(oldMenuItem.getPrice(), updatedMenuItem.getPrice()));
		assertEquals(oldMenuItem.getItemCategory(), updatedMenuItem.getItemCategory());
				
	}

	@Test
	public void testDelete() {
		// invalid request
		Response response = service.delete(null);
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.delete(new RequestBean<Long>(null, null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.delete(new RequestBean<Long>(null, new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.delete(new RequestBean<Long>(new CredentialsBean(), new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.delete(new RequestBean<Long>(new CredentialsBean("manager", "password"), null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// valid request, invalid id
		response = service.delete(new RequestBean<Long>(new CredentialsBean("manager", "password"), new Long(0)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		
		// valid request
		response = service.delete(new RequestBean<Long>(new CredentialsBean("manager", "password"), new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		Boolean deleted = (Boolean) response.getEntity();
		assertNotNull(deleted);
		assertFalse(deleted);
		
	}
	
	@Test
	public void testHide() {
		// invalid request
		Response response = service.hide(null);
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.hide(new RequestBean<Long>(null, null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.hide(new RequestBean<Long>(null, new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.hide(new RequestBean<Long>(new CredentialsBean(), new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.hide(new RequestBean<Long>(new CredentialsBean("manager", "password"), null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// valid request, invalid id
		response = service.hide(new RequestBean<Long>(new CredentialsBean("manager", "password"), new Long(0)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		
		// valid request
		response = service.hide(new RequestBean<Long>(new CredentialsBean("manager", "password"), new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		Boolean updated = (Boolean) response.getEntity();
		assertNotNull(updated);
		assertTrue(updated);
	}
	
	@Test
	public void testUnhide() {
		// invalid request
		Response response = service.unhide(null);
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.unhide(new RequestBean<Long>(null, null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		// invalid request
		response = service.unhide(new RequestBean<Long>(null, new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.unhide(new RequestBean<Long>(new CredentialsBean(), new Long(1)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.unhide(new RequestBean<Long>(new CredentialsBean("manager", "password"), null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// valid request, invalid id
		response = service.unhide(new RequestBean<Long>(new CredentialsBean("manager", "password"), new Long(0)));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		
		// valid request
		response = service.unhide(new RequestBean<Long>(new CredentialsBean("manager", "password"), new Long(7)));
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		Boolean updated = (Boolean) response.getEntity();
		assertNotNull(updated);
		assertTrue(updated);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetAllVisible() {
		// invalid request
		Response response = service.getAllVisible(null);
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		
		// invalid request
		response = service.getAllVisible(new RequestBean<Object>(null, null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.getAllVisible(new RequestBean<Object>(new CredentialsBean(), null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// valid request but unauthorized 
		response = service.getAllVisible(new RequestBean<Object>(new CredentialsBean("admin", "password"), null));
		assertNotNull(response);
		assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());

		// valid request
		response = service.getAllVisible(new RequestBean<Object>(new CredentialsBean("manager", "password"), null));
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		List<MenuItemBean> menuItems = (ArrayList<MenuItemBean>) response.getEntity();
		assertNotNull(menuItems);
		assertEquals(6, menuItems.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetAllHidden() {
		// invalid request
		Response response = service.getAllHidden(null);
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		
		
		// invalid request
		response = service.getAllHidden(new RequestBean<Object>(null, null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// invalid request
		response = service.getAllHidden(new RequestBean<Object>(new CredentialsBean(), null));
		assertNotNull(response);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// valid request but unauthorized 
		response = service.getAllHidden(new RequestBean<Object>(new CredentialsBean("admin", "password"), null));
		assertNotNull(response);
		assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());

		// valid request
		response = service.getAllHidden(new RequestBean<Object>(new CredentialsBean("manager", "password"), null));
		assertNotNull(response);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		List<MenuItemBean> menuItems = (ArrayList<MenuItemBean>) response.getEntity();
		assertNotNull(menuItems);
		assertEquals(1, menuItems.size());
	}

}
