package edu.depaul.se491.validators;

import edu.depaul.se491.beans.OrderItemBean;
import edu.depaul.se491.utils.ParamValues;

/**
 * OrderItemBean Validator
 * 
 * @author Malik
 */
public class OrderItemValidator extends BeanValidator {

	/**
	 * validate OrderItemBean
	 * @param bean
	 * @return
	 */
	public boolean validate(OrderItemBean bean) {
		boolean isValid = isValidObject(bean);

		if(isValid){
			isValid  = isValidMenuItem(bean);
			isValid &= isValidQuantity(bean);
			isValid &= isValidStatus(bean);
		}
		
		return isValid;
	}

	private boolean isValidQuantity(OrderItemBean bean) {
		return isValidValue(bean.getQuantity(), ParamValues.OrderItem.MIN_QTY, ParamValues.OrderItem.MAX_QTY);
	}

	private boolean isValidMenuItem(OrderItemBean bean) {
		return isValidObject(bean.getMenuItem());
	}
	
	private boolean isValidStatus(OrderItemBean bean) {
		return isValidObject(bean.getStatus());
	}
}
