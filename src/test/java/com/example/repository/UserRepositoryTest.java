package com.example.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.TestPropertySource;

import com.example.spring_security_jwt.model.ERole;
import com.example.spring_security_jwt.model.Role;
import com.example.spring_security_jwt.model.User;
import com.example.spring_security_jwt.repository.RoleRepository;
import com.example.spring_security_jwt.repository.UserRepository;

/**
 * Test unitarios de la capa de repositorio (repositorios JPA) de los usuarios
 * y los roles, utilizados por la autenticacion con Spring Security y JWT
 *
 * La anotacion @DataJpaTest carga unicamente la capa de persistencia (entidades
 * y repositorios), no todo el contexto de Spring, y cada test se ejecuta dentro
 * de una transaccion que se deshace (rollback) al terminar
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:repositorio_usuarios;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop" })
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Test
	@DisplayName("Test de repositorio para persistir un usuario con su rol")
	void testSaveUser() {

		// given
		Role rolAdmin = roleRepository.save(Role.builder().name(ERole.ROLE_ADMIN).build());

		User usuario = User.builder()
				.username("adminNuevo")
				.email("adminNuevo@test.com")
				.password("$2a$10$abc123")
				.roles(Set.of(rolAdmin))
				.build();

		// when
		User usuarioGuardado = userRepository.save(usuario);

		// then
		assertThat(usuarioGuardado).isNotNull();
		assertThat(usuarioGuardado.getId()).isGreaterThan(0);
		assertThat(usuarioGuardado.getEmail()).isEqualTo("adminNuevo@test.com");
		assertThat(usuarioGuardado.getRoles()).hasSize(1);
	}

	@Test
	@DisplayName("Test de repositorio para recuperar un usuario por su email")
	void testFindByEmail() {

		// given
		userRepository.save(User.builder()
				.username("usuarioPorEmail")
				.email("usuarioPorEmail@test.com")
				.password("$2a$10$abc123")
				.build());

		// when
		Optional<User> usuarioEncontrado = userRepository.findByEmail("usuarioPorEmail@test.com");

		// then
		assertThat(usuarioEncontrado).isPresent();
		assertThat(usuarioEncontrado.get().getUsername()).isEqualTo("usuarioPorEmail");
	}

	@Test
	@DisplayName("Test de repositorio para recuperar un email que no existe")
	void testFindByEmailNoExistente() {

		// when
		Optional<User> usuarioEncontrado = userRepository.findByEmail("noExiste@test.com");

		// then
		assertThat(usuarioEncontrado).isEmpty();
	}

	@Test
	@DisplayName("Test de repositorio para recuperar un usuario por su username")
	void testFindByUsername() {

		// given
		userRepository.save(User.builder()
				.username("usuarioPorUsername")
				.email("usuarioPorUsername@test.com")
				.password("$2a$10$abc123")
				.build());

		// when
		Optional<User> usuarioEncontrado = userRepository.findByUsername("usuarioPorUsername");

		// then
		assertThat(usuarioEncontrado).isPresent();
		assertThat(usuarioEncontrado.get().getEmail()).isEqualTo("usuarioPorUsername@test.com");
	}

	@Test
	@DisplayName("Test de repositorio para comprobar si un email ya esta en uso")
	void testExistsByEmail() {

		// given
		userRepository.save(User.builder()
				.username("usuarioEmailEnUso")
				.email("emailEnUso@test.com")
				.password("$2a$10$abc123")
				.build());

		// when & then
		assertThat(userRepository.existsByEmail("emailEnUso@test.com")).isTrue();
		assertThat(userRepository.existsByEmail("emailLibre@test.com")).isFalse();
	}

	@Test
	@DisplayName("Test de repositorio para comprobar si un username ya esta en uso")
	void testExistsByUsername() {

		// given
		userRepository.save(User.builder()
				.username("usernameEnUso")
				.email("usernameEnUso@test.com")
				.password("$2a$10$abc123")
				.build());

		// when & then
		assertThat(userRepository.existsByUsername("usernameEnUso")).isTrue();
		assertThat(userRepository.existsByUsername("usernameLibre")).isFalse();
	}

	@Test
	@DisplayName("Test de repositorio para contar los usuarios registrados")
	void testCountUsers() {

		// given
		userRepository.saveAll(Set.of(
				User.builder().username("contador1").email("contador1@test.com")
						.password("$2a$10$abc123").build(),
				User.builder().username("contador2").email("contador2@test.com")
						.password("$2a$10$abc123").build()));

		// when
		long totalUsuarios = userRepository.count();

		// then
		assertThat(totalUsuarios).isEqualTo(2);
	}
}
