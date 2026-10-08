package com.example.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.TestPropertySource;

import com.example.entities.Tag;
import com.example.entities.Tutorial;

/**
 * Test unitarios de la capa de repositorio (repositorios JPA) de los tutorials
 *
 * La anotacion @DataJpaTest carga unicamente la capa de persistencia (entidades
 * y repositorios), no todo el contexto de Spring, y cada test se ejecuta dentro
 * de una transaccion que se deshace (rollback) al terminar
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:repositorio_tutorials;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop" })
class TutorialRepositoryTest {

	@Autowired
	private TutorialRepository tutorialRepository;

	@Autowired
	private TagRepository tagRepository;

	@Test
	@DisplayName("Test de repositorio para persistir un tutorial")
	void testSaveTutorial() {

		// given
		Tutorial tutorial = Tutorial.builder()
				.title("Spring Boot")
				.description("APIs REST")
				.published(true)
				.build();

		// when
		Tutorial tutorialGuardado = tutorialRepository.save(tutorial);

		// then
		assertThat(tutorialGuardado).isNotNull();
		assertThat(tutorialGuardado.getId()).isGreaterThan(0);
		assertThat(tutorialGuardado.getTitle()).isEqualTo("Spring Boot");
		assertThat(tutorialGuardado.isPublished()).isTrue();
	}

	@Test
	@DisplayName("Test de repositorio para recuperar todos los tutorials")
	void testFindAllTutorials() {

		// given
		tutorialRepository.save(Tutorial.builder().title("Uno").description("d").build());
		tutorialRepository.save(Tutorial.builder().title("Dos").description("d").build());

		// when
		List<Tutorial> tutorials = tutorialRepository.findAll();

		// then
		assertThat(tutorials).hasSize(2);
	}

	@Test
	@DisplayName("Test de repositorio para recuperar tutorials por parte del titulo")
	void testFindByTitleContaining() {

		// given
		tutorialRepository.save(Tutorial.builder().title("Spring Security").description("d").build());
		tutorialRepository.save(Tutorial.builder().title("Angular").description("d").build());

		// when
		List<Tutorial> tutorials = tutorialRepository.findByTitleContaining("Spring");

		// then
		assertThat(tutorials).hasSize(1);
		assertThat(tutorials.get(0).getTitle()).isEqualTo("Spring Security");
	}

	@Test
	@DisplayName("Test de repositorio para recuperar los tutorials publicados")
	void testFindByPublished() {

		// given
		tutorialRepository.save(Tutorial.builder().title("Publicado").description("d").published(true).build());
		tutorialRepository.save(Tutorial.builder().title("Borrador").description("d").published(false).build());

		// when
		List<Tutorial> publicados = tutorialRepository.findByPublished(true);
		List<Tutorial> borradores = tutorialRepository.findByPublished(false);

		// then
		assertThat(publicados).hasSize(1);
		assertThat(publicados.get(0).getTitle()).isEqualTo("Publicado");
		assertThat(borradores).hasSize(1);
	}

	@Test
	@DisplayName("Test de repositorio para recuperar los tutorials de un tag")
	void testFindTutorialsByTagsId() {

		// given
		Tutorial tutorial = Tutorial.builder().title("Tutorial etiquetado").description("d").build();

		Tag tag = tagRepository.save(Tag.builder().name("java").build());
		tutorial.addTag(tag);
		tutorialRepository.save(tutorial);

		// when
		List<Tutorial> tutorials = tutorialRepository.findTutorialsByTagsId(tag.getId());

		// then
		assertThat(tutorials).hasSize(1);
		assertThat(tutorials.get(0).getTitle()).isEqualTo("Tutorial etiquetado");
	}

	@Test
	@DisplayName("Test de repositorio para recuperar un tutorial por su id")
	void testFindById() {

		// given
		Tutorial tutorial = tutorialRepository.save(Tutorial.builder().title("Por id").description("d").build());

		// when
		Optional<Tutorial> tutorialEncontrado = tutorialRepository.findById(tutorial.getId());

		// then
		assertThat(tutorialEncontrado).isPresent();
		assertThat(tutorialEncontrado.get().getTitle()).isEqualTo("Por id");
	}

	@Test
	@DisplayName("Test de repositorio para recuperar un tutorial que no existe")
	void testFindByIdNoExistente() {

		// when
		Optional<Tutorial> tutorialEncontrado = tutorialRepository.findById(999L);

		// then
		assertThat(tutorialEncontrado).isEmpty();
	}

	@Test
	@DisplayName("Test de repositorio para actualizar un tutorial")
	void testUpdateTutorial() {

		// given
		Tutorial tutorial = tutorialRepository.save(
				Tutorial.builder().title("Titulo antiguo").description("d").published(false).build());

		// when
		tutorial.setTitle("Titulo nuevo");
		tutorial.setPublished(true);
		Tutorial tutorialActualizado = tutorialRepository.save(tutorial);

		// then
		assertThat(tutorialActualizado.getTitle()).isEqualTo("Titulo nuevo");
		assertThat(tutorialActualizado.isPublished()).isTrue();
	}

	@Test
	@DisplayName("Test de repositorio para eliminar un tutorial")
	void testDeleteTutorial() {

		// given
		Tutorial tutorial = tutorialRepository.save(Tutorial.builder().title("Borrable").description("d").build());

		// when
		tutorialRepository.deleteById(tutorial.getId());

		// then
		assertThat(tutorialRepository.findById(tutorial.getId())).isEmpty();
	}

	@Test
	@DisplayName("Test de repositorio para eliminar todos los tutorials")
	void testDeleteAllTutorials() {

		// given
		tutorialRepository.save(Tutorial.builder().title("Uno").description("d").build());
		tutorialRepository.save(Tutorial.builder().title("Dos").description("d").build());

		// when
		tutorialRepository.deleteAll();

		// then
		assertThat(tutorialRepository.count()).isZero();
	}
}
