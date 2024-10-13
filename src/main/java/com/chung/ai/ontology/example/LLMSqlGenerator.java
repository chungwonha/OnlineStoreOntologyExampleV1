package com.chung.ai.ontology.example;

import dev.langchain4j.model.openai.OpenAiChatModelName;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * The LLMSqlGenerator class is a Spring service that generates SQL queries from natural language inputs using an OpenAI language model.
 * It can include ontology metadata in the query generation process and interacts with a data access layer to execute the generated SQL queries.
 * The class also handles initialization and configuration of the language model and ontology data.
 *
 * Author: Chung Ha
 */
@Service
@Data
@Slf4j
public class LLMSqlGenerator {

    private ChatLanguageModel model;
    private Map<String, Object> ontologyMetadata;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${spring.datasource.driver-class-name}")
    String driverClassName;

    @Autowired
    FlexibleDataAccessLayer dataAccessLayer;
    @Value("${app.ontology.file-name}")
    private String ontologyFileName;
    @Value("${app.metadata-only.file-name}")
    private String metadataOnly;

    private String ontologyJson;
    private String metadataOnlyJson;
    private String targetDatabase;

    public String ontologyJson() throws IOException {
        ClassPathResource resource = new ClassPathResource(ontologyFileName);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    public String metadataOnlyJson() throws IOException {
        ClassPathResource resource = new ClassPathResource(metadataOnly);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @PostConstruct
    public void init() throws IOException {
        // Initialize the data access layer
        this.model = OpenAiChatModel
                        .builder()
                        .apiKey(openaiApiKey)
                        .modelName(OpenAiChatModelName.GPT_4).build();
        this.ontologyJson = ontologyJson();
        this.metadataOnlyJson = metadataOnlyJson();
        this.targetDatabase = determineTargetDatabase(driverClassName);

    }

    public String generateSql(String naturalLanguageQuery) {

        String prompt = buildPrompt(naturalLanguageQuery, ontologyJson, targetDatabase);
        return model.generate(prompt);
    }

    public String generateSql(String naturalLanguageQuery, boolean includesOntology) {

        String prompt = buildPrompt(naturalLanguageQuery, ontologyJson, targetDatabase, includesOntology);
        return model.generate(prompt);
    }


    private String buildPrompt(String query, String ontologyJson, String targetDatabase) {
        return String.format(
                "Given the following %s:\n\n%s\n\n" +
                        "Generate an SQL query for the following request:\n\n%s\n\n" +
                        "The target database is %s. Please ensure the SQL is compatible with this database.\n" +
                        "For H2 database, use DATEADD function instead of DATE_SUB for date arithmetic.\n" +
                        "For non-case-sensitive fields (where 'caseSensitive' is false), use the appropriate character function in the WHERE condition to ensure case-insensitive comparison. " +
                        "For H2, use the LOWER function for case-insensitive comparisons.\n" +
                        "Please provide only the SQL query without any additional explanation.\n" +
                        "If you cannot generate a correct SQL query due to lack of information, state that explicitly.",
                ontologyJson, query, targetDatabase
        );
    }

    private String buildPrompt(String query, String schemaJson, String targetDatabase, boolean includesOntology) {
        String schemaType = includesOntology ? "schema and relationships (ontology)" : "database schema";
        return String.format(
                "Given the following %s:\n\n%s\n\n" +
                        "Generate an SQL query for the following request:\n\n%s\n\n" +
                        "The target database is %s. Please ensure the SQL is compatible with this database.\n" +
                        "For H2 database, use DATEADD function instead of DATE_SUB for date arithmetic.\n" +
                        "For non-case-sensitive fields (where 'caseSensitive' is false), use the appropriate character function in the WHERE condition to ensure case-insensitive comparison. " +
                        "For H2, use the LOWER function for case-insensitive comparisons.\n" +
                        "Please provide only the SQL query without any additional explanation.\n" +
                        "If you cannot generate a correct SQL query due to lack of information, state that explicitly.",
                schemaType, schemaJson, query, targetDatabase
        );
    }

    public String processQuery(String query,boolean includesOntology) {
        String sqlQuery = generateSql(query,includesOntology);
        if (sqlQuery == null || sqlQuery.isEmpty()) {
            return "I'm sorry, I couldn't generate a query for that request.";
        }
        log.info("Generated SQL query: " + sqlQuery);
        List<Map<String, Object>> results = dataAccessLayer.executeQuery(sqlQuery);
        return formatResults(results);
    }


    private String formatResults(List<Map<String, Object>> results) {

        StringBuilder formattedResults = new StringBuilder();
        for (Map<String, Object> row : results) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                formattedResults.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
            formattedResults.append("\n");
        }
        return formattedResults.toString();
    }

    private String determineTargetDatabase(String driverClassName) {
        if (driverClassName.contains("h2")) {
            return "H2";
        } else if (driverClassName.contains("mysql")) {
            return "MySQL";
        } else if (driverClassName.contains("postgresql")) {
            return "PostgreSQL";
        }

        return "Unknown";
    }
}