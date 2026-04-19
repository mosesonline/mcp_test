package de.mosesonline.mcptest.mcptest.book_store;

import de.mosesonline.mcptest.mcptest.book_store.model.Book;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.CountResponse;
import org.opensearch.client.opensearch.core.CreateRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;

@Configuration
class BookStoreConfiguration {

    private final OpenSearchClient openSearchClient;

    BookStoreConfiguration(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    @EventListener
    void onApplicationEvent(ContextRefreshedEvent event) throws IOException {

        if (!bookIndexExists()) {
            openSearchClient.indices().create(c -> c.index("books"));
        }

        CountRequest getRequest = new CountRequest.Builder().index("books").build();
        CountResponse count = openSearchClient.count(getRequest);
        if (count.count() == 0) {
            final var createRequest = new CreateRequest.Builder<Book>().index("books")
                    .id("1")
                    .document(new Book("The Lord of the Rings", "J.R.R. Tolkien", "978-0395647387", "A fellowship travels around middlearth to throw a ring in a volcano,"))
                    .build();
            openSearchClient.create(createRequest);
        }
    }

    private boolean bookIndexExists() throws IOException {
        return openSearchClient.indices()
                .exists(e -> e.index("books"))
                .value();
    }
}
