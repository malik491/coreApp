package edu.depaul.se491.ws;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import edu.depaul.se491.beans.MenuItemBean;
import edu.depaul.se491.beans.RequestBean;
import edu.depaul.se491.daos.DAOFactory;
import edu.depaul.se491.daos.ProductionDAOFactory;
import edu.depaul.se491.models.MenuModel;
import edu.depaul.se491.validators.CredentialsValidator;

/**
 * Menu RESTful Web Service
 * 
 * @author Malik
 */
@Path("/menuItem")
public class MenuService {
	private static DAOFactory daoFactory;
	
	/**
	 * construct MenuService
	 * with a production instance DAOFactory
	 */
	public MenuService() {
		daoFactory = ProductionDAOFactory.getInstance();
	}

	/**
	 * construct MenuService with a DAOFactory
	 * @param factory
	 */
	public MenuService(DAOFactory factory) {
		daoFactory = factory;
	}

	/**
	 * return a Response with MenuItemBean or a string message
	 * @param request
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/get")
	public Response get(RequestBean<Long> request) {
		Response response = null;
		boolean isValid = isValidRequest(request, false);
		
		if (isValid) {	
			MenuModel model = new MenuModel(daoFactory, request.getCredentials());	
			MenuItemBean menuItem  = model.read(request.getExtra());
			if (menuItem == null) {
				response = getResponse(model.getResponseStatus(), model.getResponseMessage());
			} else {
				response = getResponse(Status.OK, menuItem);
			}
		} else {
			response = getResponse(Status.BAD_REQUEST, INVALID_RQST_MSG);
		}
		
		return response;
	}
		
	/**
	 * return a Response with newly added MenuItemBean or a string message
	 * @param request
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/post")
	public Response post(RequestBean<MenuItemBean> request) {
		Response response = null;
		boolean isValid = isValidRequest(request, false);
		
		if (isValid) {	
			MenuModel model = new MenuModel(daoFactory, request.getCredentials());	
			MenuItemBean createdMenuItem  = model.create(request.getExtra());
			if (createdMenuItem == null)
				response = getResponse(model.getResponseStatus(), model.getResponseMessage());
			else
				response = getResponse(Status.OK, createdMenuItem);
		} else {
			response = getResponse(Status.BAD_REQUEST, INVALID_RQST_MSG);
		}
		
		return response;
	}
	
	/**
	 * return a Response with Boolean or a string message
	 * @param request
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/update")
	public Response update(RequestBean<MenuItemBean> request) {
		Response response = null;
		boolean isValid = isValidRequest(request, false);
		
		if (isValid) {	
			MenuModel model = new MenuModel(daoFactory, request.getCredentials());	
			Boolean updated  = model.update(request.getExtra());
			if (updated == null)
				response = getResponse(model.getResponseStatus(), model.getResponseMessage());
			else
				response = getResponse(Status.OK, updated);
		} else {
			response = getResponse(Status.BAD_REQUEST, INVALID_RQST_MSG);
		}
		
		return response;
	}
	
	/**
	 * return a Response with Boolean or a string message
	 * @param request
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/delete")
	public Response delete(RequestBean<Long> request) {
		Response response = null;
		boolean isValid = isValidRequest(request, false);
		
		if (isValid) {	
			MenuModel model = new MenuModel(daoFactory, request.getCredentials());	
			Boolean deleted  = model.delete(request.getExtra());
			if (deleted == null)
				response = getResponse(model.getResponseStatus(), model.getResponseMessage());
			else
				response = getResponse(Status.OK, deleted);
		} else {
			response = getResponse(Status.BAD_REQUEST, INVALID_RQST_MSG);
		}
		
		return response;
	}
	
	/**
	 * return a Response with Boolean or a string message
	 * @param request
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/hide")
	public Response hide(RequestBean<Long> request) {
		Response response = null;
		boolean isValid = isValidRequest(request, false);
		
		if (isValid) {	
			MenuModel model = new MenuModel(daoFactory, request.getCredentials());	
			Boolean updated  = model.updateIsHidden(request.getExtra(), true);
			if (updated == null)
				response = getResponse(model.getResponseStatus(), model.getResponseMessage());
			else
				response = getResponse(Status.OK, updated);
		} else {
			response = getResponse(Status.BAD_REQUEST, INVALID_RQST_MSG);
		}
		
		return response;
	}
	
	/**
	 * return a Response with Boolean or a string message
	 * @param request
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/unhide")
	public Response unhide(RequestBean<Long> request) {
		Response response = null;
		boolean isValid = isValidRequest(request, false);
		
		if (isValid) {	
			MenuModel model = new MenuModel(daoFactory, request.getCredentials());	
			Boolean updated  = model.updateIsHidden(request.getExtra(), false);
			if (updated == null)
				response = getResponse(model.getResponseStatus(), model.getResponseMessage());
			else
				response = getResponse(Status.OK, updated);
		} else {
			response = getResponse(Status.BAD_REQUEST, INVALID_RQST_MSG);
		}
		
		return response;
	}
	
	/**
	 * return a Response with a list of MenuItemBean or a string message
	 * @param request
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/get/all")
	public Response getAllVisible(RequestBean<Object> request) {
		Response response = null;
		boolean isValid = isValidRequest(request, true);
		
		if (isValid) {	
			MenuModel model = new MenuModel(daoFactory, request.getCredentials());	
			List<MenuItemBean> menuIitems  = model.readAllVisible();
			if (menuIitems == null)
				response = getResponse(model.getResponseStatus(), model.getResponseMessage());
			else
				response = getResponse(Status.OK, menuIitems);
		} else {
			response = getResponse(Status.BAD_REQUEST, INVALID_RQST_MSG);
		}
		
		return response;
	}
	
	/**
	 * return a Response with a list of MenuItemBean or a string message
	 * @param request
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/get/all/hidden")
	public Response getAllHidden(RequestBean<Object> request) {
		Response response = null;
		boolean isValid = isValidRequest(request, true);
		
		if (isValid) {	
			MenuModel model = new MenuModel(daoFactory, request.getCredentials());	
			List<MenuItemBean> menuIitems  = model.readAllHidden();
			if (menuIitems == null)
				response = getResponse(model.getResponseStatus(), model.getResponseMessage());
			else
				response = getResponse(Status.OK, menuIitems);
		} else {
			response = getResponse(Status.BAD_REQUEST, INVALID_RQST_MSG);
		}
		
		return response;
	}

	private <T> boolean isValidRequest(RequestBean<T> request, boolean extraCanBeNull) {
		boolean isValid = false;
		
		isValid  = request != null;
		isValid &= isValid? new CredentialsValidator().validate(request.getCredentials()) : false;
		isValid &= isValid && !extraCanBeNull? request.getExtra() != null : true;
		
		return isValid;
	}
	
	private <T> Response getResponse(Response.Status status, T entity) {
		ResponseBuilder responseBuilder = Response.status(status);
		responseBuilder = responseBuilder.entity(entity);
		return responseBuilder.build();
	}
	
	
	private static final String INVALID_RQST_MSG = "Invalid Web Service Request";
}
