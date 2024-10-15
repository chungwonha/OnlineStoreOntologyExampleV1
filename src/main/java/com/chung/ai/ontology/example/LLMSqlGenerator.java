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
    private String ontologyFilePath;

    @Value("${app.metadata-only.file-name}")
    private String metadataOnlyFilePath;

//    private String ontologyJson;
    private String owlOntology;
    private String metadataOnlyJson;
    private String targetDatabase;

    public String ontologyJson() throws IOException {
        ClassPathResource resource = new ClassPathResource(ontologyFilePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    public String retrieveOntologyOwl() throws IOException {
        ClassPathResource resource = new ClassPathResource(ontologyFilePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
    public String retrieveMetadataOnlyJson() throws IOException {
        ClassPathResource resource = new ClassPathResource(metadataOnlyFilePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @PostConstruct
    public void init() throws IOException {
        // Initialize the data access layer
        this.model = OpenAiChatModel
                        .builder()
                        .apiKey(openaiApiKey)
                        .modelName(OpenAiChatModelName.GPT_4).build();
//        this.ontologyJson = ontologyJson();
        this.owlOntology = retrieveOntologyOwl();
        this.metadataOnlyJson = retrieveMetadataOnlyJson();
        this.targetDatabase = determineTargetDatabase(driverClassName);

    }

    public String generateSql(String naturalLanguageQuery, boolean includesOntology) {
        String prompt = includesOntology ? buildOntologyPrompt(naturalLanguageQuery, targetDatabase)
                            : buildMetadataOnlyPrompt(naturalLanguageQuery, targetDatabase);
        return model.generate(prompt);
    }


    private String buildOntologyPrompt(String query, String targetDatabase) {
        log.info("buildOntologyPrompt: {}", owlOntology);
        return String.format(
                "Given the following ontology in OWL Turtle syntax:\n\n%s\n\n" +
                        "Note the following custom annotations:\n" +
                        "- meta:tableName specifies the database table name for a class\n" +
                        "- meta:columnName specifies the database column name for a property\n" +
                        "- meta:primaryKey indicates if a property is part of the primary key\n\n" +
                        "Generate an SQL query for the following request:\n%s\n\n" +
                        "The target database is %s. Please ensure the SQL is compatible with this database.\n" +
                        "For H2 database, use DATEADD function instead of DATE_SUB for date arithmetic.\n" +
                        "For non-case-sensitive fields (subproperties of :caseInsensitiveField), use the LOWER function in the WHERE condition to ensure case-insensitive comparison.\n" +
                        "Use the table and column names specified in the meta:tableName and meta:columnName annotations.\n" +
                        "Please provide only the SQL query without any additional explanation.\n" +
                        "If you cannot generate a correct SQL query due to lack of information, state that explicitly.",
                owlOntology, query, targetDatabase
        );
    }

    private String buildMetadataOnlyPrompt(String query, String targetDatabase) {
        log.info("buildMetadataOnlyPrompt: {}", metadataOnlyJson);
        return String.format(
                "Given the following database metadata:\n\n%s\n\n" +
                        "Generate an SQL query for the following request:\n%s\n\n" +
                        "The target database is %s. Please ensure the SQL is compatible with this database.\n" +
                        "For H2 database, use DATEADD function instead of DATE_SUB for date arithmetic.\n" +
                        "For non-case-sensitive fields (where \"caseSensitive\": false), use the LOWER function in the WHERE condition to ensure case-insensitive comparison.\n" +
                        "Please provide only the SQL query without any additional explanation.\n" +
                        "If you cannot generate a correct SQL query due to lack of information, state that explicitly.",
                metadataOnlyJson, query, targetDatabase
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