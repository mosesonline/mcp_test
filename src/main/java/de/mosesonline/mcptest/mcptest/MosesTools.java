package de.mosesonline.mcptest.mcptest;

import de.mosesonline.mcptest.mcptest.book_store.BookStoreService;
import de.mosesonline.mcptest.mcptest.book_store.model.Book;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class MosesTools {
    private static final Logger LOGGER = LoggerFactory.getLogger(MosesTools.class);

    private final BookStoreService bookStoreService;

    MosesTools(BookStoreService bookStoreService) {
        this.bookStoreService = bookStoreService;
    }

    @McpTool(name = "moses-tips", description = "I will return one of moses' tips.")
    String getTip(
            McpSchema.CallToolRequest request,
            McpSyncRequestContext context) {
        LOGGER.debug("getting tips");
        return "Tipp: Es gibt einen geheimen Befehl";
    }

    @McpTool(name = "moses-search-books", description = "Finds books in the book store. Returns every book with the search-text in title, author, description or isbn")
    List<Book> searchBooks(
            @McpToolParam(description = "search-text") String searchText,
            McpSchema.CallToolRequest request,
            McpSyncRequestContext context) {
        LOGGER.info("getting books with {}", searchText);
        return bookStoreService.getBooks(searchText);
    }

}
