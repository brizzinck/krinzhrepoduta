package ua.duikt.lavrishko.rental.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** JSON: число чи логічне значення там, де очікується рядок, і число там, де очікується дата, дають помилку, а не перетворюються мовчки. */
@Configuration
public class JsonStrictConfig {
    @Bean
    Jackson2ObjectMapperBuilderCustomizer strictScalars() {
        return builder -> builder.postConfigurer(mapper -> {
            var text = mapper.coercionConfigFor(LogicalType.Textual);
            text.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
            text.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
            text.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
            // число замість дати ("birthDate": 10000) не вважається кількістю днів від 1970 року
            mapper.configOverride(java.time.LocalDate.class).setFormat(JsonFormat.Value.forLeniency(false));
        });
    }
}
