package ma.usmba.mediatheque.modele;

public record CompteLecteur(String identifiant, String secretClair, String nomComplet, String fonction,
		String rolePrincipal) {
}
