package usw.suwiki.domain.user.model;

import usw.suwiki.core.secure.model.Claim;

// todo(24.05.22): 잘하면 없앨 수 있을듯?
public record UserClaim(
  String loginId,
  String role,
  boolean restricted
) implements Claim {
}
