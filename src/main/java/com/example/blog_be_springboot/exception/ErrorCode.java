package com.example.blog_be_springboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import java.util.Map;

public enum ErrorCode {
    // 400
    VALIDATION(HttpStatus.BAD_REQUEST,         "BLG-COM-VALIDATION",           "Dữ liệu không hợp lệ."),
    BAD_JSON(HttpStatus.BAD_REQUEST,           "BLG-COM-BAD_JSON",             "JSON không hợp lệ."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST,        "BLG-COM-BAD_REQUEST",          "Yêu cầu không hợp lệ."),

    // 401/403
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,      "BLG-AUTH-UNAUTHORIZED",        "Chưa đăng nhập hoặc token không hợp lệ."),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED,   "BLG-AUTH-BAD_CREDENTIALS",     "Email hoặc mật khẩu không đúng."),
    FORBIDDEN(HttpStatus.FORBIDDEN,            "BLG-AUTH-FORBIDDEN",           "Không có quyền thực hiện thao tác."),
    NOT_OWNER(HttpStatus.FORBIDDEN,            "BLG-POST-NOT_OWNER",           "Bạn không phải tác giả của tài nguyên."),

    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND,            "BLG-COM-NOT_FOUND",            "Không tìm thấy tài nguyên."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,       "BLG-USER-NOT_FOUND",           "Không tìm thấy người dùng."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND,       "BLG-POST-NOT_FOUND",           "Không tìm thấy bài viết."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND,    "BLG-COMM-NOT_FOUND",           "Không tìm thấy bình luận."),

    // 409
    CONFLICT(HttpStatus.CONFLICT,              "BLG-COM-CONFLICT",             "Xung đột dữ liệu."),
    EMAIL_ALREADY_USED(HttpStatus.CONFLICT,    "BLG-USER-EMAIL_CONFLICT",      "Email đã được sử dụng."),
    POST_SLUG_EXISTS(HttpStatus.CONFLICT,      "BLG-POST-SLUG_CONFLICT",       "Slug bài viết đã tồn tại."),
    INVALID_STATE(HttpStatus.CONFLICT,         "BLG-COM-INVALID_STATE",        "Trạng thái hiện tại không cho phép thao tác."),

    // 413/415/405/429
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "BLG-COM-PAYLOAD_TOO_LARGE","Kích thước yêu cầu quá lớn."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "BLG-COM-UNSUPPORTED_MEDIA_TYPE","Kiểu nội dung không hỗ trợ."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "BLG-COM-METHOD_NOT_ALLOWED","Phương thức không hỗ trợ."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "BLG-COM-RATE_LIMITED",         "Vượt quá giới hạn yêu cầu."),

    // 500+
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "BLG-INFRA-INTERNAL",      "Lỗi hệ thống, vui lòng thử lại sau.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.httpStatus = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() { return httpStatus; }
    public String code() { return code; }
    public String message() { return defaultMessage; }

    /** Tạo ProblemDetail chuẩn, có thể ghi đè detail và thêm extra fields (vd errors). */
    public ProblemDetail toProblemDetail(String detail, Map<String, Object> extra) {
        ProblemDetail pd = ProblemDetail.forStatus(httpStatus);
        pd.setTitle(httpStatus.getReasonPhrase());
        pd.setDetail(detail != null ? detail : defaultMessage);
        pd.setProperty("code", code);
        if (extra != null) {
            extra.forEach(pd::setProperty);
        }
        return pd;
    }

    public ProblemDetail toProblemDetail() {
        return toProblemDetail(null, null);
    }
}
