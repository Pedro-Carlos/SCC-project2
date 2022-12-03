package srv.impl;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import srv.api.service.rest.AuctionResource;
import srv.api.service.rest.ControlResource;
import srv.api.service.rest.MediaResource;
import srv.api.service.rest.UserResource;
import srv.api.service.rest.authentication.AuthenticationResource;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(UserResource.class);
		resources.add(AuctionResource.class);
		resources.add(AuthenticationResource.class);
		singletons.add( new MediaResource());
		singletons.add( new UserResource());
		singletons.add( new AuctionResource());
		singletons.add( new AuthenticationResource());
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
