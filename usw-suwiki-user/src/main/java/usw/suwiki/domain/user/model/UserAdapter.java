package usw.suwiki.domain.user.model;

import usw.suwiki.domain.user.Role;

public record UserAdapter(
  Long id,
  String loginId,
  Role role
) {
}
