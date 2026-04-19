package de.mosesonline.mcptest.mcptest.book_store;

import de.mosesonline.mcptest.mcptest.book_store.model.Book;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class BookStoreService {


    private final OpenSearchClient openSearchClient;

    public BookStoreService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    public List<Book> getBooks(String text) {
        try {
            return openSearchClient.search(s -> s
                            .index("books")
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .fields("title", "author", "isbn", "description")
                                            .query(text)
                                    )
                            ),
                    Book.class)
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
