package com.example.services;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.exception.EmailAlreadyInUseException;
import com.example.exception.UsernameAlreadyTakenException;
import com.example.spring_security_jwt.model.ERole;
import com.example.spring_security_jwt.model.Role;
import com.example.spring_security_jwt.model.User;
import com.example.spring_security_jwt.payload.request.LogginRequest;
import com.example.spring_security_jwt.payload.request.SignupRequest;
import com.example.spring_security_jwt.payload.response.JwtResponse;
import com.example.spring_security_jwt.payload.response.MessageResponse;
import com.example.spring_security_jwt.repository.RoleRepository;
import com.example.spring_security_jwt.repository.UserRepository;
import com.example.spring_security_jwt.security.jwt.JwtUtils;
import com.example.spring_security_jwt.security.service.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder encoder;
	private final JwtUtils jwtUtils;

	@Override
	public MessageResponse register(SignupRequest signupRequest) {

		if (userRepository.existsByUsername(signupRequest.getUsername())) {
			throw new UsernameAlreadyTakenException("Error: Username is already taken");
		}

		if (userRepository.existsByEmail(signupRequest.getEmail())) {
			throw new EmailAlreadyInUseException("Error: Email is already in use!!!");
		}

		User user = User.builder()
				.username(signupRequest.getUsername())
				.email(signupRequest.getEmail())
				.password(encoder.encode(signupRequest.getPassword()))
				.roles(resolveRoles(signupRequest.getRole()))
				.build();

		userRepository.save(user);

		return new MessageResponse("User registered successfully");
	}

	/**
	 * Se resuelven los roles suministrados en la peticion (request) de registro,
	 * asignando el rol ROLE_USER cuando no se ha indicado ninguno
	 */
	private Set<Role> resolveRoles(Set<String> strRoles) {

		Set<Role> roles = new HashSet<>();

		if (strRoles == null || strRoles.isEmpty()) {
			roles.add(roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role not found")));
			return roles;
		}

		strRoles.forEach(role -> {
			if ("admin".equalsIgnoreCase(role)) {
				roles.add(roleRepository.findByName(ERole.ROLE_ADMIN)
						.orElseThrow(() -> new RuntimeException("Error: Role not found")));
			} else {
				roles.add(roleRepository.findByName(ERole.ROLE_USER)
						.orElseThrow(() -> new RuntimeException("Error: Role not found")));
			}
		});

		return roles;
	}

	@Override
	public JwtResponse login(LogginRequest logginRequest) {

		/**
		 * El AuthenticationManager autentica al usuario suministrando su email (y no
		 * su username) y su password, ya que el UserDetailsService recupera al
		 * usuario por su email
		 */
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(logginRequest.getEmail(),
						logginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		Set<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());

		LOGGER.info("Roles del usuario: {}", roles);

		return JwtResponse.builder()
				.token(jwt)
				.id(userDetails.getId())
				.username(userDetails.getUsername())
				.email(userDetails.getEmail())
				.roles(roles)
				.build();
	}
}
