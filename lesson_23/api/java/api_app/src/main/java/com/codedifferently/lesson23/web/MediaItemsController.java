package com.codedifferently.lesson23.web;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.codedifferently.lesson23.library.Librarian;
import com.codedifferently.lesson23.library.Library;
import com.codedifferently.lesson23.library.MediaItem;
import com.codedifferently.lesson23.library.exceptions.MediaItemCheckedOutException;
import com.codedifferently.lesson23.library.search.SearchCriteria;

import jakarta.validation.Valid;

@RestController
@CrossOrigin
public class MediaItemsController {

  private final Library library;
  private final Librarian librarian;

  public MediaItemsController(Library library) throws IOException {
    this.library = library;
    this.librarian = library.getLibrarians().stream().findFirst().orElseThrow();
  }

  @GetMapping("/items")
  public ResponseEntity<GetMediaItemsResponse> getItems() {
    SearchCriteria criteria = new SearchCriteria();
    Set<MediaItem> items = library.search(criteria);
    List<MediaItemResponse> responseItems = items.stream().map(MediaItemResponse::from).toList();
    GetMediaItemsResponse response = GetMediaItemsResponse.builder().items(responseItems).build();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/items/{id}")
  public ResponseEntity<MediaItemResponse> getItem(@PathVariable UUID id) {
    if (!library.hasMediaItem(id)) {
      return ResponseEntity.notFound().build();
    }
    SearchCriteria criteria = new SearchCriteria();
    criteria.id = id.toString();
    Set<MediaItem> items = library.search(criteria);
    if (items.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    MediaItem item = items.iterator().next();
    MediaItemResponse response = MediaItemResponse.from(item);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/items")
  public ResponseEntity<CreateMediaItemResponse> createItem(@Valid @RequestBody CreateMediaItemRequest request) {
    MediaItem item = MediaItemRequest.asMediaItem(request.getItem());
    library.addMediaItem(item, librarian);
    MediaItemResponse itemResponse = MediaItemResponse.from(item);
    CreateMediaItemResponse response = CreateMediaItemResponse.builder().item(itemResponse).build();
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/items/{id}")
  public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
    if (!library.hasMediaItem(id)) {
      return ResponseEntity.notFound().build();
    }
    try {
      library.removeMediaItem(id, librarian);
      return ResponseEntity.noContent().build();
    } catch (MediaItemCheckedOutException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
