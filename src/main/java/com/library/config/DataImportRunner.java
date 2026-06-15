package com.library.config;

import com.library.service.DataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Runs on application startup to import the Kaggle Books Dataset
 * into the database if it hasn't been imported yet.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataImportRunner implements CommandLineRunner {

    private final DataImportService dataImportService;

    @Override
    public void run(String... args) {
        log.info("=== Checking if book data needs to be imported ===");
        dataImportService.importBooksFromCsv();
        log.info("=== Data import check completed ===");
    }
}
