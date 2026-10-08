package com.example.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.spring_security_jwt.model.ERole;
import com.example.spring_security_jwt.model.Role;
import com.example.spring_security_jwt.model.User;
import com.example.spring_security_jwt.repository.UserRepository;
import com.example.spring_security_jwt.security.service.UserDetailsServiceImpl;

/**
 * Test unitarios de la capa de servicio UserDetailsServiceImpl, que es el que
 * se encarga de recuperar al usuario que se quiere autenticar, suministrando su
 * email (el login se realiza con el email y no con el username)
 *
 * La dependencia del repositorio se simula (mock) con Mockito, de forma que el
 * test se ejecuta de forma aislada, sin necesidad de base de datos
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserDetailsServiceImpl userDetailsServiceImpl;

	private User crearUsuario() {

		Role rolAdmin = Role.builder().name(ERole.ROLE_ADMIN).build();

		return User.builder()
				.id(1L)
				.username("admin1")
				.email("admin1@gmail.com")
				.password("Temp2026$$##")
				.roles(Set.of(rolAdmin))
				.build();
	}

	@Test
	@DisplayName("Test de servicio para recuperar al usuario a partir de su email")
	void testLoadUserByUsernameConEmail() {

		// given
		given(userRepository.findByEmail("admin1@gmail.com"))
				.willReturn(Optional.of(crearUsuario()));

		// when
		UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername("admin1@gmail.com");

		// then
		assertThat(userDetails).isNotNull();
		assertThat(userDetails.getUsername()).isEqualTo("admin1");
		assertThat(userDetails.getPassword()).isEqualTo("Temp2026$$##");

		verify(userRepository).findByEmail("admin1@gmail.com");
	}

	@Test
	@DisplayName("Test de servicio para comprobar que el usuario recuperado tiene sus roles")
	void testLoadUserByUsernameRecuperaLosRoles() {

		// given
		given(userRepository.findByEmail("admin1@gmail.com"))
				.willReturn(Optional.of(crearUsuario()));

		// when
		UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername("admin1@gmail.com");

		// then
		assertThat(userDetails.getAuthorities())
				.extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_ADMIN");
	}

	@Test
	@DisplayName("Test de servicio para un email que no esta registrado")
	void testLoadUserByUsernameConEmailNoRegistrado() {

		// given
		given(userRepository.findByEmail("noExiste@gmail.com"))
				.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userDetailsServiceImpl.loadUserByUsername("noExiste@gmail.com"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("noExiste@gmail.com");
	}
}
