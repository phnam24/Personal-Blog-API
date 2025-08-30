package com.example.blog_be_springboot.helper;

import com.example.blog_be_springboot.exception.AppException;
import com.example.blog_be_springboot.exception.ErrorCode;
import lombok.NoArgsConstructor;

import java.time.*;

@NoArgsConstructor
public final class DateParsers {
    public static Instant parseStartInstant(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            if (s.length() == 10) { // yyyy-MM-dd
                return LocalDate.parse(s).atStartOfDay(ZoneOffset.UTC).toInstant();
            }
            return OffsetDateTime.parse(s).toInstant(); // expect ISO-8601
        } catch (DateTimeException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid datetime: " + s);
        }
    }

    public static Instant parseEndInstant(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            if (s.length() == 10) {
                return LocalDate.parse(s).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusMillis(1);
            }
            return OffsetDateTime.parse(s).toInstant();
        } catch (DateTimeException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid datetime: " + s);
        }
    }
}