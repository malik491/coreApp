package edu.depaul.se491.daos.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mysql.jdbc.Statement;

import edu.depaul.se491.beans.AddressBean;
import edu.depaul.se491.beans.UserBean;
import edu.depaul.se491.daos.ConnectionFactory;
import edu.depaul.se491.daos.DAOFactory;
import edu.depaul.se491.loaders.UserBeanLoader;
import edu.depaul.se491.utils.dao.DAOUtil;
import edu.depaul.se491.utils.dao.DBLabels;

/**
 * User Data Access Object (DAO)
 * 
 * @author Malik
 */
public class UserDAO {
	private ConnectionFactory connFactory;
	private UserBeanLoader loader;
	private AddressDAO addressDAO;

	/**
	 * constrcut UserDAO
	 * @param daoFactory
	 * @param connFactory
	 */
	public UserDAO(DAOFactory daoFactory, ConnectionFactory connFactory) {
		this.connFactory = connFactory;
		this.addressDAO = daoFactory.getAddressDAO();
		this.loader = new UserBeanLoader();
	}
	
	/**
	 * return user by id
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public UserBean get(long id) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		UserBean user = null;
		try {
			conn = connFactory.getConnection();
			ps = conn.prepareStatement(SELECT_BY_ID_QUERY);
			
			ps.setLong(1, id);
			rs = ps.executeQuery();
			
			if (rs.next())
				user = loader.loadSingle(rs);
			
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				DAOUtil.close(rs);
				DAOUtil.close(ps);
				DAOUtil.close(conn);
			} catch (SQLException e) {
				throw e;
			}
		}
		return user;
	}
	
	/**
	 * insert a new user using the given connection (transaction)
	 * @param conn
	 * @param user
	 * @return newly added user
	 * @throws SQLException
	 */
	public UserBean transactionAdd(Connection conn, final UserBean user) throws SQLException {
		PreparedStatement ps = null;
		UserBean addedUser = null;
		try {
			
			// add new address
			AddressBean addedAddress = addressDAO.transactionAdd(conn, user.getAddress());
			boolean added = addedAddress != null;
			
			if (added) {
				addedUser = new UserBean(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(), addedAddress);
				
				ps = conn.prepareStatement(INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
				loader.loadParameters(ps, addedUser, 1);
				added = DAOUtil.validInsert(ps.executeUpdate());
				
				if (added) {
					// set its new id
					addedUser.setId(DAOUtil.getAutGeneratedKey(ps));
				} else {
					addedUser = null;
				}
			}
						
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				DAOUtil.close(ps);
			} catch (SQLException e) {
				throw e;
			}
		}
		return addedUser;
	}

	/**
	 * update a user using the given connection (transaction)
	 * @param conn
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	public boolean transactionUpdate(Connection conn, final UserBean user) throws SQLException {
		PreparedStatement ps = null;
		boolean updated = false;
		try {
			updated = addressDAO.transactionUpdate(conn, user.getAddress());	
			
			if (updated) {
				ps = conn.prepareStatement(UPDATE_USER_QUERY);
				int paramIndex = 1;
				loader.loadParameters(ps, user, paramIndex);
				ps.setLong(paramIndex + UPDATE_COLUMNS_COUNT, user.getId());
				updated = DAOUtil.validUpdate(ps.executeUpdate());
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				DAOUtil.close(ps);
			} catch (SQLException e) {
				throw e;
			}
		}
		return updated;
	}
	
	/**
	 * delete user using the given connection (transaction)
	 * @param conn
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	public boolean transactionDelete(Connection conn, UserBean user) throws SQLException {
		PreparedStatement ps = null;
		boolean deleted = false;
		try {
			// delete user then delete address (foreign key in user)
			ps = conn.prepareStatement(DELETE_USER_QUERY);
			ps.setLong(1, user.getId());
			deleted = DAOUtil.validDelete(ps.executeUpdate());
			
			if (deleted)
				deleted = addressDAO.transactionDelete(conn, user.getAddress().getId());	
			
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				DAOUtil.close(ps);
			} catch (SQLException e) {
				throw e;
			}
		}
		return deleted;
	}
	
	private static final String SELECT_BY_ID_QUERY = String.format("SELECT * FROM %s NATURAL JOIN %s WHERE (%s = ?)", DBLabels.User.TABLE, DBLabels.Address.TABLE, DBLabels.User.ID);
	
	private static final String INSERT_USER_QUERY = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?,?,?,?,?)",
																DBLabels.User.TABLE, DBLabels.User.F_NAME, DBLabels.User.L_NAME,
																DBLabels.User.EMAIL, DBLabels.User.PHONE, DBLabels.User.ADDRESS_ID);
	
	private static final String UPDATE_USER_QUERY = String.format("UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=? WHERE (%s = ?)",
																DBLabels.User.TABLE, DBLabels.User.F_NAME, DBLabels.User.L_NAME,
																DBLabels.User.EMAIL, DBLabels.User.PHONE, DBLabels.User.ADDRESS_ID, 
																DBLabels.User.ID);
	
	private static final String DELETE_USER_QUERY = String.format("DELETE FROM %s WHERE (%s = ?)", DBLabels.User.TABLE, DBLabels.User.ID);
	
	
	private static final int UPDATE_COLUMNS_COUNT = 5;
}
