package com.example.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.exception.EmailAlreadyInUseException;
import com.example.exception.UsernameAlreadyTakenException;
import com.example.spring_security_jwt.model.ERole;
import com.example.spring_security_jwt.model.Role;
import com.example.spring_security_jwt.model.User;
import com.example.spring_security_jwt.payload.request.LogginRequest;
import com.example.spring_security_jwt.payload.request.SignupRequest;
import com.example.spring_security_jwt.payload.response.JwtResponse;
import com.example.spring_security_jwt.payload.response.MessageResponse;
import com.example.spring_security_jwt.repository.RoleRepository;
import com.example.spring_security_jwt.repository.UserRepository;
import com.example.spring_security_jwt.security.jwt.JwtUtils;
import com.example.spring_security_jwt.security.service.UserDetailsImpl;

/**
 * Test unitarios de la capa de servicio AuthService, que se encarga del
 * registro (signup) y del login (signin) de los usuarios, este ultimo
 * suministrando el email y no el username
 *
 * Las dependencias se simulan (mock) con Mockito, de forma que el test se
 * ejecuta de forma aislada, sin necesidad de base de datos ni de peticiones HTTP
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PasswordEncoder encoder;

	@Mock
	private JwtUtils jwtUtils;

	@InjectMocks
	private AuthServiceImpl authService;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private SignupRequest crearSignupRequest() {
		return SignupRequest.builder()
				.username("nuevoUsuario")
				.email("nuevoUsuario@test.com")
				.password("Temp2026##")
				.build();
	}

	@Test
	@DisplayName("Test del servicio para registrar un usuario correctamente")
	void testRegister() {

		// given
		Role rolUser = Role.builder().name(ERole.ROLE_USER).build();

		given(userRepository.existsByUsername("nuevoUsuario")).willReturn(false);
		given(userRepository.existsByEmail("nuevoUsuario@test.com")).willReturn(false);
		given(roleRepository.findByName(ERole.ROLE_USER)).willReturn(Optional.of(rolUser));
		given(encoder.encode("Temp2026##")).willReturn("$2a$10$hash");
		given(userRepository.save(any(User.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		// when
		MessageResponse mensaje = authService.register(crearSignupRequest());

		// then
		assertThat(mensaje.getMessage()).isEqualTo("User registered successfully");

		ArgumentCaptor<User> usuarioCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(usuarioCaptor.capture());

		User usuarioGuardado = usuarioCaptor.getValue();
		assertThat(usuarioGuardado.getUsername()).isEqualTo("nuevoUsuario");
		assertThat(usuarioGuardado.getEmail()).isEqualTo("nuevoUsuario@test.com");
		assertThat(usuarioGuardado.getPassword()).isEqualTo("$2a$10$hash");
		assertThat(usuarioGuardado.getRoles()).hasSize(1);
	}

	@Test
	@DisplayName("Test del servicio para registrar un usuario con rol ADMIN")
	void testRegisterConRolAdmin() {

		// given
		Role rolAdmin = Role.builder().name(ERole.ROLE_ADMIN).build();

		given(userRepository.existsByUsername(any())).willReturn(false);
		given(userRepository.existsByEmail(any())).willReturn(false);
		given(roleRepository.findByName(ERole.ROLE_ADMIN)).willReturn(Optional.of(rolAdmin));
		given(encoder.encode(any())).willReturn("$2a$10$hash");
		given(userRepository.save(any(User.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		SignupRequest signupRequest = SignupRequest.builder()
				.username("adminNuevo")
				.email("adminNuevo@test.com")
				.password("Temp2026##")
				.role(Set.of("admin"))
				.build();

		// when
		authService.register(signupRequest);

		// then
		ArgumentCaptor<User> usuarioCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(usuarioCaptor.capture());

		assertThat(usuarioCaptor.getValue().getRoles())
				.extracting(Role::getName)
				.containsExactly(ERole.ROLE_ADMIN);
	}

	@Test
	@DisplayName("Test del servicio para registrar un username que ya existe")
	void testRegisterUsernameDuplicado() {

		// given
		given(userRepository.existsByUsername("nuevoUsuario")).willReturn(true);

		// when & then
		assertThatThrownBy(() -> authService.register(crearSignupRequest()))
				.isInstanceOf(UsernameAlreadyTakenException.class)
				.hasMessageContaining("Username is already taken");

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("Test del servicio para registrar un email que ya existe")
	void testRegisterEmailDuplicado() {

		// given
		given(userRepository.existsByUsername("nuevoUsuario")).willReturn(false);
		given(userRepository.existsByEmail("nuevoUsuario@test.com")).willReturn(true);

		// when & then
		assertThatThrownBy(() -> authService.register(crearSignupRequest()))
				.isInstanceOf(EmailAlreadyInUseException.class)
				.hasMessageContaining("Email is already in use");

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("Test del servicio para loguearse con el email y generar el token JWT")
	void testLogin() {

		// given
		UserDetailsImpl userDetails = UserDetailsImpl.builder()
				.id(1L)
				.username("user1")
				.email("user1@gmail.com")
				.password("$2a$10$hash")
				.authorities(Set.of(new SimpleGrantedAuthority("ROLE_USER")))
				.build();

		Authentication authentication = new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());

		given(authenticationManager.authenticate(any())).willReturn(authentication);
		given(jwtUtils.generateJwtToken(authentication)).willReturn("jwt-token");

		// when
		JwtResponse jwtResponse = authService.login(
				LogginRequest.builder().email("user1@gmail.com").password("Temp2026$$##").build());

		// then
		assertThat(jwtResponse.getToken()).isEqualTo("jwt-token");
		assertThat(jwtResponse.getEmail()).isEqualTo("user1@gmail.com");
		assertThat(jwtResponse.getUsername()).isEqualTo("user1");
		assertThat(jwtResponse.getRoles()).containsExactly("ROLE_USER");
	}

	@Test
	@DisplayName("Test del servicio para un login con credenciales incorrectas")
	void testLoginCredencialesIncorrectas() {

		// given
		given(authenticationManager.authenticate(any()))
				.willThrow(new BadCredentialsException("Bad credentials"));

		// when & then
		assertThatThrownBy(() -> authService.login(
				LogginRequest.builder().email("user1@gmail.com").password("mal").build()))
				.isInstanceOf(BadCredentialsException.class);

		verify(jwtUtils, never()).generateJwtToken(any());
	}
}
