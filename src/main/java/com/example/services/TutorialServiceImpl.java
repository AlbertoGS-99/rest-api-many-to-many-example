package com.example.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entities.Tutorial;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.TutorialRepository;

import lombok.RequiredArgsConstructor;

/**
 * La anotacion @Transactional a nivel de clase garantiza que cada operacion del
 * servicio se ejecuta dentro de una transaccion, y por lo tanto con la sesion de
 * Hibernate abierta, de forma que se pueden inicializar las colecciones
 * relacionadas (lazy) de las entidades
 */
@Transactional
@RequiredArgsConstructor
@Service
public class TutorialServiceImpl implements TutorialService {

	private final TutorialRepository tutorialRepository;

	@Override
	public List<Tutorial> findAll(String title) {

		List<Tutorial> tutorials = new ArrayList<>();

		if (title == null) {
			tutorialRepository.findAll().forEach(tutorials::add);
		} else {
			tutorialRepository.findByTitleContaining(title).forEach(tutorials::add);
		}

		return tutorials;
	}

	@Override
	public Tutorial findById(long id) {
		return tutorialRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Not found Tutorial with id = " + id));
	}

	@Override
	public Tutorial create(Tutorial tutorial) {
		return tutorialRepository.save(
				Tutorial.builder()
						.title(tutorial.getTitle())
						.description(tutorial.getDescription())
						.published(true)
						.build());
	}

	@Override
	public Tutorial update(long id, Tutorial tutorial) {

		Tutorial _tutorial = findById(id);

		_tutorial.setTitle(tutorial.getTitle());
		_tutorial.setDescription(tutorial.getDescription());
		_tutorial.setPublished(tutorial.isPublished());

		return tutorialRepository.save(_tutorial);
	}

	@Override
	public void delete(long id) {
		tutorialRepository.deleteById(id);
	}

	@Override
	public void deleteAll() {
		tutorialRepository.deleteAll();
	}

	@Override
	public List<Tutorial> findByPublished(boolean published) {
		return tutorialRepository.findByPublished(published);
	}
}
