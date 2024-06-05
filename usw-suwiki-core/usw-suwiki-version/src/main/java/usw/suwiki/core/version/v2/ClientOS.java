package usw.suwiki.core.version.v2;

import usw.suwiki.core.exception.VersionException;

import java.util.Objects;

import static usw.suwiki.core.exception.ExceptionCode.COMMON_CLIENT_ERROR;
import static usw.suwiki.core.exception.ExceptionCode.INVALID_CLIENT_OS;

public enum ClientOS {
  ANDROID,
  IOS,
  WEB;

  public static ClientOS from(String stringOs) {
    checkNotNull(stringOs);

    try {
      return ClientOS.valueOf(stringOs.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new VersionException(COMMON_CLIENT_ERROR);
    }
  }

  private static void checkNotNull(String param) {
    if (Objects.isNull(param)) {
      throw new VersionException(INVALID_CLIENT_OS);
    }
  }
}
