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
 * Test unitarios de la capa de repositorio (repositorios JPA) de los roles,
 * utilizados para asignar los roles ROLE_USER y ROLE_ADMIN a los usuarios
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:repositorio_roles;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop" })
class RoleRepositoryTest {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("Test de repositorio para persistir un rol")
	void testSaveRole() {

		// given
		Role rol = Role.builder().name(ERole.ROLE_USER).build();

		// when
		Role rolGuardado = roleRepository.save(rol);

		// then
		assertThat(rolGuardado).isNotNull();
		assertThat(rolGuardado.getId()).isGreaterThan(0);
		assertThat(rolGuardado.getName()).isEqualTo(ERole.ROLE_USER);
	}

	@Test
	@DisplayName("Test de repositorio para recuperar un rol por su nombre")
	void testFindByName() {

		// given
		roleRepository.save(Role.builder().name(ERole.ROLE_ADMIN).build());

		// when
		Optional<Role> rolEncontrado = roleRepository.findByName(ERole.ROLE_ADMIN);

		// then
		assertThat(rolEncontrado).isPresent();
		assertThat(rolEncontrado.get().getName()).isEqualTo(ERole.ROLE_ADMIN);
	}

	@Test
	@DisplayName("Test de repositorio para recuperar un rol que no existe")
	void testFindByNameNoExistente() {

		// when
		Optional<Role> rolEncontrado = roleRepository.findByName(ERole.ROLE_USER);

		// then
		assertThat(rolEncontrado).isEmpty();
	}

	@Test
	@DisplayName("Test de repositorio para comprobar si un rol ya existe")
	void testExistsByName() {

		// given
		roleRepository.save(Role.builder().name(ERole.ROLE_USER).build());

		// when & then
		assertThat(roleRepository.existsByName(ERole.ROLE_USER)).isTrue();
		assertThat(roleRepository.existsByName(ERole.ROLE_ADMIN)).isFalse();
	}

	@Test
	@DisplayName("Test de repositorio para recuperar los roles de un usuario")
	void testRolesDelUsuario() {

		// given
		Role rolAdmin = roleRepository.save(Role.builder().name(ERole.ROLE_ADMIN).build());

		userRepository.save(User.builder()
				.username("adminConRol")
				.email("adminConRol@test.com")
				.password("$2a$10$abc123")
				.roles(Set.of(rolAdmin))
				.build());

		// when
		Optional<Role> rolEncontrado = roleRepository.findByName(ERole.ROLE_ADMIN);

		// then
		assertThat(rolEncontrado).isPresent();
		assertThat(roleRepository.count()).isEqualTo(1);
	}
}
