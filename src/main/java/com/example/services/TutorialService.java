package com.example.services;

import java.util.List;

import com.example.entities.Tutorial;

public interface TutorialService {

	List<Tutorial> findAll(String title);

	Tutorial findById(long id);

	Tutorial create(Tutorial tutorial);

	Tutorial update(long id, Tutorial tutorial);

	void delete(long id);

	void deleteAll();

	List<Tutorial> findByPublished(boolean published);
}
