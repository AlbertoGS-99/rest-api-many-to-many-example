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

import com.example.entities.Tutorial;
import com.example.spring_security_jwt.payload.request.LogginRequest;

import tools.jackson.databind.ObjectMapper;

/**
 * Test de integracion a la capa de controladores de los tutorials
 * (TutorialController), teniendo en cuenta la seguridad implementada con Spring
 * Security y JWT:
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
class TutorialControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	String adminToken;
	String userToken;
	long tutorialId;

	@BeforeEach
	void setUp() throws Exception {

		/**
		 * Necesitamos obtener dos tokens validos para presentarlos en cada test:
		 * uno de un usuario con rol ADMIN y otro de un usuario con rol USER
		 */
		this.adminToken = "Bearer " + loginAndGetToken("admin1@gmail.com");
		this.userToken = "Bearer " + loginAndGetToken("user1@gmail.com");

		// Cada test dispone de su propio tutorial creado con el rol ADMIN
		this.tutorialId = crearTutorial("Tutorial de prueba", "Descripcion de prueba");
	}

	// =====================================================================
	// TEST DE LECTURA (GET)
	// =====================================================================

	@Test
	@DisplayName("Tutorial Test: sin token la peticion (request) es rechazada con 401")
	void testGetTutorialsSinToken() throws Exception {

		mockMvc.perform(get("/api/tutorials").accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Tutorial Test: un token invalido o manipulado devuelve 401")
	void testGetTutorialsConTokenInvalido() throws Exception {

		mockMvc.perform(get("/api/tutorials")
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer tokenNoValido"))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Tutorial Test: un usuario con rol USER puede leer los tutorials (GET)")
	void testGetTutorialsConRolUser() throws Exception {

		mockMvc.perform(get("/api/tutorials")
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Tutorial Test: recuperar un tutorial por su id")
	void testGetTutorialById() throws Exception {

		mockMvc.perform(get("/api/tutorials/{id}", this.tutorialId)
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is((int) this.tutorialId)))
				.andExpect(jsonPath("$.title", is("Tutorial de prueba")));
	}

	@Test
	@DisplayName("Tutorial Test: recuperar un tutorial que no existe devuelve 404")
	void testGetTutorialInexistente() throws Exception {

		mockMvc.perform(get("/api/tutorials/{id}", 99999)
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.adminToken))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Tutorial Test: recuperar los tutorials publicados")
	void testGetTutorialsPublicados() throws Exception {

		mockMvc.perform(get("/api/tutorials/published")
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.adminToken))
				.andDo(print())
				.andExpect(status().isOk());
	}

	// =====================================================================
	// TEST DE CREACION (POST)
	// =====================================================================

	@Test
	@DisplayName("Tutorial Test: sin token no se puede crear un tutorial (POST)")
	void testCrearTutorialSinToken() throws Exception {

		mockMvc.perform(post("/api/tutorials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tutorial.builder()
						.title("Sin token")
						.description("Descripcion")
						.build())))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Tutorial Test: un usuario con rol USER NO puede crear tutorials (POST)")
	void testCrearTutorialConRolUserDevuelveForbidden() throws Exception {

		mockMvc.perform(post("/api/tutorials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tutorial.builder()
						.title("Rol user")
						.description("Descripcion")
						.build()))
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Tutorial Test: un usuario con rol ADMIN crea un tutorial (POST)")
	void testCrearTutorialConRolAdmin() throws Exception {

		mockMvc.perform(post("/api/tutorials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tutorial.builder()
						.title("Creado por admin")
						.description("Descripcion del tutorial creado")
						.build()))
				.header("Authorization", this.adminToken))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title", is("Creado por admin")));
	}

	// =====================================================================
	// TEST DE MODIFICACION (PUT)
	// =====================================================================

	@Test
	@DisplayName("Tutorial Test: un usuario con rol USER NO puede modificar (PUT)")
	void testActualizarTutorialConRolUserDevuelveForbidden() throws Exception {

		mockMvc.perform(put("/api/tutorials/{id}", this.tutorialId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tutorial.builder()
						.title("Modificado")
						.description("Descripcion")
						.published(true)
						.build()))
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Tutorial Test: un usuario con rol ADMIN modifica un tutorial (PUT)")
	void testActualizarTutorialConRolAdmin() throws Exception {

		mockMvc.perform(put("/api/tutorials/{id}", this.tutorialId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tutorial.builder()
						.title("Titulo modificado")
						.description("Descripcion modificada")
						.published(true)
						.build()))
				.header("Authorization", this.adminToken))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("Titulo modificado")))
				.andExpect(jsonPath("$.description", is("Descripcion modificada")));
	}

	// =====================================================================
	// TEST DE BORRADO (DELETE)
	// =====================================================================

	@Test
	@DisplayName("Tutorial Test: sin token no se puede eliminar (DELETE)")
	void testEliminarTutorialSinToken() throws Exception {

		mockMvc.perform(delete("/api/tutorials/{id}", this.tutorialId))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Tutorial Test: un usuario con rol USER NO puede eliminar (DELETE)")
	void testEliminarTutorialConRolUserDevuelveForbidden() throws Exception {

		mockMvc.perform(delete("/api/tutorials/{id}", this.tutorialId)
				.header("Authorization", this.userToken))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Tutorial Test: un usuario con rol ADMIN elimina un tutorial (DELETE)")
	void testEliminarTutorialConRolAdmin() throws Exception {

		mockMvc.perform(delete("/api/tutorials/{id}", this.tutorialId)
				.header("Authorization", this.adminToken))
				.andDo(print())
				.andExpect(status().isNoContent());
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
	private long crearTutorial(String title, String description) throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/api/tutorials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tutorial.builder()
						.title(title)
						.description(description)
						.build()))
				.header("Authorization", this.adminToken))
				.andExpect(status().isCreated())
				.andReturn();

		String body = mvcResult.getResponse().getContentAsString();

		return ((Number) com.jayway.jsonpath.JsonPath.read(body, "$.id")).longValue();
	}
}
