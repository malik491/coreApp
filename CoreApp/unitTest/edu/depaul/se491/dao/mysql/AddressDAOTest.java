package edu.depaul.se491.dao.mysql;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.depaul.se491.beans.AddressBean;
import edu.depaul.se491.daos.BadConnection;
import edu.depaul.se491.daos.ConnectionFactory;
import edu.depaul.se491.daos.ExceptionConnectionFactory;
import edu.depaul.se491.daos.TestConnectionFactory;
import edu.depaul.se491.daos.TestDAOFactory;
import edu.depaul.se491.daos.mysql.AddressDAO;
import edu.depaul.se491.enums.AddressState;
import edu.depaul.se491.test.DBBuilder;
import edu.depaul.se491.test.TestDataGenerator;

/**
 * 
 * @author Malik
 *
 */
public class AddressDAOTest {
	private static ConnectionFactory connFactory;
	private static DBBuilder dbBuilder;
	private static TestDataGenerator testDataGen;
	
	private static AddressDAO addressDAO;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		connFactory = TestConnectionFactory.getInstance();
		dbBuilder = new DBBuilder(connFactory);
		testDataGen = new TestDataGenerator(connFactory);
		
		
		addressDAO = new TestDAOFactory(connFactory).getAddressDAO();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// rebuild to original state
		dbBuilder.rebuildAll();
		testDataGen.generateData();
		
		// release resources
		dbBuilder = null;
		testDataGen = null;
		addressDAO = null;
		
		// close connection data source (pool)
		connFactory.close();
	}

	@Before
	public void setUp() throws Exception {
		// rebuild the DB before each method
		dbBuilder.rebuildAll();
			
		// generate test data
		testDataGen.generateData();
	}

	@Test
	public void testAddressDAO() {
		assertNotNull(addressDAO);
	}

	@Test
	public void testGet() throws SQLException {
		AddressBean address = addressDAO.get(1L);
		assertNotNull(address);
		
		assertEquals(1L, address.getId());
		assertEquals("100 line1 st", address.getLine1());
		assertEquals("apt 1", address.getLine2());
		assertEquals("Chicago", address.getCity());
		assertEquals(AddressState.IL, address.getState());
		assertEquals("60601", address.getZipcode());
		
	}

	@Test
	public void testTransactionAdd() throws SQLException {
		AddressBean address = new AddressBean(0L, "line 1", null, "Chicago", AddressState.IL, "1234567890");
		AddressBean addedAddress = null;
		
		Connection con = null;
		try {
			con = connFactory.getConnection();
			addedAddress = addressDAO.transactionAdd(con, address);
			
		} catch (SQLException e) {
			throw e;
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
		
		assertNotNull(addedAddress);
		
		long expectedId = 7L;
		assertEquals(expectedId, addedAddress.getId());
		assertEquals(address.getLine1(), addedAddress.getLine1());
		assertNull(addedAddress.getLine2());
		assertEquals(address.getCity(), addedAddress.getCity());
		assertEquals(address.getState(), addedAddress.getState());
		assertEquals(address.getZipcode(), addedAddress.getZipcode());
		
	}

	@Test
	public void testTransactionUpdate() throws SQLException {
		AddressBean oldAddress = addressDAO.get(1L);
		oldAddress.setLine1("updated line 1");
		oldAddress.setLine2(null);
		oldAddress.setCity("New York City");
		oldAddress.setState(AddressState.NY);
		oldAddress.setZipcode("54321");
		
		Connection con = null;
		boolean updated = false;
		try {
			con = connFactory.getConnection();
			updated = addressDAO.transactionUpdate(con, oldAddress);
			
		} catch (SQLException e) {
			throw e;
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
		
		assertTrue(updated);

		AddressBean updatedAddress = addressDAO.get(1L);
		
		assertEquals(oldAddress.getId(), updatedAddress.getId());
		assertEquals(oldAddress.getLine1(), updatedAddress.getLine1());
		assertNull(updatedAddress.getLine2());
		assertEquals(oldAddress.getCity(), updatedAddress.getCity());
		assertEquals(oldAddress.getState(), updatedAddress.getState());
		assertEquals(oldAddress.getZipcode(), updatedAddress.getZipcode());
	}

	@Test
	public void testTransactionDelete() throws SQLException {
		long id = -1L;
		
		Connection con = null;
		boolean deleted = false;
		try {
			con = connFactory.getConnection();
			AddressBean newAddress = addressDAO.transactionAdd(con, new AddressBean(0L, "line 1", null, "Chicago", AddressState.IL, "1234567890"));
			id = newAddress.getId();
			deleted = addressDAO.transactionDelete(con, id);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
		
		assertTrue(deleted);
		assertNull(addressDAO.get(id));
	}
	
	
	@Test
	public void testExceptions() {
		AddressDAO dao = new TestDAOFactory(new ExceptionConnectionFactory()).getAddressDAO();
		try {
			dao.get(1L);
			fail("No Exception Thrown");
		} catch (SQLException e) {}

		try {
			dao.transactionAdd(new BadConnection(), new AddressBean());
			fail("No Exception Thrown");
		} catch (SQLException e) {}
		
		try {
			dao.transactionDelete(new BadConnection(), 1L);
			fail("No Exception Thrown");			
		} catch (SQLException e) {}
		
		try {
			dao.transactionUpdate(new BadConnection(), new AddressBean());
			fail("No Exception Thrown");
		} catch (SQLException e) {}
		
		assertTrue(true);
	}

}
