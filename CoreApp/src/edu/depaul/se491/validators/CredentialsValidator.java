package edu.depaul.se491.validators;

import edu.depaul.se491.beans.CredentialsBean;
import edu.depaul.se491.utils.ParamLengths;

/**
 * CredentialsBean Validator
 * 
 * @author Malik
 */
public class CredentialsValidator extends BeanValidator {

	/**
	 * validate CredentialsBean
	 * @param bean
	 * @return
	 */
	public boolean validate(CredentialsBean bean) {
		boolean isValid = isValidObject(bean);
	
		if(isValid){
			isValid  = isValidUsername(bean.getUsername());
			isValid &= isValidPassword(bean);
		}
		
		return isValid;
	}
	
	/**
	 * validate username
	 * @param username
	 * @return
	 */
	public boolean isValidUsername(String username) {
		return isValidString(username, ParamLengths.Credentials.MIN_USERNAME, ParamLengths.Credentials.MAX_USERNAME);
	}
	
	private boolean isValidPassword(CredentialsBean bean) {
		return isValidString(bean.getPassword(), ParamLengths.Credentials.MIN_PASSWORD, ParamLengths.Credentials.MAX_PASSWORD);
	}
}
