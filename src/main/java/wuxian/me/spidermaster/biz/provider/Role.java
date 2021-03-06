package wuxian.me.spidermaster.biz.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wuxian on 20/6/2017.
 * <p>
 * 用于注解@Spider
 * 目前设计了两种角色
 * 1 通常的spider spider
 * 2 provider 比如说提供proxy能力的角色
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Role {
    String role() default Roles.ROLE_SPIDER;
}
