package com.library.service;

import com.library.entity.Book;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service responsible for importing and cleaning the Kaggle Books Dataset (BX-Books.csv).
 *
 * The CSV file uses semicolons as separators and may have encoding issues.
 * Columns: ISBN;"Book-Title";"Book-Author";"Year-Of-Publication";"Publisher";"Image-URL-S";"Image-URL-M";"Image-URL-L"
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataImportService {

    private final BookRepository bookRepository;

    private static final int BATCH_SIZE = 500;
    private static final int DEFAULT_TOTAL_COPIES = 3;

    /**
     * Import books from the BX-Books.csv file into the database.
     * Cleans invalid data and skips duplicates.
     */
    @Transactional
    public void importBooksFromCsv() {
        if (bookRepository.count() > 0) {
            log.info("Books already imported. Skipping import.");
            return;
        }

        log.info("Starting book import from BX-Books.csv...");

        try {
            ClassPathResource resource = new ClassPathResource("data/BX-Books.csv");

            if (!resource.exists()) {
                log.warn("BX-Books.csv not found in src/main/resources/data/. Skipping import.");
                log.warn("Download the dataset from: https://www.kaggle.com/datasets/saurabhbagchi/books-dataset");
                return;
            }

            List<Book> books = parseCsvFile(resource.getInputStream());
            saveBooksInBatches(books);

            log.info("Book import completed. Total books imported: {}", books.size());

        } catch (IOException e) {
            log.error("Error reading BX-Books.csv: {}", e.getMessage(), e);
        }
    }

    /**
     * Parse the CSV file and return a list of cleaned Book entities.
     */
    private List<Book> parseCsvFile(InputStream inputStream) throws IOException {
        List<Book> books = new ArrayList<>();
        Set<String> seenIsbns = new HashSet<>();
        int lineNumber = 0;
        int skippedCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1))) {

            // Skip header line
            String header = reader.readLine();
            lineNumber++;

            if (header == null) {
                log.error("CSV file is empty.");
                return books;
            }

            log.info("CSV Header: {}", header);

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                try {
                    Book book = parseLineToBook(line);

                    if (book == null) {
                        skippedCount++;
                        continue;
                    }

                    // Skip duplicate ISBNs
                    if (seenIsbns.contains(book.getIsbn())) {
                        skippedCount++;
                        continue;
                    }

                    seenIsbns.add(book.getIsbn());
                    books.add(book);

                } catch (Exception e) {
                    skippedCount++;
                    if (skippedCount <= 10) {
                        log.debug("Skipping line {}: {}", lineNumber, e.getMessage());
                    }
                }
            }
        }

        log.info("Parsed {} valid books, skipped {} invalid entries.", books.size(), skippedCount);
        return books;
    }

    /**
     * Parse a single CSV line into a Book entity.
     * The CSV uses semicolons as separators and fields may be quoted with double quotes.
     */
    private Book parseLineToBook(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        List<String> fields = parseCsvLine(line, ';');

        if (fields.size() < 5) {
            return null;
        }

        String isbn = cleanField(fields.get(0));
        String title = cleanField(fields.get(1));
        String author = cleanField(fields.get(2));
        String yearStr = cleanField(fields.get(3));
        String publisher = cleanField(fields.get(4));
        String imageUrlS = fields.size() > 5 ? cleanField(fields.get(5)) : null;
        String imageUrlM = fields.size() > 6 ? cleanField(fields.get(6)) : null;
        String imageUrlL = fields.size() > 7 ? cleanField(fields.get(7)) : null;

        // Validate ISBN
        if (isbn.isEmpty() || isbn.length() > 20) {
            return null;
        }

        // Validate title
        if (title.isEmpty()) {
            return null;
        }

        // Truncate long fields
        title = truncate(title, 500);
        author = truncate(author.isEmpty() ? "Unknown" : author, 300);
        publisher = truncate(publisher, 300);

        // Parse and validate year
        Integer year = parseYear(yearStr);

        return Book.builder()
                .isbn(isbn)
                .title(title)
                .author(author)
                .yearOfPublication(year)
                .publisher(publisher)
                .imageUrlS(truncate(imageUrlS, 500))
                .imageUrlM(truncate(imageUrlM, 500))
                .imageUrlL(truncate(imageUrlL, 500))
                .totalCopies(DEFAULT_TOTAL_COPIES)
                .availableCopies(DEFAULT_TOTAL_COPIES)
                .active(true)
                .build();
    }

    /**
     * Parse a CSV line respecting quoted fields.
     * Handles semicolons within quoted strings.
     */
    private List<String> parseCsvLine(String line, char separator) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Check for escaped quote ("")
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == separator && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        fields.add(current.toString());
        return fields;
    }

    /**
     * Clean a field value by removing surrounding quotes and trimming whitespace.
     */
    private String cleanField(String field) {
        if (field == null) {
            return "";
        }
        field = field.trim();
        // Remove surrounding double quotes
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1);
        }
        // Replace HTML entities
        field = field.replace("&amp;", "&")
                     .replace("&lt;", "<")
                     .replace("&gt;", ">")
                     .replace("&quot;", "\"");
        return field.trim();
    }

    /**
     * Parse year string, returning null for invalid values.
     */
    private Integer parseYear(String yearStr) {
        if (yearStr == null || yearStr.isEmpty()) {
            return null;
        }
        try {
            int year = Integer.parseInt(yearStr.trim());
            // Filter out clearly invalid years
            if (year <= 0 || year > 2025) {
                return null;
            }
            return year;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Truncate a string to the specified maximum length.
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    /**
     * Save books in batches for better performance.
     */
    private void saveBooksInBatches(List<Book> books) {
        int totalSaved = 0;

        for (int i = 0; i < books.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, books.size());
            List<Book> batch = books.subList(i, end);

            try {
                bookRepository.saveAll(batch);
                totalSaved += batch.size();

                if (totalSaved % 5000 == 0 || totalSaved == books.size()) {
                    log.info("Progress: {}/{} books saved.", totalSaved, books.size());
                }
            } catch (Exception e) {
                log.error("Error saving batch starting at index {}: {}", i, e.getMessage());
                // Try saving one by one to skip problematic entries
                for (Book book : batch) {
                    try {
                        bookRepository.save(book);
                    } catch (Exception ex) {
                        log.debug("Skipping book with ISBN {}: {}", book.getIsbn(), ex.getMessage());
                    }
                }
            }
        }

        log.info("Total books saved to database: {}", totalSaved);
    }
}
