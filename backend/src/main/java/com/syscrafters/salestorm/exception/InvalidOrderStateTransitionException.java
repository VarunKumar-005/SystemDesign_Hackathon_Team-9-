package com.syscrafters.salestorm.exception;

import org.springframework.http.HttpStatus;

public class InvalidOrderStateTransitionException extends BusinessException {
    public InvalidOrderStateTransitionException(String fromStatus, String toStatus) {
        super("Invalid order state transition from '" + fromStatus + "' to '" + toStatus + "'. Transition is rejected.", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
