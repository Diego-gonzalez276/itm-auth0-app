
package co.edu.itm.auth0.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;
import java.net.URI;

import java.nio.charset.StandardCharsets;

@Service
public class Auth0ManagementService {

    @Value("${auth0.management.domain}")
    private String domain;

    @Value("${auth0.management.client-id}")
    private String clientId;

    @Value("${auth0.management.client-secret}")
    private String clientSecret;

    @Value("${auth0.management.audience}")
    private String audience;

    private final RestClient restClient = RestClient.create();

    private String obtenerToken() {
        MultiValueMap<String, String> datos =
                new LinkedMultiValueMap<>();

        datos.add("grant_type", "client_credentials");
        datos.add("client_id", clientId);
        datos.add("client_secret", clientSecret);
        datos.add("audience", audience);

        Map<?, ?> respuesta = restClient.post()
            .uri("https://" + domain + "/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(datos)
            .retrieve()
            .body(Map.class);

        if (respuesta == null ||
                respuesta.get("access_token") == null) {
            throw new IllegalStateException(
                "Auth0 no devolvió un token de acceso");
        }

        return respuesta.get("access_token").toString();
    }

    public Map<String, Object> consultarMetadata(String userId) {
        String token = obtenerToken();
        
        String idCodificado = UriUtils.encodePathSegment(
                userId, StandardCharsets.UTF_8);

        System.out.println("DOMINIO MANAGEMENT: " + domain);
        System.out.println("USER ID CONSULTADO: " + userId);
        System.out.println("ID CODIFICADO: " + idCodificado);
        
        URI url = URI.create(
        	    "https://" + domain
        	    + "/api/v2/users/" + userId.replace("|", "%7C")
        	);

        	System.out.println("URL CONSULTADA: " + url);

        	Map<?, ?> usuario = restClient.get()
        	    .uri(url)
        	    .headers(h -> h.setBearerAuth(token))
        	    .retrieve()
        	    .body(Map.class);

        if (usuario == null) {
            throw new IllegalStateException(
                "No se pudo consultar el usuario");
        }

        Object metadata = usuario.get("user_metadata");

        if (metadata instanceof Map<?, ?> mapa) {
            Map<String, Object> resultado = new HashMap<>();

            mapa.forEach((clave, valor) ->
                resultado.put(clave.toString(), valor));

            return resultado;
        }

        return new HashMap<>();
    }

    public void actualizarMetadata(
            String userId,
            Map<String, Object> nuevosDatos) {

        String token = obtenerToken();

        String idCodificado = UriUtils.encodePathSegment(
            userId, StandardCharsets.UTF_8);

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("user_metadata", nuevosDatos);

        URI url = URI.create(
        	    "https://" + domain
        	    + "/api/v2/users/" + userId.replace("|", "%7C")
        	);

        	System.out.println("URL DE ACTUALIZACIÓN: " + url);

        	restClient.patch()
        	    .uri(url)
        	    .headers(h -> h.setBearerAuth(token))
        	    .body(Map.of("user_metadata", nuevosDatos))
        	    .retrieve()
        	    .toBodilessEntity();
    }
}
