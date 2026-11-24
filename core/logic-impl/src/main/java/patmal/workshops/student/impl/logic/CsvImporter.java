package patmal.workshops.student.impl.logic;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Student;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Registers students from a UTF-8 CSV file with the header id,name,email.
 * Each line goes through {@link WorkshopSystem#createStudent}, so all its rules apply.
 * The import stops at the first rejected line; students of earlier lines stay registered.
 */
class CsvImporter {

    private static final Logger LOG = LogManager.getLogger(CsvImporter.class);
    private static final List<String> EXPECTED_HEADER = List.of("id", "name", "email");

    List<Student> importStudents(WorkshopSystem system, File source) {
        if (source == null) {
            throw new IllegalArgumentException("source file is null");
        }
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();
        List<Student> imported = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(source.toPath(), StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {
            // A UTF-8 BOM is not stripped by the reader; it would stick to the first header name.
            List<String> header = new ArrayList<>(parser.getHeaderNames());
            if (!header.isEmpty() && header.get(0).startsWith("\uFEFF")) {
                header.set(0, header.get(0).substring(1));
            }
            if (!EXPECTED_HEADER.equals(header)) {
                throw new IllegalArgumentException(
                        "CSV header must be id,name,email but was " + header);
            }
            for (CSVRecord record : parser) {
                if (record.size() != EXPECTED_HEADER.size()) {
                    throw new IllegalArgumentException("CSV record " + record.getRecordNumber()
                            + " must have 3 values but has " + record.size());
                }
                String id = record.get(0);
                system.createStudent(id, record.get(1), record.get(2));
                imported.add(system.getStudent(id));
            }
        } catch (IllegalArgumentException e) {
            LOG.warn("CSV import of {} rejected after {} students: {}", source, imported.size(), e.getMessage());
            throw e;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LOG.info("Imported {} students from {}", imported.size(), source);
        return imported;
    }
}
