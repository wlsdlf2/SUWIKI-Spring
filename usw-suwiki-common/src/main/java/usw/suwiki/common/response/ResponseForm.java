package usw.suwiki.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseForm {

  private Object data;
  private Integer statusCode;
  private String message;

  public ResponseForm(Object data) {
    this.data = data;
  }

  public static ResponseForm success(Object data) {
    return new ResponseForm(data, 200, "Success");
  }
}
