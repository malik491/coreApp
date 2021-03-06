package edu.depaul.se491.daos;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database Connection factory that causes Exception for testing
 * @author Malik
 */
public class ExceptionConnectionFactory implements ConnectionFactory {

	@Override
	public Connection getConnection() throws SQLException {
		return new BadConnection();
	}

	@Override
	public void close() throws SQLException {
		throw new SQLException();
	}

}
