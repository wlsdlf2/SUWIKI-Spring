package usw.suwiki.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonResponse<T> {
    private final T data;

    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>(data);
    }

    public static CommonResponse<?> success() { return new CommonResponse<>(Map.of("success", true)); }
}

