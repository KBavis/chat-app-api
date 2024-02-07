package com.real.time.chatapp.Config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
		
		//If User is Already Authenticated, Do Not Need To Reset
		if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			//Fetch User Details From DB
			UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
			//Validate JWT Token
			//If Valid, Update SecurityCOntextHolder with Auth Token
			if(jwtService.isTokenValid(jwt, userDetails)) {
				//Needed by Spring and SecurityContextHolder to update our Security Context
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
					userDetails,
					//null credentials
					null,
					userDetails.getAuthorities()
				);
				//Set Details Based On our HTTP request
				authToken.setDetails(
						new WebAuthenticationDetailsSource().buildDetails(request)
				);
				//Update SecurityContextHolder
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		//Need to pass the hand for the next filters to be executed
		filterChain.doFilter(request, response);
	}

}
