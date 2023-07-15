package com.real.time.chatapp.Config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		//This header will contian the JWT Token we need
		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		final String username;
		//Check JWT Token --> Each Auth Header Should Start with Bearer, If Not, We Stop 
		if(authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		//Extract token from authHeader
		//We set the starting index of the substring to be 7 
		jwt = authHeader.substring(7);
		
		//Now, we must extract the username
		username = jwtService.extractUsername(jwt);
		
		//Check if the User is authenticated or not 
		//If either of these are null, the user is not authenticated 
		if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
			//If the Token is Valid, We Update the SecurityContextHolder and send a request to the DispatcherServlet
			if(jwtService.isTokenValid(jwt, userDetails)) {
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
					userDetails,
					//null authentication
					null,
					userDetails.getAuthorities()
				);
			}
		}
	}

}
