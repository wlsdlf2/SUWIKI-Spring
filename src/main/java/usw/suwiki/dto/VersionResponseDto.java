package usw.suwiki.dto;

import lombok.Getter;

@Getter
public class VersionResponseDto {
    private float version;

    public VersionResponseDto(float version) {
        this.version = version;
    }
}
