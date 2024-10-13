package com.chung.ai.ontology.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class QueryController {

    private LLMSqlGenerator llmSqlGenerator;

    @Autowired
    public QueryController(LLMSqlGenerator llmSqlGenerator,
                           Map<String, Object> ontologyMetadata) {
        this.llmSqlGenerator = llmSqlGenerator;
    }

    @PostMapping("/query")
    public String handleQuery(@RequestBody String query) {
        return llmSqlGenerator.processQuery(query,true);
    }

    @PostMapping("/query2")
    public String handleQuery(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        boolean includeOntology = (Boolean) request.get("includeOntology");
        return llmSqlGenerator.processQuery(query, includeOntology);
    }
}
