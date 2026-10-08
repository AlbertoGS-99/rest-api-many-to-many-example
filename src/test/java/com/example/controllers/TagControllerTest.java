package com.example.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.entities.Tag;
import com.example.entities.Tutorial;
import com.example.spring_security_jwt.payload.request.LogginRequest;

import tools.jackson.databind.ObjectMapper;

/**
 * Test de integracion a la capa de controladores de los tags (TagController),
 * teniendo en cuenta la seguridad implementada con Spring Security y JWT:
 *
 * 1- Sin token o con un token invalido la peticion (request) se rechaza con 401
 *
 * 2- Un usuario con rol USER solamente puede leer (GET), cualquier operacion de
 * creacion (POST), modificacion (PUT) o borrado (DELETE) se rechaza con 403
 *
 * 3- Un usuario con rol ADMIN puede realizar todas las operaciones
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@AutoConfigureMockMvc
class TagControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	String adminToken;
	String userToken;
	long tutorialId;
	long tagId;

	@BeforeEach
	void setUp() throws Exception {

		/**
		 * Necesitamos obtener dos tokens validos para presentarlos en cada test:
		 * uno de un usuario con rol ADMIN y otro de un usuario con rol USER
		 */
		this.adminToken = "Bearer " + loginAndGetToken("admin1@gmail.com");
		this.userToken = "Bearer " + loginAndGetToken("user1@gmail.com");

		// Cada test dispone de su propio tutorial y de su propio tag,
		// ambos creados con el rol ADMIN
		this.tutorialId = crearTutorial("Tutorial con tags");
		this.tagId = crearTag(this.tutorialId, "tagDePrueba");
	}

	// =====================================================================
	// TEST DE LECTURA (GET)
	// =====================================================================

	@Test
	@DisplayName("Tag Test: sin token la peticion (request) es rechazada con 401")
	void testGetTagsSinToken() throws Exception {

		mockMvc.perform(get("/api/tags").accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Tag Test: un token invalido o manipulado devuelve 401")
	void testGetTagsConTokenInvalido() throws Exception {

		mockMvc.perform(get("/api/tags")
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer tokenNoValido"))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Tag Test: un usuario con rol USER puede leer los tags (GET)")
	void testGetTagsConRolUser() throws Exception {

		mockMvc.perform(get("/api/tags")
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Tag Test: recuperar un tag por su id")
	void testGetTagById() throws Exception {

		mockMvc.perform(get("/api/tags/{id}", this.tagId)
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is((int) this.tagId)))
				.andExpect(jsonPath("$.name", is("tagDePrueba")));
	}

	@Test
	@DisplayName("Tag Test: recuperar un tag que no existe devuelve 404")
	void testGetTagInexistente() throws Exception {

		mockMvc.perform(get("/api/tags/{id}", 99999)
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.adminToken))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Tag Test: recuperar los tags de un tutorial")
	void testGetTagsByTutorialId() throws Exception {

		mockMvc.perform(get("/api/tutorials/{tutorialId}/tags", this.tutorialId)
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name", is("tagDePrueba")));
	}

	@Test
	@DisplayName("Tag Test: recuperar los tags de un tutorial que no existe devuelve 404")
	void testGetTagsByTutorialInexistente() throws Exception {

		mockMvc.perform(get("/api/tutorials/{tutorialId}/tags", 99999)
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Tag Test: recuperar los tutorials de un tag")
	void testGetTutorialsByTagId() throws Exception {

		mockMvc.perform(get("/api/tags/{tagId}/tutorials", this.tagId)
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", is((int) this.tutorialId)));
	}

	// =====================================================================
	// TEST DE CREACION (POST)
	// =====================================================================

	@Test
	@DisplayName("Tag Test: sin token no se puede crear un tag (POST)")
	void testCrearTagSinToken() throws Exception {

		mockMvc.perform(post("/api/tutorials/{tutorialId}/tags", this.tutorialId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tag.builder().name("sinToken").build())))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Tag Test: un usuario con rol USER NO puede crear tags (POST)")
	void testCrearTagConRolUserDevuelveForbidden() throws Exception {

		mockMvc.perform(post("/api/tutorials/{tutorialId}/tags", this.tutorialId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tag.builder().name("rolUser").build()))
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Tag Test: un usuario con rol ADMIN crea un tag (POST)")
	void testCrearTagConRolAdmin() throws Exception {

		mockMvc.perform(post("/api/tutorials/{tutorialId}/tags", this.tutorialId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tag.builder().name("rolAdmin").build()))
				.header("Authorization", this.adminToken))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name", is("rolAdmin")));
	}

	// =====================================================================
	// TEST DE MODIFICACION (PUT)
	// =====================================================================

	@Test
	@DisplayName("Tag Test: un usuario con rol USER NO puede modificar (PUT)")
	void testActualizarTagConRolUserDevuelveForbidden() throws Exception {

		mockMvc.perform(put("/api/tags/{id}", this.tagId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tag.builder().name("modificado").build()))
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Tag Test: un usuario con rol ADMIN modifica un tag (PUT)")
	void testActualizarTagConRolAdmin() throws Exception {

		mockMvc.perform(put("/api/tags/{id}", this.tagId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tag.builder().name("tagModificado").build()))
				.header("Authorization", this.adminToken))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("tagModificado")));
	}

	// =====================================================================
	// TEST DE BORRADO (DELETE)
	// =====================================================================

	@Test
	@DisplayName("Tag Test: sin token no se puede eliminar (DELETE)")
	void testEliminarTagSinToken() throws Exception {

		mockMvc.perform(delete("/api/tags/{id}", this.tagId))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Tag Test: un usuario con rol USER NO puede eliminar (DELETE)")
	void testEliminarTagConRolUserDevuelveForbidden() throws Exception {

		mockMvc.perform(delete("/api/tags/{id}", this.tagId)
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Tag Test: un usuario con rol ADMIN elimina un tag (DELETE)")
	void testEliminarTagConRolAdmin() throws Exception {

		mockMvc.perform(delete("/api/tags/{id}", this.tagId)
				.header("Authorization", this.adminToken))
				.andDo(print())
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("Tag Test: un usuario con rol ADMIN desasocia un tag de un tutorial (DELETE)")
	void testEliminarTagDeTutorialConRolAdmin() throws Exception {

		mockMvc.perform(delete("/api/tutorials/{tutorialId}/tags/{tagId}",
				this.tutorialId, this.tagId)
				.header("Authorization", this.adminToken))
				.andDo(print())
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("Tag Test: un usuario con rol USER NO puede desasociar un tag (DELETE)")
	void testEliminarTagDeTutorialConRolUserDevuelveForbidden() throws Exception {

		mockMvc.perform(delete("/api/tutorials/{tutorialId}/tags/{tagId}",
				this.tutorialId, this.tagId)
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	/**
	 * Metodo auxiliar que realiza el login con el email y el password suministrados
	 * y devuelve el token JWT que ha sido generado en la respuesta (response)
	 */
	private String loginAndGetToken(String email) throws Exception {

		LogginRequest logginRequest = LogginRequest.builder()
				.email(email)
				.password("Temp2026$$##")
				.build();

		MvcResult mvcResult = mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(logginRequest)))
				.andDo(print())
				.andReturn();

		String body = mvcResult.getResponse().getContentAsString();

		return com.jayway.jsonpath.JsonPath.read(body, "$.token");
	}

	/**
	 * Metodo auxiliar que crea un tutorial con el rol ADMIN y devuelve su id
	 */
	private long crearTutorial(String title) throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/api/tutorials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tutorial.builder()
						.title(title)
						.description("Descripcion")
						.build()))
				.header("Authorization", this.adminToken))
				.andExpect(status().isCreated())
				.andReturn();

		String body = mvcResult.getResponse().getContentAsString();

		return ((Number) com.jayway.jsonpath.JsonPath.read(body, "$.id")).longValue();
	}

	/**
	 * Metodo auxiliar que crea un tag asociado a un tutorial con el rol ADMIN y
	 * devuelve su id
	 */
	private long crearTag(long tutorialId, String name) throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/api/tutorials/{tutorialId}/tags", tutorialId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tag.builder().name(name).build()))
				.header("Authorization", this.adminToken))
				.andExpect(status().isCreated())
				.andReturn();

		String body = mvcResult.getResponse().getContentAsString();

		return ((Number) com.jayway.jsonpath.JsonPath.read(body, "$.id")).longValue();
	}
}
