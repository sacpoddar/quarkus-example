package org.sac;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jboss.resteasy.reactive.RestResponse;

class ExceptionMappers {

    @ServerExceptionMapper
    public RestResponse<String> mapBadRequestException(BadRequestException ex) {
        return RestResponse.status(Response.Status.BAD_REQUEST, ex.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<String> mapNotFoundException(NotFoundException ex) {
        return RestResponse.status(Response.Status.NOT_FOUND, ex.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<String> mapConstraintViolationException(ConstraintViolationException ex) {
        //ResteasyReactiveViolationException
        System.out.println("ConstraintViolationException");
        return RestResponse.status(Response.Status.BAD_REQUEST, ex.getMessage());
    }
}