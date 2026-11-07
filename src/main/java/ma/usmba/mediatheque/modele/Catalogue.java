package ma.usmba.mediatheque.modele;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class Catalogue {

	public record Ouvrage(String cote, String titre, String auteur, String categorie, boolean disponible) {
	}

	public record Pret(String reference, String ouvrage, String emprunteur, LocalDate echeance, String etat) {
	}

	public List<Ouvrage> getOuvrages() {
		return List.of(
				new Ouvrage("INF-0142", "Architecture des applications Jakarta EE", "N. El Fassi", "Informatique", true),
				new Ouvrage("INF-0187", "Spring Boot en pratique", "H. Berrada", "Informatique", false),
				new Ouvrage("LIT-0033", "Le pain nu", "M. Choukri", "Littérature", true),
				new Ouvrage("HIS-0108", "Histoire du Maroc contemporain", "A. Benjelloun", "Histoire", true),
				new Ouvrage("BIO-0055", "Biologie cellulaire", "S. Amrani", "Sciences", false),
				new Ouvrage("LIT-0091", "Il était une fois un vieux couple heureux", "M. Khair-Eddine", "Littérature", true));
	}

	public List<Pret> getPrets() {
		return List.of(
				new Pret("PR-2024-021", "Spring Boot en pratique", "Yasmine Berrada", LocalDate.of(2024, 12, 20),
						"En cours"),
				new Pret("PR-2024-022", "Le pain nu", "Omar Tazi", LocalDate.of(2024, 12, 18), "En retard"),
				new Pret("PR-2024-023", "Biologie cellulaire", "Salma Idrissi", LocalDate.of(2025, 1, 6), "En cours"),
				new Pret("PR-2024-024", "Architecture des applications Jakarta EE", "Sara Benali",
						LocalDate.of(2024, 12, 27), "Rendu"));
	}

	public long getNombreOuvrages() {
		return getOuvrages().size();
	}

	public long getNombreDisponibles() {
		return getOuvrages().stream().filter(Ouvrage::disponible).count();
	}

	public long getPretsEnRetard() {
		return getPrets().stream().filter(p -> p.etat().equals("En retard")).count();
	}
}
