package org.inboxview.app.error;

import java.time.LocalDateTime;

public record ErrorResponse(
    String id,
    LocalDateTime timestamp,
    String error,
    int statusCode
) {
    public ErrorResponse(String id, String error, int statusCode) {
        this(id, LocalDateTime.now(), error, statusCode);
    }    
}
