package com.example.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.entities.Tag;
import com.example.entities.Tutorial;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.TagRepository;
import com.example.repository.TutorialRepository;

/**
 * Test unitarios de la capa de servicio TagService
 *
 * Las dependencias de los repositorios se simulan (mock) con Mockito, de forma
 * que el test se ejecuta de forma aislada, sin necesidad de base de datos
 */
@ExtendWith(MockitoExtension.class)
class TagServiceTest {

	@Mock
	private TutorialRepository tutorialRepository;

	@Mock
	private TagRepository tagRepository;

	@InjectMocks
	private TagServiceImpl tagService;

	Tutorial tutorial;
	Tag tag1, tag2;

	@BeforeEach
	void setUp() {

		tutorial = Tutorial.builder()
				.title("Spring Boot")
				.description("APIs REST")
				.published(true)
				.build();

		tag1 = Tag.builder().id(1L).name("java").build();
		tag2 = Tag.builder().id(2L).name("spring").build();
	}

	@Test
	@DisplayName("Test del servicio para recuperar todos los tags")
	void testFindAll() {

		// given
		given(tagRepository.findAll()).willReturn(List.of(tag1, tag2));

		// when
		List<Tag> tags = tagService.findAll();

		// then
		assertThat(tags).hasSize(2);
	}

	@Test
	@DisplayName("Test del servicio para recuperar un tag por su id")
	void testFindById() {

		// given
		given(tagRepository.findById(1L)).willReturn(Optional.of(tag1));

		// when
		Tag tagEncontrado = tagService.findById(1L);

		// then
		assertThat(tagEncontrado.getName()).isEqualTo("java");
	}

	@Test
	@DisplayName("Test del servicio para recuperar un tag que no existe")
	void testFindByIdNoExistente() {

		// given
		given(tagRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> tagService.findById(999L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("999");
	}

	@Test
	@DisplayName("Test del servicio para recuperar los tags de un tutorial existente")
	void testFindByTutorialId() {

		// given
		given(tutorialRepository.existsById(1L)).willReturn(true);
		given(tagRepository.findTagsByTutorialsId(1L)).willReturn(List.of(tag1));

		// when
		List<Tag> tags = tagService.findByTutorialId(1L);

		// then
		assertThat(tags).hasSize(1);
		assertThat(tags.get(0).getName()).isEqualTo("java");
	}

	@Test
	@DisplayName("Test del servicio para recuperar los tags de un tutorial que no existe")
	void testFindByTutorialIdNoExistente() {

		// given
		given(tutorialRepository.existsById(999L)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> tagService.findByTutorialId(999L))
				.isInstanceOf(ResourceNotFoundException.class);
		verify(tagRepository, never()).findTagsByTutorialsId(any());
	}

	@Test
	@DisplayName("Test del servicio para recuperar los tutorials de un tag existente")
	void testFindTutorialsByTagId() {

		// given
		given(tagRepository.existsById(1L)).willReturn(true);
		given(tutorialRepository.findTutorialsByTagsId(1L)).willReturn(List.of(tutorial));

		// when
		List<Tutorial> tutorials = tagService.findTutorialsByTagId(1L);

		// then
		assertThat(tutorials).hasSize(1);
		assertThat(tutorials.get(0).getTitle()).isEqualTo("Spring Boot");
	}

	@Test
	@DisplayName("Test del servicio para recuperar los tutorials de un tag que no existe")
	void testFindTutorialsByTagIdNoExistente() {

		// given
		given(tagRepository.existsById(999L)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> tagService.findTutorialsByTagId(999L))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("Test del servicio para crear un tag nuevo y asociarlo a un tutorial")
	void testAddTagNuevo() {

		// given
		Tag tagNuevo = Tag.builder().id(0L).name("nuevo").build();

		given(tutorialRepository.findById(1L)).willReturn(Optional.of(tutorial));
		given(tagRepository.save(any(Tag.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		// when
		Tag tagCreado = tagService.addToTutorial(1L, tagNuevo);

		// then
		assertThat(tagCreado.getName()).isEqualTo("nuevo");
		verify(tagRepository).save(tagNuevo);
		verify(tutorialRepository, never()).save(any(Tutorial.class));
	}

	@Test
	@DisplayName("Test del servicio para asociar un tag ya existente a un tutorial")
	void testAddTagExistente() {

		// given
		given(tutorialRepository.findById(1L)).willReturn(Optional.of(tutorial));
		given(tagRepository.findById(2L)).willReturn(Optional.of(tag2));
		given(tutorialRepository.save(any(Tutorial.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		// when
		Tag tagAsociado = tagService.addToTutorial(1L, Tag.builder().id(2L).build());

		// then
		assertThat(tagAsociado).isNotNull();
		verify(tutorialRepository).save(tutorial);
	}

	@Test
	@DisplayName("Test del servicio para asociar un tag que no existe a un tutorial")
	void testAddTagQueNoExiste() {

		// given
		given(tutorialRepository.findById(1L)).willReturn(Optional.of(tutorial));
		given(tagRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> tagService.addToTutorial(1L, Tag.builder().id(999L).build()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("Test del servicio para asociar un tag a un tutorial que no existe")
	void testAddTagTutorialNoExistente() {

		// given
		given(tutorialRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> tagService.addToTutorial(999L, Tag.builder().name("x").build()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("Test del servicio para actualizar el nombre de un tag")
	void testUpdate() {

		// given
		given(tagRepository.findById(1L)).willReturn(Optional.of(tag1));
		given(tagRepository.save(any(Tag.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		// when
		Tag tagActualizado = tagService.update(1L, Tag.builder().name("angular").build());

		// then
		assertThat(tagActualizado.getName()).isEqualTo("angular");
	}

	@Test
	@DisplayName("Test del servicio para actualizar un tag que no existe")
	void testUpdateNoExistente() {

		// given
		given(tagRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> tagService.update(999L, Tag.builder().name("x").build()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("Test del servicio para desasociar un tag de un tutorial")
	void testDeleteFromTutorial() {

		// given
		tutorial.addTag(tag1);

		given(tutorialRepository.findById(1L)).willReturn(Optional.of(tutorial));
		given(tutorialRepository.save(any(Tutorial.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		// when
		tagService.deleteFromTutorial(1L, 1L);

		// then
		verify(tutorialRepository).save(tutorial);
		assertThat(tutorial.getTags()).isEmpty();
	}

	@Test
	@DisplayName("Test del servicio para desasociar un tag de un tutorial que no existe")
	void testDeleteFromTutorialNoExistente() {

		// given
		given(tutorialRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> tagService.deleteFromTutorial(999L, 1L))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("Test del servicio para eliminar un tag")
	void testDelete() {

		// given
		given(tagRepository.findById(1L)).willReturn(Optional.of(tag1));
		doNothing().when(tagRepository).deleteById(1L);

		// when
		tagService.delete(1L);

		// then
		verify(tagRepository).deleteById(1L);
	}

	@Test
	@DisplayName("Test del servicio para eliminar un tag asociado a un tutorial")
	void testDeleteTagAsociado() {

		// given
		tutorial.addTag(tag1);

		given(tagRepository.findById(1L)).willReturn(Optional.of(tag1));
		given(tutorialRepository.save(any(Tutorial.class)))
				.willAnswer(invocation -> invocation.getArgument(0));
		doNothing().when(tagRepository).deleteById(1L);

		// when
		tagService.delete(1L);

		// then
		verify(tutorialRepository).save(tutorial);
		assertThat(tutorial.getTags()).isEmpty();
		verify(tagRepository).deleteById(1L);
	}
}
