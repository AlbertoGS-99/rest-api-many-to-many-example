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
 * Test unitarios de la capa de repositorio (repositorios JPA) de los tags y los
 * tutorials, de la relacion many-to-many entre ambos.
 *
 * La anotacion @DataJpaTest carga unicamente la capa de persistencia (entidades
 * y repositorios), no todo el contexto de Spring, y cada test se ejecuta dentro
 * de una transaccion que se deshace (rollback) al terminar, por lo que la base
 * de datos se queda como estaba inicialmente.
 *
 * Se utiliza una base de datos H2 en memoria propia para no interferir con los
 * test de integracion que utilizan la base de datos de la aplicacion
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:repositorio_tags;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop" })
class TagRepositoryTest {

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private TutorialRepository tutorialRepository;

	private Tutorial crearTutorial(String title) {
		return tutorialRepository.save(Tutorial.builder()
				.title(title)
				.description("Descripcion de " + title)
				.published(true)
				.build());
	}

	@Test
	@DisplayName("Test de repositorio para persistir un tag")
	void testSaveTag() {

		// given
		Tag tag = Tag.builder().name("java").build();

		// when
		Tag tagGuardado = tagRepository.save(tag);

		// then
		assertThat(tagGuardado).isNotNull();
		assertThat(tagGuardado.getId()).isGreaterThan(0);
		assertThat(tagGuardado.getName()).isEqualTo("java");
	}

	@Test
	@DisplayName("Test de repositorio para recuperar todos los tags")
	void testFindAllTags() {

		// given
		tagRepository.save(Tag.builder().name("spring").build());
		tagRepository.save(Tag.builder().name("angular").build());

		// when
		List<Tag> tags = tagRepository.findAll();

		// then
		assertThat(tags).hasSize(2);
	}

	@Test
	@DisplayName("Test de repositorio para recuperar los tags de un tutorial")
	void testFindTagsByTutorialsId() {

		// given
		Tutorial tutorial = crearTutorial("Tutorial con tags");

		Tag tag = Tag.builder().name("mockito").build();
		tutorial.addTag(tag);
		tutorialRepository.save(tutorial);

		// when
		List<Tag> tags = tagRepository.findTagsByTutorialsId(tutorial.getId());

		// then
		assertThat(tags).hasSize(1);
		assertThat(tags.get(0).getName()).isEqualTo("mockito");
	}

	@Test
	@DisplayName("Test de repositorio para recuperar los tags de un tutorial inexistente")
	void testFindTagsByTutorialsIdInexistente() {

		// when
		List<Tag> tags = tagRepository.findTagsByTutorialsId(999L);

		// then
		assertThat(tags).isEmpty();
	}

	@Test
	@DisplayName("Test de repositorio para comprobar si un tag existe")
	void testExistsById() {

		// given
		Tag tag = tagRepository.save(Tag.builder().name("hibernate").build());

		// when & then
		assertThat(tagRepository.existsById(tag.getId())).isTrue();
		assertThat(tagRepository.existsById(999L)).isFalse();
	}

	@Test
	@DisplayName("Test de repositorio para recuperar un tag por su id")
	void testFindById() {

		// given
		Tag tag = tagRepository.save(Tag.builder().name("jpa").build());

		// when
		Optional<Tag> tagEncontrado = tagRepository.findById(tag.getId());

		// then
		assertThat(tagEncontrado).isPresent();
		assertThat(tagEncontrado.get().getName()).isEqualTo("jpa");
	}

	@Test
	@DisplayName("Test de repositorio para recuperar un tag que no existe")
	void testFindByIdNoExistente() {

		// when
		Optional<Tag> tagEncontrado = tagRepository.findById(999L);

		// then
		assertThat(tagEncontrado).isEmpty();
	}

	@Test
	@DisplayName("Test de repositorio para actualizar el nombre de un tag")
	void testUpdateTag() {

		// given
		Tag tag = tagRepository.save(Tag.builder().name("nombreAntiguo").build());

		// when
		tag.setName("nombreNuevo");
		Tag tagActualizado = tagRepository.save(tag);

		// then
		assertThat(tagActualizado.getName()).isEqualTo("nombreNuevo");
	}

	@Test
	@DisplayName("Test de repositorio para eliminar un tag")
	void testDeleteTag() {

		// given
		Tag tag = tagRepository.save(Tag.builder().name("borrable").build());

		// when
		tagRepository.deleteById(tag.getId());

		// then
		assertThat(tagRepository.findById(tag.getId())).isEmpty();
		assertThat(tagRepository.count()).isZero();
	}
}
