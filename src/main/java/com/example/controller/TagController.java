package com.example.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entities.Tag;
import com.example.entities.Tutorial;
import com.example.services.TagService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.findAll();

        if (tags.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @GetMapping("/tutorials/{tutorialId}/tags")
    public ResponseEntity<List<Tag>> getAllTagsByTutorialId(@PathVariable Long tutorialId) {
        List<Tag> tags = tagService.findByTutorialId(tutorialId);

        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @GetMapping("/tags/{id}")
    public ResponseEntity<Tag> getTagsById(@PathVariable Long id) {
        Tag tag = tagService.findById(id);

        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @GetMapping("/tags/{tagId}/tutorials")
    public ResponseEntity<List<Tutorial>> getAllTutorialsByTagId(@PathVariable Long tagId) {
        List<Tutorial> tutorials = tagService.findTutorialsByTagId(tagId);

        return new ResponseEntity<>(tutorials, HttpStatus.OK);
    }

    @PostMapping("/tutorials/{tutorialId}/tags")
    public ResponseEntity<Tag> addTag(@PathVariable Long tutorialId,
            @RequestBody Tag tagRequest) {
        Tag tag = tagService.addToTutorial(tutorialId, tagRequest);

        return new ResponseEntity<>(tag, HttpStatus.CREATED);
    }

    @PutMapping("/tags/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable long id, @RequestBody Tag tagRequest) {
        Tag tag = tagService.update(id, tagRequest);

        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @DeleteMapping("/tutorials/{tutorialId}/tags/{tagId}")
    public ResponseEntity<HttpStatus> deleteTagFromTutorial(@PathVariable Long tutorialId,
            @PathVariable Long tagId) {
        tagService.deleteFromTutorial(tutorialId, tagId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<HttpStatus> deleteTag(@PathVariable long id) {
        tagService.delete(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
