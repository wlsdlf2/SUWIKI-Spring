package usw.suwiki.statistics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Profile;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Profile({"prod", "dev", "local"})
public @interface CacheStatics {
}
