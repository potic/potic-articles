package me.potic.articles.config

import com.auth0.spring.security.api.JwtWebSecurityConfigurer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
@Configuration
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value(value = '${auth0.apiAudience}')
    String apiAudience

    @Value(value = '${auth0.issuer}')
    String issuer

    @Override
    protected void configure(HttpSecurity http) {
        JwtWebSecurityConfigurer
                .forRS256(apiAudience, issuer)
                .configure(http)
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, '/article/**').hasAuthority('get:articles')
                .anyRequest().authenticated()
                .and().cors()
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration()
        configuration.setAllowedOrigins( [ '*' ] )
        configuration.setAllowedMethods( [ 'HEAD', 'GET', 'POST', 'PUT', 'DELETE', 'PATCH' ] )
        configuration.setAllowCredentials(true)
        configuration.setAllowedHeaders( [ 'Authorization', 'Cache-Control', 'Content-Type' ])
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration('/article/**', configuration)

        source
    }
}
