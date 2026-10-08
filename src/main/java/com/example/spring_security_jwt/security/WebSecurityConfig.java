package com.example.spring_security_jwt.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.spring_security_jwt.security.jwt.AuthEntryPointJwt;
import com.example.spring_security_jwt.security.jwt.AuthTokenFilter;
import com.example.spring_security_jwt.security.jwt.JwtUtils;
import com.example.spring_security_jwt.security.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
/**
 * La anotacion anterior activa la seguridad a nivel de metodo (prePostEnabled),
 * de forma que se pueden asegurar directamente los metodos de los controladores
 * con @PreAuthorize
 */
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final UserDetailsServiceImpl userDetailsService;
	private final AuthEntryPointJwt unauthorizeHandle;
	private final JwtUtils jwtUtils;

	@Bean
	AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter(jwtUtils, userDetailsService);
	}

	@Bean
	DaoAuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
		return authConfig.getAuthenticationManager();
	}

	/**
	 * Ademas de las anotaciones @PreAuthorize de los controladores (seguridad a
	 * nivel de metodo), se configura la seguridad a nivel de URL:
	 *
	 * 1- Los endpoints de autenticacion (/api/auth/**) no requieren autenticacion
	 *
	 * 2- Cualquier endpoint de lectura (GET) requiere un usuario autenticado, con
	 * el rol USER o con el rol ADMIN
	 *
	 * 3- Los endpoints de creacion (POST), modificacion (PUT/PATCH) y borrado
	 * (DELETE) solamente los puede utilizar un usuario autenticado con rol ADMIN
	 *
	 * Cualquier otra peticion (request) necesita un usuario autenticado
	 */
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())
				.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizeHandle))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/error").permitAll()
						.requestMatchers(HttpMethod.GET, "/**").hasAnyRole("ADMIN", "USER")
						.requestMatchers(HttpMethod.POST, "/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")
						.anyRequest().authenticated());

		http.authenticationProvider(authenticationProvider());

		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
