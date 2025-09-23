package com.netflix.springai.web;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = EmbeddingController.class)
class EmbeddingControllerTest {
	@Autowired MockMvc mvc;
	@MockBean EmbeddingClient embeddingClient;

	@Test
	void embeddingsEndpointWorks() throws Exception {
		when(embeddingClient.embed("hello")).thenReturn(java.util.List.of(0.1, 0.2));
		mvc.perform(post("/api/embeddings").contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"hello\"}"))
			.andExpect(status().isOk())
			.andExpect(content().json("[0.1,0.2]"));
	}
}
