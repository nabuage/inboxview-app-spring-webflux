package org.inboxview.app.utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DateUtil {
    public static OffsetDateTime getCurrentDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
