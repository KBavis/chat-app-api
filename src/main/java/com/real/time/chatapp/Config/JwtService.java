package com.real.time.chatapp.Config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * 
 *@author Kellen Bavis 
 *@date 7/14/2023
 *
 * Service to handle JWT Token Helper Methods 
 */
@Service
public class JwtService {
	
	//TODO: Move to app.props? 
	private static final String SECRET_KEY = "3eceb15aff904ac9facc842800a7891e50a5449bffac94fbca12903412febed6";

	/**
	 * Utilizes JWT Token to extract username
	 * Subject will be the username of our user	
	 * 
	 * @param token 
	 * @return
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
	/**
	 * Generic way to extract a specific claim from a JWT token 
	 * utilizes a "Function" object that knows how to process the Claims object
	 * Allows for flexibility in extracting different types of claims for JWT tokens
	 * 
	 * @param <T>
	 * @param token
	 * @param claimsResolver
	 * @return
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	/**
	 * Determines if a token is valid or not
	 * 
	 * @param token
	 * @param userDetails
	 * @return
	 */
	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String userName = extractUsername(token);
		return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
	
	/**
	 * Determines if a token is expired or not 
	 * 
	 * @param token
	 * @return
	 */
	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	
	/**
	 * Extracts the expiration from a token 
	 * 
	 * @param token
	 * @return
	 */
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
	/**
	 *  parseBulder() used to parse the token
	 *  need to set the sign in key (when we need to generate, encode, or decode a token, we NEED the sign in key)
	 *  build() to build the object
	 *  parseClaimsJws will parse our token
	 *  getBody() will return all of the claims that we have within our token
	 * @param token
	 * @return
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSignInKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	

	/**
	 * Decodes Secret Key
	 * Returns a Key after applying algo on bytes
	 * @return
	 */
	private Key getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	
	/**
	 * Generates Token wtihout Extracted Claims
	 * 
	 * @param userDetails
	 * @return
	 */
	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}
	
	/**
	 * Method to generate a JWT Token based on Extracted Claims 
	 * 
	 * @param extractClaims 	Map contains the extracted claims that we want to add (Authorities, Info, Etc) 
	 * @param userDetails
	 * @return
	 */
	public String generateToken(Map<String,Object> extractClaims, UserDetails userDetails) {
		return Jwts
				.builder()
				.setClaims(extractClaims)
				.setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
				.signWith(getSignInKey(), SignatureAlgorithm.HS256)
				.compact();
	}
}
