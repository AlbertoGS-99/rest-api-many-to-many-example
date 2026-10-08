package com.example.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entities.Tag;
import com.example.entities.Tutorial;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.TagRepository;
import com.example.repository.TutorialRepository;

import lombok.RequiredArgsConstructor;

/**
 * La anotacion @Transactional a nivel de clase garantiza que cada operacion del
 * servicio se ejecuta dentro de una transaccion, y por lo tanto con la sesion de
 * Hibernate abierta, de forma que se pueden inicializar las colecciones
 * relacionadas (lazy) de las entidades, como por ejemplo Tutorial.tags
 */
@Transactional
@RequiredArgsConstructor
@Service
public class TagServiceImpl implements TagService {

	private final TutorialRepository tutorialRepository;
	private final TagRepository tagRepository;

	@Override
	public List<Tag> findAll() {
		List<Tag> tags = new ArrayList<Tag>();
		tagRepository.findAll().forEach(tags::add);
		return tags;
	}

	@Override
	public Tag findById(Long id) {
		return tagRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Not found Tag with id = " + id));
	}

	@Override
	public List<Tag> findByTutorialId(Long tutorialId) {

		if (!tutorialRepository.existsById(tutorialId)) {
			throw new ResourceNotFoundException("Not found Tutorial with id = " + tutorialId);
		}

		return tagRepository.findTagsByTutorialsId(tutorialId);
	}

	@Override
	public List<Tutorial> findTutorialsByTagId(Long tagId) {

		if (!tagRepository.existsById(tagId)) {
			throw new ResourceNotFoundException("Not found Tag with id = " + tagId);
		}

		return tutorialRepository.findTutorialsByTagsId(tagId);
	}

	@Override
	public Tag addToTutorial(Long tutorialId, Tag tagRequest) {

		return tutorialRepository.findById(tutorialId).map(tutorial -> {
			long tagId = tagRequest.getId();

			// tag is existed
			if (tagId != 0L) {
				Tag _tag = tagRepository.findById(tagId)
						.orElseThrow(() -> new ResourceNotFoundException(
								"Not found Tag with id = " + tagId));
				tutorial.addTag(_tag);
				tutorialRepository.save(tutorial);
				return _tag;
			}

			// add and create new Tag
			tutorial.addTag(tagRequest);
			return tagRepository.save(tagRequest);
		}).orElseThrow(() -> new ResourceNotFoundException(
				"Not found Tutorial with id = " + tutorialId));
	}

	@Override
	public Tag update(Long id, Tag tagRequest) {

		Tag tag = tagRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("TagId " + id + "not found"));

		tag.setName(tagRequest.getName());

		return tagRepository.save(tag);
	}

	@Override
	public void deleteFromTutorial(Long tutorialId, Long tagId) {

		Tutorial tutorial = tutorialRepository.findById(tutorialId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Not found Tutorial with id = " + tutorialId));

		tutorial.removeTag(tagId);
		tutorialRepository.save(tutorial);
	}

	@Override
	public void delete(Long id) {

		Tag tag = findById(id);

		/**
		 * Antes de eliminar el tag hay que desasociarlo de todos los tutorials en
		 * los que este, porque la tabla de union (tutorial_tags) es propiedad del
		 * tutorial (lado propietario de la relacion), y de lo contrario se produciria
		 * una violacion de clave foranea (foreign key)
		 */
		for (Tutorial tutorial : new ArrayList<>(tag.getTutorials())) {
			tutorial.removeTag(tag.getId());
			tutorialRepository.save(tutorial);
		}

		tagRepository.deleteById(id);
	}
}
