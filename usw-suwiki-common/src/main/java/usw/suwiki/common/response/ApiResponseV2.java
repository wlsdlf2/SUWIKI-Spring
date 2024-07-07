package usw.suwiki.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@JsonInclude(Include.NON_NULL)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponseV2<T> {
    private final T data;

    public static <T> ApiResponseV2<T> ok(T data) {
        return new ApiResponseV2<>(data);
    }

    public static ApiResponseV2<?> success() { return new ApiResponseV2<>(Map.of("success", true)); }
}

