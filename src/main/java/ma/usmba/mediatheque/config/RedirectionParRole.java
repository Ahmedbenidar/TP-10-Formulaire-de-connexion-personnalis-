package ma.usmba.mediatheque.config;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Redirection conditionnelle : au lieu d'une unique page d'accueil, chaque
 * profil repart directement dans son espace métier.
 */
public class RedirectionParRole implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest requete, HttpServletResponse reponse,
			Authentication authentification) throws IOException, ServletException {

		reponse.sendRedirect(requete.getContextPath() + cible(authentification.getAuthorities()));
	}

	private String cible(Collection<? extends GrantedAuthority> autorites) {
		if (porte(autorites, "ROLE_ADMIN")) {
			return "/espace/administration/tableau-de-bord";
		}
		if (porte(autorites, "ROLE_CONSERVATEUR")) {
			return "/espace/conservation/tableau-de-bord";
		}
		if (porte(autorites, "ROLE_BIBLIOTHECAIRE")) {
			return "/espace/bibliothecaire/tableau-de-bord";
		}
		return "/espace/lecteur/tableau-de-bord";
	}

	private boolean porte(Collection<? extends GrantedAuthority> autorites, String role) {
		return autorites.stream().anyMatch(a -> a.getAuthority().equals(role));
	}
}
