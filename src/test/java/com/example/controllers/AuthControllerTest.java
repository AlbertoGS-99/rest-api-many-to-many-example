package com.example.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

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

import com.example.spring_security_jwt.payload.request.LogginRequest;
import com.example.spring_security_jwt.payload.request.SignupRequest;

import tools.jackson.databind.ObjectMapper;

/**
 * Test de integracion a la capa de controladores de autenticacion
 * (AuthController), teniendo en cuenta la seguridad implementada con Spring
 * Security y JWT.
 *
 * Se comprueba que:
 *
 * 1- Se registran (signup) y se loguean (signin) los usuarios con el email (y
 * no con el username)
 *
 * 2- Cuando el JSON recibido esta mal formado o no supera la validacion, se
 * devuelven TODOS los errores encontrados en la peticion (request)
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	// =====================================================================
	// TEST DE SIGNUP (registro de usuarios)
	// =====================================================================

	@Test
	@DisplayName("Auth Test: registrar un usuario correctamente")
	void testSignupCorrecto() throws Exception {

		SignupRequest signupRequest = SignupRequest.builder()
				.username("usuario_demo1")
				.email("usuario_demo1@test.com")
				.password("Temp2026##")
				.build();

		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("User registered successfully")));
	}

	@Test
	@DisplayName("Auth Test: registrar un usuario con rol ADMIN")
	void testSignupConRolAdmin() throws Exception {

		SignupRequest signupRequest = SignupRequest.builder()
				.username("admin_demo1")
				.email("admin_demo1@test.com")
				.password("Temp2026##")
				.role(Set.of("admin"))
				.build();

		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("User registered successfully")));

		// El usuario recien registrado se puede loguear y su rol es ROLE_ADMIN
		SignInResponse signInResponse = signin("admin_demo1@test.com", "Temp2026##");

		assertThat(signInResponse.roles()).contains("ROLE_ADMIN");
	}

	@Test
	@DisplayName("Auth Test: el registro devuelve TODOS los errores del JSON recibido")
	void testSignupConMultiplesErroresDeValidacion() throws Exception {

		// Username vacio, email sin formato valido y password demasiado corta,
		// es decir, tres errores a la vez en el mismo JSON recibido
		SignupRequest signupRequest = SignupRequest.builder()
				.username("")
				.email("estoNoEsUnEmail")
				.password("123")
				.build();

		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message",
						containsString("el JSON recibido en la peticion de registro no es valido")))
				.andExpect(jsonPath("$.errors.username").exists())
				.andExpect(jsonPath("$.errors.email").exists())
				.andExpect(jsonPath("$.errors.password").exists());
	}

	@Test
	@DisplayName("Auth Test: el registro con un JSON mal formado devuelve 400")
	void testSignupConJsonMalFormado() throws Exception {

		String jsonMalFormado = "{\"username\": \"usuario_demo2\", \"email\": ";

		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMalFormado))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("mal formado")))
				.andExpect(jsonPath("$.errors.body").exists());
	}

	@Test
	@DisplayName("Auth Test: el registro con un username ya existente devuelve 400")
	void testSignupConUsernameDuplicado() throws Exception {

		// admin1 ya ha sido creado por la clase CreatesSamplesData
		SignupRequest signupRequest = SignupRequest.builder()
				.username("admin1")
				.email("otroAdmin@test.com")
				.password("Temp2026##")
				.build();

		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Error: Username is already taken")));
	}

	@Test
	@DisplayName("Auth Test: el registro con un email ya existente devuelve 400")
	void testSignupConEmailDuplicado() throws Exception {

		SignupRequest signupRequest = SignupRequest.builder()
				.username("otroUsuario11")
				.email("admin1@gmail.com")
				.password("Temp2026##")
				.build();

		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Error: Email is already in use!!!")));
	}

	// =====================================================================
	// TEST DE SIGNIN (login de usuarios con el email)
	// =====================================================================

	@Test
	@DisplayName("Auth Test: loguearse con el email y el password correctos")
	void testSigninConEmailCorrecto() throws Exception {

		LogginRequest logginRequest = LogginRequest.builder()
				.email("admin1@gmail.com")
				.password("Temp2026$$##")
				.build();

		mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(logginRequest)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token", not(org.hamcrest.Matchers.emptyOrNullString())))
				.andExpect(jsonPath("$.type", is("Bearer")))
				.andExpect(jsonPath("$.email", is("admin1@gmail.com")))
				.andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.hasItem("ROLE_ADMIN")));
	}

	@Test
	@DisplayName("Auth Test: loguearse con el username y no con el email devuelve error")
	void testSigninConUsernameYNoConEmail() throws Exception {

		// El login se realiza con el email, por lo que el username no es valido
		LogginRequest logginRequest = LogginRequest.builder()
				.email("admin1")
				.password("Temp2026$$##")
				.build();

		mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(logginRequest)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.email").exists());
	}

	@Test
	@DisplayName("Auth Test: loguearse con un password incorrecto devuelve 401")
	void testSigninConPasswordIncorrecto() throws Exception {

		LogginRequest logginRequest = LogginRequest.builder()
				.email("admin1@gmail.com")
				.password("passwordIncorrecto")
				.build();

		mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(logginRequest)))
				.andDo(print())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message",
						containsString("el email o el password son incorrectos")));
	}

	@Test
	@DisplayName("Auth Test: loguearse con un email no registrado devuelve 401")
	void testSigninConEmailNoRegistrado() throws Exception {

		LogginRequest logginRequest = LogginRequest.builder()
				.email("noExiste@gmail.com")
				.password("Temp2026$$##")
				.build();

		mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(logginRequest)))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Auth Test: el login devuelve TODOS los errores del JSON recibido")
	void testSigninConMultiplesErroresDeValidacion() throws Exception {

		// Email en blanco y password en blanco, dos errores a la vez
		LogginRequest logginRequest = LogginRequest.builder()
				.email("")
				.password("")
				.build();

		mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(logginRequest)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message",
						containsString("el JSON recibido en la peticion de login no es valido")))
				.andExpect(jsonPath("$.errors.email").exists())
				.andExpect(jsonPath("$.errors.password").exists());
	}

	@Test
	@DisplayName("Auth Test: el login con un JSON mal formado devuelve 400")
	void testSigninConJsonMalFormado() throws Exception {

		String jsonMalFormado = "{\"email\": \"admin1@gmail.com\", \"password\": ";

		mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonMalFormado))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("mal formado")))
				.andExpect(jsonPath("$.errors.body").exists());
	}

	@Test
	@DisplayName("Auth Test: el login sin cuerpo (body) devuelve 400")
	void testSigninSinBody() throws Exception {

		mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	/**
	 * Metodo auxiliar que realiza el login (signin) con el email y el password
	 * suministrados y devuelve el token JWT junto con los roles del usuario
	 */
	private SignInResponse signin(String email, String password) throws Exception {

		LogginRequest logginRequest = LogginRequest.builder()
				.email(email)
				.password(password)
				.build();

		MvcResult mvcResult = mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(logginRequest)))
				.andExpect(status().isOk())
				.andReturn();

		String body = mvcResult.getResponse().getContentAsString();

		String token = com.jayway.jsonpath.JsonPath.read(body, "$.token");

		@SuppressWarnings("unchecked")
		java.util.List<String> rolesList = com.jayway.jsonpath.JsonPath.read(body, "$.roles");

		return new SignInResponse(token, Set.copyOf(rolesList));
	}

	private record SignInResponse(String token, Set<String> roles) {
	}
}
