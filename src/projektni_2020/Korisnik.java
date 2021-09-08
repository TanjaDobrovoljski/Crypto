package projektni_2020;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Korisnik {


	public static ArrayList<Korisnik> kreiraniNalozi = new ArrayList<Korisnik>();
	public static final File nalozi = new File("nalozi.txt"); // putanja do kreiranih naloga
	public File privatniKljuc;
	public static Korisnik trenutniPrijavljeni = null;
	
	public String ime;
	String hashLozinke;
	public File putanjaDoSertifikata;
	public BufferedImage slikaZaSteg;
	public File putanjaDoSlike;
	File inbox;
	public ArrayList<File> putanjeDoSlika = new ArrayList<File>();
	Korisnik()
	{
		this.ime ="";
		this.hashLozinke="";
	}
	
	public Korisnik(String ime,String lozinka)
	{
		this.ime = ime;
		this.hashLozinke = lozinka;
		this.putanjaDoSertifikata = new File("digitalniSertifikatiKorisnika"+File.separator+this.ime+".pem");
		this.privatniKljuc=new File("kljucevi"+File.separator+this.ime+".key");
	}
	
	public Korisnik(String ime)
	{
		this.ime = ime;	
		this.putanjaDoSertifikata = new File("digitalniSertifikatiKorisnika"+File.separator+this.ime+".pem");
		this.privatniKljuc=new File("kljucevi"+File.separator+this.ime+".key");
	}
	
	public String getIme()
	{
		return ime;
	}
	
	String getLozinka()
	{
		return hashLozinke;
	}
	
	public static Korisnik ispravnaPrijava(String unesenoIme,String unesenaSifra)
	{
		for(Korisnik k : kreiraniNalozi)
		{
			
			if(unesenoIme.equals(k.getIme()) && unesenaSifra.equals(k.getLozinka())) {return k;}
		}
		return null;
	}
	
	
	
	public static void procitajInboxPrijavljenog() throws NoSuchAlgorithmException
	{
		File fajlInbox = new File(Korisnik.trenutniPrijavljeni.ime + ".txt");
		String linija;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fajlInbox));
			while((linija = reader.readLine()) != null)
			{
				String putanja = linija.split(" ")[0];
				String hashFajla = linija.split(" ")[1];
				if(hashFajla.equals(HashRacunanje.getHash(Files.readAllBytes(new File(putanja).toPath()))))
				{
					Korisnik.trenutniPrijavljeni.putanjeDoSlika.add(new File(putanja));
				
				}
			
			}	
			
		}
		catch (FileNotFoundException e) {} 
		catch (IOException e) {	}
		
		
	}
	public static void procitajNaloge() throws IOException 
	{
		BufferedReader reader = new BufferedReader(new FileReader(Korisnik.nalozi));
		String linija;
		while((linija = reader.readLine()) != null)
		{
			String ime = linija.split(" ")[0];
			String hashLozinke = linija.split(" ")[1];
			Korisnik.kreiraniNalozi.add(new Korisnik(ime,hashLozinke));
		}
	}
	
	
	
}



