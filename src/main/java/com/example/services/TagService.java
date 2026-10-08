package com.example.services;

import java.util.List;

import com.example.entities.Tag;
import com.example.entities.Tutorial;

public interface TagService {

	List<Tag> findAll();

	Tag findById(Long id);

	List<Tag> findByTutorialId(Long tutorialId);

	List<Tutorial> findTutorialsByTagId(Long tagId);

	Tag addToTutorial(Long tutorialId, Tag tagRequest);

	Tag update(Long id, Tag tagRequest);

	void deleteFromTutorial(Long tutorialId, Long tagId);

	void delete(Long id);
}
