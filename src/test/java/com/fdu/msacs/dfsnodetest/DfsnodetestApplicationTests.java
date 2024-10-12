package com.fdu.msacs.dfsnodetest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

@SpringBootTest
class DfsnodetestApplicationTests {

    //@LocalServerPort
    //private int port;

    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
    }

    private String getBaseUrl() {
        return "http://localhost:8080" + "/metadata";
    }

    @Test
    void registerNode() {
        String nodeAddress = "http://localhost:8081";
        RequestNode request = new RequestNode();
        request.setNodeUrl(nodeAddress);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/plain");
        HttpEntity<RequestNode> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/register-node", HttpMethod.POST, entity, String.class);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Node registered: " + nodeAddress, response.getBody());
    }

    @Test
    void registerFileLocation() {
        RequestFileLocation request = new RequestFileLocation();
        request.setFilename("testFile.txt");
        request.setNodeUrl("http://localhost:8081");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<RequestFileLocation> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/register-file-location", HttpMethod.POST, entity, String.class);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("File location registered: testFile.txt on http://localhost:8081", response.getBody());
    }

    @Test
    void getNodesForFile() {
    	clearCache();
    	clearRegisteredNodes();
    	
        // Step 1: Register multiple nodes
        String node1 = "http://localhost:8081";
        String node2 = "http://localhost:8082";
        String node3 = "http://localhost:8083";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);  // Set Content-Type to application/json

        // Registering node1
        HttpEntity<String> requestNode1 = new HttpEntity<>(node1, headers);
        restTemplate.exchange(getBaseUrl() + "/register-node", HttpMethod.POST, requestNode1, String.class);

        // Registering node2
        HttpEntity<String> requestNode2 = new HttpEntity<>(node2, headers);
        restTemplate.exchange(getBaseUrl() + "/register-node", HttpMethod.POST, requestNode2, String.class);

        // Registering node3
        HttpEntity<String> requestNode3 = new HttpEntity<>(node3, headers);
        restTemplate.exchange(getBaseUrl() + "/register-node", HttpMethod.POST, requestNode3, String.class);

        // Step 2: Register a file location to node1
        RequestFileLocation fileRequest = new RequestFileLocation();
        fileRequest.setFilename("testFile.txt");
        fileRequest.setNodeUrl(node1);

        HttpEntity<RequestFileLocation> fileRequestEntity = new HttpEntity<>(fileRequest, headers);
        restTemplate.exchange(getBaseUrl() + "/register-file-location", HttpMethod.POST, fileRequestEntity, String.class);

        // Step 3: Call the endpoint to get nodes for the file
        ResponseEntity<List<String>> response = restTemplate.exchange(getBaseUrl() + "/nodes-for-file/testFile.txt", HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});

        // Assert the response status code
        assertEquals(200, response.getStatusCodeValue());

        // Assert that the response contains the expected nodes (node2 and node3)
        List<String> expectedNodes = List.of(node1);
        assertEquals(expectedNodes, response.getBody());
    }


    @Test
    void getReplicationNodes() {
    	clearCache();
    	clearRegisteredNodes();
        // Step 1: Register three nodes
        String node1 = "http://localhost:8081";
        String node2 = "http://localhost:8082";
        String node3 = "http://localhost:8083";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);  // Set Content-Type to application/json

        // Registering node1
        HttpEntity<String> requestNode1 = new HttpEntity<>(node1, headers);
        restTemplate.exchange(getBaseUrl() + "/register-node", HttpMethod.POST, requestNode1, String.class);

        // Registering node2
        HttpEntity<String> requestNode2 = new HttpEntity<>(node2, headers);
        restTemplate.exchange(getBaseUrl() + "/register-node", HttpMethod.POST, requestNode2, String.class);

        // Registering node3
        HttpEntity<String> requestNode3 = new HttpEntity<>(node3, headers);
        restTemplate.exchange(getBaseUrl() + "/register-node", HttpMethod.POST, requestNode3, String.class);

        // Step 2: Register a file location to node2
        RequestFileLocation fileRequest = new RequestFileLocation();
        fileRequest.setFilename("testFile.txt");
        fileRequest.setNodeUrl(node2);

        HttpEntity<RequestFileLocation> fileRequestEntity = new HttpEntity<>(fileRequest, headers);
        restTemplate.exchange(getBaseUrl() + "/register-file-location", HttpMethod.POST, fileRequestEntity, String.class);

        // Step 3: Create the request for replication nodes
        RequestReplicationNodes request = new RequestReplicationNodes();
        request.setFilename("testFile.txt");
        request.setRequestingNodeUrl(node2); // Requesting from node1

        HttpEntity<RequestReplicationNodes> entity = new HttpEntity<>(request, headers);

        // Step 4: Call the endpoint to get replication nodes
        ResponseEntity<List<String>> response = restTemplate.exchange(getBaseUrl() + "/get-replication-nodes", HttpMethod.POST, entity, new ParameterizedTypeReference<List<String>>() {});

        // Assert the response status code
        assertEquals(200, response.getStatusCodeValue());

        // Assert that the response contains the expected nodes (node1 and node3)
        List<String> expectedNodes = List.of(node1, node3); // Expects nodes 8081 and 8083
        assertEquals(expectedNodes, response.getBody());
    }


    @Test
    void getRegisteredNodes() {
        ResponseEntity<List<String>> response = restTemplate.exchange(getBaseUrl() + "/get-registered-nodes", HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(List.of(), response.getBody()); // Adjust based on expected outcome
    }

    @Test
    void getNodeFiles() {
        // Step 1: Clear cache and registered nodes
        restTemplate.postForEntity(getBaseUrl() + "/clear-cache", null, String.class);
        restTemplate.postForEntity(getBaseUrl() + "/clear-registered-nodes", null, String.class);

        // Step 2: Register three nodes
        restTemplate.postForEntity(getBaseUrl() + "/register-node", "http://localhost:8081", String.class);
        restTemplate.postForEntity(getBaseUrl() + "/register-node", "http://localhost:8082", String.class);
        restTemplate.postForEntity(getBaseUrl() + "/register-node", "http://localhost:8083", String.class);

        // Step 3: Register three files to node 1
        RequestFileLocation requestFileLocation1 = new RequestFileLocation();
        requestFileLocation1.setFilename("file1.txt");
        requestFileLocation1.setNodeUrl("http://localhost:8081");
        restTemplate.postForEntity(getBaseUrl() + "/register-file-location", requestFileLocation1, String.class);

        RequestFileLocation requestFileLocation2 = new RequestFileLocation();
        requestFileLocation2.setFilename("file2.txt");
        requestFileLocation2.setNodeUrl("http://localhost:8081");
        restTemplate.postForEntity(getBaseUrl() + "/register-file-location", requestFileLocation2, String.class);

        RequestFileLocation requestFileLocation3 = new RequestFileLocation();
        requestFileLocation3.setFilename("file3.txt");
        requestFileLocation3.setNodeUrl("http://localhost:8081");
        restTemplate.postForEntity(getBaseUrl() + "/register-file-location", requestFileLocation3, String.class);

        // Step 4: Call the endpoint and verify the response
        RequestNode request = new RequestNode();
        request.setNodeUrl("http://localhost:8081");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<RequestNode> entity = new HttpEntity<>(request, headers);

        ResponseEntity<List<String>> response = restTemplate.exchange(
            getBaseUrl() + "/get-node-files",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<List<String>>() {}
        );

        // Step 5: Assert the expected outcome
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(List.of("file1.txt", "file2.txt", "file3.txt"), response.getBody());
    }

    @Test
    void getFileNodeMapping() {
    	clearCache();
    	clearRegisteredNodes();
    	
        // Step 1: Register three nodes
        restTemplate.postForEntity(getBaseUrl() + "/register-node", "http://localhost:8081", String.class);
        restTemplate.postForEntity(getBaseUrl() + "/register-node", "http://localhost:8082", String.class);
        restTemplate.postForEntity(getBaseUrl() + "/register-node", "http://localhost:8083", String.class);

        // Step 2: Register a file to nodes 2 and 3
        RequestFileLocation requestFileLocation1 = new RequestFileLocation();
        requestFileLocation1.setFilename("testFile.txt");
        requestFileLocation1.setNodeUrl("http://localhost:8082");
        restTemplate.postForEntity(getBaseUrl() + "/register-file-location", requestFileLocation1, String.class);
        
        RequestFileLocation requestFileLocation2 = new RequestFileLocation();
        requestFileLocation2.setFilename("testFile.txt");
        requestFileLocation2.setNodeUrl("http://localhost:8083");
        restTemplate.postForEntity(getBaseUrl() + "/register-file-location", requestFileLocation2, String.class);

        // Step 3: Call the endpoint and verify the response
        ResponseEntity<List<String>> response = restTemplate.exchange(
            getBaseUrl() + "/get-file-node-mapping/testFile.txt",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<String>>() {}
        );

        // Step 4: Assert the expected outcome
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(List.of("http://localhost:8082", "http://localhost:8083"), response.getBody());
    }


    @Test
    void clearCache() {
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/clear-cache", HttpMethod.POST, null, String.class);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Cache cleared", response.getBody());
    }

    @Test
    void clearRegisteredNodes() {
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/clear-registered-nodes", HttpMethod.POST, null, String.class);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Registered nodes cleared.", response.getBody());
    }

    @Test
    void pingSvr() {
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/pingsvr", HttpMethod.GET, null, String.class);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Metadata Server is running...", response.getBody());
    }
}
