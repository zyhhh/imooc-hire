package cn.stan.common.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@PropertySource("classpath:excludeUrl.properties")
@ConfigurationProperties(prefix = "exclude")
public class ExcludeUrlProperties {

    private List<String> url;
}
