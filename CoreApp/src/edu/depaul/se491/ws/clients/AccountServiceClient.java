package edu.depaul.se491.ws.clients;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.depaul.se491.beans.AccountBean;
import edu.depaul.se491.beans.CredentialsBean;
import edu.depaul.se491.beans.RequestBean;

public class AccountServiceClient extends BaseWebServiceClient {
	private final String serviceBaseUrl;
	private final Client client;
	private final CredentialsBean credentials;
	
	public AccountServiceClient(CredentialsBean credentials, String serviceBaseUrl) {
		this.client = ClientBuilder.newClient();
		this.credentials = credentials;
		this.serviceBaseUrl = serviceBaseUrl;
	}
	
	public AccountBean get(final String username) {
		final String getTarget = serviceBaseUrl + "/get";
		Invocation.Builder invocationBuilder = getJsonInvocationBuilder(client, getTarget);
		
		RequestBean<String> request = super.<String>getRequestBean(credentials, username);
		
		Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		
		AccountBean responseBean = null;
		
		if (response.getStatus() != Status.OK.getStatusCode()) {
			setResponseMessage(response.readEntity(String.class));
		} else {
			responseBean =  response.readEntity(AccountBean.class);
		}
		
		response.close();
		
		return responseBean;
	}
	
	public AccountBean post(final AccountBean newAccount) {
		final String postTarget = serviceBaseUrl + "/post";
		Invocation.Builder invocationBuilder = getJsonInvocationBuilder(client, postTarget);
		
		RequestBean<AccountBean> request = super.<AccountBean>getRequestBean(credentials, newAccount);
		
		Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		
		AccountBean responseBean = null;
		
		if (response.getStatus() != Status.OK.getStatusCode()) {
			setResponseMessage(response.readEntity(String.class));
		} else {
			responseBean =  response.readEntity(AccountBean.class);
		}
		
		
		response.close();
		
		return responseBean;
	}
	
	public Boolean update(final AccountBean updatedAccount) {
		final String updateTarget = serviceBaseUrl + "/update";
		Invocation.Builder invocationBuilder = getJsonInvocationBuilder(client, updateTarget);
		
		RequestBean<AccountBean> request = super.<AccountBean>getRequestBean(credentials, updatedAccount);
		
		Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		
		Boolean updated = null;
		
		if (response.getStatus() != Status.OK.getStatusCode()) {
			setResponseMessage(response.readEntity(String.class));
		} else {
			updated =  response.readEntity(Boolean.class);
		}
		
		response.close();
		
		return updated;
	}
	
	public Boolean delete(final String username) {
		final String deleteTarget = serviceBaseUrl + "/delete";
		Invocation.Builder invocationBuilder = getJsonInvocationBuilder(client, deleteTarget);
		
		RequestBean<String> request = super.<String>getRequestBean(credentials, username);
		
		Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		
		Boolean deleted = null;
		
		if (response.getStatus() != Status.OK.getStatusCode()) {
			setResponseMessage(response.readEntity(String.class));
		} else {
			deleted =  response.readEntity(Boolean.class);
		}
		response.close();
		
		return deleted;
	}
	
	
	public AccountBean[] getAll() {
		final String getAllTarget = serviceBaseUrl + "/get/all";
		Invocation.Builder invocationBuilder = getJsonInvocationBuilder(client, getAllTarget);
		
		RequestBean<Object> request = super.<Object>getRequestBean(credentials, null);
		
		Response response = invocationBuilder.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		
		AccountBean[] responseBeans = null;
		
		if (response.getStatus() != Status.OK.getStatusCode()) {
			setResponseMessage(response.readEntity(String.class));
		} else {
			responseBeans =  response.readEntity(AccountBean[].class);
		}
		
		response.close();
		
		return responseBeans;
	}
}
