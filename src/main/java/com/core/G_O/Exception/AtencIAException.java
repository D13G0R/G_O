package com.core.G_O.Exception;

import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;

public class AtencIAException extends RuntimeException{

    private final HttpStatus status;

    public AtencIAException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus (){
        return this.status;
    }


}
