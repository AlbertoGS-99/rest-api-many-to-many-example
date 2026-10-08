package com.example.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.entities.Tutorial;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.TutorialRepository;

/**
 * Test unitarios de la capa de servicio TutorialService
 *
 * La dependencia del repositorio se simula (mock) con Mockito, de forma que el
 * test se ejecuta de forma aislada, sin necesidad de base de datos
 */
@ExtendWith(MockitoExtension.class)
class TutorialServiceTest {

	@Mock
	private TutorialRepository tutorialRepository;

	@InjectMocks
	private TutorialServiceImpl tutorialService;

	Tutorial tutorial1, tutorial2;
	List<Tutorial> tutorialsList;

	@BeforeEach
	void setUp() {

		tutorial1 = Tutorial.builder()
				.title("Spring Boot")
				.description("APIs REST")
				.published(true)
				.build();

		tutorial2 = Tutorial.builder()
				.title("Spring Security")
				.description("Seguridad")
				.published(false)
				.build();

		tutorialsList = List.of(tutorial1, tutorial2);
	}

	@Test
	@DisplayName("Test del servicio para recuperar todos los tutorials")
	void testFindAllSinTitulo() {

		// given
		given(tutorialRepository.findAll()).willReturn(tutorialsList);

		// when
		List<Tutorial> tutorials = tutorialService.findAll(null);

		// then
		assertThat(tutorials).hasSize(2);
		verify(tutorialRepository).findAll();
	}

	@Test
	@DisplayName("Test del servicio para recuperar una lista vacia de tutorials")
	void testFindAllListaVacia() {

		// given
		given(tutorialRepository.findAll()).willReturn(Collections.emptyList());

		// when
		List<Tutorial> tutorials = tutorialService.findAll(null);

		// then
		assertThat(tutorials).isEmpty();
	}

	@Test
	@DisplayName("Test del servicio para recuperar tutorials por parte del titulo")
	void testFindAllConTitulo() {

		// given
		given(tutorialRepository.findByTitleContaining("Spring")).willReturn(List.of(tutorial1));

		// when
		List<Tutorial> tutorials = tutorialService.findAll("Spring");

		// then
		assertThat(tutorials).hasSize(1);
		assertThat(tutorials.get(0).getTitle()).isEqualTo("Spring Boot");
	}

	@Test
	@DisplayName("Test del servicio para recuperar un tutorial por su id")
	void testFindById() {

		// given
		given(tutorialRepository.findById(1L)).willReturn(Optional.of(tutorial1));

		// when
		Tutorial tutorialEncontrado = tutorialService.findById(1L);

		// then
		assertThat(tutorialEncontrado).isNotNull();
		assertThat(tutorialEncontrado.getTitle()).isEqualTo("Spring Boot");
	}

	@Test
	@DisplayName("Test del servicio para recuperar un tutorial que no existe")
	void testFindByIdNoExistente() {

		// given
		given(tutorialRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> tutorialService.findById(999L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("999");
	}

	@Test
	@DisplayName("Test del servicio para persistir un tutorial")
	void testCreate() {

		// given
		given(tutorialRepository.save(any(Tutorial.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		// when
		Tutorial tutorialCreado = tutorialService.create(tutorial1);

		// then
		assertThat(tutorialCreado).isNotNull();
		assertThat(tutorialCreado.getTitle()).isEqualTo("Spring Boot");
		assertThat(tutorialCreado.isPublished()).isTrue();
		verify(tutorialRepository).save(any(Tutorial.class));
	}

	@Test
	@DisplayName("Test del servicio para actualizar un tutorial existente")
	void testUpdate() {

		// given
		Tutorial tutorialActualizado = Tutorial.builder()
				.title("Titulo nuevo")
				.description("Descripcion nueva")
				.published(true)
				.build();

		given(tutorialRepository.findById(1L)).willReturn(Optional.of(tutorial1));
		given(tutorialRepository.save(any(Tutorial.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		// when
		Tutorial resultado = tutorialService.update(1L, tutorialActualizado);

		// then
		assertThat(resultado.getTitle()).isEqualTo("Titulo nuevo");
		assertThat(resultado.getDescription()).isEqualTo("Descripcion nueva");
		assertThat(resultado.isPublished()).isTrue();
		verify(tutorialRepository).save(tutorial1);
	}

	@Test
	@DisplayName("Test del servicio para actualizar un tutorial que no existe")
	void testUpdateNoExistente() {

		// given
		given(tutorialRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> tutorialService.update(999L, tutorial1))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("Test del servicio para eliminar un tutorial")
	void testDelete() {

		// when
		doNothing().when(tutorialRepository).deleteById(1L);

		tutorialService.delete(1L);

		// then
		verify(tutorialRepository).deleteById(1L);
	}

	@Test
	@DisplayName("Test del servicio para eliminar todos los tutorials")
	void testDeleteAll() {

		// when
		doNothing().when(tutorialRepository).deleteAll();

		tutorialService.deleteAll();

		// then
		verify(tutorialRepository).deleteAll();
	}

	@Test
	@DisplayName("Test del servicio para recuperar los tutorials publicados")
	void testFindByPublished() {

		// given
		given(tutorialRepository.findByPublished(true)).willReturn(List.of(tutorial1));

		// when
		List<Tutorial> publicados = tutorialService.findByPublished(true);

		// then
		assertThat(publicados).hasSize(1);
		assertThat(publicados.get(0).isPublished()).isTrue();
	}
}
