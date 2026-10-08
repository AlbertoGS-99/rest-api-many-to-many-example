package com.example;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.entities.Tag;
import com.example.entities.Tutorial;
import com.example.services.TagService;
import com.example.services.TutorialService;
import com.example.spring_security_jwt.model.ERole;
import com.example.spring_security_jwt.model.Role;
import com.example.spring_security_jwt.model.User;
import com.example.spring_security_jwt.repository.RoleRepository;
import com.example.spring_security_jwt.repository.UserRepository;

/**
 * Componente que crea datos de ejemplo al arrancar la aplicacion: los roles
 * (USER y ADMIN), dos usuarios (uno con rol ADMIN y otro con rol USER), asi
 * como varios tutorials y tags
 *
 * Los usuarios creados permiten probar la seguridad implementada con Spring
 * Security y JWT, tanto desde un cliente HTTP como desde los test de
 * integracion de la capa de controladores
 */
@Configuration
public class CreatesSamplesData {

	@Bean
	CommandLineRunner samplesData(TutorialService tutorialService,
			TagService tagService,
			RoleRepository roleRepository,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder) {

		return args -> {

			// Se crean los roles que va a utilizar la aplicacion
			Role rolUser = roleRepository.save(Role.builder().name(ERole.ROLE_USER).build());
			Role rolAdmin = roleRepository.save(Role.builder().name(ERole.ROLE_ADMIN).build());

			// Usuario con rol ADMIN, utilizado para crear, modificar y borrar
			userRepository.save(User.builder()
					.username("admin1")
					.email("admin1@gmail.com")
					.password(passwordEncoder.encode("Temp2026$$##"))
					.roles(Set.of(rolAdmin))
					.build());

			// Usuario con rol USER, solamente puede leer (GET)
			userRepository.save(User.builder()
					.username("user1")
					.email("user1@gmail.com")
					.password(passwordEncoder.encode("Temp2026$$##"))
					.roles(Set.of(rolUser))
					.build());

			// Tutorials de ejemplo
			Tutorial tutorial1 = tutorialService.create(Tutorial.builder()
					.title("Spring Boot")
					.description("Crear APIs REST con Spring Boot")
					.build());

			Tutorial tutorial2 = tutorialService.create(Tutorial.builder()
					.title("Spring Security")
					.description("Seguridad con Spring Security y JWT")
					.build());

			// Tags de ejemplo asociados a los tutorials anteriores
			Tag tag1 = tagService.addToTutorial(tutorial1.getId(), Tag.builder()
					.name("java")
					.build());

			tagService.addToTutorial(tutorial2.getId(), Tag.builder()
					.name("seguridad")
					.build());

			tagService.addToTutorial(tutorial2.getId(), tag1);
		};
	}
}
