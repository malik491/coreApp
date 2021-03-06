package edu.depaul.se491.daos.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import edu.depaul.se491.beans.OrderBean;
import edu.depaul.se491.beans.AddressBean;
import edu.depaul.se491.beans.PaymentBean;
import edu.depaul.se491.daos.ConnectionFactory;
import edu.depaul.se491.daos.DAOFactory;
import edu.depaul.se491.enums.OrderStatus;
import edu.depaul.se491.enums.OrderType;
import edu.depaul.se491.loaders.OrderBeanLoader;
import edu.depaul.se491.utils.dao.DAOUtil;
import edu.depaul.se491.utils.dao.DBLabels;

/**
 * Order Data Access Object (DAO)
 * 
 * @author Malik
 */
public class OrderDAO {
	private ConnectionFactory connFactory;
	private OrderBeanLoader loader;
	private AddressDAO addressDAO;
	private OrderItemDAO orderItemDAO;
	private PaymentDAO paymentDAO;
	
	/**
	 * construct OrderDAO
	 * @param daoFactory
	 * @param connFactory
	 */
	public OrderDAO(DAOFactory daoFactory, ConnectionFactory connFactory) {
		this.connFactory = connFactory;
		this.addressDAO = daoFactory.getAddressDAO();
		this.orderItemDAO = daoFactory.getOrderItemDAO();
		this.paymentDAO = daoFactory.getPaymentDAO();
		this.loader = new OrderBeanLoader();
	}
	
	/**
	 * return all orders or empty list
	 * @return
	 * @throws SQLException
	 */
	public List<OrderBean> getAll() throws SQLException {
		return getMultiple(SELECT_ALL_WITH_ORDER_BY_QUERY, null);
	}
	
	/**
	 * return all orders with the given status or empty list
	 * @param status
	 * @return
	 * @throws SQLException
	 */
	public List<OrderBean> getAllWithStatus(final OrderStatus status) throws SQLException {
		return getMultiple(SELECT_ALL_BY_STATUS_QUERY, status.toString());
	}
	
	/**
	 * return all orders with the given type or empty list
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	public List<OrderBean> getAllWithType(final OrderType type) throws SQLException {
		return getMultiple(SELECT_ALL_BY_TYPE_QUERY, type.toString());
	}
	
	/**
	 * return order with the given id or null
	 * @param orderId
	 * @return
	 * @throws SQLException
	 */
	public OrderBean get(final long orderId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		OrderBean order = null;
		try {
			conn = connFactory.getConnection();
			ps = conn.prepareStatement(SELECT_ORDER_BY_ID_QUERY);
			
			ps.setLong(1, orderId);
			
			rs = ps.executeQuery();
			if (rs.next())
				order = loader.loadSingle(rs);
			
			if (order != null)
				order.setOrderItems(orderItemDAO.getOrderItems(order.getId()));
			
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
		return order;		
	}
	
	/**
	 * return order with the given confirmation or null
	 * @param orderConfirmation
	 * @return
	 * @throws SQLException
	 */
	public OrderBean get(final String orderConfirmation) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		OrderBean order = null;
		try {
			conn = connFactory.getConnection();
			ps = conn.prepareStatement(SELECT_BY_CONFIRMATION_QUERY);
			
			ps.setString(1, orderConfirmation);
			rs = ps.executeQuery();
			
			if (rs.next())
				order = loader.loadSingle(rs);
			
			if (order != null)
				order.setOrderItems(orderItemDAO.getOrderItems(order.getId()));
			
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
		return order;		
	}
	
	/**
	 * insert a new order
	 * @param order
	 * @return newly added order
	 * @throws SQLException
	 */
	public OrderBean add(final OrderBean order) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		
		OrderBean result = null;
		try {
			boolean added = false;
			OrderBean addedOrder = copyOrder(order);
			
			/* Transaction :
			 * - add payment
			 * - add address (if delivery order)
			 * - add order
			 * - add order items
			 */
			conn = connFactory.getConnection();
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(INSERT_ORDER_QUERY, Statement.RETURN_GENERATED_KEYS);
			
			
			final PaymentBean addedPayment = paymentDAO.transactionAdd(conn, order.getPayment());
			added = (addedPayment != null);
			
			if (added) {
				addedOrder.setPayment(addedPayment);

				// then handle address
				if (order.getType() == OrderType.DELIVERY) {
					final AddressBean addedAddress = addressDAO.transactionAdd(conn, order.getAddress());
					added = (addedAddress != null);
					if (added)
						addedOrder.setAddress(addedAddress);	
				}
			}
						
			if (added) {
				// add order
				loader.loadParameters(ps, addedOrder, 1);
				added = DAOUtil.validInsert(ps.executeUpdate());
			}
			
			if (added) {
				// add order items
				addedOrder.setId(DAOUtil.getAutGeneratedKey(ps));
				added = orderItemDAO.transactionAdd(conn, addedOrder);				
			}
			
			if (added) {
				// commit the transaction
				conn.commit();
				result = addedOrder;				
			} else {
				// rollback
				conn.rollback();
			}			

		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.addSuppressed(e);
					throw e1;
				}				
			}
			throw e;
		} finally {
			try {
				DAOUtil.close(ps);
				DAOUtil.setAutoCommit(conn, true);
				DAOUtil.close(conn);
			} catch (SQLException e) {
				throw e;
			}
		}
		return result;
	}
	
	/**
	 * delete order with given id
	 * @param orderId
	 * @return
	 * @throws SQLException
	 */
	public boolean delete(final long orderId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		boolean deleted = false;
		
		try {
			OrderBean order = get(orderId);
			if (order == null)
				return false;
			
			/*
			 * Transaction:
			 * - delete order items
			 * - delete order
			 * - delete address (if any)
			 * we don't delete payments
			 */
			conn = connFactory.getConnection();
			conn.setAutoCommit(false);
			
			ps = conn.prepareStatement(DELETE_ORDER_QUERY);
			ps.setLong(1, orderId);

			// delete order items first (have foreign key to order)
			deleted = orderItemDAO.transactionDeleteAll(conn, orderId, order.getOrderItems().length);

			// delete order (has foreign key to address)
			if (deleted)
				deleted = DAOUtil.validDelete(ps.executeUpdate());

			// finally you can delete the address
			if (deleted)
				deleted = (order.getType() == OrderType.DELIVERY)? addressDAO.transactionDelete(conn, order.getAddress().getId()) : true;

			
			if (deleted) {
				// commit
				conn.commit();
			} else {
				/*try to rollback*/
				conn.rollback();
			}
			
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.addSuppressed(e);
					throw e1;
				}				
			}
			throw e;
		} finally {
			try {
				DAOUtil.close(ps);
				DAOUtil.setAutoCommit(conn, true);
				DAOUtil.close(conn);
			} catch (SQLException e) {
				throw e;
			}
		}
		return deleted;
	}
	
	/**
	 * update order
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	public boolean update(final OrderBean order) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;		
		
		boolean updated = false;
		
		try {
			final long orderId = order.getId();
			final OrderBean oldOrder = get(orderId);
			
			OrderBean updatedOrderCopy = copyOrder(order); // copy
			
			/*
			 * transaction:
			 * - update order items
			 * - update order
			 */
			conn = connFactory.getConnection();
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(UPDATE_ORDER_QUERY);
			
			
			OrderType pickup = OrderType.PICKUP;
			OrderType delivery = OrderType.DELIVERY;
			
			
			OrderType oldType = oldOrder.getType();
			OrderType updatedType = updatedOrderCopy.getType();
			
			boolean deletedOldAddress = false;
			
			if (oldType == pickup && updatedType == pickup) {
				// no address
				updated = true;
				
			} else if (oldType == delivery && updatedType == delivery){
				// addresses must have the same id
				final AddressBean oldAddr = oldOrder.getAddress();
				final AddressBean updatedAddr = updatedOrderCopy.getAddress();

				updated = (oldAddr.getId() == updatedAddr.getId());
				
				if (updated) {
					// update address data if different
					boolean isDifferent = hasDifferentData(oldAddr, updatedAddr);
					updated = isDifferent? addressDAO.transactionUpdate(conn, updatedAddr) : true;
				}
				
			} else {
				// add new address (order type changed to delivery) or remove address (type changed to pickup)
				
				if (oldType == pickup && updatedType == delivery) {
					// add new address
					
					AddressBean addedAddress = addressDAO.transactionAdd(conn, updatedOrderCopy.getAddress());
					updated = (addedAddress != null);
					
					if (updated)
						updatedOrderCopy.setAddress(addedAddress);
					
				} else if (oldType == delivery && updatedType == pickup) {
					// remove existing address
					// can't do it here because of foreign key constrain 
					updated = true;
					deletedOldAddress = true;
				}
			}

			// update order items
			if (updated)
				updated = orderItemDAO.transactionUpdate(conn, updatedOrderCopy);
						
			// update order
			if (updated) {
				int paramIndex = 1;
				loader.loadParameters(ps, updatedOrderCopy, paramIndex);
				ps.setLong(paramIndex + ORDER_UPDATE_COLUMNS_COUNT, orderId);
				
				updated = DAOUtil.validUpdate(ps.executeUpdate());

				// if this update deletes an old address, we can only do it now (after setting the order's address_id (foreign key) to NULL)
				if (updated && deletedOldAddress)
					updated = addressDAO.transactionDelete(conn, oldOrder.getAddress().getId());
			}
				
			if (updated) {
				// commit
				conn.commit();				
			} else {
				conn.rollback();				
			}
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.addSuppressed(e);
					throw e1;
				}				
			}
			throw e;
		} finally {
			try {
				DAOUtil.close(ps);
				DAOUtil.setAutoCommit(conn, true);
				DAOUtil.close(conn);
			} catch (SQLException e) {
				throw e;
			}
		}
		return updated;
	}
	
	private List<OrderBean> getMultiple(final String selectSQLQuery, final String paramValue) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<OrderBean> orders = null;
		
		try {
			conn = connFactory.getConnection();
			ps = conn.prepareStatement(selectSQLQuery);
			
			if (paramValue != null)
				ps.setString(1, paramValue);

			rs = ps.executeQuery();
			orders = loader.loadList(rs);
			
			// for each order, set order items
			for (OrderBean order: orders)
				order.setOrderItems(orderItemDAO.getOrderItems(order.getId()));
			
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
		return orders;
	}
	
	private boolean hasDifferentData(final AddressBean oldAddress, final AddressBean newAddress) {
		boolean isSame = false;
		
		isSame  = oldAddress.getLine1().equals(newAddress.getLine1());
		isSame &= oldAddress.getCity().equals(newAddress.getCity());
		isSame &= oldAddress.getState().equals(newAddress.getState());
		isSame &= oldAddress.getZipcode().equals(newAddress.getZipcode());
		
		String oldLine2 = oldAddress.getLine2();
		String newLine2 = newAddress.getLine2();
		
		if (oldLine2 != null && newLine2 != null) {
			isSame &= oldLine2.equals(newLine2);
		} else if (oldLine2 == null && newLine2 == null) {
			isSame &= true;
		} else {
			isSame &= false;
		}
		
		return !isSame;
	}
	
	private OrderBean copyOrder(final OrderBean order) {
		OrderBean bean = new OrderBean();
		
		bean.setId(order.getId());
		bean.setType(order.getType());
		bean.setStatus(order.getStatus());
		bean.setConfirmation(order.getConfirmation());
		bean.setTimestamp(order.getTimestamp());
		bean.setPayment(order.getPayment());
		bean.setOrderItems(order.getOrderItems());
		bean.setAddress(order.getAddress());
		return bean;
	}
	
	private static final String SELECT_ALL_QUERY = 
			String.format("SELECT o.*, p.*, a.%s, a.%s, a.%s, a.%s, a.%s "
						+ "FROM %s AS o NATURAL JOIN %s AS p LEFT JOIN %s AS a ON o.%s = a.%s",
						DBLabels.Address.LINE_1, DBLabels.Address.LINE_2, DBLabels.Address.CITY, 
						DBLabels.Address.STATE, DBLabels.Address.ZIPCODE, DBLabels.Order.TABLE,
						DBLabels.Payment.TABLE, DBLabels.Address.TABLE, DBLabels.Order.ADDRESS_ID,
						DBLabels.Address.ID);
	
	private static final String SELECT_ALL_WITH_ORDER_BY_QUERY = 
			String.format("%s ORDER BY o.%s", SELECT_ALL_QUERY, DBLabels.Order.ID);
	
	private static final String SELECT_ORDER_BY_ID_QUERY = 
			String.format("%s WHERE (o.%s = ?)", SELECT_ALL_QUERY, DBLabels.Order.ID);
	
	private static final String SELECT_ALL_BY_STATUS_QUERY = 
			String.format("%s WHERE (o.%s = ?) ORDER BY o.%s ASC", SELECT_ALL_QUERY, DBLabels.Order.STATUS, DBLabels.Order.TIMESTAMP);
	
	private static final String SELECT_ALL_BY_TYPE_QUERY = 
			String.format("%s WHERE (o.%s = ?) ORDER BY o.%s ASC", SELECT_ALL_QUERY, DBLabels.Order.TYPE, DBLabels.Order.ID);
	
	private static final String SELECT_BY_CONFIRMATION_QUERY = 	
			String.format("%s WHERE (UPPER(o.%s) = UPPER(?))", SELECT_ALL_QUERY, DBLabels.Order.CONFIRMATION);
	
	private static final String INSERT_ORDER_QUERY = 
			String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)", 
						  DBLabels.Order.TABLE, DBLabels.Order.TYPE, DBLabels.Order.STATUS, 
						  DBLabels.Order.CONFIRMATION, DBLabels.Order.TIMESTAMP, DBLabels.Order.PAYMENT_ID, 
						  DBLabels.Order.ADDRESS_ID);	
	
	private static final String UPDATE_ORDER_QUERY = 
			String.format("UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?, %s=? WHERE (%s = ?)", 
					  DBLabels.Order.TABLE, DBLabels.Order.TYPE, DBLabels.Order.STATUS, 
					  DBLabels.Order.CONFIRMATION, DBLabels.Order.TIMESTAMP, DBLabels.Order.PAYMENT_ID,
					  DBLabels.Order.ADDRESS_ID, DBLabels.Order.ID);		
	
	private static final String DELETE_ORDER_QUERY = 
			String.format("DELETE FROM %s WHERE (%s = ?)", DBLabels.Order.TABLE, DBLabels.Order.ID);
	
	private static final int ORDER_UPDATE_COLUMNS_COUNT = 6;
}
