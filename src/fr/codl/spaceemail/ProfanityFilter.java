package fr.codl.spaceemail;

public class ProfanityFilter {
	
	private static final String regex = "(?i)\\b(?:"
			+ "shit|fuck|fucked|fucker|fuckers|fucking|motherfucker|"
			+ "motherfucking|motherfuckers|fuckin|goddamn|asshole|"
			+ "nigger|nigga|niggers|cunt|cock|bitch|bitches|slut|sluts|whore|whores)\\b";

	public static String filter(String input){
		return input.replaceAll(regex, "🌚");
	}

}
