package usw.suwiki.core.version.v2;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import usw.suwiki.core.exception.VersionException;
import usw.suwiki.infra.jpa.BaseEntity;

import static usw.suwiki.core.exception.ExceptionCode.SERVER_ERROR;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "client_app_version_id"))
@Table(uniqueConstraints = @UniqueConstraint(name = "UNIQUE_OS_AND_VERSION_CODE", columnNames = {"os", "version_code"}))
public class ClientAppVersion extends BaseEntity {
  @Enumerated(EnumType.STRING)
  private ClientOS os;

  @Column(name = "version_code")
  private Integer versionCode;

  @Column(length = 2000)
  private String description;

  private boolean isVital;

  public boolean isUpdateMandatory(ClientOS os, Integer versionCode) {
    if (this.os != os) {
      throw new VersionException(SERVER_ERROR);
    }

    return this.isVital && this.versionCode > versionCode;
  }
}
