package de.mosesonline.mcptest.mcptest.book_store.model;

import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "books")
public record Book(String title, String author, String isbn, String description) {
}
