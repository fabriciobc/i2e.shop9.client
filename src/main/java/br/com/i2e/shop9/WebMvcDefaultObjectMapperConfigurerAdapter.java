package br.com.i2e.shop9;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebMvcDefaultObjectMapperConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    public WebMvcDefaultObjectMapperConfigurerAdapter(ObjectMapper mapper) {
        // default mapper configured with spring.*
        System.out.println( "MApper Config"  + mapper.getDeserializationConfig().getAttributes().getAttribute( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES ) );
    	mapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
    	mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
//    	mapper.configure( DeserializationFeature.FAIL_, false );
        
    }

//    @Override
//    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
//        for (HttpMessageConverter<?> httpConverter : converters) {
//            if (httpConverter instanceof MappingJackson2HttpMessageConverter) {
//                // register the configured object mapper to HttpMessageconter
//                ((MappingJackson2HttpMessageConverter) httpConverter).setObjectMapper(mapper);
//            }
//        }
//    }
}