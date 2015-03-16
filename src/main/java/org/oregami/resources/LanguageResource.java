package org.oregami.resources;

import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.apache.log4j.Logger;
import org.oregami.data.RevisionInfo;
import org.oregami.dropwizard.User;
import org.oregami.entities.Language;
import org.oregami.entities.LanguageDao;
import org.oregami.service.LanguageService;
import org.oregami.service.ServiceCallContext;
import org.oregami.service.ServiceResult;

import javax.persistence.OptimisticLockException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;


@Path("/language")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LanguageResource {

	@Inject
	private LanguageDao languageDao = null;

	@Inject
	private LanguageService languageService = null;

	public LanguageResource() {
	}


	@Path("{id}")
	@DELETE
	public Response delete(
            @Auth User user,
            @PathParam("id") String id) {
		Language l = languageDao.findOne(id);
		if (l==null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		try {
            languageDao.delete(l);
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
		return Response.ok().build();
	}

	@GET
	public List<Language> list() {
		List<Language> ret = languageDao.findAll();
		return ret;
	}

    @GET
    @Path("/{id}")
	public Response getLanguage(@PathParam("id") String id) {
    	Language language = languageDao.findOne(id);
    	if (language!=null) {
    		return Response.ok(language).build();
    	} else {
    		return Response.status(Status.NOT_FOUND).build();
    	}
	}

    @GET
    @Path("/{id}/revisions")
    public Response getLanguageRevisions(@PathParam("id") String id) {
        List<RevisionInfo> revisionList = languageDao.findRevisions(id);
        if (revisionList!=null) {
            return Response.ok(revisionList).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{id}/revisions/{revision}")
    public Response getLanguageRevision(@PathParam("id") String id, @PathParam("revision") String revision) {
        Language t = languageDao.findRevision(id, Integer.parseInt(revision));
        if (t!=null) {
            return Response.ok(t).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

	@POST
	public Response create(
            @Auth User user,
            Language t) {
		try {
            ServiceCallContext context = new ServiceCallContext(user);
            ServiceResult<Language> serviceResult = languageService.createNewEntity(t, context);
			if (serviceResult.hasErrors()) {
				return Response.status(Status.BAD_REQUEST)
						.type("text/json")
		                .entity(serviceResult.getErrors()).build();
			}
			return Response.ok().build();
		} catch (Exception e) {
			return Response.status(Status.CONFLICT).type("text/plain")
	                .entity(e.getMessage()).build();
		}

	}


	@PUT
	@Path("{id}")
	public Response update(
            @Auth User user,
            @PathParam("id") String id, Language t) {
		if (t.getId()==null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		try {
            ServiceCallContext context = new ServiceCallContext(user);
            ServiceResult<Language> serviceResult = languageService.updateEntity(t, context);
			if (serviceResult.hasErrors()) {
				return Response.status(Status.BAD_REQUEST)
						.type("text/json")
		                .entity(serviceResult.getErrors()).build();
			}
		} catch (OptimisticLockException e) {
			Logger.getLogger(this.getClass()).warn("OptimisticLockException", e);
			return Response.status(Status.BAD_REQUEST).tag("OptimisticLockException").build();
		}
		return Response.status(Status.ACCEPTED).entity(t).build();
	}

}
