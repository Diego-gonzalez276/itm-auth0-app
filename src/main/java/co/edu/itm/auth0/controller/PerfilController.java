
package co.edu.itm.auth0.controller;

import java.util.HashMap;
import java.util.Map;

import co.edu.itm.auth0.dto.DatosPersonalesDTO;
import co.edu.itm.auth0.service.Auth0ManagementService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PerfilController {

    private final Auth0ManagementService servicio;

    public PerfilController(Auth0ManagementService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(
            @AuthenticationPrincipal OidcUser usuario,
            Model model) {

        System.out.println("SUBJECT DE AUTH0: "
                + usuario.getSubject());
        
        Map<String, Object> metadata =
            servicio.consultarMetadata(usuario.getSubject());

        DatosPersonalesDTO datos = new DatosPersonalesDTO();

        datos.setTipoDocumento(
            valor(metadata, "tipoDocumento"));
        datos.setNumeroDocumento(
            valor(metadata, "numeroDocumento"));
        datos.setDireccion(
            valor(metadata, "direccion"));
        datos.setTelefono(
            valor(metadata, "telefono"));

        model.addAttribute("datos", datos);

        return "perfil";
    }

    @PostMapping("/perfil")
    public String guardarPerfil(
            @AuthenticationPrincipal OidcUser usuario,
            @Valid @ModelAttribute("datos")
            DatosPersonalesDTO datos,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return "perfil";
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tipoDocumento", datos.getTipoDocumento());
        metadata.put("numeroDocumento", datos.getNumeroDocumento());
        metadata.put("direccion", datos.getDireccion());
        metadata.put("telefono", datos.getTelefono());

        servicio.actualizarMetadata(
            usuario.getSubject(), metadata);

        redirectAttributes.addFlashAttribute(
            "mensaje", "Datos actualizados correctamente");

        return "redirect:/perfil";
    }

    private String valor(
            Map<String, Object> metadata, String clave) {

        Object valor = metadata.get(clave);
        return valor == null ? "" : valor.toString();
    }
}
